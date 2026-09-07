package com.mknlabs.expensetracker.domain.models

/**
 * Values that describe an available app update, sourced from Firebase Remote
 * Config. Blank/zero values mean "no update is configured" and the update
 * dialog falls back to localised strings for title/message.
 */
data class UpdateInfo(
    val latestVersionCode: Int = 0,
    val latestVersion: String = "",
    val forceUpdate: Boolean = false,
    val updateTitle: String = "",
    val updateMessage: String = ""
)