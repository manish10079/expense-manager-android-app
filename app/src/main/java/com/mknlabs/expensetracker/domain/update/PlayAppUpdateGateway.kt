package com.mknlabs.expensetracker.domain.update

import android.app.Activity

/**
 * Play In-App Updates. Availability comes from the store; this type never
 * reads Remote Config. Failures (no Play, sideload, emulator) are `false`,
 * not crashes.
 */
interface PlayAppUpdateGateway {

    /** True when Play has a newer installable build, or a flow is already running. */
    suspend fun isUpdateAvailable(): Boolean

    /**
     * Starts immediate or flexible update. [onFlowUnavailable] runs when Play
     * cannot start (wrong type, no Play), so the UI can open the store listing.
     */
    fun startUpdate(
        activity: Activity,
        immediate: Boolean,
        onFlowUnavailable: () -> Unit
    )

    /** Restarts an in-progress immediate update after the activity is recreated. */
    fun resumeInProgressUpdate(activity: Activity)

    /** Applies a flexible download that finished in the background. */
    fun completeDownloadedUpdate()
}
