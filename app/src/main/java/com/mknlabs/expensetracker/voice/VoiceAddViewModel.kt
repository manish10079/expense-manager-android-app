package com.mknlabs.expensetracker.voice

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.AiUsageTracker
import com.mknlabs.expensetracker.ai.aiUsageDataStore
import com.mknlabs.expensetracker.ai.cloud.AiParseApiService
import com.mknlabs.expensetracker.ai.cloud.AiParseRequest
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.ai.cloud.UserContextProvider
import com.mknlabs.expensetracker.ai.offline.OfflineVoiceParser
import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserType
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.core.ui.components.VoiceSheetState
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class VoiceInputUiState(
    val sheetState: VoiceSheetState = VoiceSheetState.LISTENING,
    val transcript: String = "",
    val parsedTransaction: ParsedVoiceTransaction? = null,
    val reviewItems: List<AiParsedReviewItem> = emptyList(),
    val errorMessageResId: Int? = null,
    val parserType: VoiceParserType? = null,
    val remainingAiParses: Int? = null,
)

@HiltViewModel
class VoiceAddViewModel @Inject constructor(
    application: Application,
    private val offlineParser: OfflineVoiceParser,
    private val aiParseApiService: AiParseApiService,
    private val userContextProvider: UserContextProvider,
    private val categoryDao: CategoryDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val monetizationRepository: MonetizationRepository,
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "VoiceAddViewModel"
    }

    private val aiUsageTracker by lazy {
        AiUsageTracker(getApplication<Application>().aiUsageDataStore)
    }

    private val _uiState = MutableStateFlow(VoiceInputUiState())
    val uiState: StateFlow<VoiceInputUiState> = _uiState.asStateFlow()

    fun onPartialResult(text: String) {
        _uiState.value = _uiState.value.copy(transcript = text)
    }

    fun onSpeechResult(text: String) {
        if (text.isBlank()) {
            _uiState.value = _uiState.value.copy(
                sheetState = VoiceSheetState.ERROR,
                errorMessageResId = R.string.msg_voice_error_empty_input,
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.PROCESSING,
            transcript = text,
        )

        viewModelScope.launch {
            val isOnline = isInternetAvailable()
            val isPro = monetizationRepository.userTier.first() == UserTier.PREMIUM
            val hasRemainingAiParses =
                BuildConfig.DEBUG || aiUsageTracker.hasRemainingParses(isPro).first()

            if (isOnline && hasRemainingAiParses) {
                try {
                    val ctx = userContextProvider.getUserContext()
                    val response = aiParseApiService.parseExpenseText(
                        AiParseRequest(
                            text = text,
                            locale = ctx.locale,
                            currency = ctx.currency,
                            expenseCategories = ctx.allExpenseCategories,
                            incomeCategories = ctx.allIncomeCategories,
                            paymentMethods = ctx.allPaymentMethods,
                            skipQuota = BuildConfig.DEBUG,
                        )
                    )
                    val items = mapCloudResponse(response.transactions)
                    if (!BuildConfig.DEBUG) {
                        aiUsageTracker.recordUsage(isPro)
                    }
                    val remaining = aiUsageTracker.remainingParses(isPro).first()
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.RESULT,
                        parsedTransaction = null,
                        reviewItems = items,
                        parserType = VoiceParserType.GEMINI,
                        remainingAiParses = remaining,
                    )
                    return@launch
                } catch (e: Exception) {
                    Log.e(TAG, "cloud parse failed, using offline", e)
                }
            }

            val offlineResult = offlineParser.parse(text)
            val remaining = aiUsageTracker.remainingParses(isPro).first()
            when (offlineResult) {
                is VoiceParseResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.RESULT,
                        parsedTransaction = offlineResult.transaction,
                        reviewItems = listOf(fromOffline(offlineResult.transaction)),
                        parserType = VoiceParserType.OFFLINE,
                        remainingAiParses = remaining,
                    )
                }
                is VoiceParseResult.Failed -> {
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.ERROR,
                        errorMessageResId = offlineResult.errorMessageResId,
                    )
                }
            }
        }
    }

    private suspend fun mapCloudResponse(
        rows: List<com.mknlabs.expensetracker.ai.cloud.AiParsedTransaction>,
    ): List<AiParsedReviewItem> {
        val categories = categoryDao.getActiveCategories()
        val payments = paymentMethodDao.getActivePaymentMethods()
        val defaultPaymentId = payments.firstOrNull { it.id == DEFAULT_PAYMENT_TYPE_ID }?.id
            ?: payments.firstOrNull()?.id
            ?: DEFAULT_PAYMENT_TYPE_ID
        return rows.map { row ->
            val category = categories.firstOrNull {
                it.transactionTypeId == row.typeId && it.name.equals(row.category, ignoreCase = true)
            }
            val payment = payments.firstOrNull {
                it.name.equals(row.paymentMethod, ignoreCase = true)
            }
            AiParsedReviewItem(
                id = UUID.randomUUID().toString(),
                amount = row.amount,
                amountMinor = row.amount.toMinorUnits(),
                categoryName = category?.name ?: row.category,
                categoryId = category?.id ?: 0,
                paymentName = payment?.name ?: row.paymentMethod,
                paymentTypeId = payment?.id ?: defaultPaymentId,
                note = row.note,
                typeId = row.typeId,
            )
        }
    }

    private fun fromOffline(parsed: ParsedVoiceTransaction): AiParsedReviewItem {
        return AiParsedReviewItem(
            id = UUID.randomUUID().toString(),
            amount = parsed.amountMinor.toMajorUnits(),
            amountMinor = parsed.amountMinor,
            categoryName = "",
            categoryId = parsed.categoryId,
            paymentName = "",
            paymentTypeId = parsed.paymentTypeId ?: DEFAULT_PAYMENT_TYPE_ID,
            note = parsed.note,
            typeId = parsed.transactionTypeId,
        )
    }

    private fun isInternetAvailable(): Boolean {
        val app = getApplication<Application>()
        val connectivityManager =
            app.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun onRecognizerError(errorResId: Int) {
        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.ERROR,
            errorMessageResId = errorResId,
        )
    }

    fun resetToListening() {
        _uiState.value = VoiceInputUiState(sheetState = VoiceSheetState.LISTENING)
    }

    fun dismiss() {
        _uiState.value = VoiceInputUiState()
    }
}
