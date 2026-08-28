package com.mknlabs.expensetracker.widget.di

import com.mknlabs.expensetracker.widget.updater.WidgetUpdater
import com.mknlabs.expensetracker.widget.updater.WidgetUpdaterImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object WidgetModule {

    @Provides
    @Singleton
    fun provideWidgetUpdater(): WidgetUpdater = WidgetUpdaterImpl()
}
