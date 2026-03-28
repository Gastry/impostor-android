package com.impostorparty.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.impostorparty.data.model.StoredAppSettings
import com.impostorparty.data.model.StoredGameSetup
import com.impostorparty.data.model.StoredReviewPromptState
import com.impostorparty.data.model.StoredWordUsageRecord
import com.impostorparty.data.model.toDomain
import com.impostorparty.data.model.toDomainOrNull
import com.impostorparty.data.model.toStored
import com.impostorparty.data.store.settingsDataStore
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.ReviewPromptState
import com.impostorparty.domain.model.WordUsageRecord
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

    override val adsRemoved: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[ADS_REMOVED_KEY] ?: false
    }

    override val lastSetup: Flow<GameSetup?> = context.settingsDataStore.data.map { preferences ->
        preferences[LAST_SETUP_KEY]
            ?.decodeOrNull<StoredGameSetup>()
            ?.toDomainOrNull()
    }

    override val reviewPromptState: Flow<ReviewPromptState> = context.settingsDataStore.data.map { preferences ->
        preferences[REVIEW_PROMPT_STATE_KEY]
            .decodeOrNull<StoredReviewPromptState>()
            ?.toDomain()
            ?: ReviewPromptState()
    }

    override val wordUsageHistory: Flow<List<WordUsageRecord>> = context.settingsDataStore.data.map { preferences ->
        val records = preferences[WORD_USAGE_HISTORY_KEY]
            .decodeOrNull<List<StoredWordUsageRecord>>()
            .orEmpty()
            .mapNotNull(StoredWordUsageRecord::toDomainOrNull)

        records.distinctBy { Triple(it.languageTag, it.category, it.normalizedWord) }
    }

    override suspend fun saveAppSettings(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[APP_SETTINGS_KEY] = json.encodeToString(settings.toStored())
        }
    }

    override suspend fun saveAdsRemoved(removed: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[ADS_REMOVED_KEY] = removed
        }
    }

    override suspend fun saveLastSetup(setup: GameSetup) {
        context.settingsDataStore.edit { preferences ->
            preferences[LAST_SETUP_KEY] = json.encodeToString(setup.toStored())
        }
    }

    override suspend fun saveReviewPromptState(state: ReviewPromptState) {
        context.settingsDataStore.edit { preferences ->
            preferences[REVIEW_PROMPT_STATE_KEY] = json.encodeToString(state.toStored())
        }
    }

    override suspend fun saveWordUsageHistory(history: List<WordUsageRecord>) {
        val stored = history
            .distinctBy { Triple(it.languageTag, it.category, it.normalizedWord) }
            .map(WordUsageRecord::toStored)
            .groupBy { it.languageTag to it.category }
            .values
            .flatMap { it.takeLast(MAX_RECORDS_PER_LANGUAGE_CATEGORY) }

        context.settingsDataStore.edit { preferences ->
            preferences[WORD_USAGE_HISTORY_KEY] = json.encodeToString(stored)
        }
    }

    override suspend fun clearAllPreferences() {
        context.settingsDataStore.edit { preferences ->
            val adsRemoved = preferences[ADS_REMOVED_KEY] ?: false
            preferences.clear()
            preferences[ADS_REMOVED_KEY] = adsRemoved
        }
    }

    private inline fun <reified T> String?.decodeOrNull(): T? {
        if (this.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<T>(this) }.getOrNull()
    }

    private companion object {
        const val MAX_RECORDS_PER_LANGUAGE_CATEGORY = 128
        val APP_SETTINGS_KEY = stringPreferencesKey("app_settings_json")
        val ADS_REMOVED_KEY = booleanPreferencesKey("ads_removed")
        val LAST_SETUP_KEY = stringPreferencesKey("last_setup_json")
        val REVIEW_PROMPT_STATE_KEY = stringPreferencesKey("review_prompt_state_json")
        val WORD_USAGE_HISTORY_KEY = stringPreferencesKey("word_usage_history_json")
    }
}
