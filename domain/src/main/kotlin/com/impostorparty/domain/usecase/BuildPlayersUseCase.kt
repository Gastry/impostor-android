package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.PlayerSlot
import javax.inject.Inject

class BuildPlayersUseCase @Inject constructor() {
    operator fun invoke(
        playerCount: Int,
        customNames: List<String>,
        fallbackNames: List<String> = emptyList(),
    ): List<PlayerSlot> {
        return (0 until playerCount).map { index ->
            val customName = customNames.getOrNull(index)?.trim().orEmpty()
            val fallbackName = fallbackNames.getOrNull(index)?.trim().orEmpty()
            PlayerSlot(
                index = index,
                name = customName.ifBlank { fallbackName.ifBlank { "Player ${index + 1}" } },
            )
        }
    }
}
