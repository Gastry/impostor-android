package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PromoBannerSlot
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.app.util.formatAsShortDateTime
import com.impostorparty.app.util.titleRes
import com.impostorparty.domain.model.RoundHistoryEntry

@Composable
fun HistoryScreen(
    history: List<RoundHistoryEntry>,
    adsEnabled: Boolean,
    bannerAdUnitId: String?,
    removeAdsPriceLabel: String?,
    onOpenRemoveAdsSettings: () -> Unit,
    onBack: () -> Unit,
) {
    val bannerReservedHeight = 88.dp

    PartyScaffold(
        title = stringResource(R.string.history_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize()) {
            if (history.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = PartyDimens.SpaceXxl + bannerReservedHeight),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PartySectionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_empty_card"),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
                        ) {
                            Text(
                                text = stringResource(R.string.history_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.history_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            SecondaryPartyButton(
                                text = stringResource(R.string.back_to_home),
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
                    contentPadding = PaddingValues(
                        top = PartyDimens.SpaceLg,
                        bottom = PartyDimens.SpaceXxl + bannerReservedHeight,
                    ),
                ) {
                    items(history, key = { it.id }) { entry ->
                        PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceXs),
                            ) {
                                Text(
                                    text = entry.word,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = stringResource(R.string.history_category_format, stringResource(entry.category.titleRes())),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = stringResource(R.string.history_impostors_format, entry.impostorNames.joinToString()),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = entry.timestampEpochMillis.formatAsShortDateTime(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
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
}
