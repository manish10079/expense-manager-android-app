package com.mknlabs.expensetracker.di

import com.mknlabs.expensetracker.data.legacy.LegacyImportRepository as LegacyImportRepositoryImpl
import com.mknlabs.expensetracker.data.repository.AppPreferencesRepositoryImpl
import com.mknlabs.expensetracker.data.repository.BudgetRepository as BudgetRepositoryImpl
import com.mknlabs.expensetracker.data.repository.CategoryRepository as CategoryRepositoryImpl
import com.mknlabs.expensetracker.data.repository.DataManagementRepository as DataManagementRepositoryImpl
import com.mknlabs.expensetracker.data.repository.GoalRepositoryImpl
import com.mknlabs.expensetracker.data.repository.PaymentMethodRepository as PaymentMethodRepositoryImpl
import com.mknlabs.expensetracker.data.repository.RecurringRuleRepository as RecurringRuleRepositoryImpl
import com.mknlabs.expensetracker.data.repository.TransactionRepository as TransactionRepositoryImpl
import com.mknlabs.expensetracker.data.repository.SecurityRepositoryImpl
import com.mknlabs.expensetracker.data.repository.ConfigurationRepositoryImpl
import com.mknlabs.expensetracker.data.repository.ProPassRepositoryImpl
import com.mknlabs.expensetracker.data.repository.SyncRepositoryImpl
import com.mknlabs.expensetracker.data.repository.CountryCodeRepositoryImpl
import com.mknlabs.expensetracker.data.repository.FcmTokenRepositoryImpl
import com.mknlabs.expensetracker.data.repository.FeedbackRepositoryImpl
import com.mknlabs.expensetracker.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        impl: FeedbackRepositoryImpl
    ): FeedbackRepository

    @Binds
    @Singleton
    abstract fun bindCountryCodeRepository(
        impl: CountryCodeRepositoryImpl
    ): CountryCodeRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindAppPreferencesRepository(
        impl: AppPreferencesRepositoryImpl
    ): AppPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindPaymentMethodRepository(
        impl: PaymentMethodRepositoryImpl
    ): PaymentMethodRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindRecurringRuleRepository(
        impl: RecurringRuleRepositoryImpl
    ): RecurringRuleRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        impl: GoalRepositoryImpl
    ): GoalRepository

    @Binds
    @Singleton
    abstract fun bindDataManagementRepository(
        impl: DataManagementRepositoryImpl
    ): DataManagementRepository

    @Binds
    @Singleton
    abstract fun bindLegacyImportRepository(
        impl: LegacyImportRepositoryImpl
    ): LegacyImportRepository

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(
        impl: SecurityRepositoryImpl
    ): SecurityRepository

    @Binds
    @Singleton
    abstract fun bindConfigurationRepository(
        impl: ConfigurationRepositoryImpl
    ): ConfigurationRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): SyncRepository

    @Binds
    @Singleton
    abstract fun bindFcmTokenRepository(
        impl: FcmTokenRepositoryImpl
    ): FcmTokenRepository

    @Binds
    @Singleton
    abstract fun bindProPassRepository(
        impl: ProPassRepositoryImpl
    ): ProPassRepository
}
