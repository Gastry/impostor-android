package com.impostorparty.domain.repository

import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackSubmission

interface FeedbackRepository {
    suspend fun sendFeedback(submission: FeedbackSubmission): FeedbackSendResult
}
