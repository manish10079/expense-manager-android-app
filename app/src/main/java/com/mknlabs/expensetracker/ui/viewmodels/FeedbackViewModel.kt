package com.mknlabs.expensetracker.ui.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.domain.repository.FeedbackRepository
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class FeedbackUiState(
    val feedbackText: String = "",
    val isLoading: Boolean = false,
    val userEmail: String = "",
    val userId: String = "",
    val cooldownRemainingMinutes: Long = 0L,
    val isCooldownActive: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val authRepository: AuthRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        // Load user details
        viewModelScope.launch {
            try {
                val profile = UserProfileDataStore.getUserProfileFlow(appContext).first()
                val currentUid = authRepository.currentUser.value?.uid ?: ""
                _uiState.update { 
                    it.copy(
                        userEmail = profile.emailAddress,
                        userId = currentUid
                    )
                }
            } catch (e: Exception) {
                // Ignore profile read errors and let them submit with anonymous/fallback details
            }
        }

        // Listen for cooldown changes
        viewModelScope.launch {
            feedbackRepository.getLastFeedbackTime().collectLatest { lastFeedbackTime ->
                checkCooldown(lastFeedbackTime)
            }
        }
    }

    fun onFeedbackTextChanged(text: String) {
        _uiState.update { it.copy(feedbackText = text, errorMessageRes = null) }
    }

    private fun checkCooldown(lastFeedbackTime: Long) {
        val now = System.currentTimeMillis()
        val difference = now - lastFeedbackTime
        val oneHourMillis = TimeUnit.HOURS.toMillis(1)

        if (difference in 0 until oneHourMillis) {
            val remainingMillis = oneHourMillis - difference
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) + 1
            _uiState.update {
                it.copy(
                    isCooldownActive = true,
                    cooldownRemainingMinutes = remainingMinutes
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isCooldownActive = false,
                    cooldownRemainingMinutes = 0L
                )
            }
        }
    }

    fun submitFeedback() {
        val text = _uiState.value.feedbackText.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(errorMessageRes = R.string.msg_feedback_empty) }
            return
        }

        if (_uiState.value.isCooldownActive) {
            _uiState.update { it.copy(errorMessageRes = R.string.msg_feedback_cooldown) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }

        viewModelScope.launch {
            val result = feedbackRepository.submitFeedback(
                userId = _uiState.value.userId,
                email = _uiState.value.userEmail,
                feedback = text
            )

            result.fold(
                onSuccess = {
                    val now = System.currentTimeMillis()
                    feedbackRepository.saveLastFeedbackTime(now)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            feedbackText = "",
                            isSuccess = true
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessageRes = R.string.msg_feedback_error
                        )
                    }
                }
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
