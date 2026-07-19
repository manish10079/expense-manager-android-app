package com.mknlabs.expensetracker.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.mknlabs.expensetracker.R

sealed class InitTask(val labelResId: Int, val progress: Int) {
    object Start : InitTask(R.string.msg_preparing_dashboard, 0)
    object Syncing : InitTask(R.string.msg_syncing_transactions, 40)
    object Securing : InitTask(R.string.msg_securing_data, 80)
    object Complete : InitTask(R.string.label_done, 100)
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val transactionRepository: TransactionRepository,
    private val configurationRepository: ConfigurationRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _currentTask = MutableStateFlow<InitTask>(InitTask.Start)
    val currentTask: StateFlow<InitTask> = _currentTask.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isUpdateRequired = MutableStateFlow(false)
    val isUpdateRequired: StateFlow<Boolean> = _isUpdateRequired.asStateFlow()

    private val _isUnderMaintenance = MutableStateFlow(false)
    val isUnderMaintenance: StateFlow<Boolean> = _isUnderMaintenance.asStateFlow()

    private val minDuration = 2400L // Restored to normal speed

    init {
        observeConfiguration()
        startInitialization()
    }

    private fun observeConfiguration() {
        viewModelScope.launch {
            configurationRepository.isUnderMaintenance.collectLatest {
                _isUnderMaintenance.value = it
            }
        }
    }

    private fun startInitialization() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val context = appContext

            try {
                // Step 1: Preparing Dashboard (Includes Prefs and DB Init)
                _currentTask.value = InitTask.Start
                AppLockPreferences.initialize(context)
                AppSettingsDataStore.initialize(context)
                ExpenseTrackerDatabaseInitializer.initialize(context)
                
                // Fetch latest remote config
                configurationRepository.fetchAndActivate()
                
                delay(500)

                // Step 2: Syncing Transactions (Includes Profile and Data Warm-up)
                _currentTask.value = InitTask.Syncing
                UserProfileDataStore.initialize(context)
                
                // 1. Force a profile sync and trigger general Cloud Sync in the background,
                // so it does not block the splash screen transition and app load time.
                viewModelScope.launch {
                    try {
                        syncRepository.syncUserProfile()
                        com.mknlabs.expensetracker.workers.SyncWorker.startImmediate(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Perform a warm-up fetch to ensure Room caches are ready
                transactionRepository.observeActiveTransactionCount().first()
                delay(500)

                // Step 3: Securing Data (Final Checks)
                _currentTask.value = InitTask.Securing
                
                // Check if update is required after config is fetched
                _isUpdateRequired.value = configurationRepository.isUpdateRequired()
                
                delay(800)

            } catch (e: Exception) {
                // Fail-safe: don't block app startup on initialization errors
                e.printStackTrace()
            }

            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < minDuration) {
                delay(minDuration - elapsed)
            }

            _currentTask.value = InitTask.Complete
            
            // Allow time for the final progress bar animation (600ms) to complete in the UI
            delay(800)
            
            _isReady.value = true
        }
    }
}
