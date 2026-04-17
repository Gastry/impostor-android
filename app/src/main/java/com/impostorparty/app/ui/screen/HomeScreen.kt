package com.impostorparty.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyBackground
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.PromoBannerSlot
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.GameStats

@Composable
fun HomeScreen(
    stats: GameStats,
    homeBannerAdUnitId: String?,
    removeAdsPriceLabel: String?,
    onNewGame: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onOpenRemoveAdsSettings: () -> Unit,
    onHistory: () -> Unit,
    onCredits: () -> Unit,
) {
    val bannerReservedHeight = 88.dp

    Box(modifier = Modifier.fillMaxSize()) {
        PartyBackground()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .testTag("home_list"),
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
            contentPadding = PaddingValues(
                start = PartyDimens.ScreenHorizontal,
                end = PartyDimens.ScreenHorizontal,
                top = PartyDimens.SpaceXxl,
                bottom = PartyDimens.SpaceXxl + bannerReservedHeight,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                ContentWidth {
                    HomeHeroCard(stats = stats)
                }
            }

            item {
                ContentWidth {
                    PrimaryPartyButton(
                        text = stringResource(R.string.new_game),
                        onClick = onNewGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_primary_cta"),
                    )
                }
            }

            item {
                ContentWidth {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                    ) {
                        SecondaryPartyButton(
                            text = stringResource(R.string.how_to_play_title),
                            onClick = onHowToPlay,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_how_to_play"),
                        )
                        SecondaryPartyButton(
                            text = stringResource(R.string.settings_title),
                            onClick = onSettings,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_settings"),
                        )
                    }
                }
            }

            item {
                ContentWidth {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(
                            onClick = onHistory,
                            modifier = Modifier.testTag("home_history"),
                        ) {
                            Text(
                                text = stringResource(R.string.history_title),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        TextButton(
                            onClick = onCredits,
                            modifier = Modifier.testTag("home_credits"),
                        ) {
                            Text(
                                text = stringResource(R.string.credits_title),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        PromoBannerSlot(
            adUnitId = homeBannerAdUnitId,
            removeAdsPriceLabel = removeAdsPriceLabel,
            onRemoveAdsClick = onOpenRemoveAdsSettings,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        )
    }
}

@Composable
private fun ContentWidth(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = PartyDimens.ContentMaxWidth),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun HomeHeroCard(
    stats: GameStats,
    modifier: Modifier = Modifier,
) {
    PartySectionCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_hero_card"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("home_title"),
            )
            Text(
                text = stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
            ) {
                HomeStatPill(text = stringResource(R.string.home_stats_games_played, stats.gamesPlayed))
                if (stats.lastPlayerCount > 0) {
                    HomeStatPill(text = stringResource(R.string.home_stats_last_players, stats.lastPlayerCount))
                }
            }
        }
    }
}

@Composable
private fun HomeStatPill(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(PartyDimens.RadiusSm),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


