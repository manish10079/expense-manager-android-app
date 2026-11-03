package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R

internal const val PAGE_LANGUAGE = 0
internal const val PAGE_TRACK = 1
internal const val PAGE_SECURE = 2
internal const val PAGE_ANALYTICS = 3
internal const val PAGE_PREMIUM = 4
internal const val PAGE_AUTH = 5
internal const val PAGE_SETUP = 6
internal const val PAGE_WELCOME_BACK = 7
internal const val PAGE_PROMO = 8
internal const val MARKETING_FIRST = PAGE_TRACK
internal const val MARKETING_LAST = PAGE_PREMIUM

internal data class OnboardingPage(
    val title: String,
    val description: String,
    val actionLabel: String,
    val accentedText: String? = null,
    val titleFontSize: TextUnit = 42.sp,
    val titleLineHeight: TextUnit = 48.sp,
    val supportingContent: (@Composable () -> Unit)? = null,
    val illustration: @Composable (BoxScope.() -> Unit)
)

@Composable
internal fun rememberOnboardingPages(): List<OnboardingPage> = listOf(
    OnboardingPage(
        title = stringResource(R.string.title_language_settings),
        description = "",
        actionLabel = stringResource(R.string.label_apply_language),
        illustration = { }
    ),
    OnboardingPage(
        title = stringResource(R.string.title_track_expensesneasily),
        description = stringResource(R.string.desc_log_daily_spending),
        actionLabel = stringResource(R.string.label_next),
        illustration = { ExpenseCardIllustration() }
    ),
    OnboardingPage(
        title = stringResource(R.string.title_secure_private),
        description = stringResource(R.string.desc_financial_data_secure),
        actionLabel = stringResource(R.string.label_next),
        illustration = { SecureTrackerIllustration() }
    ),
    OnboardingPage(
        title = stringResource(R.string.title_visual_analytics),
        description = stringResource(R.string.desc_visualize_spending),
        actionLabel = stringResource(R.string.label_next),
        illustration = { AnalyticsIllustration() }
    ),
    OnboardingPage(
        title = stringResource(R.string.title_premium_by_designnprivate_by_n),
        description = stringResource(R.string.desc_modern_finance),
        actionLabel = stringResource(R.string.label_continue),
        accentedText = stringResource(R.string.label_private_by_nature),
        titleFontSize = 34.sp,
        titleLineHeight = 40.sp,
        supportingContent = { PremiumBenefitCards() },
        illustration = { PremiumPrivacyIllustration() }
    ),
    OnboardingPage(
        title = stringResource(R.string.title_secure_your_account),
        description = stringResource(R.string.desc_sync_and_premium_features),
        actionLabel = stringResource(R.string.label_next),
        titleFontSize = 34.sp,
        titleLineHeight = 40.sp,
        illustration = { SecureTrackerIllustration() }
    ),
    OnboardingPage(
        title = stringResource(R.string.title_lets_get_started),
        description = stringResource(R.string.desc_tell_us_about_yourself),
        actionLabel = stringResource(R.string.label_get_started),
        illustration = { }
    ),
    OnboardingPage(
        title = "",
        description = "",
        actionLabel = stringResource(R.string.label_continue),
        illustration = { }
    ),
    OnboardingPage(
        title = stringResource(R.string.title_unlock_pro_access),
        description = stringResource(R.string.msg_unlock_pro_access_body),
        actionLabel = stringResource(R.string.label_apply),
        illustration = { }
    )
)
