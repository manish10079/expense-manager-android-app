package com.mkn0079.expensetracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization if needed
    }
}