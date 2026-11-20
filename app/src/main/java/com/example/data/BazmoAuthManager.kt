package com.example.data

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Keep
data class BazmoSessionUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val token: String = "",
    val isAdmin: Boolean = false,
    val isEmailVerified: Boolean = true
)

@Keep
data class BazmoAuthResponse(
    val token: String = "",
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val emailVerified: Boolean = true,
    val isAdmin: Boolean = false,
    val status: String = "not_invited",
    val user: UserEntity? = null
)

object BazmoAuthManager {
    private const val PREFS_NAME = "bazmo_auth_session"
    private const val KEY_UID = "uid"
    private const val KEY_EMAIL = "email"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_TOKEN = "token"
    private const val KEY_IS_ADMIN = "is_admin"
    private const val KEY_VERIFIED = "email_verified"

    const val SUPER_ADMIN_EMAIL = "majidboushi777@gmail.com"
    val SUPER_ADMIN_EMAILS = setOf(
        "majidboushi777@gmail.com",
        "majid.boushi77@gmail.com"
    )

    private val _sessionFlow = MutableStateFlow<BazmoSessionUser?>(null)
    private val _savedAccountFlow = MutableStateFlow<BazmoSessionUser?>(null)
    @Volatile
    private var prefsLoaded = false

    private fun ensureLoaded(context: Context) {
        if (!prefsLoaded) {
            synchronized(this) {
                if (!prefsLoaded) {
                    _savedAccountFlow.value = readFromPrefs(context.applicationContext)
                    prefsLoaded = true
                }
            }
        }
    }

    fun currentSession(context: Context): BazmoSessionUser? {
        ensureLoaded(context)
        return _sessionFlow.value
    }

    fun savedAccount(context: Context): BazmoSessionUser? {
        ensureLoaded(context)
        return _savedAccountFlow.value
    }

    fun authStateFlow(context: Context): Flow<BazmoSessionUser?> {
        ensureLoaded(context)
        return _sessionFlow.asStateFlow()
    }

    fun savedAccountFlow(context: Context): Flow<BazmoSessionUser?> {
        ensureLoaded(context)
        return _savedAccountFlow.asStateFlow()
    }

    fun isSuperAdmin(user: BazmoSessionUser?): Boolean {
        if (user == null || !user.isEmailVerified) return false
        if (user.isAdmin) return true
        val email = user.email.trim().lowercase()
        return email in SUPER_ADMIN_EMAILS
    }

    fun unlockSavedAccountWithBiometric(context: Context): Result<BazmoSessionUser> {
        ensureLoaded(context)
        val saved = _savedAccountFlow.value ?: readFromPrefs(context.applicationContext)
        if (saved == null || saved.token.isBlank() || saved.uid.isBlank()) {
            return Result.failure(IllegalStateException("هیچ حساب ذخیره‌شده‌ای برای ورود با اثر انگشت وجود ندارد."))
        }
        _savedAccountFlow.value = saved
        _sessionFlow.value = saved
        return Result.success(saved)
    }

    suspend fun refreshActiveSession(context: Context): Result<BazmoSessionUser?> {
        ensureLoaded(context)
        val existing = _sessionFlow.value ?: return Result.success(null)
        if (existing.token.isBlank()) return Result.success(existing)
        val verifyRes = verifySession(existing.token)
        if (verifyRes.isSuccess) {
            val resp = verifyRes.getOrThrow()
            val refreshed = existing.copy(
                uid = resp.uid.ifBlank { existing.uid },
                email = resp.email.ifBlank { existing.email },
                displayName = resp.displayName.ifBlank { existing.displayName },
                token = resp.token.ifBlank { existing.token },
                isAdmin = resp.isAdmin,
                isEmailVerified = resp.emailVerified
            )
            saveToPrefs(context.applicationContext, refreshed)
            _savedAccountFlow.value = refreshed
            if (_sessionFlow.value?.uid == refreshed.uid) {
                _sessionFlow.value = refreshed
            }
            return Result.success(refreshed)
        }
        val err = verifyRes.exceptionOrNull()
        if (err is BazmoApiException && err.statusCode == 401) {
            clearPrefs(context.applicationContext)
            _savedAccountFlow.value = null
            _sessionFlow.value = null
            return Result.failure(err)
        }
        // Keep cached unlocked session during transient offline state
        return Result.success(existing)
    }

