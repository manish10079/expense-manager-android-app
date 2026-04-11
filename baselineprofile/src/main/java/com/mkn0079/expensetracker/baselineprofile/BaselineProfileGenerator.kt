package com.mkn0079.expensetracker.baselineprofile

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @OptIn(ExperimentalBaselineProfilesApi::class)
    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "com.mkn0079.expensetracker",
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()

            // Cover the main bottom navigation destinations and a simple drill-in path.
            device.waitForIdle()
        }
    }
}
