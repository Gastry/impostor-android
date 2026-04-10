package com.impostorparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostorparty.domain.model.PlayerAssignment
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.model.WinnerSide
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.StatsRepository
import com.impostorparty.domain.usecase.RecordRoundResultUseCase
import com.impostorparty.domain.usecase.ReduceRevealFlowStateUseCase
import com.impostorparty.domain.usecase.RevealFlowState
import com.impostorparty.domain.usecase.ShouldShowReviewPromptUseCase
import com.impostorparty.domain.usecase.UpdateReviewPromptStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GameSessionViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val statsRepository: StatsRepository,
    private val reduceRevealFlowStateUseCase: ReduceRevealFlowStateUseCase,
    private val recordRoundResultUseCase: RecordRoundResultUseCase,
    private val shouldShowReviewPromptUseCase: ShouldShowReviewPromptUseCase,
    private val updateReviewPromptStateUseCase: UpdateReviewPromptStateUseCase,
) : ViewModel() {

    private val _activeRound = MutableStateFlow<RoundSession?>(null)
    val activeRound: StateFlow<RoundSession?> = _activeRound.asStateFlow()

    private val _revealState = MutableStateFlow<RevealFlowState>(RevealFlowState.PassingPhone(0))
    val revealState: StateFlow<RevealFlowState> = _revealState.asStateFlow()

    private val _winnerSelection = MutableStateFlow(WinnerSide.UNDECIDED)
    val winnerSelection: StateFlow<WinnerSide> = _winnerSelection.asStateFlow()

    private val _reviewPrompt = MutableStateFlow<ReviewPromptUiState?>(null)
    val reviewPrompt: StateFlow<ReviewPromptUiState?> = _reviewPrompt.asStateFlow()

    private val _pendingInAppReviewRequest = MutableStateFlow<Long?>(null)
    val pendingInAppReviewRequest: StateFlow<Long?> = _pendingInAppReviewRequest.asStateFlow()

    private val recordedRoundIds = mutableSetOf<String>()
    private val reviewTrackedRoundIds = mutableSetOf<String>()
    private var lastFeedbackContextHint: FeedbackContextHint? = null

    fun activateRound(session: RoundSession) {
        _activeRound.value = session
        _revealState.value = RevealFlowState.PassingPhone(playerIndex = 0)
        _winnerSelection.value = WinnerSide.UNDECIDED
        _reviewPrompt.value = null
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

    fun onInAppReviewRequestHandled() {
        _pendingInAppReviewRequest.value = null
    }

    fun dismissReviewPrompt() {
        viewModelScope.launch {
            updateReviewPromptState { state ->
                updateReviewPromptStateUseCase.onRemindLater(state, System.currentTimeMillis())
            }
            _reviewPrompt.value = null
        }
    }

    fun clearCurrentRound() {
        _activeRound.value = null
        _revealState.value = RevealFlowState.PassingPhone(0)
        _winnerSelection.value = WinnerSide.UNDECIDED
    }

    fun getFeedbackContextHint(): FeedbackContextHint? = lastFeedbackContextHint

    private suspend fun updateReviewPromptState(
        update: (com.impostorparty.domain.model.ReviewPromptState) -> com.impostorparty.domain.model.ReviewPromptState,
    ): com.impostorparty.domain.model.ReviewPromptState {
        val current = preferencesRepository.reviewPromptState.first()
        val updated = update(current)
        preferencesRepository.saveReviewPromptState(updated)
        return updated
    }
}
