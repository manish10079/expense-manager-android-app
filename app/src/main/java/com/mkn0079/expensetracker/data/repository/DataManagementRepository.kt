package com.mkn0079.expensetracker.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer
import com.mkn0079.expensetracker.data.local.room.toDomain
import com.mkn0079.expensetracker.data.local.room.toEntity
import com.mkn0079.expensetracker.data.local.room.entities.BudgetEntity
import com.mkn0079.expensetracker.data.local.room.entities.CategoryEntity
import com.mkn0079.expensetracker.data.local.room.entities.PaymentMethodEntity
import com.mkn0079.expensetracker.data.local.room.entities.RecurringRuleEntity
import com.mkn0079.expensetracker.data.local.room.entities.TransactionEntity
import com.mkn0079.expensetracker.domain.repository.DataManagementRepository as DomainDataManagementRepository
import com.mkn0079.expensetracker.domain.repository.JsonExportResult
import com.mkn0079.expensetracker.domain.repository.JsonImportResult
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.models.SyncState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private const val JSON_SCHEMA_VERSION = 1

class DataManagementRepository(
    context: Context
) : DomainDataManagementRepository {
    private val appContext = context.applicationContext

    override suspend fun backupDatabase(uri: Uri) {
        val database = ExpenseTrackerDatabase.getInstance(appContext)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) supports VACUUM INTO, which is the safest way 
            // to create a consistent backup of a live WAL database.
            val tempFile = File.createTempFile("expense-tracker-backup-", ".db", appContext.cacheDir)
            try {
                // Ensure the temp file is deleted before VACUUM INTO tries to create it
                if (tempFile.exists()) tempFile.delete()
                
                database.openHelper.writableDatabase.execSQL("VACUUM INTO '${tempFile.absolutePath}'")
                
                copyFileToUri(
                    file = tempFile,
                    destination = uri
                )
            } finally {
                if (tempFile.exists()) tempFile.delete()
            }
        } else {
            // Fallback for older versions: Force a full checkpoint and wait for completion.
            // RESTART is more aggressive than TRUNCATE as it ensures all data is moved
            // and the WAL file is effectively reset.
            database.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(RESTART)")).use { cursor ->
                if (cursor.moveToFirst()) {
                    // Log checkpoint result for debugging if needed
                    // val status = cursor.getInt(0)
                    // val walPages = cursor.getInt(1)
                    // val checkpointedPages = cursor.getInt(2)
                }
            }
            
            copyFileToUri(
                file = ExpenseTrackerDatabase.databaseFile(appContext),
                destination = uri
            )
        }
    }

    override suspend fun restoreDatabase(uri: Uri) {
        val tempBackupFile = copyUriToTempFile(uri, suffix = ".db")
        val databaseFile = ExpenseTrackerDatabase.databaseFile(appContext)
        val parentDirectory = databaseFile.parentFile
            ?: error("Unable to resolve the database directory.")
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }

        ExpenseTrackerDatabase.closeInstance()
        deleteDatabaseSidecars()

        tempBackupFile.inputStream().use { input ->
            databaseFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        deleteDatabaseSidecars()
        tempBackupFile.delete()
    }

    override suspend fun exportJson(uri: Uri): JsonExportResult {
        val database = ExpenseTrackerDatabase.getInstance(appContext)

        val categories = database.categoryDao().getActiveCategories()
        val paymentMethods = database.paymentMethodDao().getActivePaymentMethods()
        val transactions = database.transactionDao().getActiveTransactions()
        val budgets = database.budgetDao().getActiveBudgets()
        val recurringRules = database.recurringRuleDao().getActiveRules()

        val payload = JSONObject().apply {
            put("schemaVersion", JSON_SCHEMA_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("categories", JSONArray().apply {
                categories.forEach { put(it.toJson()) }
            })
            put("paymentMethods", JSONArray().apply {
                paymentMethods.forEach { put(it.toJson()) }
            })
            put("transactions", JSONArray().apply {
                transactions.forEach { put(it.toJson()) }
            })
            put("budgets", JSONArray().apply {
                budgets.forEach { put(it.toJson()) }
            })
            put("recurringRules", JSONArray().apply {
                recurringRules.forEach { put(it.toJson()) }
            })
        }

        writeTextToUri(
            uri = uri,
            content = payload.toString(2)
        )

        return JsonExportResult(
            exportedTransactions = transactions.size,
            exportedBudgets = budgets.size,
            exportedRecurringRules = recurringRules.size,
            exportedCategories = categories.size,
            exportedPaymentMethods = paymentMethods.size
        )
    }

    override suspend fun importJson(uri: Uri): JsonImportResult {
        ExpenseTrackerDatabaseInitializer.initialize(appContext)

        val json = appContext.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Unable to open the selected JSON file.")
        val root = JSONObject(json)
        val schemaVersion = root.optInt("schemaVersion", 1)
        require(schemaVersion <= JSON_SCHEMA_VERSION) {
            "This JSON backup was created by a newer app version and cannot be imported."
        }

        val importedCategories = root.optJSONArray("categories").toCategoryEntities()
        val importedPaymentMethods = root.optJSONArray("paymentMethods").toPaymentMethodEntities()
        val importedTransactions = root.optJSONArray("transactions").toTransactionEntities()
        val importedBudgets = root.optJSONArray("budgets").toBudgetEntities()
        val importedRecurringRules = root.optJSONArray("recurringRules").toRecurringRuleEntities()

        val database = ExpenseTrackerDatabase.getInstance(appContext)
        val categoryDao = database.categoryDao()
        val paymentMethodDao = database.paymentMethodDao()
        val transactionDao = database.transactionDao()
        val budgetDao = database.budgetDao()
        val recurringRuleDao = database.recurringRuleDao()

        var importedCategoryCount = 0
        var skippedCategoryCount = 0
        var importedPaymentMethodCount = 0
        var skippedPaymentMethodCount = 0
        var importedTransactionCount = 0
        var skippedTransactionCount = 0
        var importedBudgetCount = 0
        var skippedBudgetCount = 0
        var importedRecurringRuleCount = 0
        var skippedRecurringRuleCount = 0

        database.withTransaction {
            val categoryIdMap = mutableMapOf<Int, Int>()
            val paymentMethodIdMap = mutableMapOf<Int, Int>()
            val transactionIdMap = mutableMapOf<String, String>()
            val ruleIdMap = mutableMapOf<String, String>()
            val pendingTransactionRuleLinks = mutableMapOf<String, String>()

            val existingCategories = categoryDao.getActiveCategories().toMutableList()
            var nextCategoryId = (existingCategories.maxOfOrNull { it.id } ?: 0) + 1

            importedCategories.forEach { imported ->
                val existingCategory = when {
                    imported.isSystem -> existingCategories.firstOrNull { it.id == imported.id }
                    else -> existingCategories.firstOrNull {
                        !it.isSystem &&
                            it.transactionTypeId == imported.transactionTypeId &&
                            it.name.normalizedKey() == imported.name.normalizedKey()
                    }
                }

                if (existingCategory != null) {
                    categoryIdMap[imported.id] = existingCategory.id
                    skippedCategoryCount++
                    return@forEach
                }

                val resolvedCategory = if (imported.isSystem) {
                    imported.copy(isDeleted = false)
                } else {
                    imported.copy(
                        id = nextCategoryId++,
                        isSystem = false,
                        isDeleted = false
                    )
                }
                categoryDao.upsert(resolvedCategory)
                existingCategories += resolvedCategory
                categoryIdMap[imported.id] = resolvedCategory.id
                importedCategoryCount++
            }

            val existingPaymentMethods = paymentMethodDao.getActivePaymentMethods().toMutableList()
            var nextPaymentMethodId = (existingPaymentMethods.maxOfOrNull { it.id } ?: 0) + 1

            importedPaymentMethods.forEach { imported ->
                val existingPaymentMethod = when {
                    imported.isSystem -> existingPaymentMethods.firstOrNull { it.id == imported.id }
                    else -> existingPaymentMethods.firstOrNull {
                        !it.isSystem &&
                            it.name.normalizedKey() == imported.name.normalizedKey()
                    }
                }

                if (existingPaymentMethod != null) {
                    paymentMethodIdMap[imported.id] = existingPaymentMethod.id
                    skippedPaymentMethodCount++
                    return@forEach
                }

                val resolvedPaymentMethod = if (imported.isSystem) {
                    imported.copy(isDeleted = false)
                } else {
                    imported.copy(
                        id = nextPaymentMethodId++,
                        isSystem = false,
                        isDeleted = false
                    )
                }
                paymentMethodDao.upsert(resolvedPaymentMethod)
                existingPaymentMethods += resolvedPaymentMethod
                paymentMethodIdMap[imported.id] = resolvedPaymentMethod.id
                importedPaymentMethodCount++
            }

            val existingTransactions = transactionDao.getAllTransactions().toMutableList()

            importedTransactions.forEach { imported ->
                val resolvedTransaction = imported.copy(
                    categoryId = categoryIdMap[imported.categoryId] ?: imported.categoryId,
                    paymentMethodId = paymentMethodIdMap[imported.paymentMethodId] ?: imported.paymentMethodId,
                    isDeleted = false,
                    sourceRecurringRuleId = null
                )
                val duplicate = existingTransactions.firstOrNull { existing ->
                    existing.id == resolvedTransaction.id || existing.isLogicalDuplicateOf(resolvedTransaction)
                }

                if (duplicate != null) {
                    transactionIdMap[imported.id] = duplicate.id
                    skippedTransactionCount++
                    return@forEach
                }

                val preparedTransaction = resolvedTransaction.copy(
                    contentHash = TransactionContentHashBuilder.build(
                        resolvedTransaction.toDomain()
                    )
                )
                transactionDao.upsert(preparedTransaction)
                existingTransactions += preparedTransaction
                transactionIdMap[imported.id] = preparedTransaction.id
                importedTransactionCount++

                imported.sourceRecurringRuleId?.takeIf { it.isNotBlank() }?.let { importedRuleId ->
                    pendingTransactionRuleLinks[preparedTransaction.id] = importedRuleId
                }
            }

            val existingBudgets = budgetDao.getActiveBudgets().toMutableList()

            importedBudgets.forEach { imported ->
                val resolvedBudget = imported.copy(
                    categoryId = categoryIdMap[imported.categoryId] ?: imported.categoryId,
                    isDeleted = false
                )
                val duplicate = existingBudgets.firstOrNull {
                    it.categoryId == resolvedBudget.categoryId && it.monthStart == resolvedBudget.monthStart
                }

                if (duplicate != null) {
                    skippedBudgetCount++
                    return@forEach
                }

                val existingId = budgetDao.getById(resolvedBudget.id)
                val finalBudget = if (resolvedBudget.id.isBlank() || existingId != null) {
                    resolvedBudget.copy(id = UUID.randomUUID().toString())
                } else {
                    resolvedBudget
                }
                budgetDao.upsert(finalBudget)
                existingBudgets += finalBudget
                importedBudgetCount++
            }

            val existingRecurringRules = recurringRuleDao.getActiveRules().toMutableList()

            importedRecurringRules.forEach { imported ->
                val resolvedTransactionId = transactionIdMap[imported.transactionId]
                if (resolvedTransactionId.isNullOrBlank()) {
                    skippedRecurringRuleCount++
                    return@forEach
                }

                val duplicate = existingRecurringRules.firstOrNull {
                    it.transactionId == resolvedTransactionId
                }
                if (duplicate != null) {
                    ruleIdMap[imported.id] = duplicate.id
                    skippedRecurringRuleCount++
                    return@forEach
                }

                val finalRuleId = if (
                    imported.id.isBlank() ||
                    existingRecurringRules.any { it.id == imported.id } ||
                    recurringRuleDao.getById(imported.id) != null
                ) {
                    UUID.randomUUID().toString()
                } else {
                    imported.id
                }

                val resolvedRule = imported.copy(
                    id = finalRuleId,
                    transactionId = resolvedTransactionId,
                    isDeleted = false
                )
                recurringRuleDao.upsert(resolvedRule)
                existingRecurringRules += resolvedRule
                ruleIdMap[imported.id] = resolvedRule.id
                importedRecurringRuleCount++
            }

            val linkUpdatedAt = System.currentTimeMillis()
            pendingTransactionRuleLinks.forEach { (transactionId, importedRuleId) ->
                val resolvedRuleId = ruleIdMap[importedRuleId] ?: return@forEach
                val index = existingTransactions.indexOfFirst { it.id == transactionId }
                if (index < 0) {
                    return@forEach
                }

                val existingTransaction = existingTransactions[index]
                val updatedTransaction = existingTransaction.copy(
                    sourceRecurringRuleId = resolvedRuleId,
                    updatedAt = linkUpdatedAt,
                    contentHash = TransactionContentHashBuilder.build(
                        existingTransaction.toDomain().copy(
                            sourceRecurringRuleId = resolvedRuleId,
                            updatedAt = linkUpdatedAt
                        )
                    )
                )
                transactionDao.updateRecurringSourceReference(
                    id = updatedTransaction.id,
                    sourceRecurringRuleId = updatedTransaction.sourceRecurringRuleId,
                    contentHash = updatedTransaction.contentHash,
                    updatedAt = updatedTransaction.updatedAt
                )
                existingTransactions[index] = updatedTransaction
            }
        }

        return JsonImportResult(
            importedTransactions = importedTransactionCount,
            skippedTransactions = skippedTransactionCount,
            importedBudgets = importedBudgetCount,
            skippedBudgets = skippedBudgetCount,
            importedRecurringRules = importedRecurringRuleCount,
            skippedRecurringRules = skippedRecurringRuleCount,
            importedCategories = importedCategoryCount,
            skippedCategories = skippedCategoryCount,
            importedPaymentMethods = importedPaymentMethodCount,
            skippedPaymentMethods = skippedPaymentMethodCount
        )
    }

    private fun copyUriToTempFile(
        uri: Uri,
        suffix: String
    ): File {
        val tempFile = File.createTempFile("expense-tracker-", suffix, appContext.cacheDir)
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open the selected file.")
        return tempFile
    }

    private fun copyFileToUri(
        file: File,
        destination: Uri
    ) {
        require(file.exists()) { "Database file not found." }
        appContext.contentResolver.openOutputStream(destination, "w")?.use { output ->
            file.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: error("Unable to create the selected file.")
    }

    private fun deleteDatabaseSidecars() {
        ExpenseTrackerDatabase.databaseWalFile(appContext).delete()
        ExpenseTrackerDatabase.databaseShmFile(appContext).delete()
    }

    private fun writeTextToUri(
        uri: Uri,
        content: String
    ) {
        val fileDescriptor = appContext.contentResolver.openFileDescriptor(uri, "w")
            ?: error("Unable to create the selected file.")
        fileDescriptor.use { descriptor ->
            FileOutputStream(descriptor.fileDescriptor).bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(content)
                writer.flush()
            }
        }
    }
}

