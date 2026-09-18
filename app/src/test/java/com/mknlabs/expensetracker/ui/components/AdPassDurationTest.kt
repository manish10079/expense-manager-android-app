package com.mknlabs.expensetracker.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [adPassDurationLabel] renders the Remote Config ad-pass length (`ad_pass_duration_minutes`).
 * The text itself comes from Android resources (`duration_minutes` / `duration_hours` plurals and
 * `label_duration_hours_minutes`), which a JVM unit test cannot read, so these tests pin the
 * plain-Kotlin half that decides the wording:
 *
 * | minutes | parts   | branch     | rendered label       |
 * |---------|---------|------------|----------------------|
 * | 1       | 0 h 1 m | minutesOnly| "1 minute"           |
 * | 45      | 0 h 45 m| minutesOnly| "45 minutes"         |
 * | 60      | 1 h 0 m | hoursOnly  | "1 hour"             |
 * | 90      | 1 h 30 m| combined   | "1 hour 30 minutes"  |
 */
class AdPassDurationTest {

    @Test
    fun `one minute selects the minutes plural`() {
        val parts = adPassDurationParts(1)

        assertEquals(AdPassDurationParts(hours = 0, minutes = 1), parts)
        assertTrue("1 minute must use the minutes plural", parts.isMinutesOnly)
    }

    @Test
    fun `forty five minutes selects the minutes plural`() {
        val parts = adPassDurationParts(45)

        assertEquals(AdPassDurationParts(hours = 0, minutes = 45), parts)
        assertTrue("45 minutes must use the minutes plural", parts.isMinutesOnly)
    }

    @Test
    fun `sixty minutes selects the hours plural`() {
        val parts = adPassDurationParts(60)

        assertEquals(AdPassDurationParts(hours = 1, minutes = 0), parts)
        assertTrue("60 minutes must render as one hour", parts.isHoursOnly)
    }

    @Test
    fun `ninety minutes joins the hours and minutes plurals`() {
        val parts = adPassDurationParts(90)

        assertEquals(AdPassDurationParts(hours = 1, minutes = 30), parts)
        assertTrue("90 minutes must render as an hour plus minutes", parts.isHoursAndMinutes)
    }

    @Test
    fun `every value selects exactly one label shape`() {
        listOf(1, 45, 59, 60, 61, 90, 120, 1_440).forEach { minutes ->
            val parts = adPassDurationParts(minutes)
            val selected = listOf(parts.isMinutesOnly, parts.isHoursOnly, parts.isHoursAndMinutes)

            assertEquals(
                "minutes=$minutes must resolve to exactly one label shape, got $parts",
                1,
                selected.count { it }
            )
        }
    }

    @Test
    fun `non positive values clamp to one minute instead of rendering nothing`() {
        // A misconfigured Remote Config value must never produce an empty duration.
        listOf(0, -5).forEach { minutes ->
            assertEquals(
                "minutes=$minutes should clamp to one minute",
                AdPassDurationParts(hours = 0, minutes = 1),
                adPassDurationParts(minutes)
            )
        }
    }

    @Test
    fun `whole hours never carry leftover minutes`() {
        listOf(120, 180, 1_440).forEach { minutes ->
            val parts = adPassDurationParts(minutes)

            assertEquals("minutes=$minutes should be whole hours", 0, parts.minutes)
            assertFalse(parts.isHoursAndMinutes)
        }
    }
}
