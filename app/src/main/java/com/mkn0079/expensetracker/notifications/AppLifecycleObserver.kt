package com.mkn0079.expensetracker.notifications

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mkn0079.expensetracker.domain.repository.SecurityRepository
import com.mkn0079.expensetracker.ui.viewmodels.AppLockViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val securityRepository: SecurityRepository
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        // App backgrounded: Record the time
        securityRepository.markBackgrounded()
    }

    override fun onStart(owner: LifecycleOwner) {
        // App foregrounded: Notify repository
        securityRepository.notifyAppForeground()
    }
}
