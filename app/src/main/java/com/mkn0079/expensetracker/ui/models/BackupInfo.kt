package com.mkn0079.expensetracker.ui.models

import android.net.Uri

/**
 * Metadata for a database backup file.
 */
data class BackupInfo(
    val fileName: String,
    val filePath: String,
    val uri: Uri,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val isAutoBackup: Boolean
)
