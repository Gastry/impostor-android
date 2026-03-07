package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.WordEntry
import kotlin.random.Random

sealed interface WordSelectionResult {
    data class Success(val word: WordEntry) : WordSelectionResult
    data class Error(val type: WordSelectionError) : WordSelectionResult
}

enum class WordSelectionError {
    NO_CATEGORY_SELECTED,
    NO_WORDS_FOR_CATEGORY,
    NO_WORDS_AFTER_RECENT_FILTER,
}

class SelectSecretWordUseCase {
    operator fun invoke(
        words: List<WordEntry>,
        selectedCategories: Set<Category>,
        recentWords: List<String>,
        avoidRecentWords: Boolean,
        random: Random,
    ): WordSelectionResult {
        if (selectedCategories.isEmpty()) {
            return WordSelectionResult.Error(WordSelectionError.NO_CATEGORY_SELECTED)
        }

        val byCategory = words
            .filter { it.category in selectedCategories }
            .distinctBy { normalizeWordForComparison(it.text) }

        if (byCategory.isEmpty()) {
            return WordSelectionResult.Error(WordSelectionError.NO_WORDS_FOR_CATEGORY)
        }

        val availableWords = if (!avoidRecentWords) {
            byCategory
        } else {
            val normalizedRecent = recentWords.map(::normalizeWordForComparison).toSet()
            byCategory.filter { normalizeWordForComparison(it.text) !in normalizedRecent }
        }

        if (availableWords.isEmpty()) {
            return WordSelectionResult.Error(WordSelectionError.NO_WORDS_AFTER_RECENT_FILTER)
        }

        return WordSelectionResult.Success(availableWords.random(random))
    }
}