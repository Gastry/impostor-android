package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.PlayerSlot

class BuildPlayersUseCase {
    operator fun invoke(playerCount: Int, customNames: List<String>): List<PlayerSlot> {
        return (0 until playerCount).map { index ->
            val customName = customNames.getOrNull(index)?.trim().orEmpty()
            PlayerSlot(
                index = index,
                name = customName.ifBlank { "Player ${index + 1}" },
            )
        }
    }
}