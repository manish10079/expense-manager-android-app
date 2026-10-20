package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.domain.models.UpdateInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Force flag and dialog copy from Firebase Remote Config.
 * Play In-App Updates is the availability check.
 */
interface UpdateRepository {
    val updateInfo: StateFlow<UpdateInfo>

    /** Kicks off a Remote Config fetch; emits the fresh values on success. */
    fun fetchAndActivate()
}
