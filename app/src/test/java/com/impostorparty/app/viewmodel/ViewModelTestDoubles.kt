package com.impostorparty.app.viewmodel

import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackSubmission
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.GameStats
import com.impostorparty.domain.model.LocalizedWordPool
import com.impostorparty.domain.model.ReviewPromptState
import com.impostorparty.domain.model.RoundHistoryEntry
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.model.WordUsageRecord
import com.impostorparty.domain.repository.FeedbackRepository
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.StatsRepository
import com.impostorparty.domain.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class FakeWordRepository : WordRepository {
    var lastRequestedLanguageTag: String? = null

    override suspend fun getWords(languageTag: String?): LocalizedWordPool {
        lastRequestedLanguageTag = languageTag
        val resolved = languageTag?.substringBefore('-')?.ifBlank { "en" } ?: "en"
        val word = when (resolved) {
            "es" -> "Paella"
            "de" -> "Brezel"
            "fr" -> "Baguette"
            "it" -> "Pasta"
            "pt" -> "Feijoada"
            "ja" -> "すし"
            else -> "Pizza"
        }
        return LocalizedWordPool(
            languageTag = resolved,
            words = listOf(
                WordEntry(word, Category.FOOD),
                WordEntry("Tiger", Category.ANIMALS),
                WordEntry("Airport", Category.PLACES),
            ),
        )
    }
}

internal class FakePreferencesRepository : PreferencesRepository {
    override val appSettings = MutableStateFlow(AppSettings())
    override val adsRemoved = MutableStateFlow(false)
    override val lastSetup = MutableStateFlow<GameSetup?>(null)
    override val reviewPromptState = MutableStateFlow(ReviewPromptState())
    override val wordUsageHistory = MutableStateFlow<List<WordUsageRecord>>(emptyList())

    override suspend fun saveAppSettings(settings: AppSettings) {
        appSettings.value = settings
    }

    override suspend fun saveAdsRemoved(removed: Boolean) {
        adsRemoved.value = removed
    }

    override suspend fun saveLastSetup(setup: GameSetup) {
        lastSetup.value = setup
    }

    override suspend fun saveReviewPromptState(state: ReviewPromptState) {
        reviewPromptState.value = state
    }

    override suspend fun saveWordUsageHistory(history: List<WordUsageRecord>) {
        wordUsageHistory.value = history
    }

    override suspend fun clearAllPreferences() {
        val purchased = adsRemoved.value
        appSettings.value = AppSettings()
        adsRemoved.value = purchased
        lastSetup.value = null
        reviewPromptState.value = ReviewPromptState()
        wordUsageHistory.value = emptyList()
    }
}

internal class FakeFeedbackRepository : FeedbackRepository {
    val sentRequests = mutableListOf<FeedbackSubmission>()
    private val queuedResults = ArrayDeque<FeedbackSendResult>()

    fun enqueueResult(result: FeedbackSendResult) {
        queuedResults.addLast(result)
    }

    override suspend fun sendFeedback(submission: FeedbackSubmission): FeedbackSendResult {
        sentRequests += submission
        return queuedResults.removeFirstOrNull() ?: FeedbackSendResult.Success
    }
}

internal class FakeStatsRepository : StatsRepository {
    override val history = MutableStateFlow<List<RoundHistoryEntry>>(emptyList())
    override val stats = MutableStateFlow(GameStats())

    override suspend fun recordRound(entry: RoundHistoryEntry) {
        history.update { listOf(entry) + it }
        stats.value = GameStats(
            gamesPlayed = history.value.size,
            categoryUsage = history.value.groupingBy { it.category }.eachCount(),
            lastPlayerCount = history.value.firstOrNull()?.playerCount ?: 0,
        )
    }

    override suspend fun clearHistory() {
        history.value = emptyList()
        stats.value = GameStats()
    }
}
