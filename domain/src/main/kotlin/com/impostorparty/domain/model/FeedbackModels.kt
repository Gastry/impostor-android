package com.impostorparty.domain.model

enum class FeedbackType {
    SUGGESTION,
    PROBLEM,
}

data class FeedbackContext(
    val appVersion: String,
    val locale: String,
    val timestampEpochMillis: Long,
    val platform: String = "android",
    val clueRounds: Int? = null,
    val playerCount: Int? = null,
)

data class FeedbackSubmission(
    val type: FeedbackType,
    val message: String,
    val email: String?,
    val context: FeedbackContext,
)

enum class FeedbackValidationError {
    MESSAGE_REQUIRED,
    MESSAGE_TOO_SHORT,
    EMAIL_INVALID,
}

sealed interface FeedbackSendResult {
    data object Success : FeedbackSendResult
    data object MissingEndpoint : FeedbackSendResult
    data object NetworkError : FeedbackSendResult
    data object ServerError : FeedbackSendResult
    data object InvalidResponse : FeedbackSendResult
}
