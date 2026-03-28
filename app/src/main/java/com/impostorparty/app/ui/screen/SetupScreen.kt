package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.impostorparty.app.BuildConfig
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.app.util.titleRes
import com.impostorparty.app.viewmodel.UiMessage
import com.impostorparty.app.viewmodel.UiMessageType
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    setup: GameSetup,
    isLoading: Boolean,
    message: UiMessage?,
    onDismissMessage: () -> Unit,
    onPlayerCountChanged: (Int) -> Unit,
    onImpostorCountChanged: (Int) -> Unit,
    onToggleCategory: (Category) -> Unit,
    onRoundMinutesChanged: (Int) -> Unit,
    onClueRoundsChanged: (Int) -> Unit,
    onNoExtraHintsChanged: (Boolean) -> Unit,
    onQuickModeChanged: (Boolean) -> Unit,
    onRevealAnimationChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onAvoidRecentWordsChanged: (Boolean) -> Unit,
    onCustomPlayerNameChanged: (Int, String) -> Unit,
    onClearCustomNames: () -> Unit,
    onStartRound: () -> Unit,
    onBack: () -> Unit,
) {
    PartyScaffold(
        title = stringResource(R.string.setup_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = PartyDimens.SpaceMd),
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
        ) {
            Text(
                text = stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.setup_players_count, setup.playerCount),
                    style = MaterialTheme.typography.titleMedium,
                )
                Slider(
                    value = setup.playerCount.toFloat(),
                    onValueChange = { onPlayerCountChanged(it.toInt()) },
                    valueRange = 3f..12f,
                    steps = 8,
                )

                Text(
                    text = stringResource(R.string.setup_impostor_count_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                    val options = if (setup.playerCount >= 6) listOf(1, 2) else listOf(1)
                    options.forEach { option ->
                        FilterChip(
                            selected = setup.impostorCount == option,
                            onClick = { onImpostorCountChanged(option) },
                            label = { Text(stringResource(R.string.setup_impostor_count_item, option)) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PartyDimens.SpaceMd))
            }

            PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.setup_categories_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(PartyDimens.SpaceSm))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                ) {
                    Category.entries.forEach { category ->
                        FilterChip(
                            selected = category in setup.categories,
                            onClick = { onToggleCategory(category) },
                            label = { Text(stringResource(category.titleRes())) },
                        )
                    }
                }
            }

            PrimaryPartyButton(
                text = stringResource(R.string.setup_start_button),
                onClick = onStartRound,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_start_button"),
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(modifier = Modifier.height(PartyDimens.SpaceMd))
        }
    }

    if (message != null) {
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

