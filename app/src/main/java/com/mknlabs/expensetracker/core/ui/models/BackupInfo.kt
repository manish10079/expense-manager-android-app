package com.mknlabs.expensetracker.core.ui.models

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
    val isAutoBackup: Boolean,
    val isEncrypted: Boolean = false
)
