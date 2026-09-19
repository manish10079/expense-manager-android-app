package com.mknlabs.expensetracker.feature.smsinbox.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The fingerprint is the duplicate-prevention key, so these tests pin the two
 * ways it can go wrong in production: letting a redelivery through as a new
 * detection, and swallowing a genuine repeat charge as a duplicate.
 */
class SmsFingerprintTest {

    private val timestamp = 1_700_000_000_000L

    @Test
    fun `identical messages fingerprint identically`() {
        val first = SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp)
        val second = SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp)

        assertEquals(first, second)
    }

    @Test
    fun `a carrier redelivery inside the window is the same detection`() {
        val original = SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp)
        // Carriers re-send seconds (even a couple of minutes) later.
        val redelivery = SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp + 90_000L)

        assertEquals(original, redelivery)
    }

    @Test
    fun `an identical repeat charge outside the window is a new detection`() {
        val first = SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp)
        // Same sender, same text, same amount — but hours later. Two ₹100 coffees
        // from the same merchant must both be captured.
        val later = SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp + 6L * 60L * 60L * 1000L)

        assertNotEquals(first, later)
    }

    @Test
    fun `cosmetic whitespace and case differences do not defeat deduplication`() {
        val plain = SmsFingerprint.of(SENDER, "Rs.100 debited", AMOUNT, timestamp)
        // Same sender and message — only spacing and capitalisation differ.
        val noisy = SmsFingerprint.of(" $SENDER ", "Rs.100   DEBITED", AMOUNT, timestamp)

        assertEquals(plain, noisy)
    }

    @Test
    fun `a different amount or sender is never a duplicate`() {
        val base = SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp)

        assertNotEquals(base, SmsFingerprint.of(SENDER, BODY, AMOUNT + 1, timestamp))
        assertNotEquals(base, SmsFingerprint.of("OTHER-BANK", BODY, AMOUNT, timestamp))
        assertNotEquals(base, SmsFingerprint.of(SENDER, "$BODY ref 2", AMOUNT, timestamp))
    }

    @Test
    fun `a missing timestamp still deduplicates against itself`() {
        val first = SmsFingerprint.of(SENDER, BODY, AMOUNT, 0L)
        val second = SmsFingerprint.of(SENDER, BODY, AMOUNT, 0L)

        assertEquals(first, second)
        // ...but it must not collide with a real timestamped delivery.
        assertNotEquals(first, SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp))
    }

    @Test
    fun `timestamp buckets advance with the window`() {
        val window = SmsFingerprint.WINDOW_MILLIS
        // Start exactly on a bucket boundary, otherwise the "one window later"
        // assertion would depend on where inside a bucket the sample happened to land.
        val boundary = window * 2_833_333L

        assertEquals(
            SmsFingerprint.timeBucket(boundary, window),
            SmsFingerprint.timeBucket(boundary + window - 1, window)
        )
        assertNotEquals(
            SmsFingerprint.timeBucket(boundary, window),
            SmsFingerprint.timeBucket(boundary + window, window)
        )
        assertEquals(-1L, SmsFingerprint.timeBucket(0L, window))
    }

    @Test
    fun `fingerprint is a stable hex digest`() {
        val fingerprint = SmsFingerprint.of(SENDER, BODY, AMOUNT, timestamp)

        assertEquals(64, fingerprint.length)
        assertTrue("expected lowercase hex", fingerprint.all { it in "0123456789abcdef" })
    }

    private companion object {
        const val SENDER = "VM-HDFCBK"
        const val BODY = "Rs.450 debited from A/c XX1234 to VPA swiggy@ybl"
        const val AMOUNT = 45_000L
    }
}
