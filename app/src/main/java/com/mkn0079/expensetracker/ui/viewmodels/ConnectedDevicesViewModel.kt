package com.mkn0079.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.domain.repository.RegisteredDevice
import com.mkn0079.expensetracker.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ConnectedDevicesUiState {
    object Loading : ConnectedDevicesUiState()
    data class Success(
        val devices: List<RegisteredDevice>,
        val maxDevices: Int = 4
    ) : ConnectedDevicesUiState()
    data class Error(val message: String) : ConnectedDevicesUiState()
}

@HiltViewModel
class ConnectedDevicesViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    init {
        refreshDevices()
    }

    val uiState: StateFlow<ConnectedDevicesUiState> = syncRepository.registeredDevices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ).let { flow ->
            MutableStateFlow<ConnectedDevicesUiState>(ConnectedDevicesUiState.Loading).apply {
                viewModelScope.launch {
                    flow.collect { devices ->
                        value = ConnectedDevicesUiState.Success(devices)
                    }
                }
            }
        }

    fun unregisterDevice(deviceId: String) {
        viewModelScope.launch {
            syncRepository.unregisterDevice(deviceId)
        }
    }

    fun refreshDevices() {
        viewModelScope.launch {
            syncRepository.refreshDevices()
        }
    }
}
