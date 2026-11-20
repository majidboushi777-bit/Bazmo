package com.example.data

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.Types
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicBoolean

sealed interface EnsureProfileResult {
    data class Ready(val user: UserEntity, val promptProfile: Boolean) : EnsureProfileResult
    data object NotInvited : EnsureProfileResult
    data object Blocked : EnsureProfileResult
}

@Keep
data class BazmoDbState(
    val showDemoData: Boolean = true,
    val users: List<UserEntity> = emptyList(),
    val registrationRequests: List<RegistrationRequestEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val stores: List<StoreEntity> = emptyList(),
    val mobiles: List<MobileEntity> = emptyList(),
    val storeMobiles: List<StoreMobileEntity> = emptyList(),
    val accessories: List<AccessoryEntity> = emptyList(),
    val inquiries: List<InquiryEntity> = emptyList(),
    val swProblems: List<SwProblemEntity> = emptyList(),
    val tutorials: List<TutorialEntity> = emptyList(),
    val swRequests: List<SwRequestEntity> = emptyList(),
    val boxes: List<BoxEntity> = emptyList(),
    val hwCategories: List<HwCategoryEntity> = emptyList(),
    val hwAds: List<HwAdEntity> = emptyList(),
    val hwRequests: List<HwRequestEntity> = emptyList(),
    val orders: List<OrderEntity> = emptyList(),
    val chatThreads: List<ChatThreadEntity> = emptyList(),
    val forumPosts: List<ForumPostEntity> = emptyList(),
    val deskRentals: List<DeskRentalEntity> = emptyList(),
    val deskRequests: List<DeskRequestEntity> = emptyList(),
    val banners: List<BannerEntity> = emptyList()
)

private sealed interface FetchStateOutcome {
    data class Success(val state: BazmoDbState, val etag: String?) : FetchStateOutcome
    data class NotModified(val etag: String?) : FetchStateOutcome
}

@Keep
internal data class AdminUsersWrapper(val users: List<UserEntity> = emptyList())

@Keep
internal data class UserWrapper(val user: UserEntity? = null)

@Keep
internal data class NotificationsWrapper(val notifications: List<NotificationEntity> = emptyList())

