package com.mkn0079.expensetracker.domain.repository

import android.net.Uri

interface LegacyImportRepository {
    suspend fun importBackup(uri: Uri): LegacyImportResult
}

data class LegacyImportResult(
    val totalTransactions: Int,
    val importedTransactions: Int,
    val skippedTransactions: Int
)