    suspend fun attemptAutoSignIn(activityContext: Context): Result<BazmoSessionUser?> {
        ensureLoaded(activityContext)
        if (_sessionFlow.value != null) {
            return refreshActiveSession(activityContext)
        }
        // Keep cold start gated behind biometric unlock (or explicit password login)
        // when a previous account is saved, until explicit signOut clears it.
        return Result.success(null)
    }

    internal fun resetInMemoryForTest() {
        synchronized(this) {
            _sessionFlow.value = null
            _savedAccountFlow.value = null
            prefsLoaded = false
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<BazmoSessionUser> {
        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val webClientId = activityContext.getString(R.string.default_web_client_id)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                exchangeGoogleIdToken(activityContext, googleIdTokenCredential.idToken)
            } else {
                Result.failure(IllegalStateException("نوع اعتبارنامه دریافتشده معتبر نیست."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("BazmoAuth", "Interactive Google Sign-In was cancelled", e)
            Result.failure(Exception("ورود با حساب گوگل لغو شد. لطفاً یک حساب را انتخاب کنید.", e))
        } catch (e: NoCredentialException) {
            Log.w("BazmoAuth", "No Google account available on device", e)
            Result.failure(Exception("هیچ حساب گوگلی روی دستگاه یافت نشد.", e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("BazmoAuth", "Google Sign-In failed", e)
            val raw = e.localizedMessage.orEmpty()
            val cleanMsg = when {
                raw.contains("network", ignoreCase = true) ||
                    raw.contains("unable to resolve host", ignoreCase = true) ->
                    "خطا در اتصال به سرور بازمو. لطفاً اینترنت را بررسی کنید."
                else -> raw.ifBlank { "خطا در ورود با گوگل. لطفاً دوباره تلاش کنید." }
            }
            Result.failure(Exception(cleanMsg, e))
        }
    }

    suspend fun signInWithPassword(
        context: Context,
        email: String,
        password: String
    ): Result<BazmoSessionUser> = withContext(Dispatchers.IO) {
        val cleanEmail = normalizeEmail(email)
        if (!isValidEmailFormat(cleanEmail)) {
            return@withContext Result.failure(Exception("لطفاً نام کاربری (ایمیل معتبر) را وارد کنید."))
        }
        if (password.isEmpty()) {
            return@withContext Result.failure(Exception("لطفاً رمز عبور را وارد کنید."))
        }
        try {
            val payload = JSONObject()
                .put("email", cleanEmail)
                .put("password", password)
                .toString()
            val req = Request.Builder()
                .url("${BazmoServerConfig.baseUrl()}/api/auth/login")
                .post(payload.toRequestBody(BazmoServerConfig.JSON_MEDIA_TYPE))
                .build()
            BazmoServerConfig.okHttpClient.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    val msg = extractErrorMessage(
                        body,
                        "نام کاربری یا رمز عبور اشتباه است (کد ${res.code})."
                    )
                    return@withContext Result.failure(BazmoApiException(res.code, msg))
                }
                parseAndSaveAuthResponse(context, body)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleApiError(e, OperationType.WRITE, "/api/auth/login", null)
        }
    }

    suspend fun verifySession(token: String): Result<BazmoAuthResponse> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("${BazmoServerConfig.baseUrl()}/api/auth/session")
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            BazmoServerConfig.okHttpClient.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    val msg = extractErrorMessage(body, "نشست کاربری معتبر نیست.")
                    return@withContext Result.failure(BazmoApiException(res.code, msg))
                }
                val adapter = BazmoServerConfig.moshi.adapter(BazmoAuthResponse::class.java)
                val parsed = adapter.fromJson(body)
                    ?: return@withContext Result.failure(BazmoApiException(500, "پاسخ نامعتبر از سرور."))
                Result.success(parsed)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun exchangeGoogleIdToken(
        context: Context,
        idToken: String
    ): Result<BazmoSessionUser> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().put("idToken", idToken).toString()
            val req = Request.Builder()
                .url("${BazmoServerConfig.baseUrl()}/api/auth/google")
                .post(payload.toRequestBody(BazmoServerConfig.JSON_MEDIA_TYPE))
                .build()
            BazmoServerConfig.okHttpClient.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    val msg = extractErrorMessage(
                        body,
                        "خطا در تایید حساب گوگل روی سرور بازمو (کد ${res.code})."
                    )
                    return@withContext Result.failure(BazmoApiException(res.code, msg))
                }
                parseAndSaveAuthResponse(context, body)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleApiError(e, OperationType.WRITE, "/api/auth/google", null)
        }
    }

