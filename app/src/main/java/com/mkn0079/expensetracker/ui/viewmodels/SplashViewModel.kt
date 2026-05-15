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
    object Start : InitTask(R.string.msg_initializing, 0)
    object AppLock : InitTask(R.string.msg_starting_security_services, 15)
    object LoadPrefs : InitTask(R.string.msg_loading_preferences, 30)
    object LoadProfile : InitTask(R.string.msg_preparing_database, 45)
    object InitDB : InitTask(R.string.msg_initializing_database, 70)
    object WarmUp : InitTask(R.string.msg_warming_up_engine, 85)
    object Finalize : InitTask(R.string.msg_getting_things_ready, 95)
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

    private val minDuration = 1800L // Increased slightly for data warm-up

    init {
        startInitialization()
    }

    private fun startInitialization() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val context = appContext

            try {
                // Core Preferences
                _currentTask.value = InitTask.AppLock
                AppLockPreferences.initialize(context)
                delay(400)

                _currentTask.value = InitTask.LoadPrefs
                AppSettingsDataStore.initialize(context)
                delay(400)

                _currentTask.value = InitTask.LoadProfile
                UserProfileDataStore.initialize(context)
                delay(400)

                // Database and Data Warming
                _currentTask.value = InitTask.InitDB
                ExpenseTrackerDatabaseInitializer.initialize(context)
                delay(300)

                _currentTask.value = InitTask.WarmUp
                // Perform a warm-up fetch to ensure Room caches are ready
                transactionRepository.observeActiveTransactionCount().first()
                delay(300)

                _currentTask.value = InitTask.Finalize
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
