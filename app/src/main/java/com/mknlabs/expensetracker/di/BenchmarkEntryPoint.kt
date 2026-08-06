package com.mknlabs.expensetracker.di

import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point used only by the benchmark seed hook ([com.mknlabs.expensetracker.benchmark.BenchmarkHooks])
 * to bulk-insert sample transactions via the DAO. Mirrors [MonetizationEntryPoint].
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface BenchmarkEntryPoint {
    fun transactionDao(): TransactionDao
}
