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
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.parseDate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val syncCount = java.util.concurrent.atomic.AtomicInteger(0)

    private fun incrementSync() {
        if (syncCount.incrementAndGet() == 1) {
            _isSyncing.value = true
        }
    }

    private fun decrementSync() {
        if (syncCount.decrementAndGet() <= 0) {
            syncCount.set(0)
            _isSyncing.value = false
        }
    }

    private val androidId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private val deviceModel: String by lazy {
        "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    override suspend fun registerCurrentDevice(): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return@withContext Result.failure(Exception("User not logged in"))
        }
        if (!currentUser.isAnonymous && !currentUser.isEmailVerified) {
            android.util.Log.d("Sync", "Skipping device registration: user email is not verified yet.")
            return@withContext Result.success(Unit)
        }
        val uid = currentUser.uid
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

    override suspend fun syncUserProfile(isNewUser: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return@withContext Result.success(Unit)
        }
        if (!currentUser.isAnonymous && !currentUser.isEmailVerified) {
            android.util.Log.d("Sync", "Skipping user profile sync: user email is not verified yet.")
            return@withContext Result.success(Unit)
        }
        val uid = currentUser.uid
        
        try {
            incrementSync()
            
            // Stabilization: For anonymous users, wait a moment for the session to "settle" before Firestore ops
            if (currentUser.isAnonymous) {
                android.util.Log.d("Sync", "Stabilizing anonymous session for uid: $uid...")
                kotlinx.coroutines.delay(500)
            }
            
            if (uid.isBlank()) {
                throw Exception("Invalid UID for sync")
            }

            syncUserProfileInternal(uid, isNewUser)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("Sync", "Sync User Profile failed for uid: $uid", e)
            Result.failure(e)
        } finally {
            decrementSync()
        }
    }

    override suspend fun syncTransactions(): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return@withContext Result.failure(Exception("User not logged in"))
        }
        if (!currentUser.isAnonymous && !currentUser.isEmailVerified) {
            android.util.Log.d("Sync", "Skipping transaction sync: user email is not verified yet.")
            return@withContext Result.success(Unit)
        }
        val uid = currentUser.uid
        try {
            incrementSync()

            // One-time reset of lastSyncTimeMillis to heal any clock-drift issues from the old implementation
            val migrationPrefs = context.getSharedPreferences("sync_migration_prefs", Context.MODE_PRIVATE)
            val resetDone = migrationPrefs.getBoolean("watermark_reset_done_v3", false)
            if (!resetDone) {
                AppSettingsDataStore.updateAppSettings(context) { it.copy(lastSyncTimeMillis = 0L) }
                migrationPrefs.edit().putBoolean("watermark_reset_done_v3", true).apply()
                android.util.Log.i("Sync", "One-time watermark reset triggered for clock-drift correction.")
            }

            val settings = AppSettingsDataStore.getAppSettingsFlow(context).first()
            val lastSync = settings.lastSyncTimeMillis
            val currentSyncStart = System.currentTimeMillis()

            syncUserProfileInternal(uid, isNewUser = false)
            
            var maxUpdatedAt = 0L
            val localMax = pushLocalChanges(uid)
            maxUpdatedAt = java.lang.Math.max(maxUpdatedAt, localMax)

            val remoteMax = pullCloudChanges(uid, lastSync)
            maxUpdatedAt = java.lang.Math.max(maxUpdatedAt, remoteMax)

            val newSyncTime = if (maxUpdatedAt > lastSync) {
                maxUpdatedAt
            } else {
                if (lastSync == 0L) currentSyncStart else lastSync
            }

            AppSettingsDataStore.updateAppSettings(context) { it.copy(lastSyncTimeMillis = newSyncTime) }

            // Purge local soft-deleted synced records older than 30 days
            try {
                val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                database.withTransaction {
                    transactionDao.purgeOldDeleted(threshold)
                    goalDao.purgeOldDeleted(threshold)
                    budgetDao.purgeOldDeleted(threshold)
                    recurringRuleDao.purgeOldDeleted(threshold)
                }
                android.util.Log.i("Sync", "Successfully purged local synced deleted records older than 30 days.")
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Failed to purge local synced deleted records", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            decrementSync()
        }
    }

    override suspend fun forceSyncTransactions(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
        try {
            incrementSync()

            // 1. Reset local lastSyncTimeMillis so pullCloudChanges queries everything from Firestore
            AppSettingsDataStore.updateAppSettings(context) { it.copy(lastSyncTimeMillis = 0L) }

            // 2. Mark all synced records as PENDING_UPLOAD so pushLocalChanges uploads them
            database.withTransaction {
                val db = database.openHelper.writableDatabase
                db.execSQL("UPDATE transactions SET sync_state = 'PENDING_UPLOAD' WHERE sync_state = 'SYNCED'")
                db.execSQL("UPDATE categories SET sync_state = 'PENDING_UPLOAD' WHERE sync_state = 'SYNCED'")
                db.execSQL("UPDATE budgets SET sync_state = 'PENDING_UPLOAD' WHERE sync_state = 'SYNCED'")
                db.execSQL("UPDATE payment_methods SET sync_state = 'PENDING_UPLOAD' WHERE sync_state = 'SYNCED'")
                db.execSQL("UPDATE recurring_rules SET sync_state = 'PENDING_UPLOAD' WHERE sync_state = 'SYNCED'")
                db.execSQL("UPDATE goals SET sync_state = 'PENDING_UPLOAD' WHERE sync_state = 'SYNCED'")
            }

            // 3. Trigger immediate sync
            syncTransactions()
        } catch (e: Exception) {
            android.util.Log.e("Sync", "Force sync failed", e)
            Result.failure(e)
        } finally {
            decrementSync()
        }
    }

    private suspend fun syncUserProfileInternal(uid: String, isNewUser: Boolean) {
        // First check if local profile is expired
        val localProfile = UserProfileDataStore.getUserProfileFlow(context).first()
        val now = System.currentTimeMillis()
        val isLocalExpired = localProfile.accountTier == "PREMIUM" && localProfile.proExpiryTimestamp in 1..<now
        if (isLocalExpired) {
            android.util.Log.d("Sync", "Local Premium has expired. Downgrading locally before sync.")
            val updatedProfile = localProfile.copy(
                accountTier = "FREE",
                updatedAtMillis = now
            )
            UserProfileDataStore.setUserProfile(context, updatedProfile)
            AppSettingsDataStore.updateAppSettings(context) { current ->
                current.copy(
                    userTier = com.mknlabs.expensetracker.models.UserTier.FREE
                )
            }
        }

        pushUserProfile(uid, isNewUser)
        pullUserProfile(uid)
    }

    private suspend fun pushUserProfile(uid: String, isNewUser: Boolean) {
        val userDoc = firestore.collection("users").document(uid)
        val currentUser = firebaseAuth.currentUser
        var localProfile = UserProfileDataStore.getUserProfileFlow(context).first()

        // Optimization: For new users, skip the 'get()' to avoid permission issues with non-existent documents
        var remoteUpdatedAt = 0L
        var remoteAccountTier = ""
        var remoteAccountCreatedOn: String? = null
        var docExists = false
        var remoteProExpiryTimestamp = 0L

        if (!isNewUser) {
            try {
                val snapshot = userDoc.get().await()
                docExists = snapshot.exists()
                if (docExists) {
                    remoteUpdatedAt = snapshot.getLong("profileUpdatedAtMillis") ?: 0L
                    remoteAccountTier = snapshot.getString("accountTier") ?: ""
                    remoteAccountCreatedOn = snapshot.getString("accountCreatedOn") ?: snapshot.getString("AccountCreatedOn")
                    remoteProExpiryTimestamp = snapshot.getLong("proExpiryTimestamp") ?: 0L
                }
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Failed to fetch remote profile for uid: $uid", e)
                // Continue with local data if fetch fails
            }
        }

        // Robust Detection: Treat as new if flag is true OR doc doesn't exist OR critical field missing
        val isFirstTimeInitialization = isNewUser || !docExists || remoteAccountCreatedOn == null

        android.util.Log.d("Sync", "Push Decision - uid: $uid, isInit: $isFirstTimeInitialization, localUp: ${localProfile.updatedAtMillis}, remoteUp: $remoteUpdatedAt")

        val now = System.currentTimeMillis()
        val isRemotePremiumExpired = remoteAccountTier == "PREMIUM" && remoteProExpiryTimestamp in 1..<now

        if (localProfile.accountTier != "PREMIUM" && remoteAccountTier == "PREMIUM" && !isRemotePremiumExpired) {
            android.util.Log.d("Sync", "Skipping push: Remote is active PREMIUM, local is not.")
            return
        }
        if (docExists && localProfile.updatedAtMillis <= remoteUpdatedAt && !isFirstTimeInitialization) {
            android.util.Log.d("Sync", "Skipping push: Cloud is up-to-date or newer.")
            return
        }

        // Initialize local creation timestamp if missing
        if (localProfile.accountCreatedMillis == 0L) {
            val creationTime = System.currentTimeMillis()
            localProfile = localProfile.copy(accountCreatedMillis = creationTime)
            UserProfileDataStore.setUserProfile(context, localProfile)
        }

        val finalFullName = if (localProfile.fullName == defaultUserProfile.fullName) {
            currentUser?.displayName ?: localProfile.fullName
        } else localProfile.fullName

        val finalPhotoUri = if (localProfile.photoUri?.startsWith("file") == true) {
            currentUser?.photoUrl?.toString()
        } else {
            localProfile.photoUri ?: currentUser?.photoUrl?.toString()
        }

        val profileData = mutableMapOf(
            "uid" to uid,
            "fullName" to finalFullName,
            "emailAddress" to localProfile.emailAddress.ifBlank { currentUser?.email ?: "" },
            "phoneNumber" to localProfile.phoneNumber,
            "dateOfBirthOn" to localProfile.dateOfBirthMillis?.takeIf { it != 0L }?.let { formatDate(it, "dd MMMM yyyy") }.orEmpty(),
            "gender" to localProfile.gender,
            "financialGoal" to localProfile.financialGoal,
            "accountTier" to if (localProfile.accountTier == "PREMIUM") "PREMIUM" else "FREE",
            "proExpiryTimestamp" to localProfile.proExpiryTimestamp,
            "photoUri" to finalPhotoUri,
            "isAnonymous" to (currentUser?.isAnonymous ?: false),
            "authProvider" to if (localProfile.authProvider.isNotBlank()) localProfile.authProvider else {
                if (currentUser?.isAnonymous == true) "anonymous" else {
                    currentUser?.providerData?.firstOrNull { it.providerId != "firebase" }?.providerId ?: "email"
                }
            },
            "profileUpdatedAtMillis" to if (localProfile.updatedAtMillis == 0L) System.currentTimeMillis() else localProfile.updatedAtMillis
        )

        // Always push creation date if it's a first-time init or missing in cloud
        if (isFirstTimeInitialization) {
            profileData["accountCreatedOn"] = formatDate(localProfile.accountCreatedMillis, "dd MMMM yyyy")
        }

        try {
            userDoc.set(profileData, SetOptions.merge()).await()
            android.util.Log.i("Sync", "Successfully pushed profile for uid: $uid (isNewUser: $isNewUser)")
        } catch (e: Exception) {
            android.util.Log.e("Sync", "Failed to push profile for uid: $uid", e)
            throw e // Re-throw to trigger worker retry
        }
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
            dateOfBirthMillis = (snapshot.getString("dateOfBirthOn") ?: snapshot.getString("DateOfBirthOn"))?.let { parseDate(it, "dd MMMM yyyy") } ?: localProfile.dateOfBirthMillis,
            gender = snapshot.getString("gender") ?: localProfile.gender,
            financialGoal = snapshot.getString("financialGoal") ?: localProfile.financialGoal,
            accountCreatedMillis = localProfile.accountCreatedMillis, // Don't pull from cloud, keep local
            accountTier = remoteAccountTier,
            proExpiryTimestamp = snapshot.getLong("proExpiryTimestamp") ?: localProfile.proExpiryTimestamp,
            photoUri = snapshot.getString("photoUri") ?: (if (localProfile.photoUri == null) authUser?.photoUrl?.toString() else null) ?: localProfile.photoUri,
            updatedAtMillis = remoteUpdatedAt
        )

        // Industry Standard: Handle automatic downgrade if PREMIUM has expired
        val now = System.currentTimeMillis()
        val isExpired = remoteAccountTier == "PREMIUM" && remoteProfile.proExpiryTimestamp in 1..<now
        
        val finalTier = if (isExpired) "FREE" else remoteAccountTier
        val finalProfile = if (isExpired) remoteProfile.copy(accountTier = "FREE", updatedAtMillis = now) else remoteProfile

        UserProfileDataStore.setUserProfile(context, finalProfile)
        
        val tier = com.mknlabs.expensetracker.models.UserTier.entries.firstOrNull { it.name == finalTier } ?: com.mknlabs.expensetracker.models.UserTier.FREE
        
        // Update tier and automatically enable sync if user is Premium
        AppSettingsDataStore.updateAppSettings(context) { current ->
            current.copy(
                userTier = tier,
                isCloudSyncEnabled = if (tier == com.mknlabs.expensetracker.models.UserTier.PREMIUM) true else current.isCloudSyncEnabled
            )
        }
        
        com.mknlabs.expensetracker.data.local.MonetizationDataStore.updateGlobalAdAccessExpiry(context, finalProfile.proExpiryTimestamp)

        // If we downgraded locally, push the "FREE" status back to Firestore immediately
        if (isExpired) {
            pushUserProfile(uid, isNewUser = false)
        }
    }

    private suspend fun pushLocalChanges(uid: String): Long {
        val userDoc = firestore.collection("users").document(uid)
        
        // Prioritize metadata over transactions for reliable initial setup
        val allTasks = mutableListOf<SyncTask>()
        var maxLocalUpdatedAt = 0L
        
        categoryDao.getUnsynced().forEach { 
            allTasks.add(SyncTask.CategoryTask(it)) 
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        paymentMethodDao.getUnsynced().forEach { 
            allTasks.add(SyncTask.PaymentMethodTask(it)) 
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        budgetDao.getUnsynced().forEach { 
            allTasks.add(SyncTask.BudgetTask(it)) 
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        recurringRuleDao.getUnsynced().forEach { 
            allTasks.add(SyncTask.RecurringRuleTask(it)) 
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        goalDao.getUnsynced().forEach { 
            allTasks.add(SyncTask.GoalTask(it)) 
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        transactionDao.getUnsynced().forEach { 
            allTasks.add(SyncTask.TransactionTask(it)) 
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }

        if (allTasks.isEmpty()) return maxLocalUpdatedAt

        // Process in chunks of 200 for better reliability on slow networks
        allTasks.chunked(200).forEach { chunk ->
            try {
                firestore.runBatch { batch ->
                    chunk.forEach { task ->
                        val collectionName = task.collectionName
                        val docId = task.id
                        val docRef = userDoc.collection(collectionName).document(docId)
                        
                        // Soft delete propagation: always upload the state to Firestore (including isDeleted = true)
                        // so that other devices can pull the soft-deleted state and hide the items locally.
                        val data = task.toCloudMap()
                        batch.set(docRef, data, SetOptions.merge())
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
        
        return maxLocalUpdatedAt
    }

    private suspend fun pullCloudChanges(uid: String, lastSync: Long): Long {
        var maxRemoteUpdatedAt = 0L
        val userDoc = firestore.collection("users").document(uid)

        // 1. Pull Metadata (Categories & Payment Methods)
        // These MUST be pulled first because almost everything else depends on them.
        try {
            val catMax = pullCollection(userDoc, "categories", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity ->
                categoryDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, catMax)
        } catch (e: Exception) { android.util.Log.e("Sync", "Categories pull failed", e) }

        try {
            val pmMax = pullCollection(userDoc, "payment_methods", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity ->
                paymentMethodDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, pmMax)
        } catch (e: Exception) { android.util.Log.e("Sync", "Payment methods pull failed", e) }

        // 2. Relational Data (Handle circular/reverse dependencies)
        // We use a raw PRAGMA to disable foreign keys globally for this connection.
        // This is the most reliable way to handle high-volume sync with circular dependencies.
        try {
            database.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys = OFF")

            // Pull independent items
            val goalMax = pullCollection(userDoc, "goals", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.GoalEntity ->
                goalDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, goalMax)

            // Pull Transactions & Rules (Circular Dependency Zone)
            val txMax = pullCollection(userDoc, "transactions", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity ->
                transactionDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, txMax)

            val rrMax = pullCollection(userDoc, "recurring_rules", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity ->
                recurringRuleDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, rrMax)

            val budgetMax = pullCollection(userDoc, "budgets", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity ->
                budgetDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, budgetMax)
        } catch (e: Exception) {
            android.util.Log.e("Sync", "Relational data pull failed", e)
        } finally {
            database.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys = ON")
        }

        return maxRemoteUpdatedAt
    }

    private suspend inline fun <reified T> pullCollection(
        userDoc: com.google.firebase.firestore.DocumentReference,
        collectionName: String,
        lastSync: Long,
        crossinline onPull: suspend (T) -> Unit
    ): Long {
        // Subtract a safety buffer of 5 minutes (300,000 ms) to account for clock drift
        // and network propagation delays.
        val safeLastSync = if (lastSync == 0L) 0L else (lastSync - 5 * 60 * 1000L).coerceAtLeast(0L)

        android.util.Log.d("Sync", "[$collectionName] Querying with lastSync=$lastSync, safeLastSync=$safeLastSync")

        // "Full Recovery" Mode: If lastSync is 0, fetch ALL documents.
        // Otherwise, fetch only those modified after safeLastSync.
        val query = if (lastSync == 0L) {
            userDoc.collection(collectionName)
        } else {
            userDoc.collection(collectionName)
                .whereGreaterThan("updatedAt", safeLastSync)
        }

        // Bug #4 fix: Each collection get() is individually bounded to 30 seconds.
        // Without this, a single slow Firestore read on a weak network would block the
        // entire syncTransactions() until Firestore's SDK timeout (~60s), then throw,
        // causing the whole sync to retry with even more exponential backoff.
        // Now, a timed-out collection returns 0L (no watermark advance) and lets all
        // other collections continue independently.
        val snapshot = withTimeoutOrNull(30_000L) {
            query.get().await()
        } ?: run {
            android.util.Log.w("Sync", "[$collectionName] Timed out after 30s — skipping this collection, will retry next sync cycle.")
            return 0L
        }

        var deserializedCount = 0
        var savedCount = 0
        var maxDocUpdatedAt = 0L

        android.util.Log.d("Sync", "[$collectionName] Total documents in Cloud: ${snapshot.size()}")

        database.withTransaction {
            snapshot.documents.forEach { doc ->
                try {
                    val docUpdatedAt = doc.getLong("updatedAt") ?: 0L
                    maxDocUpdatedAt = java.lang.Math.max(maxDocUpdatedAt, docUpdatedAt)

                    android.util.Log.d("Sync", "[$collectionName] Found doc: ${doc.id}, updatedAt=$docUpdatedAt")
                    val item = doc.toObject(T::class.java)
                    if (item != null) {
                        deserializedCount++
                        onPull(item)
                        savedCount++
                    } else {
                        android.util.Log.w("Sync", "[$collectionName] Deserialization returned null for ID: ${doc.id}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Sync", "[$collectionName] Failed to process document ID: ${doc.id}", e)
                }
            }
        }

        android.util.Log.i("Sync", "[$collectionName] Completed: Fetched=${snapshot.size()}, Deserialized=$deserializedCount, Saved=$savedCount")
        return maxDocUpdatedAt
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
