package com.mknlabs.expensetracker.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
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
    private val goalDao: com.mknlabs.expensetracker.data.local.room.dao.GoalDao
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
            val userDocRef = firestore.collection("users").document(uid)
            val devicesCollection = userDocRef.collection("devices")
            
            // 1. Get current devices
            val snapshot = devicesCollection.get().await()
            val existingDevices = snapshot.documents.map { it.id }
            
            // 2. If this device is already registered, just update last active
            if (existingDevices.contains(androidId)) {
                devicesCollection.document(androidId).update("lastActiveMillis", System.currentTimeMillis()).await()
                refreshDevices()
                return@withContext Result.success(Unit)
            }
            
            // 3. Check limit from Remote Config
            val maxLimit = configRepository.maxSyncDevices.value
            
            if (existingDevices.size >= maxLimit) {
                return@withContext Result.failure(Exception("Device limit reached ($maxLimit). Please remove another device first."))
            }
            
            // 4. Register new device
            val deviceData = mapOf(
                "modelName" to deviceModel,
                "lastActiveMillis" to System.currentTimeMillis()
            )
            devicesCollection.document(androidId).set(deviceData).await()
            refreshDevices()
            
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    override suspend fun unregisterDevice(deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
        return@withContext try {
            firestore.collection("users").document(uid)
                .collection("devices").document(deviceId)
                .delete().await()
            refreshDevices()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshDevices(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
        return@withContext try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("devices").get().await()
            
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

        return@withContext try {
            syncUserProfileInternal(uid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTransactions(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(Exception("User not logged in"))
        
        return@withContext try {
            val settings = AppSettingsDataStore.getAppSettingsFlow(context).first()
            val lastSync = settings.lastSyncTimeMillis
            val currentSyncStart = System.currentTimeMillis()

            syncUserProfileInternal(uid)

            // 1. PUSH (Local -> Cloud)
            pushLocalChanges(uid)

            // 2. PULL (Cloud -> Local)
            pullCloudChanges(uid, lastSync)

            // 3. Update Last Sync Time
            AppSettingsDataStore.updateAppSettings(context) { it.copy(lastSyncTimeMillis = currentSyncStart) }
            
            Result.success(Unit)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private suspend fun syncUserProfileInternal(uid: String) {
        pushUserProfile(uid)
        pullUserProfile(uid)
    }

    private suspend fun pushUserProfile(uid: String) {
        val userDoc = firestore.collection("users").document(uid)
        val localProfile = UserProfileDataStore.getUserProfileFlow(context).first()
        val currentUser = firebaseAuth.currentUser
        val remoteUpdatedAt = userDoc.get().await().getLong("profileUpdatedAtMillis") ?: 0L

        if (localProfile.updatedAtMillis <= remoteUpdatedAt) {
            return
        }

        val profileData = mapOf(
            "fullName" to localProfile.fullName,
            "emailAddress" to localProfile.emailAddress.ifBlank { currentUser?.email ?: "" },
            "phoneNumber" to localProfile.phoneNumber,
            "dateOfBirthMillis" to (localProfile.dateOfBirthMillis ?: 0L),
            "gender" to localProfile.gender,
            "memberSinceLabel" to localProfile.memberSinceLabel,
            "accountTier" to localProfile.accountTier,
            "photoUri" to localProfile.photoUri,
            "isAnonymous" to (currentUser?.isAnonymous ?: false),
            "profileUpdatedAtMillis" to localProfile.updatedAtMillis
        )

        userDoc.set(profileData, SetOptions.merge()).await()
    }

    private suspend fun pullUserProfile(uid: String) {
        val userDoc = firestore.collection("users").document(uid)
        val snapshot = userDoc.get().await()
        if (!snapshot.exists()) return

        val remoteUpdatedAt = snapshot.getLong("profileUpdatedAtMillis") ?: 0L
        val localProfile = UserProfileDataStore.getUserProfileFlow(context).first()
        if (remoteUpdatedAt <= localProfile.updatedAtMillis) return

        val authUser = firebaseAuth.currentUser
        val remoteAccountTier = snapshot.getString("accountTier") ?: localProfile.accountTier
        val remoteProfile = UserProfile(
            fullName = snapshot.getString("fullName") ?: localProfile.fullName,
            emailAddress = snapshot.getString("emailAddress")
                ?: authUser?.email
                ?: localProfile.emailAddress,
            phoneNumber = snapshot.getString("phoneNumber") ?: localProfile.phoneNumber,
            dateOfBirthMillis = snapshot.getLong("dateOfBirthMillis") ?: localProfile.dateOfBirthMillis,
            gender = snapshot.getString("gender") ?: localProfile.gender,
            memberSinceLabel = snapshot.getString("memberSinceLabel") ?: localProfile.memberSinceLabel,
            accountTier = remoteAccountTier,
            photoUri = snapshot.getString("photoUri") ?: localProfile.photoUri,
            updatedAtMillis = remoteUpdatedAt
        )

        UserProfileDataStore.setUserProfile(context, remoteProfile)

        // Sync Tier to AppSettings
        val tier = com.mknlabs.expensetracker.models.UserTier.entries.firstOrNull { it.name == remoteAccountTier }
            ?: com.mknlabs.expensetracker.models.UserTier.FREE
        AppSettingsDataStore.updateUserTier(context, tier)
    }

    private suspend fun pushLocalChanges(uid: String) {
        val userDoc = firestore.collection("users").document(uid)
        
        // Push Transactions
        transactionDao.getUnsynced().forEach { entity ->
            if (entity.isDeleted) {
                userDoc.collection("transactions").document(entity.id).delete().await()
            } else {
                userDoc.collection("transactions").document(entity.id).set(entity).await()
            }
            transactionDao.updateSyncState(entity.id, SyncState.SYNCED.name)
        }

        // Push Categories
        categoryDao.getUnsynced().forEach { entity ->
            if (entity.isDeleted) {
                userDoc.collection("categories").document(entity.id.toString()).delete().await()
            } else {
                userDoc.collection("categories").document(entity.id.toString()).set(entity).await()
            }
            categoryDao.updateSyncState(entity.id, SyncState.SYNCED.name)
        }

        // Push Budgets
        budgetDao.getUnsynced().forEach { entity ->
            if (entity.isDeleted) {
                userDoc.collection("budgets").document(entity.id).delete().await()
            } else {
                userDoc.collection("budgets").document(entity.id).set(entity).await()
            }
            budgetDao.updateSyncState(entity.id, SyncState.SYNCED.name)
        }

        // Push Payment Methods
        paymentMethodDao.getUnsynced().forEach { entity ->
            if (entity.isDeleted) {
                userDoc.collection("payment_methods").document(entity.id.toString()).delete().await()
            } else {
                userDoc.collection("payment_methods").document(entity.id.toString()).set(entity).await()
            }
            paymentMethodDao.updateSyncState(entity.id, SyncState.SYNCED.name)
        }

        // Push Recurring Rules
        recurringRuleDao.getUnsynced().forEach { entity ->
            if (entity.isDeleted) {
                userDoc.collection("recurring_rules").document(entity.id).delete().await()
            } else {
                userDoc.collection("recurring_rules").document(entity.id).set(entity).await()
            }
            recurringRuleDao.updateSyncState(entity.id, SyncState.SYNCED.name)
        }

        // Push Goals
        goalDao.getUnsynced().forEach { entity ->
            if (entity.isDeleted) {
                userDoc.collection("goals").document(entity.id).delete().await()
            } else {
                userDoc.collection("goals").document(entity.id).set(entity).await()
            }
            goalDao.updateSyncState(entity.id, SyncState.SYNCED.name)
        }
    }

    private suspend fun pullCloudChanges(uid: String, lastSync: Long) {
        val userDoc = firestore.collection("users").document(uid)

        // Pull Transactions
        val transSnapshot = userDoc.collection("transactions")
            .whereGreaterThan("updatedAt", lastSync)
            .get().await()
        
        transSnapshot.documents.forEach { doc ->
            val cloudItem = doc.toObject(com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity::class.java)
            cloudItem?.let {
                val localItem = transactionDao.getById(it.id)
                if (localItem == null || it.updatedAt > localItem.updatedAt) {
                    transactionDao.upsert(it.copy(syncState = SyncState.SYNCED))
                }
            }
        }

        // Pull Categories
        val catSnapshot = userDoc.collection("categories")
            .whereGreaterThan("updatedAt", lastSync)
            .get().await()
        
        catSnapshot.documents.forEach { doc ->
            val cloudItem = doc.toObject(com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity::class.java)
            cloudItem?.let {
                val localItem = categoryDao.getById(it.id)
                if (localItem == null || it.updatedAt > localItem.updatedAt) {
                    categoryDao.upsert(it.copy(syncState = SyncState.SYNCED))
                }
            }
        }

        // Pull Budgets
        val budgetSnapshot = userDoc.collection("budgets")
            .whereGreaterThan("updatedAt", lastSync)
            .get().await()
        
        budgetSnapshot.documents.forEach { doc ->
            val cloudItem = doc.toObject(com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity::class.java)
            cloudItem?.let {
                val localItem = budgetDao.getById(it.id)
                if (localItem == null || it.updatedAt > localItem.updatedAt) {
                    budgetDao.upsert(it.copy(syncState = SyncState.SYNCED))
                }
            }
        }

        // Pull Payment Methods
        val pmSnapshot = userDoc.collection("payment_methods")
            .whereGreaterThan("updatedAt", lastSync)
            .get().await()
        
        pmSnapshot.documents.forEach { doc ->
            val cloudItem = doc.toObject(com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity::class.java)
            cloudItem?.let {
                val localItem = paymentMethodDao.getActivePaymentMethods().find { pm -> pm.id == it.id }
                if (localItem == null || it.updatedAt > localItem.updatedAt) {
                    paymentMethodDao.upsert(it.copy(syncState = SyncState.SYNCED))
                }
            }
        }

        // Pull Recurring Rules
        val rrSnapshot = userDoc.collection("recurring_rules")
            .whereGreaterThan("updatedAt", lastSync)
            .get().await()
        
        rrSnapshot.documents.forEach { doc ->
            val cloudItem = doc.toObject(com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity::class.java)
            cloudItem?.let {
                val localItem = recurringRuleDao.getById(it.id)
                if (localItem == null || it.updatedAt > localItem.updatedAt) {
                    recurringRuleDao.upsert(it.copy(syncState = SyncState.SYNCED))
                }
            }
        }

        // Pull Goals
        val goalSnapshot = userDoc.collection("goals")
            .whereGreaterThan("updatedAt", lastSync)
            .get().await()
        
        goalSnapshot.documents.forEach { doc ->
            val cloudItem = doc.toObject(com.mknlabs.expensetracker.data.local.room.entities.GoalEntity::class.java)
            cloudItem?.let {
                val localItem = goalDao.getById(it.id)
                if (localItem == null || it.updatedAt > localItem.updatedAt) {
                    goalDao.upsert(it.copy(syncState = SyncState.SYNCED))
                }
            }
        }
    }
}
