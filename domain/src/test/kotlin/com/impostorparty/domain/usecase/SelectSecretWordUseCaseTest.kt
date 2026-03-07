package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.WordEntry
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectSecretWordUseCaseTest {

    private val useCase = SelectSecretWordUseCase()

    private val words = listOf(
        WordEntry("Pizza", Category.FOOD),
        WordEntry("Pasta", Category.FOOD),
        WordEntry("Tiger", Category.ANIMALS),
        WordEntry("Football", Category.SPORTS),
    )

    @Test
    fun `selects from chosen categories only`() {
        val result = useCase(
            words = words,
            selectedCategories = setOf(Category.ANIMALS),
            recentWords = emptyList(),
            avoidRecentWords = true,
            random = Random(1),
        )

        assertTrue(result is WordSelectionResult.Success)
        val selected = (result as WordSelectionResult.Success).word
        assertEquals(Category.ANIMALS, selected.category)
    }

    @Test
    fun `filters recent words when enabled`() {
        val result = useCase(
            words = words,
            selectedCategories = setOf(Category.FOOD),
            recentWords = listOf("pizza", "PASTA"),
            avoidRecentWords = true,
            random = Random(1),
        )

        assertTrue(result is WordSelectionResult.Error)
        assertEquals(
            WordSelectionError.NO_WORDS_AFTER_RECENT_FILTER,
            (result as WordSelectionResult.Error).type,
        )
    }

    @Test
    fun `ignores recent words when feature disabled`() {
        val result = useCase(
            words = words,
            selectedCategories = setOf(Category.FOOD),
            recentWords = listOf("pizza", "pasta"),
            avoidRecentWords = false,
            random = Random(4),
        )

        assertTrue(result is WordSelectionResult.Success)
    }
}