private fun JSONArray?.toCategoryEntities(): List<CategoryEntity> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            add(getJSONObject(index).toCategoryEntity())
        }
    }
}

private fun JSONArray?.toPaymentMethodEntities(): List<PaymentMethodEntity> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            add(getJSONObject(index).toPaymentMethodEntity())
        }
    }
}

private fun JSONArray?.toTransactionEntities(): List<TransactionEntity> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            add(getJSONObject(index).toTransactionEntity())
        }
    }
}

private fun JSONArray?.toBudgetEntities(): List<BudgetEntity> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            add(getJSONObject(index).toBudgetEntity())
        }
    }
}

private fun JSONArray?.toRecurringRuleEntities(): List<RecurringRuleEntity> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            add(getJSONObject(index).toRecurringRuleEntity())
        }
    }
}

private fun CategoryEntity.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("name", name)
        put("transactionTypeId", transactionTypeId)
        put("iconKey", iconKey)
        put("isSystem", isSystem)
        put("sortOrder", sortOrder)
        put("isDeleted", isDeleted)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }
}

private fun PaymentMethodEntity.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("name", name)
        put("iconKey", iconKey)
        put("isSystem", isSystem)
        put("sortOrder", sortOrder)
        put("isDeleted", isDeleted)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }
}

private fun TransactionEntity.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("note", note)
        put("amountMinor", amountMinor)
        put("occurredAt", occurredAt)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("transactionTypeId", transactionTypeId)
        put("categoryId", categoryId)
        put("paymentMethodId", paymentMethodId)
        put("isDeleted", isDeleted)
        put("syncState", syncState.name)
        putNullable("contentHash", contentHash)
        putNullable("sourceRecurringRuleId", sourceRecurringRuleId)
    }
}

