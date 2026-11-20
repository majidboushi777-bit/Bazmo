package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AccessoryEntity
import com.example.data.BackgroundNotifConfig
import com.example.data.BannerEntity
import com.example.data.BazmoAuthManager
import com.example.data.BazmoDbState
import com.example.data.BazmoLocations
import com.example.data.BazmoNotificationHelper
import com.example.data.BazmoRealtimeNotifService
import com.example.data.BazmoRepository
import com.example.data.BazmoRoles
import com.example.data.BazmoServerConfig
import com.example.data.BazmoSessionUser
import com.example.data.BoxEntity
import com.example.data.ChatMessage
import com.example.data.ChatThreadEntity
import com.example.data.DeskRentalEntity
import com.example.data.DeskRequestEntity
import com.example.data.EnsureProfileResult
import com.example.data.ForumComment
import com.example.data.ForumPostEntity
import com.example.data.ForumReply
import com.example.data.HwAdEntity
import com.example.data.HwRequestEntity
import com.example.data.InquiryEntity
import com.example.data.MobileEntity
import com.example.data.NotificationEntity
import com.example.data.OrderCustomer
import com.example.data.OrderEntity
import com.example.data.OrderItem
import com.example.data.RegistrationRequestEntity
import com.example.data.StoreEntity
import com.example.data.StoreMobileEntity
import com.example.data.SwRequestEntity
import com.example.data.TutorialEntity
import com.example.data.UserEntity
import com.example.data.generateUid
import com.example.data.isValidEmailFormat
import com.example.data.normalizeEmail
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppRole { USER, ADMIN }
enum class AdminTab { DASHBOARD, USERS, NOTIFICATIONS, ADS, REQUESTS, CATALOG }
enum class MobileSubMode { LIST, MODEL, STORE }
enum class SoftwareSubMode { HOME, PROBLEM, TUTORIALS, REQUESTS, BOXES, FORUM }
enum class HardwareSubMode { HOME, AD_DETAIL, MY_ADS, SPECIAL_REQ, FORUM }
enum class ForumSort { HOT, NEW, TOP }

data class CartItem(
    val id: String,
    val name: String,
    val price: Long,
    val storeName: String,
    val qty: Int = 1
)

data class PendingHwReqData(
    val brand: String,
    val model: String,
    val partType: String,
    val series: String
)

sealed interface BazmoModal {
    data object LocationPicker : BazmoModal
    data object ProfileSetup : BazmoModal
    data object Notifications : BazmoModal
    data object Cart : BazmoModal
    data object Orders : BazmoModal
    data class HwMatchFound(val reqData: PendingHwReqData, val matches: List<HwAdEntity>) : BazmoModal
    data class NewRedditPost(val board: String) : BazmoModal
    data class Chat(val adId: String) : BazmoModal
    data object NewHwAd : BazmoModal
    data class NewMarketAd(val section: String) : BazmoModal
    data class RegisterStore(val defaultType: String) : BazmoModal
    data object SellTutorial : BazmoModal
    data object SellBox : BazmoModal
    data object NewSwReq : BazmoModal
    data class NewDeskEntry(val initialKind: String = "rental") : BazmoModal
    data class GenericChat(
        val adId: String,
        val title: String,
        val partnerName: String,
        val sellerUid: String
    ) : BazmoModal
    data class MyStoreAddMobile(val storeId: String) : BazmoModal
    data class MyStoreAddAccessory(val storeId: String) : BazmoModal
}

data class MobileUiFilter(
    val mode: MobileSubMode = MobileSubMode.LIST,
    val search: String = "",
    val brand: String = "همه",
    val maxPrice: Long? = null,
    val modelId: String? = null,
    val storeId: String? = null
)

data class AccUiFilter(
    val cat: String = "همه",
    val selectedStoreId: String? = null,
    val search: String = ""
)

data class SwUiFilter(
    val mode: SoftwareSubMode = SoftwareSubMode.HOME,
    val problemId: String? = null,
    val search: String = ""
)

data class HwUiFilter(
    val mode: HardwareSubMode = HardwareSubMode.HOME,
    val cat: String = "همه",
    val search: String = "",
    val adId: String? = null
)

data class ForumUiFilter(
    val sort: ForumSort = ForumSort.HOT,
    val flair: String = "همه",
    val search: String = "",
    val threadId: String? = null
)

data class AdminNotifFilter(
    val province: String = "همه استان‌ها",
    val city: String = "همه شهرها",
    val region: String = "همه مناطق",
    val tierFilter: String = "همه",
    val sectionFilter: String = "همه بخش‌ها"
)

data class AdminUserFilter(
    val search: String = "",
    val tier: String = "همه",
    val province: String = "همه استان‌ها",
    val statusFilter: String = "همه"
)

data class BazmoSessionState(
    val role: AppRole = AppRole.USER,
    val isAdminUser: Boolean = false,
    val currentUser: UserEntity? = null,
    val province: String = "تهران",
    val city: String = "تهران",
    val region: String = "علاءالدین و جمهوری",
    val activeTab: Int = 0,
    val showDeskRentals: Boolean = false,
    val showMyStore: Boolean = false,
    val adminTab: AdminTab = AdminTab.DASHBOARD,
    val adminUsersSubTab: String = "users",
    val adminAdsSection: String = "hw",
    val adminNotifFilter: AdminNotifFilter = AdminNotifFilter(),
    val adminUserFilter: AdminUserFilter = AdminUserFilter(),
    val mobileView: MobileUiFilter = MobileUiFilter(),
    val accView: AccUiFilter = AccUiFilter(),
    val swView: SwUiFilter = SwUiFilter(),
    val hwView: HwUiFilter = HwUiFilter(),
    val swForum: ForumUiFilter = ForumUiFilter(),
    val hwForum: ForumUiFilter = ForumUiFilter(),
    val cart: List<CartItem> = emptyList(),
    val modal: BazmoModal? = null,
    val isSeedingDemo: Boolean = false
)

data class BazmoUiState(
    val isReady: Boolean = false,
    val currentUid: String = "",
    val authEmail: String = "",
    val authDisplayName: String = "",
    val isNotRegistered: Boolean = false,
    val isBlocked: Boolean = false,
    val db: BazmoDbState = BazmoDbState(),
    val session: BazmoSessionState = BazmoSessionState()
) {
    val activeDisplayName: String
        get() = session.currentUser?.fullName?.ifBlank { null }
            ?: authDisplayName.ifBlank { null }
            ?: authEmail.substringBefore("@").ifBlank { "همکار بازمو" }

    val currentTier: String
        get() = session.currentUser?.tier.orEmpty()

    val canCreateStore: Boolean
        get() = BazmoRoles.canCreateStore(currentTier, session.isAdminUser)

    val canAccessHardwareForum: Boolean
        get() = BazmoRoles.canAccessHardwareForum(currentTier, session.isAdminUser)

    val canAccessSoftwareSpecialized: Boolean
        get() = BazmoRoles.canAccessSoftwareSpecialized(currentTier, session.isAdminUser)

    fun getStore(id: String): StoreEntity =
        db.stores.find { it.id == id } ?: StoreEntity(
            id = id,
            name = "فروشگاه همکار",
            province = session.province,
            city = session.city,
            region = session.region,
            phone = "021-66700000",
            address = "مرکز بازار موبایل",
            hours = "۱۰ تا ۲۰",
            rating = "۴.۸",
            type = "both"
        )

    fun userNotifications(): List<NotificationEntity> {
        val tier = session.currentUser?.tier?.ifBlank { BazmoRoles.REPAIR_TECH } ?: BazmoRoles.REPAIR_TECH
        val sessProv = BazmoLocations.normalizeZwnj(session.province)
        val sessCity = BazmoLocations.normalizeZwnj(session.city)
        val sessReg = BazmoLocations.normalizeZwnj(session.region)
        val normTier = BazmoLocations.normalizeZwnj(tier)
        return db.notifications.filter { n ->
            val matchProv = BazmoLocations.isAllScope(n.province) || BazmoLocations.isAllScope(session.province) || BazmoLocations.normalizeZwnj(n.province) == sessProv
            val matchCity = BazmoLocations.isAllScope(n.city) || BazmoLocations.isAllScope(session.city) || BazmoLocations.normalizeZwnj(n.city) == sessCity
            val matchRegion = BazmoLocations.isAllScope(n.region) || BazmoLocations.isAllScope(session.region) || BazmoLocations.normalizeZwnj(n.region) == sessReg
            val matchTier = session.isAdminUser || BazmoLocations.isAllScope(n.tierFilter) || BazmoLocations.normalizeZwnj(n.tierFilter) == normTier
            matchProv && matchCity && matchRegion && matchTier
        }
    }

    fun unreadNotificationCount(): Int =
        userNotifications().count { !it.readBy.contains(currentUid) }

    fun targetedUsersCount(f: AdminNotifFilter): Int =
        db.users.count { u ->
            if (!u.active) return@count false
            val pOk = BazmoLocations.isAllScope(f.province) || BazmoLocations.normalizeZwnj(u.province) == BazmoLocations.normalizeZwnj(f.province)
            val cOk = BazmoLocations.isAllScope(f.city) || BazmoLocations.normalizeZwnj(u.city) == BazmoLocations.normalizeZwnj(f.city)
            val rOk = BazmoLocations.isAllScope(f.region) || BazmoLocations.normalizeZwnj(u.region) == BazmoLocations.normalizeZwnj(f.region)
            val tOk = BazmoLocations.isAllScope(f.tierFilter) || BazmoLocations.normalizeZwnj(u.tier) == BazmoLocations.normalizeZwnj(f.tierFilter)
            pOk && cOk && rOk && tOk
        }

    fun chatThreadsForAd(adId: String): List<ChatThreadEntity> =
        db.chatThreads.filter { it.adId == adId }

    fun bannersForSection(section: String): List<BannerEntity> =
        db.banners.filter { it.section == section && it.hasImage }.sortedBy { it.slotIndex }
}

