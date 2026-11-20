package com.example.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BazmoBiometricHelper {
    private const val ALLOWED_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun canAuthenticate(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun promptBiometricUnlock(
        activity: FragmentActivity,
        accountLabel: String,
        onSuccess: () -> Unit,
        onCancelledOrFallback: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (!canAuthenticate(activity)) {
            onError("اثر انگشت روی این دستگاه فعال یا ثبت نشده است.")
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_CANCELED -> {
                        onCancelledOrFallback()
                    }
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        onError("احراز هویت با اثر انگشت موقتاً قفل شده است. لطفاً با رمز عبور وارد شوید.")
                    }
                    BiometricPrompt.ERROR_NO_BIOMETRICS,
                    BiometricPrompt.ERROR_HW_NOT_PRESENT,
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                        onError("حسگر اثر انگشت در دسترس نیست. لطفاً از رمز عبور استفاده کنید.")
                    }
                    else -> {
                        val msg = errString.toString().trim()
                        onError(if (msg.isNotBlank()) msg else "خطا در تایید اثر انگشت.")
                    }
                }
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val subtitleText = if (accountLabel.isNotBlank()) {
            "حساب ذخیره‌شده: $accountLabel"
        } else {
            "ورود به آخرین حساب کاربری فعال"
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ورود با اثر انگشت به بازمو")
            .setSubtitle(subtitleText)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .setConfirmationRequired(false)
            .setNegativeButtonText("استفاده از رمز عبور")
            .build()

        prompt.authenticate(promptInfo)
    }
}
