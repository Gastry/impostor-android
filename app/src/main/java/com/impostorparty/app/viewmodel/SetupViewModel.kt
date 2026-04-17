package com.impostorparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.WordRepository
import com.impostorparty.domain.usecase.CreateRoundResult
import com.impostorparty.domain.usecase.CreateRoundUseCase
import com.impostorparty.domain.usecase.GetAllowedImpostorCountsUseCase
import com.impostorparty.domain.usecase.SetupValidationError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val wordRepository: WordRepository,
    private val getAllowedImpostorCountsUseCase: GetAllowedImpostorCountsUseCase,
    private val createRoundUseCase: CreateRoundUseCase,
) : ViewModel() {

    private val _setup = MutableStateFlow(GameSetup())
    val setup: StateFlow<GameSetup> = _setup.asStateFlow()

    private val _isStartingRound = MutableStateFlow(false)
    val isStartingRound: StateFlow<Boolean> = _isStartingRound.asStateFlow()

    private val _message = MutableStateFlow<UiMessage?>(null)
    val message: StateFlow<UiMessage?> = _message.asStateFlow()

    private var setupLoaded = false

    init {
        viewModelScope.launch {
            preferencesRepository.lastSetup.collect { saved ->
                if (setupLoaded) return@collect

                val base = fixedSetup(saved ?: _setup.value)
                _setup.value = getAllowedImpostorCountsUseCase.clamp(base)
                setupLoaded = true
            }
        }
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun resetToDefaults() {
        _setup.value = GameSetup()
        _message.value = null
        setupLoaded = true
    }

    fun updatePlayerCount(count: Int) {
        _setup.update {
            val adjusted = it.copy(playerCount = count)
            getAllowedImpostorCountsUseCase.clamp(
                adjusted.copy(categories = adjusted.categories - Category.PLAYERS),
            )
        }
    }

    fun updateImpostorCount(count: Int) {
        _setup.update {
            getAllowedImpostorCountsUseCase.clamp(it.copy(impostorCount = count))
        }
    }

    fun toggleCategory(category: Category) {
        _setup.update { current ->
            val nextCategories = if (category in current.categories) {
                current.categories - category
            } else {
                current.categories + category
            }
            current.copy(categories = nextCategories)
        }
    }

    fun preparePlayerNames(currentAppLanguageTag: String?) {
        _setup.update { current ->
            val defaults = localizedDefaultPlayerNames(
                playerCount = current.playerCount,
                languageTag = currentAppLanguageTag,
            )
            val next = current.copy(customPlayerNames = current.resolvedPlayerNames(defaults))
            if (next.canUsePlayerNamesAsWords(defaults)) {
                next
            } else {
                next.copy(categories = next.categories - Category.PLAYERS)
            }
        }
    }

    fun updatePlayerName(index: Int, name: String) {
        _setup.update { current ->
            if (index !in 0 until current.playerCount) return@update current

            val names = current.customPlayerNames
                .normalizedSize(current.playerCount)
                .toMutableList()
            names[index] = name
            val next = current.copy(customPlayerNames = names)
            if (names.any { it.isBlank() }) {
                next.copy(categories = next.categories - Category.PLAYERS)
            } else {
                next
            }
        }
    }

    fun clearPlayerName(index: Int) {
        updatePlayerName(index, "")
    }

    fun clearPlayerNames() {
        _setup.update { current ->
            current.copy(
                categories = current.categories - Category.PLAYERS,
                customPlayerNames = List(current.playerCount) { "" },
            )
        }
    }

    fun canUsePlayerNamesAsWords(currentAppLanguageTag: String?): Boolean {
        val current = _setup.value
        val defaults = localizedDefaultPlayerNames(
            playerCount = current.playerCount,
            languageTag = currentAppLanguageTag,
        )
        return current.canUsePlayerNamesAsWords(defaults)
    }

    fun setPlayerNamesAsWordsEnabled(enabled: Boolean, currentAppLanguageTag: String?) {
        _setup.update { current ->
            val defaults = localizedDefaultPlayerNames(
                playerCount = current.playerCount,
                languageTag = currentAppLanguageTag,
            )
            val categories = if (enabled && current.canUsePlayerNamesAsWords(defaults)) {
                current.categories + Category.PLAYERS
            } else {
                current.categories - Category.PLAYERS
            }
            current.copy(categories = categories)
        }
    }

    suspend fun createRound(
        currentAppLanguageTag: String?,
        sourceSetup: GameSetup? = null,
    ): CreateRoundResult {
        _isStartingRound.value = true
        val clampedSetup = getAllowedImpostorCountsUseCase.clamp(
            fixedSetup(sourceSetup ?: _setup.value),
        )

        val settingsSnapshot = preferencesRepository.appSettings.first()
        val effectiveLanguageTag = resolveEffectiveLanguageTag(
            preferredLanguageTag = settingsSnapshot.languageTag,
            currentAppLanguageTag = currentAppLanguageTag,
        )
        val fallbackNames = localizedDefaultPlayerNames(
            playerCount = clampedSetup.playerCount,
            languageTag = effectiveLanguageTag,
        )
        if (Category.PLAYERS in clampedSetup.categories && !clampedSetup.canUsePlayerNamesAsWords(fallbackNames)) {
            val result = CreateRoundResult.InvalidSetup(SetupValidationError.PLAYER_NAMES_REQUIRED)
            _message.value = UiMessage.fromSetupError(result.error)
            _isStartingRound.value = false
            return result
        }
        val setupSnapshot = clampedSetup.copy(
            customPlayerNames = clampedSetup.resolvedPlayerNames(fallbackNames),
        )
        _setup.value = setupSnapshot

        val result = createRoundUseCase(
            setup = setupSnapshot,
            activeLanguageTag = effectiveLanguageTag,
            wordUsageHistory = preferencesRepository.wordUsageHistory.first(),
            wordRepository = wordRepository,
            random = Random(System.nanoTime()),
            fallbackPlayerNames = fallbackNames,
        )

        when (result) {
            is CreateRoundResult.Success -> {
                preferencesRepository.saveWordUsageHistory(result.updatedWordUsageHistory)
                preferencesRepository.saveLastSetup(setupSnapshot)
            }

            is CreateRoundResult.InvalidSetup -> {
                _message.value = UiMessage.fromSetupError(result.error)
            }

            is CreateRoundResult.WordSelectionFailed -> {
                _message.value = UiMessage.fromWordError(result.error)
            }
        }

        _isStartingRound.value = false
        return result
    }

    private fun fixedSetup(setup: GameSetup): GameSetup {
        val defaults = GameSetup()
        return setup.copy(
            suggestedRoundMinutes = defaults.suggestedRoundMinutes,
            clueRounds = defaults.clueRounds,
            noExtraHints = defaults.noExtraHints,
            revealAnimation = defaults.revealAnimation,
            hapticsEnabled = defaults.hapticsEnabled,
            avoidRecentWords = true,
            quickMode = defaults.quickMode,
            customPlayerNames = setup.customPlayerNames.normalizedSize(setup.playerCount),
        )
    }

    private fun localizedDefaultPlayerNames(playerCount: Int, languageTag: String?): List<String> {
        val label = when (languageTag?.substringBefore('-')?.lowercase(Locale.ROOT)) {
            "es" -> "Jugador"
            "fr" -> "Joueur"
            "de" -> "Spieler"
            "it" -> "Giocatore"
            "pt" -> "Jogador"
            "ja" -> "プレイヤー"
            else -> "Player"
        }
        return List(playerCount) { index -> "$label ${index + 1}" }
    }

    private fun GameSetup.resolvedPlayerNames(fallbackNames: List<String>): List<String> {
        return List(playerCount) { index ->
            customPlayerNames.getOrNull(index)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: fallbackNames.getOrNull(index)
                ?: "Player ${index + 1}"
        }
    }

    private fun List<String>.normalizedSize(playerCount: Int): List<String> {
        return List(playerCount) { index -> getOrNull(index).orEmpty() }
    }

    private fun GameSetup.canUsePlayerNamesAsWords(defaultNames: List<String>): Boolean {
        val names = customPlayerNames.normalizedSize(playerCount).map { it.trim() }
        return names.all { it.isNotBlank() } &&
            names.zip(defaultNames).all { (name, defaultName) -> name != defaultName }
    }

    private fun resolveEffectiveLanguageTag(
        preferredLanguageTag: String?,
        currentAppLanguageTag: String?,
    ): String {
        return preferredLanguageTag
            ?.ifBlank { null }
            ?: currentAppLanguageTag
                ?.ifBlank { null }
            ?: Locale.getDefault().toLanguageTag().ifBlank { "en" }
    }

}
