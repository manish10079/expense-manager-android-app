package com.mkn0079.expensetracker.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.mkn0079.expensetracker.data.constants.DEFAULT_APP_LOCK_TIMEOUT_MINUTES
import com.mkn0079.expensetracker.data.constants.DEFAULT_BIOMETRIC_LOCK_ENABLED
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val LEGACY_APP_LOCK_PREFS_NAME = "app_lock_prefs"
private const val ENCRYPTED_APP_LOCK_PREFS_NAME = "app_lock_secure_prefs"
private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
private const val KEY_APP_LOCK_PIN_HASH = "app_lock_pin_hash"
private const val KEY_APP_LOCK_PIN_SALT = "app_lock_pin_salt"
private const val KEY_APP_LOCK_PIN_HASH_VERSION = "app_lock_pin_hash_version"
private const val KEY_APP_LOCK_FAST_PIN_HASH = "app_lock_fast_pin_hash"
private const val KEY_APP_LOCK_FAST_PIN_SALT = "app_lock_fast_pin_salt"
private const val KEY_APP_LOCK_FAST_PIN_HASH_VERSION = "app_lock_fast_pin_hash_version"
private const val KEY_BIOMETRIC_LOCK_ENABLED = "biometric_lock_enabled"
private const val KEY_AUTO_LOCK_DURATION_MINUTES = "auto_lock_duration_minutes"
private const val KEY_SECURITY_QUESTION_ID = "security_question_id"
private const val KEY_SECURITY_ANSWER_HASH = "security_answer_hash"
private const val KEY_SECURITY_ANSWER_SALT = "security_answer_salt"
private const val KEY_SECURITY_ANSWER_HASH_VERSION = "security_answer_hash_version"
private const val KEY_LAST_BACKGROUND_AT_MILLIS = "last_background_at_millis"
private const val KEY_LAST_UNLOCKED_AT_MILLIS = "last_unlocked_at_millis"
private const val KEY_STORAGE_MIGRATION_COMPLETE = "storage_migration_complete"

private const val HASH_VERSION_LEGACY_SHA256 = "legacy_sha256"
private const val HASH_VERSION_PBKDF2_SHA256_V1 = "pbkdf2_sha256_v1"
private const val HASH_VERSION_PBKDF2_SHA256_V2 = "pbkdf2_sha256_v2"
private const val HASH_VERSION_FAST_SHA256_V1 = "fast_sha256_v1"
private const val SALT_LENGTH_BYTES = 16
private const val PBKDF2_ITERATION_COUNT_V1 = 120_000
private const val PBKDF2_ITERATION_COUNT_V2 = 45_000
private const val PBKDF2_KEY_LENGTH_BITS = 256

data class AppLockCachedState(
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = DEFAULT_BIOMETRIC_LOCK_ENABLED,
    val autoLockDurationMinutes: Int = DEFAULT_APP_LOCK_TIMEOUT_MINUTES,
    val hasPin: Boolean = false,
    val hasFastPinVerifier: Boolean = false,
    val securityQuestionId: String? = null,
    val lastBackgroundedAtMillis: Long = -1L,
    val lastUnlockedAtMillis: Long = -1L
)

object AppLockPreferences {
    @Volatile
    private var encryptedPreferencesCache: SharedPreferences? = null
    @Volatile
    private var cachedState = AppLockCachedState()
    @Volatile
    private var cachedFastPinVerifier: FastPinVerifier? = null
    private val encryptedPreferencesLock = Any()
    private val secureRandom = SecureRandom()

    private data class FastPinVerifier(
        val hash: String,
        val salt: String,
        val version: String
    )

    fun initialize(context: Context): AppLockCachedState {
        return refreshCache(context)
    }

    fun getCachedState(): AppLockCachedState {
        return cachedState
    }

