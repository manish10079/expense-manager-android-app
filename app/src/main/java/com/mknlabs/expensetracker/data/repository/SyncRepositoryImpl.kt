package com.mknlabs.expensetracker.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import androidx.room.withTransaction
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.BudgetDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.data.local.room.dao.RecurringRuleDao
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import com.mknlabs.expensetracker.domain.repository.RegisteredDevice
import com.mknlabs.expensetracker.domain.repository.SyncRepository
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val configRepository: ConfigurationRepository,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val recurringRuleDao: RecurringRuleDao,
    private val goalDao: com.mknlabs.expensetracker.data.local.room.dao.GoalDao,
    private val database: com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
) : SyncRepository {

    private val _registeredDevices = MutableStateFlow<List<RegisteredDevice>>(emptyList())
    override val registeredDevices: StateFlow<List<RegisteredDevice>> = _registeredDevices.asStateFlow()

    private val _isSyncEnabled = MutableStateFlow(false)
    override val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()

    private val androidId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private val deviceModel: String by lazy {
        "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    override suspend fun registerCurrentDevice(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
        try {
            val devicesCollection = firestore.collection("users").document(uid).collection("devices")
            val snapshot = devicesCollection.get().await()
            val existingDevices = snapshot.documents.map { it.id }
            
            if (existingDevices.contains(androidId)) {
                devicesCollection.document(androidId).update("lastActiveMillis", System.currentTimeMillis()).await()
                refreshDevices()
                return@withContext Result.success(Unit)
            }
            
            val maxLimit = configRepository.maxSyncDevices.value
            if (existingDevices.size >= maxLimit) {
                return@withContext Result.failure(Exception("Device limit reached ($maxLimit). Please remove another device first."))
            }
            
            val deviceData = mapOf("modelName" to deviceModel, "lastActiveMillis" to System.currentTimeMillis())
            devicesCollection.document(androidId).set(deviceData).await()
            refreshDevices()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unregisterDevice(deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
        try {
            firestore.collection("users").document(uid).collection("devices").document(deviceId).delete().await()
            refreshDevices()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshDevices(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
        try {
            val snapshot = firestore.collection("users").document(uid).collection("devices").get().await()
            val devices = snapshot.documents.map { doc ->
                RegisteredDevice(
                    id = doc.id,
                    modelName = doc.getString("modelName") ?: "Unknown Device",
                    lastActiveMillis = doc.getLong("lastActiveMillis") ?: 0L,
                    isCurrentDevice = doc.id == androidId
                )
            }
            _registeredDevices.value = devices
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncUserProfile(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.success(Unit)
        try {
            syncUserProfileInternal(uid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTransactions(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
        try {
            val settings = AppSettingsDataStore.getAppSettingsFlow(context).first()
            val lastSync = settings.lastSyncTimeMillis
            val currentSyncStart = System.currentTimeMillis()

            syncUserProfileInternal(uid)
            pushLocalChanges(uid)
            pullCloudChanges(uid, lastSync)

            AppSettingsDataStore.updateAppSettings(context) { it.copy(lastSyncTimeMillis = currentSyncStart) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncUserProfileInternal(uid: String) {
        pushUserProfile(uid)
        pullUserProfile(uid)
    }

    private suspend fun pushUserProfile(uid: String) {
        val userDoc = firestore.collection("users").document(uid)
        val snapshot = userDoc.get().await()
        val remoteUpdatedAt = snapshot.getLong("profileUpdatedAtMillis") ?: 0L
        val remoteAccountTier = snapshot.getString("accountTier") ?: ""
        val localProfile = UserProfileDataStore.getUserProfileFlow(context).first()
        val currentUser = firebaseAuth.currentUser

        if (localProfile.accountTier != "PREMIUM" && remoteAccountTier == "PREMIUM") return
        if (snapshot.exists() && localProfile.updatedAtMillis <= remoteUpdatedAt) return

        val finalFullName = if (localProfile.fullName == defaultUserProfile.fullName) {
            currentUser?.displayName ?: localProfile.fullName
        } else localProfile.fullName

        val finalPhotoUri = if (localProfile.photoUri?.startsWith("file") == true) {
            currentUser?.photoUrl?.toString()
        } else {
            localProfile.photoUri ?: currentUser?.photoUrl?.toString()
        }

        val profileData = mapOf(
            "fullName" to finalFullName,
            "emailAddress" to localProfile.emailAddress.ifBlank { currentUser?.email ?: "" },
            "phoneNumber" to localProfile.phoneNumber,
            "dateOfBirthMillis" to (localProfile.dateOfBirthMillis ?: 0L),
            "gender" to localProfile.gender,
            "financialGoal" to localProfile.financialGoal,
            "memberSinceLabel" to localProfile.memberSinceLabel,
            "accountTier" to localProfile.accountTier,
            "proExpiryTimestamp" to localProfile.proExpiryTimestamp,
            "photoUri" to finalPhotoUri,
            "isAnonymous" to (currentUser?.isAnonymous ?: false),
            "profileUpdatedAtMillis" to if (localProfile.updatedAtMillis == 0L) System.currentTimeMillis() else localProfile.updatedAtMillis
        )
        userDoc.set(profileData, SetOptions.merge()).await()
    }

    private suspend fun pullUserProfile(uid: String) {
        val userDoc = firestore.collection("users").document(uid)
        val snapshot = userDoc.get().await()
        val authUser = firebaseAuth.currentUser
        val localProfile = UserProfileDataStore.getUserProfileFlow(context).first()

        if (!snapshot.exists()) {
            if (localProfile.fullName == defaultUserProfile.fullName || localProfile.photoUri == null) {
                val hydratedProfile = localProfile.copy(
                    fullName = authUser?.displayName ?: localProfile.fullName,
                    emailAddress = authUser?.email ?: localProfile.emailAddress,
                    photoUri = authUser?.photoUrl?.toString() ?: localProfile.photoUri,
                    updatedAtMillis = System.currentTimeMillis()
                )
                UserProfileDataStore.setUserProfile(context, hydratedProfile)
            }
            return
        }

        val remoteUpdatedAt = snapshot.getLong("profileUpdatedAtMillis") ?: 0L
        val remoteAccountTier = snapshot.getString("accountTier") ?: ""
        val shouldPull = remoteUpdatedAt > localProfile.updatedAtMillis || 
                         (localProfile.accountTier != "PREMIUM" && remoteAccountTier == "PREMIUM") ||
                         localProfile.accountTier.isBlank()

        if (!shouldPull) return

        val remoteProfile = UserProfile(
            fullName = snapshot.getString("fullName") ?: (if (localProfile.fullName == defaultUserProfile.fullName) authUser?.displayName else null) ?: localProfile.fullName,
            emailAddress = snapshot.getString("emailAddress") ?: authUser?.email ?: localProfile.emailAddress,
            phoneNumber = snapshot.getString("phoneNumber") ?: localProfile.phoneNumber,
            dateOfBirthMillis = snapshot.getLong("dateOfBirthMillis") ?: localProfile.dateOfBirthMillis,
            gender = snapshot.getString("gender") ?: localProfile.gender,
            financialGoal = snapshot.getString("financialGoal") ?: localProfile.financialGoal,
            memberSinceLabel = snapshot.getString("memberSinceLabel") ?: localProfile.memberSinceLabel,
            accountTier = remoteAccountTier,
            proExpiryTimestamp = snapshot.getLong("proExpiryTimestamp") ?: localProfile.proExpiryTimestamp,
            photoUri = snapshot.getString("photoUri") ?: (if (localProfile.photoUri == null) authUser?.photoUrl?.toString() else null) ?: localProfile.photoUri,
            updatedAtMillis = remoteUpdatedAt
        )

        UserProfileDataStore.setUserProfile(context, remoteProfile)
        val tier = com.mknlabs.expensetracker.models.UserTier.entries.firstOrNull { it.name == remoteAccountTier } ?: com.mknlabs.expensetracker.models.UserTier.FREE
        AppSettingsDataStore.updateUserTier(context, tier)
        com.mknlabs.expensetracker.data.local.MonetizationDataStore.updateGlobalAdAccessExpiry(context, remoteProfile.proExpiryTimestamp)
    }

    private suspend fun pushLocalChanges(uid: String) {
        val userDoc = firestore.collection("users").document(uid)
        
        // Prioritize metadata over transactions for reliable initial setup
        val allTasks = mutableListOf<SyncTask>()
        
        categoryDao.getUnsynced().forEach { allTasks.add(SyncTask.CategoryTask(it)) }
        paymentMethodDao.getUnsynced().forEach { allTasks.add(SyncTask.PaymentMethodTask(it)) }
        budgetDao.getUnsynced().forEach { allTasks.add(SyncTask.BudgetTask(it)) }
        recurringRuleDao.getUnsynced().forEach { allTasks.add(SyncTask.RecurringRuleTask(it)) }
        goalDao.getUnsynced().forEach { allTasks.add(SyncTask.GoalTask(it)) }
        transactionDao.getUnsynced().forEach { allTasks.add(SyncTask.TransactionTask(it)) }

        if (allTasks.isEmpty()) return

        // Process in chunks of 200 for better reliability on slow networks
        allTasks.chunked(200).forEach { chunk ->
            try {
                firestore.runBatch { batch ->
                    chunk.forEach { task ->
                        val collectionName = task.collectionName
                        val docId = task.id
                        val docRef = userDoc.collection(collectionName).document(docId)
                        
                        if (task.isDeleted) {
                            batch.delete(docRef)
                        } else {
                            // Convert to Map and remove local sync metadata
                            val data = task.toCloudMap()
                            batch.set(docRef, data, SetOptions.merge())
                        }
                    }
                }.await()

                // Mark as SYNCED locally only after batch success
                val txIds = chunk.filterIsInstance<SyncTask.TransactionTask>().map { it.entity.id }
                if (txIds.isNotEmpty()) transactionDao.updateSyncStates(txIds, SyncState.SYNCED.name)

                val catIds = chunk.filterIsInstance<SyncTask.CategoryTask>().map { it.entity.id }
                if (catIds.isNotEmpty()) categoryDao.updateSyncStates(catIds, SyncState.SYNCED.name)

                val budgetIds = chunk.filterIsInstance<SyncTask.BudgetTask>().map { it.entity.id }
                if (budgetIds.isNotEmpty()) budgetDao.updateSyncStates(budgetIds, SyncState.SYNCED.name)

                val pmIds = chunk.filterIsInstance<SyncTask.PaymentMethodTask>().map { it.entity.id }
                if (pmIds.isNotEmpty()) paymentMethodDao.updateSyncStates(pmIds, SyncState.SYNCED.name)

                val rrIds = chunk.filterIsInstance<SyncTask.RecurringRuleTask>().map { it.entity.id }
                if (rrIds.isNotEmpty()) recurringRuleDao.updateSyncStates(rrIds, SyncState.SYNCED.name)

                val goalIds = chunk.filterIsInstance<SyncTask.GoalTask>().map { it.entity.id }
                if (goalIds.isNotEmpty()) goalDao.updateSyncStates(goalIds, SyncState.SYNCED.name)

            } catch (e: Exception) {
                // If a batch fails, we skip it and continue to the next one to ensure other data is synced
                android.util.Log.e("Sync", "Batch failed", e)
            }
        }
    }

    private suspend fun pullCloudChanges(uid: String, lastSync: Long) {
        val userDoc = firestore.collection("users").document(uid)

        // Optimized Pull logic with strict sequencing: Metadata -> Structural -> Data
        // 1. Metadata (Required for transactions/budgets)
        pullCollection(userDoc, "categories", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity ->
            val local = categoryDao.getById(cloudItem.id)
            if (local == null || cloudItem.updatedAt > local.updatedAt) {
                categoryDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
        }

        pullCollection(userDoc, "payment_methods", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity ->
            val local = paymentMethodDao.getById(cloudItem.id)
            if (local == null || cloudItem.updatedAt > local.updatedAt) {
                paymentMethodDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
        }

        // 2. Structural/Rules
        pullCollection(userDoc, "budgets", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity ->
            val local = budgetDao.getById(cloudItem.id)
            if (local == null || cloudItem.updatedAt > local.updatedAt) {
                budgetDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
        }

        pullCollection(userDoc, "recurring_rules", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity ->
            val local = recurringRuleDao.getById(cloudItem.id)
            if (local == null || cloudItem.updatedAt > local.updatedAt) {
                recurringRuleDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
        }

        // 3. Independent Entities
        pullCollection(userDoc, "goals", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.GoalEntity ->
            val local = goalDao.getById(cloudItem.id)
            if (local == null || cloudItem.updatedAt > local.updatedAt) {
                goalDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
        }

        // 4. Heavy Data (Dependencies now met)
        pullCollection(userDoc, "transactions", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity ->
            val local = transactionDao.getById(cloudItem.id)
            if (local == null || cloudItem.updatedAt > local.updatedAt) {
                transactionDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
        }
    }

    private suspend inline fun <reified T> pullCollection(
        userDoc: com.google.firebase.firestore.DocumentReference,
        collectionName: String,
        lastSync: Long,
        crossinline onPull: suspend (T) -> Unit
    ) {
        // "Full Recovery" Mode: If lastSync is 0, fetch ALL documents. 
        // Otherwise, fetch only those modified after lastSync.
        val query = if (lastSync == 0L) {
            userDoc.collection(collectionName)
        } else {
            userDoc.collection(collectionName)
                .whereGreaterThan("updatedAt", lastSync)
        }
        
        val snapshot = query.get().await()
        
        snapshot.documents.forEach { doc ->
            doc.toObject(T::class.java)?.let { onPull(it) }
        }
    }

    private sealed class SyncTask {
        abstract val id: String
        abstract val collectionName: String
        abstract val isDeleted: Boolean
        abstract fun toCloudMap(): Map<String, Any?>

        data class TransactionTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity) : SyncTask() {
            override val id = entity.id
            override val collectionName = "transactions"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "note" to entity.note, "amountMinor" to entity.amountMinor,
                "occurredAt" to entity.occurredAt, "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt,
                "transactionTypeId" to entity.transactionTypeId, "categoryId" to entity.categoryId,
                "paymentMethodId" to entity.paymentMethodId, "isDeleted" to entity.isDeleted,
                "contentHash" to entity.contentHash, "sourceRecurringRuleId" to entity.sourceRecurringRuleId
            )
        }
        data class CategoryTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity) : SyncTask() {
            override val id = entity.id.toString()
            override val collectionName = "categories"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "name" to entity.name, "iconKey" to entity.iconKey,
                "transactionTypeId" to entity.transactionTypeId, "isSystem" to entity.isSystem,
                "sortOrder" to entity.sortOrder, "isDeleted" to entity.isDeleted,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt
            )
        }
        data class BudgetTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity) : SyncTask() {
            override val id = entity.id
            override val collectionName = "budgets"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "categoryId" to entity.categoryId, "monthStart" to entity.monthStart,
                "limitMinor" to entity.limitMinor, "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt,
                "editCount" to entity.editCount, "isDeleted" to entity.isDeleted
            )
        }
        data class PaymentMethodTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity) : SyncTask() {
            override val id = entity.id.toString()
            override val collectionName = "payment_methods"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "name" to entity.name, "iconKey" to entity.iconKey,
                "isSystem" to entity.isSystem, "sortOrder" to entity.sortOrder, "isDeleted" to entity.isDeleted,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt
            )
        }
        data class RecurringRuleTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity) : SyncTask() {
            override val id = entity.id
            override val collectionName = "recurring_rules"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "transactionId" to entity.transactionId, "frequency" to entity.frequency,
                "repeatCount" to entity.repeatCount, "isEnabled" to entity.isEnabled, "intervalCount" to entity.intervalCount,
                "remainingCount" to entity.remainingCount, "anchorAt" to entity.anchorAt, "nextRunAt" to entity.nextRunAt,
                "lastRunAt" to entity.lastRunAt, "lastNotifiedOccurrenceAt" to entity.lastNotifiedOccurrenceAt,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt, "isDeleted" to entity.isDeleted
            )
        }
        data class GoalTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.GoalEntity) : SyncTask() {
            override val id = entity.id
            override val collectionName = "goals"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "name" to entity.name, "targetAmountMinor" to entity.targetAmountMinor,
                "currentAmountMinor" to entity.currentAmountMinor, "deadlineAt" to entity.deadlineAt,
                "iconKey" to entity.iconKey, "colorHex" to entity.colorHex, "isCompleted" to entity.isCompleted,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt, "isDeleted" to entity.isDeleted
            )
        }
    }
}
