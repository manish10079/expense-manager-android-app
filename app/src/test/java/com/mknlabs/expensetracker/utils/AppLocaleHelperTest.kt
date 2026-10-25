package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AppLocaleHelperTest {

    @Test
    fun `areLocalesEquivalent matches identical locales`() {
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.ENGLISH, Locale.ENGLISH))
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("es"), Locale.forLanguageTag("es")))
    }

    @Test
    fun `areLocalesEquivalent matches Indonesian legacy code in and id`() {
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("in"), Locale.forLanguageTag("id")))
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("id"), Locale.forLanguageTag("in")))
    }

    @Test
    fun `areLocalesEquivalent matches Hebrew legacy code iw and he`() {
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("iw"), Locale.forLanguageTag("he")))
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("he"), Locale.forLanguageTag("iw")))
    }

    @Test
    fun `areLocalesEquivalent matches Portuguese Brazil`() {
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("pt-BR"), Locale.forLanguageTag("pt-BR")))
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("pt-BR"), Locale.forLanguageTag("pt")))
        assertTrue(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("pt"), Locale.forLanguageTag("pt-BR")))
    }

    @Test
    fun `areLocalesEquivalent returns false for distinct languages`() {
        assertFalse(AppLocaleHelper.areLocalesEquivalent(Locale.FRENCH, Locale.GERMAN))
        assertFalse(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("es"), Locale.forLanguageTag("it")))
    }

    @Test
    fun `areLocalesEquivalent returns false for conflicting country variants`() {
        assertFalse(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("pt-BR"), Locale.forLanguageTag("pt-PT")))
        assertFalse(AppLocaleHelper.areLocalesEquivalent(Locale.forLanguageTag("en-US"), Locale.forLanguageTag("en-GB")))
    }
}