    fun isEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_APP_LOCK_ENABLED, false)
    }

    fun hasPin(context: Context): Boolean {
        return !prefs(context).getString(KEY_APP_LOCK_PIN_HASH, null).isNullOrBlank()
    }

    fun savePin(context: Context, pin: String) {
        val normalizedPin = normalizePin(pin)
        val fastPinVerifier = createFastPinVerifier(normalizedPin)
        prefs(context)
            .edit()
            .putSaltedSecret(
                hashKey = KEY_APP_LOCK_PIN_HASH,
                saltKey = KEY_APP_LOCK_PIN_SALT,
                versionKey = KEY_APP_LOCK_PIN_HASH_VERSION,
                normalizedSecret = normalizedPin
            )
            .putFastPinVerifier(fastPinVerifier)
            .putBoolean(KEY_APP_LOCK_ENABLED, true)
            .apply()
        cacheFastPinVerifier(fastPinVerifier)
        updateCachedState {
            it.copy(
                isAppLockEnabled = true,
                hasPin = true,
                hasFastPinVerifier = true
            )
        }
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_BIOMETRIC_LOCK_ENABLED, DEFAULT_BIOMETRIC_LOCK_ENABLED)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        prefs(context)
            .edit()
            .putBoolean(KEY_BIOMETRIC_LOCK_ENABLED, enabled)
            .apply()
        updateCachedState { it.copy(isBiometricEnabled = enabled) }
    }

    fun getAutoLockDurationMinutes(context: Context): Int {
        return prefs(context).getInt(KEY_AUTO_LOCK_DURATION_MINUTES, DEFAULT_APP_LOCK_TIMEOUT_MINUTES)
    }

    fun setAutoLockDurationMinutes(context: Context, minutes: Int) {
        val sanitizedMinutes = minutes.coerceAtLeast(0)
        prefs(context)
            .edit()
            .putInt(KEY_AUTO_LOCK_DURATION_MINUTES, sanitizedMinutes)
            .apply()
        updateCachedState { it.copy(autoLockDurationMinutes = sanitizedMinutes) }
    }

    fun validatePinForUnlock(context: Context, pin: String): Boolean {
        val hasFastVerifier = cachedState.hasFastPinVerifier || cachedFastPinVerifier != null
        if (hasFastVerifier) {
            return validatePinFromMemory(pin)
        }

        return validatePin(context, pin)
    }

    fun validatePinFromMemory(pin: String): Boolean {
        val verifier = cachedFastPinVerifier ?: return false
        if (verifier.version != HASH_VERSION_FAST_SHA256_V1) {
            return false
        }

        val candidateHash = fastPinHash(
            normalizedPin = normalizePin(pin),
            salt = verifier.salt
        )
        return MessageDigest.isEqual(
            verifier.hash.toByteArray(),
            candidateHash.toByteArray()
        )
    }

    fun validatePin(context: Context, pin: String): Boolean {
        val preferences = prefs(context)
        val normalizedPin = normalizePin(pin)
        val savedHash = preferences.getString(KEY_APP_LOCK_PIN_HASH, null) ?: return false
        val hashVersion = preferences.getString(KEY_APP_LOCK_PIN_HASH_VERSION, null)
        val salt = preferences.getString(KEY_APP_LOCK_PIN_SALT, null)

        val isValid = when {
            hashVersion == HASH_VERSION_PBKDF2_SHA256_V2 && !salt.isNullOrBlank() -> {
                savedHash == hashSecret(
                    normalizedSecret = normalizedPin,
                    salt = salt,
                    iterations = PBKDF2_ITERATION_COUNT_V2
                )
            }

            hashVersion == HASH_VERSION_PBKDF2_SHA256_V1 && !salt.isNullOrBlank() -> {
                savedHash == hashSecret(
                    normalizedSecret = normalizedPin,
                    salt = salt,
                    iterations = PBKDF2_ITERATION_COUNT_V1
                )
            }

            else -> {
                savedHash == legacySha256(normalizedPin)
            }
        }

        if (isValid) {
            val shouldUpgradeSecureHash = hashVersion != HASH_VERSION_PBKDF2_SHA256_V2 || salt.isNullOrBlank()
            val existingFastPinVerifier = readFastPinVerifier(preferences)
            val fastPinVerifier = existingFastPinVerifier ?: createFastPinVerifier(normalizedPin)

            if (shouldUpgradeSecureHash || existingFastPinVerifier == null) {
                preferences
                    .edit()
                    .apply {
                        if (shouldUpgradeSecureHash) {
                            putSaltedSecret(
                                hashKey = KEY_APP_LOCK_PIN_HASH,
                                saltKey = KEY_APP_LOCK_PIN_SALT,
                                versionKey = KEY_APP_LOCK_PIN_HASH_VERSION,
                                normalizedSecret = normalizedPin
                            )
                        }
                    }
                    .putFastPinVerifier(fastPinVerifier)
                    .apply()
            }

            cacheFastPinVerifier(fastPinVerifier)
            updateCachedState {
                it.copy(
                    isAppLockEnabled = preferences.getBoolean(KEY_APP_LOCK_ENABLED, false),
                    hasPin = true,
                    hasFastPinVerifier = true
                )
            }
        }

        return isValid
    }

    fun refreshCache(context: Context): AppLockCachedState {
        val preferences = prefs(context)
        val fastPinVerifier = readFastPinVerifier(preferences)
        val nextState = AppLockCachedState(
            isAppLockEnabled = preferences.getBoolean(KEY_APP_LOCK_ENABLED, false),
            isBiometricEnabled = preferences.getBoolean(
                KEY_BIOMETRIC_LOCK_ENABLED,
                DEFAULT_BIOMETRIC_LOCK_ENABLED
            ),
            autoLockDurationMinutes = preferences.getInt(
                KEY_AUTO_LOCK_DURATION_MINUTES,
                DEFAULT_APP_LOCK_TIMEOUT_MINUTES
            ),
            hasPin = !preferences.getString(KEY_APP_LOCK_PIN_HASH, null).isNullOrBlank(),
            hasFastPinVerifier = fastPinVerifier != null,
            securityQuestionId = preferences.getString(KEY_SECURITY_QUESTION_ID, null),
            lastBackgroundedAtMillis = preferences.getLong(KEY_LAST_BACKGROUND_AT_MILLIS, -1L),
            lastUnlockedAtMillis = preferences.getLong(KEY_LAST_UNLOCKED_AT_MILLIS, -1L)
        )

        cachedFastPinVerifier = fastPinVerifier
        cachedState = nextState
        return nextState
    }

    fun shouldRequireUnlockFromMemory(
        autoLockDurationMinutes: Int,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val state = cachedState
        if (!state.hasPin) {
            return false
        }

        if (state.lastBackgroundedAtMillis <= 0L ||
            state.lastBackgroundedAtMillis <= state.lastUnlockedAtMillis
        ) {
            return false
        }

        if (autoLockDurationMinutes <= 0) {
            return true
        }

        val elapsedMillis = currentTimeMillis - state.lastBackgroundedAtMillis
        return elapsedMillis >= autoLockDurationMinutes * 60_000L
    }

    fun markBackgroundedInMemory(
        backgroundedAtMillis: Long = System.currentTimeMillis()
    ): Long {
        updateCachedState {
            it.copy(lastBackgroundedAtMillis = backgroundedAtMillis)
        }
        return backgroundedAtMillis
    }

    fun persistBackgrounded(
        context: Context,
        backgroundedAtMillis: Long
    ) {
        prefs(context)
            .edit()
            .putLong(KEY_LAST_BACKGROUND_AT_MILLIS, backgroundedAtMillis)
            .apply()
    }

    fun markUnlockedInMemory(
        unlockedAtMillis: Long = System.currentTimeMillis()
    ): Long {
        updateCachedState {
            it.copy(lastUnlockedAtMillis = unlockedAtMillis)
        }
        return unlockedAtMillis
    }

    fun persistUnlocked(
        context: Context,
        unlockedAtMillis: Long
    ) {
        prefs(context)
            .edit()
            .putLong(KEY_LAST_UNLOCKED_AT_MILLIS, unlockedAtMillis)
            .apply()
    }

    private fun updateCachedState(update: (AppLockCachedState) -> AppLockCachedState) {
        cachedState = update(cachedState)
    }

    private fun cacheFastPinVerifier(verifier: FastPinVerifier) {
        cachedFastPinVerifier = verifier
    }

    private fun readFastPinVerifier(preferences: SharedPreferences): FastPinVerifier? {
        val hash = preferences.getString(KEY_APP_LOCK_FAST_PIN_HASH, null)
        val salt = preferences.getString(KEY_APP_LOCK_FAST_PIN_SALT, null)
        val version = preferences.getString(KEY_APP_LOCK_FAST_PIN_HASH_VERSION, null)
        if (hash.isNullOrBlank() || salt.isNullOrBlank() || version.isNullOrBlank()) {
            return null
        }

        return FastPinVerifier(
            hash = hash,
            salt = salt,
            version = version
        )
    }

    private fun createFastPinVerifier(normalizedPin: String): FastPinVerifier {
        val salt = generateSalt()
        return FastPinVerifier(
            hash = fastPinHash(
                normalizedPin = normalizedPin,
                salt = salt
            ),
            salt = salt,
            version = HASH_VERSION_FAST_SHA256_V1
        )
    }

    private fun SharedPreferences.Editor.putFastPinVerifier(
        verifier: FastPinVerifier
    ): SharedPreferences.Editor {
        return putString(KEY_APP_LOCK_FAST_PIN_HASH, verifier.hash)
            .putString(KEY_APP_LOCK_FAST_PIN_SALT, verifier.salt)
            .putString(KEY_APP_LOCK_FAST_PIN_HASH_VERSION, verifier.version)
    }

    private fun fastPinHash(
        normalizedPin: String,
        salt: String
    ): String {
        return legacySha256("$salt:$normalizedPin")
    }

    fun saveSecurityQuestion(
        context: Context,
        questionId: String,
        answer: String
    ) {
        prefs(context)
            .edit()
            .putString(KEY_SECURITY_QUESTION_ID, questionId)
            .putFastSaltedSecret(
                hashKey = KEY_SECURITY_ANSWER_HASH,
                saltKey = KEY_SECURITY_ANSWER_SALT,
                versionKey = KEY_SECURITY_ANSWER_HASH_VERSION,
                normalizedSecret = normalizeAnswer(answer)
            )
            .apply()
        updateCachedState { it.copy(securityQuestionId = questionId) }
    }

    fun hasSecurityQuestion(context: Context): Boolean {
        return !prefs(context).getString(KEY_SECURITY_QUESTION_ID, null).isNullOrBlank() &&
            !prefs(context).getString(KEY_SECURITY_ANSWER_HASH, null).isNullOrBlank()
    }

    fun getSecurityQuestionId(context: Context): String? {
        return prefs(context).getString(KEY_SECURITY_QUESTION_ID, null)
    }

    fun validateSecurityAnswer(context: Context, answer: String): Boolean {
        val preferences = prefs(context)
        val normalizedAnswer = normalizeAnswer(answer)
        val savedHash = preferences.getString(KEY_SECURITY_ANSWER_HASH, null) ?: return false
        val hashVersion = preferences.getString(KEY_SECURITY_ANSWER_HASH_VERSION, null)
        val salt = preferences.getString(KEY_SECURITY_ANSWER_SALT, null)

        val isValid = when {
            hashVersion == HASH_VERSION_FAST_SHA256_V1 && !salt.isNullOrBlank() -> {
                savedHash == fastPinHash(normalizedAnswer, salt)
            }

            hashVersion == HASH_VERSION_PBKDF2_SHA256_V2 && !salt.isNullOrBlank() -> {
                savedHash == hashSecret(
                    normalizedSecret = normalizedAnswer,
                    salt = salt,
                    iterations = PBKDF2_ITERATION_COUNT_V2
                )
            }

            hashVersion == HASH_VERSION_PBKDF2_SHA256_V1 && !salt.isNullOrBlank() -> {
                savedHash == hashSecret(
                    normalizedSecret = normalizedAnswer,
                    salt = salt,
                    iterations = PBKDF2_ITERATION_COUNT_V1
                )
            }

            else -> {
                savedHash == legacySha256(normalizedAnswer)
            }
        }

        if (isValid && (hashVersion != HASH_VERSION_FAST_SHA256_V1 || salt.isNullOrBlank())) {
            preferences
                .edit()
                .putFastSaltedSecret(
                    hashKey = KEY_SECURITY_ANSWER_HASH,
                    saltKey = KEY_SECURITY_ANSWER_SALT,
                    versionKey = KEY_SECURITY_ANSWER_HASH_VERSION,
                    normalizedSecret = normalizedAnswer
                )
                .apply()
        }

        return isValid
    }

    fun markBackgrounded(
        context: Context,
        backgroundedAtMillis: Long = System.currentTimeMillis()
    ) {
        markBackgroundedInMemory(backgroundedAtMillis)
        persistBackgrounded(context, backgroundedAtMillis)
    }

    fun markUnlocked(
        context: Context,
        unlockedAtMillis: Long = System.currentTimeMillis()
    ) {
        markUnlockedInMemory(unlockedAtMillis)
        persistUnlocked(context, unlockedAtMillis)
    }

    fun shouldRequireUnlock(
        context: Context,
        autoLockDurationMinutes: Int,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (!hasPin(context)) {
            return false
        }

        val preferences = prefs(context)
        val lastBackgroundedAtMillis = preferences.getLong(KEY_LAST_BACKGROUND_AT_MILLIS, -1L)
        val lastUnlockedAtMillis = preferences.getLong(KEY_LAST_UNLOCKED_AT_MILLIS, -1L)

        if (lastBackgroundedAtMillis <= 0L || lastBackgroundedAtMillis <= lastUnlockedAtMillis) {
            return false
        }

        if (autoLockDurationMinutes <= 0) {
            return true
        }

        val elapsedMillis = currentTimeMillis - lastBackgroundedAtMillis
        return elapsedMillis >= autoLockDurationMinutes * 60_000L
    }

    fun clear(context: Context) {
        prefs(context)
            .edit()
            .remove(KEY_APP_LOCK_PIN_HASH)
            .remove(KEY_APP_LOCK_PIN_SALT)
            .remove(KEY_APP_LOCK_PIN_HASH_VERSION)
            .remove(KEY_APP_LOCK_FAST_PIN_HASH)
            .remove(KEY_APP_LOCK_FAST_PIN_SALT)
            .remove(KEY_APP_LOCK_FAST_PIN_HASH_VERSION)
            .remove(KEY_SECURITY_QUESTION_ID)
            .remove(KEY_SECURITY_ANSWER_HASH)
            .remove(KEY_SECURITY_ANSWER_SALT)
            .remove(KEY_SECURITY_ANSWER_HASH_VERSION)
            .remove(KEY_LAST_BACKGROUND_AT_MILLIS)
            .remove(KEY_LAST_UNLOCKED_AT_MILLIS)
            .putBoolean(KEY_APP_LOCK_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_LOCK_ENABLED, false)
            .apply()

        legacyPrefs(context)
            .edit()
            .clear()
            .apply()

        cachedFastPinVerifier = null
        cachedState = AppLockCachedState(
            isAppLockEnabled = false,
            isBiometricEnabled = false,
            autoLockDurationMinutes = cachedState.autoLockDurationMinutes
        )
    }

    private fun prefs(context: Context): SharedPreferences {
        val encryptedPreferences = encryptedPrefs(context.applicationContext)
        migrateLegacyPrefsIfNeeded(context, encryptedPreferences)
        return encryptedPreferences
    }

    private fun encryptedPrefs(context: Context): SharedPreferences {
        encryptedPreferencesCache?.let { return it }

        return synchronized(encryptedPreferencesLock) {
            encryptedPreferencesCache ?: EncryptedSharedPreferences.create(
                ENCRYPTED_APP_LOCK_PREFS_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).also { createdPreferences ->
                encryptedPreferencesCache = createdPreferences
            }
        }
    }

    private fun legacyPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(LEGACY_APP_LOCK_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun migrateLegacyPrefsIfNeeded(
        context: Context,
        encryptedPreferences: SharedPreferences
    ) {
        if (encryptedPreferences.getBoolean(KEY_STORAGE_MIGRATION_COMPLETE, false)) {
            return
        }

        val legacyPreferences = legacyPrefs(context)
        val hasLegacyData = legacyPreferences.all.isNotEmpty()
        val hasEncryptedData = encryptedPreferences.all.isNotEmpty()

        if (!hasLegacyData) {
            encryptedPreferences
                .edit()
                .putBoolean(KEY_STORAGE_MIGRATION_COMPLETE, true)
                .apply()
            return
        }

        if (hasEncryptedData) {
            encryptedPreferences
                .edit()
                .putBoolean(KEY_STORAGE_MIGRATION_COMPLETE, true)
                .apply()
            legacyPreferences.edit().clear().apply()
            return
        }

        val legacyPinHash = legacyPreferences.getString(KEY_APP_LOCK_PIN_HASH, null)
        val legacySecurityAnswerHash = legacyPreferences.getString(KEY_SECURITY_ANSWER_HASH, null)

        val migrationSucceeded = encryptedPreferences
            .edit()
            .putBoolean(
                KEY_APP_LOCK_ENABLED,
                legacyPreferences.getBoolean(KEY_APP_LOCK_ENABLED, false)
            )
            .putBoolean(
                KEY_BIOMETRIC_LOCK_ENABLED,
                legacyPreferences.getBoolean(
                    KEY_BIOMETRIC_LOCK_ENABLED,
                    DEFAULT_BIOMETRIC_LOCK_ENABLED
                )
            )
            .putInt(
                KEY_AUTO_LOCK_DURATION_MINUTES,
                legacyPreferences.getInt(
                    KEY_AUTO_LOCK_DURATION_MINUTES,
                    DEFAULT_APP_LOCK_TIMEOUT_MINUTES
                )
            )
            .putString(
                KEY_SECURITY_QUESTION_ID,
                legacyPreferences.getString(KEY_SECURITY_QUESTION_ID, null)
            )
            .putLong(
                KEY_LAST_BACKGROUND_AT_MILLIS,
                legacyPreferences.getLong(KEY_LAST_BACKGROUND_AT_MILLIS, -1L)
            )
            .putLong(
                KEY_LAST_UNLOCKED_AT_MILLIS,
                legacyPreferences.getLong(KEY_LAST_UNLOCKED_AT_MILLIS, -1L)
            )
            .apply {
                if (!legacyPinHash.isNullOrBlank()) {
                    putString(KEY_APP_LOCK_PIN_HASH, legacyPinHash)
                    putString(KEY_APP_LOCK_PIN_HASH_VERSION, HASH_VERSION_LEGACY_SHA256)
                }

                if (!legacySecurityAnswerHash.isNullOrBlank()) {
                    putString(KEY_SECURITY_ANSWER_HASH, legacySecurityAnswerHash)
                    putString(KEY_SECURITY_ANSWER_HASH_VERSION, HASH_VERSION_LEGACY_SHA256)
                }
            }
            .putBoolean(KEY_STORAGE_MIGRATION_COMPLETE, true)
            .commit()

        if (migrationSucceeded) {
            legacyPreferences.edit().clear().apply()
        }
    }

    private fun SharedPreferences.Editor.putSaltedSecret(
        hashKey: String,
        saltKey: String,
        versionKey: String,
        normalizedSecret: String
    ): SharedPreferences.Editor {
        val salt = generateSalt()
        return putString(
            hashKey,
            hashSecret(
                normalizedSecret = normalizedSecret,
                salt = salt,
                iterations = PBKDF2_ITERATION_COUNT_V2
            )
        )
            .putString(saltKey, salt)
            .putString(versionKey, HASH_VERSION_PBKDF2_SHA256_V2)
    }

    private fun SharedPreferences.Editor.putFastSaltedSecret(
        hashKey: String,
        saltKey: String,
        versionKey: String,
        normalizedSecret: String
    ): SharedPreferences.Editor {
        val salt = generateSalt()
        return putString(hashKey, fastPinHash(normalizedSecret, salt))
            .putString(saltKey, salt)
            .putString(versionKey, HASH_VERSION_FAST_SHA256_V1)
    }

    private fun generateSalt(): String {
        val saltBytes = ByteArray(SALT_LENGTH_BYTES)
        secureRandom.nextBytes(saltBytes)
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }

    private fun hashSecret(
        normalizedSecret: String,
        salt: String,
        iterations: Int
    ): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val keySpec = PBEKeySpec(
            normalizedSecret.toCharArray(),
            saltBytes,
            iterations,
            PBKDF2_KEY_LENGTH_BITS
        )
        return try {
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hashBytes = secretKeyFactory.generateSecret(keySpec).encoded
            hashBytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
        } finally {
            keySpec.clearPassword()
        }
    }

    private fun legacySha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(value.toByteArray())
        return hashBytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun normalizePin(pin: String): String {
        return pin.trim()
    }

    private fun normalizeAnswer(answer: String): String {
        return answer
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }
}
