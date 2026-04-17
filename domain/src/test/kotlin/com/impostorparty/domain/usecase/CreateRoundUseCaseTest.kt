package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.LocalizedWordPool
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.repository.WordRepository
import kotlin.random.Random
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateRoundUseCaseTest {

    private val useCase = CreateRoundUseCase()

    @Test
    fun `uses words from repository language resolution`() = runTest {
        val repository = FakeLocalizedRepository()
        val setup = GameSetup(
            playerCount = 5,
            impostorCount = 1,
            categories = setOf(Category.FOOD),
            clueRounds = 3,
        )

        val result = useCase(
            setup = setup,
            activeLanguageTag = "es-ES",
            wordUsageHistory = emptyList(),
            wordRepository = repository,
            random = Random(1),
        )

        assertTrue(result is CreateRoundResult.Success)
        val success = result as CreateRoundResult.Success
        assertEquals("Paella", success.session.word.text)
        assertEquals(3, success.session.setup.clueRounds)
        assertTrue(
            success.updatedWordUsageHistory.any {
                it.languageTag == "es" && it.category == Category.FOOD
            },
        )
    }

    @Test
    fun `falls back to english when requested language is missing`() = runTest {
        val repository = FakeLocalizedRepository()
        val setup = GameSetup(
            playerCount = 5,
            impostorCount = 1,
            categories = setOf(Category.ANIMALS),
        )

        val result = useCase(
            setup = setup,
            activeLanguageTag = "ja",
            wordUsageHistory = emptyList(),
            wordRepository = repository,
            random = Random(3),
        )

        assertTrue(result is CreateRoundResult.Success)
        val success = result as CreateRoundResult.Success
        assertEquals("Tiger", success.session.word.text)
        assertTrue(success.updatedWordUsageHistory.any { it.languageTag == "en" })
    }

    @Test
    fun `can use player names as dynamic word category`() = runTest {
        val repository = FakeLocalizedRepository()
        val setup = GameSetup(
            playerCount = 4,
            impostorCount = 1,
            categories = setOf(Category.PLAYERS),
            customPlayerNames = listOf("Ana", "Luis", "Mia", "Noa"),
        )

        val result = useCase(
            setup = setup,
            activeLanguageTag = "es-ES",
            wordUsageHistory = emptyList(),
            wordRepository = repository,
            random = Random(1),
        )

        assertTrue(result is CreateRoundResult.Success)
        val success = result as CreateRoundResult.Success
        assertTrue(success.session.word.text in setup.customPlayerNames)
        assertEquals(Category.PLAYERS, success.session.word.category)
    }

    private class FakeLocalizedRepository : WordRepository {
        override suspend fun getWords(languageTag: String?): LocalizedWordPool {
            val normalized = languageTag?.substringBefore('-')
            return when (normalized) {
                "es" -> LocalizedWordPool(
                    languageTag = "es",
                    words = listOf(
                        WordEntry("Paella", Category.FOOD),
                        WordEntry("Leon", Category.ANIMALS),
                    ),
                )

                else -> LocalizedWordPool(
                    languageTag = "en",
                    words = listOf(
                        WordEntry("Pizza", Category.FOOD),
                        WordEntry("Tiger", Category.ANIMALS),
                    ),
                )
            }
        }
    }
}
