package com.mknlabs.expensetracker.feature.auth.ui

import com.mknlabs.expensetracker.R
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun signup_already_in_use_message() {
        val error = RuntimeException("The email address is already in use by another account.")
        assertEquals(R.string.error_auth_email_already_in_use, mapFirebaseAuthError(error, isSignUp = true))
    }

    @Test
    fun signup_invalid_credential_treated_as_already_registered() {
        val error = RuntimeException("ERROR_INVALID_CREDENTIAL: invalid-credential")
        assertEquals(R.string.error_auth_email_already_in_use, mapFirebaseAuthError(error, isSignUp = true))
    }

    @Test
    fun signin_invalid_credential_stays_invalid_credentials() {
        val error = RuntimeException("invalid-credential")
        assertEquals(R.string.error_auth_invalid_credentials, mapFirebaseAuthError(error, isSignUp = false))
    }

    @Test
    fun signin_user_not_found() {
        val error = RuntimeException("There is no user record corresponding to this identifier. user-not-found")
        assertEquals(R.string.error_auth_user_not_found, mapFirebaseAuthError(error, isSignUp = false))
    }

    @Test
    fun signin_wrong_password() {
        val error = RuntimeException("wrong-password")
        assertEquals(R.string.error_auth_wrong_password, mapFirebaseAuthError(error, isSignUp = false))
    }

    @Test
    fun weak_password() {
        val error = RuntimeException("Password should be at least 6 characters. weak-password")
        assertEquals(R.string.error_auth_weak_password, mapFirebaseAuthError(error, isSignUp = true))
    }

    @Test
    fun invalid_email() {
        val error = RuntimeException("The email address is badly formatted.")
        assertEquals(R.string.error_invalid_email, mapFirebaseAuthError(error, isSignUp = true))
    }

    @Test
    fun normalize_error_codes() {
        assertEquals("EMAIL_ALREADY_IN_USE", normalizeAuthErrorCode("ERROR_EMAIL_ALREADY_IN_USE"))
        assertEquals("EMAIL_ALREADY_IN_USE", normalizeAuthErrorCode("auth/email-already-in-use"))
        assertEquals("INVALID_CREDENTIAL", normalizeAuthErrorCode("ERROR_INVALID_CREDENTIAL"))
    }
}
