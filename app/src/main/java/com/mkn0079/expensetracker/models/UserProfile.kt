package com.mkn0079.expensetracker.models

import androidx.compose.runtime.Immutable

@Immutable
data class UserProfile(
    val fullName: String,
    val emailAddress: String,
    val phoneNumber: String,
    val dateOfBirthMillis: Long?,
    val gender: String,
    val memberSinceLabel: String,
    val accountTier: String,
    val photoUri: String? = null,
    val updatedAtMillis: Long = 0L
)

val defaultUserProfile = UserProfile(
    fullName = "Guest User",
    emailAddress = "",
    phoneNumber = "",
    dateOfBirthMillis = null,
    gender = "",
    memberSinceLabel = "",
    accountTier = ""
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