private fun BudgetEntity.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("categoryId", categoryId)
        put("monthStart", monthStart)
        put("limitMinor", limitMinor)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("syncState", syncState.name)
        put("isDeleted", isDeleted)
    }
}

private fun RecurringRuleEntity.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("transactionId", transactionId)
        put("frequency", frequency.name)
        put("intervalCount", intervalCount)
        put("repeatCount", repeatCount)
        putNullable("remainingCount", remainingCount)
        put("anchorAt", anchorAt)
        put("nextRunAt", nextRunAt)
        putNullable("lastRunAt", lastRunAt)
        put("isEnabled", isEnabled)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("syncState", syncState.name)
        put("isDeleted", isDeleted)
    }
}

private fun JSONObject.toCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = getInt("id"),
        name = getString("name"),
        transactionTypeId = getInt("transactionTypeId"),
        iconKey = getString("iconKey"),
        isSystem = optBoolean("isSystem", true),
        sortOrder = optInt("sortOrder", getInt("id")),
        isDeleted = optBoolean("isDeleted", false),
        createdAt = optLong("createdAt", 0L),
        updatedAt = optLong("updatedAt", optLong("createdAt", 0L))
    )
}

private fun JSONObject.toPaymentMethodEntity(): PaymentMethodEntity {
    return PaymentMethodEntity(
        id = getInt("id"),
        name = getString("name"),
        iconKey = getString("iconKey"),
        isSystem = optBoolean("isSystem", true),
        sortOrder = optInt("sortOrder", getInt("id")),
        isDeleted = optBoolean("isDeleted", false),
        createdAt = optLong("createdAt", 0L),
        updatedAt = optLong("updatedAt", optLong("createdAt", 0L))
    )
}

