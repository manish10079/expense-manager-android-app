package com.mknlabs.expensetracker.benchmark

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMacrobenchmarkApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Free-vs-Pro ad-jank baselines (ADS_UI_JANK_FIX_PLAN Phase 0).
 *
 * Each journey is measured twice: forced FREE tier (ads enabled) and forced PRO tier
 * (ads disabled). The app under test reads the launch extras defined in
 * `:app` [BenchmarkHooks] (`benchmark_force_pro`, `benchmark_seed`) — the string keys
 * below must stay in sync with that class.
 *
 * Run (requires a connected device/emulator, release-quality numbers only on real hardware):
 * ```
 * ./gradlew :benchmark:connectedCheck \
 *   -P android.testInstrumentationRunnerArguments.class=com.mknlabs.expensetracker.benchmark.AdJankBenchmark
 * ```
 */
@OptIn(ExperimentalMacrobenchmarkApi::class)
@RunWith(AndroidJUnit4::class)
class AdJankBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private companion object {
        const val TARGET_PACKAGE = "com.mknlabs.expensetracker"
        const val ITERATIONS = 6
        const val SEED_NOTE_PREFIX = "Benchmark tx"

        // Keys must match :app BenchmarkHooks (com.mknlabs.expensetracker.benchmark.BenchmarkHooks).
        const val EXTRA_FORCE_PRO = "benchmark_force_pro"
        const val EXTRA_SEED = "benchmark_seed"

        /** Measured launches carry only the tier extra — seeding happens in the unmeasured
         *  setupBlock, so cold starts measured here have zero hook overhead.
         *
         *  FLAG_ACTIVITY_CLEAR_TASK is REQUIRED for the scrollTransactions journeys: the
         *  Macrobenchmark library defaults `startupMode` to null (NOT COLD — verified in
         *  MacrobenchmarkRule.kt 1.3.4), so `launchWithClearTask` is false and launches
         *  reuse the existing task. The app then restores its saved navigation state and
         *  reopens on the Transactions screen (where the previous iteration's setup left
         *  it) instead of Home — and "See All" doesn't exist there, so the journey always
         *  failed setup. CLEAR_TASK makes every launch start from a fresh task at Home.
         *  (StartupMode.COLD would also fix it, but its dropKernelPageCache() does a
         *  `setprop perf.drop_caches 3` that throws on non-rooted user builds.) */
        fun measureIntent(forcePro: Boolean): Intent = Intent().apply {
            setClassName(TARGET_PACKAGE, "$TARGET_PACKAGE.MainActivity")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(EXTRA_FORCE_PRO, forcePro)
        }

