package com.mknlabs.expensetracker.di

import android.content.Context
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideExpenseTrackerDatabase(@ApplicationContext context: Context): ExpenseTrackerDatabase {
        return ExpenseTrackerDatabase.getInstance(context)
    }

    @Provides
    fun provideTransactionDao(database: ExpenseTrackerDatabase) = database.transactionDao()

    @Provides
    fun provideCategoryDao(database: ExpenseTrackerDatabase) = database.categoryDao()

    @Provides
    fun provideBudgetDao(database: ExpenseTrackerDatabase) = database.budgetDao()

    @Provides
    fun provideRecurringRuleDao(database: ExpenseTrackerDatabase) = database.recurringRuleDao()

    @Provides
    fun providePaymentMethodDao(database: ExpenseTrackerDatabase) = database.paymentMethodDao()
}