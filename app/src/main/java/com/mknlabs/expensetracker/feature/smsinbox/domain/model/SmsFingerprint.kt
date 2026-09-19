package com.mknlabs.expensetracker.feature.smsinbox.domain.model

import java.security.MessageDigest
import java.util.Locale

/**
 * Duplicate-prevention key for a detected SMS (Phase 6).
 *
 * A fingerprint is built from the **four** signals that together mean "this is the
 * same bank message", not one of them:
 *
 *  1. sender        — the bank/TRAI header,
 *  2. amount        — the parsed amount, so two different charges never collide,
 *  3. body          — whitespace-normalised and case-folded, because carriers and
 *                     OEMs re-deliver the same PDU with cosmetic differences,
 *  4. time bucket   — the receipt timestamp folded into a window.
 *
 * The time bucket is what makes this safe to enforce with a UNIQUE index. Hashing
 * content alone would swallow a legitimate second identical charge (two ₹100 coffees
 * from the same merchant) for the rest of time; hashing the raw timestamp alone would
 * let a carrier's redelivery a second later through as a new detection. Bucketing the
 * timestamp keeps redelivery inside one bucket while letting a genuine repeat an hour
 * later through as its own detection.
 *
 * Pure and side-effect free so it is unit-testable on the JVM.
 */
object SmsFingerprint {

    /**
     * How long one SMS event stays indistinguishable from a redelivery of itself.
     * Carriers re-send within seconds; real repeat charges are minutes apart at the
     * very least, which is why ten minutes is the default over a tighter window.
     */
    const val WINDOW_MILLIS: Long = 10L * 60L * 1000L

    /** Bucket used when a PDU carries no usable timestamp. */
    private const val UNKNOWN_TIME_BUCKET = -1L

    /**
     * Stable hex fingerprint for one detection. Deterministic for identical input —
     * which is exactly what the unique index relies on.
     */
    fun of(
        sender: String,
        body: String,
        amountMinor: Long,
        smsTimestamp: Long,
        windowMillis: Long = WINDOW_MILLIS
    ): String {
        val bucket = timeBucket(smsTimestamp, windowMillis)
        val material = buildString {
            append(normalize(sender))
            append('|')
            append(amountMinor)
            append('|')
            append(normalize(body))
            append('|')
            append(bucket)
        }
        return sha256Hex(material)
    }

    /**
     * The time component of the fingerprint. A non-positive or missing timestamp
     * folds to a single bucket so an unknown-time message still dedupes against
     * itself instead of being re-filed on every redelivery.
     */
    fun timeBucket(smsTimestamp: Long, windowMillis: Long = WINDOW_MILLIS): Long {
        if (smsTimestamp <= 0L || windowMillis <= 0L) return UNKNOWN_TIME_BUCKET
        return smsTimestamp / windowMillis
    }

    /**
     * Collapses whitespace and case so a redelivery that differs only in spacing or
     * capitalisation fingerprints identically.
     */
    private fun normalize(value: String): String =
        value.trim().replace(WHITESPACE, " ").lowercase(Locale.ROOT)

    private fun sha256Hex(material: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(material.toByteArray(Charsets.UTF_8))
        return buildString(bytes.size * 2) {
            for (byte in bytes) {
                val value = byte.toInt() and 0xFF
                if (value < 0x10) append('0')
                append(Integer.toHexString(value))
            }
        }
    }

    private val WHITESPACE = Regex("""\s+""")
}
