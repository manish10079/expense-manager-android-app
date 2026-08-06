package com.mknlabs.expensetracker.benchmark

import android.content.Context
import android.content.Intent
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity
import com.mknlabs.expensetracker.di.BenchmarkEntryPoint
import com.mknlabs.expensetracker.models.SyncState
import dagger.hilt.EntryPoints
import kotlinx.coroutines.runBlocking
import java.util.UUID

/**
 * Benchmark-only hooks for the `:benchmark` Macrobenchmark module (ADS_UI_JANK_FIX_PLAN Phase 0).
 *
 * **Production safety:** every call site is gated by `BuildConfig.BUILD_TYPE == "benchmark"`
 * in the app. `BuildConfig.BUILD_TYPE` is a compile-time constant, so R8 constant-folds the
 * gate to `false` in release/debug and **removes this class entirely from those APKs** — it
 * exists only in the non-debuggable `benchmark` build type that Macrobenchmark measures.
 * Even where present, the hooks are inert unless the app is launched with the
 * [EXTRA_FORCE_PRO] / [EXTRA_SEED] intent extras, which normal app code never sets.
 *
 * The two extras drive the free-vs-pro journeys:
 * - `EXTRA_FORCE_PRO = true`  → Pro simulation (ads off, all features granted).
 * - `EXTRA_FORCE_PRO = false` → Free simulation (ads on, locked premium UI).
 * - `EXTRA_SEED = true`       → skip onboarding/permission prompts and bulk-seed
 *   sample transactions so list-scroll journeys have real content.
 *
 * Note: [prepare] intentionally blocks the main thread (runBlocking) for determinism. The
 * benchmark launches it only from the unmeasured setupBlock, so measured cold starts are
 * unaffected; the already-seeded early return keeps even setupBlock launches cheap.
 */
object BenchmarkHooks {

    const val EXTRA_FORCE_PRO = "benchmark_force_pro"
    const val EXTRA_SEED = "benchmark_seed"

    /** Null = normal runtime behavior. True/False force the tier for benchmark runs. */
    @Volatile
    var forcePro: Boolean? = null

    @Volatile
    var seedRequested: Boolean = false

    fun readExtras(intent: Intent?) {
        if (intent?.hasExtra(EXTRA_FORCE_PRO) == true) {
            forcePro = intent.getBooleanExtra(EXTRA_FORCE_PRO, false)
        }
        if (intent?.getBooleanExtra(EXTRA_SEED, false) == true) {
            seedRequested = true
        }
    }

    /**
     * Prepares the benchmark device state (blocks startup; only ever runs when launched with
     * [EXTRA_SEED]):
     * 1. App settings — skip onboarding and permission prompts so automation is deterministic.
     * 2. Ensure system categories/payment methods exist (foreign-key requirement).
     * 3. Bulk-seed sample transactions.
     *
     * Idempotent: once transactions exist, everything (including the settings write) is
     * skipped, so repeated launches in the benchmark loop are cheap and measured cold starts
     * carry no hook overhead (the benchmark only passes EXTRA_SEED in the unmeasured
     * setupBlock anyway).
     */
    fun prepare(context: Context) {
        if (!seedRequested) return
        seedRequested = false

        runBlocking {
            val dao: TransactionDao = EntryPoints
                .get(context.applicationContext, BenchmarkEntryPoint::class.java)
                .transactionDao()

            // Always write benchmark-friendly settings (cheap, idempotent) so EVERY setup
            // launch is deterministic regardless of prior device state: no onboarding, no
            // permission prompts, no app-lock overlay, no notification setup. The previous
            // code only wrote settings when seeding, so a pre-seeded DB could still leave
            // e.g. the app lock or onboarding enabled.
            AppSettingsDataStore.updateAppSettings(context) {
                it.copy(
                    showOnboardingScreen = false,
                    smartSmsPrompted = true,
                    notificationsEnabled = false,
                    isAutoBackupEnabled = false,
                    isCloudSyncEnabled = false,
                    appLockEnabled = false
                )
            }

            if (dao.countAll() > 0) return@runBlocking // already seeded

            ExpenseTrackerDatabaseInitializer.initialize(context)

            val now = System.currentTimeMillis()
            val entities = (0 until SEED_COUNT).map { i ->
                // 60s spacing keeps every seed inside the current calendar day (and month),
                // so the default MONTHLY Transactions filter shows all rows.
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    note = "$SEED_NOTE_PREFIX $i",
                    amountMinor = 100L * (i % 97 + 1),
                    occurredAt = now - i * 60_000L,
                    createdAt = now - i * 60_000L,
                    updatedAt = now,
                    transactionTypeId = if (i % 7 == 0) 1 else 2,
                    categoryId = (i % 20) + 1,
                    paymentMethodId = 1,
                    isDeleted = false,
                    syncState = SyncState.SYNCED
                )
            }
            dao.upsertAll(entities)
        }
    }

    const val SEED_NOTE_PREFIX = "Benchmark tx"
    private const val SEED_COUNT = 180
}
