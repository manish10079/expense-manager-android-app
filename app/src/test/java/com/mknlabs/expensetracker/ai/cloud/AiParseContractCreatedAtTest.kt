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
}
