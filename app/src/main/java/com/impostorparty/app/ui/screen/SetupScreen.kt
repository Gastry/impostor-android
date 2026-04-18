package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.PromoBannerSlot
import com.impostorparty.app.ui.components.partyFilterChipColors
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.app.util.titleRes
import com.impostorparty.app.viewmodel.UiMessage
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.GameSetup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    setup: GameSetup,
    adsEnabled: Boolean,
    bannerAdUnitId: String?,
    removeAdsPriceLabel: String?,
    onOpenRemoveAdsSettings: () -> Unit,
    isLoading: Boolean,
    message: UiMessage?,
    onDismissMessage: () -> Unit,
    onPlayerCountChanged: (Int) -> Unit,
    onImpostorCountChanged: (Int) -> Unit,
    onToggleCategory: (Category) -> Unit,
    onStartRound: () -> Unit,
    onBack: () -> Unit,
) {
    val bannerReservedHeight = 88.dp

    PartyScaffold(
        title = stringResource(R.string.setup_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = PartyDimens.SpaceMd)
                    .padding(bottom = PartyDimens.SpaceXxl + bannerReservedHeight),
                verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
            ) {
                Text(
                    text = stringResource(R.string.setup_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                PartySectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                        Text(
                            text = stringResource(R.string.setup_basics_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.setup_players_count, setup.playerCount),
                            style = MaterialTheme.typography.bodyLarge,
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
                                    colors = partyFilterChipColors(),
                                )
                            }
                        }
                    }
                }

                PartySectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.setup_categories_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.setup_categories_selected, setup.categories.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = stringResource(R.string.setup_categories_support),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                        ) {
                            Category.wordDatasetCategories.forEach { category ->
                                FilterChip(
                                    selected = category in setup.categories,
                                    onClick = { onToggleCategory(category) },
                                    label = { Text(stringResource(category.titleRes())) },
                                    colors = partyFilterChipColors(),
                                )
                            }
                        }
                    }
                }

                PartySectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                    ) {
                        Text(
                            text = stringResource(R.string.setup_players_count, setup.playerCount),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
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
                    }
                }

                Spacer(modifier = Modifier.height(PartyDimens.SpaceSm))
            }

            PromoBannerSlot(
                adsEnabled = adsEnabled,
                adUnitId = bannerAdUnitId,
                removeAdsPriceLabel = removeAdsPriceLabel,
                onRemoveAdsClick = onOpenRemoveAdsSettings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }

    SetupErrorDialog(
        message = message,
        onDismissMessage = onDismissMessage,
    )
}
