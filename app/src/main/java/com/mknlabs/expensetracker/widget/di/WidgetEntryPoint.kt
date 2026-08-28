package com.mknlabs.expensetracker.widget.di

import android.content.Context
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import com.mknlabs.expensetracker.widget.updater.WidgetUpdater
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for widget and service code that cannot use @Inject.
 *
 * Widgets and services are not managed by Hilt's lifecycle, so we access
 * singleton dependencies through [EntryPointAccessors].
 *
 * Usage:
 * ```kotlin
 * val entryPoint = WidgetEntryPoint.get(context)
 * val transactionRepository = entryPoint.transactionRepository()
 * ```
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {

    fun transactionRepository(): TransactionRepository

    fun voiceParserRepository(): VoiceParserRepository

    fun transactionDao(): TransactionDao

    fun categoryDao(): CategoryDao

    fun paymentMethodDao(): PaymentMethodDao

    fun widgetUpdater(): WidgetUpdater

    companion object {

        fun get(context: Context): WidgetEntryPoint {
            return EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
        }
    }
}
