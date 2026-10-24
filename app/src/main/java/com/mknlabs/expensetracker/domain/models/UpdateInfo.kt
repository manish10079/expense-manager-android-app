package com.mknlabs.expensetracker.domain.models

/**
 * Copy and force flag for the in-app update dialog, from Firebase Remote
 * Config. Whether a store update exists is decided by Play In-App Updates,
 * not these fields. Blank message falls back to localised strings.
 */
data class UpdateInfo(
    val forceUpdate: Boolean = false,
    val updateMessage: String = "",
    val latestVersion: String = ""
)