private fun JSONObject.toTransactionEntity(): TransactionEntity {
    val occurredAt = optLong("occurredAt", optLong("createdAt", 0L))
    return TransactionEntity(
        id = getString("id"),
        note = optString("note"),
        amountMinor = getLong("amountMinor"),
        occurredAt = occurredAt,
        createdAt = optLong("createdAt", occurredAt),
        updatedAt = optLong("updatedAt", occurredAt),
        transactionTypeId = getInt("transactionTypeId"),
        categoryId = getInt("categoryId"),
        paymentMethodId = getInt("paymentMethodId"),
        isDeleted = optBoolean("isDeleted", false),
        syncState = optString("syncState").toSyncState(),
        contentHash = optNullableString("contentHash"),
        sourceRecurringRuleId = optNullableString("sourceRecurringRuleId")
    )
}

private fun JSONObject.toBudgetEntity(): BudgetEntity {
    val createdAt = optLong("createdAt", System.currentTimeMillis())
    return BudgetEntity(
        id = getString("id"),
        categoryId = getInt("categoryId"),
        monthStart = getLong("monthStart"),
        limitMinor = getLong("limitMinor"),
        createdAt = createdAt,
        updatedAt = optLong("updatedAt", createdAt),
        syncState = optString("syncState").toSyncState(),
        isDeleted = optBoolean("isDeleted", false)
    )
}

