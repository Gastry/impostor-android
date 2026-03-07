package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.GameSetup
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.repository.WordRepository
import java.util.UUID
import kotlin.random.Random

sealed interface CreateRoundResult {
    data class Success(val session: RoundSession) : CreateRoundResult
    data class InvalidSetup(val error: SetupValidationError) : CreateRoundResult
    data class WordSelectionFailed(val error: WordSelectionError) : CreateRoundResult
}

class CreateRoundUseCase(
    private val validateGameSetupUseCase: ValidateGameSetupUseCase = ValidateGameSetupUseCase(),
    private val buildPlayersUseCase: BuildPlayersUseCase = BuildPlayersUseCase(),
    private val selectSecretWordUseCase: SelectSecretWordUseCase = SelectSecretWordUseCase(),
    private val assignRolesUseCase: AssignRolesUseCase = AssignRolesUseCase(),
) {
    suspend operator fun invoke(
        setup: GameSetup,
        recentWords: List<String>,
        wordRepository: WordRepository,
        random: Random,
        timestampProvider: () -> Long = { System.currentTimeMillis() },
    ): CreateRoundResult {
        when (val validation = validateGameSetupUseCase(setup)) {
            is SetupValidationResult.Invalid -> return CreateRoundResult.InvalidSetup(validation.reason)
            SetupValidationResult.Valid -> Unit
        }

        val words = wordRepository.getWords()
        val selectedWord = when (
            val selection = selectSecretWordUseCase(
                words = words,
                selectedCategories = setup.categories,
                recentWords = recentWords,
                avoidRecentWords = setup.avoidRecentWords,
                random = random,
            )
        ) {
            is WordSelectionResult.Error -> return CreateRoundResult.WordSelectionFailed(selection.type)
            is WordSelectionResult.Success -> selection.word
        }

        val players = buildPlayersUseCase(setup.playerCount, setup.customPlayerNames)
        val assignments = assignRolesUseCase(
            players = players,
            impostorCount = setup.impostorCount,
            word = selectedWord.text,
            random = random,
        )

        return CreateRoundResult.Success(
            RoundSession(
                id = UUID.randomUUID().toString(),
                setup = setup,
                word = selectedWord,
                assignments = assignments,
                createdAtEpochMillis = timestampProvider(),
            ),
        )
    }
}