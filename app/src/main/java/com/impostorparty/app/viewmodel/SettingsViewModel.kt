package com.impostorparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.GameStats
import com.impostorparty.domain.model.RoundHistoryEntry
import com.impostorparty.domain.model.ThemeMode
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.StatsRepository
import com.impostorparty.domain.usecase.UpdateReviewPromptStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val statsRepository: StatsRepository,
    private val updateReviewPromptStateUseCase: UpdateReviewPromptStateUseCase,
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = preferencesRepository.appSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings(),
    )

    val adsRemoved: StateFlow<Boolean> = preferencesRepository.adsRemoved.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    val history: StateFlow<List<RoundHistoryEntry>> = statsRepository.history.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    val stats: StateFlow<GameStats> = statsRepository.stats.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        GameStats(),
    )

    private var reviewSessionTracked = false

    init {
        registerSessionIfNeeded()
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

    fun updateHaptics(enabled: Boolean) {
        updateAppSettings { it.copy(hapticsEnabled = enabled) }
    }

    fun updateAvoidRecentWords(enabled: Boolean) {
        updateAppSettings { it.copy(avoidRecentWords = enabled) }
    }

    fun updateRevealAnimation(enabled: Boolean) {
        updateAppSettings { it.copy(showRevealAnimation = enabled) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            statsRepository.clearHistory()
        }
    }

    fun resetPreferences() {
        viewModelScope.launch {
            preferencesRepository.clearAllPreferences()
        }
    }

    private fun updateAppSettings(update: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = preferencesRepository.appSettings.first()
            preferencesRepository.saveAppSettings(update(current))
        }
    }

    private fun registerSessionIfNeeded() {
        if (reviewSessionTracked) return
        reviewSessionTracked = true

        viewModelScope.launch {
            val current = preferencesRepository.reviewPromptState.first()
            val updated = updateReviewPromptStateUseCase.onSessionStarted(current, System.currentTimeMillis())
            preferencesRepository.saveReviewPromptState(updated)
        }
    }
}
