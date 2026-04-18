package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.impostorparty.app.BuildConfig
import com.impostorparty.app.R
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.app.viewmodel.UiMessage
import com.impostorparty.app.viewmodel.UiMessageType

@Composable
internal fun SetupErrorDialog(
    message: UiMessage?,
    onDismissMessage: () -> Unit,
) {
    if (message == null) return

    AlertDialog(
        onDismissRequest = onDismissMessage,
        confirmButton = {
            TextButton(onClick = onDismissMessage) {
                Text(stringResource(R.string.ok))
            }
        },
        title = { Text(stringResource(R.string.setup_error_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceXs)) {
                Text(
                    text = when (message.type) {
                        UiMessageType.INVALID_SETUP -> invalidSetupMessage(message.detail)
                        UiMessageType.WORDS_UNAVAILABLE -> wordsUnavailableMessage(message.detail)
                        UiMessageType.UNKNOWN -> stringResource(R.string.error_generic)
                    },
                )

                if (shouldShowTechnicalDiagnostic(message.detail)) {
                    Text(
                        text = message.detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Composable
private fun invalidSetupMessage(detail: String): String {
    return when (detail) {
        "PLAYER_COUNT_OUT_OF_RANGE" -> stringResource(R.string.error_player_count)
        "IMPOSTOR_COUNT_INVALID" -> stringResource(R.string.error_impostor_count)
        "CATEGORIES_EMPTY" -> stringResource(R.string.error_categories_empty)
        "ROUND_TIME_INVALID" -> stringResource(R.string.error_round_time)
        "CLUE_ROUNDS_INVALID" -> stringResource(R.string.error_clue_rounds)
        "NOT_ENOUGH_NON_IMPOSTORS" -> stringResource(R.string.error_not_enough_civilians)
        "PLAYER_NAMES_REQUIRED" -> stringResource(R.string.error_player_names_required)
        else -> stringResource(R.string.error_generic)
    }
}

@Composable
private fun wordsUnavailableMessage(detail: String): String {
    return when (detail) {
        "NO_CATEGORY_SELECTED" -> stringResource(R.string.error_categories_empty)
        "NO_WORDS_FOR_CATEGORY" -> stringResource(R.string.error_no_words_for_category)
        "NO_WORDS_AFTER_RECENT_FILTER" -> stringResource(R.string.error_no_words_recent_filter)
        else -> stringResource(R.string.error_generic)
    }
}

private fun shouldShowTechnicalDiagnostic(detail: String): Boolean {
    if (!BuildConfig.DEBUG || detail.isBlank()) return false

    val key = detail.trim().uppercase()
    return key.contains("PLAN") ||
        key.contains("STRATEGY") ||
        key.contains("TIMING") ||
        key.contains("POLICY") ||
        key.contains("COMPOSITION")
}
