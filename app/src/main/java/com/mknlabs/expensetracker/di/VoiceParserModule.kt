package com.mknlabs.expensetracker.di

import com.mknlabs.expensetracker.ai.cloud.FirebaseGeminiParser
import com.mknlabs.expensetracker.ai.offline.OfflineVoiceParser
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module that binds VoiceParserRepository implementations.
 *
 * - @Named("offline"): OfflineVoiceParser (always available, no network)
 * - @Named("gemini"): FirebaseGeminiParser (needs internet + Firebase AI Logic)
 * - Default: OfflineVoiceParser (used when no qualifier specified)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceParserModule {

    @Binds
    @Singleton
    @Named("offline")
    abstract fun bindOfflineParser(
        impl: OfflineVoiceParser
    ): VoiceParserRepository

    @Binds
    @Singleton
    @Named("gemini")
    abstract fun bindGeminiParser(
        impl: FirebaseGeminiParser
    ): VoiceParserRepository

    @Binds
    @Singleton
    abstract fun bindDefaultParser(
        impl: OfflineVoiceParser
    ): VoiceParserRepository
}
