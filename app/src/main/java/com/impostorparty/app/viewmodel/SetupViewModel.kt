package com.impostorparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.WordRepository
import com.impostorparty.domain.usecase.CreateRoundResult
import com.impostorparty.domain.usecase.CreateRoundUseCase
import com.impostorparty.domain.usecase.GetAllowedImpostorCountsUseCase
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
            getAllowedImpostorCountsUseCase.clamp(adjusted)
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

    fun updateRoundMinutes(minutes: Int) {
        _setup.update { it.copy(suggestedRoundMinutes = minutes) }
    }

    fun updateClueRounds(rounds: Int) {
        _setup.update { it.copy(clueRounds = rounds.coerceIn(1, 3)) }
    }

    fun updateNoExtraHints(enabled: Boolean) {
        _setup.update { it.copy(noExtraHints = enabled) }
    }

    fun updateQuickMode(enabled: Boolean) {
        _setup.update { it.copy(quickMode = enabled) }
    }

    fun updateRevealAnimation(enabled: Boolean) {
        _setup.update { it.copy(revealAnimation = enabled) }
        updateAppSettings { it.copy(showRevealAnimation = enabled) }
    }

    fun updateHaptics(enabled: Boolean) {
        _setup.update { it.copy(hapticsEnabled = enabled) }
        updateAppSettings { it.copy(hapticsEnabled = enabled) }
    }

    fun updateAvoidRecentWords(enabled: Boolean) {
        _setup.update { it.copy(avoidRecentWords = enabled) }
        updateAppSettings { it.copy(avoidRecentWords = enabled) }
    }

    fun updateCustomPlayerName(index: Int, name: String) {
        _setup.update { current ->
            val list = current.customPlayerNames.toMutableList()
            while (list.size < current.playerCount) {
                list.add("")
            }
            list[index] = name
            current.copy(customPlayerNames = list)
        }
    }

    fun clearCustomNames() {
        _setup.update { it.copy(customPlayerNames = emptyList()) }
    }

    suspend fun createRound(
        currentAppLanguageTag: String?,
        sourceSetup: GameSetup? = null,
    ): CreateRoundResult {
        _isStartingRound.value = true
        val setupSnapshot = getAllowedImpostorCountsUseCase.clamp(
            fixedSetup(sourceSetup ?: _setup.value),
        )
        _setup.value = setupSnapshot

        val settingsSnapshot = preferencesRepository.appSettings.first()
        val effectiveLanguageTag = resolveEffectiveLanguageTag(
            preferredLanguageTag = settingsSnapshot.languageTag,
            currentAppLanguageTag = currentAppLanguageTag,
        )
        val result = createRoundUseCase(
            setup = setupSnapshot,
            activeLanguageTag = effectiveLanguageTag,
            wordUsageHistory = preferencesRepository.wordUsageHistory.first(),
            wordRepository = wordRepository,
            random = Random(System.nanoTime()),
            fallbackPlayerNames = localizedDefaultPlayerNames(
                playerCount = setupSnapshot.playerCount,
                languageTag = effectiveLanguageTag,
            ),
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
            avoidRecentWords = defaults.avoidRecentWords,
            quickMode = defaults.quickMode,
            customPlayerNames = defaults.customPlayerNames,
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

    private fun updateAppSettings(update: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = preferencesRepository.appSettings.first()
            preferencesRepository.saveAppSettings(update(current))
        }
    }
}
