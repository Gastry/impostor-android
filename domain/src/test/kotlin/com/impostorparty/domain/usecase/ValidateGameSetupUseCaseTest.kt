package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateGameSetupUseCaseTest {

    private val useCase = ValidateGameSetupUseCase()

    @Test
    fun `valid setup returns valid`() {
        val setup = GameSetup(
            playerCount = 7,
            impostorCount = 2,
            categories = setOf(Category.FOOD, Category.OBJECTS),
            suggestedRoundMinutes = 8,
            clueRounds = 2,
        )

        val result = useCase(setup)
        assertEquals(SetupValidationResult.Valid, result)
    }

    @Test
    fun `rejects invalid impostor count`() {
        val setup = GameSetup(playerCount = 4, impostorCount = 2, categories = setOf(Category.FOOD))

        val result = useCase(setup)
        assertEquals(
            SetupValidationResult.Invalid(SetupValidationError.IMPOSTOR_COUNT_INVALID),
            result,
        )
    }

    @Test
    fun `rejects clue rounds outside allowed range`() {
        val setup = GameSetup(
            playerCount = 6,
            impostorCount = 1,
            categories = setOf(Category.FOOD),
            clueRounds = 4,
        )

        val result = useCase(setup)

        assertEquals(
            SetupValidationResult.Invalid(SetupValidationError.CLUE_ROUNDS_INVALID),
            result,
        )
    }
}
