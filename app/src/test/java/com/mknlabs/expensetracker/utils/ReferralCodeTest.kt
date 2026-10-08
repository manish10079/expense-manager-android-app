package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ReferralCodeTest {

    @Test
    fun firstFourAndLastFourUppercase() {
        assertEquals("ABCDWXYZ", referralCodeFromUid("abcd1234wxyz"))
    }

    @Test
    fun shortUidUppercased() {
        assertEquals("ABC", referralCodeFromUid("abc"))
    }
}
