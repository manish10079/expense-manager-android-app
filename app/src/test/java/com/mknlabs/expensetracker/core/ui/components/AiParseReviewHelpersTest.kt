package com.mknlabs.expensetracker.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.DateFormat
import java.util.Date

class AiParseReviewHelpersTest {

    @Test
    fun formatReviewDate_withExplicitTimestamp_formatsMediumDate() {
        val timestamp = 1_704_067_200_000L // 2024-01-01 00:00:00 UTC
        val expected = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
        val actual = formatReviewDate(timestamp)
        assertEquals(expected, actual)
        assertNotEquals("—", actual)
    }

    @Test
    fun formatReviewDate_withNullTimestamp_fallsBackToCurrentDate() {
        val before = System.currentTimeMillis()
        val result = formatReviewDate(null)
        val after = System.currentTimeMillis()

        assertNotEquals("—", result)
        assertTrue(result.isNotBlank())

        val expectedBefore = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(before))
        val expectedAfter = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(after))
        assertTrue(result == expectedBefore || result == expectedAfter)
    }

    @Test
    fun formatReviewTime_withExplicitTimestamp_formatsShortTime() {
        val timestamp = 1_704_067_200_000L
        val expected = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestamp))
        val actual = formatReviewTime(timestamp)
        assertEquals(expected, actual)
        assertNotEquals("—", actual)
    }

    @Test
    fun formatReviewTime_withNullTimestamp_fallsBackToCurrentTime() {
        val before = System.currentTimeMillis()
        val result = formatReviewTime(null)
        val after = System.currentTimeMillis()

        assertNotEquals("—", result)
        assertTrue(result.isNotBlank())

        val expectedBefore = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(before))
        val expectedAfter = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(after))
        assertTrue(result == expectedBefore || result == expectedAfter)
    }
}
