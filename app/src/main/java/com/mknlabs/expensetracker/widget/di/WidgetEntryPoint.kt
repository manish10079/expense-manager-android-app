package com.mknlabs.expensetracker.widget.di

import android.content.Context
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.FundDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.data.local.room.dao.TagDao
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.widget.updater.WidgetUpdater
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {

    fun transactionRepository(): TransactionRepository

    fun transactionDao(): TransactionDao

    fun categoryDao(): CategoryDao

    fun paymentMethodDao(): PaymentMethodDao

    fun tagDao(): TagDao

    fun fundDao(): FundDao

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
