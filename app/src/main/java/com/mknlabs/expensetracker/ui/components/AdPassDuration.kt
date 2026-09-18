package com.mknlabs.expensetracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R

/**
 * How the Remote Config ad-pass length splits into display units: 90 → 1 hour + 30 minutes.
 *
 * Exactly one of the three flags is true for any value produced by [adPassDurationParts]:
 * a single plural is used for the first two, the third joins both labels with
 * `label_duration_hours_minutes`.
 */
internal data class AdPassDurationParts(
    val hours: Int,
    val minutes: Int
) {
    /** "1 minute", "45 minutes". */
    val isMinutesOnly: Boolean get() = hours == 0

    /** "1 hour", "2 hours". */
    val isHoursOnly: Boolean get() = hours > 0 && minutes == 0

    /** "1 hour 30 minutes". */
    val isHoursAndMinutes: Boolean get() = hours > 0 && minutes > 0
}

/**
 * Splits the ad-pass length into display units. A non-positive Remote Config value is
 * clamped to one minute so a misconfiguration can never render an empty duration.
 */
internal fun adPassDurationParts(totalMinutes: Int): AdPassDurationParts {
    val safeMinutes = totalMinutes.coerceAtLeast(1)
    return AdPassDurationParts(hours = safeMinutes / 60, minutes = safeMinutes % 60)
}

/**
 * Renders the ad-pass length (`ad_pass_duration_minutes` in Remote Config) for display.
 *
 * Every string that promises the user a duration must go through here, otherwise the copy
 * keeps advertising the old length after the Remote Config value changes:
 * 1 → "1 minute", 45 → "45 minutes", 60 → "1 hour", 90 → "1 hour 30 minutes".
 */
@Composable
fun adPassDurationLabel(minutes: Int): String {
    val parts = adPassDurationParts(minutes)
    return when {
        parts.isMinutesOnly ->
            pluralStringResource(R.plurals.duration_minutes, parts.minutes, parts.minutes)

        parts.isHoursOnly ->
            pluralStringResource(R.plurals.duration_hours, parts.hours, parts.hours)

        else -> stringResource(
            R.string.label_duration_hours_minutes,
            pluralStringResource(R.plurals.duration_hours, parts.hours, parts.hours),
            pluralStringResource(R.plurals.duration_minutes, parts.minutes, parts.minutes)
        )
    }
}
