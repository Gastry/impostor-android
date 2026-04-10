package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.GameSetup

import javax.inject.Inject

class GetAllowedImpostorCountsUseCase @Inject constructor() {
    operator fun invoke(playerCount: Int): List<Int> {
        return if (playerCount >= 6) listOf(1, 2) else listOf(1)
    }

    fun clamp(setup: GameSetup): GameSetup {
        val allowed = invoke(setup.playerCount)
        return if (setup.impostorCount in allowed) setup else setup.copy(impostorCount = allowed.first())
    }
}