private fun JSONObject.toRecurringRuleEntity(): RecurringRuleEntity {
    val anchorAt = optLong("anchorAt", System.currentTimeMillis())
    val createdAt = optLong("createdAt", anchorAt)
    return RecurringRuleEntity(
        id = getString("id"),
        transactionId = getString("transactionId"),
        frequency = optString("frequency").toRecurringFrequency(),
        intervalCount = optInt("intervalCount", 1),
        repeatCount = optInt("repeatCount", 1),
        remainingCount = optNullableInt("remainingCount"),
        anchorAt = anchorAt,
        nextRunAt = optLong("nextRunAt", anchorAt),
        lastRunAt = optNullableLong("lastRunAt"),
        isEnabled = optBoolean("isEnabled", true),
        createdAt = createdAt,
        updatedAt = optLong("updatedAt", createdAt),
        syncState = optString("syncState").toSyncState(),
        isDeleted = optBoolean("isDeleted", false)
    )
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key).takeIf { it.isNotBlank() }
}

private fun JSONObject.optNullableLong(key: String): Long? {
    if (!has(key) || isNull(key)) return null
    return getLong(key)
}

private fun JSONObject.optNullableInt(key: String): Int? {
    if (!has(key) || isNull(key)) return null
    return getInt(key)
}

private fun String.toSyncState(): SyncState {
    return SyncState.entries.firstOrNull { it.name == this } ?: SyncState.LOCAL_ONLY
}

private fun String.toRecurringFrequency(): RecurringFrequency {
    return RecurringFrequency.entries.firstOrNull { it.name == this }
        ?: RecurringFrequency.Monthly
}

private fun String.normalizedKey(): String {
    return trim().lowercase()
}

private fun TransactionEntity.isLogicalDuplicateOf(other: TransactionEntity): Boolean {
    return note.normalizedKey() == other.note.normalizedKey() &&
        amountMinor == other.amountMinor &&
        occurredAt == other.occurredAt &&
        transactionTypeId == other.transactionTypeId &&
        categoryId == other.categoryId &&
        paymentMethodId == other.paymentMethodId
}

private fun JSONObject.putNullable(
    key: String,
    value: Any?
): JSONObject {
    return put(key, value ?: JSONObject.NULL)
}
