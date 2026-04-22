package com.mkn0079.expensetracker.monetization

enum class AccessLevel {
    FREE,
    PREMIUM,
    AD_SUPPORTED
}

sealed class AccessStatus {
    object Granted : AccessStatus()
    object DeniedPremium : AccessStatus()
    object DeniedAd : AccessStatus()
}
