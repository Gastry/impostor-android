package com.impostorparty.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ReduceRevealFlowStateUseCaseTest {

    private val useCase = ReduceRevealFlowStateUseCase()

    @Test
    fun `flow moves from pass to reveal to next player`() {
        val initial = RevealFlowState.PassingPhone(playerIndex = 0)

        val reveal = useCase.onRevealRequested(initial)
        val next = useCase.onHideRequested(reveal, totalPlayers = 3)

        assertEquals(RevealFlowState.RevealingSecret(0), reveal)
        assertEquals(RevealFlowState.PassingPhone(1), next)
    }

    @Test
    fun `flow reaches round ready after last player`() {
        val revealLast = RevealFlowState.RevealingSecret(playerIndex = 2)

        val next = useCase.onHideRequested(revealLast, totalPlayers = 3)

        assertEquals(RevealFlowState.RoundReady, next)
    }
}