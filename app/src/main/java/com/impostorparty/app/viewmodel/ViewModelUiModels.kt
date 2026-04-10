package com.impostorparty.app.viewmodel

import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackType
import com.impostorparty.domain.model.FeedbackValidationError
import com.impostorparty.domain.usecase.SetupValidationError
import com.impostorparty.domain.usecase.WordSelectionError

data class ReviewPromptUiState(
    val roundId: String,
)

data class FeedbackFormUiState(
    val type: FeedbackType = FeedbackType.SUGGESTION,
    val message: String = "",
    val email: String = "",
    val isSending: Boolean = false,
    val isSuccess: Boolean = false,
    val sendResult: FeedbackSendResult? = null,
    val validationErrors: Set<FeedbackValidationError> = emptySet(),
)

data class FeedbackContextHint(
    val clueRounds: Int?,
    val playerCount: Int?,
)

enum class UiMessageType {
    INVALID_SETUP,
    WORDS_UNAVAILABLE,
    UNKNOWN,
}

data class UiMessage(
    val type: UiMessageType,
    val detail: String,
) {
    companion object {
        fun fromSetupError(error: SetupValidationError): UiMessage {
            return UiMessage(UiMessageType.INVALID_SETUP, error.name)
        }

        fun fromWordError(error: WordSelectionError): UiMessage {
            return UiMessage(UiMessageType.WORDS_UNAVAILABLE, error.name)
        }
    }
}
