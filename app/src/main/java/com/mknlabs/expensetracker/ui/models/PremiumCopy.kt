package com.mknlabs.expensetracker.ui.models

import androidx.annotation.StringRes
import com.mknlabs.expensetracker.R

data class PremiumCopy(
    @get:StringRes val sloganResId: Int,
    @get:StringRes val subheadlineResId: Int,
    @get:StringRes val ctaResId: Int
)

val PremiumCopyOptions = listOf(
    PremiumCopy(R.string.pro_slogan_1, R.string.pro_subheadline_1, R.string.pro_cta_1),
    PremiumCopy(R.string.pro_slogan_2, R.string.pro_subheadline_2, R.string.pro_cta_2),
    PremiumCopy(R.string.pro_slogan_3, R.string.pro_subheadline_3, R.string.pro_cta_3),
    PremiumCopy(R.string.pro_slogan_4, R.string.pro_subheadline_4, R.string.pro_cta_4),
    PremiumCopy(R.string.pro_slogan_5, R.string.pro_subheadline_5, R.string.pro_cta_5),
    PremiumCopy(R.string.pro_slogan_6, R.string.pro_subheadline_6, R.string.pro_cta_6),
    PremiumCopy(R.string.pro_slogan_7, R.string.pro_subheadline_7, R.string.pro_cta_7),
    PremiumCopy(R.string.pro_slogan_8, R.string.pro_subheadline_8, R.string.pro_cta_8)
)
