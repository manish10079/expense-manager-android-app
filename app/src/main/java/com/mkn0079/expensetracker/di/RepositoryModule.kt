package com.mkn0079.expensetracker.di

import android.content.Context
import com.mkn0079.expensetracker.data.legacy.LegacyImportRepository as LegacyImportRepositoryImpl
import com.mkn0079.expensetracker.data.repository.AppPreferencesRepositoryImpl
import com.mkn0079.expensetracker.data.repository.BudgetRepository as BudgetRepositoryImpl
import com.mkn0079.expensetracker.data.repository.CategoryRepository as CategoryRepositoryImpl
import com.mkn0079.expensetracker.data.repository.DataManagementRepository as DataManagementRepositoryImpl
import com.mkn0079.expensetracker.data.repository.PaymentMethodRepository as PaymentMethodRepositoryImpl
import com.mkn0079.expensetracker.data.repository.RecurringRuleRepository as RecurringRuleRepositoryImpl
import com.mkn0079.expensetracker.data.repository.TransactionRepository as TransactionRepositoryImpl
import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository
import com.mkn0079.expensetracker.domain.repository.BudgetRepository
import com.mkn0079.expensetracker.domain.repository.CategoryRepository
import com.mkn0079.expensetracker.domain.repository.DataManagementRepository
import com.mkn0079.expensetracker.domain.repository.LegacyImportRepository
import com.mkn0079.expensetracker.domain.repository.PaymentMethodRepository
import com.mkn0079.expensetracker.domain.repository.RecurringRuleRepository
import com.mkn0079.expensetracker.domain.repository.TransactionRepository
import com.mkn0079.expensetracker.domain.repository.SecurityRepository
import com.mkn0079.expensetracker.data.repository.SecurityRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTransactionRepository(
        @ApplicationContext context: Context
    ): TransactionRepository {
        return TransactionRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(
        @ApplicationContext context: Context
    ): AppPreferencesRepository {
        return AppPreferencesRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        @ApplicationContext context: Context
    ): CategoryRepository {
        return CategoryRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePaymentMethodRepository(
        @ApplicationContext context: Context
    ): PaymentMethodRepository {
        return PaymentMethodRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        @ApplicationContext context: Context
    ): BudgetRepository {
        return BudgetRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideRecurringRuleRepository(
        @ApplicationContext context: Context
    ): RecurringRuleRepository {
        return RecurringRuleRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDataManagementRepository(
        @ApplicationContext context: Context
    ): DataManagementRepository {
        return DataManagementRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideLegacyImportRepository(
        @ApplicationContext context: Context
    ): LegacyImportRepository {
        return LegacyImportRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideSecurityRepository(
        @ApplicationContext context: Context
    ): SecurityRepository {
        return SecurityRepositoryImpl(context)
    }
}
