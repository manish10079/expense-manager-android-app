package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionNudgeTest {

    private val now = 1_700_000_000_000L
    private val day = 86_400_000L

    @Test
    fun firstAskAlways() {
        assertTrue(
            PermissionNudge.shouldAsk(
                granted = false,
                askCount = 0,
                lastAskAtMillis = 0L,
                nowMillis = now,
                intervalDays = 7,
                canShowRationale = false,
            )
        )
    }

    @Test
    fun secondAskOnNextLaunch() {
        assertTrue(
            PermissionNudge.shouldAsk(
                granted = false,
                askCount = 1,
                lastAskAtMillis = now,
                nowMillis = now,
                intervalDays = 7,
                canShowRationale = true,
            )
        )
    }

    @Test
    fun skipsWhenGranted() {
        assertFalse(
            PermissionNudge.shouldAsk(
                granted = true,
                askCount = 0,
                lastAskAtMillis = 0L,
                nowMillis = now,
                intervalDays = 7,
                canShowRationale = true,
            )
        )
    }

    @Test
    fun settingsCardWaitsFiveToTenDaysAfterTwoOsAsks() {
        assertFalse(
            PermissionNudge.shouldShowSettingsCard(
                missingNotifications = true,
                missingSms = false,
                notificationAskCount = 2,
                smsAskCount = 0,
                lastOsAskAtMillis = now - 4 * day,
                lastCardAtMillis = 0L,
                nextIntervalDays = 7,
                nowMillis = now,
            )
        )
        assertTrue(
            PermissionNudge.shouldShowSettingsCard(
                missingNotifications = true,
                missingSms = false,
                notificationAskCount = 2,
                smsAskCount = 0,
                lastOsAskAtMillis = now - 7 * day,
                lastCardAtMillis = 0L,
                nextIntervalDays = 7,
                nowMillis = now,
            )
        )
        assertFalse(
            PermissionNudge.shouldShowSettingsCard(
                missingNotifications = true,
                missingSms = false,
                notificationAskCount = 1,
                smsAskCount = 0,
                lastOsAskAtMillis = now - 30 * day,
                lastCardAtMillis = 0L,
                nextIntervalDays = 7,
                nowMillis = now,
            )
        )
    }

    @Test
    fun stopsAfterMaxOrPermanentDeny() {
        assertFalse(
            PermissionNudge.shouldAsk(
                granted = false,
                askCount = PermissionNudge.MAX_ASKS,
                lastAskAtMillis = now - 30 * day,
                nowMillis = now,
                intervalDays = 7,
                canShowRationale = true,
            )
        )
        assertFalse(
            PermissionNudge.shouldAsk(
                granted = false,
                askCount = 1,
                lastAskAtMillis = now - 30 * day,
                nowMillis = now,
                intervalDays = 7,
                canShowRationale = false,
            )
        )
    }
}
