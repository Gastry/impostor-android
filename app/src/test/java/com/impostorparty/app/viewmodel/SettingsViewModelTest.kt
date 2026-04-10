package com.impostorparty.app.viewmodel

import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.ThemeMode
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

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

    private fun buildViewModel(): SettingsViewModel {
        return SettingsViewModel(
            preferencesRepository = preferencesRepository,
            statsRepository = statsRepository,
            updateReviewPromptStateUseCase = UpdateReviewPromptStateUseCase(),
        )
    }

    @Test
    fun `updates app settings`() = runTest(dispatcher) {
        val viewModel = buildViewModel()

        viewModel.updateThemeMode(ThemeMode.DARK)
        viewModel.updateLanguageTag("fr")
        viewModel.updateReducedMotion(true)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, preferencesRepository.appSettings.value.themeMode)
        assertEquals("fr", preferencesRepository.appSettings.value.languageTag)
        assertTrue(preferencesRepository.appSettings.value.reducedMotion)
    }

    @Test
    fun `reset preferences preserves purchase flag`() = runTest(dispatcher) {
        preferencesRepository.adsRemoved.value = true
        preferencesRepository.appSettings.value = AppSettings(languageTag = "de")
        val viewModel = buildViewModel()

        viewModel.resetPreferences()
        advanceUntilIdle()

        assertTrue(preferencesRepository.adsRemoved.value)
        assertEquals(AppSettings(), preferencesRepository.appSettings.value)
    }

    @Test
    fun `clear history resets stats`() = runTest(dispatcher) {
        statsRepository.stats.value = com.impostorparty.domain.model.GameStats(gamesPlayed = 4)
        val viewModel = buildViewModel()

        viewModel.clearHistory()
        advanceUntilIdle()

        assertTrue(statsRepository.history.value.isEmpty())
        assertFalse(statsRepository.stats.value.gamesPlayed > 0)
    }
}
