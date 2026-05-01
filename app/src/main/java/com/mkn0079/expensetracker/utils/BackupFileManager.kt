package com.mkn0079.expensetracker.utils

import android.content.Context
import android.net.Uri
import com.mkn0079.expensetracker.ui.models.BackupInfo
import java.io.File

object BackupFileManager {

    /**
     * Scans the internal backup directory for .db files.
     */
    fun getAvailableBackups(context: Context): List<BackupInfo> {
        val mediaDirs = context.getExternalMediaDirs()
        if (mediaDirs.isEmpty()) return emptyList()
        
        val backupDir = File(mediaDirs[0], "backup")
        if (!backupDir.exists() || !backupDir.isDirectory) return emptyList()

        return backupDir.listFiles { _, name -> name.endsWith(".db") }
            ?.map { file ->
                BackupInfo(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    uri = Uri.fromFile(file),
                    sizeBytes = file.length(),
                    lastModifiedMillis = file.lastModified(),
                    isAutoBackup = file.name.startsWith("expense_tracker_backup_")
                )
            }
            ?.sortedByDescending { it.lastModifiedMillis }
            ?: emptyList()
    }

    /**
     * Formats file size to human readable string.
     */
    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", sizeBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
