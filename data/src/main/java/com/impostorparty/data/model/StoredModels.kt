package com.impostorparty.data.model

import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.ReviewPromptState
import com.impostorparty.domain.model.RoundHistoryEntry
import com.impostorparty.domain.model.ThemeMode
import com.impostorparty.domain.model.WinnerSide
import com.impostorparty.domain.model.WordUsageRecord
import kotlinx.serialization.Serializable

@Serializable
data class StoredGameSetup(
    val playerCount: Int,
    val impostorCount: Int,
    val categories: List<String>,
    val suggestedRoundMinutes: Int,
    val clueRounds: Int = 2,
    val noExtraHints: Boolean,
    val revealAnimation: Boolean,
    val hapticsEnabled: Boolean,
    val avoidRecentWords: Boolean,
    val quickMode: Boolean,
    val customPlayerNames: List<String>,
)

@Serializable
data class StoredAppSettings(
    val hapticsEnabled: Boolean,
    val reducedMotion: Boolean,
    val showQuickInstructions: Boolean,
    val themeMode: String,
    val languageTag: String?,
    val avoidRecentWords: Boolean,
    val secureScreen: Boolean,
    val showRevealAnimation: Boolean,
)

@Serializable
data class StoredReviewPromptState(
    val completedGames: Int = 0,
    val distinctUseDays: Int = 0,
    val sessionCount: Int = 0,
    val lastUsedEpochDay: Long? = null,
    val lastReviewAttemptEpochMillis: Long? = null,
    val lastReviewLaterEpochMillis: Long? = null,
    val lastCompletedRoundId: String? = null,
)

@Serializable
data class StoredRoundHistoryEntry(
    val id: String,
    val timestampEpochMillis: Long,
    val category: String,
    val word: String,
    val playerCount: Int,
    val impostorNames: List<String>,
    val winnerSide: String,
)

@Serializable
data class StoredWordUsageRecord(
    val languageTag: String,
    val category: String,
    val normalizedWord: String,
)

fun GameSetup.toStored(): StoredGameSetup = StoredGameSetup(
    playerCount = playerCount,
    impostorCount = impostorCount,
    categories = categories.map { it.code },
    suggestedRoundMinutes = suggestedRoundMinutes,
    clueRounds = clueRounds,
    noExtraHints = noExtraHints,
    revealAnimation = revealAnimation,
    hapticsEnabled = hapticsEnabled,
    avoidRecentWords = avoidRecentWords,
    quickMode = quickMode,
    customPlayerNames = customPlayerNames,
)

fun StoredGameSetup.toDomainOrNull(): GameSetup? {
    val categorySet = categories.mapNotNull(Category::fromCode).toSet()
    if (playerCount !in 3..12 || categorySet.isEmpty()) return null

    return GameSetup(
        playerCount = playerCount,
        impostorCount = impostorCount,
        categories = categorySet,
        suggestedRoundMinutes = suggestedRoundMinutes,
        clueRounds = clueRounds.coerceIn(1, 3),
        noExtraHints = noExtraHints,
        revealAnimation = revealAnimation,
        hapticsEnabled = hapticsEnabled,
        avoidRecentWords = avoidRecentWords,
        quickMode = quickMode,
        customPlayerNames = customPlayerNames,
    )
}

fun AppSettings.toStored(): StoredAppSettings = StoredAppSettings(
    hapticsEnabled = hapticsEnabled,
    reducedMotion = reducedMotion,
    showQuickInstructions = showQuickInstructions,
    themeMode = themeMode.name,
    languageTag = languageTag,
    avoidRecentWords = avoidRecentWords,
    secureScreen = secureScreen,
    showRevealAnimation = showRevealAnimation,
)

fun StoredAppSettings.toDomain(): AppSettings = AppSettings(
    hapticsEnabled = hapticsEnabled,
    reducedMotion = reducedMotion,
    showQuickInstructions = showQuickInstructions,
    themeMode = ThemeMode.entries.firstOrNull { it.name == themeMode } ?: ThemeMode.SYSTEM,
    languageTag = languageTag,
    avoidRecentWords = avoidRecentWords,
    secureScreen = secureScreen,
    showRevealAnimation = showRevealAnimation,
)

fun ReviewPromptState.toStored(): StoredReviewPromptState = StoredReviewPromptState(
    completedGames = completedGames,
    distinctUseDays = distinctUseDays,
    sessionCount = sessionCount,
    lastUsedEpochDay = lastUsedEpochDay,
    lastReviewAttemptEpochMillis = lastReviewAttemptEpochMillis,
    lastReviewLaterEpochMillis = lastReviewLaterEpochMillis,
    lastCompletedRoundId = lastCompletedRoundId,
)

fun StoredReviewPromptState.toDomain(): ReviewPromptState = ReviewPromptState(
    completedGames = completedGames.coerceAtLeast(0),
    distinctUseDays = distinctUseDays.coerceAtLeast(0),
    sessionCount = sessionCount.coerceAtLeast(0),
    lastUsedEpochDay = lastUsedEpochDay,
    lastReviewAttemptEpochMillis = lastReviewAttemptEpochMillis,
    lastReviewLaterEpochMillis = lastReviewLaterEpochMillis,
    lastCompletedRoundId = lastCompletedRoundId,
)

fun RoundHistoryEntry.toStored(): StoredRoundHistoryEntry = StoredRoundHistoryEntry(
    id = id,
    timestampEpochMillis = timestampEpochMillis,
    category = category.code,
    word = word,
    playerCount = playerCount,
    impostorNames = impostorNames,
    winnerSide = winnerSide.name,
)

fun StoredRoundHistoryEntry.toDomainOrNull(): RoundHistoryEntry? {
    val categoryValue = Category.fromCode(category) ?: return null
    val winner = WinnerSide.entries.firstOrNull { it.name == winnerSide } ?: WinnerSide.UNDECIDED

    return RoundHistoryEntry(
        id = id,
        timestampEpochMillis = timestampEpochMillis,
        category = categoryValue,
        word = word,
        playerCount = playerCount,
        impostorNames = impostorNames,
        winnerSide = winner,
    )
}

fun WordUsageRecord.toStored(): StoredWordUsageRecord = StoredWordUsageRecord(
    languageTag = languageTag,
    category = category.code,
    normalizedWord = normalizedWord,
)

fun StoredWordUsageRecord.toDomainOrNull(): WordUsageRecord? {
    val categoryValue = Category.fromCode(category) ?: return null
    val language = languageTag.trim().lowercase()
    val normalized = normalizedWord.trim().lowercase()
    if (language.isEmpty() || normalized.isEmpty()) return null

    return WordUsageRecord(
        languageTag = language,
        category = categoryValue,
        normalizedWord = normalized,
    )
}
