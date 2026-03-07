package com.impostorparty.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_STORE = "impostor_party_settings"
private const val STATS_STORE = "impostor_party_stats"

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_STORE)
val Context.statsDataStore: DataStore<Preferences> by preferencesDataStore(name = STATS_STORE)