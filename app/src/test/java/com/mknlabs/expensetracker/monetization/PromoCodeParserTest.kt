package com.mknlabs.expensetracker.monetization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PromoCodeParserTest {

    @Test
    fun referrerUtmContent() {
        assertEquals(
            "SAVE20",
            PromoCodeParser.fromReferrer("utm_source=firebase&utm_content=SAVE20"),
        )
    }

    @Test
    fun referrerUrlEncoded() {
        assertEquals(
            "SAVE20",
            PromoCodeParser.fromReferrer("utm_source%3Dfirebase%26utm_content%3DSAVE20"),
        )
    }

    @Test
    fun referrerPromoKey() {
        assertEquals("PRO_PASS", PromoCodeParser.fromReferrer("promo=pro_pass"))
    }

    @Test
    fun organicReferrerHasNoCode() {
        assertNull(PromoCodeParser.fromReferrer("utm_source=google-play&utm_medium=organic"))
    }

    @Test
    fun rejectsShortOrJunk() {
        assertNull(PromoCodeParser.sanitize("ab"))
        assertNull(PromoCodeParser.sanitize("bad code"))
        assertNull(PromoCodeParser.fromAppLink("evil.com", "/p/SAVE20", null))
    }

    @Test
    fun appLinkPath() {
        assertEquals(
            "SAVE20",
            PromoCodeParser.fromAppLink(
                host = "expense-tracker-2ea00.web.app",
                path = "/p/SAVE20",
                queryCode = null,
            ),
        )
    }
}
