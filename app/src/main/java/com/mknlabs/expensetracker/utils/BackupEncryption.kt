package com.mknlabs.expensetracker.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Pure, JVM-testable AES-256-GCM envelope format used by encrypted database
 * backups written by [AutoBackupWorker][com.mknlabs.expensetracker.workers.AutoBackupWorker].
 *
 * On-disk layout (all lengths in bytes):
 * ```
 * | 0..4    magic   "ETBK"                    |
 * | 4       format version (1)                |
 * | 5..17   GCM IV / nonce (12)               |
 * | 17..    ciphertext + 16-byte GCM auth tag |
 * ```
 *
 * The magic header doubles as the format detector: [isEncrypted] lets the
 * restore path tell encrypted auto-backups apart from legacy plaintext .db
 * exports (SQLite files start with `"SQLite format 3\0"`, never `"ETBK"`).
 */
object BackupCipher {

    private val MAGIC: ByteArray = byteArrayOf(0x45, 0x54, 0x42, 0x4B) // "ETBK"
    const val FORMAT_VERSION: Byte = 1
    const val IV_LENGTH = 12
    const val TAG_LENGTH_BITS = 128
    const val MAGIC_SIZE = 4
    const val HEADER_SIZE = MAGIC_SIZE + 1 + IV_LENGTH // 17

    fun hasMagic(payload: ByteArray): Boolean {
        if (payload.size < MAGIC_SIZE) return false
        for (i in 0 until MAGIC_SIZE) {
            if (payload[i] != MAGIC[i]) return false
        }
        return true
    }

    fun isEncrypted(payload: ByteArray): Boolean =
        payload.size >= HEADER_SIZE && hasMagic(payload)

    fun encrypt(key: SecretKey, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return ByteArray(HEADER_SIZE + ciphertext.size).also { out ->
            MAGIC.copyInto(out, 0)
            out[MAGIC_SIZE] = FORMAT_VERSION
            iv.copyInto(out, MAGIC_SIZE + 1)
            ciphertext.copyInto(out, HEADER_SIZE)
        }
    }

    fun decrypt(key: SecretKey, payload: ByteArray): ByteArray {
        require(isEncrypted(payload)) { "Payload is not in encrypted backup format." }
        val version = payload[MAGIC_SIZE]
        require(version == FORMAT_VERSION) { "Unsupported backup format version: $version" }
        val iv = payload.copyOfRange(MAGIC_SIZE + 1, HEADER_SIZE)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(payload, HEADER_SIZE, payload.size - HEADER_SIZE)
    }
}

/**
 * Thrown when a backup is recognized as encrypted but cannot be decrypted on
 * this device — e.g. the Keystore key belongs to a different install, or the
 * file is corrupted. Lets the UI show a specific message instead of the
 * generic restore-failure toast.
 */
class BackupDecryptionException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

/**
 * Android wrapper around [BackupCipher] that owns the AES-256-GCM key inside
 * the Android Keystore. The key never leaves the device, so encrypted backups
 * are only restorable on the same install that created them — a deliberate
 * trade-off: the auto-backups are an on-device safety net (DB corruption,
 * accidental deletes), not a cross-device migration path. Cross-device restore
 * is covered by the in-app manual .db export and Firestore sync.
 */
object BackupEncryption {

    private const val KEY_ALIAS = "expense_tracker_backup_key"

    @Volatile
    private var cachedKey: SecretKey? = null

    private val keyLock = Any()

    fun ensureKey(context: Context): SecretKey {
        cachedKey?.let { return it }
        return synchronized(keyLock) {
            cachedKey ?: loadOrCreateKey(context).also { cachedKey = it }
        }
    }

    private fun loadOrCreateKey(context: Context): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    /**
     * Encrypts [plaintext] for an auto-backup file. Failures propagate — the
     * auto-backup worker catches broadly and retries.
     */
    fun encrypt(context: Context, plaintext: ByteArray): ByteArray =
        BackupCipher.encrypt(ensureKey(context), plaintext)

    fun decrypt(context: Context, payload: ByteArray): ByteArray {
        return try {
            BackupCipher.decrypt(ensureKey(context), payload)
        } catch (e: Exception) {
            throw BackupDecryptionException("Failed to decrypt database backup on this device.", e)
        }
    }

    fun isEncrypted(payload: ByteArray): Boolean = BackupCipher.isEncrypted(payload)

    /** Cheap magic-header sniff (reads 4 bytes) used by the backup picker UI. */
    fun isEncryptedFile(file: File): Boolean {
        if (!file.exists() || file.length() < BackupCipher.HEADER_SIZE) return false
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(BackupCipher.MAGIC_SIZE)
                input.read(header) == header.size && BackupCipher.hasMagic(header)
            }
        } catch (e: Exception) {
            false
        }
    }
}
