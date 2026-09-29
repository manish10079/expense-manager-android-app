package com.mknlabs.expensetracker.data.local.room

import androidx.room.withTransaction
import android.content.Context
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.data.constants.countryCodeMap
import com.mknlabs.expensetracker.data.local.room.entities.CountryCodeEntity

object ExpenseTrackerDatabaseInitializer {

    suspend fun initialize(context: Context) {
        initialize(ExpenseTrackerDatabase.getInstance(context))
    }

    /**
     * Rewrites the seeded categories, payment methods and country codes from the constants.
     *
     * Runs on every launch, which is the point: it is how a release adds a category or corrects a
     * name without a migration. `upsertAll` is replace-on-primary-key, so each seeded row is
     * rebuilt wholesale — with exactly one exception.
     *
     * ## The colour is carried over, and that is not an optimisation
     *
     * `color_hex` is the only column of a seeded row the user owns. Before this carried it, a
     * colour they had chosen for a built-in category was wiped on the *next* launch and not before,
     * because the write that killed it happened at startup rather than at the moment of choosing —
     * which is the worst shape a data-loss bug can take. It presents as "my colours reset
     * sometimes", it reproduces only across a restart, and nothing about the picker, the repository
     * or the screen looks wrong.
     *
     * So the stored colours are read first and copied onto the rows being written. A row with no
     * stored colour stays null and keeps resolving from the palette by id, which is still the
     * ordinary case for all 27 seeded categories and 6 payment methods.
     *
     * ## Why the database is a parameter
     *
     * The colour surviving a restart is the whole contract, and a restart is exactly what a test
     * cannot perform. Taking the database instead of looking it up lets a test do the next best
     * thing — seed, recolour, seed again against the same store — which is the same sequence of
     * writes the app performs across two launches. `initialize(context)` is the app's entry point
     * and stays a one-liner over this.
     */
    internal suspend fun initialize(database: ExpenseTrackerDatabase) {
        database.withTransaction {
            val storedCategoryColors = database.categoryDao().getStoredColors().toColorMap()
            database.categoryDao().upsertAll(
                categoryMap.values.map { category ->
                    // The constant's own colour is always null, so this is simply "whatever the user
                    // stored, or nothing" — the palette is not consulted here and never needs to be.
                    category.toEntity().copy(colorHex = storedCategoryColors[category.id])
                }
            )

            val storedPaymentColors = database.paymentMethodDao().getStoredColors().toColorMap()
            database.paymentMethodDao().upsertAll(
                paymentTypeMap.values.map { paymentMethod ->
                    paymentMethod.toEntity().copy(colorHex = storedPaymentColors[paymentMethod.id])
                }
            )

            database.countryCodeDao().upsertAll(countryCodeMap.values.map { CountryCodeEntity.fromDomain(it) })
        }
    }
}

/**
 * The stored colours by row id.
 *
 * Absent and stored-null both mean "no colour of its own", so the caller cannot tell them apart —
 * which is right here, because the seeder treats them identically: it writes null either way.
 */
private fun List<StoredColorRow>.toColorMap(): Map<Int, String?> = associate { it.id to it.colorHex }
