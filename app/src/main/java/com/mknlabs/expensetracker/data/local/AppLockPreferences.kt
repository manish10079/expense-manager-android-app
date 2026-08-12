package com.mknlabs.expensetracker.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.mknlabs.expensetracker.data.constants.DEFAULT_APP_LOCK_TIMEOUT_MINUTES
import com.mknlabs.expensetracker.data.constants.DEFAULT_BIOMETRIC_LOCK_ENABLED
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
private const val KEY_APP_LOCK_PIN_ITERATIONS = "app_lock_pin_iterations"
private const val KEY_SECURITY_ANSWER_ITERATIONS = "security_answer_iterations"
private const val KEY_FAILED_ATTEMPT_COUNT = "failed_attempt_count"
private const val KEY_LOCKOUT_BLOCK_INDEX = "lockout_block_index"
private const val KEY_LOCKOUT_UNTIL_MILLIS = "lockout_until_millis"
private const val KEY_STORAGE_MIGRATION_COMPLETE = "storage_migration_complete"

private const val HASH_VERSION_LEGACY_SHA256 = "legacy_sha256"
private const val HASH_VERSION_FAST_SHA256_V1 = "fast_sha256_v1"
private const val HASH_VERSION_PBKDF2_SHA256_V1 = "pbkdf2_sha256_v1"
private const val HASH_VERSION_PBKDF2_SHA1_V1 = "pbkdf2_sha1_v1"
private const val PBKDF2_ALGORITHM_SHA256 = "PBKDF2WithHmacSHA256"
private const val PBKDF2_ALGORITHM_SHA1 = "PBKDF2WithHmacSHA1"
private const val SALT_LENGTH_BYTES = 16
private const val PBKDF2_ITERATIONS = 120_000
private const val PBKDF2_KEY_LENGTH_BITS = 256

// Brute-force lockout: 5 failures -> 30s, doubling per subsequent 5-failure block, capped at 15 min.
private const val MAX_FAILED_ATTEMPTS_BEFORE_LOCKOUT = 5
private const val LOCKOUT_INITIAL_MILLIS = 30_000L
private const val LOCKOUT_MAX_MILLIS = 15 * 60_000L

