package com.impostorparty.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    var showAdvanced by remember { mutableStateOf(false) }
    var showNameFields by remember { mutableStateOf(setup.customPlayerNames.any { it.isNotBlank() }) }

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

                Text(
                    text = stringResource(R.string.setup_clue_rounds_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.setup_clue_rounds_support),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = PartyDimens.SpaceXs),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_clue_rounds_row"),
                    horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                ) {
                    listOf(1, 2, 3).forEach { option ->
                        FilterChip(
                            modifier = Modifier.testTag("setup_clue_round_$option"),
                            selected = setup.clueRounds == option,
                            onClick = { onClueRoundsChanged(option) },
                            label = { Text(stringResource(clueRoundsOptionRes(option))) },
                        )
                    }
                }

                Text(
                    text = stringResource(clueRoundsHintRes(setup.clueRounds)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = PartyDimens.SpaceSm),
                )
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

            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Icon(
                    imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.setup_advanced_options))
            }

            AnimatedVisibility(visible = showAdvanced) {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                        Text(
                            text = stringResource(R.string.setup_round_time_title, setup.suggestedRoundMinutes),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Slider(
                            value = setup.suggestedRoundMinutes.toFloat(),
                            onValueChange = { onRoundMinutesChanged(it.toInt()) },
                            valueRange = 3f..20f,
                            steps = 16,
                        )

                        ToggleRow(
                            title = stringResource(R.string.setup_no_extra_hints),
                            checked = setup.noExtraHints,
                            onCheckedChanged = onNoExtraHintsChanged,
                        )
                        ToggleRow(
                            title = stringResource(R.string.setup_show_animation),
                            checked = setup.revealAnimation,
                            onCheckedChanged = onRevealAnimationChanged,
                        )
                        ToggleRow(
                            title = stringResource(R.string.setup_haptics),
                            checked = setup.hapticsEnabled,
                            onCheckedChanged = onHapticsChanged,
                        )
                        ToggleRow(
                            title = stringResource(R.string.setup_avoid_recent),
                            checked = setup.avoidRecentWords,
                            onCheckedChanged = onAvoidRecentWordsChanged,
                        )
                        ToggleRow(
                            title = stringResource(R.string.setup_quick_mode),
                            checked = setup.quickMode,
                            onCheckedChanged = onQuickModeChanged,
                        )
                        ToggleRow(
                            title = stringResource(R.string.setup_custom_names),
                            checked = showNameFields,
                            onCheckedChanged = {
                                showNameFields = it
                                if (!it) onClearCustomNames()
                            },
                        )

                        AnimatedVisibility(visible = showNameFields) {
                            Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceXs)) {
                                repeat(setup.playerCount) { index ->
                                    OutlinedTextField(
                                        value = setup.customPlayerNames.getOrNull(index).orEmpty(),
                                        onValueChange = { onCustomPlayerNameChanged(index, it) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = {
                                            Text(stringResource(R.string.setup_player_name_label, index + 1))
                                        },
                                    )
                                }
                            }
                        }
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
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChanged)
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

private fun clueRoundsOptionRes(option: Int): Int {
    return when (option) {
        1 -> R.string.setup_clue_round_option_1
        2 -> R.string.setup_clue_round_option_2
        else -> R.string.setup_clue_round_option_3
    }
}

private fun clueRoundsHintRes(option: Int): Int {
    return when (option) {
        1 -> R.string.setup_clue_round_hint_1
        2 -> R.string.setup_clue_round_hint_2
        else -> R.string.setup_clue_round_hint_3
    }
}





