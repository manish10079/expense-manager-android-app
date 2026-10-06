package com.mknlabs.expensetracker.feature.analytics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.FundRepository
import com.mknlabs.expensetracker.models.Fund
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Supplies the Analytics screen with the user's funds so the fund breakdown can label its
 * slices. Its own ViewModel, mirroring how tags reach the screen, so the navigation host
 * does not have to grow another parameter it would only pass through.
 */
@HiltViewModel
class AnalyticsFundsViewModel @Inject constructor(
    fundRepository: FundRepository
) : ViewModel() {

    val funds: StateFlow<List<Fund>> = fundRepository.observeActiveFunds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
