package com.mknlabs.expensetracker.di

import com.mknlabs.expensetracker.ai.CategoryPredictor
import com.mknlabs.expensetracker.domain.repository.CategoryPredictorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [CategoryPredictorRepository] to [CategoryPredictor].
 *
 * To swap in a future cloud-powered implementation (e.g. Gemini-backed
 * [com.mknlabs.expensetracker.ai.cloud.GeminiCategoryPredictor]), change
 * the binding here based on [com.mknlabs.expensetracker.models.UserTier].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CategoryPredictorModule {

    @Binds
    @Singleton
    abstract fun bindCategoryPredictorRepository(
        impl: CategoryPredictor
    ): CategoryPredictorRepository
}
