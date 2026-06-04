package com.mknlabs.expensetracker.notifications

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mknlabs.expensetracker.domain.repository.SecurityRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val securityRepository: SecurityRepository
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        // App backgrounded: Record the time and notify
        securityRepository.markBackgrounded()
        securityRepository.notifyAppBackground()
    }

    override fun onStart(owner: LifecycleOwner) {
        // App foregrounded: Notify repository
        securityRepository.notifyAppForeground()
    }
}