    private fun parseAndSaveAuthResponse(context: Context, body: String): Result<BazmoSessionUser> {
        val adapter = BazmoServerConfig.moshi.adapter(BazmoAuthResponse::class.java)
        val parsed = adapter.fromJson(body)
            ?: return Result.failure(BazmoApiException(500, "پاسخ نامعتبر از سرور."))
        if (parsed.token.isBlank() || parsed.uid.isBlank()) {
            return Result.failure(BazmoApiException(500, "اطلاعات نشست از سرور دریافت نشد."))
        }
        val session = BazmoSessionUser(
            uid = parsed.uid,
            email = parsed.email,
            displayName = parsed.displayName,
            token = parsed.token,
            isAdmin = parsed.isAdmin,
            isEmailVerified = parsed.emailVerified
        )
        saveToPrefs(context.applicationContext, session)
        prefsLoaded = true
        _savedAccountFlow.value = session
        _sessionFlow.value = session
        return Result.success(session)
    }

    suspend fun signOut(
        context: Context,
        clearCredentialManager: Boolean = true,
        beforeAuthSignOut: suspend () -> Unit = {}
    ) = withContext(NonCancellable) {
        try {
            beforeAuthSignOut()
        } catch (e: Exception) {
            Log.w("BazmoAuth", "Error during pre-signOut cleanup", e)
        }
        clearPrefs(context.applicationContext)
        prefsLoaded = true
        _savedAccountFlow.value = null
        _sessionFlow.value = null
        if (clearCredentialManager) {
            try {
                withTimeoutOrNull(1500L) {
                    CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
                }
            } catch (e: Exception) {
                Log.w("BazmoAuth", "clearCredentialState failed", e)
            }
        }
    }

    private fun extractErrorMessage(body: String, fallback: String): String {
        return runCatching {
            JSONObject(body).optString("error").ifBlank { fallback }
        }.getOrDefault(fallback)
    }

    private fun readFromPrefs(context: Context): BazmoSessionUser? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_TOKEN, null).orEmpty()
        val uid = prefs.getString(KEY_UID, null).orEmpty()
        if (token.isBlank() || uid.isBlank()) return null
        return BazmoSessionUser(
            uid = uid,
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
            displayName = prefs.getString(KEY_DISPLAY_NAME, "").orEmpty(),
            token = token,
            isAdmin = prefs.getBoolean(KEY_IS_ADMIN, false),
            isEmailVerified = prefs.getBoolean(KEY_VERIFIED, true)
        )
    }

    private fun saveToPrefs(context: Context, session: BazmoSessionUser) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_UID, session.uid)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_TOKEN, session.token)
            .putBoolean(KEY_IS_ADMIN, session.isAdmin)
            .putBoolean(KEY_VERIFIED, session.isEmailVerified)
            .apply()
    }

    private fun clearPrefs(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
