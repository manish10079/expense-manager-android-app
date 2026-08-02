package com.mknlabs.expensetracker.monetization

/**
 * Unique identifiers for features that can be gated behind paywalls or ads.
 */
enum class Feature(val id: String, val displayName: String) {
    SEARCH_TRANSACTIONS("search_transactions", "Search"),
    ADVANCED_ANALYTICS("advanced_analytics", "Detailed Analytics"),
    RECURRING_RULES("recurring_rules", "Recurring Expenses"),
    RECURRING_RULE_EDIT("recurring_rule_edit", "Edit Recurring Rules"),
    AUTO_BACKUP("auto_backup", "Auto Backup"),
    DATA_EXPORT("data_export", "Data Export"),
    BUDGET_CUSTOM_MONTH("budget_custom_month", "Custom Budget Month"),
    BUDGET_INSIGHTS("budget_insights", "Budget Insights"),
    AUTO_LOCK_SETTING("auto_lock_setting", "Auto Lock Duration"),
    SCRAMBLED_PIN_KEYPAD("scrambled_pin_keypad", "Scrambled PIN Keypad"),
    CALENDAR_YEAR_VIEW("calendar_year_view", "Calendar Year View"),
    CALENDAR_DIRECT_MONTH_PICKER("calendar_direct_month_picker", "Calendar Month Picker"),
    CALENDAR_DIRECT_YEAR_PICKER("calendar_direct_year_picker", "Calendar Year Picker"),
    PRIVACY_PROTECTION("privacy_protection", "Privacy Features"), 
    BIOMETRIC_LOCK("biometric_lock", "Biometric Lock"),
    CARD_CUSTOMIZATION("card_customization", "Card Customization"),
    ADVANCED_SEARCH_SCOPE("advanced_search_scope", "Advanced Search"),
    DASHBOARD_PRIVACY_MODE("dashboard_privacy_mode", "Privacy Mode"),
    SMART_PRIVACY_MODE("smart_privacy_mode", "Smart Privacy (Auto-Blur)"),
    ANALYTICS_PERIOD_YEAR("analytics_period_year", "Yearly Analytics"),
    ANALYTICS_CUSTOM_RANGE("analytics_custom_range", "Custom Range Analytics"),
    ANALYTICS_CATEGORY_BREAKDOWN("analytics_category_breakdown", "Category Breakdown"),
    ANALYTICS_PAYMENT_BREAKDOWN("analytics_payment_breakdown", "Payment Mode Breakdown"),
    ANALYTICS_TOP_SPENDING("analytics_top_spending", "Top Spending Insights"),
    ANALYTICS_SMART_TIPS("analytics_smart_tips", "Smart Spending Tips"),
    TRANSACTION_COUNT("transaction_count", "Transaction Statistics"),
    AD_FREE_GLOBAL("ad_free_global", "Ad-Free Experience")
}

/**
 * The Master Registry for all app feature monetization policies.
 * 
 * Scalable Design:
 * - If a feature is mapped to an AccessLevel, that level applies to the whole feature.
 * - If a feature is mapped to a Map<String, AccessLevel>, individual options are gated.
 */
object FeatureRegistry {
    
