package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.PlayerAssignment
import com.impostorparty.domain.model.PlayerSecret
import com.impostorparty.domain.model.PlayerSlot
import kotlin.random.Random

class AssignRolesUseCase {
    operator fun invoke(
        players: List<PlayerSlot>,
        impostorCount: Int,
        word: String,
        random: Random,
    ): List<PlayerAssignment> {
        require(players.isNotEmpty()) { "Players cannot be empty" }
        require(impostorCount in 1 until players.size) { "Invalid impostor count" }

        val impostorIndexes = players.indices.shuffled(random).take(impostorCount).toSet()

        return players.map { player ->
            val secret = if (player.index in impostorIndexes) {
                PlayerSecret.Impostor
            } else {
                PlayerSecret.Civilian(word)
            }
            PlayerAssignment(player = player, secret = secret)
        }
    }
}