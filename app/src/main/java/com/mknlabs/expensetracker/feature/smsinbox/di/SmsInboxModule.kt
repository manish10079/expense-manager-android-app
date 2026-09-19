package com.mknlabs.expensetracker.feature.smsinbox.di

import com.mknlabs.expensetracker.feature.smsinbox.data.notification.AndroidSmsNotificationCleaner
import com.mknlabs.expensetracker.feature.smsinbox.data.repository.SmsInboxRepositoryImpl
import com.mknlabs.expensetracker.feature.smsinbox.data.repository.SmsTransactionWriterImpl
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxRepository
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsNotificationCleaner
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsTransactionWriter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the inbox repository.
 *
 * The DAO itself needs no provider here: `DetectedSmsNotificationDao` is an abstract
 * method on `ExpenseTrackerDatabase`, which Hilt already exposes through the existing
 * database module, so the inbox only has to declare its own abstraction.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SmsInboxModule {

    @Binds
    @Singleton
    abstract fun bindSmsInboxRepository(
        impl: SmsInboxRepositoryImpl
    ): SmsInboxRepository

    @Binds
    @Singleton
    abstract fun bindSmsTransactionWriter(
        impl: SmsTransactionWriterImpl
    ): SmsTransactionWriter

    @Binds
    @Singleton
    abstract fun bindSmsNotificationCleaner(
        impl: AndroidSmsNotificationCleaner
    ): SmsNotificationCleaner
}
