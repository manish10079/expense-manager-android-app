package com.mknlabs.expensetracker.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.mknlabs.expensetracker.data.repository.AuthRepositoryImpl
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.mknlabs.expensetracker.utils.GoogleAuthHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideGoogleAuthHelper(
        @ApplicationContext context: Context
    ): GoogleAuthHelper {
        return GoogleAuthHelper(context)
    }
}
