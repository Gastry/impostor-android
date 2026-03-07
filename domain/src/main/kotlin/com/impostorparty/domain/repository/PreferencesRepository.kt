package com.impostorparty.domain.repository

import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.GameSetup
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val appSettings: Flow<AppSettings>
    val lastSetup: Flow<GameSetup?>
    val recentWords: Flow<List<String>>

    suspend fun saveAppSettings(settings: AppSettings)
    suspend fun saveLastSetup(setup: GameSetup)
    suspend fun saveRecentWords(words: List<String>)
    suspend fun clearAllPreferences()
}