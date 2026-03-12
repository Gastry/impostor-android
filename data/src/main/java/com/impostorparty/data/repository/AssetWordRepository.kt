package com.impostorparty.data.repository

import android.content.Context
import com.impostorparty.data.model.WordDataset
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.LocalizedWordPool
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.repository.WordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class AssetWordRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : WordRepository {

    private val mutex = Mutex()
    private var cache: CachedWordDataset? = null

    override suspend fun getWords(languageTag: String?): LocalizedWordPool = mutex.withLock {
        val dataset = cache ?: loadWords().also { cache = it }
        val resolvedLanguage = resolveLanguageTag(
            requested = languageTag,
            available = dataset.wordsByLanguage.keys,
            fallback = dataset.fallbackLanguage,
        )

        val words = dataset.wordsByLanguage[resolvedLanguage]
            ?: dataset.wordsByLanguage[dataset.fallbackLanguage]
            ?: emptyList()

        LocalizedWordPool(
            languageTag = if (words.isEmpty()) dataset.fallbackLanguage else resolvedLanguage,
            words = words,
        )
    }

    private suspend fun loadWords(): CachedWordDataset = withContext(Dispatchers.IO) {
        val raw = context.assets.open(DATASET_FILE).bufferedReader().use { it.readText() }
        val dataset = json.decodeFromString<WordDataset>(raw)

        WordDatasetValidator.validateOrThrow(dataset)

        val wordsByLanguage = dataset.languages.associate { language ->
            val normalizedTag = language.tag.trim().lowercase()
            normalizedTag to language.categories.flatMap { categoryPayload ->
                val category = Category.fromCode(categoryPayload.code.trim().lowercase())
                if (category == null) {
                    emptyList()
                } else {
                    categoryPayload.words.map { word ->
                        WordEntry(
                            text = word.trim(),
                            category = category,
                        )
                    }
                }
            }.filter { it.text.isNotEmpty() }
        }

        CachedWordDataset(
            fallbackLanguage = dataset.fallbackLanguage.trim().lowercase(),
            wordsByLanguage = wordsByLanguage,
        )
    }

    private fun resolveLanguageTag(
        requested: String?,
        available: Set<String>,
        fallback: String,
    ): String {
        val normalizedRequested = requested
            ?.trim()
            ?.replace('_', '-')
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }

        val systemLocale = Locale.getDefault()
        val systemTag = systemLocale.toLanguageTag().lowercase()
        val systemLanguage = systemLocale.language.lowercase()

        val candidates = listOfNotNull(
            normalizedRequested,
            normalizedRequested?.substringBefore('-'),
            systemTag,
            systemTag.substringBefore('-'),
            systemLanguage,
            fallback,
        )

        return candidates.firstOrNull { it in available }
            ?: available.firstOrNull()
            ?: fallback
    }

    private data class CachedWordDataset(
        val fallbackLanguage: String,
        val wordsByLanguage: Map<String, List<WordEntry>>,
    )

    private companion object {
        const val DATASET_FILE = "words_v1.json"
    }
}
