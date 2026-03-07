package com.impostorparty.domain.repository

import com.impostorparty.domain.model.GameStats
import com.impostorparty.domain.model.RoundHistoryEntry
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    val stats: Flow<GameStats>
    val history: Flow<List<RoundHistoryEntry>>

    suspend fun recordRound(entry: RoundHistoryEntry)
    suspend fun clearHistory()
}