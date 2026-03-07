package com.impostorparty.data.repository

import android.content.Context
import com.impostorparty.data.model.WordDataset
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.repository.WordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private var cache: List<WordEntry>? = null

    override suspend fun getWords(): List<WordEntry> = mutex.withLock {
        cache ?: loadWords().also { cache = it }
    }

    private suspend fun loadWords(): List<WordEntry> = withContext(Dispatchers.IO) {
        val raw = context.assets.open(DATASET_FILE).bufferedReader().use { it.readText() }
        val dataset = json.decodeFromString<WordDataset>(raw)

        dataset.categories.flatMap { categoryPayload ->
            val category = Category.fromCode(categoryPayload.code)
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

    private companion object {
        const val DATASET_FILE = "words_v1.json"
    }
}