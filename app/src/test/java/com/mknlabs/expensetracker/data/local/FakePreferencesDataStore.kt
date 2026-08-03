package com.mknlabs.expensetracker.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory [DataStore] for JVM tests. Fully synchronous — `updateData` applies
 * the transform atomically and completes without touching disk, so tests avoid
 * the real DataStore's file I/O (which is flaky on Windows for back-to-back
 * writes).
 */
class FakePreferencesDataStore(
    initial: Preferences = emptyPreferences()
) : DataStore<Preferences> {

    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> = state.asStateFlow()

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.update { transform(it) }
        return state.value
    }
}
