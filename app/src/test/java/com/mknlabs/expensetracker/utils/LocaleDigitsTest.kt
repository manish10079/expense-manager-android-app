package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocaleDigitsTest {

    @Test
    fun hindiUsesDevanagariDigits() {
        assertEquals("१२३", localizeWesternDigits("123", "hi"))
        assertTrue(usesNativeDigits("hi"))
    }

    @Test
    fun odiaUsesOdiaDigits() {
        assertEquals("୧୨୩", localizeWesternDigits("123", "or"))
        assertTrue(usesNativeDigits("or"))
    }

    @Test
    fun otherLanguagesKeepWesternDigits() {
        assertEquals("123", localizeWesternDigits("123", "en"))
        assertEquals("123", localizeWesternDigits("123", "ja"))
        assertFalse(usesNativeDigits("en"))
    }

    @Test
    fun nonDigitsUnchanged() {
        assertEquals("१२+३", localizeWesternDigits("12+3", "hi"))
    }
}
