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

    /**
     * Fast-start init: only the essential local initialization blocks the first
     * frame (DataStore, Room, App Lock, profile) plus a quick Room warm-up fetch.
     * Remote config (update-required / maintenance flags) and cloud sync continue
     * in the background. Snappy delays are used between tasks to ensure the custom
     * overlay and progress animation are visible to the user.
     */
    private fun startInitialization() {
        viewModelScope.launch {
            val context = appContext
            try {
                // Step 1: Preparing Dashboard
                _currentTask.value = InitTask.Start
                AppLockPreferences.initialize(context)
                AppSettingsDataStore.initialize(context)
                ExpenseTrackerDatabaseInitializer.initialize(context)
                UserProfileDataStore.initialize(context)

                // Warm up Room so the first list renders without a loading hitch.
                transactionRepository.observeActiveTransactionCount().first()

                // Ensure AppSettings has emitted before transitioning
                AppSettingsDataStore.getAppSettingsFlow(context).first()

                delay(400)

                // Step 2: Syncing Tasks (runs actual network operations in bg, shows status)
                _currentTask.value = InitTask.Syncing
                
                // Remote config in the background
                viewModelScope.launch {
                    try {
                        configurationRepository.fetchAndActivate()
                        _isUpdateRequired.value = configurationRepository.isUpdateRequired()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Cloud sync in the background
                viewModelScope.launch {
                    try {
                        syncRepository.syncUserProfile()
                        com.mknlabs.expensetracker.workers.SyncWorker.startImmediate(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                delay(400)

                // Step 3: Securing Data
                _currentTask.value = InitTask.Securing
                
                delay(400)
            } catch (e: Exception) {
                // Fail-safe: don't block app startup on initialization errors
                e.printStackTrace()
            }

            _currentTask.value = InitTask.Complete
            // Snappy delay to let the progress bar animation catch up to 100%
            delay(400)
            _isReady.value = true
        }
    }
}
