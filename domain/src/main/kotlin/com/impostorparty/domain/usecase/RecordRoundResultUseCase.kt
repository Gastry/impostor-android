package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.GameStats
import com.impostorparty.domain.model.PlayerSecret
import com.impostorparty.domain.model.RoundHistoryEntry
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.model.WinnerSide
import com.impostorparty.domain.repository.StatsRepository
import javax.inject.Inject

class RecordRoundResultUseCase @Inject constructor() {
    suspend operator fun invoke(
        session: RoundSession,
        winnerSide: WinnerSide,
        statsRepository: StatsRepository,
    ) {
        val impostorNames = session.assignments
            .filter { it.secret is PlayerSecret.Impostor }
            .map { it.player.name }

        val entry = RoundHistoryEntry(
            id = session.id,
            timestampEpochMillis = System.currentTimeMillis(),
            category = session.word.category,
            word = session.word.text,
            playerCount = session.setup.playerCount,
            impostorNames = impostorNames,
            winnerSide = winnerSide,
        )

        statsRepository.recordRound(entry)
    }
}

class BuildStatsUseCase @Inject constructor() {
    operator fun invoke(history: List<RoundHistoryEntry>): GameStats {
        val usage = history.groupingBy { it.category }.eachCount()
        return GameStats(
            gamesPlayed = history.size,
            categoryUsage = usage,
            lastPlayerCount = history.lastOrNull()?.playerCount ?: 0,
        )
    }
}
