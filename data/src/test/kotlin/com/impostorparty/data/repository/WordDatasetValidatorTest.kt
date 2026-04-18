package com.impostorparty.data.repository

import com.impostorparty.data.model.LanguagePayload
import com.impostorparty.data.model.WordCategoryPayload
import com.impostorparty.data.model.WordDataset
import com.impostorparty.domain.model.Category
import java.io.File
import java.nio.charset.StandardCharsets
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordDatasetValidatorTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun readDataset(): WordDataset {
        val file = File("src/main/assets/words_v1.json")
        return json.decodeFromString(file.readText(StandardCharsets.UTF_8))
    }

    @Test
    fun `dataset file is well formed and complete`() {
        val dataset = readDataset()

        WordDatasetValidator.validateOrThrow(dataset)

        assertEquals(9, dataset.languages.size)

        dataset.languages.forEach { language ->
            assertEquals(10, language.categories.size)
            language.categories.forEach { category ->
                assertTrue(
                    "Language ${language.tag}, category ${category.code} has ${category.words.size} words",
                    category.words.size >= 30,
                )
            }
        }
    }

    @Test
    fun `movies and series titles use localized market names`() {
        val dataset = readDataset()

        fun moviesFor(tag: String): List<String> =
            dataset.languages
                .first { it.tag == tag }
                .categories
                .first { it.code == "movies_series" }
                .words

        val spanishSpain = moviesFor("es-ES")
        assertTrue(spanishSpain.containsAll(listOf("Origen", "Parque Jurásico", "Black Panther", "Miércoles")))
        assertFalse(spanishSpain.contains("El origen"))
        assertFalse(spanishSpain.contains("Jurassic Park"))
        assertFalse(spanishSpain.contains("Merlina"))
        assertFalse(spanishSpain.contains("Pantera Negra"))

        val spanishLatam = moviesFor("es-419")
        assertTrue(spanishLatam.containsAll(listOf("El origen", "Jurassic Park", "Black Panther", "Merlina")))
        assertFalse(spanishLatam.contains("Origen"))
        assertFalse(spanishLatam.contains("Parque Jurásico"))
        assertFalse(spanishLatam.contains("Miércoles"))
        assertFalse(spanishLatam.contains("Aquí no hay quien viva"))
        assertFalse(spanishLatam.contains("Pantera Negra"))

        assertTrue(moviesFor("fr").contains("La Reine des neiges"))
        assertTrue(moviesFor("de").contains("Die Eiskönigin – Völlig unverfroren"))
        assertTrue(moviesFor("it").contains("Il gladiatore"))
        assertTrue(moviesFor("pt").contains("A Origem"))
        assertTrue(moviesFor("ja").contains("アナと雪の女王"))
    }

    @Test
    fun `core categories stay inside their own language blocks`() {
        val dataset = readDataset()

        fun wordsFor(tag: String, code: String): List<String> =
            dataset.languages
                .first { it.tag == tag }
                .categories
                .first { it.code == code }
                .words

        val spanishAnimals = wordsFor("es", "animals")
        assertTrue(spanishAnimals.contains("Perro"))
        assertFalse(spanishAnimals.contains("Dog"))
        assertFalse(spanishAnimals.contains("Chien"))
        assertFalse(spanishAnimals.contains("Hund"))

        val englishAnimals = wordsFor("en", "animals")
        assertTrue(englishAnimals.contains("Dog"))
        assertFalse(englishAnimals.contains("Perro"))
        assertFalse(englishAnimals.contains("Chien"))

        val frenchAnimals = wordsFor("fr", "animals")
        assertTrue(frenchAnimals.contains("Chien"))
        assertFalse(frenchAnimals.contains("Dog"))
        assertFalse(frenchAnimals.contains("Perro"))

        val germanAnimals = wordsFor("de", "animals")
        assertTrue(germanAnimals.contains("Hund"))
        assertFalse(germanAnimals.contains("Dog"))
        assertFalse(germanAnimals.contains("Perro"))

        val italianAnimals = wordsFor("it", "animals")
        assertTrue(italianAnimals.contains("Cane"))
        assertFalse(italianAnimals.contains("Dog"))
        assertFalse(italianAnimals.contains("Perro"))

        val portugueseAnimals = wordsFor("pt", "animals")
        assertTrue(portugueseAnimals.contains("Cachorro"))
        assertFalse(portugueseAnimals.contains("Dog"))
        assertFalse(portugueseAnimals.contains("Perro"))

        val japaneseAnimals = wordsFor("ja", "animals")
        assertTrue(japaneseAnimals.contains("いぬ"))
        assertFalse(japaneseAnimals.contains("Dog"))
        assertFalse(japaneseAnimals.contains("Perro"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validator rejects category below minimum size`() {
        WordDatasetValidator.validateOrThrow(
            WordDataset(
                fallbackLanguage = "en",
                languages = listOf(
                    LanguagePayload(
                        tag = "en",
                        categories = Category.wordDatasetCategories.map { category ->
                            if (category == Category.FOOD) {
                                WordCategoryPayload(
                                    code = category.code,
                                    words = List(29) { "word$it" },
                                )
                            } else {
                                WordCategoryPayload(
                                    code = category.code,
                                    words = List(30) { "${category.code}-$it" },
                                )
                            }
                        },
                    ),
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validator rejects empty categories`() {
        WordDatasetValidator.validateOrThrow(
            WordDataset(
                fallbackLanguage = "en",
                languages = listOf(
                    LanguagePayload(
                        tag = "en",
                        categories = listOf(
                            WordCategoryPayload(code = "food", words = emptyList()),
                        ),
                    ),
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validator rejects duplicates in same language and category`() {
        WordDatasetValidator.validateOrThrow(
            WordDataset(
                fallbackLanguage = "en",
                languages = listOf(
                    LanguagePayload(
                        tag = "en",
                        categories = Category.wordDatasetCategories.map { category ->
                            if (category.code == "food") {
                                WordCategoryPayload(
                                    code = "food",
                                    words = listOf(
                                        "Pizza",
                                        "pizza",
                                    ) + List(28) { "food-$it" },
                                )
                            } else {
                                WordCategoryPayload(
                                    code = category.code,
                                    words = List(30) { "${category.code}-$it" },
                                )
                            }
                        },
                    ),
                ),
            ),
        )
    }
}
