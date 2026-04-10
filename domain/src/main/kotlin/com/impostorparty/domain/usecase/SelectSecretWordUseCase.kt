package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.model.WordUsageRecord
import javax.inject.Inject
import kotlin.random.Random

sealed interface WordSelectionResult {
    data class Success(
        val word: WordEntry,
        val updatedWordUsageHistory: List<WordUsageRecord>,
    ) : WordSelectionResult

    data class Error(val type: WordSelectionError) : WordSelectionResult
}

enum class WordSelectionError {
    NO_CATEGORY_SELECTED,
    NO_WORDS_FOR_CATEGORY,
    NO_WORDS_AFTER_RECENT_FILTER,
}

class SelectSecretWordUseCase @Inject constructor(
    private val wordNormalization: WordNormalization,
) {
    constructor() : this(WordNormalization())

    operator fun invoke(
        words: List<WordEntry>,
        selectedCategories: Set<Category>,
        languageTag: String,
        wordUsageHistory: List<WordUsageRecord>,
        avoidRecentWords: Boolean,
        random: Random,
    ): WordSelectionResult {
        if (selectedCategories.isEmpty()) {
            return WordSelectionResult.Error(WordSelectionError.NO_CATEGORY_SELECTED)
        }

        val byCategory = words
            .filter { it.category in selectedCategories }
            .groupBy { it.category }
            .mapValues { (_, entries) -> entries.distinctBy { wordNormalization(it.text) } }
            .filterValues { it.isNotEmpty() }

        if (byCategory.isEmpty()) {
            return WordSelectionResult.Error(WordSelectionError.NO_WORDS_FOR_CATEGORY)
        }

        val categoryUsage = buildCategoryUsageMap(
            languageTag = languageTag,
            selectedCategories = byCategory.keys,
            history = wordUsageHistory,
        )

        val chosenCategory = chooseBalancedCategory(
            byCategory = byCategory,
            usageByCategory = categoryUsage,
            avoidRecentWords = avoidRecentWords,
            random = random,
        )

        val pool = byCategory.getValue(chosenCategory)
        val usedSet = categoryUsage.getValue(chosenCategory)

        val availableWords = if (!avoidRecentWords) {
            pool
        } else {
            val filtered = pool.filter { wordNormalization(it.text) !in usedSet }
            if (filtered.isEmpty()) {
                usedSet.clear()
                pool
            } else {
                filtered
            }
        }

        if (availableWords.isEmpty()) {
            return WordSelectionResult.Error(WordSelectionError.NO_WORDS_AFTER_RECENT_FILTER)
        }

        val selectedWord = availableWords.random(random)
        usedSet += wordNormalization(selectedWord.text)

        return WordSelectionResult.Success(
            word = selectedWord,
            updatedWordUsageHistory = mergeUpdatedUsageHistory(
                existing = wordUsageHistory,
                languageTag = languageTag,
                usageByCategory = categoryUsage,
            ),
        )
    }

    private fun buildCategoryUsageMap(
        languageTag: String,
        selectedCategories: Set<Category>,
        history: List<WordUsageRecord>,
    ): MutableMap<Category, MutableSet<String>> {
        val usage = selectedCategories.associateWith { mutableSetOf<String>() }.toMutableMap()

        history.asSequence()
            .filter { it.languageTag == languageTag && it.category in selectedCategories }
            .forEach { record ->
                usage.getOrPut(record.category) { mutableSetOf() } += record.normalizedWord
            }

        return usage
    }

    private fun chooseBalancedCategory(
        byCategory: Map<Category, List<WordEntry>>,
        usageByCategory: MutableMap<Category, MutableSet<String>>,
        avoidRecentWords: Boolean,
        random: Random,
    ): Category {
        val scored = byCategory.map { (category, pool) ->
            val normalizedPoolSize = pool.size.coerceAtLeast(1)
            val usedSet = usageByCategory.getOrPut(category) { mutableSetOf() }

            if (avoidRecentWords && usedSet.size >= normalizedPoolSize) {
                // Restart only this category when its own rotation is exhausted.
                usedSet.clear()
            }

            val progress = usedSet.size.toDouble() / normalizedPoolSize.toDouble()
            category to progress
        }

        val minProgress = scored.minOf { it.second }
        val candidates = scored.filter { it.second == minProgress }.map { it.first }
        return candidates.random(random)
    }

    private fun mergeUpdatedUsageHistory(
        existing: List<WordUsageRecord>,
        languageTag: String,
        usageByCategory: Map<Category, Set<String>>,
    ): List<WordUsageRecord> {
        val selectedCategories = usageByCategory.keys

        val untouched = existing.filterNot {
            it.languageTag == languageTag && it.category in selectedCategories
        }

        val rebuilt = usageByCategory.flatMap { (category, words) ->
            words.map { normalizedWord ->
                WordUsageRecord(
                    languageTag = languageTag,
                    category = category,
                    normalizedWord = normalizedWord,
                )
            }
        }

        return (untouched + rebuilt)
            .distinctBy { Triple(it.languageTag, it.category, it.normalizedWord) }
    }
}
