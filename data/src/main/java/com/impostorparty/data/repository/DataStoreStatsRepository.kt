package com.impostorparty.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.impostorparty.data.model.StoredRoundHistoryEntry
import com.impostorparty.data.model.toDomainOrNull
import com.impostorparty.data.model.toStored
import com.impostorparty.data.store.statsDataStore
import com.impostorparty.domain.model.GameStats
import com.impostorparty.domain.model.RoundHistoryEntry
import com.impostorparty.domain.repository.StatsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class DataStoreStatsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : StatsRepository {

    override val history: Flow<List<RoundHistoryEntry>> = context.statsDataStore.data.map { preferences ->
        preferences[HISTORY_KEY]
            .decodeOrNull<List<StoredRoundHistoryEntry>>()
            .orEmpty()
            .mapNotNull { it.toDomainOrNull() }
            .sortedByDescending { it.timestampEpochMillis }
    }

    override val stats: Flow<GameStats> = history.map { entries ->
        GameStats(
            gamesPlayed = entries.size,
            categoryUsage = entries.groupingBy { it.category }.eachCount(),
            lastPlayerCount = entries.firstOrNull()?.playerCount ?: 0,
        )
    }

    override suspend fun recordRound(entry: RoundHistoryEntry) {
        context.statsDataStore.edit { preferences ->
            val current = preferences[HISTORY_KEY]
                .decodeOrNull<List<StoredRoundHistoryEntry>>()
                .orEmpty()
                .filterNot { it.id == entry.id }

            val updated = (listOf(entry.toStored()) + current).take(MAX_HISTORY_ENTRIES)
            preferences[HISTORY_KEY] = json.encodeToString(updated)
        }
    }

    override suspend fun clearHistory() {
        context.statsDataStore.edit { preferences ->
            preferences.remove(HISTORY_KEY)
        }
    }

    private inline fun <reified T> String?.decodeOrNull(): T? {
        if (this.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<T>(this) }.getOrNull()
    }

    private companion object {
        val HISTORY_KEY = stringPreferencesKey("round_history_json")
        const val MAX_HISTORY_ENTRIES = 120
    }
}