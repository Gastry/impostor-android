package com.impostorparty.app.viewmodel

import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.PlayerAssignment
import com.impostorparty.domain.model.PlayerSecret
import com.impostorparty.domain.model.PlayerSlot
import com.impostorparty.domain.model.ReviewPromptState
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.model.WinnerSide
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.usecase.RecordRoundResultUseCase
import com.impostorparty.domain.usecase.ReduceRevealFlowStateUseCase
import com.impostorparty.domain.usecase.RevealFlowState
import com.impostorparty.domain.usecase.ShouldShowReviewPromptUseCase
import com.impostorparty.domain.usecase.UpdateReviewPromptStateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameSessionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var statsRepository: FakeStatsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        preferencesRepository = FakePreferencesRepository()
        statsRepository = FakeStatsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): GameSessionViewModel {
        return GameSessionViewModel(
            preferencesRepository = preferencesRepository,
            statsRepository = statsRepository,
            reduceRevealFlowStateUseCase = ReduceRevealFlowStateUseCase(),
            recordRoundResultUseCase = RecordRoundResultUseCase(),
            shouldShowReviewPromptUseCase = ShouldShowReviewPromptUseCase(),
            updateReviewPromptStateUseCase = UpdateReviewPromptStateUseCase(),
        )
    }

    @Test
    fun `simulate 6 player session flow`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        val setup = GameSetup(playerCount = 6, impostorCount = 1)
        val assignments = (0..5).map { i ->
            PlayerAssignment(
                player = PlayerSlot(i, "Player ${i + 1}"),
                secret = if (i == 0) PlayerSecret.Impostor else PlayerSecret.Civilian("Pizza"),
            )
        }
        val session = RoundSession(
            id = "test-session",
            setup = setup,
            word = WordEntry("Pizza", Category.FOOD),
            assignments = assignments,
            createdAtEpochMillis = 123456789L,
        )

        viewModel.activateRound(session)
        assertEquals(session, viewModel.activeRound.value)
        assertTrue(viewModel.revealState.value is RevealFlowState.PassingPhone)

        viewModel.requestReveal()
        assertTrue(viewModel.revealState.value is RevealFlowState.RevealingSecret)
        assertEquals(PlayerSecret.Impostor, viewModel.currentAssignment()?.secret)

        viewModel.hideAndPass()
        assertTrue(viewModel.revealState.value is RevealFlowState.PassingPhone)

        viewModel.selectWinner(WinnerSide.IMPOSTORS)
        assertEquals(WinnerSide.IMPOSTORS, viewModel.winnerSelection.value)
    }

    @Test
    fun `persist round result stores history`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.activateRound(sampleSession())

        viewModel.selectWinner(WinnerSide.CIVILIANS)
        viewModel.persistRoundResultIfNeeded()
        advanceUntilIdle()

        assertEquals(1, statsRepository.history.value.size)
        assertEquals(1, statsRepository.stats.value.gamesPlayed)
    }

    @Test
    fun `review prompt does not show before five completed games`() = runTest(dispatcher) {
        preferencesRepository.reviewPromptState.value = ReviewPromptState(
            completedGames = 3,
            distinctUseDays = 2,
            sessionCount = 2,
        )
        val viewModel = buildViewModel()
        viewModel.activateRound(sampleSession())

        viewModel.onResultScreenViewed()
        advanceUntilIdle()

        assertNull(viewModel.reviewPrompt.value)
    }

    @Test
    fun `review prompt shows when conditions are met`() = runTest(dispatcher) {
        preferencesRepository.reviewPromptState.value = ReviewPromptState(
            completedGames = 4,
            distinctUseDays = 2,
            sessionCount = 2,
        )
        val viewModel = buildViewModel()
        viewModel.activateRound(sampleSession())

        viewModel.onResultScreenViewed()
        advanceUntilIdle()

        assertNotNull(viewModel.reviewPrompt.value)
    }

    @Test
    fun `review request is consumed even if play flow does not show`() = runTest(dispatcher) {
        val viewModel = buildViewModel()

        viewModel.onReviewNowSelected()
        advanceUntilIdle()

        assertNotNull(viewModel.pendingInAppReviewRequest.value)
        assertNull(viewModel.reviewPrompt.value)

        viewModel.onInAppReviewRequestHandled()

        assertNull(viewModel.pendingInAppReviewRequest.value)
    }

    private fun sampleSession(): RoundSession {
        return RoundSession(
            id = "round-1",
            setup = GameSetup(playerCount = 5, impostorCount = 1),
            word = WordEntry("Pizza", Category.FOOD),
            assignments = listOf(
                PlayerAssignment(PlayerSlot(0, "Player 1"), PlayerSecret.Impostor),
                PlayerAssignment(PlayerSlot(1, "Player 2"), PlayerSecret.Civilian("Pizza")),
                PlayerAssignment(PlayerSlot(2, "Player 3"), PlayerSecret.Civilian("Pizza")),
                PlayerAssignment(PlayerSlot(3, "Player 4"), PlayerSecret.Civilian("Pizza")),
                PlayerAssignment(PlayerSlot(4, "Player 5"), PlayerSecret.Civilian("Pizza")),
            ),
            createdAtEpochMillis = 123L,
        )
    }
}
