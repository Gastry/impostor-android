package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.PromoBannerSlot
import com.impostorparty.app.ui.components.partyFilterChipColors
import com.impostorparty.app.ui.components.partyOutlinedTextFieldColors
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.app.util.titleRes
import com.impostorparty.app.viewmodel.UiMessage
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup

@Composable
fun PlayerNamesScreen(
    setup: GameSetup,
    adsEnabled: Boolean,
    bannerAdUnitId: String?,
    removeAdsPriceLabel: String?,
    onOpenRemoveAdsSettings: () -> Unit,
    isLoading: Boolean,
    message: UiMessage?,
    onDismissMessage: () -> Unit,
    onNameChanged: (Int, String) -> Unit,
    onClearName: (Int) -> Unit,
    onClearAll: () -> Unit,
    playerNamesAsWordsAvailable: Boolean,
    onPlayerNamesAsWordsChanged: (Boolean) -> Unit,
    onStartRound: () -> Unit,
    onBack: () -> Unit,
) {
    val playerNames = List(setup.playerCount) { index ->
        setup.customPlayerNames.getOrNull(index).orEmpty()
    }
    val includePlayerNamesAsWords = Category.PLAYERS in setup.categories

    LaunchedEffect(playerNamesAsWordsAvailable, includePlayerNamesAsWords) {
        if (!playerNamesAsWordsAvailable && includePlayerNamesAsWords) {
            onPlayerNamesAsWordsChanged(false)
        }
    }

    PartyScaffold(
        title = stringResource(R.string.player_names_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        Column(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .padding(top = PartyDimens.SpaceMd)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
            ) {
                Text(
                    text = stringResource(R.string.player_names_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                PartySectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.setup_players_count, setup.playerCount),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            TextButton(
                                onClick = onClearAll,
                                enabled = playerNames.any { it.isNotBlank() } && !isLoading,
                            ) {
                                Text(stringResource(R.string.player_names_clear_all))
                            }
                        }

                        Text(
                            text = stringResource(R.string.player_names_empty_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.player_names_saved_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        playerNames.forEachIndexed { index, name ->
                            PlayerNameField(
                                index = index,
                                name = name,
                                enabled = !isLoading,
                                onNameChanged = onNameChanged,
                                onClearName = onClearName,
                            )
                        }
                    }
                }

                PartySectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                    ) {
                        Text(
                            text = stringResource(R.string.player_names_word_category_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.player_names_word_category_support),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FilterChip(
                            selected = includePlayerNamesAsWords,
                            enabled = playerNamesAsWordsAvailable && !isLoading,
                            onClick = { onPlayerNamesAsWordsChanged(!includePlayerNamesAsWords) },
                            label = { Text(stringResource(Category.PLAYERS.titleRes())) },
                            colors = partyFilterChipColors(),
                        )
                        if (!playerNamesAsWordsAvailable) {
                            Text(
                                text = stringResource(R.string.player_names_word_category_disabled),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PartyDimens.SpaceSm))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(bottom = PartyDimens.SpaceSm),
                    )
                }
                PrimaryPartyButton(
                    text = stringResource(R.string.setup_start_button),
                    onClick = onStartRound,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PartyDimens.SpaceMd, vertical = PartyDimens.SpaceSm)
                        .testTag("player_names_start_button"),
                )
                PromoBannerSlot(
                    adsEnabled = adsEnabled,
                    adUnitId = bannerAdUnitId,
                    removeAdsPriceLabel = removeAdsPriceLabel,
                    onRemoveAdsClick = onOpenRemoveAdsSettings,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    SetupErrorDialog(
        message = message,
        onDismissMessage = onDismissMessage,
    )
}

@Composable
private fun PlayerNameField(
    index: Int,
    name: String,
    enabled: Boolean,
    onNameChanged: (Int, String) -> Unit,
    onClearName: (Int) -> Unit,
) {
    OutlinedTextField(
        value = name,
        onValueChange = { onNameChanged(index, it) },
        enabled = enabled,
        singleLine = true,
        label = { Text(stringResource(R.string.setup_player_name_label, index + 1)) },
        placeholder = { Text(stringResource(R.string.player_names_empty_placeholder)) },
        trailingIcon = {
            if (name.isNotBlank()) {
                IconButton(
                    onClick = { onClearName(index) },
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(
                            R.string.player_names_clear_field_content_description,
                            index + 1,
                        ),
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        colors = partyOutlinedTextFieldColors(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_name_${index + 1}"),
    )
}
