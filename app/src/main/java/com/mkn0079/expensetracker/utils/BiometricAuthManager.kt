package com.mkn0079.expensetracker.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

private const val BIOMETRIC_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK

data class BiometricAvailability(
    val isAvailable: Boolean,
    val message: String? = null
)

object BiometricAuthManager {

    fun getAvailability(context: Context): BiometricAvailability {
        return when (BiometricManager.from(context).canAuthenticate(BIOMETRIC_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability(isAvailable = true)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability(
                isAvailable = false,
                message = "Set up fingerprint or face unlock in phone settings first."
            )

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability(
                isAvailable = false,
                message = "This device does not support biometric authentication."
            )

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability(
                isAvailable = false,
                message = "Biometric hardware is currently unavailable."
            )

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability(
                isAvailable = false,
                message = "A security update is required before biometric unlock can be used."
            )

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability(
                isAvailable = false,
                message = "Biometric authentication is not available right now."
            )

            else -> BiometricAvailability(
                isAvailable = false,
                message = "Unable to start biometric authentication on this device."
            )
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String? = null,
        negativeButtonText: String = "Cancel",
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit = {},
        onCancel: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> onCancel()

                        else -> onFailure(errString.toString())
                    }
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_AUTHENTICATORS)
            .setNegativeButtonText(negativeButtonText)
            .apply {
                description?.let(::setDescription)
            }
            .build()

        prompt.authenticate(promptInfo)
    }

    fun createAuthenticator(activity: FragmentActivity): BiometricAuthenticator {
        return BiometricAuthenticator(activity)
    }

    class BiometricAuthenticator internal constructor(
        activity: FragmentActivity
    ) {
        private var onSuccess: () -> Unit = {}
        private var onFailure: (String) -> Unit = {}
        private var onCancel: () -> Unit = {}
        private val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> onCancel()

                        else -> onFailure(errString.toString())
                    }
                }
            }
        )

        fun authenticate(
            title: String,
            subtitle: String,
            description: String? = null,
            negativeButtonText: String = "Cancel",
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit = {},
            onCancel: () -> Unit = {}
        ) {
            this.onSuccess = onSuccess
            this.onFailure = onFailure
            this.onCancel = onCancel

            prompt.authenticate(
                buildPromptInfo(
                    title = title,
                    subtitle = subtitle,
                    description = description,
                    negativeButtonText = negativeButtonText
                )
            )
        }
    }

    private fun buildPromptInfo(
        title: String,
        subtitle: String,
        description: String?,
        negativeButtonText: String
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_AUTHENTICATORS)
            .setNegativeButtonText(negativeButtonText)
            .apply {
                description?.let(::setDescription)
            }
            .build()
    }
}

fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
