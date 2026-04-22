package com.mkn0079.expensetracker.di

import com.mkn0079.expensetracker.data.repository.MonetizationRepositoryImpl
import com.mkn0079.expensetracker.domain.repository.MonetizationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonetizationModule {

    @Binds
    @Singleton
    abstract fun bindMonetizationRepository(
        monetizationRepositoryImpl: MonetizationRepositoryImpl
    ): MonetizationRepository
}
