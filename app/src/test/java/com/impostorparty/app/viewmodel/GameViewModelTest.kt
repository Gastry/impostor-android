package com.impostorparty.app.viewmodel

import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackSubmission
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.LocalizedWordPool
import com.impostorparty.domain.model.ReviewPromptState
import com.impostorparty.domain.model.RoundHistoryEntry
import com.impostorparty.domain.model.WinnerSide
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.model.WordUsageRecord
import com.impostorparty.domain.repository.FeedbackRepository
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.StatsRepository
import com.impostorparty.domain.repository.WordRepository
import com.impostorparty.domain.usecase.RevealFlowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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
class GameViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var statsRepository: FakeStatsRepository
    private lateinit var wordRepository: FakeWordRepository
    private lateinit var feedbackRepository: FakeFeedbackRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        preferencesRepository = FakePreferencesRepository()
        statsRepository = FakeStatsRepository()
        wordRepository = FakeWordRepository()
        feedbackRepository = FakeFeedbackRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default setup uses two clue rounds`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)
        advanceUntilIdle()

        assertEquals(2, viewModel.setup.value.clueRounds)
    }

    @Test
    fun `clue rounds stay fixed to default when creating a round`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)
        viewModel.updateClueRounds(3)

        viewModel.startRound()
        advanceUntilIdle()

        assertEquals(2, viewModel.setup.value.clueRounds)
        assertEquals(2, viewModel.activeRound.value?.setup?.clueRounds)
    }

    @Test
    fun `start round creates active session`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)
        viewModel.updatePlayerCount(6)
        viewModel.updateImpostorCount(1)
        viewModel.toggleCategory(Category.FOOD)

        viewModel.startRound()
        advanceUntilIdle()

        assertNotNull(viewModel.activeRound.value)
        assertTrue(viewModel.revealState.value is RevealFlowState.PassingPhone)
    }

    @Test
    fun `reveal state transitions to round ready`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)
        viewModel.updatePlayerCount(3)
        viewModel.startRound()
        advanceUntilIdle()

        repeat(3) {
            viewModel.requestReveal()
            viewModel.hideAndPass()
        }

        assertEquals(RevealFlowState.RoundReady, viewModel.revealState.value)
    }

    @Test
    fun `persist round result stores history`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)
        viewModel.startRound()
        advanceUntilIdle()

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
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)
        viewModel.startRound()
        advanceUntilIdle()

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
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)
        viewModel.startRound()
        advanceUntilIdle()

        viewModel.onResultScreenViewed()
        advanceUntilIdle()

        assertNotNull(viewModel.reviewPrompt.value)
    }

    @Test
    fun `review request is consumed even if play flow does not show`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)

        viewModel.onReviewNowSelected()
        advanceUntilIdle()

        assertNotNull(viewModel.pendingInAppReviewRequest.value)
        assertNull(viewModel.reviewPrompt.value)

        viewModel.onInAppReviewRequestHandled()

        assertNull(viewModel.pendingInAppReviewRequest.value)
    }

    @Test
    fun `feedback requires message`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)

        viewModel.updateFeedbackMessage(" ")
        viewModel.submitFeedback()
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.validationErrors.isNotEmpty())
        assertEquals(0, feedbackRepository.sentRequests.size)
    }

    @Test
    fun `feedback rejects invalid optional email`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)

        viewModel.updateFeedbackMessage("Enough detail here")
        viewModel.updateFeedbackEmail("broken-email")
        viewModel.submitFeedback()
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.validationErrors.isNotEmpty())
        assertEquals(0, feedbackRepository.sentRequests.size)
    }

    @Test
    fun `feedback rejects messages over max length`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)

        viewModel.updateFeedbackMessage("a".repeat(1_201))
        viewModel.submitFeedback()
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.validationErrors.isNotEmpty())
        assertEquals(0, feedbackRepository.sentRequests.size)
    }

    @Test
    fun `feedback submit success updates ui state`() = runTest(dispatcher) {
        feedbackRepository.enqueueResult(FeedbackSendResult.Success)
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)

        viewModel.updateFeedbackMessage("Enough detail here")
        viewModel.updateFeedbackEmail("mail@example.com")
        viewModel.submitFeedback()
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.isSuccess)
        assertEquals(1, feedbackRepository.sentRequests.size)
    }

    @Test
    fun `feedback submit network error can retry`() = runTest(dispatcher) {
        feedbackRepository.enqueueResult(FeedbackSendResult.NetworkError)
        feedbackRepository.enqueueResult(FeedbackSendResult.Success)
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)

        viewModel.updateFeedbackMessage("Enough detail here")
        viewModel.submitFeedback()
        advanceUntilIdle()

        assertEquals(FeedbackSendResult.NetworkError, viewModel.feedbackForm.value.sendResult)

        viewModel.retryFeedbackSubmission()
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.isSuccess)
        assertEquals(2, feedbackRepository.sentRequests.size)
    }

    @Test
    fun `feedback double tap does not duplicate sends`() = runTest(dispatcher) {
        feedbackRepository.enqueueResult(FeedbackSendResult.Success)
        val viewModel = GameViewModel(wordRepository, feedbackRepository, preferencesRepository, statsRepository)

        viewModel.updateFeedbackMessage("Enough detail here")
        viewModel.submitFeedback()
        viewModel.submitFeedback()
        advanceUntilIdle()

        assertEquals(1, feedbackRepository.sentRequests.size)
    }
}

private class FakeWordRepository : WordRepository {
    override suspend fun getWords(languageTag: String?): LocalizedWordPool {
        val resolved = languageTag?.substringBefore('-')?.ifBlank { "en" } ?: "en"
        return LocalizedWordPool(
            languageTag = resolved,
            words = listOf(
                WordEntry("Pizza", Category.FOOD),
                WordEntry("Tiger", Category.ANIMALS),
                WordEntry("Airport", Category.PLACES),
            ),
        )
    }
}

private class FakePreferencesRepository : PreferencesRepository {
    override val appSettings = MutableStateFlow(AppSettings())
    override val lastSetup = MutableStateFlow<GameSetup?>(null)
    override val reviewPromptState = MutableStateFlow(ReviewPromptState())
    override val wordUsageHistory = MutableStateFlow<List<WordUsageRecord>>(emptyList())

    override suspend fun saveAppSettings(settings: AppSettings) {
        appSettings.value = settings
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
        appSettings.value = AppSettings()
        lastSetup.value = null
        reviewPromptState.value = ReviewPromptState()
        wordUsageHistory.value = emptyList()
    }
}

private class FakeFeedbackRepository : FeedbackRepository {
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

private class FakeStatsRepository : StatsRepository {
    override val history = MutableStateFlow<List<RoundHistoryEntry>>(emptyList())
    override val stats = MutableStateFlow(com.impostorparty.domain.model.GameStats())

    override suspend fun recordRound(entry: RoundHistoryEntry) {
        history.update { listOf(entry) + it }
        stats.value = com.impostorparty.domain.model.GameStats(
            gamesPlayed = history.value.size,
            categoryUsage = history.value.groupingBy { it.category }.eachCount(),
            lastPlayerCount = history.value.firstOrNull()?.playerCount ?: 0,
        )
    }

    override suspend fun clearHistory() {
        history.value = emptyList()
        stats.value = com.impostorparty.domain.model.GameStats()
    }
}
