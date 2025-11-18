package com.arcadia.trivora

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object BiometricLogin {

    private const val KEY_NAME = "trivora_biometric_key"
    private const val KEYSTORE_NAME = "AndroidKeyStore"
    private const val SHARED_PREFS_NAME = "biometric_prefs"
    private const val IV_KEY = "biometric_iv"

    interface BiometricCallback {
        fun onBiometricSuccess()
        fun onBiometricError(errorCode: Int, errString: CharSequence)
        fun onBiometricFailed()
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            else -> false
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Authenticate with Biometrics",
        subtitle: String = "Please authenticate to access your account",
        negativeButtonText: String = "Use Password",
        callback: BiometricCallback
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.onBiometricError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Store that user successfully used biometrics
                    saveBiometricPreference(activity, true)
                    callback.onBiometricSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.onBiometricFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setConfirmationRequired(false)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun saveBiometricPreference(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("biometric_enabled", true)
    }

    fun canUseBiometric(context: Context): Boolean {
        return isBiometricAvailable(context) && isBiometricEnabled(context)
    }
}