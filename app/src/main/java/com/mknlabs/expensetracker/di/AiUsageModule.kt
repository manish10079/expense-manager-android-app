package com.mknlabs.expensetracker.di

import android.content.Context
import com.mknlabs.expensetracker.ai.AiUsageDataStore
import com.mknlabs.expensetracker.ai.aiUsageDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides [AiUsageDataStore] for the AI usage tracker.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiUsageModule {

    @Provides
    @AiUsageDataStore
    @Singleton
    fun provideAiUsageDataStore(
        @ApplicationContext context: Context
    ) = context.aiUsageDataStore
}
