package com.impostorparty.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class AppSettings(
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val showQuickInstructions: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String? = null,
    val avoidRecentWords: Boolean = true,
    val secureScreen: Boolean = true,
    val showRevealAnimation: Boolean = true,
)

enum class WinnerSide {
    CIVILIANS,
    IMPOSTORS,
    UNDECIDED,
}

data class RoundHistoryEntry(
    val id: String,
    val timestampEpochMillis: Long,
    val category: Category,
    val word: String,
    val playerCount: Int,
    val impostorNames: List<String>,
    val winnerSide: WinnerSide,
)

data class GameStats(
    val gamesPlayed: Int = 0,
    val categoryUsage: Map<Category, Int> = emptyMap(),
    val lastPlayerCount: Int = 0,
)