package com.impostorparty.data.di

import com.impostorparty.data.repository.AssetWordRepository
import com.impostorparty.data.repository.AppsScriptFeedbackRepository
import com.impostorparty.data.repository.DataStorePreferencesRepository
import com.impostorparty.data.repository.DataStoreStatsRepository
import com.impostorparty.domain.repository.FeedbackRepository
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.repository.StatsRepository
import com.impostorparty.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {

    @Binds
    @Singleton
    abstract fun bindWordRepository(impl: AssetWordRepository): WordRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: DataStorePreferencesRepository): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: DataStoreStatsRepository): StatsRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(impl: AppsScriptFeedbackRepository): FeedbackRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataSerializationModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }
}
