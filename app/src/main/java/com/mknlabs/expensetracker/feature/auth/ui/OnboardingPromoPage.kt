package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.core.ui.components.ProPassRedeemDialog
import com.mknlabs.expensetracker.monetization.MonetizationViewModel

@Composable
internal fun OnboardingPromoPage(
    onFinished: () -> Unit,
) {
    val viewModel: MonetizationViewModel = hiltViewModel()
    ProPassRedeemDialog(
        viewModel = viewModel,
        onDismiss = onFinished,
    )
}
