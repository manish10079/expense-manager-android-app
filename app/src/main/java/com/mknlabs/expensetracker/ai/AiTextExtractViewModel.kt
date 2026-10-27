package com.mknlabs.expensetracker.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.ai.cloud.FirebaseGeminiParser
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiTextExtractViewModel @Inject constructor(
    private val firebaseGeminiParser: FirebaseGeminiParser,
    private val aiUsageTracker: AiUsageTracker,
) : ViewModel() {

    fun parse(
        text: String,
        onResult: (ParsedVoiceTransaction?) -> Unit,
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            onResult(null)
            return
        }
        viewModelScope.launch {
            val parsed = runCatching {
                if (!aiUsageTracker.hasRemainingParses.first()) return@runCatching null
                when (val result = firebaseGeminiParser.parseAsync(trimmed)) {
                    is VoiceParseResult.Success -> {
                        aiUsageTracker.recordUsage()
                        result.transaction
                    }
                    else -> null
                }
            }.getOrNull()
            onResult(parsed)
        }
    }
}
