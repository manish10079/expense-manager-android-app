package com.mkn0079.expensetracker.utils

import android.content.Context
import android.content.ContextWrapper
import com.mkn0079.expensetracker.R
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

private const val BIOMETRIC_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG

data class BiometricAvailability(
    val isAvailable: Boolean,
    @androidx.annotation.StringRes val messageRes: Int? = null
)

object BiometricAuthManager {

    fun getAvailability(context: Context): BiometricAvailability {
        return when (BiometricManager.from(context).canAuthenticate(BIOMETRIC_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability(isAvailable = true)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability(
                isAvailable = false,
                messageRes = R.string.msg_set_up_fingerprint_or_face_unl
            )

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability(
                isAvailable = false,
                messageRes = R.string.msg_this_device_does_not_support_b
            )

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability(
                isAvailable = false,
                messageRes = R.string.msg_biometric_hardware_is_currentl
            )

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability(
                isAvailable = false,
                messageRes = R.string.msg_a_security_update_is_required
            )

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability(
                isAvailable = false,
                messageRes = R.string.msg_biometric_authentication_is_no
            )

            else -> BiometricAvailability(
                isAvailable = false,
                messageRes = R.string.msg_error_unable_to_start_biometric_auth
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
        private val activity: FragmentActivity
    ) {
        fun authenticate(
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
