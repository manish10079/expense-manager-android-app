package com.mknlabs.expensetracker.models

import androidx.annotation.StringRes
import com.mknlabs.expensetracker.R

enum class BudgetPeriod(@StringRes val labelRes: Int) {
    MONTHLY(R.string.label_monthly),
    YEARLY(R.string.label_yearly)
}
