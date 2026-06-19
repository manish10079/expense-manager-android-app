package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.CountryCodeRepository
import com.mknlabs.expensetracker.models.CountryCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val countryCodeRepository: CountryCodeRepository
) : ViewModel() {

    val countryCodes: StateFlow<List<CountryCode>> = flow {
        emit(countryCodeRepository.getCountryCodes())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
