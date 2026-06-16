package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class UserProfile(
    val fullName: String,
    val emailAddress: String,
    val phoneNumber: String,
    val dateOfBirthMillis: Long?,
    val gender: String,
    val financialGoal: String = "",
    val memberSinceLabel: String,
    val accountTier: String,
    val photoUri: String? = null,
    val proExpiryTimestamp: Long = 0L,
    val updatedAtMillis: Long = 0L
)

val defaultUserProfile = UserProfile(
    fullName = "Guest User",
    emailAddress = "",
    phoneNumber = "",
    dateOfBirthMillis = null,
    gender = "",
    memberSinceLabel = "",
    accountTier = "",
    proExpiryTimestamp = 0L
)

fun UserProfile.firstName(): String {
    return fullName.trim().substringBefore(" ").ifBlank { "Guest" }
}

fun UserProfile.avatarInitials(): String {
    val parts = fullName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) {
        return "U"
    }

    return parts
        .take(2)
        .joinToString(separator = "") { it.first().uppercase() }
}
