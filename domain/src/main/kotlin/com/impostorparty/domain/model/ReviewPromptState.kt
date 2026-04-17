package com.impostorparty.domain.model

data class ReviewPromptState(
    val completedGames: Int = 0,
    val distinctUseDays: Int = 0,
    val sessionCount: Int = 0,
    val lastUsedEpochDay: Long? = null,
    val lastReviewAttemptEpochMillis: Long? = null,
    val lastReviewLaterEpochMillis: Long? = null,
    val lastCompletedRoundId: String? = null,
    val nextPromptAtCompletedGames: Int = 5,
    val isPermanentlyDismissed: Boolean = false,
)
