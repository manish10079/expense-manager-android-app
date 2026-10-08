package com.mknlabs.expensetracker.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mknlabs.expensetracker.domain.repository.ApplyReferralOutcome
import com.mknlabs.expensetracker.domain.repository.ReferralError
import com.mknlabs.expensetracker.data.local.ReferPromptStore
import com.mknlabs.expensetracker.domain.repository.ReferralInvite
import com.mknlabs.expensetracker.domain.repository.ReferralRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReferAFriendUiState(
    val isAnonymous: Boolean = true,
    val code: String = "",
    val successCount: Int = 0,
    val rewardsThisMonth: Int = 0,
    val alreadyReferred: Boolean = false,
    val applyBusy: Boolean = false,
    val applyError: ReferralError? = null,
    val applyWelcomeDays: Int? = null,
    val needsFirstTx: Boolean = false,
    val invites: List<ReferralInvite> = emptyList(),
    val welcomeExpiryMillis: Long? = null,
    val referrerRewardInvite: ReferralInvite? = null
)

@HiltViewModel
class ReferAFriendViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
    private val referralRepository: ReferralRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReferAFriendUiState(isAnonymous = firebaseAuth.currentUser == null || firebaseAuth.currentUser?.isAnonymous == true)
    )
    val uiState: StateFlow<ReferAFriendUiState> = _uiState.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { refresh() }

    init {
        firebaseAuth.addAuthStateListener(authListener)
        refresh()
    }

    override fun onCleared() {
        firebaseAuth.removeAuthStateListener(authListener)
        super.onCleared()
    }

    fun refresh() {
        val user = firebaseAuth.currentUser
        val anonymous = user == null || user.isAnonymous
        _uiState.update { it.copy(isAnonymous = anonymous) }
        if (anonymous) return
        viewModelScope.launch {
            referralRepository.ensureCode().onSuccess { snap ->
                ReferPromptStore.setSuccessCount(appContext, snap.successCount)
                val invites = referralRepository.listMyReferrals()
                val celebrated = ReferPromptStore.lastCelebratedSuccessCount(appContext)
                val newReward = if (snap.successCount > celebrated) {
                    invites.firstOrNull { it.qualified } ?: _uiState.value.referrerRewardInvite
                } else {
                    _uiState.value.referrerRewardInvite
                }
                _uiState.update {
                    it.copy(
                        code = snap.code,
                        successCount = snap.successCount,
                        rewardsThisMonth = snap.rewardsThisMonth,
                        alreadyReferred = snap.referredByUid.isNotBlank(),
                        invites = invites,
                        referrerRewardInvite = newReward
                    )
                }
            }
        }
    }

    fun applyCode(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(applyBusy = true, applyError = null, applyWelcomeDays = null) }
            when (val outcome = referralRepository.applyCode(code)) {
                is ApplyReferralOutcome.Success -> {
                    _uiState.update {
                        it.copy(
                            applyBusy = false,
                            alreadyReferred = true,
                            applyWelcomeDays = outcome.welcomeDays,
                            needsFirstTx = outcome.needsFirstTx,
                            welcomeExpiryMillis = if (outcome.welcomeDays > 0) outcome.newExpiry else null
                        )
                    }
                    refresh()
                }
                is ApplyReferralOutcome.Failure -> _uiState.update {
                    it.copy(applyBusy = false, applyError = outcome.error)
                }
            }
        }
    }

    fun dismissWelcomeCelebration() {
        _uiState.update { it.copy(welcomeExpiryMillis = null) }
    }

    fun dismissReferrerCelebration() {
        viewModelScope.launch {
            ReferPromptStore.setLastCelebratedSuccessCount(appContext, _uiState.value.successCount)
            _uiState.update { it.copy(referrerRewardInvite = null) }
        }
    }
}
