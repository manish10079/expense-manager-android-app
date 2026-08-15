package com.mknlabs.expensetracker.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.utils.BackupEncryption
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoBackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val appSettings = AppSettingsDataStore.getAppSettingsFlow(context).first()

            if (!appSettings.isAutoBackupEnabled) {
                return Result.success()
            }

            // App-specific external files dir (getExternalMediaDirs() is deprecated
            // in API 34 and not meant for non-media app files).
            val backupDir = context.getExternalFilesDir("backup")
            if (backupDir == null) {
                Log.e("AutoBackupWorker", "No external files directory available")
                return Result.failure()
            }
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Cleanup old backups if > 5
            val existingBackups = backupDir.listFiles { _, name -> name.endsWith(".db") }
                ?.sortedBy { it.lastModified() }
                ?: emptyList()

            if (existingBackups.size >= 5) {
                // Delete oldest files to make room
                val filesToDelete = existingBackups.take(existingBackups.size - 4)
                filesToDelete.forEach { it.delete() }
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val dateString = dateFormat.format(Date())
            val newBackupFile = File(backupDir, "expense_tracker_backup_$dateString.db")

            // Ensure flush before copy
            val database = ExpenseTrackerDatabase.getInstance(context)
            database.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")).close()
            
            val dbFile = ExpenseTrackerDatabase.databaseFile(context)

            // Copy the live DB and encrypt it at rest (AES-256-GCM, Keystore key).
            // The Keystore key is device-bound, so these rolling backups are an
            // on-device safety net (corruption / accidental deletes), not a
            // cross-device restore path — that's the manual .db export or Firestore.
            val plaintext = dbFile.inputStream().use { it.readBytes() }
            val encrypted = BackupEncryption.encrypt(plaintext)
            newBackupFile.outputStream().use { it.write(encrypted) }

            // Update last backup time
            AppSettingsDataStore.updateAppSettings(context) { settings ->
                settings.copy(lastAutoBackupTimeMillis = System.currentTimeMillis())
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("AutoBackupWorker", "Error during backup", e)
            val settings = try { AppSettingsDataStore.getAppSettingsFlow(context).first() } catch (_: Exception) { null }
            if (settings?.isCloudSecurityEnabled != false) {
                com.mknlabs.expensetracker.notifications.NotificationHelper.showAutoBackupFailedNotification(context)
            }
            Result.retry() // Retry if failed due to transient issues
        }
    }
}