class BazmoViewModel(
    private val appContext: Context,
    baseUrl: String,
    authToken: String,
    private val uid: String,
    private val email: String,
    private val displayName: String,
    private val initialIsSuperAdmin: Boolean
) : ViewModel() {

    private val sessionToken: String = authToken

    private val repository = BazmoRepository(
        baseUrl = baseUrl,
        authToken = authToken,
        currentUid = uid
    )

    private val sessionState = MutableStateFlow(BazmoSessionState(isAdminUser = initialIsSuperAdmin))
    private val dbState = MutableStateFlow(BazmoDbState())
    private val isReadyState = MutableStateFlow(false)
    private val isNotRegisteredState = MutableStateFlow(false)
    private val isBlockedInviteState = MutableStateFlow(false)

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    private var dbListenerJob: Job? = null
    private var observedIsAdmin: Boolean = false
    private var observedTier: String? = null
    private var locationInitializedFromProfile = false

    val uiState: StateFlow<BazmoUiState> = combine(
        isReadyState,
        isNotRegisteredState,
        isBlockedInviteState,
        dbState,
        sessionState
    ) { ready, notRegistered, blockedInvite, db, session ->
        val ownProfile = db.users.find { it.uid == uid || it.id == uid } ?: session.currentUser
        val updatedSession = if (ownProfile != null && session.currentUser != ownProfile) {
            if (!locationInitializedFromProfile) {
                locationInitializedFromProfile = true
                session.copy(
                    currentUser = ownProfile,
                    province = ownProfile.province.ifBlank { session.province },
                    city = ownProfile.city.ifBlank { session.city },
                    region = ownProfile.region.ifBlank { session.region }
                )
            } else {
                session.copy(currentUser = ownProfile)
            }
        } else {
            session
        }
        BazmoUiState(
            isReady = ready,
            currentUid = uid,
            authEmail = email,
            authDisplayName = displayName,
            isNotRegistered = notRegistered && !updatedSession.isAdminUser,
            isBlocked = (blockedInvite || ownProfile?.active == false) && !updatedSession.isAdminUser,
            db = db,
            session = updatedSession
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BazmoUiState(
            currentUid = uid,
            authEmail = email,
            authDisplayName = displayName,
            session = BazmoSessionState(isAdminUser = initialIsSuperAdmin)
        )
    )

    private var hasReceivedInitialDbSnapshot = false

    init {
        BazmoNotificationHelper.ensureChannel(appContext)
        startSession()
        viewModelScope.launch {
            uiState.collect { st ->
                val latestTier = st.session.currentUser?.tier
                if (st.isReady && !st.isNotRegistered && !st.isBlocked &&
                    dbListenerJob?.isActive == true &&
                    latestTier != null && latestTier != observedTier && !st.session.isAdminUser
                ) {
                    startObservingDb(st.session.isAdminUser, latestTier)
                }
            }
        }
    }

    private fun syncBackgroundNotificationService() {
        val sess = sessionState.value
        val tier = sess.currentUser?.tier?.ifBlank { BazmoRoles.REPAIR_TECH } ?: BazmoRoles.REPAIR_TECH
        BazmoNotificationHelper.saveBackgroundConfig(
            appContext,
            BackgroundNotifConfig(
                token = sessionToken,
                uid = uid,
                tier = tier,
                isAdmin = sess.isAdminUser,
                province = sess.province,
                city = sess.city,
                region = sess.region
            )
        )
        BazmoNotificationHelper.scheduleBackgroundCheck(appContext)
        BazmoRealtimeNotifService.startIfPermitted(appContext)
    }

    private fun matchingUnreadNotifications(db: BazmoDbState): List<NotificationEntity> {
        val sess = sessionState.value
        val tier = sess.currentUser?.tier?.ifBlank { BazmoRoles.REPAIR_TECH } ?: BazmoRoles.REPAIR_TECH
        return BazmoNotificationHelper.filterMatchingUnread(
            notifications = db.notifications,
            uid = uid,
            isAdmin = sess.isAdminUser,
            tier = tier,
            province = sess.province,
            city = sess.city,
            region = sess.region
        )
    }

    fun onAppResumed() {
        repository.triggerRefresh(wakeSocket = true)
        if (isReadyState.value && !isNotRegisteredState.value && !isBlockedInviteState.value) {
            BazmoRealtimeNotifService.startIfPermitted(appContext)
            viewModelScope.launch {
                val sess = sessionState.value
                val tier = sess.currentUser?.tier?.ifBlank { BazmoRoles.REPAIR_TECH } ?: BazmoRoles.REPAIR_TECH
                repository.fetchUnreadNotifications(
                    province = sess.province,
                    city = sess.city,
                    region = sess.region
                ).onSuccess { list ->
                    val matching = BazmoNotificationHelper.filterMatchingUnread(
                        notifications = list,
                        uid = uid,
                        isAdmin = sess.isAdminUser,
                        tier = tier,
                        province = sess.province,
                        city = sess.city,
                        region = sess.region
                    )
                    if (matching.isNotEmpty()) {
                        val posted = BazmoNotificationHelper.dispatchNewNotifications(
                            context = appContext,
                            matchingUnread = matching,
                            isInitialSnapshot = !hasReceivedInitialDbSnapshot
                        )
                        if (posted.isNotEmpty()) {
                            showToast("🔔 اعلان جدید: ${posted.first().title}")
                        }
                    }
                }
            }
        }
    }

    fun setRealtimeNotificationsEnabled(enabled: Boolean) {
        BazmoNotificationHelper.setRealtimeServiceEnabled(appContext, enabled)
        if (enabled) {
            syncBackgroundNotificationService()
            showToast("سرویس دریافت آنی اعلان‌ها در پس‌زمینه فعال شد")
        } else {
            BazmoRealtimeNotifService.stop(appContext)
            BazmoNotificationHelper.scheduleBackgroundCheck(appContext)
            showToast("سرویس زنده متوقف شد؛ بررسی دوره‌ای پس‌زمینه همچنان فعال است")
        }
    }

    private fun startObservingDb(isAdmin: Boolean, userTier: String?) {
        observedIsAdmin = isAdmin
        observedTier = userTier
        dbListenerJob?.cancel()
        dbListenerJob = viewModelScope.launch {
            try {
                repository.observeDbState(isAdmin = isAdmin, userTier = userTier).collect { latest ->
                    val isInitial = !hasReceivedInitialDbSnapshot
                    hasReceivedInitialDbSnapshot = true
                    dbState.value = latest
                    isReadyState.value = true
                    viewModelScope.launch { repository.saveCachedDbState(appContext, latest) }

                    val unreadMatching = matchingUnreadNotifications(latest)
                    val postedLive = BazmoNotificationHelper.dispatchNewNotifications(
                        context = appContext,
                        matchingUnread = unreadMatching,
                        isInitialSnapshot = isInitial
                    )
                    if (postedLive.isNotEmpty()) {
                        showToast("🔔 اعلان جدید: ${postedLive.first().title}")
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                isReadyState.value = true
                showToast(e.message ?: "خطا در همگام‌سازی با سرور")
            }
        }
    }

    private fun startSession(notifyIfDenied: Boolean = false) {
        viewModelScope.launch {
            val cachedDb = repository.loadCachedDbState(appContext)
            if (cachedDb != null) {
                dbState.value = cachedDb
                hasReceivedInitialDbSnapshot = true
            }

            val isAdmin = initialIsSuperAdmin || repository.checkIsAdminDoc(uid)
            sessionState.update { it.copy(isAdminUser = isAdmin) }

            val profileRes = repository.ensureUserProfile(uid, email, displayName, isAdmin)
            var shouldObserve = isAdmin
            var initialTier: String? = null

            profileRes.onSuccess { result ->
                when (result) {
                    EnsureProfileResult.NotInvited -> {
                        shouldObserve = false
                        isNotRegisteredState.value = true
                        isBlockedInviteState.value = false
                        isReadyState.value = true
                        if (notifyIfDenied) {
                            showToast("هنوز حساب شما توسط مدیر بازمو ثبت نشده است")
                        }
                    }

                    EnsureProfileResult.Blocked -> {
                        shouldObserve = false
                        isNotRegisteredState.value = false
                        isBlockedInviteState.value = true
                        isReadyState.value = true
                        if (notifyIfDenied) {
                            showToast("حساب کاربری شما مسدود است")
                        }
                    }

                    is EnsureProfileResult.Ready -> {
                        shouldObserve = true
                        isNotRegisteredState.value = false
                        isBlockedInviteState.value = false
                        initialTier = result.user.tier
                        locationInitializedFromProfile = true
                        sessionState.update {
                            it.copy(
                                currentUser = result.user,
                                province = result.user.province.ifBlank { it.province },
                                city = result.user.city.ifBlank { it.city },
                                region = result.user.region.ifBlank { it.region },
                                modal = if (result.promptProfile) BazmoModal.ProfileSetup else it.modal
                            )
                        }
                        if (cachedDb != null) {
                            isReadyState.value = true
                        }
                        syncBackgroundNotificationService()
                    }
                }
            }.onFailure { err ->
                if (cachedDb != null) {
                    isReadyState.value = true
                    shouldObserve = true
                } else {
                    showToast(err.message ?: "خطا در دریافت پروفایل")
                    if (!isAdmin) {
                        isNotRegisteredState.value = true
                        isReadyState.value = true
                    }
                }
            }

            if (!shouldObserve) return@launch

            if (isAdmin) {
                syncBackgroundNotificationService()
                repository.seedInitialMarketplaceIfEmpty(uid).onFailure { err ->
                    showToast(err.message ?: "خطا در راه‌اندازی داده‌های پایه")
                }
            }

            startObservingDb(isAdmin = isAdmin, userTier = initialTier)
        }
    }

    fun recheckInviteStatus() {
        isReadyState.value = false
        startSession(notifyIfDenied = true)
    }

    fun detachListeners() {
        dbListenerJob?.cancel()
        dbListenerJob = null
    }

    override fun onCleared() {
        detachListeners()
        super.onCleared()
    }

    fun showToast(msg: String) {
        _toastMessages.tryEmit(msg)
    }

    // ==================== ROLE & PROFILE ====================
    fun refreshOwnProfileStatus() {
        viewModelScope.launch {
            repository.fetchOwnUserProfile(uid)
                .onSuccess { latest ->
                    if (latest != null) {
                        isBlockedInviteState.value = !latest.active
                        dbState.update { cur ->
                            val updatedUsers = cur.users.filterNot { it.uid == uid || it.id == uid } + latest
                            cur.copy(users = updatedUsers.sortedBy { it.fullName })
                        }
                        if (latest.active && (dbListenerJob == null || dbListenerJob?.isActive != true)) {
                            startObservingDb(isAdmin = sessionState.value.isAdminUser, userTier = latest.tier)
                        }
                        showToast(if (latest.active) "حساب کاربری شما فعال است" else "حساب کاربری هنوز مسدود است")
                    }
                }
                .onFailure { err ->
                    showToast(err.message ?: "خطا در بررسی وضعیت حساب")
                }
        }
    }

    fun switchRole(role: AppRole) {
        if (role == AppRole.ADMIN && !sessionState.value.isAdminUser) {
            showToast("شما دسترسی مدیریت ندارید")
            return
        }
        sessionState.update { it.copy(role = role, modal = null) }
    }

    fun saveOwnProfile(
        fullName: String,
        phone: String
    ) {
        val fn = fullName.trim().take(120)
        if (fn.isEmpty()) {
            showToast("نام و نام خانوادگی الزامی است")
            return
        }
        val cleanPhone = phone.trim().take(40)
        viewModelScope.launch {
            repository.updateOwnProfile(
                uid = uid,
                fullName = fn,
                phone = cleanPhone
            ).onSuccess {
                sessionState.update {
                    it.copy(
                        currentUser = it.currentUser?.copy(fullName = fn, phone = cleanPhone),
                        modal = null
                    )
                }
                showToast("نام و شماره تماس پروفایل ذخیره شد")
            }.onFailure { err ->
                showToast(err.message ?: "خطا در ذخیره پروفایل")
            }
        }
    }

    fun openPromoBanner(banner: BannerEntity, onOpenExternalUrl: (String) -> Unit) {
        when (banner.linkType) {
            "store" -> {
                val target = banner.linkTarget.trim()
                if (target.isNotEmpty()) {
                    sessionState.update {
                        it.copy(
                            activeTab = 0,
                            showDeskRentals = false,
                            showMyStore = false,
                            mobileView = it.mobileView.copy(
                                mode = MobileSubMode.STORE,
                                storeId = target
                            )
                        )
                    }
                }
            }
            "url" -> {
                val raw = banner.linkTarget.trim()
                if (raw.isNotEmpty() && !raw.startsWith("javascript:", ignoreCase = true) && !raw.startsWith("data:", ignoreCase = true)) {
                    val safeUrl = if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
                        raw
                    } else {
                        "https://$raw"
                    }
                    onOpenExternalUrl(safeUrl)
                }
            }
            else -> Unit
        }
    }

    // ==================== HEADER, LOCATION & NAVIGATION ====================
    fun setActiveTab(idx: Int) {
        if (idx in 0..3) {
            sessionState.update { it.copy(activeTab = idx, showDeskRentals = false, showMyStore = false) }
        }
    }

    fun toggleDeskRentals(open: Boolean? = null) {
        sessionState.update { cur ->
            val next = open ?: !cur.showDeskRentals
            cur.copy(showDeskRentals = next, showMyStore = if (next) false else cur.showMyStore)
        }
    }

    fun toggleMyStore(open: Boolean? = null) {
        if (!uiState.value.canCreateStore) {
            showToast("مدیریت فروشگاه فقط برای نقش‌های فروشنده عمده و قطعه‌فروش فعال است")
            return
        }
        sessionState.update { cur ->
            val next = open ?: !cur.showMyStore
            cur.copy(showMyStore = next, showDeskRentals = if (next) false else cur.showDeskRentals)
        }
    }

    fun setLocation(province: String, city: String, region: String) {
        sessionState.update {
            it.copy(
                province = province,
                city = city,
                region = region,
                modal = null
            )
        }
        syncBackgroundNotificationService()
        val label = when {
            BazmoLocations.isAllScope(province) -> "همه کشور"
            BazmoLocations.isAllScope(city) -> "استان $province"
            else -> "$city ($region)"
        }
        showToast("موقعیت روی $label تنظیم شد")
    }

    fun openModal(modal: BazmoModal) {
        when (modal) {
            is BazmoModal.RegisterStore -> {
                if (!uiState.value.canCreateStore) {
                    showToast("ثبت فروشگاه فقط برای عمده\u200cفروش موبایل، عمده\u200cفروش جانبی و قطعه\u200cفروش مجاز است")
                    return
                }
            }
            BazmoModal.SellTutorial -> {
                if (!uiState.value.canAccessSoftwareSpecialized) {
                    showToast("ثبت و مشاهده آموزش‌های نرم‌افزار فقط برای نقش نرم\u200cافزارکار مجاز است")
                    return
                }
            }
            is BazmoModal.NewRedditPost -> {
                val allowed = if (modal.board == "hardware") uiState.value.canAccessHardwareForum
                else uiState.value.canAccessSoftwareSpecialized
                if (!allowed) {
                    showToast(
                        if (modal.board == "hardware") "دسترسی به انجمن سخت‌افزار فقط برای نقش تعمیرکار مجاز است"
                        else "دسترسی به انجمن نرم‌افزار فقط برای نقش نرم\u200cافزارکار مجاز است"
                    )
                    return
                }
            }
            else -> Unit
        }
        sessionState.update { it.copy(modal = modal) }
    }

    fun closeModal() {
        sessionState.update { it.copy(modal = null) }
    }

    // ==================== NOTIFICATIONS ====================
    fun markNotificationRead(id: String) {
        val n = uiState.value.db.notifications.find { it.id == id } ?: return
        if (!n.readBy.contains(uid)) {
            viewModelScope.launch {
                repository.markNotificationRead(id, uid).onFailure { err ->
                    showToast(err.message ?: "خطا در ثبت خوانده شدن اعلان")
                }
            }
        }
    }

    fun markAllNotificationsRead() {
        val myNotifs = uiState.value.userNotifications()
        viewModelScope.launch {
            myNotifs.forEach { n ->
                if (!n.readBy.contains(uid)) {
                    repository.markNotificationRead(n.id, uid)
                }
            }
        }
    }

    // ==================== CART & ORDERS ====================
    fun addToCart(id: String, name: String, price: Long, storeName: String) {
        sessionState.update { st ->
            val existing = st.cart.find { it.id == id }
            val updatedCart = if (existing != null) {
                st.cart.map { if (it.id == id) it.copy(qty = it.qty + 1) else it }
            } else {
                st.cart + CartItem(id = id, name = name, price = price, storeName = storeName, qty = 1)
            }
            st.copy(cart = updatedCart)
        }
        showToast("به سبد خرید اضافه شد")
    }

    fun updateCartQty(id: String, delta: Int) {
        sessionState.update { st ->
            val updated = st.cart.mapNotNull { item ->
                if (item.id == id) {
                    val nextQty = item.qty + delta
                    if (nextQty <= 0) null else item.copy(qty = nextQty)
                } else item
            }
            st.copy(cart = updated)
        }
    }

    fun submitOrder(
        deliveryType: String,
        firstName: String = "",
        lastName: String = "",
        phone: String = "",
        address: String = ""
    ) {
        val st = sessionState.value
        if (st.cart.isEmpty()) return

        val customer = if (deliveryType == "پیک") {
            val fn = firstName.trim()
            val ln = lastName.trim()
            val ph = phone.trim()
            val ad = address.trim()
            if (fn.isEmpty() || ln.isEmpty() || ph.isEmpty()) {
                showToast("لطفاً نام، نام خانوادگی و شماره تلفن را وارد کنید")
                return
            }
            OrderCustomer(
                firstName = fn,
                lastName = ln,
                phone = ph,
                address = ad.ifEmpty { "${st.city}، ${st.region}" }
            )
        } else {
            OrderCustomer(
                firstName = uiState.value.activeDisplayName,
                lastName = "حضوری",
                phone = st.currentUser?.phone?.ifBlank { "تحویل در فروشگاه" } ?: "تحویل در فروشگاه",
                address = "${st.city} - ${st.region}"
            )
        }

        val orderItems = st.cart.map { OrderItem(it.id, it.name, it.price, it.qty, it.storeName) }
        val total = st.cart.sumOf { it.price * it.qty }
        val order = OrderEntity(
            id = "ORD-${(1000..9999).random()}-${generateUid("").take(3)}",
            userId = uid,
            userName = uiState.value.activeDisplayName,
            items = orderItems,
            total = total,
            deliveryType = deliveryType,
            customer = customer,
            status = "ارسال شده به فروشگاه",
            date = "امروز",
            city = st.city,
            sortOrder = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.createOrder(order)
                .onSuccess {
                    sessionState.update { it.copy(cart = emptyList(), modal = BazmoModal.Orders) }
                    showToast("سفارش شما ثبت و به فروشگاه ارسال شد!")
                }
                .onFailure { err ->
                    showToast(err.message ?: "خطا در ثبت سفارش")
                }
        }
    }

    // ==================== MOBILE TAB ====================
    fun updateMobileSearch(q: String) =
        sessionState.update { it.copy(mobileView = it.mobileView.copy(search = q)) }

    fun updateMobileBrand(b: String) =
        sessionState.update { it.copy(mobileView = it.mobileView.copy(brand = b)) }

    fun updateMobileMaxPrice(max: Long?) =
        sessionState.update { it.copy(mobileView = it.mobileView.copy(maxPrice = max)) }

    fun openMobileModel(modelId: String) =
        sessionState.update { it.copy(mobileView = it.mobileView.copy(mode = MobileSubMode.MODEL, modelId = modelId)) }

    fun openMobileStore(storeId: String) =
        sessionState.update { it.copy(mobileView = it.mobileView.copy(mode = MobileSubMode.STORE, storeId = storeId)) }

    fun backMobileToList() =
        sessionState.update { it.copy(mobileView = it.mobileView.copy(mode = MobileSubMode.LIST, modelId = null, storeId = null)) }

    fun backMobileToModel() =
        sessionState.update {
            val nextMode = if (it.mobileView.modelId != null) MobileSubMode.MODEL else MobileSubMode.LIST
            it.copy(mobileView = it.mobileView.copy(mode = nextMode, storeId = null))
        }

    // ==================== ACCESSORIES TAB ====================
    fun updateAccSearch(q: String) =
        sessionState.update { it.copy(accView = it.accView.copy(search = q)) }

    fun updateAccCat(cat: String) =
        sessionState.update { it.copy(accView = it.accView.copy(cat = cat, selectedStoreId = null)) }

    fun toggleAccStore(storeId: String) =
        sessionState.update {
            val next = if (it.accView.selectedStoreId == storeId) null else storeId
            it.copy(accView = it.accView.copy(selectedStoreId = next))
        }

    fun clearAccStore() =
        sessionState.update { it.copy(accView = it.accView.copy(selectedStoreId = null)) }

    fun sendAccessoryInquiry(acc: AccessoryEntity) {
        val store = uiState.value.getStore(acc.storeId)
        val inquiry = InquiryEntity(
            id = generateUid("inq_"),
            accId = acc.id,
            accName = acc.name,
            storeName = store.name,
            userId = uid,
            userName = uiState.value.activeDisplayName,
            status = "ارسال شده به فروشنده (در انتظار تایید موجودی)",
            date = "امروز",
            sortOrder = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.createInquiry(inquiry)
                .onSuccess { showToast("استعلام به فروشنده اطلاع داده شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت استعلام") }
        }
    }

    // ==================== SOFTWARE TAB ====================
    fun setSwMode(mode: SoftwareSubMode, problemId: String? = null) {
        val requiresSpecialist = mode in listOf(
            SoftwareSubMode.TUTORIALS,
            SoftwareSubMode.FORUM
        )
        if (requiresSpecialist && !uiState.value.canAccessSoftwareSpecialized) {
            showToast("دسترسی به بخش آموزش‌ها و انجمن نرم‌افزار فقط برای نقش نرم\u200cافزارکار مجاز است")
            return
        }
        sessionState.update {
            it.copy(swView = SwUiFilter(mode = mode, problemId = problemId, search = ""))
        }
    }

    fun updateSwSearch(q: String) =
        sessionState.update { it.copy(swView = it.swView.copy(search = q)) }

    fun submitTutorial(problemId: String, title: String, teacher: String, priceText: String, terms: String) {
        if (!uiState.value.canAccessSoftwareSpecialized) {
            showToast("ثبت آموزش فقط برای نقش نرم\u200cافزارکار مجاز است")
            return
        }
        val t = title.trim()
        val price = priceText.trim().toLongOrNull() ?: 0L
        if (t.isEmpty() || price <= 0L) {
            showToast("عنوان و قیمت معتبر الزامی است")
            return
        }
        val tut = TutorialEntity(
            id = generateUid("tut_"),
            problemId = problemId,
            title = t,
            teacher = teacher.trim().ifEmpty { uiState.value.activeDisplayName },
            rating = "۵.۰",
            price = price,
            terms = terms.trim().ifEmpty { "پشتیبانی آنلاین" },
            city = sessionState.value.city,
            status = "pending",
            ownerId = uid
        )
        viewModelScope.launch {
            repository.createTutorial(tut)
                .onSuccess {
                    closeModal()
                    showToast("آموزش برای تایید ادمین ارسال شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت آموزش") }
        }
    }

    fun submitBox(name: String, priceText: String, condition: String, seller: String) {
        val n = name.trim()
        val price = priceText.trim().toLongOrNull() ?: 0L
        if (n.isEmpty() || price <= 0L) {
            showToast("نام باکس و قیمت معتبر الزامی است")
            return
        }
        val box = BoxEntity(
            id = generateUid("bx_"),
            name = n,
            price = price,
            seller = seller.trim().ifEmpty { uiState.value.activeDisplayName },
            city = sessionState.value.city,
            condition = condition.trim().ifEmpty { "سالم" },
            status = "pending",
            ownerId = uid
        )
        viewModelScope.launch {
            repository.createBox(box)
                .onSuccess {
                    closeModal()
                    showToast("آگهی باکس برای تایید ادمین ارسال شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت آگهی باکس") }
        }
    }

    fun submitSwRequest(title: String) {
        val t = title.trim()
        if (t.isEmpty()) {
            showToast("شرح درخواست را وارد کنید")
            return
        }
        val req = SwRequestEntity(
            id = generateUid("sr_"),
            title = t,
            userId = uid,
            userName = uiState.value.activeDisplayName,
            city = sessionState.value.city,
            status = "در انتظار بررسی ادمین",
            reply = "",
            published = true,
            sortOrder = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.createSwRequest(req)
                .onSuccess {
                    closeModal()
                    showToast("درخواست ثبت شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت درخواست") }
        }
    }

    // ==================== HARDWARE & MARKETPLACE ADS / STORES ====================
    fun setHwMode(mode: HardwareSubMode, adId: String? = null) {
        if (mode == HardwareSubMode.FORUM && !uiState.value.canAccessHardwareForum) {
            showToast("ورود به انجمن سخت‌افزار فقط برای نقش تعمیرکار مجاز است")
            return
        }
        sessionState.update { it.copy(hwView = it.hwView.copy(mode = mode, adId = adId)) }
    }

    fun updateHwSearch(q: String) =
        sessionState.update { it.copy(hwView = it.hwView.copy(search = q)) }

    fun toggleHwCat(cat: String) {
        sessionState.update {
            val next = if (it.hwView.cat == cat) "همه" else cat
            it.copy(hwView = it.hwView.copy(cat = next))
        }
    }

    fun submitNewStore(
        name: String,
        province: String,
        city: String,
        region: String,
        phone: String,
        address: String,
        hours: String,
        type: String
    ) {
        if (!uiState.value.canCreateStore) {
            showToast("فقط عمده\u200cفروش موبایل، عمده\u200cفروش جانبی و قطعه\u200cفروش امکان ثبت فروشگاه دارند")
            return
        }
        val cleanName = name.trim().take(140)
        if (cleanName.isEmpty()) {
            showToast("نام فروشگاه را وارد کنید")
            return
        }
        val safeType = if (type in listOf("mobile", "acc", "hw", "both")) type
        else BazmoRoles.defaultStoreTypeForRole(uiState.value.currentTier)

        val isAdmin = sessionState.value.isAdminUser
        val st = StoreEntity(
            id = generateUid("st_"),
            name = cleanName,
            province = province.trim().ifEmpty { sessionState.value.province }.take(80),
            city = city.trim().ifEmpty { sessionState.value.city }.take(80),
            region = region.trim().ifEmpty { sessionState.value.region }.take(80),
            phone = phone.trim().ifEmpty { sessionState.value.currentUser?.phone.orEmpty() }.take(40),
            address = address.trim().ifEmpty { "${city.ifBlank { sessionState.value.city }}، $region" }.take(300),
            hours = hours.trim().ifEmpty { "۱۰ تا ۲۰" }.take(80),
            rating = "۵.۰",
            type = safeType,
            ownerId = uid,
            published = isAdmin,
            status = if (isAdmin) "approved" else "pending"
        )
        viewModelScope.launch {
            repository.createStore(st)
                .onSuccess {
                    closeModal()
                    showToast(
                        if (isAdmin) "فروشگاه شما با موفقیت ثبت و فعال شد!"
                        else "فروشگاه شما ثبت شد و پس از تایید مدیریت فعال می‌شود."
                    )
                }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت فروشگاه") }
        }
    }

    fun submitNewHwAd(
        title: String,
        cat: String,
        brand: String,
        model: String,
        series: String,
        priceText: String,
        phone: String,
        desc: String,
        section: String = "hw"
    ) {
        val t = title.trim()
        val price = priceText.trim().toLongOrNull() ?: 0L
        if (t.isEmpty() || price <= 0L) {
            showToast("عنوان و قیمت معتبر را وارد کنید")
            return
        }
        val safeSection = if (section in listOf("hw", "mobile", "acc")) section else "hw"
        val ad = HwAdEntity(
            id = generateUid("ad_"),
            cat = cat.trim().ifEmpty { "سایر" },
            title = t,
            brand = brand.trim().ifEmpty { "عمومی" },
            model = model.trim().ifEmpty { "-" },
            series = series.trim().ifEmpty { "-" },
            price = price,
            seller = uiState.value.activeDisplayName,
            phone = phone.trim().ifEmpty { sessionState.value.currentUser?.phone?.ifBlank { "09120000000" } ?: "09120000000" },
            city = sessionState.value.city,
            province = sessionState.value.province,
            region = sessionState.value.region,
            ownerId = uid,
            ownerName = uiState.value.activeDisplayName,
            status = "pending",
            desc = desc.trim(),
            section = safeSection,
            sortOrder = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.createHwAd(ad)
                .onSuccess {
                    closeModal()
                    showToast("آگهی برای تایید ادمین ارسال شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت آگهی") }
        }
    }

    fun editMyHwAdPrice(ad: HwAdEntity, newPriceText: String) {
        val p = newPriceText.trim().toLongOrNull() ?: return
        if (p <= 0L) return
        viewModelScope.launch {
            repository.updateHwAdPrice(ad.id, p)
                .onSuccess { showToast("قیمت به‌روزرسانی شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در ویرایش قیمت") }
        }
    }

    fun deleteMyHwAd(id: String) {
        viewModelScope.launch {
            repository.deleteHwAd(id)
                .onSuccess { showToast("آگهی حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف آگهی") }
        }
    }

    fun checkAndSubmitHwRequest(brand: String, model: String, partType: String, series: String) {
        val m = model.trim()
        val s = series.trim()
        if (m.isEmpty() || s.isEmpty()) {
            showToast("لطفاً مدل و سری/کد قطعه را وارد کنید")
            return
        }
        val matches = uiState.value.db.hwAds.filter { ad ->
            ad.status == "approved" &&
                (ad.model.contains(m, ignoreCase = true) || ad.series.contains(s, ignoreCase = true)) &&
                ad.cat == partType
        }
        val reqData = PendingHwReqData(brand, m, partType, s)
        if (matches.isNotEmpty()) {
            openModal(BazmoModal.HwMatchFound(reqData, matches))
        } else {
            finalizeHwRequest(reqData)
        }
    }

    fun finalizeHwRequest(reqData: PendingHwReqData) {
        val req = HwRequestEntity(
            id = generateUid("hr_"),
            brand = reqData.brand,
            model = reqData.model,
            partType = reqData.partType,
            series = reqData.series,
            city = sessionState.value.city,
            userId = uid,
            userName = uiState.value.activeDisplayName,
            status = "ثبت شد - در حال پیگیری تأمین‌کننده",
            note = "",
            sortOrder = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.createHwRequest(req)
                .onSuccess {
                    closeModal()
                    showToast("درخواست قطعه ثبت شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت درخواست") }
        }
    }

    fun sendChatMessage(adId: String, targetThreadId: String?, text: String) {
        val ad = uiState.value.db.hwAds.find { it.id == adId } ?: return
        sendGenericChatMessage(
            adId = adId,
            sellerUid = ad.ownerId.ifBlank { uid },
            targetThreadId = targetThreadId,
            text = text
        )
    }

    fun sendGenericChatMessage(adId: String, sellerUid: String, targetThreadId: String?, text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        val existingThread = if (targetThreadId != null) {
            uiState.value.db.chatThreads.find { it.id == targetThreadId }
        } else {
            uiState.value.db.chatThreads.find {
                it.adId == adId && (it.buyerUid == uid || it.participants.contains(uid))
            }
        }
        val threadId = existingThread?.id ?: "${adId}_${uid}".replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val buyerUid = existingThread?.buyerUid ?: uid
        val newMsg = ChatMessage(
            senderUid = uid,
            senderName = uiState.value.activeDisplayName,
            text = t,
            sentAtMs = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.sendChatMessage(
                threadId = threadId,
                adId = adId,
                buyerUid = buyerUid,
                sellerUid = sellerUid.ifBlank { uid },
                existingMessages = existingThread?.messages.orEmpty(),
                newMessage = newMsg
            ).onFailure { err ->
                showToast(err.message ?: "خطا در ارسال پیام")
            }
        }
    }

    // ==================== REDDIT-STYLE FORUMS ====================
    private fun updateForum(board: String, transform: (ForumUiFilter) -> ForumUiFilter) {
        sessionState.update { st ->
            if (board == "software") st.copy(swForum = transform(st.swForum))
            else st.copy(hwForum = transform(st.hwForum))
        }
    }

    fun setForumThread(board: String, threadId: String?) = updateForum(board) { it.copy(threadId = threadId) }
    fun setForumSort(board: String, sort: ForumSort) = updateForum(board) { it.copy(sort = sort) }
    fun setForumFlair(board: String, flair: String) = updateForum(board) { it.copy(flair = flair) }
    fun setForumSearch(board: String, search: String) = updateForum(board) { it.copy(search = search) }

    private fun canPostInForumBoard(board: String): Boolean =
        if (board == "hardware") uiState.value.canAccessHardwareForum
        else uiState.value.canAccessSoftwareSpecialized

    fun submitForumPost(board: String, flair: String, deviceTag: String, title: String, body: String) {
        if (!canPostInForumBoard(board)) {
            showToast("شما مجاز به ارسال پست در این انجمن نیستید")
            return
        }
        val t = title.trim()
        val b = body.trim()
        if (t.isEmpty() || b.isEmpty()) {
            showToast("عنوان و متن پست را وارد کنید")
            return
        }
        val cu = sessionState.value.currentUser
        val post = ForumPostEntity(
            id = generateUid("fp_"),
            board = board,
            flair = flair,
            author = uiState.value.activeDisplayName,
            authorUid = uid,
            authorBadge = cu?.tier ?: "عضو بازمو",
            city = sessionState.value.city,
            deviceTag = deviceTag.trim(),
            pinned = false,
            verifiedSolution = false,
            title = t,
            body = b,
            votes = 1,
            upvotedBy = listOf(uid),
            downvotedBy = emptyList(),
            savedBy = emptyList(),
            createdAtMs = System.currentTimeMillis(),
            timeLabel = "هم‌اکنون",
            comments = emptyList(),
            published = true
        )
        viewModelScope.launch {
            repository.createForumPost(post)
                .onSuccess {
                    closeModal()
                    showToast("پست منتشر شد!")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در انتشار پست") }
        }
    }

    fun votePost(post: ForumPostEntity, dir: Int) {
        if (!canPostInForumBoard(post.board)) return
        val currentVote = post.userVoteFor(uid)
        val newVote = if (currentVote == dir) 0 else dir
        val newUp = (post.upvotedBy - uid) + if (newVote == 1) listOf(uid) else emptyList()
        val newDown = (post.downvotedBy - uid) + if (newVote == -1) listOf(uid) else emptyList()
        val newScore = post.votes + (newVote - currentVote)
        viewModelScope.launch {
            repository.updateForumPostInteraction(
                postId = post.id,
                votes = newScore,
                upvotedBy = newUp.distinct(),
                downvotedBy = newDown.distinct(),
                savedBy = post.savedBy,
                comments = post.comments
            ).onFailure { err -> showToast(err.message ?: "خطا در ثبت رای") }
        }
    }

    fun toggleSavePost(post: ForumPostEntity) {
        if (!canPostInForumBoard(post.board)) return
        val currentlySaved = post.isSavedBy(uid)
        val nextSavedBy = if (currentlySaved) post.savedBy - uid else (post.savedBy + uid).distinct()
        viewModelScope.launch {
            repository.updateForumPostInteraction(
                postId = post.id,
                votes = post.votes,
                upvotedBy = post.upvotedBy,
                downvotedBy = post.downvotedBy,
                savedBy = nextSavedBy,
                comments = post.comments
            ).onSuccess {
                showToast(if (!currentlySaved) "پست ذخیره شد" else "از ذخیره‌ها حذف شد")
            }.onFailure { err ->
                showToast(err.message ?: "خطا در ذخیره پست")
            }
        }
    }

    fun addForumComment(post: ForumPostEntity, text: String) {
        if (!canPostInForumBoard(post.board)) return
        val t = text.trim()
        if (t.isEmpty()) return
        val cu = sessionState.value.currentUser
        val newComment = ForumComment(
            id = generateUid("cm_"),
            author = uiState.value.activeDisplayName,
            badge = cu?.tier ?: "عضو بازمو",
            text = t,
            votes = 1,
            upvotedBy = listOf(uid),
            downvotedBy = emptyList(),
            timeLabel = "هم‌اکنون",
            replies = emptyList()
        )
        viewModelScope.launch {
            repository.updateForumPostInteraction(
                postId = post.id,
                votes = post.votes,
                upvotedBy = post.upvotedBy,
                downvotedBy = post.downvotedBy,
                savedBy = post.savedBy,
                comments = listOf(newComment) + post.comments
            ).onSuccess {
                showToast("دیدگاه ثبت شد")
            }.onFailure { err ->
                showToast(err.message ?: "خطا در ثبت دیدگاه")
            }
        }
    }

    fun addNestedForumReply(post: ForumPostEntity, commentId: String, text: String) {
        if (!canPostInForumBoard(post.board)) return
        val t = text.trim()
        if (t.isEmpty()) return
        val cu = sessionState.value.currentUser
        val newReply = ForumReply(
            id = generateUid("rp_"),
            author = uiState.value.activeDisplayName,
            badge = cu?.tier ?: "عضو بازمو",
            text = t,
            votes = 1,
            upvotedBy = listOf(uid),
            downvotedBy = emptyList(),
            timeLabel = "هم‌اکنون"
        )
        val updatedComments = post.comments.map { cm ->
            if (cm.id == commentId) cm.copy(replies = cm.replies + newReply) else cm
        }
        viewModelScope.launch {
            repository.updateForumPostInteraction(
                postId = post.id,
                votes = post.votes,
                upvotedBy = post.upvotedBy,
                downvotedBy = post.downvotedBy,
                savedBy = post.savedBy,
                comments = updatedComments
            ).onSuccess {
                showToast("پاسخ ثبت شد")
            }.onFailure { err ->
                showToast(err.message ?: "خطا در ثبت پاسخ")
            }
        }
    }

    fun voteForumComment(post: ForumPostEntity, commentId: String, replyId: String?, dir: Int) {
        if (!canPostInForumBoard(post.board)) return
        val updatedComments = post.comments.map { cm ->
            if (cm.id != commentId) cm
            else if (replyId == null) {
                val cur = cm.userVoteFor(uid)
                val next = if (cur == dir) 0 else dir
                val up = ((cm.upvotedBy - uid) + if (next == 1) listOf(uid) else emptyList()).distinct()
                val down = ((cm.downvotedBy - uid) + if (next == -1) listOf(uid) else emptyList()).distinct()
                cm.copy(votes = cm.votes + (next - cur), upvotedBy = up, downvotedBy = down)
            } else {
                cm.copy(
                    replies = cm.replies.map { rep ->
                        if (rep.id != replyId) rep
                        else {
                            val cur = rep.userVoteFor(uid)
                            val next = if (cur == dir) 0 else dir
                            val up = ((rep.upvotedBy - uid) + if (next == 1) listOf(uid) else emptyList()).distinct()
                            val down = ((rep.downvotedBy - uid) + if (next == -1) listOf(uid) else emptyList()).distinct()
                            rep.copy(votes = rep.votes + (next - cur), upvotedBy = up, downvotedBy = down)
                        }
                    }
                )
            }
        }
        viewModelScope.launch {
            repository.updateForumPostInteraction(
                postId = post.id,
                votes = post.votes,
                upvotedBy = post.upvotedBy,
                downvotedBy = post.downvotedBy,
                savedBy = post.savedBy,
                comments = updatedComments
            ).onFailure { err -> showToast(err.message ?: "خطا در ثبت رای") }
        }
    }

    // ==================== ADMIN ACTIONS ====================
    fun setAdminTab(tab: AdminTab) {
        sessionState.update { it.copy(adminTab = tab) }
        if (tab == AdminTab.USERS && sessionState.value.isAdminUser) {
            refreshAdminUsers()
        }
    }

    fun setAdminUsersSubTab(subTab: String) {
        sessionState.update { it.copy(adminUsersSubTab = subTab) }
    }

    fun setAdminAdsSection(section: String) {
        sessionState.update { it.copy(adminAdsSection = section) }
    }

    fun navigateToAdminSection(
        tab: AdminTab,
        usersSubTab: String? = null,
        adsSection: String? = null
    ) {
        sessionState.update {
            it.copy(
                adminTab = tab,
                adminUsersSubTab = usersSubTab ?: it.adminUsersSubTab,
                adminAdsSection = adsSection ?: it.adminAdsSection
            )
        }
        if (tab == AdminTab.USERS && sessionState.value.isAdminUser) {
            refreshAdminUsers()
        }
    }

    fun refreshAdminUsers() {
        if (!sessionState.value.isAdminUser) return
        viewModelScope.launch {
            repository.fetchUsersForAdmin()
        }
    }

    fun updateAdminUserFilter(transform: (AdminUserFilter) -> AdminUserFilter) {
        sessionState.update { it.copy(adminUserFilter = transform(it.adminUserFilter)) }
    }

    fun updateAdminNotifFilter(transform: (AdminNotifFilter) -> AdminNotifFilter) {
        sessionState.update { it.copy(adminNotifFilter = transform(it.adminNotifFilter)) }
    }

    fun adminCreateUser(
        emailInput: String,
        fullName: String,
        phone: String,
        tier: String,
        province: String,
        city: String,
        region: String,
        password: String = "",
        onCreated: () -> Unit = {}
    ): Boolean {
        val em = normalizeEmail(emailInput).take(200)
        val fn = fullName.trim().take(120)
        if (!isValidEmailFormat(em)) {
            showToast("لطفاً ایمیل معتبر کاربر را وارد کنید")
            return false
        }
        if (fn.isEmpty()) {
            showToast("نام و نام خانوادگی الزامی است")
            return false
        }
        if (password.isNotEmpty() && (password.length < 6 || password.length > 128)) {
            showToast("رمز عبور باید حداقل ۶ کاراکتر باشد")
            return false
        }
        val existing = uiState.value.db.users.find { normalizeEmail(it.email) == em }
        if (existing != null) {
            if (!existing.active) {
                viewModelScope.launch {
                    repository.setUserActiveByAdmin(existing, true)
                        .onSuccess {
                            dbState.update { cur ->
                                cur.copy(
                                    users = cur.users.map { u ->
                                        if (u.id == existing.id || u.uid == existing.uid || normalizeEmail(u.email) == em) {
                                            u.copy(active = true)
                                        } else u
                                    }
                                )
                            }
                            sessionState.update { it.copy(adminUserFilter = AdminUserFilter()) }
                            onCreated()
                            showToast("حساب قبلی با این ایمیل مجدداً فعال شد")
                        }
                        .onFailure { err -> showToast(err.message ?: "خطا در فعال‌سازی مجدد") }
                }
                return true
            }
            showToast("کاربری با این ایمیل از قبل فعال است")
            return false
        }
        val docId = generateUid("u_")
        val safeTier = BazmoRoles.normalizeTier(tier)
        val newUser = UserEntity(
            id = docId,
            uid = docId,
            email = em,
            fullName = fn,
            phone = phone.trim().ifEmpty { "-" }.take(40),
            tier = safeTier,
            province = province.trim().ifEmpty { "تهران" }.take(80),
            city = city.trim().ifEmpty { "تهران" }.take(80),
            region = region.trim().ifEmpty { "همه مناطق" }.take(80),
            active = true,
            createdDateLabel = "امروز"
        )
        viewModelScope.launch {
            repository.createUserByAdmin(newUser, password)
                .onSuccess { savedUser ->
                    dbState.update { cur ->
                        val updated = cur.users.filterNot {
                            it.id == savedUser.id ||
                                it.uid == savedUser.uid ||
                                (em.isNotBlank() && normalizeEmail(it.email) == em)
                        } + savedUser
                        cur.copy(users = updated.sortedBy { it.fullName })
                    }
                    sessionState.update { it.copy(adminUserFilter = AdminUserFilter()) }
                    onCreated()
                    showToast("کاربر $fn با نقش $safeTier تعریف و مجاز به ورود شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در تعریف کاربر") }
        }
        return true
    }

    fun adminUpdateUserTier(user: UserEntity, newTier: String) {
        val targetId = user.id.ifBlank { user.uid }
        val safeTier = BazmoRoles.normalizeTier(newTier)
        viewModelScope.launch {
            repository.updateUserTierByAdmin(targetId, user.email, safeTier)
                .onSuccess {
                    dbState.update { cur ->
                        cur.copy(
                            users = cur.users.map { u ->
                                if (u.id == targetId || u.uid == user.uid) u.copy(tier = safeTier) else u
                            }
                        )
                    }
                    showToast("نقش کاربر به «$safeTier» تغییر یافت")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر نقش") }
        }
    }

    fun adminSetUserPassword(user: UserEntity, password: String, onSuccess: () -> Unit = {}) {
        val clean = password
        if (clean.length < 6 || clean.length > 128) {
            showToast("رمز عبور باید بین ۶ تا ۱۲۸ کاراکتر باشد")
            return
        }
        val targetId = user.id.ifBlank { user.uid }
        viewModelScope.launch {
            repository.setUserPasswordByAdmin(targetId, user.email, clean)
                .onSuccess {
                    showToast("رمز عبور ${user.fullName.ifBlank { user.email }} تنظیم شد")
                    onSuccess()
                }
                .onFailure { err -> showToast(err.message ?: "خطا در تنظیم رمز عبور") }
        }
    }

    fun adminUpdateUserLocation(
        user: UserEntity,
        province: String,
        city: String,
        region: String,
        fullName: String,
        phone: String,
        onSuccess: () -> Unit = {}
    ) {
        val targetId = user.id.ifBlank { user.uid }
        val safeProv = province.trim().ifEmpty { "تهران" }.take(80)
        val safeCity = city.trim().ifEmpty { "تهران" }.take(80)
        val safeRegion = region.trim().ifEmpty { "همه مناطق" }.take(80)
        val safeName = fullName.trim().take(120).ifEmpty { user.fullName }
        val safePhone = phone.trim().take(40)
        viewModelScope.launch {
            repository.updateUserLocationByAdmin(
                userId = targetId,
                email = user.email,
                province = safeProv,
                city = safeCity,
                region = safeRegion,
                fullName = safeName,
                phone = safePhone
            ).onSuccess {
                dbState.update { cur ->
                    cur.copy(
                        users = cur.users.map { u ->
                            if (u.id == targetId || u.uid == user.uid) {
                                u.copy(
                                    province = safeProv,
                                    city = safeCity,
                                    region = safeRegion,
                                    fullName = safeName,
                                    phone = safePhone
                                )
                            } else u
                        }
                    )
                }
                showToast("آدرس و مشخصات ${safeName.ifBlank { user.email }} ذخیره شد")
                onSuccess()
            }.onFailure { err ->
                showToast(err.message ?: "خطا در ذخیره آدرس کاربر")
            }
        }
    }

    fun adminUpsertBanner(
        section: String,
        slotIndex: Int,
        title: String,
        linkType: String,
        linkTarget: String,
        imageMime: String,
        imageData: String,
        keepExistingImage: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        if (imageData.isBlank() && !keepExistingImage) {
            showToast("لطفاً تصویر بنر را انتخاب کنید")
            return
        }
        viewModelScope.launch {
            repository.upsertBannerByAdmin(
                section = section,
                slotIndex = slotIndex,
                title = title,
                linkType = linkType,
                linkTarget = linkTarget,
                imageMime = imageMime,
                imageData = imageData,
                keepExistingImage = keepExistingImage
            ).onSuccess {
                showToast("بنر تبلیغاتی با موفقیت ذخیره شد")
                onSuccess()
            }.onFailure { err ->
                showToast(err.message ?: "خطا در ذخیره بنر تبلیغاتی")
            }
        }
    }

    fun adminDeleteBanner(id: String) {
        viewModelScope.launch {
            repository.deleteBannerByAdmin(id)
                .onSuccess {
                    dbState.update { cur ->
                        cur.copy(banners = cur.banners.filterNot { it.id == id })
                    }
                    showToast("بنر حذف شد")
                }
                .onFailure { err ->
                    showToast(err.message ?: "خطا در حذف بنر")
                }
        }
    }

    fun adminApproveRegistration(req: RegistrationRequestEntity, assignedTier: String) {
        val existingUser = uiState.value.db.users.find { normalizeEmail(it.email) == normalizeEmail(req.email) }
        val safeTier = BazmoRoles.normalizeTier(assignedTier)
        viewModelScope.launch {
            repository.approveRegistrationRequest(req, safeTier, existingUser)
                .onSuccess { showToast("درخواست ${req.fullName} با نقش «$safeTier» تایید و اکانت فعال شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تایید درخواست عضویت") }
        }
    }

    fun adminRejectRegistration(reqId: String) {
        viewModelScope.launch {
            repository.rejectRegistrationRequest(reqId)
                .onSuccess { showToast("درخواست عضویت رد شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در رد درخواست") }
        }
    }

    fun adminToggleUserActive(user: UserEntity) {
        val next = !user.active
        val targetId = user.id.ifBlank { user.uid }
        val normEmail = normalizeEmail(user.email)
        viewModelScope.launch {
            repository.setUserActiveByAdmin(user, next)
                .onSuccess {
                    dbState.update { cur ->
                        cur.copy(
                            users = cur.users.map { u ->
                                val matches = (targetId.isNotBlank() && (u.id == targetId || u.uid == targetId)) ||
                                    (normEmail.isNotBlank() && normalizeEmail(u.email) == normEmail)
                                if (matches) u.copy(active = next) else u
                            }
                        )
                    }
                    showToast(if (next) "کاربر مجدداً فعال شد" else "کاربر مسدود شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر وضعیت کاربر") }
        }
    }

    fun adminDeleteUser(id: String) {
        val email = uiState.value.db.users.find { it.id == id || it.uid == id }?.email.orEmpty()
        viewModelScope.launch {
            repository.deleteUser(id, email)
                .onSuccess {
                    dbState.update { cur ->
                        cur.copy(users = cur.users.filterNot { it.id == id || it.uid == id })
                    }
                    showToast("کاربر حذف شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف کاربر") }
        }
    }

    fun adminSendNotification(type: String, title: String, body: String): Boolean {
        val t = title.trim()
        val b = body.trim()
        if (t.isEmpty() || b.isEmpty()) {
            showToast("عنوان و متن نوتیفیکیشن را وارد کنید")
            return false
        }
        val nf = sessionState.value.adminNotifFilter
        val notif = NotificationEntity(
            id = generateUid("nt_"),
            title = t,
            body = b,
            type = type,
            province = nf.province,
            city = nf.city,
            region = nf.region,
            tierFilter = nf.tierFilter,
            sectionFilter = nf.sectionFilter,
            date = "هم‌اکنون",
            readBy = emptyList(),
            published = true,
            sortOrder = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.createNotification(notif)
                .onSuccess { showToast("نوتیفیکیشن هدفمند با موفقیت ارسال شد!") }
                .onFailure { err -> showToast(err.message ?: "خطا در ارسال نوتیفیکیشن") }
        }
        return true
    }

    fun adminDeleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
                .onSuccess { showToast("حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف اعلان") }
        }
    }

    fun adminSetHwAdStatus(ad: HwAdEntity, status: String) {
        viewModelScope.launch {
            repository.updateHwAdStatus(ad.id, status)
                .onSuccess { showToast("وضعیت به‌روزرسانی شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در به‌روزرسانی") }
        }
    }

    fun adminDeleteHwAd(id: String) {
        viewModelScope.launch {
            repository.deleteHwAd(id)
                .onSuccess { showToast("حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف آگهی") }
        }
    }

    fun adminApproveTutorial(tut: TutorialEntity) {
        viewModelScope.launch {
            repository.approveTutorial(tut.id)
                .onSuccess { showToast("وضعیت به‌روزرسانی شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تایید آموزش") }
        }
    }

    fun adminDeleteTutorial(id: String) {
        viewModelScope.launch {
            repository.deleteTutorial(id)
                .onSuccess { showToast("حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف") }
        }
    }

    fun adminApproveBox(box: BoxEntity) {
        viewModelScope.launch {
            repository.approveBox(box.id)
                .onSuccess { showToast("وضعیت به‌روزرسانی شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تایید باکس") }
        }
    }

    fun adminDeleteBox(id: String) {
        viewModelScope.launch {
            repository.deleteBox(id)
                .onSuccess { showToast("حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف") }
        }
    }

    fun adminAddStore(name: String, province: String, city: String, region: String, phone: String, type: String): Boolean {
        val n = name.trim()
        if (n.isEmpty()) {
            showToast("نام فروشگاه را وارد کنید")
            return false
        }
        val p = province.trim().ifEmpty { "تهران" }
        val c = city.trim().ifEmpty { "تهران" }
        val r = region.trim().ifEmpty { "مرکز" }
        val st = StoreEntity(
            id = generateUid("st_"),
            name = n,
            province = p,
            city = c,
            region = r,
            phone = phone.trim().ifEmpty { "021-00000000" },
            address = "$c، $r",
            hours = "۱۰ تا ۲۱",
            rating = "۵.۰",
            type = type,
            published = true
        )
        viewModelScope.launch {
            repository.createStore(st)
                .onSuccess { showToast("فروشگاه جدید اضافه شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در افزودن فروشگاه") }
        }
        return true
    }

    fun adminDeleteStore(id: String) {
        viewModelScope.launch {
            repository.deleteStore(id)
                .onSuccess { showToast("حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف فروشگاه") }
        }
    }

    fun adminUpdateOrderStatus(order: OrderEntity, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(order.id, status)
                .onSuccess { showToast("وضعیت سفارش تغییر کرد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر وضعیت") }
        }
    }

    fun adminDeleteOrder(id: String) {
        viewModelScope.launch {
            repository.deleteOrder(id)
                .onSuccess { showToast("حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف سفارش") }
        }
    }

    fun adminReplyInquiry(inq: InquiryEntity, status: String) {
        viewModelScope.launch {
            repository.updateInquiryStatus(inq.id, status)
                .onSuccess { showToast("پاسخ ارسال شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در ارسال پاسخ") }
        }
    }

    fun adminReplyHwRequest(req: HwRequestEntity, note: String) {
        viewModelScope.launch {
            repository.replyHwRequest(req.id, note.trim())
                .onSuccess { showToast("ثبت شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت پاسخ") }
        }
    }

    fun adminReplySwRequest(req: SwRequestEntity, reply: String) {
        viewModelScope.launch {
            repository.replySwRequest(req.id, reply.trim())
                .onSuccess { showToast("ثبت شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت پاسخ") }
        }
    }

    fun adminAddMobile(brand: String, model: String, priceText: String, info: String): Boolean {
        val b = brand.trim()
        val m = model.trim()
        val p = priceText.trim().toLongOrNull() ?: 0L
        if (b.isEmpty() || m.isEmpty() || p <= 0L) {
            showToast("برند، مدل و قیمت معتبر الزامی است")
            return false
        }
        val mId = generateUid("m_")
        val storeId = uiState.value.db.stores.firstOrNull()?.id ?: "st1"
        val mobile = MobileEntity(mId, b, m, "مدل جدید ثبت‌شده توسط مدیریت", "📱", -System.currentTimeMillis(), true)
        val offer = StoreMobileEntity(generateUid("sm_"), storeId, mId, p, info.trim().ifEmpty { "۱۸ ماه گارانتی شرکتی" }, true)
        viewModelScope.launch {
            repository.createMobileWithOffer(mobile, offer)
                .onSuccess { showToast("موبایل اضافه شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در افزودن موبایل") }
        }
        return true
    }

    fun adminAddAccessory(cat: String, name: String, priceText: String): Boolean {
        val n = name.trim()
        val p = priceText.trim().toLongOrNull() ?: 0L
        if (n.isEmpty() || p <= 0L) {
            showToast("نام محصول و قیمت معتبر الزامی است")
            return false
        }
        val storeId = uiState.value.db.stores.firstOrNull { it.type == "acc" || it.type == "both" }?.id ?: "st5"
        val acc = AccessoryEntity(
            id = generateUid("ac_"),
            cat = cat.trim().ifEmpty { "قاب و گارد" },
            name = n,
            storeId = storeId,
            price = p,
            desc = "ثبت‌شده توسط مدیریت بازمو",
            published = true
        )
        viewModelScope.launch {
            repository.createAccessory(acc)
                .onSuccess { showToast("محصول جانبی اضافه شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در افزودن محصول") }
        }
        return true
    }

    fun adminTogglePinPost(post: ForumPostEntity) {
        val next = !post.pinned
        viewModelScope.launch {
            repository.updateForumPostModeration(post.id, pinned = next, verifiedSolution = post.verifiedSolution)
                .onSuccess { showToast(if (next) "پست سنجاق شد" else "سنجاق برداشته شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر سنجاق") }
        }
    }

    fun adminToggleVerifyPost(post: ForumPostEntity) {
        val next = !post.verifiedSolution
        viewModelScope.launch {
            repository.updateForumPostModeration(post.id, pinned = post.pinned, verifiedSolution = next)
                .onSuccess { showToast("نشان تاییدیه ادمین تغییر کرد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر تاییدیه") }
        }
    }

    fun adminDeleteForumPost(id: String) {
        viewModelScope.launch {
            repository.deleteForumPost(id)
                .onSuccess { showToast("پست حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف پست") }
        }
    }

    fun adminResetDatabase() {
        viewModelScope.launch {
            repository.resetAllToDefaults(uid)
                .onSuccess { showToast("داده‌های پایه بازنشانی شدند") }
                .onFailure { err -> showToast(err.message ?: "خطا در بازنشانی داده‌ها") }
        }
    }

    fun adminToggleDemoData(visible: Boolean) {
        viewModelScope.launch {
            repository.setDemoVisibility(visible)
                .onSuccess {
                    showToast(
                        if (visible) "داده‌های نمونه کل ایران نمایش داده می‌شوند"
                        else "داده‌های نمونه مخفی شدند (اطلاعات واقعی حفظ شد)"
                    )
                }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر وضعیت نمایش داده‌های نمونه") }
        }
    }

    fun adminSeedDemoDataset() {
        if (sessionState.value.isSeedingDemo) {
            showToast("ساخت داده‌های نمونه در حال انجام است، لطفاً چند لحظه صبر کنید...")
            return
        }
        sessionState.update { it.copy(isSeedingDemo = true) }
        showToast("در حال ساخت داده‌های نمونه برای ۴۸ منطقه ایران...")
        viewModelScope.launch {
            try {
                repository.seedDemoDataset()
                    .onSuccess { showToast("داده‌های نمونه کل ایران با موفقیت ساخته شد") }
                    .onFailure { err -> showToast(err.message ?: "خطا در ساخت داده‌های نمونه") }
            } finally {
                sessionState.update { it.copy(isSeedingDemo = false) }
            }
        }
    }

    fun adminSetStoreStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateStoreStatus(id, status)
                .onSuccess { showToast(if (status == "approved") "فروشگاه تایید و فعال شد" else "فروشگاه رد شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر وضعیت فروشگاه") }
        }
    }

    fun adminSetDeskRentalStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateDeskRentalStatus(id, status)
                .onSuccess { showToast(if (status == "approved") "آگهی میز کار تایید شد" else "آگهی میز کار رد شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر وضعیت میز کار") }
        }
    }

    // ==================== STORE OWNER DASHBOARD ACTIONS ====================
    fun myOwnedStores(): List<StoreEntity> {
        val state = uiState.value
        return state.db.stores.filter {
            it.ownerId == uid || (state.session.isAdminUser && !it.isDemo)
        }
    }

    fun updateOwnStoreInfo(storeId: String, name: String, phone: String, address: String, hours: String) {
        val cleanName = name.trim().take(140)
        if (cleanName.isEmpty()) {
            showToast("نام فروشگاه نمی‌تواند خالی باشد")
            return
        }
        viewModelScope.launch {
            repository.updateStore(storeId, cleanName, phone.trim(), address.trim(), hours.trim().ifEmpty { "۱۰ تا ۲۰" })
                .onSuccess { showToast("مشخصات فروشگاه ذخیره شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در ذخیره مشخصات فروشگاه") }
        }
    }

    fun addStoreMobileOffer(
        storeId: String,
        mobileId: String,
        priceText: String,
        info: String,
        newBrand: String = "",
        newModel: String = "",
        newSpecs: String = ""
    ) {
        val price = priceText.trim().toLongOrNull() ?: 0L
        if (price <= 0L) {
            showToast("قیمت معتبر وارد کنید")
            return
        }
        if (mobileId.isBlank() && newModel.trim().isEmpty()) {
            showToast("مدل گوشی را انتخاب یا وارد کنید")
            return
        }
        viewModelScope.launch {
            repository.createStoreMobile(
                storeId = storeId,
                mobileId = mobileId,
                price = price,
                info = info.trim().ifEmpty { "۱۸ ماه گارانتی شرکتی" },
                newBrand = newBrand.trim(),
                newModel = newModel.trim(),
                newSpecs = newSpecs.trim()
            )
                .onSuccess {
                    closeModal()
                    showToast("موبایل به فروشگاه اضافه شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در افزودن محصول") }
        }
    }

    fun updateStoreMobileOffer(id: String, priceText: String, info: String) {
        val price = priceText.trim().toLongOrNull() ?: 0L
        if (price <= 0L) {
            showToast("قیمت معتبر وارد کنید")
            return
        }
        viewModelScope.launch {
            repository.updateStoreMobile(id, price, info.trim())
                .onSuccess { showToast("قیمت موبایل بروزرسانی شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در ویرایش قیمت") }
        }
    }

    fun deleteStoreMobileOffer(id: String) {
        viewModelScope.launch {
            repository.deleteStoreMobile(id)
                .onSuccess { showToast("از لیست فروشگاه حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف محصول") }
        }
    }

    fun addStoreAccessory(storeId: String, cat: String, name: String, priceText: String, desc: String) {
        val cleanName = name.trim()
        val price = priceText.trim().toLongOrNull() ?: 0L
        if (cleanName.isEmpty() || price <= 0L) {
            showToast("نام و قیمت معتبر الزامی است")
            return
        }
        val acc = AccessoryEntity(
            id = generateUid("ac_"),
            cat = cat.trim().ifEmpty { "قاب و گارد" },
            name = cleanName,
            storeId = storeId,
            price = price,
            desc = desc.trim(),
            published = true
        )
        viewModelScope.launch {
            repository.createAccessory(acc)
                .onSuccess {
                    closeModal()
                    showToast("کالای جانبی به فروشگاه اضافه شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در افزودن کالا") }
        }
    }

    fun updateStoreAccessoryPrice(id: String, priceText: String) {
        val price = priceText.trim().toLongOrNull() ?: 0L
        if (price <= 0L) {
            showToast("قیمت معتبر وارد کنید")
            return
        }
        viewModelScope.launch {
            repository.updateAccessory(id, price)
                .onSuccess { showToast("قیمت کالای جانبی بروزرسانی شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در ویرایش قیمت") }
        }
    }

    fun deleteStoreAccessory(id: String) {
        viewModelScope.launch {
            repository.deleteAccessory(id)
                .onSuccess { showToast("کالای جانبی حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف کالا") }
        }
    }

    // ==================== DESK RENTALS & COOPERATION REQUESTS ====================
    fun submitDeskRental(
        title: String,
        category: String,
        storeName: String,
        province: String,
        city: String,
        region: String,
        rentDailyText: String,
        rentMonthlyText: String,
        cooperationModel: String,
        equipment: String,
        phone: String,
        desc: String
    ) {
        val cleanTitle = title.trim().take(180)
        if (cleanTitle.isEmpty()) {
            showToast("عنوان آگهی میز کار الزامی است")
            return
        }
        val isAdmin = sessionState.value.isAdminUser
        val rental = DeskRentalEntity(
            id = generateUid("dr_"),
            title = cleanTitle,
            category = category.ifBlank { "تعمیرات سخت‌افزار" },
            storeName = storeName.trim(),
            province = province.ifBlank { sessionState.value.province },
            city = city.ifBlank { sessionState.value.city },
            region = region.ifBlank { sessionState.value.region },
            rentDaily = rentDailyText.trim().toLongOrNull() ?: 0L,
            rentMonthly = rentMonthlyText.trim().toLongOrNull() ?: 0L,
            cooperationModel = cooperationModel.trim(),
            equipment = equipment.trim(),
            phone = phone.trim().ifEmpty { sessionState.value.currentUser?.phone.orEmpty() },
            ownerId = uid,
            ownerName = uiState.value.activeDisplayName,
            status = if (isAdmin) "approved" else "pending",
            desc = desc.trim(),
            sortOrder = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.createDeskRental(rental)
                .onSuccess {
                    closeModal()
                    showToast(
                        if (isAdmin) "آگهی میز کار منتشر شد"
                        else "آگهی میز کار ثبت شد و پس از تایید مدیریت نمایش داده می‌شود"
                    )
                }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت میز کار") }
        }
    }

    fun deleteDeskRental(id: String) {
        viewModelScope.launch {
            repository.deleteDeskRental(id)
                .onSuccess { showToast("آگهی میز کار حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف میز کار") }
        }
    }

    fun submitDeskRequest(
        title: String,
        category: String,
        province: String,
        city: String,
        region: String,
        budgetText: String,
        phone: String,
        proposalText: String
    ) {
        val cleanTitle = title.trim().take(180)
        val cleanProposal = proposalText.trim()
        if (cleanTitle.isEmpty() || cleanProposal.isEmpty()) {
            showToast("عنوان و متن پیشنهاد همکاری الزامی است")
            return
        }
        val req = DeskRequestEntity(
            id = generateUid("drq_"),
            title = cleanTitle,
            category = category.ifBlank { "تعمیرات سخت‌افزار" },
            requesterName = uiState.value.activeDisplayName,
            requesterPhone = phone.trim().ifEmpty { sessionState.value.currentUser?.phone.orEmpty() },
            province = province.ifBlank { sessionState.value.province },
            city = city.ifBlank { sessionState.value.city },
            region = region.ifBlank { sessionState.value.region },
            proposedBudget = budgetText.trim().toLongOrNull() ?: 0L,
            proposalText = cleanProposal,
            ownerId = uid,
            status = "open",
            sortOrder = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.createDeskRequest(req)
                .onSuccess {
                    closeModal()
                    showToast("درخواست و پیشنهاد اجاره میز ثبت شد")
                }
                .onFailure { err -> showToast(err.message ?: "خطا در ثبت درخواست") }
        }
    }

    fun updateDeskRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateDeskRequestStatus(id, status)
                .onSuccess { showToast("وضعیت درخواست تغییر کرد") }
                .onFailure { err -> showToast(err.message ?: "خطا در تغییر وضعیت") }
        }
    }

    fun deleteDeskRequest(id: String) {
        viewModelScope.launch {
            repository.deleteDeskRequest(id)
                .onSuccess { showToast("درخواست حذف شد") }
                .onFailure { err -> showToast(err.message ?: "خطا در حذف درخواست") }
        }
    }

    fun provincesForLocation(): List<String> = BazmoLocations.provinces()

    companion object {
        fun factory(applicationContext: Context, user: BazmoSessionUser): ViewModelProvider.Factory {
            val baseUrl = BazmoServerConfig.baseUrl()
            val isSuperAdmin = BazmoAuthManager.isSuperAdmin(user)
            val uid = user.uid
            val email = user.email
            val displayName = user.displayName
            val authToken = user.token
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BazmoViewModel(
                        appContext = applicationContext,
                        baseUrl = baseUrl,
                        authToken = authToken,
                        uid = uid,
                        email = email,
                        displayName = displayName,
                        initialIsSuperAdmin = isSuperAdmin
                    ) as T
                }
            }
        }
    }
}
