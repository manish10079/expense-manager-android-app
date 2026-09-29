package com.mknlabs.expensetracker.data.local.room

import androidx.room.ColumnInfo

/**
 * One row's id and the colour stored on it, and nothing else.
 *
 * The projection both DAOs return from `getStoredColors()`, which the seeder reads before it
 * rewrites the seeded rows. It is deliberately its own type rather than the full entity: the
 * seeder is allowed to rebuild every column of a seeded row from the constant, and the colour is
 * the single column it must not — so a query that can only return the colour is one that cannot
 * accidentally be used to reintroduce the rest.
 *
 * `colorHex` is nullable and stays nullable: null means "no colour of its own", which is the state
 * every seeded row is in until a user picks one, and it has to survive the rewrite as such.
 */
data class StoredColorRow(
    val id: Int,
    @ColumnInfo(name = "color_hex")
    val colorHex: String?
)
