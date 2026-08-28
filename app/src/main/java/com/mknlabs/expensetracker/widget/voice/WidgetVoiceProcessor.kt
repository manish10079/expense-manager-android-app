package com.mknlabs.expensetracker.widget.voice

import android.util.Log
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.widget.model.WidgetParsedTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bridges the existing [VoiceParserRepository] to the widget's [WidgetParsedTransaction].
 *
 * Responsibilities:
 * - Call VoiceParserRepository.parse(text)
 * - Convert parser result to WidgetParsedTransaction
 * - Return immutable model
 *
 * Does NOT duplicate parser logic — delegates entirely.
 */
internal class WidgetVoiceProcessor(
    private val voiceParserRepository: VoiceParserRepository
) {

    /**
     * Parse the transcript and return a widget-ready transaction model.
     *
     * @param transcript The speech-to-text result.
     * @return Success with [WidgetParsedTransaction], or Failure with error resource ID.
     */
    fun process(transcript: String): WidgetParseResult {
        if (transcript.isBlank()) {
            return WidgetParseResult.Failure(com.mknlabs.expensetracker.R.string.msg_voice_error_empty_input)
        }

        return when (val result = voiceParserRepository.parse(transcript)) {
            is VoiceParseResult.Success -> {
                val tx = result.transaction
                val amountMajor = tx.amountMinor.toMajorUnits()
                val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                    .format(Date(tx.createdAt))

                WidgetParseResult.Success(
                    WidgetParsedTransaction(
                        amountText = formatAmount(amountMajor),
                        amountMinor = tx.amountMinor,
                        categoryName = "", // Will be resolved from categoryId at save time
                        categoryId = tx.categoryId,
                        merchant = tx.merchant,
                        note = tx.note.ifBlank { "Expense • $dateStr" },
                        transactionTypeId = tx.transactionTypeId,
                        paymentTypeId = tx.paymentTypeId,
                        confidenceText = tx.confidence.name,
                        createdAt = tx.createdAt
                    )
                )
            }
            is VoiceParseResult.Failed -> {
                WidgetParseResult.Failure(result.errorMessageResId)
            }
        }
    }

    private fun formatAmount(amountMajor: Double): String {
        return if (amountMajor == amountMajor.toLong().toDouble()) {
            "₹${amountMajor.toLong()}"
        } else {
            "₹${String.format(Locale.US, "%.2f", amountMajor)}"
        }
    }
}

/** Result of widget voice processing. */
internal sealed class WidgetParseResult {
    data class Success(val parsedTransaction: WidgetParsedTransaction) : WidgetParseResult()
    data class Failure(val errorMessageResId: Int) : WidgetParseResult()
}
