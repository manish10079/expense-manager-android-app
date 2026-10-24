package com.mknlabs.expensetracker.domain.models

/** How Remote Config for the update dialog was obtained. */
enum class UpdateConfigStatus {
    Unknown,
    Loading,
    Fetched,
    Offline,
    Failed
}

/**
 * Copy and force flag for the in-app update dialog, from Firebase Remote
 * Config. Whether a store update exists is decided by Play In-App Updates,
 * not these fields.
 */
data class UpdateInfo(
    val forceUpdate: Boolean = false,
    val updateMessage: String = "",
    val latestVersion: String = "",
    val configStatus: UpdateConfigStatus = UpdateConfigStatus.Unknown
)
