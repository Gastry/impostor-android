package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.PlayerSlot
import com.impostorparty.domain.model.PlayerSecret
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssignRolesUseCaseTest {

    private val useCase = AssignRolesUseCase()

    @Test
    fun `assigns exact impostor count without duplicates`() {
        val players = (0 until 8).map { PlayerSlot(it, "P${it + 1}") }

        val assignments = useCase(players, impostorCount = 2, word = "Volcano", random = Random(7))

        val impostors = assignments.count { it.secret is PlayerSecret.Impostor }
        val civilians = assignments.count { it.secret is PlayerSecret.Civilian }

        assertEquals(2, impostors)
        assertEquals(6, civilians)
        assertEquals(8, assignments.map { it.player.index }.distinct().size)
    }

    @Test
    fun `all civilians receive same word`() {
        val players = (0 until 5).map { PlayerSlot(it, "P${it + 1}") }

        val assignments = useCase(players, impostorCount = 1, word = "Pizza", random = Random(3))
        val civilianWords = assignments
            .filter { it.secret is PlayerSecret.Civilian }
            .map { (it.secret as PlayerSecret.Civilian).word }

        assertTrue(civilianWords.all { it == "Pizza" })
    }
}