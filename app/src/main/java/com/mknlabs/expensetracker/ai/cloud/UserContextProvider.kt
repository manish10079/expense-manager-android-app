package com.mknlabs.expensetracker.ai.cloud

import android.content.Context
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import kotlinx.coroutines.flow.first
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User context for personalized Gemini AI parsing.
 * Contains only metadata — no transaction data (privacy safe).
 */
data class UserAiContext(
    val currency: String = "INR",
    val locale: String = "en-US",
    val topCategories: List<String> = listOf("Food", "Transport", "Shopping"),
    val topPaymentMethods: List<String> = listOf("UPI", "Card", "Cash")
)

/**
 * Provides user context for personalized Gemini prompts.
 *
 * Reads currency from local DataStore (not Firestore) — zero network cost.
 * Top categories/payment methods use smart defaults based on user's region.
 */
@Singleton
class UserContextProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Fetches user context for AI parsing.
     * All data comes from local storage — no Firestore reads needed.
     */
    suspend fun getUserContext(): UserAiContext {
        val settings = AppSettingsDataStore.getAppSettingsFlow(context)
            .first()

        val currencyId = settings.currencyId
        val currencyCode = resolveCurrencyCode(currencyId)
        val locale = resolveLocale(currencyId)

        return UserAiContext(
            currency = currencyCode,
            locale = locale,
            topCategories = getDefaultCategories(currencyId),
            topPaymentMethods = getDefaultPaymentMethods(currencyId)
        )
    }

    /**
     * Maps currencyId to ISO currency code for Gemini.
     */
    private fun resolveCurrencyCode(currencyId: Int): String {
        return when (currencyId) {
            1 -> "INR"   // India
            2 -> "USD"   // USA
            3 -> "GBP"   // UK
            4 -> "EUR"   // Europe
            5 -> "JPY"   // Japan
            6 -> "CNY"   // China
            7 -> "KRW"   // South Korea
            8 -> "AED"   // UAE
            9 -> "SAR"   // Saudi Arabia
            10 -> "SGD"  // Singapore
            11 -> "THB"  // Thailand
            12 -> "IDR"  // Indonesia
            13 -> "MYR"  // Malaysia
            14 -> "CHF"  // Switzerland
            15 -> "RUB"  // Russia
            16 -> "TRY"  // Turkey
            17 -> "CAD"  // Canada
            18 -> "AUD"  // Australia
            19 -> "BRL"  // Brazil
            20 -> "MXN"  // Mexico
            21 -> "ZAR"  // South Africa
            22 -> "EGP"  // Egypt
            23 -> "NGN"  // Nigeria
            24 -> "PKR"  // Pakistan
            25 -> "BDT"  // Bangladesh
            26 -> "LKR"  // Sri Lanka
            27 -> "NPR"  // Nepal
            else -> "USD"
        }
    }

    /**
     * Resolves locale based on currency region.
     */
    private fun resolveLocale(currencyId: Int): String {
        return when (currencyId) {
            1 -> "en-IN"   // India
            2 -> "en-US"   // USA
            3 -> "en-GB"   // UK
            4 -> "de-DE"   // Europe
            5 -> "ja-JP"   // Japan
            6 -> "zh-CN"   // China
            7 -> "ko-KR"   // South Korea
            8 -> "ar-AE"   // UAE
            9 -> "ar-SA"   // Saudi Arabia
            10 -> "en-SG"  // Singapore
            11 -> "th-TH"  // Thailand
            12 -> "id-ID"  // Indonesia
            13 -> "ms-MY"  // Malaysia
            14 -> "de-CH"  // Switzerland
            15 -> "ru-RU"  // Russia
            16 -> "tr-TR"  // Turkey
            17 -> "en-CA"  // Canada
            18 -> "en-AU"  // Australia
            19 -> "pt-BR"  // Brazil
            20 -> "es-MX"  // Mexico
            21 -> "en-ZA"  // South Africa
            22 -> "ar-EG"  // Egypt
            23 -> "en-NG"  // Nigeria
            24 -> "en-PK"  // Pakistan
            25 -> "bn-BD"  // Bangladesh
            26 -> "si-LK"  // Sri Lanka
            27 -> "ne-NP"  // Nepal
            else -> "en-US"
        }
    }

    /**
     * Default top categories based on region.
     * These are the most commonly used categories in each region.
     */
    private fun getDefaultCategories(currencyId: Int): List<String> {
        return when (currencyId) {
            1 -> listOf("Food", "Transport", "Shopping")     // India
            2 -> listOf("Food", "Shopping", "Entertainment") // USA
            3 -> listOf("Food", "Transport", "Bills")        // UK
            4 -> listOf("Food", "Transport", "Bills")        // Europe
            else -> listOf("Food", "Transport", "Shopping")
        }
    }

    /**
     * Default top payment methods based on region.
     */
    private fun getDefaultPaymentMethods(currencyId: Int): List<String> {
        return when (currencyId) {
            1 -> listOf("UPI", "Card", "Cash")    // India — UPI dominant
            2 -> listOf("Card", "Bank", "Cash")   // USA — Card dominant
            3 -> listOf("Card", "Bank", "Cash")   // UK
            4 -> listOf("Card", "Bank", "Cash")   // Europe
            else -> listOf("Card", "Bank", "Cash")
        }
    }
}
