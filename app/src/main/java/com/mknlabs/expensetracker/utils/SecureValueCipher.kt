package com.mknlabs.expensetracker.utils

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Pure, JVM-testable AES-256-GCM envelope for individual DataStore field values
 * (security plan **Item 5**). Layout (all lengths in bytes):
 * ```
 * | 0..3    magic  "MKV"            |
 * | 3       format version (1)      |
 * | 4..16   GCM IV / nonce (12)     |
 * | 16..    ciphertext + auth tag   |
 * ```
 * The magic header doubles as the detector: [isEncrypted] lets the DataStore
 * read path tell encrypted values apart from legacy plaintext ones, so existing
 * installs self-migrate — plaintext values keep reading fine and get encrypted
 * on the next write.
 */
object ValueCipher {

    private val MAGIC: ByteArray = byteArrayOf(0x4D, 0x4B, 0x56) // "MKV"
    const val FORMAT_VERSION: Byte = 1
    const val IV_LENGTH = 12
    const val TAG_LENGTH_BITS = 128
    const val MAGIC_SIZE = 3
    const val HEADER_SIZE = MAGIC_SIZE + 1 + IV_LENGTH // 16

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
        require(isEncrypted(payload)) { "Payload is not in encrypted value format." }
        val version = payload[MAGIC_SIZE]
        require(version == FORMAT_VERSION) { "Unsupported value format version: $version" }
        val iv = payload.copyOfRange(MAGIC_SIZE + 1, HEADER_SIZE)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(payload, HEADER_SIZE, payload.size - HEADER_SIZE)
    }
}

/**
 * Android wrapper around [ValueCipher] that owns the AES-256-GCM key in the
 * Android Keystore for DataStore field values (security plan Item 5). Uses its
 * own key alias, separate from the backup key in [BackupEncryption], so the two
 * use-cases never share key material.
 */
object SecureValueCipher {

    private const val KEY_ALIAS = "expense_tracker_datastore_key"

    /** Encrypts [plaintext] and returns a base64 string storable in a DataStore. */
    fun encrypt(plaintext: String): String {
        val payload =
            ValueCipher.encrypt(AndroidKeystoreKeys.getOrCreateAesGcmKey(KEY_ALIAS), plaintext.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(payload, android.util.Base64.NO_WRAP)
    }

    /**
     * Decrypts [encoded] if it is in our encrypted format; returns `null` for
     * legacy plaintext values (or anything that fails to decode/decrypt) so the
     * caller can fall back to treating the raw value as plaintext — never throws.
     */
    fun decryptOrNull(encoded: String): String? {
        val payload = try {
            android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            return null
        }
        if (!ValueCipher.isEncrypted(payload)) return null
        return try {
            String(ValueCipher.decrypt(AndroidKeystoreKeys.getOrCreateAesGcmKey(KEY_ALIAS), payload), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
