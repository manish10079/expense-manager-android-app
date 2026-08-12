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
 * JVM tests for the pure [BackupCipher] envelope used by encrypted auto-backups.
 * The Android Keystore wrapper ([BackupEncryption]) can't run on the JVM, but
 * every format/round-trip/failure rule lives in the pure core and is covered here.
 */
class BackupEncryptionTest {

    private val key: SecretKey = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")

    // --- format detection --------------------------------------------------

    @Test
    fun empty_payload_is_not_encrypted() {
        assertFalse(BackupCipher.isEncrypted(ByteArray(0)))
    }

    @Test
    fun short_payload_is_not_encrypted() {
        assertFalse(BackupCipher.isEncrypted(ByteArray(BackupCipher.HEADER_SIZE - 1)))
    }

    @Test
    fun sqlite_header_is_not_encrypted() {
        // Real SQLite databases start with "SQLite format 3\0".
        assertFalse(BackupCipher.isEncrypted("SQLite format 3\u0000".toByteArray()))
    }

    @Test
    fun encrypted_payload_is_detected() {
        assertTrue(BackupCipher.isEncrypted(BackupCipher.encrypt(key, "hello".toByteArray())))
    }

    @Test
    fun header_starts_with_magic_and_version() {
        val encrypted = BackupCipher.encrypt(key, byteArrayOf(1, 2, 3))
        assertTrue(BackupCipher.hasMagic(encrypted))
        assertTrue(encrypted[BackupCipher.MAGIC_SIZE] == BackupCipher.FORMAT_VERSION)
        // Header + plaintext + 16-byte GCM auth tag.
        assertTrue(encrypted.size == BackupCipher.HEADER_SIZE + 3 + 16)
    }

    // --- round trips -------------------------------------------------------

    @Test
    fun encrypt_decrypt_round_trip() {
        val plaintext = "SELECT * FROM transactions;".repeat(100).toByteArray()
        val encrypted = BackupCipher.encrypt(key, plaintext)
        assertArrayEquals(plaintext, BackupCipher.decrypt(key, encrypted))
    }

    @Test
    fun empty_database_bytes_round_trip() {
        val encrypted = BackupCipher.encrypt(key, ByteArray(0))
        assertArrayEquals(ByteArray(0), BackupCipher.decrypt(key, encrypted))
    }

    @Test
    fun each_encryption_uses_fresh_iv() {
        val plaintext = "same input".toByteArray()
        val first = BackupCipher.encrypt(key, plaintext)
        val second = BackupCipher.encrypt(key, plaintext)

        // Same plaintext must never produce identical ciphertext (GCM IV reuse
        // would be catastrophic, so fresh IVs are a hard requirement).
        assertFalse(first.contentEquals(second))

        val iv1 = first.copyOfRange(BackupCipher.MAGIC_SIZE + 1, BackupCipher.HEADER_SIZE)
        val iv2 = second.copyOfRange(BackupCipher.MAGIC_SIZE + 1, BackupCipher.HEADER_SIZE)
        assertFalse(iv1.contentEquals(iv2))
    }

    // --- failure modes -----------------------------------------------------

    @Test
    fun decrypt_with_wrong_key_throws() {
        val encrypted = BackupCipher.encrypt(key, "secret".toByteArray())
        val wrongKey = SecretKeySpec(ByteArray(32) { (it + 1).toByte() }, "AES")
        // GCM authentication guarantees a wrong key fails the tag check.
        assertThrows(AEADBadTagException::class.java) { BackupCipher.decrypt(wrongKey, encrypted) }
    }

    @Test
    fun decrypt_corrupted_ciphertext_throws() {
        val encrypted = BackupCipher.encrypt(key, "secret".toByteArray())
        encrypted[encrypted.size - 1] = (encrypted.last().toInt() xor 0xFF).toByte()
        assertThrows(AEADBadTagException::class.java) { BackupCipher.decrypt(key, encrypted) }
    }

    @Test
    fun decrypt_plaintext_payload_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupCipher.decrypt(key, "SQLite format 3\u0000".toByteArray())
        }
    }

    @Test
    fun decrypt_unsupported_version_throws() {
        val encrypted = BackupCipher.encrypt(key, byteArrayOf(1))
        encrypted[BackupCipher.MAGIC_SIZE] = 99
        assertThrows(IllegalArgumentException::class.java) { BackupCipher.decrypt(key, encrypted) }
    }
}
