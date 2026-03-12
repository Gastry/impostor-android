package com.impostorparty.data.repository

import com.impostorparty.data.model.LanguagePayload
import com.impostorparty.data.model.WordCategoryPayload
import com.impostorparty.data.model.WordDataset
import com.impostorparty.domain.model.Category
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordDatasetValidatorTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `dataset file is well formed and complete`() {
        val file = File("src/main/assets/words_v1.json")
        val dataset = json.decodeFromString<WordDataset>(file.readText())

        WordDatasetValidator.validateOrThrow(dataset)

        assertEquals(7, dataset.languages.size)

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

    @Test(expected = IllegalArgumentException::class)
    fun `validator rejects category below minimum size`() {
        WordDatasetValidator.validateOrThrow(
            WordDataset(
                fallbackLanguage = "en",
                languages = listOf(
                    LanguagePayload(
                        tag = "en",
                        categories = Category.entries.map { category ->
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
                        categories = Category.entries.map { category ->
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
