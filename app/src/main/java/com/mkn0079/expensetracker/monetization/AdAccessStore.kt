package com.mkn0079.expensetracker.monetization

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manages temporary feature unlocks (e.g., from watching ads).
 * Features in this store have an expiration time.
 */
object AdAccessStore {
    
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    
    // Key: FeatureId (+ Optional OptionId), Value: Expiration Epoch Millis
    private val _unlockedFeatures = MutableStateFlow<Map<String, Long>>(emptyMap())
    val unlockedFeatures = _unlockedFeatures.asStateFlow()

    init {
        startExpirationWatcher()
    }

    private fun startExpirationWatcher() {
        scope.launch {
            while (true) {
                delay(1000) // Check every second for reactivity
                val now = System.currentTimeMillis()
                
                val current = _unlockedFeatures.value
                val expiredKeys = current.filter { it.value <= now }.keys
                
                if (expiredKeys.isNotEmpty()) {
                    _unlockedFeatures.update { it - expiredKeys }
                }
            }
        }
    }

    /**
     * Grants temporary access to a feature or option.
     */
    fun grantAccess(feature: Feature, optionId: String? = null, durationMillis: Long) {
        val key = getFeatureKey(feature, optionId)
        val expiry = System.currentTimeMillis() + durationMillis
        
        _unlockedFeatures.update { it + (key to expiry) }
    }

    /**
     * Checks if a feature/option is currently unlocked via ad reward.
     */
    fun isUnlocked(feature: Feature, optionId: String? = null): Boolean {
        val key = getFeatureKey(feature, optionId)
        val expiry = _unlockedFeatures.value[key] ?: return false
        return expiry > System.currentTimeMillis()
    }

    private fun getFeatureKey(feature: Feature, optionId: String?): String {
        return if (optionId != null) "${feature.id}_$optionId" else feature.id
    }
}
