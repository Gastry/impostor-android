package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.ReviewPromptState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPromptPolicyTest {

    private val useCase = ShouldShowReviewPromptUseCase()
    private val now = 40L * DAY

    @Test
    fun `does not show before five completed games`() {
        val state = eligibleBaseState.copy(completedGames = 4)

        assertFalse(useCase(state, now))
    }

    @Test
    fun `does not show before two distinct use days`() {
        val state = eligibleBaseState.copy(distinctUseDays = 1)

        assertFalse(useCase(state, now))
    }

    @Test
    fun `does not show again after rate now`() {
        val state = eligibleBaseState.copy(isPermanentlyDismissed = true)

        assertFalse(useCase(state, now))
    }

    @Test
    fun `does not show again until seven more completed games after later`() {
        val state = eligibleBaseState.copy(
            completedGames = 11,
            nextPromptAtCompletedGames = 12,
        )

        assertFalse(useCase(state, now))
    }

    @Test
    fun `shows again after seven more completed games after later`() {
        val state = eligibleBaseState.copy(
            completedGames = 12,
            nextPromptAtCompletedGames = 12,
        )

        assertTrue(useCase(state, now))
    }

    @Test
    fun `shows when all conditions are met`() {
        assertTrue(useCase(eligibleBaseState, now))
    }

    private companion object {
        const val DAY = 24L * 60L * 60L * 1000L

        val eligibleBaseState = ReviewPromptState(
            completedGames = 5,
            distinctUseDays = 2,
            sessionCount = 2,
        )
    }
}
