package com.mknlabs.expensetracker.ai

enum class AiRawAddReason {
    OcrBlank,
    OcrWhitespace,
    NoTransactions,
    ParseFailedWithText,
    SharedText,
}

fun AiRawAddReason.messageRes(): Int = when (this) {
    AiRawAddReason.OcrBlank -> com.mknlabs.expensetracker.R.string.msg_ai_scan_ocr_blank
    AiRawAddReason.OcrWhitespace -> com.mknlabs.expensetracker.R.string.msg_ai_scan_ocr_whitespace
    AiRawAddReason.NoTransactions -> com.mknlabs.expensetracker.R.string.msg_ai_scan_no_transactions
    AiRawAddReason.ParseFailedWithText -> com.mknlabs.expensetracker.R.string.msg_ai_scan_parse_failed
    AiRawAddReason.SharedText -> com.mknlabs.expensetracker.R.string.msg_ai_scan_shared_text
}

sealed class DocumentOcrResult {
    data class Text(val value: String) : DocumentOcrResult()
    data object PlayServicesUnavailable : DocumentOcrResult()
}

sealed class DocumentParseOutcome {
    data class Review(
        val sourceText: String,
        val items: List<com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem>,
        val fingerprint: String = "",
    ) : DocumentParseOutcome()

    data class RawAdd(
        val sourceText: String,
        val fingerprint: String = "",
        val reason: AiRawAddReason = AiRawAddReason.NoTransactions,
    ) : DocumentParseOutcome()
    data object Duplicate : DocumentParseOutcome()
    data object PlayServicesUnavailable : DocumentParseOutcome()
    data object DailyLimit : DocumentParseOutcome()
    data object Failed : DocumentParseOutcome()
}

sealed class TextParseOutcome {
    data class Review(val items: List<com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem>) : TextParseOutcome()
    data class RawAdd(val reason: AiRawAddReason = AiRawAddReason.NoTransactions) : TextParseOutcome()
    data object DailyLimit : TextParseOutcome()
    data object Failed : TextParseOutcome()
}
