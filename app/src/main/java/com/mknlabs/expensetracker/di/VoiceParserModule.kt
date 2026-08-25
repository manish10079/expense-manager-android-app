package com.mknlabs.expensetracker.di

import com.mknlabs.expensetracker.ai.offline.OfflineVoiceParser
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import com.mknlabs.expensetracker.domain.repository.VoiceParserType
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module that binds [VoiceParserRepository] to the appropriate implementation.
 *
 * - "offline" → [OfflineVoiceParser] (free, on-device, rule-based)
 * - "gemini" → [com.mknlabs.expensetracker.ai.cloud.GeminiVoiceParser] (cloud, daily-limited)
 *
 * The VoiceAddViewModel decides which to use based on AiUsageTracker limits.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceParserModule {

    @Binds
    @Named("offline")
    @Singleton
    abstract fun bindOfflineVoiceParser(
        impl: OfflineVoiceParser
    ): VoiceParserRepository
}
