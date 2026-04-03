package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HideSource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.AdMobBanner
import com.impostorparty.app.ui.components.BannerLoadState
import com.impostorparty.app.ui.components.PartyBackground
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.GameStats

@Composable
fun HomeScreen(
    stats: GameStats,
    homeBannerAdUnitId: String?,
    onNewGame: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onOpenRemoveAdsSettings: () -> Unit,
    onHistory: () -> Unit,
    onCredits: () -> Unit,
) {
    val bannerReservedHeight = if (homeBannerAdUnitId != null) 88.dp else 0.dp
    var bannerState by rememberSaveable(homeBannerAdUnitId) {
        mutableStateOf(
            if (homeBannerAdUnitId == null) BannerLoadState.FAILED else BannerLoadState.LOADING,
        )
    }

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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("home_title"),
                        )
                        Text(
                            text = stringResource(R.string.home_tagline),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
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
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                        SecondaryPartyButton(
                            text = stringResource(R.string.how_to_play_title),
                            onClick = onHowToPlay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_how_to_play"),
                        )
                        SecondaryPartyButton(
                            text = stringResource(R.string.settings_title),
                            onClick = onSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_settings"),
                        )
                    }
                }
            }

            item {
                ContentWidth {
                    PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceXs)) {
                            Text(
                                text = stringResource(R.string.home_stats_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.home_stats_games_played, stats.gamesPlayed),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (stats.lastPlayerCount > 0) {
                                Text(
                                    text = stringResource(R.string.home_stats_last_players, stats.lastPlayerCount),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            item {
                ContentWidth {
                    SupportTheAppCard(
                        onClick = onOpenRemoveAdsSettings,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                ContentWidth {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onHistory) {
                            Text(stringResource(R.string.history_title))
                        }
                        TextButton(onClick = onCredits) {
                            Text(stringResource(R.string.credits_title))
                        }
                    }
                }
            }
        }

        if (homeBannerAdUnitId != null) {
            when (bannerState) {
                BannerLoadState.LOADING,
                BannerLoadState.LOADED,
                -> AdMobBanner(
                    adUnitId = homeBannerAdUnitId,
                    onLoadStateChanged = { bannerState = it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = PartyDimens.ScreenHorizontal),
                )

                BannerLoadState.FAILED -> RemoveAdsFallbackCard(
                    onClick = onOpenRemoveAdsSettings,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = PartyDimens.ScreenHorizontal),
                )
            }
        }
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
private fun SupportTheAppCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag("home_support_card"),
        shape = RoundedCornerShape(PartyDimens.RadiusMd),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_support_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.home_banner_fallback_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.home_banner_fallback_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.home_banner_fallback_cta),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun RemoveAdsFallbackCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .height(64.dp)
            .testTag("home_banner_fallback"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            ),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f),
                    ),
                ) {}
                Icon(
                    imageVector = Icons.Rounded.HideSource,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_banner_fallback_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.home_banner_fallback_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(R.string.home_banner_fallback_cta),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
    }
}


