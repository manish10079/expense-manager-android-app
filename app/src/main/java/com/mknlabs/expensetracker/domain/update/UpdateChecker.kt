package com.mknlabs.expensetracker.domain.update

import com.mknlabs.expensetracker.domain.models.UpdateInfo
import javax.inject.Inject

/** Result of evaluating Play availability against Remote Config copy. */
sealed interface UpdateDecision {
    data object NoUpdate : UpdateDecision
    data class UpdateAvailable(
        val info: UpdateInfo,
        val force: Boolean
    ) : UpdateDecision
}

/**
 * Shows the dialog only when Play reports an installable update.
 * [UpdateInfo.forceUpdate] chooses blocking vs optional; it never invents
 * availability.
 */
class UpdateChecker @Inject constructor() {

    fun evaluate(
        playUpdateAvailable: Boolean,
        info: UpdateInfo
    ): UpdateDecision {
        if (!playUpdateAvailable) return UpdateDecision.NoUpdate
        return UpdateDecision.UpdateAvailable(info = info, force = info.forceUpdate)
    }
}
