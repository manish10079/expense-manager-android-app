package com.mknlabs.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mknlabs.expensetracker.models.CalculatorHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

val Context.calculatorHistoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "calculator_history"
)

/**
 * Qualifier for the dedicated calculator-history DataStore. Kept separate from
 * the app-settings and other learning stores so calculator history stays private
 * to the Itemized Calculator feature.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CalculatorHistoryDataStore

/**
 * Persists completed Normal-mode calculator calculations, newest first.
 *
 * Entries are capped at [MAX_HISTORY_ENTRIES]; when a new entry is recorded and
 * the cap is exceeded, the oldest entry is dropped automatically.
 */
@Singleton
class CalculatorHistoryStore @Inject constructor(
    @CalculatorHistoryDataStore private val dataStore: DataStore<Preferences>
) {

    /** Observable snapshot of the stored history (newest entry first). */
    fun observeHistory(): Flow<List<CalculatorHistoryEntry>> =
        dataStore.data.map { preferences -> decode(preferences[ENTRIES_KEY]) }

    /**
     * Records a completed calculation at the front of the list and trims the
     * oldest entries past [MAX_HISTORY_ENTRIES].
     */
    suspend fun addEntry(expression: String, result: String) {
        val entry = CalculatorHistoryEntry(
            expression = expression,
            result = result,
            timestampMillis = System.currentTimeMillis()
        )
        dataStore.edit { preferences ->
            val updated = (listOf(entry) + decode(preferences[ENTRIES_KEY]))
                .take(MAX_HISTORY_ENTRIES)
            preferences[ENTRIES_KEY] = encode(updated)
        }
    }

    /** Removes every stored history entry. */
    suspend fun clearHistory() {
        dataStore.edit { it.remove(ENTRIES_KEY) }
    }

    private fun encode(entries: List<CalculatorHistoryEntry>): String {
        // Entries are tab-delimited so newlines/commas inside expressions and
        // formatted results never interfere with the on-disk format.
        return entries.joinToString(separator = "\n") { entry ->
            listOf(
                entry.expression,
                entry.result,
                entry.timestampMillis.toString()
            ).joinToString(separator = "\t")
        }
    }

    private fun decode(raw: String?): List<CalculatorHistoryEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.lines().mapNotNull { line ->
            val parts = line.split("\t")
            if (parts.size != 3) return@mapNotNull null
            val timestamp = parts[2].toLongOrNull() ?: return@mapNotNull null
            CalculatorHistoryEntry(
                expression = parts[0],
                result = parts[1],
                timestampMillis = timestamp
            )
        }
    }

    private companion object {
        val ENTRIES_KEY = stringPreferencesKey("history_entries")
        const val MAX_HISTORY_ENTRIES = 50
    }
}
