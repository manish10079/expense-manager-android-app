package com.mkn0079.expensetracker.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.data.local.AppLockPreferences
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.UserProfileDataStore
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer
import com.mkn0079.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.mkn0079.expensetracker.R

sealed class InitTask(val labelResId: Int, val progress: Int) {
    object Start : InitTask(R.string.msg_preparing_dashboard, 0)
    object Syncing : InitTask(R.string.msg_syncing_transactions, 40)
    object Securing : InitTask(R.string.msg_securing_data, 80)
    object Complete : InitTask(R.string.label_done, 100)
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _currentTask = MutableStateFlow<InitTask>(InitTask.Start)
    val currentTask: StateFlow<InitTask> = _currentTask.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val minDuration = 2400L // Adjusted for 3 major steps

    init {
        startInitialization()
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
                delay(800)

                // Step 2: Syncing Transactions (Includes Profile and Data Warm-up)
                _currentTask.value = InitTask.Syncing
                UserProfileDataStore.initialize(context)
                // Perform a warm-up fetch to ensure Room caches are ready
                transactionRepository.observeActiveTransactionCount().first()
                delay(800)

                // Step 3: Securing Data (Final Checks)
                _currentTask.value = InitTask.Securing
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
            _isReady.value = true
        }
    }
}
