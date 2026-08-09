package com.mknlabs.expensetracker.baselineprofile

import android.content.Intent
import androidx.benchmark.macro.ExperimentalMacrobenchmarkApi
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline profile generator for the Expense Tracker app (ADS_UI_JANK_FIX_PLAN Phase 5 §8.1).
 *
 * Covers the **ad-enabled (free-tier) path** so free users get the same AOT treatment as Pro:
 * 1. Startup (MainActivity → Home).
 * 2. Ad-enabled bottom-nav tab journey: Home → Analytics → Budget → Calendar → Home.
 * 3. Transactions list scroll (the every-5th-row ad slot + recycled NativeAdView path).
 * 4. Analytics scroll (locked premium cards + mid-list ad slot).
 *
 * The launch intent carries the same `:app` `BenchmarkHooks` extras the Macrobenchmark journeys
 * use (`benchmark_seed` skips onboarding/permission prompts and bulk-seeds 180 transactions;
 * `benchmark_force_pro = false` forces the FREE tier so ads are composed). The collector runs
 * against the app's `benchmark` build type (where `BuildConfig.BUILD_TYPE == "benchmark"` makes
 * the hooks active), so the seeded data and tier forcing behave exactly like the benchmark runs.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @OptIn(ExperimentalMacrobenchmarkApi::class)
    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "com.mknlabs.expensetracker",
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait(intent = launchIntent())

            // Startup profile block above covers MainActivity / Home startup.
            device.waitForIdle()

            // --- Ad-enabled tab journey (free tier, ads on) ---
            tapByDesc("Analytics")
            tapByDesc("Budget")
            tapByDesc("Calendar")
            tapByDesc("Home")

            // --- Transactions scroll (ad slot every 5th row) ---
            openTransactionsList()
            flingList(times = 4)

            // The Transactions list is a full-screen screen (opened via "See All")
            // with no bottom nav — go back to Home before the Analytics journey.
            device.pressBack()
            device.waitForIdle()

            // --- Analytics scroll (locked premium cards + ad slot) ---
            tapByDesc("Analytics")
            flingList(times = 4)
        }
    }

    /**
     * Free-tier (ads enabled) launch with seeding. Keys must match
     * `:app` [com.mknlabs.expensetracker.benchmark.BenchmarkHooks].
     */
    private fun launchIntent(): Intent = Intent().apply {
        setClassName(
            "com.mknlabs.expensetracker",
            "com.mknlabs.expensetracker.MainActivity"
        )
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        putExtra("benchmark_force_pro", false) // FREE tier — ads composed
        putExtra("benchmark_seed", true)       // seed transactions + skip onboarding
    }

    private fun MacrobenchmarkScope.openTransactionsList() {
        var clicked = false
        for (i in 1..3) {
            try {
                val seeAll = device.wait(Until.findObject(By.text("See All")), 5_000)
                    ?: error("'See All' not found on Home screen")
                seeAll.click()
                clicked = true
                break
            } catch (e: Exception) {
                // Wait briefly and retry if StaleObjectException or click failure occurs.
                Thread.sleep(500)
            }
        }
        check(clicked) { "'See All' click failed after retries" }
        // Wait until a seeded transaction row is rendered (stable, populated list).
        check(device.wait(Until.hasObject(By.textContains("Benchmark tx")), 10_000)) {
            "Seeded transactions never appeared in the list"
        }
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.tapByDesc(desc: String) {
        var clicked = false
        for (i in 1..5) {
            try {
                val node = device.wait(Until.findObject(By.desc(desc)), 5_000)
                    ?: error("Node with content-desc '$desc' not found")
                node.click()
                clicked = true
                break
            } catch (e: Exception) {
                // Wait and retry if the node goes stale during recomposition.
                Thread.sleep(500)
            }
        }
        check(clicked) { "Failed to click node with content-desc '$desc' after retries" }
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.flingList(times: Int) {
        repeat(times) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.72).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.28).toInt(),
                15
            )
            device.waitForIdle()
        }
    }
}
