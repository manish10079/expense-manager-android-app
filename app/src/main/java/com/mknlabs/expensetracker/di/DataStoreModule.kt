package com.mknlabs.expensetracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mknlabs.expensetracker.data.local.appSettingsDataStore
import com.mknlabs.expensetracker.data.local.userProfileDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.appSettingsDataStore
    }

    @Provides
    @Singleton
    fun provideUserProfileDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.userProfileDataStore
    }
}
