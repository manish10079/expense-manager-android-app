package com.mknlabs.expensetracker.feature.auth.ui

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.mknlabs.expensetracker.R
import java.net.UnknownHostException

/**
 * Maps Firebase / network auth failures to user-facing string resources.
 *
 * [isSignUp] changes how enumeration-protected codes are shown: on sign-up,
 * "invalid credential" almost always means the email is already registered.
 */
fun mapFirebaseAuthError(error: Throwable, isSignUp: Boolean = false): Int {
    var current: Throwable? = error
    while (current != null) {
        when (current) {
            is FirebaseAuthUserCollisionException -> return R.string.error_auth_email_already_in_use
            is FirebaseAuthWeakPasswordException -> return R.string.error_auth_weak_password
            is FirebaseAuthInvalidUserException -> {
                return when (normalizeAuthErrorCode(current.errorCode)) {
                    "USER_DISABLED" -> R.string.error_auth_user_disabled
                    "USER_NOT_FOUND" -> if (isSignUp) {
                        R.string.error_auth_email_already_in_use
                    } else {
                        R.string.error_auth_user_not_found
                    }
                    else -> R.string.error_auth_user_not_found
                }
            }
            is FirebaseAuthException -> {
                val mapped = mapNormalizedAuthCode(normalizeAuthErrorCode(current.errorCode), isSignUp)
                if (mapped != null) return mapped
            }
            is UnknownHostException -> return R.string.error_no_internet
        }
        current = current.cause
    }

    val message = (error.message ?: "").lowercase()
    return when {
        message.contains("email-already-in-use") ||
            message.contains("email_already_in_use") ||
            message.contains("already in use") ||
            message.contains("already exists") ||
            message.contains("already-in-use") -> R.string.error_auth_email_already_in_use
        message.contains("weak-password") || message.contains("weak_password") ->
            R.string.error_auth_weak_password
        message.contains("user-disabled") || message.contains("user_disabled") ->
            R.string.error_auth_user_disabled
        message.contains("too-many-requests") || message.contains("too_many_requests") ->
            R.string.error_auth_too_many_requests
        message.contains("invalid-email") || message.contains("invalid_email") ||
            message.contains("badly formatted") -> R.string.error_invalid_email
        message.contains("network") || message.contains("unable to resolve host") ->
            R.string.error_no_internet
        message.contains("user-not-found") || message.contains("user_not_found") ->
            if (isSignUp) R.string.error_auth_email_already_in_use else R.string.error_auth_user_not_found
        message.contains("wrong-password") || message.contains("wrong_password") ->
            if (isSignUp) R.string.error_auth_email_already_in_use else R.string.error_auth_wrong_password
        message.contains("invalid-credential") || message.contains("invalid_credential") ||
            message.contains("invalid-login-credentials") ||
            message.contains("invalid_login_credentials") ->
            if (isSignUp) R.string.error_auth_email_already_in_use else R.string.error_auth_invalid_credentials
        else -> R.string.error_auth_generic_fail
    }
}

internal fun normalizeAuthErrorCode(code: String?): String {
    if (code.isNullOrBlank()) return ""
    return code
        .removePrefix("ERROR_")
        .removePrefix("auth/")
        .uppercase()
        .replace('-', '_')
}

private fun mapNormalizedAuthCode(code: String, isSignUp: Boolean): Int? = when (code) {
    "EMAIL_ALREADY_IN_USE",
    "CREDENTIAL_ALREADY_IN_USE",
    "ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> R.string.error_auth_email_already_in_use
    "WEAK_PASSWORD" -> R.string.error_auth_weak_password
    "USER_DISABLED" -> R.string.error_auth_user_disabled
    "TOO_MANY_REQUESTS" -> R.string.error_auth_too_many_requests
    "INVALID_EMAIL" -> R.string.error_invalid_email
    "NETWORK_REQUEST_FAILED" -> R.string.error_no_internet
    "OPERATION_NOT_ALLOWED" -> R.string.error_auth_operation_not_allowed
    "MISSING_PASSWORD" -> R.string.error_auth_wrong_password
    "USER_NOT_FOUND" ->
        if (isSignUp) R.string.error_auth_email_already_in_use else R.string.error_auth_user_not_found
    "WRONG_PASSWORD" ->
        if (isSignUp) R.string.error_auth_email_already_in_use else R.string.error_auth_wrong_password
    "INVALID_CREDENTIAL",
    "INVALID_LOGIN_CREDENTIALS",
    "INVALID_LOGIN_CREDENTIAL" ->
        if (isSignUp) R.string.error_auth_email_already_in_use else R.string.error_auth_invalid_credentials
    else -> null
}
