package com.mknlabs.expensetracker.data.repository

import android.content.Context
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecurityRepository {

    override fun hasPin(): Boolean {
        return AppLockPreferences.hasPin(context)
    }

    override fun isLockEnabled(): Boolean {
        return AppLockPreferences.isEnabled(context)
    }

    override fun markBackgrounded(timestamp: Long) {
        AppLockPreferences.markBackgrounded(context, timestamp)
    }

    override fun markUnlocked(timestamp: Long) {
        AppLockPreferences.markUnlocked(context, timestamp)
    }

    override fun shouldRequireUnlock(): Boolean {
        return AppLockPreferences.shouldRequireUnlock(
            context = context,
            autoLockDurationMinutes = getAutoLockDurationMinutes()
        )
    }

    override fun validatePin(pin: String): Boolean {
        val isValid = AppLockPreferences.validatePin(context, pin)
        if (isValid) {
            markUnlocked()
        }
        return isValid
    }

    override fun getAutoLockDurationMinutes(): Int {
        return AppLockPreferences.getAutoLockDurationMinutes(context)
    }

    private val _appForegroundEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val appForegroundEvents = _appForegroundEvents.asSharedFlow()

    override fun notifyAppForeground() {
        _appForegroundEvents.tryEmit(Unit)
    }

    private val _appBackgroundEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val appBackgroundEvents = _appBackgroundEvents.asSharedFlow()

    override fun notifyAppBackground() {
        _appBackgroundEvents.tryEmit(Unit)
    }

    override suspend fun disableLock() {
        // 1. Clear PIN and security questions from encrypted preferences
        AppLockPreferences.clearAll(context)
        
        // 2. Update DataStore settings to reflect that lock is disabled
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(
                appLockEnabled = false,
                biometricLockEnabled = false
            )
        }
    }
}
