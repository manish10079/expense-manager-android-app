package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM tests for the Item 20 magic-link deep-link guard ([DeepLinkUtils]).
 * Covers the real Firebase magic-link shape plus lookalike/bypass attempts that
 * a naive string-prefix check would wrongly accept.
 */
class DeepLinkUtilsTest {

    // --- valid links -------------------------------------------------------

    @Test
    fun valid_magic_link_with_standard_params() {
        assertTrue(
            DeepLinkUtils.isValidMagicLink(
                "https://expense-tracker-2ea00.web.app/login?oobCode=abc&apiKey=k&mode=signIn"
            )
        )
    }

    @Test
    fun valid_magic_link_with_extra_path_segments() {
        // Prefix semantics mirror Android's pathPrefix intent-filter matching.
        assertTrue(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app/login/"))
        assertTrue(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app/loginx?mode=signIn"))
    }

    @Test
    fun valid_magic_link_case_insensitive_host() {
        // DNS hosts are case-insensitive; Java URI normalizes (and we ignore case).
        assertTrue(DeepLinkUtils.isValidMagicLink("https://EXPENSE-TRACKER-2EA00.WEB.APP/login"))
    }

    // --- rejected lookalikes / bypass attempts ------------------------------

    @Test
    fun wrong_host_is_rejected() {
        assertFalse(DeepLinkUtils.isValidMagicLink("https://evil.com/login?oobCode=abc"))
    }

    @Test
    fun host_suffix_lookalike_is_rejected() {
        // The exact-host check is what a naive startsWith() guard would miss.
        assertFalse(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app.evil.com/login"))
    }

    @Test
    fun host_swapped_via_userinfo_is_rejected() {
        // The userinfo trick: everything before @ is userinfo, so the real host
        // here is "evil.com" — the exact-host comparison must reject it.
        assertFalse(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app@evil.com/login"))
    }

    @Test
    fun at_symbol_after_path_stays_on_our_host() {
        // A '/' terminates the authority, so "…/login@evil.com" is still OUR host
        // with a (harmless) '@' in the path — accepted, same as a browser would
        // resolve it. Firebase's action-code validation remains the auth gate.
        assertTrue(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app/login@evil.com"))
    }

    @Test
    fun http_scheme_is_rejected() {
        assertFalse(DeepLinkUtils.isValidMagicLink("http://expense-tracker-2ea00.web.app/login"))
    }

    @Test
    fun missing_path_is_rejected() {
        assertFalse(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app"))
    }

    @Test
    fun wrong_path_is_rejected() {
        assertFalse(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app/register"))
    }

    @Test
    fun bare_string_without_scheme_is_rejected() {
        assertFalse(DeepLinkUtils.isValidMagicLink("expense-tracker-2ea00.web.app/login"))
    }

    @Test
    fun other_schemes_are_rejected() {
        assertFalse(DeepLinkUtils.isValidMagicLink("file:///etc/passwd"))
        assertFalse(DeepLinkUtils.isValidMagicLink("intent://expense-tracker-2ea00.web.app/login"))
    }

    @Test
    fun empty_and_malformed_input_are_rejected() {
        assertFalse(DeepLinkUtils.isValidMagicLink(""))
        assertFalse(DeepLinkUtils.isValidMagicLink("not a url"))
        assertFalse(DeepLinkUtils.isValidMagicLink("https://"))
        // Opaque / no-authority forms must fail the check, not throw.
        assertFalse(DeepLinkUtils.isValidMagicLink("https:foo"))
        assertFalse(DeepLinkUtils.isValidMagicLink("https:///login"))
    }

    @Test
    fun port_variant_stays_on_our_host() {
        // A port still resolves to our host (consistent with the port-less
        // intent-filter) — accepted; Firebase validates the action code.
        assertTrue(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app:443/login"))
    }

    @Test
    fun backslash_lookalike_is_rejected() {
        // Browsers treat \ as /; the intent-filter would never match this, and
        // java.net.URI rejects the illegal backslash — fail-closed either way.
        assertFalse(DeepLinkUtils.isValidMagicLink("https://expense-tracker-2ea00.web.app\\@evil.com/login"))
    }
}
