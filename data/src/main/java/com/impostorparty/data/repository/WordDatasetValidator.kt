package com.impostorparty.data.repository

import com.impostorparty.data.model.WordDataset
import com.impostorparty.domain.model.Category
import java.text.Normalizer
import java.util.Locale

internal object WordDatasetValidator {

    private const val MIN_WORDS_PER_CATEGORY = 30

    fun validateOrThrow(dataset: WordDataset) {
        require(dataset.languages.isNotEmpty()) { "Dataset has no languages" }

        val expectedCategoryCodes = Category.wordDatasetCategories.map { it.code }.toSet()
        val languageTags = mutableSetOf<String>()

        dataset.languages.forEach { language ->
            val tag = language.tag.trim().lowercase(Locale.ROOT)
            require(tag.isNotEmpty()) { "Language tag is blank" }
            require(languageTags.add(tag)) { "Duplicated language tag: $tag" }

            val categoryCodes = mutableSetOf<String>()
            language.categories.forEach { category ->
                val code = category.code.trim().lowercase(Locale.ROOT)
                require(code.isNotEmpty()) { "Blank category code in language '$tag'" }
                require(code in expectedCategoryCodes) {
                    "Unknown category code '$code' for language '$tag'"
                }
                require(categoryCodes.add(code)) { "Duplicated category '$code' in language '$tag'" }
                require(category.words.size >= MIN_WORDS_PER_CATEGORY) {
                    "Category '$code' in language '$tag' has ${category.words.size} words; minimum is $MIN_WORDS_PER_CATEGORY"
                }

                val normalizedWords = mutableSetOf<String>()
                category.words.forEach { rawWord ->
                    val word = rawWord.trim()
                    require(word.isNotEmpty()) { "Blank word in '$tag/$code'" }

                    val normalized = normalize(word)
                    require(normalized.isNotEmpty()) {
                        "Word '$word' in '$tag/$code' becomes empty after normalization"
                    }
                    require(normalizedWords.add(normalized)) {
                        "Duplicated word '$word' in '$tag/$code'"
                    }
                }
            }

            val missingCategories = expectedCategoryCodes.filterNot { it in categoryCodes }
            require(missingCategories.isEmpty()) {
                "Language '$tag' is missing categories: ${missingCategories.joinToString()}"
            }
        }

        val fallback = dataset.fallbackLanguage.trim().lowercase(Locale.ROOT)
        require(fallback.isNotEmpty()) { "Fallback language is blank" }
        require(fallback in languageTags) {
            "Fallback language '$fallback' is not present in dataset"
        }
    }

    private fun normalize(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        val primary = normalized
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{Nd}]+"), "")

        if (primary.isNotBlank()) {
            return primary
        }

        return value
            .lowercase(Locale.ROOT)
            .filter { it.isLetterOrDigit() }
    }
}
