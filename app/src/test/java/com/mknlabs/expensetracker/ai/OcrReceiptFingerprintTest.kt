package com.mknlabs.expensetracker.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OcrReceiptFingerprintTest {

    @Test
    fun identicalReceiptsHashIdentically() {
        val body = "Paytm\nOrder Successful\n₹226"
        assertEquals(OcrReceiptFingerprint.of(body), OcrReceiptFingerprint.of(body))
    }

    @Test
    fun whitespaceAndCaseDoNotDefeatDedup() {
        val a = OcrReceiptFingerprint.of("Paytm  ₹226")
        val b = OcrReceiptFingerprint.of("  paytm\n\n₹226  ")
        assertEquals(a, b)
    }

    @Test
    fun differentAmountsDoNotCollide() {
        assertNotEquals(
            OcrReceiptFingerprint.of("₹226 Paytm"),
            OcrReceiptFingerprint.of("₹227 Paytm"),
        )
    }
}
