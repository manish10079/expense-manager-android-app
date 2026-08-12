package com.mknlabs.expensetracker.utils

import java.net.URI

/**
 * Pure, JVM-testable validation for the magic-link (email sign-in) deep link.
 * Security plan **Item 20**: the manifest intent-filter and Firebase's own
 * action-code validation are the first two gates, but the app code must also
 * re-verify what it is handed before completing a sign-in.
 *
 * Uses [URI] with an **exact** host comparison rather than a string
 * `startsWith(...)` prefix check: a naive prefix match would accept lookalike
 * hosts such as `expense-tracker-2ea00.web.app.evil.com`. These constants must
 * stay in sync with the `AndroidManifest.xml` intent-filter
 * (`https://expense-tracker-2ea00.web.app/login`).
 */
object DeepLinkUtils {

    const val MAGIC_LINK_HOST = "expense-tracker-2ea00.web.app"
    const val MAGIC_LINK_PATH_PREFIX = "/login"

    /**
     * True only for `https` URLs on our exact host whose path starts with our
     * prefix (prefix semantics mirror Android's `pathPrefix` intent-filter
     * matching). Any other scheme, host, missing path, or malformed input is
     * rejected — fail-closed.
     */
    fun isValidMagicLink(link: String): Boolean {
        val uri = try {
            URI(link)
        } catch (e: Exception) {
            return false
        }
        // Constant-first comparison: java.net.URI can parse an input with a null
        // scheme or host (e.g. opaque `https:foo`), and a null receiver must fail
        // the check — never NPE inside a security guard.
        return "https".equals(uri.scheme, ignoreCase = true) &&
            MAGIC_LINK_HOST.equals(uri.host, ignoreCase = true) &&
            (uri.path ?: "").startsWith(MAGIC_LINK_PATH_PREFIX)
    }
}
