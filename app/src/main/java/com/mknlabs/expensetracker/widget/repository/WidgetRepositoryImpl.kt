package com.mknlabs.expensetracker.widget.repository

import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

internal interface WidgetRepository {

    suspend fun getTodayExpenseMinor(): Long

    suspend fun getCurrencySymbol(): String
}

@Singleton
internal class WidgetRepositoryImpl @Inject constructor(
    private val database: ExpenseTrackerDatabase
) : WidgetRepository {

    private val transactionDao = database.transactionDao()

    override suspend fun getTodayExpenseMinor(): Long = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().time)
        transactionDao.getTodayExpenseMinor(todayStr)
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.IO) {
        "₹"
    }
}
