package com.mkn0079.expensetracker.workers

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
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

            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val backupDir = File(documentsDir, "ExpenseTracker")
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
            
            dbFile.inputStream().use { input ->
                newBackupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Update last backup time
            AppSettingsDataStore.updateAppSettings(context) { settings ->
                settings.copy(lastAutoBackupTimeMillis = System.currentTimeMillis())
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("AutoBackupWorker", "Error during backup", e)
            Result.retry() // Retry if failed due to transient issues
        }
    }
}
