package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.PaymentMethodPredictorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for auto payment method prediction.
 *
 * Predicts the payment method for a given merchant text and learns
 * from user transaction history. Used in [AddTransactionScreen] to
 * auto-fill the payment method picker.
 *
 * Follows GEMINI.md conventions:
 * - @HiltViewModel with constructor injection
 * - StateFlow for UI state
 * - ViewModelScope for coroutines
 */
@HiltViewModel
class PaymentMethodPredictorViewModel @Inject constructor(
    private val predictor: PaymentMethodPredictorRepository
) : ViewModel() {

    /** Predicted payment method ID for the current merchant, or null if no prediction. */
    private val _predictedPaymentMethodId = MutableStateFlow<Int?>(null)
    val predictedPaymentMethodId: StateFlow<Int?> = _predictedPaymentMethodId.asStateFlow()

    /**
     * Predicts the payment method for [merchantText].
     * Called when the user types in the note/merchant field.
     */
    fun predict(merchantText: String) {
        viewModelScope.launch {
            _predictedPaymentMethodId.value = predictor.predict(merchantText)
        }
    }

    /**
     * Records that [merchantText] was paid with [paymentMethodId].
     * Called after a transaction is saved to learn from user behavior.
     */
    fun learn(merchantText: String, paymentMethodId: Int) {
        viewModelScope.launch {
            predictor.learn(merchantText, paymentMethodId)
        }
    }

    /**
     * Clears the current prediction (e.g., when user manually changes payment method).
     */
    fun clearPrediction() {
        _predictedPaymentMethodId.value = null
    }
}
