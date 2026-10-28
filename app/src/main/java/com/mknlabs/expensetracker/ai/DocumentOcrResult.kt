package com.mknlabs.expensetracker.ai

sealed class DocumentOcrResult {
    data class Text(val value: String) : DocumentOcrResult()
    data object PlayServicesUnavailable : DocumentOcrResult()
}

sealed class DocumentParseOutcome {
    data class Review(
        val sourceText: String,
        val items: List<com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem>,
    ) : DocumentParseOutcome()

    data class RawAdd(val sourceText: String) : DocumentParseOutcome()
    data object PlayServicesUnavailable : DocumentParseOutcome()
    data object DailyLimit : DocumentParseOutcome()
    data object Failed : DocumentParseOutcome()
}

sealed class TextParseOutcome {
    data class Review(val items: List<com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem>) : TextParseOutcome()
    data object RawAdd : TextParseOutcome()
    data object DailyLimit : TextParseOutcome()
    data object Failed : TextParseOutcome()
}
