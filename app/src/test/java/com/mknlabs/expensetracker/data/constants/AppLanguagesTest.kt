package com.mknlabs.expensetracker.data.constants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLanguagesTest {

    @Test
    fun systemRowHasNoGlyph() {
        val system = supportedAppLanguages.first { it.code == SYSTEM_LANGUAGE_CODE }
        assertNull(system.glyph)
    }

    @Test
    fun everyLanguageHasAGlyph() {
        val missing = supportedAppLanguages
            .filter { it.code != SYSTEM_LANGUAGE_CODE && it.glyph.isNullOrBlank() }
            .map { it.englishName }
        assertEquals(emptyList<String>(), missing)
    }

    @Test
    fun glyphsAreOneLetterEach() {
        supportedAppLanguages.mapNotNull { it.glyph }.forEach { glyph ->
            assertNotNull(glyph)
            assertTrue(
                "glyph \"$glyph\" is more than one character",
                glyph.codePointCount(0, glyph.length) == 1
            )
        }
    }

    @Test
    fun noTwoLanguagesShareAGlyph() {
        val duplicates = supportedAppLanguages
            .mapNotNull { it.glyph }
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
        assertEquals(emptyMap<String, Int>(), duplicates)
    }

    @Test
    fun glyphsUseTheLanguagesOwnScript() {
        // A Latin letter on an Indian-language row would be the globe icon again, just typed.
        val expected = mapOf(
            "en" to "A",
            "hi" to "अ",
            "or" to "ଅ",
            "kn" to "ಕ",
            "ta" to "த",
            "bn" to "ব",
            "mr" to "म",
            "ml" to "മ",
            "te" to "త",
            "as" to "অ",
            "pa" to "ਪ",
            "gu" to "ગ",
            "ja" to "日",
            "ko" to "한",
            "ru" to "Р"
        )
        expected.forEach { (code, glyph) ->
            assertEquals(
                "glyph for $code",
                glyph,
                supportedAppLanguages.first { it.code == code }.glyph
            )
        }
    }
}