class BazmoRepository(
    private val baseUrl: String = BazmoServerConfig.baseUrl(),
    private val authToken: String,
    private val currentUid: String
) {
    private val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    private val client = BazmoServerConfig.okHttpClient
    private val moshi = BazmoServerConfig.moshi

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 32)
    private val wsWakeTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    private val adminUsersSyncFlow = MutableSharedFlow<List<UserEntity>>(extraBufferCapacity = 16)
    @Volatile
    private var lastStateEtag: String? = null

    private fun normalizeDbState(state: BazmoDbState): BazmoDbState = state.copy(
        users = state.users.sortedBy { it.fullName },
        registrationRequests = state.registrationRequests.sortedWith(
            compareBy<RegistrationRequestEntity> { it.status != "pending" }
                .thenByDescending { it.createdAtSeconds }
        ),
        notifications = state.notifications.sortedByDescending { it.sortOrder },
        stores = state.stores.sortedBy { it.id },
        mobiles = state.mobiles.sortedBy { it.sortOrder },
        storeMobiles = state.storeMobiles.sortedBy { it.price },
        accessories = state.accessories.sortedBy { it.id },
        inquiries = state.inquiries.sortedByDescending { it.sortOrder },
        swProblems = state.swProblems.sortedBy { it.id },
        tutorials = state.tutorials.sortedBy { it.id },
        swRequests = state.swRequests.sortedByDescending { it.sortOrder },
        boxes = state.boxes.sortedBy { it.id },
        hwCategories = state.hwCategories.sortedBy { it.id },
        hwAds = state.hwAds.sortedByDescending { it.sortOrder },
        hwRequests = state.hwRequests.sortedByDescending { it.sortOrder },
        orders = state.orders.sortedByDescending { it.sortOrder },
        forumPosts = state.forumPosts.sortedByDescending { it.createdAtMs },
        deskRentals = state.deskRentals.sortedByDescending { it.sortOrder },
        deskRequests = state.deskRequests.sortedByDescending { it.sortOrder },
        banners = state.banners.sortedWith(compareBy<BannerEntity> { it.section }.thenBy { it.slotIndex })
    )

    suspend fun loadCachedDbState(context: Context): BazmoDbState? = withContext(Dispatchers.IO) {
        runCatching {
            val safeUid = currentUid.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "default" }
            val file = File(context.applicationContext.filesDir, "bazmo_cache_$safeUid.json")
            if (!file.exists() || file.length() <= 0L) return@withContext null
            val raw = file.readText(Charsets.UTF_8)
            moshi.adapter(BazmoDbState::class.java).fromJson(raw)?.let(::normalizeDbState)
        }.getOrNull()
    }

    suspend fun saveCachedDbState(context: Context, state: BazmoDbState) = withContext(Dispatchers.IO) {
        runCatching {
            val safeUid = currentUid.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "default" }
            val file = File(context.applicationContext.filesDir, "bazmo_cache_$safeUid.json")
            val json = moshi.adapter(BazmoDbState::class.java).toJson(state)
            file.writeText(json, Charsets.UTF_8)
        }
    }

    fun triggerRefresh(wakeSocket: Boolean = false, clearEtag: Boolean = false) {
        if (clearEtag) lastStateEtag = null
        refreshTrigger.tryEmit(Unit)
        if (wakeSocket) {
            wsWakeTrigger.tryEmit(Unit)
        }
    }

    internal fun sanitizeAsciiEtag(value: String?): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        val isAscii = trimmed.all { it == '\t' || it in '\u0020'..'\u007e' }
        return if (isAscii) trimmed else null
    }

    private suspend fun fetchStateFromServer(): Result<FetchStateOutcome> = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder()
                .url("$normalizedBaseUrl/api/state")
                .header("Authorization", "Bearer $authToken")
                .get()
            val knownEtag = sanitizeAsciiEtag(lastStateEtag)
            if (knownEtag != null) {
                reqBuilder.header("If-None-Match", knownEtag)
            } else {
                lastStateEtag = null
            }
            client.newCall(reqBuilder.build()).execute().use { res ->
                val etagHeader = sanitizeAsciiEtag(res.header("ETag")) ?: knownEtag
                if (res.code == 304) {
                    return@withContext Result.success(FetchStateOutcome.NotModified(etagHeader))
                }
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    val msg = extractError(body, "خطا در دریافت اطلاعات از سرور")
                    return@withContext Result.failure(BazmoApiException(res.code, msg))
                }
                val parsed = moshi.adapter(BazmoDbState::class.java).fromJson(body)
                    ?: return@withContext Result.failure(BazmoApiException(500, "ساختار داده سرور نامعتبر است."))
                lastStateEtag = etagHeader
                Result.success(FetchStateOutcome.Success(normalizeDbState(parsed), etagHeader))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleApiError(e, OperationType.LIST, "/api/state", currentUid)
        }
    }

    suspend fun fetchUnreadNotifications(
        province: String,
        city: String,
        region: String
    ): Result<List<NotificationEntity>> = withContext(Dispatchers.IO) {
        try {
            val url = "$normalizedBaseUrl/api/notifications/unread" +
                "?province=${urlEncode(province)}&city=${urlEncode(city)}&region=${urlEncode(region)}"
            val req = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $authToken")
                .get()
                .build()
            client.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    return@withContext Result.failure(
                        BazmoApiException(res.code, extractError(body, "خطا در بررسی اعلان‌های پس‌زمینه"))
                    )
                }
                val list = moshi.adapter(NotificationsWrapper::class.java).fromJson(body)?.notifications.orEmpty()
                Result.success(list)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleApiError(e, OperationType.LIST, "/api/notifications/unread", currentUid)
        }
    }

    fun observeDbState(isAdmin: Boolean, userTier: String?): Flow<BazmoDbState> = callbackFlow {
        var latestState: BazmoDbState? = null
        val wsConnected = AtomicBoolean(false)

        suspend fun reloadState(throwOnInitialFailure: Boolean = false) {
            val res = fetchStateFromServer()
            res.onSuccess { outcome ->
                when (outcome) {
                    is FetchStateOutcome.Success -> {
                        latestState = outcome.state
                        trySend(outcome.state)
                    }
                    is FetchStateOutcome.NotModified -> {
                        if (latestState == null) {
                            lastStateEtag = null
                            val retryRes = fetchStateFromServer()
                            retryRes.onSuccess { retryOutcome ->
                                if (retryOutcome is FetchStateOutcome.Success) {
                                    latestState = retryOutcome.state
                                    trySend(retryOutcome.state)
                                }
                            }.onFailure { err ->
                                if (throwOnInitialFailure && latestState == null) {
                                    close(err)
                                }
                            }
                        }
                    }
                }
            }.onFailure { err ->
                if (throwOnInitialFailure && latestState == null) {
                    close(err)
                }
            }
        }

        // Initial load
        reloadState(throwOnInitialFailure = true)

        // Manual refresh & service sync bus listeners
        val refreshJob = launch {
            refreshTrigger.collect {
                reloadState(throwOnInitialFailure = false)
            }
        }
        val busSyncJob = launch {
            BazmoSyncBus.syncEvents.collect {
                refreshTrigger.tryEmit(Unit)
            }
        }

        // Immediate admin users cache patch if triggered manually
        val adminSyncJob = if (isAdmin) {
            launch {
                adminUsersSyncFlow.collect { usersList ->
                    val current = latestState ?: return@collect
                    val patched = current.copy(users = usersList.sortedBy { it.fullName })
                    latestState = patched
                    trySend(patched)
                }
            }
        } else null

        // Real-time WebSocket listener with fast reconnect and resume wake
        val wsJob = launch(Dispatchers.IO) {
            var activeWs: WebSocket? = null
            var backoffMs = 3_000L
            val wsEndpoint = "${BazmoServerConfig.wsUrl(normalizedBaseUrl)}?token=${urlEncode(authToken)}"
            try {
                while (isActive) {
                    val closedSignal = kotlinx.coroutines.CompletableDeferred<Unit>()
                    val wsReq = Request.Builder()
                        .url(wsEndpoint)
                        .build()
                    activeWs = client.newWebSocket(wsReq, object : WebSocketListener() {
                        override fun onOpen(webSocket: WebSocket, response: Response) {
                            wsConnected.set(true)
                            backoffMs = 3_000L
                            refreshTrigger.tryEmit(Unit)
                        }

                        override fun onMessage(webSocket: WebSocket, text: String) {
                            if (text.contains("\"sync\"")) {
                                refreshTrigger.tryEmit(Unit)
                            }
                        }

                        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                            wsConnected.set(false)
                            closedSignal.complete(Unit)
                        }

                        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                            wsConnected.set(false)
                            closedSignal.complete(Unit)
                        }
                    })
                    closedSignal.await()
                    kotlinx.coroutines.withTimeoutOrNull(backoffMs) {
                        wsWakeTrigger.first()
                    }
                    backoffMs = (backoffMs * 2).coerceAtMost(20_000L)
                }
            } finally {
                wsConnected.set(false)
                activeWs?.cancel()
            }
        }

        // Server-Sent Events (/api/events) ONLY as fallback when WebSocket is disconnected
        val sseJob = launch(Dispatchers.IO) {
            val sseClient = client.newBuilder()
                .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
            var backoffMs = 4_000L
            delay(3_500L)
            while (isActive) {
                if (wsConnected.get()) {
                    delay(5_000L)
                    continue
                }
                var supported = true
                try {
                    val req = Request.Builder()
                        .url("$normalizedBaseUrl/api/events?token=${urlEncode(authToken)}")
                        .header("Accept", "text/event-stream")
                        .get()
                        .build()
                    sseClient.newCall(req).execute().use { res ->
                        val ct = res.header("Content-Type").orEmpty().lowercase()
                        if (!res.isSuccessful || !ct.contains("text/event-stream")) {
                            supported = false
                        } else {
                            backoffMs = 4_000L
                            val source = res.body?.source()
                            while (isActive && source != null && !wsConnected.get()) {
                                val line = source.readUtf8Line() ?: break
                                if (line.startsWith("data:") && line.contains("\"sync\"")) {
                                    refreshTrigger.tryEmit(Unit)
                                }
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Transient network failure; retry
                }
                if (!supported) break
                kotlinx.coroutines.withTimeoutOrNull(backoffMs) {
                    wsWakeTrigger.first()
                }
                backoffMs = (backoffMs * 2).coerceAtMost(25_000L)
            }
        }

        // Low-overhead fallback polling (60s when WS active, 15s when offline/polling)
        val pollJob = launch {
            var ticks = 0
            while (isActive) {
                delay(if (wsConnected.get()) 60_000L else 15_000L)
                ticks++
                val knownIds = latestState?.notifications?.map { it.id }?.toSet().orEmpty()
                val me = latestState?.users?.find { it.uid == currentUid || it.id == currentUid }
                val unreadRes = fetchUnreadNotifications(
                    province = me?.province?.ifBlank { "تهران" } ?: "تهران",
                    city = me?.city?.ifBlank { "تهران" } ?: "تهران",
                    region = me?.region?.ifBlank { "همه مناطق" } ?: "همه مناطق"
                )
                val hasNewNotif = unreadRes.getOrNull()?.any { it.id.isNotBlank() && it.id !in knownIds } == true
                if (hasNewNotif || ticks >= 2) {
                    ticks = 0
                    reloadState(throwOnInitialFailure = false)
                }
            }
        }

        awaitClose {
            refreshJob.cancel()
            busSyncJob.cancel()
            adminSyncJob?.cancel()
            wsJob.cancel()
            sseJob.cancel()
            pollJob.cancel()
        }
    }

    suspend fun checkIsAdminDoc(uid: String): Boolean {
        BazmoAuthManager.peekCachedAuthResponse(uid, authToken)?.let { return it.isAdmin }
        val sessionRes = BazmoAuthManager.verifySession(authToken)
        return sessionRes.getOrNull()?.isAdmin ?: false
    }

    suspend fun ensureUserProfile(
        uid: String,
        email: String,
        displayName: String?,
        isAdmin: Boolean
    ): Result<EnsureProfileResult> {
        val cached = BazmoAuthManager.peekCachedAuthResponse(uid, authToken)
        val sessionRes = if (cached != null) Result.success(cached) else BazmoAuthManager.verifySession(authToken)
        return sessionRes.fold(
            onSuccess = { resp ->
                when (resp.status) {
                    "ready" -> {
                        val user = resp.user ?: UserEntity(
                            id = uid,
                            uid = uid,
                            email = normalizeEmail(email),
                            fullName = displayName?.ifBlank { "همکار بازمو" } ?: "همکار بازمو",
                            tier = if (resp.isAdmin) "ادمین" else BazmoRoles.REPAIR_TECH,
                            active = true
                        )
                        Result.success(EnsureProfileResult.Ready(user, promptProfile = false))
                    }
                    "blocked" -> Result.success(EnsureProfileResult.Blocked)
                    else -> Result.success(EnsureProfileResult.NotInvited)
                }
            },
            onFailure = { err ->
                handleApiError(err, OperationType.GET, "/api/auth/session", currentUid)
            }
        )
    }

    suspend fun fetchOwnUserProfile(uid: String): Result<UserEntity?> {
        val sessionRes = BazmoAuthManager.verifySession(authToken)
        return sessionRes.fold(
            onSuccess = { resp ->
                when (resp.status) {
                    "ready" -> Result.success(resp.user)
                    "blocked" -> Result.success(resp.user?.copy(active = false) ?: UserEntity(id = uid, uid = uid, active = false))
                    else -> Result.success(null)
                }
            },
            onFailure = { err ->
                handleApiError(err, OperationType.GET, "/api/auth/session", currentUid)
            }
        )
    }

    suspend fun updateOwnProfile(
        uid: String,
        fullName: String,
        phone: String
    ): Result<Unit> = executeJsonMutation(
        method = "PATCH",
        path = "/api/users/me",
        operationType = OperationType.UPDATE,
        bodyMap = mapOf(
            "fullName" to fullName,
            "phone" to phone
        )
    )

    suspend fun seedInitialMarketplaceIfEmpty(adminUid: String): Result<Boolean> =
        Result.success(false) // Handled automatically on Weblines server startup & admin session

    suspend fun resetAllToDefaults(adminUid: String): Result<Unit> = executeJsonMutation(
        method = "POST",
        path = "/api/admin/reset-defaults",
        operationType = OperationType.WRITE,
        bodyMap = emptyMap(),
        useLongTimeout = true
    )

    // ==================== MUTATIONS ====================

    suspend fun fetchUsersForAdmin(): Result<List<UserEntity>> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("$normalizedBaseUrl/api/admin/users")
                .header("Authorization", "Bearer $authToken")
                .get()
                .build()
            client.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    return@withContext Result.failure(
                        BazmoApiException(res.code, extractError(body, "خطا در دریافت لیست کاربران"))
                    )
                }
                val list = moshi.adapter(AdminUsersWrapper::class.java).fromJson(body)?.users
                    .orEmpty()
                    .sortedBy { it.fullName }
                adminUsersSyncFlow.tryEmit(list)
                Result.success(list)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleApiError(e, OperationType.LIST, "/api/admin/users", currentUid)
        }
    }

    suspend fun createUserByAdmin(user: UserEntity, password: String = ""): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val payload = mutableMapOf<String, Any?>(
                "id" to user.id,
                "uid" to user.uid,
                "email" to normalizeEmail(user.email).take(200),
                "fullName" to user.fullName.trim().take(120),
                "phone" to user.phone.trim().take(40),
                "tier" to BazmoRoles.normalizeTier(user.tier),
                "province" to user.province.trim().ifBlank { "تهران" }.take(80),
                "city" to user.city.trim().ifBlank { "تهران" }.take(80),
                "region" to user.region.trim().ifBlank { "همه مناطق" }.take(80),
                "createdDateLabel" to user.createdDateLabel.ifBlank { "امروز" }
            )
            if (password.isNotEmpty()) {
                payload["password"] = password
            }
            val json = mapToJson(payload)
            val req = Request.Builder()
                .url("$normalizedBaseUrl/api/admin/users")
                .header("Authorization", "Bearer $authToken")
                .post(json.toRequestBody(BazmoServerConfig.JSON_MEDIA_TYPE))
                .build()
            client.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    return@withContext Result.failure(
                        BazmoApiException(res.code, extractError(body, "خطا در تعریف کاربر"))
                    )
                }
                val savedUser = moshi.adapter(UserWrapper::class.java).fromJson(body)?.user ?: user
                fetchUsersForAdmin()
                triggerRefresh()
                Result.success(savedUser)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleApiError(e, OperationType.CREATE, "/api/admin/users", currentUid)
        }
    }

    suspend fun setUserActiveByAdmin(user: UserEntity, active: Boolean): Result<Unit> {
        val targetId = user.id.ifBlank { user.uid }
        val encodedId = urlEncode(targetId)
        return executeJsonMutation(
            method = "PATCH",
            path = "/api/admin/users/$encodedId/active",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf(
                "active" to active,
                "email" to normalizeEmail(user.email),
                "user" to user.toCreateMap()
            )
        ).also { if (it.isSuccess) fetchUsersForAdmin() }
    }

    suspend fun setUserActiveByAdmin(userId: String, email: String, active: Boolean): Result<Unit> =
        setUserActiveByAdmin(
            user = UserEntity(id = userId, uid = userId, email = email, active = !active),
            active = active
        )

    suspend fun updateUserTierByAdmin(userId: String, email: String, newTier: String): Result<Unit> {
        val encodedId = urlEncode(userId)
        return executeJsonMutation(
            method = "PATCH",
            path = "/api/admin/users/$encodedId/tier",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf(
                "tier" to BazmoRoles.normalizeTier(newTier),
                "email" to normalizeEmail(email)
            )
        ).also { if (it.isSuccess) fetchUsersForAdmin() }
    }

    suspend fun setUserPasswordByAdmin(userId: String, email: String, password: String): Result<Unit> {
        val encodedId = urlEncode(userId)
        return executeJsonMutation(
            method = "PATCH",
            path = "/api/admin/users/$encodedId/password",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf(
                "email" to normalizeEmail(email),
                "password" to password
            )
        )
    }

    suspend fun updateUserLocationByAdmin(
        userId: String,
        email: String,
        province: String,
        city: String,
        region: String,
        fullName: String? = null,
        phone: String? = null
    ): Result<Unit> {
        val encodedId = urlEncode(userId)
        val body = buildMap<String, Any?> {
            put("email", normalizeEmail(email))
            put("province", province.trim().ifBlank { "تهران" }.take(80))
            put("city", city.trim().ifBlank { "تهران" }.take(80))
            put("region", region.trim().ifBlank { "همه مناطق" }.take(80))
            if (fullName != null) put("fullName", fullName.trim().take(120))
            if (phone != null) put("phone", phone.trim().take(40))
        }
        return executeJsonMutation(
            method = "PATCH",
            path = "/api/admin/users/$encodedId/location",
            operationType = OperationType.UPDATE,
            bodyMap = body
        ).also { if (it.isSuccess) fetchUsersForAdmin() }
    }

    suspend fun upsertBannerByAdmin(
        section: String,
        slotIndex: Int,
        title: String,
        linkType: String,
        linkTarget: String,
        imageMime: String,
        imageData: String,
        keepExistingImage: Boolean
    ): Result<Unit> = executeJsonMutation(
        method = "POST",
        path = "/api/admin/banners",
        operationType = OperationType.WRITE,
        bodyMap = mapOf(
            "section" to section,
            "slotIndex" to slotIndex,
            "title" to title.trim().take(120),
            "linkType" to linkType,
            "linkTarget" to linkTarget.trim().take(500),
            "imageMime" to imageMime,
            "imageData" to imageData,
            "keepExistingImage" to keepExistingImage
        )
    )

    suspend fun deleteBannerByAdmin(id: String): Result<Unit> =
        executeJsonMutation(
            method = "DELETE",
            path = "/api/admin/banners/${urlEncode(id)}",
            operationType = OperationType.DELETE,
            bodyMap = null
        )

    suspend fun approveRegistrationRequest(
        req: RegistrationRequestEntity,
        assignedTier: String,
        existingUser: UserEntity?
    ): Result<Unit> {
        val encodedId = urlEncode(req.id)
        return executeJsonMutation(
            method = "POST",
            path = "/api/admin/registration-requests/$encodedId/approve",
            operationType = OperationType.WRITE,
            bodyMap = mapOf(
                "assignedTier" to BazmoRoles.normalizeTier(assignedTier),
                "request" to req.toCreateMap()
            )
        ).also { if (it.isSuccess) fetchUsersForAdmin() }
    }

    suspend fun rejectRegistrationRequest(reqId: String): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/admin/registration-requests/${urlEncode(reqId)}/reject",
            operationType = OperationType.UPDATE,
            bodyMap = emptyMap()
        )

    suspend fun deleteUser(id: String, email: String): Result<Unit> =
        executeJsonMutation(
            method = "DELETE",
            path = "/api/admin/users/${urlEncode(id)}?email=${urlEncode(normalizeEmail(email))}",
            operationType = OperationType.DELETE,
            bodyMap = null
        ).also { if (it.isSuccess) fetchUsersForAdmin() }

    suspend fun createNotification(notif: NotificationEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/notifications",
            operationType = OperationType.CREATE,
            bodyMap = notif.toCreateMap() + ("id" to notif.id)
        )

    suspend fun markNotificationRead(notifId: String, uid: String): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/notifications/${urlEncode(notifId)}/read",
            operationType = OperationType.UPDATE,
            bodyMap = emptyMap()
        )

    suspend fun deleteNotification(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/notifications/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun setDemoVisibility(visible: Boolean): Result<Unit> = executeJsonMutation(
        method = "POST",
        path = "/api/admin/demo-visibility",
        operationType = OperationType.WRITE,
        bodyMap = mapOf("visible" to visible)
    )

    suspend fun seedDemoDataset(): Result<Unit> = executeJsonMutation(
        method = "POST",
        path = "/api/admin/seed-demo",
        operationType = OperationType.WRITE,
        bodyMap = emptyMap(),
        useLongTimeout = true
    )

    suspend fun createStore(store: StoreEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/stores",
            operationType = OperationType.CREATE,
            bodyMap = store.toCreateMap() + ("id" to store.id)
        )

    suspend fun updateStoreStatus(id: String, status: String): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/stores/${urlEncode(id)}/status",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("status" to status)
        )

    suspend fun updateStore(
        id: String,
        name: String,
        phone: String,
        address: String,
        hours: String
    ): Result<Unit> = executeJsonMutation(
        method = "PATCH",
        path = "/api/stores/${urlEncode(id)}",
        operationType = OperationType.UPDATE,
        bodyMap = mapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "hours" to hours
        )
    )

    suspend fun deleteStore(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/stores/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun createMobileWithOffer(mobile: MobileEntity, offer: StoreMobileEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/mobiles",
            operationType = OperationType.CREATE,
            bodyMap = mapOf(
                "mobile" to (mobile.toCreateMap() + ("id" to mobile.id)),
                "offer" to (offer.toCreateMap() + ("id" to offer.id))
            )
        )

    suspend fun createStoreMobile(
        storeId: String,
        mobileId: String,
        price: Long,
        info: String,
        newBrand: String = "",
        newModel: String = "",
        newSpecs: String = ""
    ): Result<Unit> = executeJsonMutation(
        method = "POST",
        path = "/api/store-mobiles",
        operationType = OperationType.CREATE,
        bodyMap = mapOf(
            "storeId" to storeId,
            "mobileId" to mobileId,
            "price" to price,
            "info" to info,
            "newMobile" to if (mobileId.isBlank() && newModel.isNotBlank()) {
                mapOf(
                    "brand" to newBrand.ifBlank { "سایر" },
                    "model" to newModel,
                    "specs" to newSpecs
                )
            } else null
        )
    )

    suspend fun updateStoreMobile(id: String, price: Long, info: String): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/store-mobiles/${urlEncode(id)}",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("price" to price, "info" to info)
        )

    suspend fun deleteStoreMobile(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/store-mobiles/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun createAccessory(acc: AccessoryEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/accessories",
            operationType = OperationType.CREATE,
            bodyMap = acc.toCreateMap() + ("id" to acc.id)
        )

    suspend fun updateAccessory(id: String, price: Long, desc: String? = null): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/accessories/${urlEncode(id)}",
            operationType = OperationType.UPDATE,
            bodyMap = buildMap {
                put("price", price)
                if (desc != null) put("desc", desc)
            }
        )

    suspend fun deleteAccessory(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/accessories/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun createInquiry(inq: InquiryEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/inquiries",
            operationType = OperationType.CREATE,
            bodyMap = inq.toCreateMap() + ("id" to inq.id)
        )

    suspend fun updateInquiryStatus(id: String, status: String): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/inquiries/${urlEncode(id)}/status",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("status" to status)
        )

    suspend fun createTutorial(tut: TutorialEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/tutorials",
            operationType = OperationType.CREATE,
            bodyMap = tut.toCreateMap() + ("id" to tut.id)
        )

    suspend fun approveTutorial(id: String): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/tutorials/${urlEncode(id)}/approve",
            operationType = OperationType.UPDATE,
            bodyMap = emptyMap()
        )

    suspend fun deleteTutorial(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/tutorials/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun createSwRequest(req: SwRequestEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/sw-requests",
            operationType = OperationType.CREATE,
            bodyMap = req.toCreateMap() + ("id" to req.id)
        )

    suspend fun replySwRequest(id: String, reply: String): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/sw-requests/${urlEncode(id)}/reply",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("reply" to reply)
        )

    suspend fun createBox(box: BoxEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/boxes",
            operationType = OperationType.CREATE,
            bodyMap = box.toCreateMap() + ("id" to box.id)
        )

    suspend fun approveBox(id: String): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/boxes/${urlEncode(id)}/approve",
            operationType = OperationType.UPDATE,
            bodyMap = emptyMap()
        )

    suspend fun deleteBox(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/boxes/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun createHwAd(ad: HwAdEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/hw-ads",
            operationType = OperationType.CREATE,
            bodyMap = ad.toCreateMap() + ("id" to ad.id)
        )

    suspend fun updateHwAdPrice(id: String, price: Long): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/hw-ads/${urlEncode(id)}/price",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("price" to price)
        )

    suspend fun updateHwAdStatus(id: String, status: String): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/hw-ads/${urlEncode(id)}/status",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("status" to status)
        )

    suspend fun deleteHwAd(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/hw-ads/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun createHwRequest(req: HwRequestEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/hw-requests",
            operationType = OperationType.CREATE,
            bodyMap = req.toCreateMap() + ("id" to req.id)
        )

    suspend fun replyHwRequest(id: String, note: String): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/hw-requests/${urlEncode(id)}/reply",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("note" to note)
        )

    suspend fun createOrder(order: OrderEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/orders",
            operationType = OperationType.CREATE,
            bodyMap = order.toCreateMap() + ("id" to order.id)
        )

    suspend fun updateOrderStatus(id: String, status: String): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/orders/${urlEncode(id)}/status",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("status" to status)
        )

    suspend fun deleteOrder(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/orders/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun sendChatMessage(
        threadId: String,
        adId: String,
        buyerUid: String,
        sellerUid: String,
        existingMessages: List<ChatMessage>,
        newMessage: ChatMessage
    ): Result<Unit> = executeJsonMutation(
        method = "POST",
        path = "/api/chats/${urlEncode(threadId)}/messages",
        operationType = OperationType.WRITE,
        bodyMap = mapOf(
            "adId" to adId,
            "buyerUid" to buyerUid,
            "sellerUid" to sellerUid,
            "newMessage" to newMessage.toMap()
        )
    )

    suspend fun createForumPost(post: ForumPostEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/forum-posts",
            operationType = OperationType.CREATE,
            bodyMap = post.toCreateMap() + ("id" to post.id)
        )

    suspend fun updateForumPostInteraction(
        postId: String,
        votes: Int,
        upvotedBy: List<String>,
        downvotedBy: List<String>,
        savedBy: List<String>,
        comments: List<ForumComment>
    ): Result<Unit> = executeJsonMutation(
        method = "PATCH",
        path = "/api/forum-posts/${urlEncode(postId)}/interaction",
        operationType = OperationType.UPDATE,
        bodyMap = mapOf(
            "votes" to votes,
            "upvotedBy" to upvotedBy,
            "downvotedBy" to downvotedBy,
            "savedBy" to savedBy,
            "comments" to comments.map { it.toMap() }
        )
    )

    suspend fun updateForumPostModeration(
        postId: String,
        pinned: Boolean,
        verifiedSolution: Boolean
    ): Result<Unit> = executeJsonMutation(
        method = "PATCH",
        path = "/api/forum-posts/${urlEncode(postId)}/moderation",
        operationType = OperationType.UPDATE,
        bodyMap = mapOf("pinned" to pinned, "verifiedSolution" to verifiedSolution)
    )

    suspend fun deleteForumPost(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/forum-posts/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun createDeskRental(rental: DeskRentalEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/desk-rentals",
            operationType = OperationType.CREATE,
            bodyMap = rental.toCreateMap() + ("id" to rental.id)
        )

    suspend fun updateDeskRentalStatus(id: String, status: String): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/desk-rentals/${urlEncode(id)}/status",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("status" to status)
        )

    suspend fun deleteDeskRental(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/desk-rentals/${urlEncode(id)}", OperationType.DELETE, null)

    suspend fun createDeskRequest(req: DeskRequestEntity): Result<Unit> =
        executeJsonMutation(
            method = "POST",
            path = "/api/desk-requests",
            operationType = OperationType.CREATE,
            bodyMap = req.toCreateMap() + ("id" to req.id)
        )

    suspend fun updateDeskRequestStatus(id: String, status: String): Result<Unit> =
        executeJsonMutation(
            method = "PATCH",
            path = "/api/desk-requests/${urlEncode(id)}/status",
            operationType = OperationType.UPDATE,
            bodyMap = mapOf("status" to status)
        )

    suspend fun deleteDeskRequest(id: String): Result<Unit> =
        executeJsonMutation("DELETE", "/api/desk-requests/${urlEncode(id)}", OperationType.DELETE, null)

    private suspend fun executeJsonMutation(
        method: String,
        path: String,
        operationType: OperationType,
        bodyMap: Map<String, Any?>?,
        useLongTimeout: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val builder = Request.Builder()
                .url("$normalizedBaseUrl$path")
                .header("Authorization", "Bearer $authToken")

            val request = when (method.uppercase()) {
                "POST" -> builder.post(mapToJson(bodyMap.orEmpty()).toRequestBody(BazmoServerConfig.JSON_MEDIA_TYPE)).build()
                "PATCH" -> builder.patch(mapToJson(bodyMap.orEmpty()).toRequestBody(BazmoServerConfig.JSON_MEDIA_TYPE)).build()
                "PUT" -> builder.put(mapToJson(bodyMap.orEmpty()).toRequestBody(BazmoServerConfig.JSON_MEDIA_TYPE)).build()
                "DELETE" -> builder.delete().build()
                else -> builder.get().build()
            }

            val targetClient = if (useLongTimeout) BazmoServerConfig.longOpOkHttpClient else client
            targetClient.newCall(request).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    val defaultMsg = when (res.code) {
                        404 -> "این سرویس روی سرور یافت نشد (کد ۴۰۴). لطفاً نسخه جدید سرور را روی هاست مستقر کنید."
                        502, 503, 504 -> "سرور موقتاً شلوغ یا در حال راه‌اندازی مجدد است (کد ${res.code}). چند ثانیه دیگر دوباره تلاش کنید."
                        else -> "خطا در انجام عملیات روی سرور (کد ${res.code})"
                    }
                    return@withContext Result.failure(
                        BazmoApiException(res.code, extractError(body, defaultMsg))
                    )
                }
                triggerRefresh(clearEtag = true)
                Result.success(Unit)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleApiError(e, operationType, path, currentUid)
        }
    }

    private fun mapToJson(map: Map<String, Any?>): String {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        return moshi.adapter<Map<String, Any?>>(type).toJson(map)
    }

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, "UTF-8")

    private fun extractError(body: String, fallback: String): String =
        runCatching { JSONObject(body).optString("error").ifBlank { fallback } }.getOrDefault(fallback)

    companion object {
        suspend fun submitRegistrationRequest(
            req: RegistrationRequestEntity,
            password: String = "",
            baseUrl: String = BazmoServerConfig.baseUrl()
        ): Result<Unit> = withContext(Dispatchers.IO) {
            val docId = req.id.ifBlank { generateUid("reg_") }
            try {
                val baseMap = req.toCreateMap() + ("id" to docId)
                val map = if (password.isNotEmpty()) baseMap + ("password" to password) else baseMap
                val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                val json = BazmoServerConfig.moshi.adapter<Map<String, Any?>>(type).toJson(map)
                val normalized = baseUrl.trim().trimEnd('/')
                val request = Request.Builder()
                    .url("$normalized/api/registration-requests")
                    .post(json.toRequestBody(BazmoServerConfig.JSON_MEDIA_TYPE))
                    .build()
                BazmoServerConfig.okHttpClient.newCall(request).execute().use { res ->
                    val body = res.body?.string().orEmpty()
                    if (!res.isSuccessful) {
                        val msg = runCatching {
                            JSONObject(body).optString("error").ifBlank { "خطا در ارسال درخواست عضویت" }
                        }.getOrDefault("خطا در ارسال درخواست عضویت")
                        return@withContext Result.failure(BazmoApiException(res.code, msg))
                    }
                    Result.success(Unit)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleApiError(e, OperationType.CREATE, "/api/registration-requests/$docId", null)
            }
        }
    }
}
