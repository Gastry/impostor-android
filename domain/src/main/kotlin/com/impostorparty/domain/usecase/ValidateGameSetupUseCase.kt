package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.GameSetup

sealed interface SetupValidationResult {
    data object Valid : SetupValidationResult
    data class Invalid(val reason: SetupValidationError) : SetupValidationResult
}

enum class SetupValidationError {
    PLAYER_COUNT_OUT_OF_RANGE,
    IMPOSTOR_COUNT_INVALID,
    CATEGORIES_EMPTY,
    ROUND_TIME_INVALID,
    CLUE_ROUNDS_INVALID,
    NOT_ENOUGH_NON_IMPOSTORS,
}

class ValidateGameSetupUseCase(
    private val getAllowedImpostorCountsUseCase: GetAllowedImpostorCountsUseCase = GetAllowedImpostorCountsUseCase(),
) {
    operator fun invoke(setup: GameSetup): SetupValidationResult {
        if (setup.playerCount !in 3..12) {
            return SetupValidationResult.Invalid(SetupValidationError.PLAYER_COUNT_OUT_OF_RANGE)
        }
        if (setup.impostorCount !in getAllowedImpostorCountsUseCase(setup.playerCount)) {
            return SetupValidationResult.Invalid(SetupValidationError.IMPOSTOR_COUNT_INVALID)
        }
        if (setup.categories.isEmpty()) {
            return SetupValidationResult.Invalid(SetupValidationError.CATEGORIES_EMPTY)
        }
        if (setup.suggestedRoundMinutes !in 3..20) {
            return SetupValidationResult.Invalid(SetupValidationError.ROUND_TIME_INVALID)
        }
        if (setup.clueRounds !in 1..3) {
            return SetupValidationResult.Invalid(SetupValidationError.CLUE_ROUNDS_INVALID)
        }
        if (setup.playerCount - setup.impostorCount < 2) {
            return SetupValidationResult.Invalid(SetupValidationError.NOT_ENOUGH_NON_IMPOSTORS)
        }
        return SetupValidationResult.Valid
    }
}
