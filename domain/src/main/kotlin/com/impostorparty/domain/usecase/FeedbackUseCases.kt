package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackSubmission
import com.impostorparty.domain.model.FeedbackValidationError
import com.impostorparty.domain.repository.FeedbackRepository
import javax.inject.Inject

class ValidateFeedbackInputUseCase @Inject constructor() {
    operator fun invoke(
        message: String,
        email: String,
    ): Set<FeedbackValidationError> {
        val trimmedMessage = message.trim()
        val trimmedEmail = email.trim()
        val errors = linkedSetOf<FeedbackValidationError>()

        if (trimmedMessage.isEmpty()) {
            errors += FeedbackValidationError.MESSAGE_REQUIRED
        } else if (trimmedMessage.length < MIN_MESSAGE_LENGTH) {
            errors += FeedbackValidationError.MESSAGE_TOO_SHORT
        } else if (trimmedMessage.length > MAX_MESSAGE_LENGTH) {
            errors += FeedbackValidationError.MESSAGE_TOO_LONG
        }

        if (trimmedEmail.isNotEmpty() && !EMAIL_REGEX.matches(trimmedEmail)) {
            errors += FeedbackValidationError.EMAIL_INVALID
        }

        return errors
    }

    private companion object {
        const val MIN_MESSAGE_LENGTH = 8
        const val MAX_MESSAGE_LENGTH = 1_200
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}

class SendFeedbackUseCase @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
) {
    suspend operator fun invoke(submission: FeedbackSubmission): FeedbackSendResult {
        return feedbackRepository.sendFeedback(submission)
    }
}
