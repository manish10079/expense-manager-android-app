package com.mknlabs.expensetracker.di

import com.mknlabs.expensetracker.ai.offline.OfflineVoiceParser
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [VoiceParserRepository] to [OfflineVoiceParser].
 *
 * When a cloud-powered Gemini parser is added (Pro tier), create
 * [GeminiVoiceParser] and swap the binding here based on [UserTier].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceParserModule {

    @Binds
    @Singleton
    abstract fun bindVoiceParserRepository(
        impl: OfflineVoiceParser
    ): VoiceParserRepository
}
