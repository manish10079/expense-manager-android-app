package com.mknlabs.expensetracker.di

import android.content.Context
import com.mknlabs.expensetracker.ai.PaymentMethodOverrideStore
import com.mknlabs.expensetracker.ai.PaymentMethodPredictor
import com.mknlabs.expensetracker.data.local.PaymentMethodLearningDataStore
import com.mknlabs.expensetracker.data.local.PaymentMethodLearningStore
import com.mknlabs.expensetracker.data.local.paymentMethodLearningDataStore
import com.mknlabs.expensetracker.domain.repository.PaymentMethodPredictorRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides [PaymentMethodLearningStore] and binds
 * [PaymentMethodPredictorRepository] to [PaymentMethodPredictor].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentMethodPredictorModule {

    @Binds
    @Singleton
    abstract fun bindPaymentMethodPredictorRepository(
        impl: PaymentMethodPredictor
    ): PaymentMethodPredictorRepository

    @Binds
    @Singleton
    abstract fun bindPaymentMethodOverrideStore(
        impl: PaymentMethodLearningStore
    ): PaymentMethodOverrideStore

    companion object {
        @Provides
        @PaymentMethodLearningDataStore
        @Singleton
        fun providePaymentMethodLearningDataStore(
            @ApplicationContext context: Context
        ) = context.paymentMethodLearningDataStore
    }
}