        /** Unmeasured setup launches also trigger one-time seeding + settings prep. */
        fun setupIntent(forcePro: Boolean): Intent = measureIntent(forcePro).apply {
            putExtra(EXTRA_SEED, true)
        }
    }

    // --- Tab switching ---------------------------------------------------

    @Test
    fun tabSwitch_free() = tabSwitchJourney(forcePro = false)

    @Test
    fun tabSwitch_pro() = tabSwitchJourney(forcePro = true)

    private fun tabSwitchJourney(forcePro: Boolean) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric(), StartupTimingMetric()),
        // MIUI blocks USB re-installs (INSTALL_FAILED_USER_RESTRICTED), and a failed
        // reinstall uninstalls the app. The benchmark APK is already installed and
        // compiled, so skip the reinstall/compile step entirely — this also keeps
        // free-vs-pro measurements perfectly comparable (identical install state).
        //
        // NB: this MUST be CompilationMode.Ignore(), NOT CompilationMode.None(). On user
        // builds pre-Android 14 (SDK < 34, not rooted) CompilationMode.None().shouldReset()
        // returns true, so the library performs a full `pm uninstall` + `pm install` of the
        // target app before every journey (CompilationMode.kt: resetAndCompile). On MIUI that
        // reinstall pops the "Install via USB" confirmation dialog (INSTALL_FAILED_USER_RESTRICTED)
        // and wipes the seeded DB + granted permissions, so the "See All" setup step always times
        // out and no benchmarkData.json is ever emitted. Ignore() has shouldReset() == false
        // (verified in the 1.3.4 library source) → no reinstall, no profile reset, identical
        // install state across free/pro journeys.
        compilationMode = CompilationMode.Ignore(),
        iterations = ITERATIONS,
        setupBlock = {
            prepareDevice()
            // Launch once per iteration so the app seeds data + skips onboarding (unmeasured).
            // COLD mode kills the process afterwards; the seeded DB/DataStore state persists.
            startActivityAndWait(intent = setupIntent(forcePro))
            device.waitForIdle()
        }
    ) {
        startActivityAndWait(intent = measureIntent(forcePro))
        device.waitForIdle()
        tapByDesc("Analytics")
        tapByDesc("Budget")
        tapByDesc("Calendar")
        tapByDesc("Home")
    }

    // --- Transactions list scroll ----------------------------------------

    @Test
    fun scrollTransactions_free() = scrollTransactionsJourney(forcePro = false)

    @Test
    fun scrollTransactions_pro() = scrollTransactionsJourney(forcePro = true)

    private fun scrollTransactionsJourney(forcePro: Boolean) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Ignore(),
        iterations = ITERATIONS,
        setupBlock = {
            prepareDevice()
            // Cold start the app, run the setup to seed db
            startActivityAndWait(intent = setupIntent(forcePro))
            openTransactionsList()
            // Kill the app process so the next iteration starts fresh (cold launch)
            // and doesn't restore to the Transactions screen.
            killProcess()
        }
    ) {
        startActivityAndWait(intent = measureIntent(forcePro))
        openTransactionsList()
        flingList(times = 4)
    }

    // --- Analytics scroll ------------------------------------------------

    @Test
    fun scrollAnalytics_free() = scrollAnalyticsJourney(forcePro = false)

    @Test
    fun scrollAnalytics_pro() = scrollAnalyticsJourney(forcePro = true)

    private fun scrollAnalyticsJourney(forcePro: Boolean) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Ignore(),
        iterations = ITERATIONS,
        setupBlock = {
            prepareDevice()
            startActivityAndWait(intent = setupIntent(forcePro))
            killProcess()
        }
    ) {
        startActivityAndWait(intent = measureIntent(forcePro))
        tapByDesc("Analytics")
        flingList(times = 4)
    }

    // --- Helpers ---------------------------------------------------------

    /**
     * Writes the current UI hierarchy to the test app's external files dir so failures can
     * be diagnosed from the device (pull via adb). Uses the benchmark's own UiAutomation
     * connection — safe to call inside a journey (unlike a separate `uiautomator dump`,
     * which would steal the accessibility connection and crash the benchmark).
     */
    private fun MacrobenchmarkScope.dumpHierarchyDiagnostic(name: String) {
        try {
            // Test APK context — MacrobenchmarkScope.context is internal, and we can only
            // write to our own scoped external dir from the benchmark process.
            val ctx = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().context
            val dir = ctx.getExternalFilesDir(null) ?: return
            val file = java.io.File(dir, "$name.xml")
            device.dumpWindowHierarchy(file)
            android.util.Log.w("BENCHDIAG", "Hierarchy dumped to ${file.absolutePath}")
        } catch (e: Exception) {
            android.util.Log.w("BENCHDIAG", "Hierarchy dump failed: $e")
        }
    }

    /**
     * Real-device hardening: the Macrobenchmark library does NOT wake the device or
     * dismiss the keyguard itself (verified in MacrobenchmarkScope source), so an idle
     * screen timeout would leave the app running behind the lockscreen and UiAutomator
     * would never find "See All". Wake + dismiss keyguard at the start of every setup
     * block so the app under test is actually visible.
     */
    private fun MacrobenchmarkScope.prepareDevice() {
        try {
            device.wakeUp()
        } catch (_: Exception) {
            // Already awake — ignore.
        }
        try {
            device.executeShellCommand("wm dismiss-keyguard")
        } catch (_: Exception) {
            // Keyguard already dismissed / not supported — ignore.
        }
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.openTransactionsList() {
        // Try up to 3 times to find and click the button to handle UI layout shifts and recompositions.
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
        if (!clicked) {
            dumpHierarchyDiagnostic("see-all-not-found")
            error("'See All' click failed after retries")
        }
        // Wait until a seeded transaction row is rendered (stable, populated list).
        check(device.wait(Until.hasObject(By.textContains(SEED_NOTE_PREFIX)), 10_000)) {
            "Seeded transactions never appeared in the list"
        }
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.tapByDesc(desc: String) {
        var clicked = false
        for (i in 1..5) {
            try {
                // Wait up to 5 seconds for the element to appear
                val node = device.wait(Until.findObject(By.desc(desc)), 5_000)
                    ?: error("Node with content-desc '$desc' not found")
                node.click()
                clicked = true
                break
            } catch (e: Exception) {
                // Wait and retry if the node goes stale or is covered during recomposition
                Thread.sleep(500)
            }
        }
        if (!clicked) {
            error("Failed to click node with content-desc '$desc' after retries")
        }
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
