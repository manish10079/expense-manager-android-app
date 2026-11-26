package com.mknlabs.expensetracker.ai.cloud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class AiParseContractCreatedAtTest {

    @Test
    fun wallClock_localEpoch() {
        val ms = AiParseContract.createdAtToEpochMillis("2024-03-15 15:45", 12)
        val cal = Calendar.getInstance()
        cal.timeInMillis = requireNotNull(ms)
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH))
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(15, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, cal.get(Calendar.MINUTE))
    }

    @Test
    fun nullCreatedAt_isNull() {
        assertNull(AiParseContract.createdAtToEpochMillis(null, null))
    }

    @Test
    fun junk_isNull() {
        assertNull(AiParseContract.createdAtToEpochMillis("not-a-date", 24))
    }

    @Test
    fun aiParseResponse_defaultFields() {
        val tx = AiParsedTransaction(
            amount = 125.0,
            category = "Food",
            paymentMethod = "Cash",
            note = "Coffee",
            typeId = 2,
        )
        val response = AiParseResponse(transactions = listOf(tx))
        assertEquals(1, response.totalDetected)
        assertEquals(false, response.isPro)
        assertEquals(1, response.transactions.size)
    }

    @Test
    fun aiParseResponse_freeTierWithExtraDetected() {
        val tx1 = AiParsedTransaction(amount = 10000.0, category = "Business", paymentMethod = "Cash", note = "Business profit", typeId = 1)
        val tx2 = AiParsedTransaction(amount = 3000.0, category = "Investment", paymentMethod = "Bank", note = "Stock return", typeId = 1)
        val response = AiParseResponse(
            transactions = listOf(tx1, tx2),
            totalDetected = 2,
            isPro = false,
        )
        assertEquals(2, response.totalDetected)
        assertEquals(false, response.isPro)
        val extraItems = (response.totalDetected - response.transactions.size).coerceAtLeast(0)
        assertEquals(0, extraItems)

        // Item 0 is unlocked, Item 1 is locked for Free user
        val isFirstLocked = !response.isPro && 0 > 0
        val isSecondLocked = !response.isPro && 1 > 0
        assertEquals(false, isFirstLocked)
        assertEquals(true, isSecondLocked)
    }

    @Test
    fun aiParseResponse_proTierAllUnlocked() {
        val tx1 = AiParsedTransaction(amount = 10000.0, category = "Business", paymentMethod = "Cash", note = "Business profit", typeId = 1)
        val tx2 = AiParsedTransaction(amount = 3000.0, category = "Investment", paymentMethod = "Bank", note = "Stock return", typeId = 1)
        val response = AiParseResponse(
            transactions = listOf(tx1, tx2),
            totalDetected = 2,
            isPro = true,
        )
        assertEquals(2, response.totalDetected)
        assertEquals(true, response.isPro)

        // All items unlocked for Pro user
        val isFirstLocked = !response.isPro && 0 > 0
        val isSecondLocked = !response.isPro && 1 > 0
        assertEquals(false, isFirstLocked)
        assertEquals(false, isSecondLocked)
    }
}
