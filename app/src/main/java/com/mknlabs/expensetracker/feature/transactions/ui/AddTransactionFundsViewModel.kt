package com.mknlabs.expensetracker.feature.transactions.ui

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
 * The funds a new expense can be drawn from.
 *
 * Its own ViewModel rather than a parameter threaded down from the navigation host: the
 * form only needs the list, and keeping it here leaves every existing caller's signature
 * untouched.
 */
@HiltViewModel
class AddTransactionFundsViewModel @Inject constructor(
    fundRepository: FundRepository
) : ViewModel() {

    val funds: StateFlow<List<Fund>> = fundRepository.observeActiveFunds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
