package com.mkn0079.expensetracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.data.local.AppLockPreferences
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.UserProfileDataStore
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer
import com.mkn0079.expensetracker.data.repository.ExpenseTrackerRepositoryProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class InitTask(val label: String, val progress: Int) {
    object Start : InitTask("Initializing", 0)
    object AppLock : InitTask("Starting security services", 15)
    object LoadPrefs : InitTask("Loading your preferences", 30)
    object LoadProfile : InitTask("Preparing your profile", 45)
    object InitDB : InitTask("Initializing database", 70)
    object WarmUp : InitTask("Warming up engine", 85)
    object Finalize : InitTask("Getting things ready", 95)
    object Complete : InitTask("Done", 100)
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {

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
            val context = getApplication<Application>().applicationContext

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
                val transactionRepo = ExpenseTrackerRepositoryProvider.transactionRepository(context)
                transactionRepo.observeActiveTransactionCount().first() 
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
