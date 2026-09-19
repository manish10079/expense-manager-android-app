package com.mknlabs.expensetracker.feature.smsinbox.domain.repository

/**
 * Clears the shade notification that announced a detection.
 *
 * Deciding a detection's fate in the app has to take its notification with it: a card
 * left in the shade would keep offering Add / Ignore on a decision already made, and
 * tapping it would open a row that is no longer actionable.
 *
 * The inbox only states *that* a card should go; removing one is an Android concern, so
 * it sits behind this port and the use cases stay free of a Context — which is what
 * keeps them testable without a device.
 */
interface SmsNotificationCleaner {

    /**
     * Removes the notification posted for one detection. Idempotent: clearing a card
     * that is already gone (the user swiped it away, or it was never posted) does
     * nothing.
     */
    fun clear(notificationId: Int)
}
