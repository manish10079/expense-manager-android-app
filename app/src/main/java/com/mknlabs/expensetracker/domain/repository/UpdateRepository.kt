package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.domain.models.UpdateInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Provides the "latest version" parameters from Firebase Remote Config.
 * Values are emitted reactively whenever the config is fetched/activated.
 */
interface UpdateRepository {
    val updateInfo: StateFlow<UpdateInfo>

    /** Kicks off a Remote Config fetch; emits the fresh values on success. */
    fun fetchAndActivate()
}