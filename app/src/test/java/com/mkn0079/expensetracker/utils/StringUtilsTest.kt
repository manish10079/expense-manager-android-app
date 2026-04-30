package com.mkn0079.expensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsTest {

    @Test
    fun `toTitleCase converts lower case name to title case`() {
        val input = "john doe"
        val expected = "John Doe"
        assertEquals(expected, input.toTitleCase())
    }

    @Test
    fun `toTitleCase converts upper case name to title case`() {
        val input = "JOHN DOE"
        val expected = "John Doe"
        assertEquals(expected, input.toTitleCase())
    }

    @Test
    fun `toTitleCase handles multiple spaces`() {
        val input = "john    doe"
        val expected = "John Doe"
        assertEquals(expected, input.toTitleCase())
    }

    @Test
    fun `toTitleCase handles empty string`() {
        val input = ""
        val expected = ""
        assertEquals(expected, input.toTitleCase())
    }

    @Test
    fun `toTitleCase handles blank string`() {
        val input = "   "
        val expected = "   "
        assertEquals(expected, input.toTitleCase())
    }

    @Test
    fun `toTitleCase handles mixed case`() {
        val input = "jOhN dOe"
        val expected = "John Doe"
        assertEquals(expected, input.toTitleCase())
    }
}
