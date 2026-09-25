package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest
import java.util.Locale

enum class LockType(val title: String, val length: Int) {
    PIN_4("پین‌کد ۴ رقمی", 4),
    PIN_6("پین‌کد ۶ رقمی", 6),
    PASSWORD("گذرواژه متنی", 0)
}

enum class AutoLockDuration(val title: String, val millis: Long) {
    IMMEDIATELY("بلافاصله پس از خروج", 0L),
    ONE_MINUTE("پس از ۱ دقیقه", 60_000L),
    FIVE_MINUTES("پس از ۵ دقیقه", 300_000L)
}

enum class BiometricStatus {
    AVAILABLE,
    NOT_ENROLLED,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE
}

class SecurityManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_LOCK_TYPE = "lock_type"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTO_LOCK_DURATION = "auto_lock_duration"
        private const val KEY_SCREEN_SECURITY = "screen_security_enabled"
        private const val KEY_LAST_BACKGROUND_TIME = "last_background_timestamp"
        private const val SALT = "FinSecure_App_Salt_2026_@!"
    }

    private fun hash(input: String): String {
        val bytes = (input + SALT).toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { String.format(Locale.US, "%02x", it) }
    }

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    fun hasPasscode(): Boolean {
        val hash = prefs.getString(KEY_PIN_HASH, null)
        return !hash.isNullOrBlank()
    }

    fun setPasscode(passcode: String) {
        val hashed = hash(passcode)
        prefs.edit().putString(KEY_PIN_HASH, hashed).apply()
    }

    fun verifyPasscode(input: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return stored == hash(input)
    }

    fun removePasscode() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_APP_LOCK_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    fun getLockType(): LockType {
        val name = prefs.getString(KEY_LOCK_TYPE, LockType.PIN_4.name)
        return try {
            LockType.valueOf(name ?: LockType.PIN_4.name)
        } catch (e: Exception) {
            LockType.PIN_4
        }
    }

    fun setLockType(lockType: LockType) {
        prefs.edit().putString(KEY_LOCK_TYPE, lockType.name).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getAutoLockDuration(): AutoLockDuration {
        val name = prefs.getString(KEY_AUTO_LOCK_DURATION, AutoLockDuration.IMMEDIATELY.name)
        return try {
            AutoLockDuration.valueOf(name ?: AutoLockDuration.IMMEDIATELY.name)
        } catch (e: Exception) {
            AutoLockDuration.IMMEDIATELY
        }
    }

    fun setAutoLockDuration(duration: AutoLockDuration) {
        prefs.edit().putString(KEY_AUTO_LOCK_DURATION, duration.name).apply()
    }

    fun isScreenSecurityEnabled(): Boolean {
        val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic")
            || android.os.Build.FINGERPRINT.startsWith("unknown")
            || android.os.Build.MODEL.contains("google_sdk")
            || android.os.Build.MODEL.contains("Emulator")
            || android.os.Build.MODEL.contains("Android SDK built for x86")
            || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
            || "google_sdk" == android.os.Build.PRODUCT

        if (isEmulator || com.example.BuildConfig.DEBUG) {
            return false
        }
        return prefs.getBoolean(KEY_SCREEN_SECURITY, false)
    }

    fun setScreenSecurityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_SECURITY, enabled).apply()
    }

    fun recordBackgroundTimestamp() {
        prefs.edit().putLong(KEY_LAST_BACKGROUND_TIME, System.currentTimeMillis()).apply()
    }

    fun shouldLockOnResume(): Boolean {
        if (!isAppLockEnabled() || !hasPasscode()) return false
        val lastTime = prefs.getLong(KEY_LAST_BACKGROUND_TIME, 0L)
        if (lastTime == 0L) return true
        val duration = getAutoLockDuration()
        val elapsed = System.currentTimeMillis() - lastTime
        return elapsed >= duration.millis
    }

    fun checkBiometricStatus(): BiometricStatus {
        return try {
            val bm = BiometricManager.from(context)
            when (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
                else -> BiometricStatus.HARDWARE_UNAVAILABLE
            }
        } catch (e: Exception) {
            BiometricStatus.HARDWARE_UNAVAILABLE
        }
    }

    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        title: String = "احراز هویت بیومتریک",
        subtitle: String = "برای ورود اثر انگشت خود را اسکن کنید",
        negativeButtonText: String = "ورود با رمز عبور",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        prompt.authenticate(promptInfo)
    }
}
