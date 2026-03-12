package com.impostorparty.app.viewmodel

import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.LocalizedWordPool
import com.impostorparty.domain.model.RoundHistoryEntry
import com.impostorparty.domain.model.WinnerSide
import com.impostorparty.domain.model.WordEntry
import com.impostorparty.domain.model.WordUsageRecord
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var statsRepository: FakeStatsRepository
    private lateinit var wordRepository: FakeWordRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        preferencesRepository = FakePreferencesRepository()
        statsRepository = FakeStatsRepository()
        wordRepository = FakeWordRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default setup uses two clue rounds`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, preferencesRepository, statsRepository)
        advanceUntilIdle()

        assertEquals(2, viewModel.setup.value.clueRounds)
    }

    @Test
    fun `user can choose clue rounds and value is kept in created round`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, preferencesRepository, statsRepository)
        viewModel.updateClueRounds(3)

        viewModel.startRound()
        advanceUntilIdle()

        assertEquals(3, viewModel.setup.value.clueRounds)
        assertEquals(3, viewModel.activeRound.value?.setup?.clueRounds)
    }

    @Test
    fun `start round creates active session`() = runTest(dispatcher) {
        val viewModel = GameViewModel(wordRepository, preferencesRepository, statsRepository)
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
        val viewModel = GameViewModel(wordRepository, preferencesRepository, statsRepository)
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
        val viewModel = GameViewModel(wordRepository, preferencesRepository, statsRepository)
        viewModel.startRound()
        advanceUntilIdle()

        viewModel.selectWinner(WinnerSide.CIVILIANS)
        viewModel.persistRoundResultIfNeeded()
        advanceUntilIdle()

        assertEquals(1, statsRepository.history.value.size)
        assertEquals(1, statsRepository.stats.value.gamesPlayed)
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
    override val wordUsageHistory = MutableStateFlow<List<WordUsageRecord>>(emptyList())

    override suspend fun saveAppSettings(settings: AppSettings) {
        appSettings.value = settings
    }

    override suspend fun saveLastSetup(setup: GameSetup) {
        lastSetup.value = setup
    }

    override suspend fun saveWordUsageHistory(history: List<WordUsageRecord>) {
        wordUsageHistory.value = history
    }

    override suspend fun clearAllPreferences() {
        appSettings.value = AppSettings()
        lastSetup.value = null
        wordUsageHistory.value = emptyList()
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

