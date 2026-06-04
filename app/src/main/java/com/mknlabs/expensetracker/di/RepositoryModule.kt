package com.mknlabs.expensetracker.di

import android.content.Context
import com.mknlabs.expensetracker.data.legacy.LegacyImportRepository as LegacyImportRepositoryImpl
import com.mknlabs.expensetracker.data.repository.AppPreferencesRepositoryImpl
import com.mknlabs.expensetracker.data.repository.BudgetRepository as BudgetRepositoryImpl
import com.mknlabs.expensetracker.data.repository.CategoryRepository as CategoryRepositoryImpl
import com.mknlabs.expensetracker.data.repository.DataManagementRepository as DataManagementRepositoryImpl
import com.mknlabs.expensetracker.data.repository.PaymentMethodRepository as PaymentMethodRepositoryImpl
import com.mknlabs.expensetracker.data.repository.RecurringRuleRepository as RecurringRuleRepositoryImpl
import com.mknlabs.expensetracker.data.repository.TransactionRepository as TransactionRepositoryImpl
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.domain.repository.BudgetRepository
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.DataManagementRepository
import com.mknlabs.expensetracker.domain.repository.LegacyImportRepository
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.domain.repository.RecurringRuleRepository
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.domain.repository.SecurityRepository
import com.mknlabs.expensetracker.data.repository.SecurityRepositoryImpl
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import com.mknlabs.expensetracker.data.repository.ConfigurationRepositoryImpl
import com.mknlabs.expensetracker.domain.repository.SyncRepository
import com.mknlabs.expensetracker.data.repository.SyncRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    @Provides
    @Singleton
    fun provideConfigurationRepository(): ConfigurationRepository {
        return ConfigurationRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        configRepository: ConfigurationRepository
    ): SyncRepository {
        return SyncRepositoryImpl(context, firestore, firebaseAuth, configRepository)
    }
}