    private val registry = mapOf<Feature, Any>(
        Feature.SEARCH_TRANSACTIONS to AccessLevel.FREE, // Search button is now free
        Feature.ADVANCED_SEARCH_SCOPE to AccessLevel.AD_SUPPORTED, // Searching by Category/Wallet is gated
        Feature.DASHBOARD_PRIVACY_MODE to AccessLevel.FREE,
        Feature.SMART_PRIVACY_MODE to AccessLevel.FREE,
        Feature.ADVANCED_ANALYTICS to AccessLevel.PREMIUM,
        Feature.RECURRING_RULES to AccessLevel.PREMIUM,
        Feature.RECURRING_RULE_EDIT to AccessLevel.AD_SUPPORTED,
        Feature.AUTO_BACKUP to mapOf(
            "1" to AccessLevel.PREMIUM,
            "7" to AccessLevel.FREE,
            "15" to AccessLevel.FREE,
            "30" to AccessLevel.FREE,
            "custom" to AccessLevel.PREMIUM
        ),
        Feature.DATA_EXPORT to AccessLevel.AD_SUPPORTED,
        Feature.TRANSACTION_COUNT to AccessLevel.AD_SUPPORTED,
        Feature.BUDGET_CUSTOM_MONTH to AccessLevel.AD_SUPPORTED,
        Feature.BUDGET_INSIGHTS to AccessLevel.AD_SUPPORTED,
        Feature.SCRAMBLED_PIN_KEYPAD to AccessLevel.AD_SUPPORTED,
        Feature.CALENDAR_YEAR_VIEW to AccessLevel.AD_SUPPORTED,
        Feature.CALENDAR_DIRECT_MONTH_PICKER to AccessLevel.AD_SUPPORTED,
        Feature.CALENDAR_DIRECT_YEAR_PICKER to AccessLevel.AD_SUPPORTED,
        Feature.ANALYTICS_PERIOD_YEAR to AccessLevel.AD_SUPPORTED,
        Feature.ANALYTICS_CUSTOM_RANGE to AccessLevel.AD_SUPPORTED,
        Feature.ANALYTICS_CATEGORY_BREAKDOWN to AccessLevel.AD_SUPPORTED,
        Feature.ANALYTICS_PAYMENT_BREAKDOWN to AccessLevel.AD_SUPPORTED,
        Feature.ANALYTICS_TOP_SPENDING to AccessLevel.AD_SUPPORTED,
        Feature.ANALYTICS_SMART_TIPS to AccessLevel.AD_SUPPORTED,
        
        // Strategic Option Gating for Auto Lock durations
        Feature.AUTO_LOCK_SETTING to mapOf(
            "0" to AccessLevel.FREE,          // Immediate
            "1" to AccessLevel.FREE,          // 1 minute buffer
            "5" to AccessLevel.AD_SUPPORTED,   // Convenience (1-hour pass)
            "10" to AccessLevel.AD_SUPPORTED,
            "15" to AccessLevel.AD_SUPPORTED,
            "20" to AccessLevel.PREMIUM,       // Power User Perk
            "25" to AccessLevel.PREMIUM,
            "30" to AccessLevel.PREMIUM,
            "35" to AccessLevel.PREMIUM,
            "40" to AccessLevel.PREMIUM,
            "45" to AccessLevel.PREMIUM,
            "50" to AccessLevel.PREMIUM,
            "55" to AccessLevel.PREMIUM,
            "60" to AccessLevel.PREMIUM,
            "custom" to AccessLevel.PREMIUM    // Ultimate Personalization
        ),
        
        // Gating for Transaction Card Customization
        Feature.CARD_CUSTOMIZATION to mapOf(
            "showIncomeExpenseLabels" to AccessLevel.FREE,
            "showTransactionDate" to AccessLevel.FREE,
            "showCategoryIcon" to AccessLevel.FREE,
            "showCategoryLabel" to AccessLevel.FREE,        // Aligned with category icon — same concept
            "showTransactionTime" to AccessLevel.PREMIUM,
            "showDateSeparators" to AccessLevel.PREMIUM,
            "showPaymentMethod" to AccessLevel.PREMIUM,
            "showTransactionListSummaries" to AccessLevel.PREMIUM
        ),
        
        Feature.PRIVACY_PROTECTION to AccessLevel.PREMIUM,
        Feature.BIOMETRIC_LOCK to AccessLevel.FREE
    )

    /**
     * Resolves the access level for a feature or a specific option within a feature.
     */
    fun getAccessLevel(feature: Feature, optionId: String? = null): AccessLevel {
        val entry = registry[feature] ?: return AccessLevel.FREE
        
        return if (optionId != null && entry is Map<*, *>) {
            (entry[optionId] as? AccessLevel) ?: AccessLevel.FREE
        } else if (entry is AccessLevel) {
            entry
        } else {
            AccessLevel.FREE
        }
    }
}