// In-memory only: the window during which an external activity (photo/file picker,
// browser, system settings screen) is expected to background the app. A pending
// suppression is consumed on the next foreground return and is only honored if that
// return happens within this TTL — so a genuine long background (e.g. the user
// detours to the launcher instead of returning to the app) still triggers the lock.
private const val LOCK_SUPPRESSION_TTL_MS = 5 * 60_000L

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
    @Volatile
    private var lockSuppressionSetAtMillis = 0L
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
                iterationsKey = KEY_APP_LOCK_PIN_ITERATIONS,
                normalizedSecret = normalizedPin
            )
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
        if (isLockedOut(context)) {
            return false
        }

        val isValid = validatePinInternal(context, pin)
        if (isValid) {
            resetFailedAttempts(context)
        } else {
            registerFailedAttempt(context)
        }
        return isValid
    }

    private fun validatePinInternal(context: Context, pin: String): Boolean {
        // 1. Memory Fast-Path
        if (validatePinFromMemory(pin)) {
            return true
        }

        val preferences = prefs(context)
        val normalizedPin = normalizePin(pin)
        val savedHash = preferences.getString(KEY_APP_LOCK_PIN_HASH, null) ?: return false
        val salt = preferences.getString(KEY_APP_LOCK_PIN_SALT, null) ?: return false
        val version = preferences.getString(KEY_APP_LOCK_PIN_HASH_VERSION, HASH_VERSION_FAST_SHA256_V1)

        // 2. Verify according to the stored hash version
        val isValid = when (version) {
            HASH_VERSION_PBKDF2_SHA256_V1, HASH_VERSION_PBKDF2_SHA1_V1 -> {
                val iterations = preferences.getInt(KEY_APP_LOCK_PIN_ITERATIONS, PBKDF2_ITERATIONS)
                val algorithm = if (version == HASH_VERSION_PBKDF2_SHA256_V1) PBKDF2_ALGORITHM_SHA256 else PBKDF2_ALGORITHM_SHA1
                MessageDigest.isEqual(
                    savedHash.toByteArray(),
                    pbkdf2Hash(normalizedPin, salt, iterations, algorithm).toByteArray()
                )
            }

            else -> {
                // Legacy fast/plain SHA-256 — verify, then upgrade to PBKDF2 in place.
                val legacyValid = MessageDigest.isEqual(
                    savedHash.toByteArray(),
                    fastPinHash(normalizedPin, salt).toByteArray()
                )
                if (legacyValid) {
                    upgradePinHashToPbkdf2(preferences, normalizedPin)
                }
                legacyValid
            }
        }

        if (isValid) {
            // Refresh the in-memory fast verifier (verification-only; not persisted).
            val verifier = FastPinVerifier(
                hash = fastPinHash(normalizedPin, salt),
                salt = salt,
                version = HASH_VERSION_FAST_SHA256_V1
            )
            cacheFastPinVerifier(verifier)
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

        if (state.lastBackgroundedAtMillis <= 0L) {
            return true
        }

        if (state.lastBackgroundedAtMillis <= state.lastUnlockedAtMillis) {
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

    /**
     * Marks (or clears) the in-memory "external activity" lock-suppression window.
     * Call right before launching an external activity (photo/file picker, browser,
     * system settings screen) so the app doesn't lock when the user returns.
     *
     * The window is only honored for LOCK_SUPPRESSION_TTL_MS (5 minutes); a stale
     * window is ignored and cleared on the next foreground check. Note: if the launch itself
     * fails (e.g. an ActivityNotFoundException swallowed by the caller), the flag
     * stays armed until the TTL — a genuine background + quick return inside that
     * window would then skip the lock once. Arm the flag immediately before the
     * launch, never earlier.
     */
    fun setLockSuppressed(active: Boolean) {
        lockSuppressionSetAtMillis = if (active) System.currentTimeMillis() else 0L
    }

    /**
     * True while a lock-suppression window is active and still within its validity
     * period. A stale window (the user never returned in time) expires and no longer
     * suppresses the lock. [currentTimeMillis] is injectable for tests.
     */
    fun isLockSuppressionActive(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        val setAt = lockSuppressionSetAtMillis
        return setAt > 0L && currentTimeMillis - setAt < LOCK_SUPPRESSION_TTL_MS
    }

    /**
     * Reads and clears the suppression window in one step. Returns true only if the
     * window was active (and unexpired); the caller is then expected to skip the
     * auto-lock check for this foreground cycle. [currentTimeMillis] is injectable
     * for tests.
     */
    fun consumeLockSuppression(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        val wasActive = isLockSuppressionActive(currentTimeMillis)
        lockSuppressionSetAtMillis = 0L
        return wasActive
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
            .putSaltedSecret(
                hashKey = KEY_SECURITY_ANSWER_HASH,
                saltKey = KEY_SECURITY_ANSWER_SALT,
                versionKey = KEY_SECURITY_ANSWER_HASH_VERSION,
                iterationsKey = KEY_SECURITY_ANSWER_ITERATIONS,
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
        if (isLockedOut(context)) {
            return false
        }

        val preferences = prefs(context)
        val normalizedAnswer = normalizeAnswer(answer)
        val savedHash = preferences.getString(KEY_SECURITY_ANSWER_HASH, null) ?: return false
        val salt = preferences.getString(KEY_SECURITY_ANSWER_SALT, null) ?: return false
        val version = preferences.getString(KEY_SECURITY_ANSWER_HASH_VERSION, HASH_VERSION_FAST_SHA256_V1)

        val isValid = when (version) {
            HASH_VERSION_PBKDF2_SHA256_V1, HASH_VERSION_PBKDF2_SHA1_V1 -> {
                val iterations = preferences.getInt(KEY_SECURITY_ANSWER_ITERATIONS, PBKDF2_ITERATIONS)
                val algorithm = if (version == HASH_VERSION_PBKDF2_SHA256_V1) PBKDF2_ALGORITHM_SHA256 else PBKDF2_ALGORITHM_SHA1
                MessageDigest.isEqual(
                    savedHash.toByteArray(),
                    pbkdf2Hash(normalizedAnswer, salt, iterations, algorithm).toByteArray()
                )
            }

            else -> {
                // Legacy fast/plain SHA-256 — verify, then upgrade to PBKDF2 in place.
                val legacyValid = MessageDigest.isEqual(
                    savedHash.toByteArray(),
                    fastPinHash(normalizedAnswer, salt).toByteArray()
                )
                if (legacyValid) {
                    upgradeAnswerHashToPbkdf2(preferences, normalizedAnswer)
                }
                legacyValid
            }
        }

        if (isValid) {
            resetFailedAttempts(context)
        } else {
            registerFailedAttempt(context)
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
        // A successful unlock (PIN or biometric) clears the brute-force counter.
        resetFailedAttempts(context)
    }

    /**
     * True while the app lock is in a brute-force lockout window (persisted, so it
     * survives app restarts). [currentTimeMillis] is injectable for tests.
     */
    fun isLockedOut(
        context: Context,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return getLockoutRemainingMillis(context, currentTimeMillis) > 0L
    }

    /**
     * Milliseconds remaining in the current lockout window (0 when not locked out).
     * [currentTimeMillis] is injectable for tests.
     */
    fun getLockoutRemainingMillis(
        context: Context,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        val until = prefs(context).getLong(KEY_LOCKOUT_UNTIL_MILLIS, 0L)
        return (until - currentTimeMillis).coerceAtLeast(0L)
    }

    /**
     * Records one failed PIN/answer attempt. Every [MAX_FAILED_ATTEMPTS_BEFORE_LOCKOUT]
     * consecutive failures arms the next lockout window: block 0 -> 30s, block 1 -> 60s,
     * block 2 -> 120s, ... capped at 15 min. The counter + block index are persisted so
     * the escalation survives app restarts, and attempts made DURING a lockout window are
     * ignored (they neither extend it nor advance the counter).
     */
    fun registerFailedAttempt(
        context: Context,
        currentTimeMillis: Long = System.currentTimeMillis()
    ) {
        val preferences = prefs(context)
        if (isLockedOut(context, currentTimeMillis)) {
            return
        }

        val newCount = preferences.getInt(KEY_FAILED_ATTEMPT_COUNT, 0) + 1
        val editor = preferences.edit().putInt(KEY_FAILED_ATTEMPT_COUNT, newCount)
        if (newCount >= MAX_FAILED_ATTEMPTS_BEFORE_LOCKOUT) {
            val blockIndex = preferences.getInt(KEY_LOCKOUT_BLOCK_INDEX, 0)
            editor
                .putLong(
                    KEY_LOCKOUT_UNTIL_MILLIS,
                    currentTimeMillis + computeLockoutDurationMillis(blockIndex)
                )
                .putInt(KEY_FAILED_ATTEMPT_COUNT, 0)
                .putInt(KEY_LOCKOUT_BLOCK_INDEX, blockIndex + 1)
        }
        // commit() (not apply()): the lockout is the "persist across restarts"
        // enforcement — an async write could be lost to a process kill right after
        // the 5th failure, silently clearing the lockout.
        editor.commit()
    }

    /**
     * Clears the failed-attempt counter, the lockout escalation block, and any
     * active lockout window.
     */
    fun resetFailedAttempts(context: Context) {
        prefs(context)
            .edit()
            .remove(KEY_FAILED_ATTEMPT_COUNT)
            .remove(KEY_LOCKOUT_BLOCK_INDEX)
            .remove(KEY_LOCKOUT_UNTIL_MILLIS)
            // commit(): clearing the counter is also security-relevant (it re-arms
            // the escalation from block 0), so it must survive a process kill.
            .commit()
    }

    /**
     * Number of consecutive failed attempts recorded since the last successful
     * unlock or since the last lockout window was armed (0 when clean).
     */
    fun getFailedAttemptCount(context: Context): Int {
        return prefs(context).getInt(KEY_FAILED_ATTEMPT_COUNT, 0)
    }

    /**
     * Lockout duration for a given lockout block index: block 0 -> 30s, block 1 -> 60s,
     * block 2 -> 120s, ... capped at [LOCKOUT_MAX_MILLIS] (15 min).
     */
    internal fun computeLockoutDurationMillis(
        lockoutBlockIndex: Int,
        initialLockoutMillis: Long = LOCKOUT_INITIAL_MILLIS,
        maxLockoutMillis: Long = LOCKOUT_MAX_MILLIS
    ): Long {
        if (lockoutBlockIndex < 0) return 0L
        val duration = initialLockoutMillis shl lockoutBlockIndex.coerceAtMost(10)
        return duration.coerceAtMost(maxLockoutMillis)
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

        if (lastBackgroundedAtMillis <= 0L || lastUnlockedAtMillis <= 0L) {
            return true
        }

        if (lastBackgroundedAtMillis < lastUnlockedAtMillis) {
            // App was likely force-stopped or crashed while unlocked
            return true
        }

        if (autoLockDurationMinutes <= 0) {
            return true
        }

        val elapsedMillis = currentTimeMillis - lastBackgroundedAtMillis
        return elapsedMillis >= autoLockDurationMinutes * 60_000L
    }

    fun clearAll(context: Context) {
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
            .remove(KEY_APP_LOCK_PIN_ITERATIONS)
            .remove(KEY_SECURITY_ANSWER_ITERATIONS)
            .remove(KEY_FAILED_ATTEMPT_COUNT)
            .remove(KEY_LOCKOUT_BLOCK_INDEX)
            .remove(KEY_LOCKOUT_UNTIL_MILLIS)
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
        iterationsKey: String,
        normalizedSecret: String
    ): SharedPreferences.Editor {
        val salt = generateSalt()
        val algorithm = defaultPbkdf2Algorithm()
        return putString(hashKey, pbkdf2Hash(normalizedSecret, salt, PBKDF2_ITERATIONS, algorithm))
            .putString(saltKey, salt)
            .putString(versionKey, hashVersionFor(algorithm))
            .putInt(iterationsKey, PBKDF2_ITERATIONS)
    }



    private fun generateSalt(): String {
        val saltBytes = ByteArray(SALT_LENGTH_BYTES)
        secureRandom.nextBytes(saltBytes)
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }



    private fun legacySha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(value.toByteArray())
        return hashBytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    /**
     * PBKDF2 (slow KDF) — the stored hash format for all new PINs/answers.
     * Uses HMAC-SHA-256 on API 26+ (PBKDF2WithHmacSHA256 requires O), falling
     * back to HMAC-SHA-1 on older devices. Iterations + algorithm are persisted
     * so verification always uses the same derivation the hash was created with.
     */
    internal fun pbkdf2Hash(
        normalizedSecret: String,
        salt: String,
        iterations: Int,
        algorithm: String = defaultPbkdf2Algorithm()
    ): String {
        return pbkdf2Hash(
            normalizedSecret = normalizedSecret,
            saltBytes = Base64.decode(salt, Base64.NO_WRAP),
            iterations = iterations,
            algorithm = algorithm
        )
    }

    /**
     * Pure PBKDF2 core (no android.util.Base64, so it is JVM unit-testable).
     */
    internal fun pbkdf2Hash(
        normalizedSecret: String,
        saltBytes: ByteArray,
        iterations: Int,
        algorithm: String = defaultPbkdf2Algorithm()
    ): String {
        val spec = PBEKeySpec(
            normalizedSecret.toCharArray(),
            saltBytes,
            iterations,
            PBKDF2_KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance(algorithm)
        val hashBytes = factory.generateSecret(spec).encoded
        return hashBytes.joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xFF) }
    }

    private fun upgradePinHashToPbkdf2(preferences: SharedPreferences, normalizedPin: String) {
        upgradeSecretToPbkdf2(
            preferences = preferences,
            hashKey = KEY_APP_LOCK_PIN_HASH,
            saltKey = KEY_APP_LOCK_PIN_SALT,
            versionKey = KEY_APP_LOCK_PIN_HASH_VERSION,
            iterationsKey = KEY_APP_LOCK_PIN_ITERATIONS,
            normalizedSecret = normalizedPin
        )
    }

    private fun upgradeAnswerHashToPbkdf2(preferences: SharedPreferences, normalizedAnswer: String) {
        upgradeSecretToPbkdf2(
            preferences = preferences,
            hashKey = KEY_SECURITY_ANSWER_HASH,
            saltKey = KEY_SECURITY_ANSWER_SALT,
            versionKey = KEY_SECURITY_ANSWER_HASH_VERSION,
            iterationsKey = KEY_SECURITY_ANSWER_ITERATIONS,
            normalizedSecret = normalizedAnswer
        )
    }

    private fun upgradeSecretToPbkdf2(
        preferences: SharedPreferences,
        hashKey: String,
        saltKey: String,
        versionKey: String,
        iterationsKey: String,
        normalizedSecret: String
    ) {
        try {
            val salt = generateSalt()
            val algorithm = defaultPbkdf2Algorithm()
            preferences.edit()
                .putString(hashKey, pbkdf2Hash(normalizedSecret, salt, PBKDF2_ITERATIONS, algorithm))
                .putString(saltKey, salt)
                .putString(versionKey, hashVersionFor(algorithm))
                .putInt(iterationsKey, PBKDF2_ITERATIONS)
                .apply()
        } catch (t: Throwable) {
            // Best-effort in-place upgrade: the legacy hash is left untouched, so a
            // storage failure here must never fail the unlock that triggered it —
            // the next successful unlock simply retries the upgrade.
        }
    }

    private fun defaultPbkdf2Algorithm(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PBKDF2_ALGORITHM_SHA256
        } else {
            PBKDF2_ALGORITHM_SHA1
        }
    }

    private fun hashVersionFor(algorithm: String): String {
        return if (algorithm == PBKDF2_ALGORITHM_SHA256) HASH_VERSION_PBKDF2_SHA256_V1 else HASH_VERSION_PBKDF2_SHA1_V1
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
