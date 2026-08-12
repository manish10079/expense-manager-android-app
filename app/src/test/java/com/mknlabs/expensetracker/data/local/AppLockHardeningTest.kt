package com.mknlabs.expensetracker.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the Item 10 app-lock hardening primitives in [AppLockPreferences]:
 * the PBKDF2 slow hash (pure function, JVM-verifiable) and the brute-force
 * lockout duration escalation (pure function).
 *
 * The pure [AppLockPreferences.pbkdf2Hash] core takes raw salt bytes precisely so
 * JVM unit tests can call it without android.util.Base64 (a stub that returns null).
 * The PIN/answer save + validate paths need a Context +
 * EncryptedSharedPreferences, so they are exercised by instrumented/manual tests
 * instead (mirroring the existing AppLockPreferencesTest pattern).
 */
class AppLockHardeningTest {

    // --- PBKDF2 slow hash -------------------------------------------------

    @Test
    fun pbkdf2Hash_matchesKnownTestVector() {
        // RFC 7914-style known answer: PBKDF2-HMAC-SHA256(P="password", S="salt", c=1, dkLen=32)
        val hash = AppLockPreferences.pbkdf2Hash(
            normalizedSecret = "password",
            saltBytes = "salt".toByteArray(),
            iterations = 1,
            algorithm = "PBKDF2WithHmacSHA256"
        )
        assertEquals(
            "120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b",
            hash
        )
    }

    @Test
    fun pbkdf2Hash_isDeterministic() {
        val a = AppLockPreferences.pbkdf2Hash("1234", "salt".toByteArray(), 1000, "PBKDF2WithHmacSHA256")
        val b = AppLockPreferences.pbkdf2Hash("1234", "salt".toByteArray(), 1000, "PBKDF2WithHmacSHA256")
        assertEquals(a, b)
    }

    @Test
    fun pbkdf2Hash_changesWithSalt() {
        val a = AppLockPreferences.pbkdf2Hash("1234", "saltA".toByteArray(), 1000, "PBKDF2WithHmacSHA256")
        val b = AppLockPreferences.pbkdf2Hash("1234", "saltB".toByteArray(), 1000, "PBKDF2WithHmacSHA256")
        assertNotEquals(a, b)
    }

    @Test
    fun pbkdf2Hash_changesWithSecret() {
        val a = AppLockPreferences.pbkdf2Hash("1111", "salt".toByteArray(), 1000, "PBKDF2WithHmacSHA256")
        val b = AppLockPreferences.pbkdf2Hash("2222", "salt".toByteArray(), 1000, "PBKDF2WithHmacSHA256")
        assertNotEquals(a, b)
    }

    @Test
    fun pbkdf2Hash_produces64HexCharsFor256BitKey() {
        val hash = AppLockPreferences.pbkdf2Hash("1234", "salt".toByteArray(), 1000, "PBKDF2WithHmacSHA256")
        assertEquals(64, hash.length)
        assertTrue(hash.all { it.isDigit() || it in 'a'..'f' })
    }

    @Test
    fun pbkdf2Hash_sha1VariantAlsoWorks() {
        // SHA-1 fallback (API 24-25 devices) must produce a valid 64-char hex too.
        val hash = AppLockPreferences.pbkdf2Hash("1234", "salt".toByteArray(), 1000, "PBKDF2WithHmacSHA1")
        assertEquals(64, hash.length)
    }

    // --- Lockout duration escalation --------------------------------------

    @Test
    fun lockoutDuration_block0_is30Seconds() {
        assertEquals(30_000L, AppLockPreferences.computeLockoutDurationMillis(0))
    }

    @Test
    fun lockoutDuration_doublesPerBlock() {
        assertEquals(30_000L, AppLockPreferences.computeLockoutDurationMillis(0))
        assertEquals(60_000L, AppLockPreferences.computeLockoutDurationMillis(1))
        assertEquals(120_000L, AppLockPreferences.computeLockoutDurationMillis(2))
        assertEquals(240_000L, AppLockPreferences.computeLockoutDurationMillis(3))
    }

    @Test
    fun lockoutDuration_capsAt15Minutes() {
        // 30s << 10 = 30,720s — must be capped at 15 min.
        assertEquals(15 * 60_000L, AppLockPreferences.computeLockoutDurationMillis(10))
        assertEquals(15 * 60_000L, AppLockPreferences.computeLockoutDurationMillis(50))
    }

    @Test
    fun lockoutDuration_negativeBlockIsZero() {
        assertEquals(0L, AppLockPreferences.computeLockoutDurationMillis(-1))
    }
}
