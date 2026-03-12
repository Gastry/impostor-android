package com.impostorparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.PlayerAssignment
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.model.ThemeMode
import com.impostorparty.domain.model.WinnerSide
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.StatsRepository
import com.impostorparty.domain.repository.WordRepository
import com.impostorparty.domain.usecase.CreateRoundResult
import com.impostorparty.domain.usecase.CreateRoundUseCase
import com.impostorparty.domain.usecase.GetAllowedImpostorCountsUseCase
import com.impostorparty.domain.usecase.RecordRoundResultUseCase
import com.impostorparty.domain.usecase.ReduceRevealFlowStateUseCase
import com.impostorparty.domain.usecase.RevealFlowState
import com.impostorparty.domain.usecase.SetupValidationError
import com.impostorparty.domain.usecase.WordSelectionError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GameViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val preferencesRepository: PreferencesRepository,
    private val statsRepository: StatsRepository,
) : ViewModel() {

    private val createRoundUseCase = CreateRoundUseCase()
    private val getAllowedImpostorCountsUseCase = GetAllowedImpostorCountsUseCase()
    private val reduceRevealFlowStateUseCase = ReduceRevealFlowStateUseCase()
    private val recordRoundResultUseCase = RecordRoundResultUseCase()

    private val _setup = MutableStateFlow(GameSetup())
    val setup: StateFlow<GameSetup> = _setup.asStateFlow()

    private val _activeRound = MutableStateFlow<RoundSession?>(null)
    val activeRound: StateFlow<RoundSession?> = _activeRound.asStateFlow()

    private val _revealState = MutableStateFlow<RevealFlowState>(RevealFlowState.PassingPhone(0))
    val revealState: StateFlow<RevealFlowState> = _revealState.asStateFlow()

    private val _isStartingRound = MutableStateFlow(false)
    val isStartingRound: StateFlow<Boolean> = _isStartingRound.asStateFlow()

    private val _winnerSelection = MutableStateFlow(WinnerSide.UNDECIDED)
    val winnerSelection: StateFlow<WinnerSide> = _winnerSelection.asStateFlow()

    private val _message = MutableStateFlow<UiMessage?>(null)
    val message: StateFlow<UiMessage?> = _message.asStateFlow()

    val appSettings: StateFlow<AppSettings> = preferencesRepository.appSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings(),
    )

    val history = statsRepository.history.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    val stats = statsRepository.stats.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        com.impostorparty.domain.model.GameStats(),
    )

    private val recordedRoundIds = mutableSetOf<String>()
    private var setupLoaded = false

    init {
        viewModelScope.launch {
            appSettings.collect { settings ->
                _setup.update {
                    it.copy(
                        hapticsEnabled = settings.hapticsEnabled,
                        avoidRecentWords = settings.avoidRecentWords,
                        revealAnimation = settings.showRevealAnimation,
                    )
                }
            }
        }

        viewModelScope.launch {
            preferencesRepository.lastSetup.collect { saved ->
                if (setupLoaded) return@collect

                val base = saved ?: _setup.value
                _setup.value = getAllowedImpostorCountsUseCase.clamp(base)
                setupLoaded = true
            }
        }
    }

    fun dismissMessage() {
        _message.value = null
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

    fun startRound() {
        viewModelScope.launch {
            _isStartingRound.value = true
            val setupSnapshot = getAllowedImpostorCountsUseCase.clamp(_setup.value)
            _setup.value = setupSnapshot

            val settingsSnapshot = appSettings.first()
            val result = createRoundUseCase(
                setup = setupSnapshot,
                activeLanguageTag = settingsSnapshot.languageTag,
                wordUsageHistory = preferencesRepository.wordUsageHistory.first(),
                wordRepository = wordRepository,
                random = Random(System.nanoTime()),
            )

            when (result) {
                is CreateRoundResult.Success -> {
                    _activeRound.value = result.session
                    _revealState.value = RevealFlowState.PassingPhone(playerIndex = 0)
                    _winnerSelection.value = WinnerSide.UNDECIDED
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
        }
    }

    fun requestReveal() {
        _revealState.update { state -> reduceRevealFlowStateUseCase.onRevealRequested(state) }
    }

    fun hideAndPass() {
        val totalPlayers = _activeRound.value?.assignments?.size ?: return
        _revealState.update { state -> reduceRevealFlowStateUseCase.onHideRequested(state, totalPlayers) }
    }

    fun currentAssignment(): PlayerAssignment? {
        val round = _activeRound.value ?: return null
        val index = when (val state = _revealState.value) {
            is RevealFlowState.PassingPhone -> state.playerIndex
            is RevealFlowState.RevealingSecret -> state.playerIndex
            RevealFlowState.RoundReady -> return null
        }
        return round.assignments.getOrNull(index)
    }

    fun selectWinner(winnerSide: WinnerSide) {
        _winnerSelection.value = winnerSide
    }

    fun persistRoundResultIfNeeded() {
        val round = _activeRound.value ?: return
        if (!recordedRoundIds.add(round.id)) return

        viewModelScope.launch {
            recordRoundResultUseCase(
                session = round,
                winnerSide = _winnerSelection.value,
                statsRepository = statsRepository,
            )
        }
    }

    fun startRematch() {
        val previousSetup = _activeRound.value?.setup ?: _setup.value
        _setup.value = previousSetup
        startRound()
    }

    fun clearCurrentRound() {
        _activeRound.value = null
        _revealState.value = RevealFlowState.PassingPhone(0)
        _winnerSelection.value = WinnerSide.UNDECIDED
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        updateAppSettings { it.copy(themeMode = themeMode) }
    }

    fun updateLanguageTag(languageTag: String?) {
        updateAppSettings { it.copy(languageTag = languageTag?.ifBlank { null }) }
    }

    fun updateReducedMotion(enabled: Boolean) {
        updateAppSettings { it.copy(reducedMotion = enabled) }
    }

    fun updateShowQuickInstructions(enabled: Boolean) {
        updateAppSettings { it.copy(showQuickInstructions = enabled) }
    }

    fun updateSecureScreen(enabled: Boolean) {
        updateAppSettings { it.copy(secureScreen = enabled) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            statsRepository.clearHistory()
        }
    }

    fun resetPreferences() {
        viewModelScope.launch {
            preferencesRepository.clearAllPreferences()
            _setup.value = GameSetup()
        }
    }

    private fun updateAppSettings(update: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = appSettings.first()
            preferencesRepository.saveAppSettings(update(current))
        }
    }
}

enum class UiMessageType {
    INVALID_SETUP,
    WORDS_UNAVAILABLE,
    UNKNOWN,
}

data class UiMessage(
    val type: UiMessageType,
    val detail: String,
) {
    companion object {
        fun fromSetupError(error: SetupValidationError): UiMessage {
            return UiMessage(UiMessageType.INVALID_SETUP, error.name)
        }

        fun fromWordError(error: WordSelectionError): UiMessage {
            return UiMessage(UiMessageType.WORDS_UNAVAILABLE, error.name)
        }
    }
}


