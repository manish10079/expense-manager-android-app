package com.mkn0079.expensetracker.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.mkn0079.expensetracker.data.repository.AuthRepositoryImpl
import com.mkn0079.expensetracker.domain.repository.AuthRepository
import com.mkn0079.expensetracker.utils.GoogleAuthHelper
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
