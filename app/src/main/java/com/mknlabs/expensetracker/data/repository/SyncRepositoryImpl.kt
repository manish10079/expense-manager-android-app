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
import com.mknlabs.expensetracker.models.CURRENT_TERMS_VERSION
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.monetization.EntitlementResolver
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private val installmentOccurrenceDao: com.mknlabs.expensetracker.data.local.room.dao.InstallmentOccurrenceDao,
    private val goalDao: com.mknlabs.expensetracker.data.local.room.dao.GoalDao,
    private val favoriteTransactionDao: com.mknlabs.expensetracker.data.local.room.dao.FavoriteTransactionDao,
    private val tagDao: com.mknlabs.expensetracker.data.local.room.dao.TagDao,
    private val fundDao: com.mknlabs.expensetracker.data.local.room.dao.FundDao,
    private val database: com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
) : SyncRepository {

    private val _registeredDevices = MutableStateFlow<List<RegisteredDevice>>(emptyList())
    override val registeredDevices: StateFlow<List<RegisteredDevice>> = _registeredDevices.asStateFlow()

    private val _isSyncEnabled = MutableStateFlow(false)
    override val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimeMillis = MutableStateFlow(0L)
    override val lastSyncTimeMillis: StateFlow<Long> = _lastSyncTimeMillis.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            AppSettingsDataStore.getAppSettingsFlow(context).collect { settings ->
                _lastSyncTimeMillis.value = settings.lastSyncTimeMillis
            }
        }
    }

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
        val isGoogle = currentUser.providerData.any { it.providerId == "google.com" }
        if (!currentUser.isAnonymous && !currentUser.isEmailVerified && !isGoogle) {
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
        val isGoogle = currentUser.providerData.any { it.providerId == "google.com" }
        if (!currentUser.isAnonymous && !currentUser.isEmailVerified && !isGoogle) {
            android.util.Log.d("Sync", "Skipping user profile sync: user email is not verified yet.")
            return@withContext Result.success(Unit)
        }
        val uid = currentUser.uid
        
        try {
            incrementSync()
            
            // Stabilization: For anonymous users, wait a moment for the session to "settle" before Firestore ops
            if (currentUser.isAnonymous) {
                android.util.Log.d("Sync", "Stabilizing anonymous session...")
                kotlinx.coroutines.delay(500)
            }
            
            if (uid.isBlank()) {
                throw Exception("Invalid UID for sync")
            }

            syncUserProfileInternal(uid, isNewUser)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("Sync", "Sync User Profile failed", e)
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
        val isGoogle = currentUser.providerData.any { it.providerId == "google.com" }
        if (!currentUser.isAnonymous && !currentUser.isEmailVerified && !isGoogle) {
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
                    installmentOccurrenceDao.purgeOldDeleted(threshold)
                    favoriteTransactionDao.purgeOldDeleted(threshold)
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

    override suspend fun fetchUserProfileFromCloud(uid: String): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            if (!snapshot.exists()) return@withContext null
            val remoteAccountCreatedOn = snapshot.getString("accountCreatedOn")
                ?: snapshot.getString("AccountCreatedOn")
                ?: snapshot.getString("createdOn")
            val remoteCreatedAt = snapshot.getLong("createdAt")
                ?: snapshot.getLong("createdOn")
                ?: remoteAccountCreatedOn?.let { parseDate(it, "dd MMMM yyyy") }
                ?: 0L
            val remoteTermsAcceptedAt = snapshot.getLong("termsAcceptedAt") ?: 0L
            val remoteTermsVersion = snapshot.getString("termsVersion").orEmpty()
            val remoteDob = (snapshot.getString("dateOfBirthOn") ?: snapshot.getString("DateOfBirthOn"))
                ?.let { parseDate(it, "dd MMMM yyyy") }

            UserProfile(
                fullName = snapshot.getString("fullName").orEmpty(),
                emailAddress = snapshot.getString("emailAddress").orEmpty(),
                phoneNumber = snapshot.getString("phoneNumber").orEmpty(),
                dateOfBirthMillis = remoteDob,
                gender = snapshot.getString("gender").orEmpty(),
                financialGoal = snapshot.getString("financialGoal").orEmpty(),
                accountCreatedMillis = remoteCreatedAt,
                accountTier = snapshot.getString("accountTier").orEmpty(),
                photoUri = snapshot.getString("photoUri"),
                proExpiryTimestamp = snapshot.getLong("proExpiryTimestamp") ?: 0L,
                isSubscription = snapshot.getBoolean("isSubscription") ?: false,
                updatedAtMillis = snapshot.getLong("profileUpdatedAtMillis") ?: snapshot.getLong("updatedAt") ?: 0L,
                termsAcceptedAt = remoteTermsAcceptedAt,
                termsVersion = remoteTermsVersion
            )
        } catch (e: Exception) {
            android.util.Log.e("Sync", "fetchUserProfileFromCloud failed", e)
            null
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
                db.execSQL("UPDATE installment_occurrences SET sync_state = 'PENDING_UPLOAD' WHERE sync_state = 'SYNCED'")
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
        val isLocalExpired = EntitlementResolver.isProTier(localProfile.accountTier) && localProfile.proExpiryTimestamp in 1..<now
        if (isLocalExpired) {
            android.util.Log.d("Sync", "Local Premium has expired. Downgrading locally before sync.")
            val updatedProfile = localProfile.copy(
                accountTier = EntitlementResolver.TIER_FREE,
                updatedAtMillis = now
            )
            UserProfileDataStore.setUserProfile(context, updatedProfile)
            AppSettingsDataStore.updateAppSettings(context) { current ->
                current.copy(
                    userTier = com.mknlabs.expensetracker.models.UserTier.FREE
                )
            }
        }

        // Login/sync hydration: fill any blank name/gender/phone/DOB fields from
        // Firestore so a fresh install or re-login restores the cloud profile into
        // DataStore (and therefore the Edit Profile screen). Runs BEFORE the push
        // so a freshly-hydrated local profile can never overwrite cloud values with
        // blanks. Cloud values only fill gaps — local edits are never clobbered.
        hydrateProfileFromCloud(uid)

        pushUserProfile(uid, isNewUser)
        pullUserProfile(uid)
    }

    /**
     * Fill-blank profile hydration from Firestore. Cloud values are used ONLY to
     * fill local blank fields (name, gender, phone number, date of birth) — never
     * to override local edits. No-op when the local profile is already complete or
     * the cloud document is missing/unreadable.
     */
    private suspend fun hydrateProfileFromCloud(uid: String) {
        val localProfile = UserProfileDataStore.getUserProfileFlow(context).first()
        val needsName = localProfile.fullName.isBlank() || localProfile.fullName == defaultUserProfile.fullName
        val needsGender = localProfile.gender.isBlank()
        val needsPhone = localProfile.phoneNumber.isBlank()
        val needsDob = localProfile.dateOfBirthMillis == null || localProfile.dateOfBirthMillis == 0L
        val needsGoal = localProfile.financialGoal.isBlank()
        val needsCreated = localProfile.accountCreatedMillis == 0L
        val needsTerms = localProfile.termsAcceptedAt == 0L || localProfile.termsVersion.isBlank()
        if (!needsName && !needsGender && !needsPhone && !needsDob && !needsGoal && !needsCreated && !needsTerms) return

        val snapshot = try {
            firestore.collection("users").document(uid).get().await()
        } catch (e: Exception) {
            android.util.Log.e("Sync", "hydrateProfileFromCloud failed", e)
            return
        }
        if (!snapshot.exists()) return

        val cloudName = snapshot.getString("fullName").orEmpty()
        val cloudGender = snapshot.getString("gender").orEmpty()
        val cloudPhone = snapshot.getString("phoneNumber").orEmpty()
        val cloudDob = (snapshot.getString("dateOfBirthOn") ?: snapshot.getString("DateOfBirthOn"))
            ?.let { parseDate(it, "dd MMMM yyyy") }
        val cloudGoal = snapshot.getString("financialGoal").orEmpty()
        val cloudAccountCreatedOn = snapshot.getString("accountCreatedOn")
            ?: snapshot.getString("AccountCreatedOn")
            ?: snapshot.getString("createdOn")
        val cloudCreatedAt = snapshot.getLong("createdAt")
            ?: snapshot.getLong("createdOn")
            ?: cloudAccountCreatedOn?.let { parseDate(it, "dd MMMM yyyy") }
            ?: 0L
        val cloudTermsAcceptedAt = snapshot.getLong("termsAcceptedAt") ?: 0L
        val cloudTermsVersion = snapshot.getString("termsVersion").orEmpty()

        val hydrated = localProfile.copy(
            fullName = if (needsName && cloudName.isNotBlank()) cloudName else localProfile.fullName,
            gender = if (needsGender && cloudGender.isNotBlank()) cloudGender else localProfile.gender,
            phoneNumber = if (needsPhone && cloudPhone.isNotBlank()) cloudPhone else localProfile.phoneNumber,
            dateOfBirthMillis = if (needsDob && cloudDob != null) cloudDob else localProfile.dateOfBirthMillis,
            financialGoal = if (needsGoal && cloudGoal.isNotBlank()) cloudGoal else localProfile.financialGoal,
            accountCreatedMillis = if (needsCreated && cloudCreatedAt != 0L) cloudCreatedAt else localProfile.accountCreatedMillis,
            termsAcceptedAt = if (localProfile.termsAcceptedAt == 0L && cloudTermsAcceptedAt != 0L) cloudTermsAcceptedAt else localProfile.termsAcceptedAt,
            termsVersion = if (localProfile.termsVersion.isBlank() && cloudTermsVersion.isNotBlank()) cloudTermsVersion else localProfile.termsVersion
        )
        if (hydrated != localProfile) {
            UserProfileDataStore.setUserProfile(context, hydrated)
        }
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
        var remoteFullName = ""
        var remoteGender = ""
        var remotePhoneNumber = ""
        var remoteDateOfBirthOn = ""
        var remoteFinancialGoal = ""
        var remoteTermsAcceptedAt = 0L
        var remoteTermsVersion = ""
        var remoteCreatedAt = 0L

        if (!isNewUser) {
            try {
                val snapshot = userDoc.get().await()
                docExists = snapshot.exists()
                if (docExists) {
                    remoteUpdatedAt = snapshot.getLong("profileUpdatedAtMillis") ?: snapshot.getLong("updatedAt") ?: 0L
                    remoteAccountTier = snapshot.getString("accountTier") ?: ""
                    remoteAccountCreatedOn = snapshot.getString("accountCreatedOn")
                        ?: snapshot.getString("AccountCreatedOn")
                        ?: snapshot.getString("createdOn")
                    remoteProExpiryTimestamp = snapshot.getLong("proExpiryTimestamp") ?: 0L
                    // Capture cloud profile values so blank local fields never overwrite them.
                    remoteFullName = snapshot.getString("fullName").orEmpty()
                    remoteGender = snapshot.getString("gender").orEmpty()
                    remotePhoneNumber = snapshot.getString("phoneNumber").orEmpty()
                    remoteDateOfBirthOn = (snapshot.getString("dateOfBirthOn") ?: snapshot.getString("DateOfBirthOn")).orEmpty()
                    remoteFinancialGoal = snapshot.getString("financialGoal").orEmpty()
                    remoteTermsAcceptedAt = snapshot.getLong("termsAcceptedAt") ?: 0L
                    remoteTermsVersion = snapshot.getString("termsVersion").orEmpty()
                    remoteCreatedAt = snapshot.getLong("createdAt")
                        ?: snapshot.getLong("createdOn")
                        ?: remoteAccountCreatedOn?.let { parseDate(it, "dd MMMM yyyy") }
                        ?: 0L
                }
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Failed to fetch remote profile", e)
                // Continue with local data if fetch fails
            }
        }

        // Robust Detection: Treat as new if flag is true OR doc doesn't exist OR critical field missing
        val isFirstTimeInitialization = isNewUser || !docExists || (remoteCreatedAt == 0L && remoteAccountCreatedOn == null)
        val isMissingTermsInCloud = docExists && (remoteTermsAcceptedAt == 0L || remoteTermsVersion.isBlank()) && (localProfile.termsAcceptedAt != 0L || localProfile.termsVersion.isNotBlank())
        val isMissingCreatedAtInCloud = docExists && remoteCreatedAt == 0L

        android.util.Log.d("Sync", "Push Decision - isInit: $isFirstTimeInitialization, isMissingTerms: $isMissingTermsInCloud, isMissingCreated: $isMissingCreatedAtInCloud, localUp: ${localProfile.updatedAtMillis}, remoteUp: $remoteUpdatedAt")

        val now = System.currentTimeMillis()
        val isRemotePremiumExpired = EntitlementResolver.isProTier(remoteAccountTier) && remoteProExpiryTimestamp in 1..<now

        if (!EntitlementResolver.isProTier(localProfile.accountTier) && EntitlementResolver.isProTier(remoteAccountTier) && !isRemotePremiumExpired) {
            android.util.Log.d("Sync", "Skipping push: Remote is active PREMIUM, local is not.")
            return
        }
        if (docExists && localProfile.updatedAtMillis <= remoteUpdatedAt && !isFirstTimeInitialization && !isMissingTermsInCloud && !isMissingCreatedAtInCloud) {
            android.util.Log.d("Sync", "Skipping push: Cloud is up-to-date or newer.")
            return
        }

        val finalCreatedAt = when {
            remoteCreatedAt != 0L -> remoteCreatedAt
            localProfile.accountCreatedMillis != 0L -> localProfile.accountCreatedMillis
            else -> now
        }

        // Initialize / fix local creation timestamp if missing or out of sync with cloud
        if (localProfile.accountCreatedMillis == 0L || (remoteCreatedAt != 0L && localProfile.accountCreatedMillis != remoteCreatedAt)) {
            localProfile = localProfile.copy(accountCreatedMillis = finalCreatedAt)
            UserProfileDataStore.setUserProfile(context, localProfile)
        }

        val finalFullName = if (localProfile.fullName.isBlank() || localProfile.fullName == defaultUserProfile.fullName) {
            remoteFullName.ifBlank { currentUser?.displayName ?: localProfile.fullName }
        } else localProfile.fullName

        val finalPhotoUri = if (localProfile.photoUri?.startsWith("file") == true) {
            currentUser?.photoUrl?.toString()
        } else {
            localProfile.photoUri ?: currentUser?.photoUrl?.toString()
        }

        // A profile with no local date must not clear the stored cloud one: the
        // previous inline fallback only ran when a date existed but formatted
        // blank, which is effectively never, so an absent date was pushed as "".
        val formattedDateOfBirth = localProfile.dateOfBirthMillis
            ?.takeIf { it != 0L }
            ?.let { formatDate(it, "dd MMMM yyyy") }
            .orEmpty()
            .ifBlank { remoteDateOfBirthOn }

        val finalTermsAcceptedAt = when {
            remoteTermsAcceptedAt != 0L -> remoteTermsAcceptedAt
            localProfile.termsAcceptedAt != 0L -> localProfile.termsAcceptedAt
            else -> now
        }
        val finalTermsVersion = when {
            remoteTermsVersion.isNotBlank() -> remoteTermsVersion
            localProfile.termsVersion.isNotBlank() -> localProfile.termsVersion
            else -> CURRENT_TERMS_VERSION
        }
        val finalUpdatedAt = if (localProfile.updatedAtMillis == 0L) now else localProfile.updatedAtMillis

        val profileData = mutableMapOf<String, Any?>(
            "uid" to uid,
            "fullName" to finalFullName,
            "emailAddress" to localProfile.emailAddress.ifBlank { currentUser?.email ?: "" },
            // Blank local fields fall back to existing cloud values so a fresh
            // install / re-login can never wipe the stored profile.
            "phoneNumber" to localProfile.phoneNumber.ifBlank { remotePhoneNumber },
            "dateOfBirthOn" to formattedDateOfBirth,
            "gender" to localProfile.gender.ifBlank { remoteGender },
            "photoUri" to finalPhotoUri,
            "authProvider" to if (localProfile.authProvider.isNotBlank()) localProfile.authProvider else {
                if (currentUser?.isAnonymous == true) "anonymous" else {
                    currentUser?.providerData?.firstOrNull { it.providerId != "firebase" }?.providerId ?: "email"
                }
            },
            "profileUpdatedAtMillis" to finalUpdatedAt,
            "createdAt" to finalCreatedAt,
            "termsAcceptedAt" to finalTermsAcceptedAt,
            "termsVersion" to finalTermsVersion
        )

        // Security: accountTier / proExpiryTimestamp / isSubscription are
        // server-authoritative — only the redeemProPass Cloud Function writes
        // them (see implementation_plans/security_implementation_plan.md, Items
        // 14/22). The single client write the Firestore rules allow is pushing
        // accountTier=Free when the local premium has expired, so the cloud
        // stops re-granting Pro on the next pull.
        if (!EntitlementResolver.isProTier(localProfile.accountTier)) {
            profileData["accountTier"] = EntitlementResolver.TIER_FREE
        }

        try {
            userDoc.set(profileData, SetOptions.merge()).await()
            android.util.Log.i("Sync", "Successfully pushed profile (isNewUser: $isNewUser)")
        } catch (e: Exception) {
            android.util.Log.e("Sync", "Failed to push profile", e)
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

        val remoteUpdatedAt = snapshot.getLong("profileUpdatedAtMillis") ?: snapshot.getLong("updatedAt") ?: 0L
        val remoteAccountTier = snapshot.getString("accountTier") ?: ""
        val remoteAccountCreatedOn = snapshot.getString("accountCreatedOn")
            ?: snapshot.getString("AccountCreatedOn")
            ?: snapshot.getString("createdOn")
        val remoteCreatedAt = snapshot.getLong("createdAt")
            ?: snapshot.getLong("createdOn")
            ?: remoteAccountCreatedOn?.let { parseDate(it, "dd MMMM yyyy") }
            ?: 0L
        val remoteTermsAcceptedAt = snapshot.getLong("termsAcceptedAt") ?: 0L
        val remoteTermsVersion = snapshot.getString("termsVersion").orEmpty()

        val shouldPull = remoteUpdatedAt > localProfile.updatedAtMillis || 
                         (!EntitlementResolver.isProTier(localProfile.accountTier) && EntitlementResolver.isProTier(remoteAccountTier)) ||
                         localProfile.accountTier.isBlank() ||
                         (localProfile.accountCreatedMillis == 0L && remoteCreatedAt != 0L) ||
                         (localProfile.termsAcceptedAt == 0L && remoteTermsAcceptedAt != 0L)

        if (!shouldPull) return

        val finalCreatedMillis = when {
            remoteCreatedAt != 0L -> remoteCreatedAt
            localProfile.accountCreatedMillis != 0L -> localProfile.accountCreatedMillis
            else -> 0L
        }
        val finalTermsAcceptedAt = when {
            localProfile.termsAcceptedAt != 0L -> localProfile.termsAcceptedAt
            remoteTermsAcceptedAt != 0L -> remoteTermsAcceptedAt
            else -> 0L
        }
        val finalTermsVersion = when {
            localProfile.termsVersion.isNotBlank() -> localProfile.termsVersion
            remoteTermsVersion.isNotBlank() -> remoteTermsVersion
            else -> ""
        }

        val remoteProfile = UserProfile(
            fullName = snapshot.getString("fullName") ?: (if (localProfile.fullName == defaultUserProfile.fullName) authUser?.displayName else null) ?: localProfile.fullName,
            emailAddress = snapshot.getString("emailAddress") ?: authUser?.email ?: localProfile.emailAddress,
            phoneNumber = snapshot.getString("phoneNumber") ?: localProfile.phoneNumber,
            dateOfBirthMillis = (snapshot.getString("dateOfBirthOn") ?: snapshot.getString("DateOfBirthOn"))?.let { parseDate(it, "dd MMMM yyyy") } ?: localProfile.dateOfBirthMillis,
            gender = snapshot.getString("gender") ?: localProfile.gender,
            financialGoal = snapshot.getString("financialGoal") ?: localProfile.financialGoal,
            accountCreatedMillis = finalCreatedMillis,
            accountTier = remoteAccountTier,
            proExpiryTimestamp = snapshot.getLong("proExpiryTimestamp") ?: localProfile.proExpiryTimestamp,
            isSubscription = snapshot.getBoolean("isSubscription") ?: localProfile.isSubscription,
            photoUri = snapshot.getString("photoUri") ?: (if (localProfile.photoUri == null) authUser?.photoUrl?.toString() else null) ?: localProfile.photoUri,
            authProvider = snapshot.getString("authProvider") ?: localProfile.authProvider.ifBlank {
                if (authUser?.isAnonymous == true) "anonymous" else {
                    authUser?.providerData?.firstOrNull { it.providerId != "firebase" }?.providerId ?: "email"
                }
            },
            updatedAtMillis = remoteUpdatedAt,
            termsAcceptedAt = finalTermsAcceptedAt,
            termsVersion = finalTermsVersion
        )

        // Industry Standard: Handle automatic downgrade if PREMIUM has expired
        val now = System.currentTimeMillis()
        val isExpired = EntitlementResolver.isProTier(remoteAccountTier) && remoteProfile.proExpiryTimestamp in 1..<now
        
        val finalTier = if (isExpired) EntitlementResolver.TIER_FREE else remoteAccountTier
        val finalProfile = if (isExpired) remoteProfile.copy(accountTier = EntitlementResolver.TIER_FREE, updatedAtMillis = now) else remoteProfile

        UserProfileDataStore.setUserProfile(context, finalProfile)
        
        val tier = if (EntitlementResolver.isProTier(finalTier)) com.mknlabs.expensetracker.models.UserTier.PREMIUM else com.mknlabs.expensetracker.models.UserTier.FREE
        
        // Update tier and automatically enable sync if user is Premium
        AppSettingsDataStore.updateAppSettings(context) { current ->
            current.copy(
                userTier = tier,
                isCloudSyncEnabled = if (tier == com.mknlabs.expensetracker.models.UserTier.PREMIUM) true else current.isCloudSyncEnabled
            )
        }
        
        // Pro Pass users do not use temporary ad-pass expiry; ensure it is cleared (0L)
        com.mknlabs.expensetracker.data.local.MonetizationDataStore.updateGlobalAdAccessExpiry(context, 0L)

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
        // Tags before the links that reference them: a pull of the link table joins on
        // tag ids, so the tag rows should already be on the other device when its own
        // pull runs. Ordering within one push does not guarantee that across devices, but
        // it keeps the common case — one device, one batch — self-consistent.
        tagDao.getUnsynced().forEach {
            allTasks.add(SyncTask.TagTask(it))
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        tagDao.getUnsyncedLinks().forEach {
            allTasks.add(SyncTask.TransactionTagTask(it))
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
        installmentOccurrenceDao.getUnsynced().forEach {
            allTasks.add(SyncTask.InstallmentOccurrenceTask(it))
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        favoriteTransactionDao.getUnsynced().forEach {
            allTasks.add(SyncTask.FavoriteTask(it))
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        goalDao.getUnsynced().forEach { 
            allTasks.add(SyncTask.GoalTask(it)) 
            maxLocalUpdatedAt = java.lang.Math.max(maxLocalUpdatedAt, it.updatedAt)
        }
        // Funds before the transactions that link to them, so a device pulling this batch
        // in one cycle has the bucket before the spending that references it.
        fundDao.getUnsynced().forEach {
            allTasks.add(SyncTask.FundTask(it))
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

                val occIds = chunk.filterIsInstance<SyncTask.InstallmentOccurrenceTask>().map { it.entity.id }
                if (occIds.isNotEmpty()) installmentOccurrenceDao.updateSyncStates(occIds, SyncState.SYNCED.name)

                val goalIds = chunk.filterIsInstance<SyncTask.GoalTask>().map { it.entity.id }
                if (goalIds.isNotEmpty()) goalDao.updateSyncStates(goalIds, SyncState.SYNCED.name)

                val fundIds = chunk.filterIsInstance<SyncTask.FundTask>().map { it.entity.id }
                if (fundIds.isNotEmpty()) fundDao.updateSyncStates(fundIds, SyncState.SYNCED.name)

                val favIds = chunk.filterIsInstance<SyncTask.FavoriteTask>().map { it.entity.id }
                if (favIds.isNotEmpty()) favoriteTransactionDao.updateSyncStates(favIds, SyncState.SYNCED.name)

                val tagIds = chunk.filterIsInstance<SyncTask.TagTask>().map { it.entity.id }
                if (tagIds.isNotEmpty()) tagDao.updateSyncStates(tagIds, SyncState.SYNCED.name)

                val linkTasks = chunk.filterIsInstance<SyncTask.TransactionTagTask>()
                if (linkTasks.isNotEmpty()) {
                    // A link has no id column of its own, so it is acknowledged by re-writing
                    // the rows the push just sent with SYNCED. The tombstoned ones are then
                    // purged, which is the only place a link row is ever hard-deleted on the
                    // normal path.
                    tagDao.markLinksSynced(
                        linkTasks.map { it.entity.transactionId },
                        linkTasks.map { it.entity.tagId }
                    )
                    tagDao.purgeDeletedLinks()
                }

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
                // Dedup: if a local category with same name+type exists (different ID),
                // merge into the local one to avoid duplicates across devices.
                val existing = categoryDao.findActiveByNameAndType(cloudItem.name, cloudItem.transactionTypeId)
                if (existing != null && existing.id != cloudItem.id) {
                    categoryDao.upsert(cloudItem.copy(id = existing.id, syncState = SyncState.SYNCED))
                } else {
                    categoryDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
                }
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, catMax)
        } catch (e: Exception) { android.util.Log.e("Sync", "Categories pull failed", e) }

        try {
            val pmMax = pullCollection(userDoc, "payment_methods", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity ->
                // Dedup: if a local payment method with same name exists (different ID),
                // merge into the local one to avoid duplicates across devices.
                val existing = paymentMethodDao.findActiveByName(cloudItem.name)
                if (existing != null && existing.id != cloudItem.id) {
                    paymentMethodDao.upsert(cloudItem.copy(id = existing.id, syncState = SyncState.SYNCED))
                } else {
                    paymentMethodDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
                }
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

            // Funds before the transactions that link to them, so the bucket exists locally
            // by the time a transaction referencing it lands.
            val fundMax = pullCollection(userDoc, "funds", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.FundEntity ->
                fundDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, fundMax)

            // Pull Transactions & Rules (Circular Dependency Zone)
            val txMax = pullCollection(userDoc, "transactions", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity ->
                transactionDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, txMax)

            // After tags and transactions, both of which the link references.
            val tagPullMax = pullCollection(userDoc, "tags", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.TagEntity ->
                // Dedup by name, case-insensitively, so the same tag created on two devices
                // collapses to the local row rather than colliding with the unique index.
                val existing = tagDao.findActiveByNameLower(cloudItem.nameLower)
                if (existing != null && existing.id != cloudItem.id) {
                    tagDao.upsert(cloudItem.copy(id = existing.id, syncState = SyncState.SYNCED))
                } else {
                    tagDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
                }
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, tagPullMax)

            val linkMax = pullCollection(userDoc, "transaction_tags", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.TransactionTagEntity ->
                // The pair is the key, so an incoming row simply replaces the local one.
                // A tombstone (`isDeleted = true`) is written through as well, so an
                // untag on another device detaches here too.
                tagDao.upsertLinks(listOf(cloudItem.copy(syncState = SyncState.SYNCED)))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, linkMax)

            val rrMax = pullCollection(userDoc, "recurring_rules", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity ->
                recurringRuleDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, rrMax)

            // After recurring_rules on purpose: occurrences carry an FK to the
            // rule, so the parent must already exist locally.
            val occMax = pullCollection(userDoc, "installment_occurrences", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.InstallmentOccurrenceEntity ->
                installmentOccurrenceDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, occMax)

            val budgetMax = pullCollection(userDoc, "budgets", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity ->
                budgetDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, budgetMax)

            val favMax = pullCollection(userDoc, "favorite_transactions", lastSync) { cloudItem: com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity ->
                // The unique index on transaction_id allows one favorite per transaction
                // per device, but two devices can favorite the same transaction on their
                // own. The incoming row is merged onto the local row for that transaction
                // rather than colliding with the index, which would drop the pull.
                val existing = cloudItem.transactionId?.let { favoriteTransactionDao.findByTransactionId(it) }
                if (existing != null && existing.id != cloudItem.id) {
                    favoriteTransactionDao.upsert(cloudItem.copy(id = existing.id, syncState = SyncState.SYNCED))
                } else {
                    favoriteTransactionDao.upsert(cloudItem.copy(syncState = SyncState.SYNCED))
                }
            }
            maxRemoteUpdatedAt = java.lang.Math.max(maxRemoteUpdatedAt, favMax)
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
                    val item = when (T::class) {
                        com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity::class -> {
                            val id = doc.getLong("id")?.toInt() ?: 0
                            val name = doc.getString("name").orEmpty()
                            val transactionTypeId = doc.getLong("transactionTypeId")?.toInt() ?: 0
                            val iconKey = doc.getString("iconKey").orEmpty()
                            // Read by hand rather than falling through to `toObject`, because
                            // this entity has a branch of its own. A field the branch does not
                            // name is simply absent from the pulled row, which would have shown
                            // up as "my colours reset after syncing" rather than as an error.
                            val colorHex = doc.getString("colorHex")
                            val isSystem = doc.getBoolean("isSystem") ?: false
                            val sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity(
                                id = id, name = name, transactionTypeId = transactionTypeId,
                                iconKey = iconKey, colorHex = colorHex, isSystem = isSystem,
                                sortOrder = sortOrder, isDeleted = isDeleted, createdAt = createdAt,
                                updatedAt = updatedAt
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity::class -> {
                            val id = doc.getLong("id")?.toInt() ?: 0
                            val name = doc.getString("name").orEmpty()
                            val iconKey = doc.getString("iconKey").orEmpty()
                            // Read by hand for the same reason as the category branch above.
                            val colorHex = doc.getString("colorHex")
                            val isSystem = doc.getBoolean("isSystem") ?: false
                            val sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity(
                                id = id, name = name, iconKey = iconKey, colorHex = colorHex,
                                isSystem = isSystem, sortOrder = sortOrder, isDeleted = isDeleted,
                                createdAt = createdAt, updatedAt = updatedAt
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.GoalEntity::class -> {
                            val id = doc.getString("id").orEmpty()
                            val name = doc.getString("name").orEmpty()
                            val targetAmountMinor = doc.getLong("targetAmountMinor") ?: 0L
                            val currentAmountMinor = doc.getLong("currentAmountMinor") ?: 0L
                            val deadlineAt = doc.getLong("deadlineAt")
                            val iconKey = doc.getString("iconKey").orEmpty()
                            val colorHex = doc.getString("colorHex").orEmpty()
                            val isCompleted = doc.getBoolean("isCompleted") ?: false
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            com.mknlabs.expensetracker.data.local.room.entities.GoalEntity(
                                id = id, name = name, targetAmountMinor = targetAmountMinor,
                                currentAmountMinor = currentAmountMinor, deadlineAt = deadlineAt,
                                iconKey = iconKey, colorHex = colorHex, isCompleted = isCompleted,
                                createdAt = createdAt, updatedAt = updatedAt, isDeleted = isDeleted
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.FundEntity::class -> {
                            val id = doc.getString("id").orEmpty()
                            val name = doc.getString("name").orEmpty()
                            val amountMinor = doc.getLong("amountMinor") ?: 0L
                            val startDate = doc.getLong("startDate") ?: 0L
                            val iconKey = doc.getString("iconKey").orEmpty()
                            val colorHex = doc.getString("colorHex").orEmpty()
                            val note = doc.getString("note").orEmpty()
                            val isArchived = doc.getBoolean("isArchived") ?: false
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            com.mknlabs.expensetracker.data.local.room.entities.FundEntity(
                                id = id, name = name, amountMinor = amountMinor, startDate = startDate,
                                iconKey = iconKey, colorHex = colorHex, note = note, isArchived = isArchived,
                                createdAt = createdAt, updatedAt = updatedAt, isDeleted = isDeleted
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity::class -> {
                            val id = doc.getString("id").orEmpty()
                            val note = doc.getString("note").orEmpty()
                            val amountMinor = doc.getLong("amountMinor") ?: 0L
                            val occurredAt = doc.getLong("occurredAt") ?: 0L
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            val transactionTypeId = doc.getLong("transactionTypeId")?.toInt() ?: 0
                            val categoryId = doc.getLong("categoryId")?.toInt() ?: 0
                            val paymentMethodId = doc.getLong("paymentMethodId")?.toInt() ?: 0
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            val contentHash = doc.getString("contentHash")
                            val sourceRecurringRuleId = doc.getString("sourceRecurringRuleId")
                            // Carried through so a transaction pulled on another device keeps the
                            // bucket it was spent from. Absent on rows pushed before funds existed.
                            val fundId = doc.getString("fundId")
                            com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity(
                                id = id, note = note, amountMinor = amountMinor, occurredAt = occurredAt,
                                createdAt = createdAt, updatedAt = updatedAt, transactionTypeId = transactionTypeId,
                                categoryId = categoryId, paymentMethodId = paymentMethodId, isDeleted = isDeleted,
                                contentHash = contentHash, sourceRecurringRuleId = sourceRecurringRuleId,
                                fundId = fundId
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity::class -> {
                            val id = doc.getString("id").orEmpty()
                            val transactionId = doc.getString("transactionId").orEmpty()
                            val frequencyStr = doc.getString("frequency").orEmpty()
                            val frequency = try {
                                com.mknlabs.expensetracker.models.RecurringFrequency.valueOf(frequencyStr)
                            } catch (e: Exception) {
                                com.mknlabs.expensetracker.models.RecurringFrequency.Monthly
                            }
                            val intervalCount = doc.getLong("intervalCount")?.toInt() ?: 1
                            val repeatCount = doc.getLong("repeatCount")?.toInt() ?: 0
                            val remainingCount = doc.getLong("remainingCount")?.toInt()
                            val anchorAt = doc.getLong("anchorAt") ?: 0L
                            val nextRunAt = doc.getLong("nextRunAt") ?: 0L
                            val lastRunAt = doc.getLong("lastRunAt")
                            val lastNotifiedOccurrenceAt = doc.getLong("lastNotifiedOccurrenceAt")
                            val isEnabled = doc.getBoolean("isEnabled") ?: true
                            val notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true
                            val lastNotifiedWindowDays = doc.getLong("lastNotifiedWindowDays")?.toInt()
                            val recurringTypeStr = doc.getString("recurringType")
                            val recurringType = try {
                                com.mknlabs.expensetracker.models.RecurringType.valueOf(recurringTypeStr.orEmpty())
                            } catch (e: Exception) {
                                // Pre-installment docs (and any unknown value) are plain repeating rules.
                                com.mknlabs.expensetracker.models.RecurringType.REGULAR
                            }
                            val installmentStatusStr = doc.getString("installmentStatus")
                            val installmentStatus = installmentStatusStr?.let { raw ->
                                com.mknlabs.expensetracker.models.InstallmentStatus.entries.firstOrNull { it.name == raw }
                            }
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity(
                                id = id, transactionId = transactionId, frequency = frequency,
                                intervalCount = intervalCount, repeatCount = repeatCount, remainingCount = remainingCount,
                                anchorAt = anchorAt, nextRunAt = nextRunAt, lastRunAt = lastRunAt,
                                lastNotifiedOccurrenceAt = lastNotifiedOccurrenceAt, isEnabled = isEnabled,
                                notificationsEnabled = notificationsEnabled,
                                lastNotifiedWindowDays = lastNotifiedWindowDays,
                                recurringType = recurringType,
                                installmentTotalMinor = doc.getLong("installmentTotalMinor"),
                                installmentAmountMinor = doc.getLong("installmentAmountMinor"),
                                installmentTotalCount = doc.getLong("installmentTotalCount")?.toInt(),
                                installmentStatus = installmentStatus,
                                createdAt = createdAt, updatedAt = updatedAt, isDeleted = isDeleted
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.InstallmentOccurrenceEntity::class -> {
                            val id = doc.getString("id").orEmpty()
                            val statusStr = doc.getString("status").orEmpty()
                            val status = try {
                                com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus.valueOf(statusStr)
                            } catch (e: Exception) {
                                com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus.PENDING
                            }
                            com.mknlabs.expensetracker.data.local.room.entities.InstallmentOccurrenceEntity(
                                id = id,
                                ruleId = doc.getString("ruleId").orEmpty(),
                                installmentIndex = doc.getLong("installmentIndex")?.toInt() ?: 0,
                                dueAt = doc.getLong("dueAt") ?: 0L,
                                amountMinor = doc.getLong("amountMinor") ?: 0L,
                                paidAt = doc.getLong("paidAt"),
                                status = status,
                                transactionId = doc.getString("transactionId"),
                                createdAt = doc.getLong("createdAt") ?: 0L,
                                updatedAt = doc.getLong("updatedAt") ?: 0L,
                                isDeleted = doc.getBoolean("isDeleted") ?: false
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity::class -> {
                            val id = doc.getString("id").orEmpty()
                            val categoryId = doc.getLong("categoryId")?.toInt() ?: 0
                            val monthStart = doc.getLong("monthStart") ?: 0L
                            val limitMinor = doc.getLong("limitMinor") ?: 0L
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            val editCount = doc.getLong("editCount")?.toInt() ?: 0
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity(
                                id = id, categoryId = categoryId, monthStart = monthStart, limitMinor = limitMinor,
                                createdAt = createdAt, updatedAt = updatedAt, editCount = editCount, isDeleted = isDeleted
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity::class -> {
                            val id = doc.getString("id").orEmpty()
                            val transactionId = doc.getString("transactionId")
                            val title = doc.getString("title").orEmpty()
                            val amountMinor = doc.getLong("amountMinor") ?: 0L
                            val transactionTypeId = doc.getLong("transactionTypeId")?.toInt() ?: 2
                            val categoryId = doc.getLong("categoryId")?.toInt() ?: 0
                            val paymentTypeId = doc.getLong("paymentTypeId")?.toInt() ?: 0
                            val note = doc.getString("note").orEmpty()
                            val isPinned = doc.getBoolean("isPinned") ?: true
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity(
                                id = id, transactionId = transactionId, title = title,
                                amountMinor = amountMinor, transactionTypeId = transactionTypeId,
                                categoryId = categoryId, paymentTypeId = paymentTypeId, note = note,
                                isPinned = isPinned, createdAt = createdAt, updatedAt = updatedAt,
                                isDeleted = isDeleted
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.TagEntity::class -> {
                            val id = doc.getString("id").orEmpty()
                            val name = doc.getString("name").orEmpty()
                            // `nameLower` is the unique key, so it must never arrive blank on a
                            // document written before the field existed; deriving it from the
                            // name is the only safe fallback.
                            val nameLower = doc.getString("nameLower")?.takeIf { it.isNotBlank() }
                                ?: name.lowercase()
                            val colorHex = doc.getString("colorHex")
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: 0L
                            com.mknlabs.expensetracker.data.local.room.entities.TagEntity(
                                id = id, name = name, nameLower = nameLower, colorHex = colorHex,
                                isDeleted = isDeleted, createdAt = createdAt, updatedAt = updatedAt
                            ) as T
                        }
                        com.mknlabs.expensetracker.data.local.room.entities.TransactionTagEntity::class -> {
                            val transactionId = doc.getString("transactionId").orEmpty()
                            val tagId = doc.getString("tagId").orEmpty()
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val updatedAt = doc.getLong("updatedAt") ?: createdAt
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            com.mknlabs.expensetracker.data.local.room.entities.TransactionTagEntity(
                                transactionId = transactionId, tagId = tagId, createdAt = createdAt,
                                updatedAt = updatedAt, isDeleted = isDeleted
                            ) as T
                        }
                        else -> doc.toObject(T::class.java)
                    }
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
                "contentHash" to entity.contentHash, "sourceRecurringRuleId" to entity.sourceRecurringRuleId,
                "fundId" to entity.fundId
            )
        }
        data class TagTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.TagEntity) : SyncTask() {
            override val id = entity.id
            override val collectionName = "tags"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "name" to entity.name, "nameLower" to entity.nameLower,
                "colorHex" to entity.colorHex, "isDeleted" to entity.isDeleted,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt
            )
        }
        data class TransactionTagTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.TransactionTagEntity) : SyncTask() {
            // The pair is the identity. A single string rather than a nested map so the
            // Firestore document id is stable and readable, and so a re-push of the same
            // link lands on the same document instead of creating a second one.
            override val id = "${entity.transactionId}_${entity.tagId}"
            override val collectionName = "transaction_tags"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "transactionId" to entity.transactionId, "tagId" to entity.tagId,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt,
                "isDeleted" to entity.isDeleted
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
                "colorHex" to entity.colorHex,
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
                "colorHex" to entity.colorHex,
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
                "notificationsEnabled" to entity.notificationsEnabled,
                "lastNotifiedWindowDays" to entity.lastNotifiedWindowDays,
                "recurringType" to entity.recurringType.name,
                "installmentTotalMinor" to entity.installmentTotalMinor,
                "installmentAmountMinor" to entity.installmentAmountMinor,
                "installmentTotalCount" to entity.installmentTotalCount,
                "installmentStatus" to entity.installmentStatus?.name,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt, "isDeleted" to entity.isDeleted
            )
        }
        data class InstallmentOccurrenceTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.InstallmentOccurrenceEntity) : SyncTask() {
            override val id = entity.id
            override val collectionName = "installment_occurrences"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "ruleId" to entity.ruleId, "installmentIndex" to entity.installmentIndex,
                "dueAt" to entity.dueAt, "amountMinor" to entity.amountMinor, "paidAt" to entity.paidAt,
                "status" to entity.status.name, "transactionId" to entity.transactionId,
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
        data class FundTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.FundEntity) : SyncTask() {
            override val id = entity.id
            override val collectionName = "funds"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "name" to entity.name, "amountMinor" to entity.amountMinor,
                "startDate" to entity.startDate, "iconKey" to entity.iconKey, "colorHex" to entity.colorHex,
                "note" to entity.note, "isArchived" to entity.isArchived,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt, "isDeleted" to entity.isDeleted
            )
        }
        data class FavoriteTask(val entity: com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity) : SyncTask() {
            override val id = entity.id
            override val collectionName = "favorite_transactions"
            override val isDeleted = entity.isDeleted
            override fun toCloudMap() = mapOf(
                "id" to entity.id, "transactionId" to entity.transactionId, "title" to entity.title,
                "amountMinor" to entity.amountMinor, "transactionTypeId" to entity.transactionTypeId,
                "categoryId" to entity.categoryId, "paymentTypeId" to entity.paymentTypeId,
                "note" to entity.note, "isPinned" to entity.isPinned,
                "createdAt" to entity.createdAt, "updatedAt" to entity.updatedAt, "isDeleted" to entity.isDeleted
            )
        }
    }
}
