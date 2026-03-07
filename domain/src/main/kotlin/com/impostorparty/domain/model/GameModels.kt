package com.impostorparty.domain.model

data class WordEntry(
    val text: String,
    val category: Category,
)

data class GameSetup(
    val playerCount: Int = 6,
    val impostorCount: Int = 1,
    val categories: Set<Category> = Category.defaultSelection,
    val suggestedRoundMinutes: Int = 8,
    val noExtraHints: Boolean = true,
    val revealAnimation: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val avoidRecentWords: Boolean = true,
    val quickMode: Boolean = false,
    val customPlayerNames: List<String> = emptyList(),
) {
    fun playerNameAt(index: Int): String {
        val custom = customPlayerNames.getOrNull(index)?.trim().orEmpty()
        return custom.ifBlank { "Player ${index + 1}" }
    }
}

data class PlayerSlot(
    val index: Int,
    val name: String,
)

sealed interface PlayerSecret {
    data class Civilian(val word: String) : PlayerSecret
    data object Impostor : PlayerSecret
}

data class PlayerAssignment(
    val player: PlayerSlot,
    val secret: PlayerSecret,
)

data class RoundSession(
    val id: String,
    val setup: GameSetup,
    val word: WordEntry,
    val assignments: List<PlayerAssignment>,
    val createdAtEpochMillis: Long,
)