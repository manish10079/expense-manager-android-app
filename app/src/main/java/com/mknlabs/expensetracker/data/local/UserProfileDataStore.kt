package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.utils.SecureValueCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userProfileDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

/**
 * Security plan Item 5: the PII + premium fields below are stored encrypted at
 * rest via [SecureValueCipher] (AES-256-GCM, Keystore key). Legacy plaintext
 * values from older installs keep reading fine ([SecureValueCipher.decryptOrNull]
 * falls back to the raw value) and are re-written encrypted on the next update.
 *
 * Long-valued keys had to move to new `*_enc` string keys (DataStore keys are
 * typed) — the old long keys are still read as a fallback during migration.
 */
object UserProfileDataStore {

    private object Keys {
        val fullName = stringPreferencesKey("full_name")
        val emailAddress = stringPreferencesKey("email_address")
        val phoneNumber = stringPreferencesKey("phone_number")
        // Legacy (plaintext) long keys — still read until migrated.
        val dateOfBirthMillis = longPreferencesKey("date_of_birth_millis")
        val proExpiryTimestamp = longPreferencesKey("pro_expiry_timestamp")
        // Encrypted replacements (string keys, AES-GCM envelopes).
        val dateOfBirthMillisEnc = stringPreferencesKey("date_of_birth_millis_enc")
        val proExpiryTimestampEnc = stringPreferencesKey("pro_expiry_timestamp_enc")
        val gender = stringPreferencesKey("gender")
        val financialGoal = stringPreferencesKey("financial_goal")
        val accountCreatedMillis = longPreferencesKey("account_created_millis")
        val accountTier = stringPreferencesKey("account_tier")
        val photoUri = stringPreferencesKey("photo_uri")
        val isSubscription = androidx.datastore.preferences.core.booleanPreferencesKey("is_subscription")
        val updatedAtMillis = longPreferencesKey("updated_at_millis")
        val authProvider = stringPreferencesKey("auth_provider")
    }

    fun getUserProfileFlow(context: Context): Flow<UserProfile> {
        return context.applicationContext.userProfileDataStore.data.map { preferences ->
            preferences.toUserProfile(context)
        }
    }

    suspend fun initialize(context: Context) {
        context.applicationContext.userProfileDataStore.edit { preferences ->
            if (preferences[Keys.updatedAtMillis] == null) {
                preferences[Keys.updatedAtMillis] = 0L
            }
        }
    }

    suspend fun updateUserProfile(context: Context, transform: (UserProfile) -> UserProfile) {
        context.applicationContext.userProfileDataStore.edit { preferences ->
            val currentProfile = preferences.toUserProfile(context)
            val updatedProfile = transform(currentProfile)
            preferences.writeUserProfile(context, updatedProfile)
        }
    }

    suspend fun setUserProfile(context: Context, profile: UserProfile) {
        context.applicationContext.userProfileDataStore.edit { preferences ->
            preferences.writeUserProfile(context, profile)
        }
    }

    private fun Preferences.toUserProfile(context: Context): UserProfile {
        fun decrypt(raw: String?): String? {
            if (raw == null) return null
            // Encrypted value -> plaintext; legacy plaintext -> itself.
            return SecureValueCipher.decryptOrNull(raw) ?: raw
        }

        fun decryptLong(encRaw: String?, legacy: Long?): Long? {
            if (encRaw != null) {
                return SecureValueCipher.decryptOrNull(encRaw)?.toLongOrNull() ?: legacy
            }
            return legacy
        }

        return UserProfile(
            fullName = decrypt(this[Keys.fullName]) ?: defaultUserProfile.fullName,
            emailAddress = decrypt(this[Keys.emailAddress]) ?: defaultUserProfile.emailAddress,
            phoneNumber = decrypt(this[Keys.phoneNumber]) ?: defaultUserProfile.phoneNumber,
            dateOfBirthMillis = decryptLong(this[Keys.dateOfBirthMillisEnc], this[Keys.dateOfBirthMillis])
                ?: defaultUserProfile.dateOfBirthMillis,
            gender = this[Keys.gender] ?: defaultUserProfile.gender,
            financialGoal = this[Keys.financialGoal] ?: defaultUserProfile.financialGoal,
            accountCreatedMillis = this[Keys.accountCreatedMillis] ?: defaultUserProfile.accountCreatedMillis,
            accountTier = decrypt(this[Keys.accountTier]) ?: defaultUserProfile.accountTier,
            photoUri = decrypt(this[Keys.photoUri]) ?: defaultUserProfile.photoUri,
            proExpiryTimestamp = decryptLong(this[Keys.proExpiryTimestampEnc], this[Keys.proExpiryTimestamp])
                ?: defaultUserProfile.proExpiryTimestamp,
            isSubscription = this[Keys.isSubscription] ?: defaultUserProfile.isSubscription,
            updatedAtMillis = this[Keys.updatedAtMillis] ?: defaultUserProfile.updatedAtMillis,
            authProvider = this[Keys.authProvider] ?: defaultUserProfile.authProvider
        )
    }

    private fun MutablePreferences.writeUserProfile(context: Context, profile: UserProfile) {
        fun encrypt(value: String): String = SecureValueCipher.encrypt(value)

        this[Keys.fullName] = encrypt(profile.fullName)
        this[Keys.emailAddress] = encrypt(profile.emailAddress)
        this[Keys.phoneNumber] = encrypt(profile.phoneNumber)

        profile.dateOfBirthMillis?.let {
            this[Keys.dateOfBirthMillisEnc] = encrypt(it.toString())
            remove(Keys.dateOfBirthMillis)
        } ?: run {
            remove(Keys.dateOfBirthMillisEnc)
            remove(Keys.dateOfBirthMillis)
        }

        this[Keys.gender] = profile.gender
        this[Keys.financialGoal] = profile.financialGoal
        this[Keys.accountCreatedMillis] = profile.accountCreatedMillis
        this[Keys.accountTier] = encrypt(profile.accountTier)

        profile.photoUri?.let { this[Keys.photoUri] = encrypt(it) } ?: remove(Keys.photoUri)

        this[Keys.proExpiryTimestampEnc] = encrypt(profile.proExpiryTimestamp.toString())
        remove(Keys.proExpiryTimestamp)

        this[Keys.isSubscription] = profile.isSubscription
        this[Keys.updatedAtMillis] = profile.updatedAtMillis
        this[Keys.authProvider] = profile.authProvider
    }

    suspend fun clearAll(context: Context) {
        context.applicationContext.userProfileDataStore.edit { it.clear() }
    }
}
