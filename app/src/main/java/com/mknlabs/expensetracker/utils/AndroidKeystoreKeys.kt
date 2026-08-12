package com.mknlabs.expensetracker.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Shared bootstrap for AES-256-GCM keys in the Android Keystore, used by both
 * [BackupEncryption] (backup files) and [SecureValueCipher] (DataStore field
 * values). Each caller passes its **own alias** so key material is never shared
 * between use-cases.
 *
 * Note: the first call for a fresh alias performs Keystore I/O (load + getKey,
 * or key generation) synchronously; the key is cached afterwards. Callers with
 * a hot startup path may warm the key up on a background dispatcher.
 */
object AndroidKeystoreKeys {

    private val cache = ConcurrentHashMap<String, SecretKey>()

    private val lock = Any()

    fun getOrCreateAesGcmKey(alias: String): SecretKey {
        cache[alias]?.let { return it }
        return synchronized(lock) {
            cache[alias] ?: loadOrCreate(alias).also { cache[alias] = it }
        }
    }

    private fun loadOrCreate(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }
}
