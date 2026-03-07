package com.impostorparty.domain.usecase

sealed interface RevealFlowState {
    data class PassingPhone(val playerIndex: Int) : RevealFlowState
    data class RevealingSecret(val playerIndex: Int) : RevealFlowState
    data object RoundReady : RevealFlowState
}

class ReduceRevealFlowStateUseCase {
    fun onRevealRequested(state: RevealFlowState): RevealFlowState {
        return when (state) {
            is RevealFlowState.PassingPhone -> RevealFlowState.RevealingSecret(state.playerIndex)
            else -> state
        }
    }

    fun onHideRequested(state: RevealFlowState, totalPlayers: Int): RevealFlowState {
        return when (state) {
            is RevealFlowState.RevealingSecret -> {
                val nextIndex = state.playerIndex + 1
                if (nextIndex >= totalPlayers) RevealFlowState.RoundReady
                else RevealFlowState.PassingPhone(nextIndex)
            }

            else -> state
        }
    }
}