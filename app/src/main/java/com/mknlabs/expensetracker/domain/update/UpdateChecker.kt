package com.mknlabs.expensetracker.domain.update

import com.mknlabs.expensetracker.domain.models.UpdateInfo
import javax.inject.Inject

/** Result of evaluating the installed version against the remote values. */
sealed interface UpdateDecision {
    data object NoUpdate : UpdateDecision
    data class UpdateAvailable(
        val info: UpdateInfo,
        val force: Boolean
    ) : UpdateDecision
}

/**
 * Pure, Android-free logic that compares the installed version with the
 * Remote Config values. Version code is the primary signal; version name is
 * used as a fallback when no remote version code is configured.
 */
class UpdateChecker @Inject constructor() {

    fun evaluate(
        currentVersionCode: Int,
        currentVersionName: String,
        info: UpdateInfo
    ): UpdateDecision {
        val newerAvailable = when {
            info.latestVersionCode > 0 -> info.latestVersionCode > currentVersionCode
            info.latestVersion.isNotBlank() -> isVersionNameNewer(currentVersionName, info.latestVersion)
            else -> false
        }
        return if (newerAvailable) {
            UpdateDecision.UpdateAvailable(info = info, force = info.forceUpdate)
        } else {
            UpdateDecision.NoUpdate
        }
    }

    private fun isVersionNameNewer(current: String, latest: String): Boolean {
        val currentParts = current.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }

        // If either version is not fully numeric, fall back to simple inequality.
        if (currentParts.isEmpty() || latestParts.isEmpty()) {
            return latest != current
        }

        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val currentPart = currentParts.getOrNull(i) ?: 0
            val latestPart = latestParts.getOrNull(i) ?: 0
            if (latestPart != currentPart) {
                return latestPart > currentPart
            }
        }
        return false
    }
}