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
import org.junit.Assert.assertNotNull
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
    fun `create round stores last setup and word history on success`() = runTest(dispatcher) {
        val viewModel = buildViewModel()
        viewModel.toggleCategory(Category.FOOD)

        val result = viewModel.createRound(currentAppLanguageTag = "en")
        advanceUntilIdle()

        assertNotNull(preferencesRepository.lastSetup.value)
        assertNotNull((result as com.impostorparty.domain.usecase.CreateRoundResult.Success).updatedWordUsageHistory)
    }
}
