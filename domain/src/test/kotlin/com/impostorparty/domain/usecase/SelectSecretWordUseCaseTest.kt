package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.model.WordUsageRecord
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
        WordEntry("Bear", Category.ANIMALS),
        WordEntry("Football", Category.SPORTS),
    )

    @Test
    fun `selects from chosen categories only`() {
        val result = useCase(
            words = words,
            selectedCategories = setOf(Category.ANIMALS),
            languageTag = "en",
            wordUsageHistory = emptyList(),
            avoidRecentWords = true,
            random = Random(1),
        )

        assertTrue(result is WordSelectionResult.Success)
        val selected = (result as WordSelectionResult.Success).word
        assertEquals(Category.ANIMALS, selected.category)
    }

    @Test
    fun `uses language scoped history only`() {
        val result = useCase(
            words = words,
            selectedCategories = setOf(Category.FOOD),
            languageTag = "es",
            wordUsageHistory = listOf(
                WordUsageRecord("en", Category.FOOD, normalizeWordForComparison("pizza")),
                WordUsageRecord("en", Category.FOOD, normalizeWordForComparison("pasta")),
            ),
            avoidRecentWords = true,
            random = Random(3),
        )

        assertTrue(result is WordSelectionResult.Success)
        val selectedWord = (result as WordSelectionResult.Success).word.text
        assertTrue(selectedWord == "Pizza" || selectedWord == "Pasta")
    }

    @Test
    fun `filters recent words for selected language and category`() {
        val result = useCase(
            words = words,
            selectedCategories = setOf(Category.FOOD),
            languageTag = "en",
            wordUsageHistory = listOf(
                WordUsageRecord("en", Category.FOOD, normalizeWordForComparison("pizza")),
            ),
            avoidRecentWords = true,
            random = Random(5),
        )

        assertTrue(result is WordSelectionResult.Success)
        val selected = (result as WordSelectionResult.Success).word.text
        assertEquals("Pasta", selected)
    }

    @Test
    fun `single selected category restarts its own rotation when exhausted`() {
        val history = listOf(
            WordUsageRecord("en", Category.FOOD, normalizeWordForComparison("pizza")),
            WordUsageRecord("en", Category.FOOD, normalizeWordForComparison("pasta")),
        )

        val result = useCase(
            words = words,
            selectedCategories = setOf(Category.FOOD),
            languageTag = "en",
            wordUsageHistory = history,
            avoidRecentWords = true,
            random = Random(8),
        )

        assertTrue(result is WordSelectionResult.Success)
        val success = result as WordSelectionResult.Success
        assertEquals(Category.FOOD, success.word.category)

        val records = success.updatedWordUsageHistory.filter {
            it.languageTag == "en" && it.category == Category.FOOD
        }
        assertTrue(records.size in 1..2)
    }

    @Test
    fun `when category pool is exhausted it restarts only that category`() {
        val history = listOf(
            WordUsageRecord("en", Category.FOOD, normalizeWordForComparison("pizza")),
            WordUsageRecord("en", Category.FOOD, normalizeWordForComparison("pasta")),
            WordUsageRecord("en", Category.ANIMALS, normalizeWordForComparison("tiger")),
        )

        val result = useCase(
            words = words,
            selectedCategories = setOf(Category.FOOD, Category.ANIMALS),
            languageTag = "en",
            wordUsageHistory = history,
            avoidRecentWords = true,
            random = Random(2),
        )

        assertTrue(result is WordSelectionResult.Success)
        val success = result as WordSelectionResult.Success

        val foodRecords = success.updatedWordUsageHistory.filter {
            it.languageTag == "en" && it.category == Category.FOOD
        }
        val animalRecords = success.updatedWordUsageHistory.filter {
            it.languageTag == "en" && it.category == Category.ANIMALS
        }

        assertTrue(foodRecords.size in 1..2)
        assertEquals(1, animalRecords.size)
        assertEquals(normalizeWordForComparison("tiger"), animalRecords.first().normalizedWord)
    }

    @Test
    fun `keeps category distribution reasonably balanced over many rounds`() {
        var history: List<WordUsageRecord> = emptyList()
        val counts = mutableMapOf(Category.FOOD to 0, Category.ANIMALS to 0)

        repeat(120) { turn ->
            val result = useCase(
                words = words,
                selectedCategories = setOf(Category.FOOD, Category.ANIMALS),
                languageTag = "en",
                wordUsageHistory = history,
                avoidRecentWords = true,
                random = Random(100 + turn),
            )

            assertTrue(result is WordSelectionResult.Success)
            val success = result as WordSelectionResult.Success
            counts[success.word.category] = counts.getValue(success.word.category) + 1
            history = success.updatedWordUsageHistory
        }

        val diff = kotlin.math.abs(counts.getValue(Category.FOOD) - counts.getValue(Category.ANIMALS))
        assertTrue("Distribution is too unbalanced: $counts", diff <= 20)
    }

    @Test
    fun `tie on progress does not bias to first category`() {
        val counts = mutableMapOf(Category.FOOD to 0, Category.ANIMALS to 0)

        repeat(200) { seed ->
            val result = useCase(
                words = words,
                selectedCategories = setOf(Category.FOOD, Category.ANIMALS),
                languageTag = "en",
                wordUsageHistory = emptyList(),
                avoidRecentWords = true,
                random = Random(seed + 1),
            )

            assertTrue(result is WordSelectionResult.Success)
            val success = result as WordSelectionResult.Success
            counts[success.word.category] = counts.getValue(success.word.category) + 1
        }

        assertTrue(counts.getValue(Category.FOOD) > 0)
        assertTrue(counts.getValue(Category.ANIMALS) > 0)
        val diff = kotlin.math.abs(counts.getValue(Category.FOOD) - counts.getValue(Category.ANIMALS))
        assertTrue("Tie-break is too biased: $counts", diff <= 80)
    }

    @Test
    fun `never picks categories outside selection`() {
        var history: List<WordUsageRecord> = emptyList()

        repeat(40) { turn ->
            val result = useCase(
                words = words,
                selectedCategories = setOf(Category.SPORTS),
                languageTag = "en",
                wordUsageHistory = history,
                avoidRecentWords = true,
                random = Random(300 + turn),
            )

            assertTrue(result is WordSelectionResult.Success)
            val success = result as WordSelectionResult.Success
            assertEquals(Category.SPORTS, success.word.category)
            history = success.updatedWordUsageHistory
        }
    }

    @Test
    fun `history stays bounded to current cycle progress`() {
        var history: List<WordUsageRecord> = emptyList()

        repeat(500) { turn ->
            val result = useCase(
                words = words,
                selectedCategories = setOf(Category.FOOD, Category.ANIMALS),
                languageTag = "en",
                wordUsageHistory = history,
                avoidRecentWords = true,
                random = Random(400 + turn),
            )

            assertTrue(result is WordSelectionResult.Success)
            history = (result as WordSelectionResult.Success).updatedWordUsageHistory
            val scoped = history.filter { it.languageTag == "en" && it.category in setOf(Category.FOOD, Category.ANIMALS) }
            assertTrue(scoped.size <= 4)
        }
    }

    @Test
    fun `normalization keeps japanese words valid`() {
        val japaneseWord = String(charArrayOf(0x30A4.toChar(), 0x30F3.toChar(), 0x30BF.toChar(), 0x30FC.toChar(), 0x30CD.toChar(), 0x30C3.toChar(), 0x30C8.toChar()))
        val normalized = normalizeWordForComparison(japaneseWord)
        assertTrue(normalized.isNotBlank())
    }
}
