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
    fun kannadaUsesKannadaDigits() {
        assertEquals("೧೨೩", localizeWesternDigits("123", "kn"))
        assertTrue(usesNativeDigits("kn"))
    }

    @Test
    fun tamilUsesTamilDigits() {
        assertEquals("௧௨௩", localizeWesternDigits("123", "ta"))
        assertTrue(usesNativeDigits("ta"))
    }

    @Test
    fun marathiUsesDevanagariDigits() {
        assertEquals("१२३", localizeWesternDigits("123", "mr"))
        assertTrue(usesNativeDigits("mr"))
    }

    @Test
    fun bengaliUsesBengaliDigits() {
        assertEquals("১২৩", localizeWesternDigits("123", "bn"))
        assertTrue(usesNativeDigits("bn"))
    }

    @Test
    fun malayalamUsesMalayalamDigits() {
        assertEquals("൧൨൩", localizeWesternDigits("123", "ml"))
        assertTrue(usesNativeDigits("ml"))
    }

    @Test
    fun teluguUsesTeluguDigits() {
        assertEquals("౧౨౩", localizeWesternDigits("123", "te"))
        assertTrue(usesNativeDigits("te"))
    }

    @Test
    fun assameseUsesBengaliAssameseDigits() {
        assertEquals("০১২", localizeWesternDigits("012", "as"))
        assertTrue(usesNativeDigits("as"))
    }

    @Test
    fun punjabiUsesGurmukhiDigits() {
        assertEquals("੧੨੩", localizeWesternDigits("123", "pa"))
        assertTrue(usesNativeDigits("pa"))
    }

    @Test
    fun gujaratiUsesGujaratiDigits() {
        assertEquals("૧૨૩", localizeWesternDigits("123", "gu"))
        assertTrue(usesNativeDigits("gu"))
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
