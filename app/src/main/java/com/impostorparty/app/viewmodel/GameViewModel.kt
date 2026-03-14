package com.impostorparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostorparty.app.BuildConfig
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.FeedbackContext
import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackSubmission
import com.impostorparty.domain.model.FeedbackType
import com.impostorparty.domain.model.FeedbackValidationError
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.PlayerAssignment
import com.impostorparty.domain.model.ReviewPromptState
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.model.ThemeMode
import com.impostorparty.domain.model.WinnerSide
import com.impostorparty.domain.repository.FeedbackRepository
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.StatsRepository
import com.impostorparty.domain.repository.WordRepository
import com.impostorparty.domain.usecase.CreateRoundResult
import com.impostorparty.domain.usecase.CreateRoundUseCase
import com.impostorparty.domain.usecase.GetAllowedImpostorCountsUseCase
import com.impostorparty.domain.usecase.RecordRoundResultUseCase
import com.impostorparty.domain.usecase.ReduceRevealFlowStateUseCase
import com.impostorparty.domain.usecase.RevealFlowState
import com.impostorparty.domain.usecase.SendFeedbackUseCase
import com.impostorparty.domain.usecase.ShouldShowReviewPromptUseCase
import com.impostorparty.domain.usecase.SetupValidationError
import com.impostorparty.domain.usecase.UpdateReviewPromptStateUseCase
import com.impostorparty.domain.usecase.ValidateFeedbackInputUseCase
import com.impostorparty.domain.usecase.WordSelectionError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import java.util.Locale
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
    private val feedbackRepository: FeedbackRepository,
    private val preferencesRepository: PreferencesRepository,
    private val statsRepository: StatsRepository,
) : ViewModel() {

    private val createRoundUseCase = CreateRoundUseCase()
    private val getAllowedImpostorCountsUseCase = GetAllowedImpostorCountsUseCase()
    private val reduceRevealFlowStateUseCase = ReduceRevealFlowStateUseCase()
    private val recordRoundResultUseCase = RecordRoundResultUseCase()
    private val shouldShowReviewPromptUseCase = ShouldShowReviewPromptUseCase()
    private val updateReviewPromptStateUseCase = UpdateReviewPromptStateUseCase()
    private val validateFeedbackInputUseCase = ValidateFeedbackInputUseCase()
    private val sendFeedbackUseCase = SendFeedbackUseCase(feedbackRepository)

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

    private val _reviewPrompt = MutableStateFlow<ReviewPromptUiState?>(null)
    val reviewPrompt: StateFlow<ReviewPromptUiState?> = _reviewPrompt.asStateFlow()

    private val _pendingInAppReviewRequest = MutableStateFlow<Long?>(null)
    val pendingInAppReviewRequest: StateFlow<Long?> = _pendingInAppReviewRequest.asStateFlow()

    private val _feedbackForm = MutableStateFlow(FeedbackFormUiState())
    val feedbackForm: StateFlow<FeedbackFormUiState> = _feedbackForm.asStateFlow()

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
    private val reviewTrackedRoundIds = mutableSetOf<String>()
    private var lastFeedbackContextHint: FeedbackContextHint? = null
    private var setupLoaded = false
    private var reviewSessionTracked = false

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

        registerSessionIfNeeded()
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun dismissReviewPrompt() {
        viewModelScope.launch {
            updateReviewPromptState { state ->
                updateReviewPromptStateUseCase.onRemindLater(state, System.currentTimeMillis())
            }
            _reviewPrompt.value = null
        }
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

    fun onResultScreenViewed() {
        val round = _activeRound.value ?: return
        if (!reviewTrackedRoundIds.add(round.id)) return
        lastFeedbackContextHint = FeedbackContextHint(
            clueRounds = round.setup.clueRounds,
            playerCount = round.setup.playerCount,
        )

        viewModelScope.launch {
            val updated = updateReviewPromptState { state ->
                updateReviewPromptStateUseCase.onRoundCompleted(state, round.id)
            }
            if (shouldShowReviewPromptUseCase(updated, System.currentTimeMillis())) {
                _reviewPrompt.value = ReviewPromptUiState(round.id)
            }
        }
    }

    fun onReviewNowSelected() {
        viewModelScope.launch {
            updateReviewPromptState { state ->
                updateReviewPromptStateUseCase.onReviewAttempted(state, System.currentTimeMillis())
            }
            _reviewPrompt.value = null
            _pendingInAppReviewRequest.value = System.nanoTime()
        }
    }

    fun onSendSuggestionSelected() {
        viewModelScope.launch {
            updateReviewPromptState { state ->
                updateReviewPromptStateUseCase.onRemindLater(state, System.currentTimeMillis())
            }
            _reviewPrompt.value = null
        }
    }

    fun launchManualReviewFlow() {
        viewModelScope.launch {
            updateReviewPromptState { state ->
                updateReviewPromptStateUseCase.onReviewAttempted(state, System.currentTimeMillis())
            }
            _pendingInAppReviewRequest.value = System.nanoTime()
        }
    }

    fun onInAppReviewRequestHandled() {
        _pendingInAppReviewRequest.value = null
    }

    fun updateFeedbackType(type: FeedbackType) {
        _feedbackForm.update { it.copy(type = type, isSuccess = false, sendResult = null) }
    }

    fun updateFeedbackMessage(message: String) {
        _feedbackForm.update {
            it.copy(
                message = message,
                validationErrors = it.validationErrors - setOf(
                    FeedbackValidationError.MESSAGE_REQUIRED,
                    FeedbackValidationError.MESSAGE_TOO_SHORT,
                ),
                isSuccess = false,
                sendResult = null,
            )
        }
    }

    fun updateFeedbackEmail(email: String) {
        _feedbackForm.update {
            it.copy(
                email = email,
                validationErrors = it.validationErrors - FeedbackValidationError.EMAIL_INVALID,
                isSuccess = false,
                sendResult = null,
            )
        }
    }

    fun submitFeedback() {
        val current = _feedbackForm.value
        if (current.isSending) return

        val validationErrors = validateFeedbackInputUseCase(current.message, current.email)
        if (validationErrors.isNotEmpty()) {
            _feedbackForm.update { it.copy(validationErrors = validationErrors, isSuccess = false, sendResult = null) }
            return
        }

        _feedbackForm.value = current.copy(
            isSending = true,
            validationErrors = emptySet(),
            isSuccess = false,
            sendResult = null,
        )

        viewModelScope.launch {
            val settingsSnapshot = appSettings.first()
            val localeTag = settingsSnapshot.languageTag
                ?.ifBlank { null }
                ?: Locale.getDefault().toLanguageTag().ifBlank { "en" }

            val feedbackContext = FeedbackContext(
                appVersion = BuildConfig.VERSION_NAME,
                locale = localeTag,
                timestampEpochMillis = System.currentTimeMillis(),
                clueRounds = lastFeedbackContextHint?.clueRounds,
                playerCount = lastFeedbackContextHint?.playerCount,
            )

            val result = sendFeedbackUseCase(
                FeedbackSubmission(
                    type = current.type,
                    message = current.message.trim(),
                    email = current.email.trim().ifBlank { null },
                    context = feedbackContext,
                ),
            )

            _feedbackForm.update { state ->
                if (result == FeedbackSendResult.Success) {
                    FeedbackFormUiState(
                        type = state.type,
                        isSuccess = true,
                    )
                } else {
                    state.copy(
                        isSending = false,
                        sendResult = result,
                    )
                }
            }
        }
    }

    fun retryFeedbackSubmission() {
        submitFeedback()
    }

    fun clearFeedbackStatus() {
        _feedbackForm.update { it.copy(isSuccess = false, sendResult = null, validationErrors = emptySet()) }
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

    private fun registerSessionIfNeeded() {
        if (reviewSessionTracked) return
        reviewSessionTracked = true

        viewModelScope.launch {
            updateReviewPromptState { state ->
                updateReviewPromptStateUseCase.onSessionStarted(state, System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateReviewPromptState(
        update: (ReviewPromptState) -> ReviewPromptState,
    ): ReviewPromptState {
        val current = preferencesRepository.reviewPromptState.first()
        val updated = update(current)
        preferencesRepository.saveReviewPromptState(updated)
        return updated
    }
}

data class ReviewPromptUiState(
    val roundId: String,
)

data class FeedbackFormUiState(
    val type: FeedbackType = FeedbackType.SUGGESTION,
    val message: String = "",
    val email: String = "",
    val isSending: Boolean = false,
    val isSuccess: Boolean = false,
    val sendResult: FeedbackSendResult? = null,
    val validationErrors: Set<FeedbackValidationError> = emptySet(),
)

data class FeedbackContextHint(
    val clueRounds: Int?,
    val playerCount: Int?,
)

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


