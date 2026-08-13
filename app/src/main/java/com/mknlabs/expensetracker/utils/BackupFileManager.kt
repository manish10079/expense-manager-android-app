package com.mknlabs.expensetracker.utils

import android.content.Context
import android.net.Uri
import com.mknlabs.expensetracker.ui.models.BackupInfo
import java.io.File

object BackupFileManager {

    /**
     * Scans the internal backup directory for .db files.
     */
    fun getAvailableBackups(context: Context): List<BackupInfo> {
        // Primary location: app-specific external files dir
        // (Android/data/<pkg>/files/backup) — getExternalMediaDirs() is deprecated
        // (API 34) and unsuitable for non-media app files.
        val primaryBackupDir = context.getExternalFilesDir("backup")
        // Legacy location: older builds wrote to Android/media/<pkg>/backup via
        // getExternalMediaDirs(). Keep scanning it so existing backups stay visible
        // until they are overwritten/migrated.
        @Suppress("DEPRECATION")
        val legacyBackupDir = context.getExternalMediaDirs().firstOrNull()?.let { File(it, "backup") }

        val backupDirs = listOfNotNull(primaryBackupDir, legacyBackupDir)
            .filter { it.exists() && it.isDirectory }
        if (backupDirs.isEmpty()) return emptyList()

        return backupDirs.flatMap { backupDir ->
            backupDir.listFiles { _, name -> name.endsWith(".db") }
                ?.map { file ->
                    BackupInfo(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        uri = Uri.fromFile(file),
                        sizeBytes = file.length(),
                        lastModifiedMillis = file.lastModified(),
                        isAutoBackup = file.name.startsWith("expense_tracker_backup_"),
                        isEncrypted = BackupEncryption.isEncryptedFile(file)
                    )
                }
                ?: emptyList()
        }.sortedByDescending { it.lastModifiedMillis }
    }

    /**
     * Formats file size to human readable string.
     */
    fun formatFileSize(context: Context, sizeBytes: Long): String {
        if (sizeBytes <= 0) return context.getString(com.mknlabs.expensetracker.R.string.label_zero_size)
        val units = arrayOf(
            context.getString(com.mknlabs.expensetracker.R.string.label_size_unit_b),
            context.getString(com.mknlabs.expensetracker.R.string.label_size_unit_kb),
            context.getString(com.mknlabs.expensetracker.R.string.label_size_unit_mb),
            context.getString(com.mknlabs.expensetracker.R.string.label_size_unit_gb),
            context.getString(com.mknlabs.expensetracker.R.string.label_size_unit_tb)
        )
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        return context.getString(
            com.mknlabs.expensetracker.R.string.format_size_with_unit,
            sizeBytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }
}
