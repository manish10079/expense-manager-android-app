package com.mknlabs.expensetracker.ai

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrReceiptDeduper @Inject constructor(
    @AiUsageDataStore private val dataStore: DataStore<Preferences>,
) {
    suspend fun wasSavedRecently(
        fingerprint: String,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        if (fingerprint.isBlank()) return false
        val fresh = prune(load(), now)
        persist(fresh)
        return fresh.any { it.first == fingerprint }
    }

    suspend fun rememberSaved(
        fingerprint: String,
        now: Long = System.currentTimeMillis(),
    ) {
        if (fingerprint.isBlank()) return
        val fresh = prune(load(), now).toMutableList()
        fresh.removeAll { it.first == fingerprint }
        fresh.add(fingerprint to now)
        persist(fresh)
    }

    private suspend fun load(): List<Pair<String, Long>> {
        val raw = dataStore.data.first()[KEY].orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(',').mapNotNull { part ->
            val sep = part.lastIndexOf(':')
            if (sep <= 0) return@mapNotNull null
            val fp = part.substring(0, sep)
            val at = part.substring(sep + 1).toLongOrNull() ?: return@mapNotNull null
            fp to at
        }
    }

    private suspend fun persist(entries: List<Pair<String, Long>>) {
        val packed = entries.joinToString(",") { "${it.first}:${it.second}" }
        dataStore.edit { prefs ->
            if (packed.isBlank()) prefs.remove(KEY) else prefs[KEY] = packed
        }
    }

    private fun prune(entries: List<Pair<String, Long>>, now: Long): List<Pair<String, Long>> =
        entries.filter { now - it.second in 0..WINDOW_MILLIS }

    companion object {
        const val WINDOW_MILLIS: Long = 7L * 24L * 60L * 60L * 1000L
        private val KEY = stringPreferencesKey("ocr_saved_fingerprints")
    }
}
