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
 * The VoiceAddViewModel injects OfflineVoiceParser directly (not via interface)
 * so it can use the offline parser without Hilt qualifier issues.
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
