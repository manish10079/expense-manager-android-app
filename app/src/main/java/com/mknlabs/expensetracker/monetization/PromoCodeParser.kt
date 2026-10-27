package com.mknlabs.expensetracker.monetization

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Pulls a Pro Pass / promo code out of a Play install referrer or an https://…/p/{code} App Link.
 * Invalid junk is dropped so a poisoned referrer cannot fill the redeem field.
 */
object PromoCodeParser {

    private val codePattern = Regex("^[A-Z0-9][A-Z0-9_-]{2,31}$")

    const val PROMO_HOST = "expense-tracker-2ea00.web.app"
    const val PROMO_HOST_FIREBASEAPP = "expense-tracker-2ea00.firebaseapp.com"
    const val PROMO_PATH_PREFIX = "p"

    fun sanitize(raw: String?): String? {
        val code = raw?.trim()?.uppercase().orEmpty()
        return code.takeIf { codePattern.matches(it) }
    }

    fun fromReferrer(referrer: String): String? {
        if (referrer.isBlank()) return null
        val decoded = runCatching {
            URLDecoder.decode(referrer, StandardCharsets.UTF_8.name())
        }.getOrDefault(referrer)
        val params = linkedMapOf<String, String>()
        for (part in decoded.split('&')) {
            val eq = part.indexOf('=')
            if (eq <= 0) continue
            val key = part.substring(0, eq).trim().lowercase()
            val value = part.substring(eq + 1).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) {
                params[key] = value
            }
        }
        return sanitize(params["utm_content"] ?: params["promo"] ?: params["code"])
    }

    fun fromAppLink(host: String?, path: String?, queryCode: String?): String? {
        val fromQuery = sanitize(queryCode)
        val normalizedHost = host?.lowercase().orEmpty()
        val isPromoHost = normalizedHost == PROMO_HOST || normalizedHost == PROMO_HOST_FIREBASEAPP
        val segments = path.orEmpty().trim('/').split('/').filter { it.isNotEmpty() }
        if (isPromoHost && segments.firstOrNull() == PROMO_PATH_PREFIX) {
            val pathCode = segments.getOrNull(1)
            return sanitize(pathCode) ?: fromQuery
        }
        return fromQuery
    }
}
