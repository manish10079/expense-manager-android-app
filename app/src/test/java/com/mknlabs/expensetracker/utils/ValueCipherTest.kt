package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.AEADBadTagException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * JVM tests for the pure [ValueCipher] envelope used by Item 5 DataStore field
 * encryption. The Android Keystore wrapper ([SecureValueCipher]) can't run on
 * the JVM; every format/round-trip/failure rule lives in the pure core.
 */
class ValueCipherTest {

    private val key: SecretKey = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")

    // --- format detection --------------------------------------------------

    @Test
    fun empty_payload_is_not_encrypted() {
        assertFalse(ValueCipher.isEncrypted(ByteArray(0)))
    }

    @Test
    fun short_payload_is_not_encrypted() {
        assertFalse(ValueCipher.isEncrypted(ByteArray(ValueCipher.HEADER_SIZE - 1)))
    }

    @Test
    fun plaintext_like_bytes_are_not_encrypted() {
        // "user@example.com" style legacy values never carry our magic header.
        assertFalse(ValueCipher.isEncrypted("user@example.com".toByteArray()))
    }

    @Test
    fun encrypted_payload_is_detected() {
        assertTrue(ValueCipher.isEncrypted(ValueCipher.encrypt(key, "hello".toByteArray())))
    }

    @Test
    fun header_starts_with_magic_and_version() {
        val encrypted = ValueCipher.encrypt(key, byteArrayOf(1, 2, 3))
        assertTrue(ValueCipher.hasMagic(encrypted))
        assertTrue(encrypted[ValueCipher.MAGIC_SIZE] == ValueCipher.FORMAT_VERSION)
        // Header + plaintext + 16-byte GCM auth tag.
        assertTrue(encrypted.size == ValueCipher.HEADER_SIZE + 3 + 16)
    }

    // --- round trips -------------------------------------------------------

    @Test
    fun encrypt_decrypt_round_trip() {
        val plaintext = "email+premium fields with unicode \u20b91,234".toByteArray()
        val encrypted = ValueCipher.encrypt(key, plaintext)
        assertArrayEquals(plaintext, ValueCipher.decrypt(key, encrypted))
    }

    @Test
    fun empty_string_round_trip() {
        val encrypted = ValueCipher.encrypt(key, ByteArray(0))
        assertArrayEquals(ByteArray(0), ValueCipher.decrypt(key, encrypted))
    }

    @Test
    fun each_encryption_uses_fresh_iv() {
        val plaintext = "same input".toByteArray()
        val first = ValueCipher.encrypt(key, plaintext)
        val second = ValueCipher.encrypt(key, plaintext)

        assertFalse(first.contentEquals(second))

        val iv1 = first.copyOfRange(ValueCipher.MAGIC_SIZE + 1, ValueCipher.HEADER_SIZE)
        val iv2 = second.copyOfRange(ValueCipher.MAGIC_SIZE + 1, ValueCipher.HEADER_SIZE)
        assertFalse(iv1.contentEquals(iv2))
    }

    // --- failure modes -----------------------------------------------------

    @Test
    fun decrypt_with_wrong_key_throws() {
        val encrypted = ValueCipher.encrypt(key, "secret".toByteArray())
        val wrongKey = SecretKeySpec(ByteArray(32) { (it + 1).toByte() }, "AES")
        // GCM authentication guarantees a wrong key fails the tag check.
        assertThrows(AEADBadTagException::class.java) { ValueCipher.decrypt(wrongKey, encrypted) }
    }

    @Test
    fun decrypt_corrupted_ciphertext_throws() {
        val encrypted = ValueCipher.encrypt(key, "secret".toByteArray())
        encrypted[encrypted.size - 1] = (encrypted.last().toInt() xor 0xFF).toByte()
        assertThrows(AEADBadTagException::class.java) { ValueCipher.decrypt(key, encrypted) }
    }

    @Test
    fun decrypt_plaintext_payload_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            ValueCipher.decrypt(key, "user@example.com".toByteArray())
        }
    }

    @Test
    fun decrypt_unsupported_version_throws() {
        val encrypted = ValueCipher.encrypt(key, byteArrayOf(1))
        encrypted[ValueCipher.MAGIC_SIZE] = 99
        assertThrows(IllegalArgumentException::class.java) { ValueCipher.decrypt(key, encrypted) }
    }
}
