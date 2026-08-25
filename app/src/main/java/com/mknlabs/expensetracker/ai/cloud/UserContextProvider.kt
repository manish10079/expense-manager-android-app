package com.mknlabs.expensetracker.ai.cloud

import android.content.Context
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User context for personalized Gemini AI parsing.
 * Contains metadata computed from local Room DB — no privacy risk.
 */
data class UserAiContext(
    val currency: String = "INR",
    val locale: String = "en-US",
    val allExpenseCategories: List<String> = emptyList(),
    val allIncomeCategories: List<String> = emptyList(),
    val allPaymentMethods: List<String> = emptyList(),
    val topCategories: List<String> = emptyList(),
    val topPaymentMethods: List<String> = emptyList()
)

/**
 * Provides user context for personalized Gemini prompts.
 *
 * All data comes from local Room DB and DataStore — zero network cost.
 * Queries are fast (~15ms total) since they run on indexed local tables.
 */
@Singleton
class UserContextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDao: CategoryDao,
    private val paymentMethodDao: PaymentMethodDao
) {
    // 60 days window for "frequently used" calculation
    private val sixtyDaysMillis = 60L * 24 * 60 * 60 * 1000
    private val sinceMillis = System.currentTimeMillis() - sixtyDaysMillis

    /**
     * Fetches user context for AI parsing.
     * All data comes from local Room DB — no Firestore reads needed.
     */
    suspend fun getUserContext(): UserAiContext {
        // 1. Currency from DataStore
        val settings = AppSettingsDataStore.getAppSettingsFlow(context).first()
        val currencyCode = resolveCurrencyCode(settings.currencyId)
        val locale = resolveLocale(settings.currencyId)

        // 2. All active categories from Room DB
        val allCategories = categoryDao.getActiveCategories()
        val expenseCategories = allCategories
            .filter { it.transactionTypeId == 2 }
            .map { it.name }
        val incomeCategories = allCategories
            .filter { it.transactionTypeId == 1 }
            .map { it.name }

        // 3. All active payment methods from Room DB
        val allPaymentMethods = paymentMethodDao.getActivePaymentMethods()
            .map { it.name }

        // 4. Top 3 most-used categories (from actual transaction history)
        val topExpenseCategories = categoryDao.getFrequentlyUsedCategories(
            transactionTypeId = 2,
            limit = 3,
            sinceMillis = sinceMillis
        ).map { it.name }
        val topIncomeCategories = categoryDao.getFrequentlyUsedCategories(
            transactionTypeId = 1,
            limit = 3,
            sinceMillis = sinceMillis
        ).map { it.name }
        val topCategories = (topExpenseCategories + topIncomeCategories).distinct().take(3)

        // 5. Top 3 most-used payment methods (from actual transaction history)
        val topPaymentMethods = paymentMethodDao.getFrequentlyUsedPaymentMethods(
            limit = 3,
            sinceMillis = sinceMillis
        ).map { it.name }

        return UserAiContext(
            currency = currencyCode,
            locale = locale,
            allExpenseCategories = expenseCategories,
            allIncomeCategories = incomeCategories,
            allPaymentMethods = allPaymentMethods,
            topCategories = topCategories,
            topPaymentMethods = topPaymentMethods
        )
    }

    /**
     * Maps currencyId to ISO currency code for Gemini.
     */
    private fun resolveCurrencyCode(currencyId: Int): String {
        return when (currencyId) {
            1 -> "INR"; 2 -> "USD"; 3 -> "GBP"; 4 -> "EUR"
            5 -> "JPY"; 6 -> "CNY"; 7 -> "KRW"; 8 -> "AED"
            9 -> "SAR"; 10 -> "SGD"; 11 -> "THB"; 12 -> "IDR"
            13 -> "MYR"; 14 -> "CHF"; 15 -> "RUB"; 16 -> "TRY"
            17 -> "CAD"; 18 -> "AUD"; 19 -> "BRL"; 20 -> "MXN"
            21 -> "ZAR"; 22 -> "EGP"; 23 -> "NGN"; 24 -> "PKR"
            25 -> "BDT"; 26 -> "LKR"; 27 -> "NPR"; else -> "USD"
        }
    }

    /**
     * Resolves locale based on currency region.
     */
    private fun resolveLocale(currencyId: Int): String {
        return when (currencyId) {
            1 -> "en-IN"; 2 -> "en-US"; 3 -> "en-GB"; 4 -> "de-DE"
            5 -> "ja-JP"; 6 -> "zh-CN"; 7 -> "ko-KR"; 8 -> "ar-AE"
            9 -> "ar-SA"; 10 -> "en-SG"; 11 -> "th-TH"; 12 -> "id-ID"
            13 -> "ms-MY"; 14 -> "de-CH"; 15 -> "ru-RU"; 16 -> "tr-TR"
            17 -> "en-CA"; 18 -> "en-AU"; 19 -> "pt-BR"; 20 -> "es-MX"
            21 -> "en-ZA"; 22 -> "ar-EG"; 23 -> "en-NG"; 24 -> "en-PK"
            25 -> "bn-BD"; 26 -> "si-LK"; 27 -> "ne-NP"; else -> "en-US"
        }
    }
}
