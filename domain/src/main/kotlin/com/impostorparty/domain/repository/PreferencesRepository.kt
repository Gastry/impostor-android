package com.impostorparty.domain.repository

import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.ReviewPromptState
import com.impostorparty.domain.model.WordUsageRecord
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val appSettings: Flow<AppSettings>
    val lastSetup: Flow<GameSetup?>
    val reviewPromptState: Flow<ReviewPromptState>
    val wordUsageHistory: Flow<List<WordUsageRecord>>

    suspend fun saveAppSettings(settings: AppSettings)
    suspend fun saveLastSetup(setup: GameSetup)
    suspend fun saveReviewPromptState(state: ReviewPromptState)
    suspend fun saveWordUsageHistory(history: List<WordUsageRecord>)
    suspend fun clearAllPreferences()
}
