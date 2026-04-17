package com.impostorparty.app.viewmodel

import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.usecase.CreateRoundUseCase
import com.impostorparty.domain.usecase.GetAllowedImpostorCountsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var wordRepository: FakeWordRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        preferencesRepository = FakePreferencesRepository()
        wordRepository = FakeWordRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): SetupViewModel {
        return SetupViewModel(
            preferencesRepository = preferencesRepository,
            wordRepository = wordRepository,
            getAllowedImpostorCountsUseCase = GetAllowedImpostorCountsUseCase(),
            createRoundUseCase = CreateRoundUseCase(),
        )
    }

    @Test
    fun `default setup uses two clue rounds`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.setup.value.clueRounds)
    }

    @Test
    fun `create round uses current app locale when no language preference is saved`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.toggleCategory(Category.ANIMALS)
        viewModel.toggleCategory(Category.PLACES)

        val result = viewModel.createRound(currentAppLanguageTag = "es-ES")
        advanceUntilIdle()

        assertEquals("es-ES", wordRepository.lastRequestedLanguageTag)
        assertEquals("Paella", (result as com.impostorparty.domain.usecase.CreateRoundResult.Success).session.word.text)
    }

    @Test
    fun `saved language preference overrides current app locale`() = runTest(dispatcher) {
        preferencesRepository.appSettings.value = AppSettings(languageTag = "de")
        val viewModel = buildViewModel()
        viewModel.toggleCategory(Category.ANIMALS)
        viewModel.toggleCategory(Category.PLACES)
        advanceUntilIdle()

        val result = viewModel.createRound(currentAppLanguageTag = "es-ES")
        advanceUntilIdle()

        assertEquals("de", wordRepository.lastRequestedLanguageTag)
        assertEquals("Brezel", (result as com.impostorparty.domain.usecase.CreateRoundResult.Success).session.word.text)
    }

    @Test
    fun `reset to defaults clears in memory setup`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.updatePlayerCount(8)
        viewModel.toggleCategory(Category.FOOD)
        viewModel.resetToDefaults()

        assertEquals(GameSetup(), viewModel.setup.value)
    }

    @Test
    fun `saved player names are restored from last setup`() = runTest(dispatcher) {
        preferencesRepository.lastSetup.value = GameSetup(
            playerCount = 4,
            customPlayerNames = listOf("Ana", "Luis", "Mia", "Noa"),
        )

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(listOf("Ana", "Luis", "Mia", "Noa"), viewModel.setup.value.customPlayerNames)
    }

    @Test
    fun `prepare player names fills missing names with localized defaults`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.updatePlayerCount(3)
        viewModel.preparePlayerNames("es-ES")
        advanceUntilIdle()

        assertEquals(listOf("Jugador 1", "Jugador 2", "Jugador 3"), viewModel.setup.value.customPlayerNames)
    }

    @Test
    fun `create round replaces blank player names with defaults`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.updatePlayerCount(3)
        viewModel.preparePlayerNames("es-ES")
        viewModel.updatePlayerName(0, "Ana")
        viewModel.clearPlayerName(1)
        viewModel.updatePlayerName(2, "Luis")

        val result = viewModel.createRound(currentAppLanguageTag = "es-ES")
        advanceUntilIdle()

        val session = (result as com.impostorparty.domain.usecase.CreateRoundResult.Success).session
        assertEquals(listOf("Ana", "Jugador 2", "Luis"), session.assignments.map { it.player.name })
        assertEquals(listOf("Ana", "Jugador 2", "Luis"), preferencesRepository.lastSetup.value?.customPlayerNames)
    }

    @Test
    fun `player names category is available only with real names`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.updatePlayerCount(3)
        viewModel.preparePlayerNames("es-ES")

        assertFalse(viewModel.canUsePlayerNamesAsWords("es-ES"))

        viewModel.updatePlayerName(0, "Ana")
        viewModel.updatePlayerName(1, "Luis")
        viewModel.updatePlayerName(2, "Mia")

        assertTrue(viewModel.canUsePlayerNamesAsWords("es-ES"))
        viewModel.setPlayerNamesAsWordsEnabled(true, "es-ES")
        assertTrue(Category.PLAYERS in viewModel.setup.value.categories)

        viewModel.clearPlayerName(1)

        assertFalse(Category.PLAYERS in viewModel.setup.value.categories)
    }

    @Test
    fun `create round can select player names as words`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.updatePlayerCount(3)
        listOf(Category.FOOD, Category.ANIMALS, Category.OBJECTS, Category.PLACES).forEach(viewModel::toggleCategory)
        viewModel.updatePlayerName(0, "Ana")
        viewModel.updatePlayerName(1, "Luis")
        viewModel.updatePlayerName(2, "Mia")
        viewModel.setPlayerNamesAsWordsEnabled(true, "es-ES")

        val result = viewModel.createRound(currentAppLanguageTag = "es-ES")
        advanceUntilIdle()

        val session = (result as com.impostorparty.domain.usecase.CreateRoundResult.Success).session
        assertEquals(Category.PLAYERS, session.word.category)
        assertTrue(session.word.text in listOf("Ana", "Luis", "Mia"))
    }

    @Test
    fun `create round stores last setup and word history on success`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.toggleCategory(Category.FOOD)

        val result = viewModel.createRound(currentAppLanguageTag = "en")
        advanceUntilIdle()

        assertNotNull(preferencesRepository.lastSetup.value)
        assertNotNull((result as com.impostorparty.domain.usecase.CreateRoundResult.Success).updatedWordUsageHistory)
    }
}
