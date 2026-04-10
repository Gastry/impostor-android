package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.model.WordUsageRecord
import com.impostorparty.domain.repository.WordRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

sealed interface CreateRoundResult {
    data class Success(
        val session: RoundSession,
        val updatedWordUsageHistory: List<WordUsageRecord>,
    ) : CreateRoundResult

    data class InvalidSetup(val error: SetupValidationError) : CreateRoundResult
    data class WordSelectionFailed(val error: WordSelectionError) : CreateRoundResult
}

class CreateRoundUseCase @Inject constructor(
    private val validateGameSetupUseCase: ValidateGameSetupUseCase,
    private val buildPlayersUseCase: BuildPlayersUseCase,
    private val selectSecretWordUseCase: SelectSecretWordUseCase,
    private val assignRolesUseCase: AssignRolesUseCase,
) {
    constructor() : this(
        ValidateGameSetupUseCase(),
        BuildPlayersUseCase(),
        SelectSecretWordUseCase(),
        AssignRolesUseCase(),
    )

    suspend operator fun invoke(
        setup: GameSetup,
        activeLanguageTag: String?,
        wordUsageHistory: List<WordUsageRecord>,
        wordRepository: WordRepository,
        random: Random,
        fallbackPlayerNames: List<String> = emptyList(),
        timestampProvider: () -> Long = { System.currentTimeMillis() },
    ): CreateRoundResult {
        when (val validation = validateGameSetupUseCase(setup)) {
            is SetupValidationResult.Invalid -> return CreateRoundResult.InvalidSetup(validation.reason)
            SetupValidationResult.Valid -> Unit
        }

        val localizedWords = wordRepository.getWords(activeLanguageTag)
        val selectedWordResult = selectSecretWordUseCase(
            words = localizedWords.words,
            selectedCategories = setup.categories,
            languageTag = localizedWords.languageTag,
            wordUsageHistory = wordUsageHistory,
            avoidRecentWords = setup.avoidRecentWords,
            random = random,
        )

        val selectedWord = when (selectedWordResult) {
            is WordSelectionResult.Error -> return CreateRoundResult.WordSelectionFailed(selectedWordResult.type)
            is WordSelectionResult.Success -> selectedWordResult.word
        }

        val players = buildPlayersUseCase(
            playerCount = setup.playerCount,
            customNames = setup.customPlayerNames,
            fallbackNames = fallbackPlayerNames,
        )
        val assignments = assignRolesUseCase(
            players = players,
            impostorCount = setup.impostorCount,
            word = selectedWord.text,
            random = random,
        )

        return CreateRoundResult.Success(
            session = RoundSession(
                id = UUID.randomUUID().toString(),
                setup = setup,
                word = selectedWord,
                assignments = assignments,
                createdAtEpochMillis = timestampProvider(),
            ),
            updatedWordUsageHistory = (selectedWordResult as WordSelectionResult.Success).updatedWordUsageHistory,
        )
    }
}
