package com.impostorparty.app.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyBackground
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.GameStats

@Composable
fun HomeScreen(
    stats: GameStats,
    onNewGame: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit,
    onCredits: () -> Unit,
) {
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
                bottom = PartyDimens.SpaceXxl,
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
                        Box(
                            modifier = Modifier
                                .size(PartyDimens.HeroBadge)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_logo_mark),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(PartyDimens.HeroIcon)
                                    .testTag("home_logo"),
                            )
                        }

                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
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


