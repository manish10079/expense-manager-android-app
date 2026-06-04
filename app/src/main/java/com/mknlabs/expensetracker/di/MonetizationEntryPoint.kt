package com.mknlabs.expensetracker.di

import com.mknlabs.expensetracker.monetization.AdsCoordinator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MonetizationEntryPoint {
    fun adsCoordinator(): AdsCoordinator
}
