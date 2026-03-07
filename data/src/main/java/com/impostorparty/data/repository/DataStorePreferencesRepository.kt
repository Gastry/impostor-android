package com.impostorparty.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.impostorparty.data.model.StoredAppSettings
import com.impostorparty.data.model.StoredGameSetup
import com.impostorparty.data.model.toDomain
import com.impostorparty.data.model.toDomainOrNull
import com.impostorparty.data.model.toStored
import com.impostorparty.data.store.settingsDataStore
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class DataStorePreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : PreferencesRepository {

    override val appSettings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        val encoded = preferences[APP_SETTINGS_KEY]
        encoded?.decodeOrNull<StoredAppSettings>()?.toDomain() ?: AppSettings()
    }

    override val lastSetup: Flow<GameSetup?> = context.settingsDataStore.data.map { preferences ->
        preferences[LAST_SETUP_KEY]
            ?.decodeOrNull<StoredGameSetup>()
            ?.toDomainOrNull()
    }

    override val recentWords: Flow<List<String>> = context.settingsDataStore.data.map { preferences ->
        preferences[RECENT_WORDS_KEY].decodeOrNull<List<String>>() ?: emptyList()
    }

    override suspend fun saveAppSettings(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[APP_SETTINGS_KEY] = json.encodeToString(settings.toStored())
        }
    }

    override suspend fun saveLastSetup(setup: GameSetup) {
        context.settingsDataStore.edit { preferences ->
            preferences[LAST_SETUP_KEY] = json.encodeToString(setup.toStored())
        }
    }

    override suspend fun saveRecentWords(words: List<String>) {
        context.settingsDataStore.edit { preferences ->
            preferences[RECENT_WORDS_KEY] = json.encodeToString(words)
        }
    }

    override suspend fun clearAllPreferences() {
        context.settingsDataStore.edit { it.clear() }
    }

    private inline fun <reified T> String?.decodeOrNull(): T? {
        if (this.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<T>(this) }.getOrNull()
    }

    private companion object {
        val APP_SETTINGS_KEY = stringPreferencesKey("app_settings_json")
        val LAST_SETUP_KEY = stringPreferencesKey("last_setup_json")
        val RECENT_WORDS_KEY = stringPreferencesKey("recent_words_json")
    }
}