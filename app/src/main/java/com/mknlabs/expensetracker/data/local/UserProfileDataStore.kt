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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userProfileDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

object UserProfileDataStore {

    private object Keys {
        val fullName = stringPreferencesKey("full_name")
        val emailAddress = stringPreferencesKey("email_address")
        val phoneNumber = stringPreferencesKey("phone_number")
        val dateOfBirthMillis = longPreferencesKey("date_of_birth_millis")
        val gender = stringPreferencesKey("gender")
        val financialGoal = stringPreferencesKey("financial_goal")
        val memberSinceLabel = stringPreferencesKey("member_since_label")
        val accountTier = stringPreferencesKey("account_tier")
        val photoUri = stringPreferencesKey("photo_uri")
        val proExpiryTimestamp = longPreferencesKey("pro_expiry_timestamp")
        val updatedAtMillis = longPreferencesKey("updated_at_millis")
    }

    fun getUserProfileFlow(context: Context): Flow<UserProfile> {
        return context.applicationContext.userProfileDataStore.data.map { preferences ->
            preferences.toUserProfile()
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
            val currentProfile = preferences.toUserProfile()
            val updatedProfile = transform(currentProfile)
            preferences.writeUserProfile(updatedProfile)
        }
    }

    suspend fun setUserProfile(context: Context, profile: UserProfile) {
        context.applicationContext.userProfileDataStore.edit { preferences ->
            preferences.writeUserProfile(profile)
        }
    }

    private fun Preferences.toUserProfile(): UserProfile {
        return UserProfile(
            fullName = this[Keys.fullName] ?: defaultUserProfile.fullName,
            emailAddress = this[Keys.emailAddress] ?: defaultUserProfile.emailAddress,
            phoneNumber = this[Keys.phoneNumber] ?: defaultUserProfile.phoneNumber,
            dateOfBirthMillis = this[Keys.dateOfBirthMillis] ?: defaultUserProfile.dateOfBirthMillis,
            gender = this[Keys.gender] ?: defaultUserProfile.gender,
            financialGoal = this[Keys.financialGoal] ?: defaultUserProfile.financialGoal,
            memberSinceLabel = this[Keys.memberSinceLabel] ?: defaultUserProfile.memberSinceLabel,
            accountTier = this[Keys.accountTier] ?: defaultUserProfile.accountTier,
            photoUri = this[Keys.photoUri] ?: defaultUserProfile.photoUri,
            proExpiryTimestamp = this[Keys.proExpiryTimestamp] ?: defaultUserProfile.proExpiryTimestamp,
            updatedAtMillis = this[Keys.updatedAtMillis] ?: defaultUserProfile.updatedAtMillis
        )
    }

    private fun MutablePreferences.writeUserProfile(profile: UserProfile) {
        this[Keys.fullName] = profile.fullName
        this[Keys.emailAddress] = profile.emailAddress
        this[Keys.phoneNumber] = profile.phoneNumber
        profile.dateOfBirthMillis?.let { this[Keys.dateOfBirthMillis] = it } ?: remove(Keys.dateOfBirthMillis)
        this[Keys.gender] = profile.gender
        this[Keys.financialGoal] = profile.financialGoal
        this[Keys.memberSinceLabel] = profile.memberSinceLabel
        this[Keys.accountTier] = profile.accountTier
        profile.photoUri?.let { this[Keys.photoUri] = it } ?: remove(Keys.photoUri)
        this[Keys.proExpiryTimestamp] = profile.proExpiryTimestamp
        this[Keys.updatedAtMillis] = profile.updatedAtMillis
    }

    suspend fun clearAll(context: Context) {
        context.applicationContext.userProfileDataStore.edit { it.clear() }
    }
}
