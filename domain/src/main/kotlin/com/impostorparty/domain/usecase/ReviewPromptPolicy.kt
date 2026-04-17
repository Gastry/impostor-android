package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.ReviewPromptState
import javax.inject.Inject
import kotlin.math.floor

private const val MIN_COMPLETED_GAMES = 5
private const val MIN_DISTINCT_USE_DAYS = 2
private const val MIN_SESSION_COUNT = 2
private const val REVIEW_PROMPT_COOLDOWN_GAMES = 7
private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

class ShouldShowReviewPromptUseCase @Inject constructor() {
    operator fun invoke(
        state: ReviewPromptState,
        @Suppress("UNUSED_PARAMETER") nowEpochMillis: Long,
    ): Boolean {
        if (state.isPermanentlyDismissed) return false
        if (state.completedGames < MIN_COMPLETED_GAMES) return false
        if (state.completedGames < state.nextPromptAtCompletedGames) return false
        if (state.distinctUseDays < MIN_DISTINCT_USE_DAYS) return false
        if (state.sessionCount < MIN_SESSION_COUNT) return false

        return true
    }
}

class UpdateReviewPromptStateUseCase @Inject constructor() {
    fun onSessionStarted(
        state: ReviewPromptState,
        nowEpochMillis: Long,
    ): ReviewPromptState {
        val epochDay = floor(nowEpochMillis.toDouble() / MILLIS_PER_DAY).toLong()
        val distinctUseDays = if (state.lastUsedEpochDay == epochDay) {
            state.distinctUseDays
        } else {
            state.distinctUseDays + 1
        }

        return state.copy(
            sessionCount = state.sessionCount + 1,
            distinctUseDays = distinctUseDays,
            lastUsedEpochDay = epochDay,
        )
    }

    fun onRoundCompleted(
        state: ReviewPromptState,
        roundId: String,
    ): ReviewPromptState {
        if (state.lastCompletedRoundId == roundId) return state

        return state.copy(
            completedGames = state.completedGames + 1,
            lastCompletedRoundId = roundId,
        )
    }

    fun onReviewAttempted(
        state: ReviewPromptState,
        nowEpochMillis: Long,
    ): ReviewPromptState {
        return state.copy(
            lastReviewAttemptEpochMillis = nowEpochMillis,
            isPermanentlyDismissed = true,
        )
    }

    fun onRemindLater(
        state: ReviewPromptState,
        nowEpochMillis: Long,
    ): ReviewPromptState {
        return state.copy(
            lastReviewLaterEpochMillis = nowEpochMillis,
            nextPromptAtCompletedGames = maxOf(
                MIN_COMPLETED_GAMES,
                state.completedGames + REVIEW_PROMPT_COOLDOWN_GAMES,
            ),
        )
    }
}
