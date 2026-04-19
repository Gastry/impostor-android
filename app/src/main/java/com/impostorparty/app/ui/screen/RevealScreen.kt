package com.impostorparty.app.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.HoldToRevealButton
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.PromoBannerSlot
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.PlayerSecret
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.usecase.RevealFlowState

@Composable
fun RevealScreen(
    roundSession: RoundSession?,
    adsEnabled: Boolean,
    bannerAdUnitId: String?,
    removeAdsPriceLabel: String?,
    onOpenRemoveAdsSettings: () -> Unit,
    flowState: RevealFlowState,
    reducedMotion: Boolean,
    hapticsEnabled: Boolean,
    onRequestReveal: () -> Unit,
    onHideAndPass: () -> Unit,
    onExit: () -> Unit,
) {
    val bannerReservedHeight = 88.dp

    if (roundSession == null) {
        PartyScaffold(title = stringResource(R.string.reveal_title)) { modifier ->
            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = PartyDimens.SpaceLg,
                            bottom = PartyDimens.SpaceXxl + bannerReservedHeight,
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.error_generic), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(PartyDimens.SpaceMd))
                    PrimaryPartyButton(
                        text = stringResource(R.string.back_to_home),
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth(),
                    )
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
        return
    }

    val haptic = LocalHapticFeedback.current
    val contentState = when (flowState) {
        is RevealFlowState.PassingPhone -> RevealContentState.Player(flowState.playerIndex)
        is RevealFlowState.RevealingSecret -> RevealContentState.Player(flowState.playerIndex)
        RevealFlowState.RoundReady -> RevealContentState.Ready
    }

    PartyScaffold(
        title = stringResource(R.string.reveal_title),
        modifier = Modifier.testTag("reveal_screen"),
        navigationIcon = {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
            }
        },
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = contentState,
                transitionSpec = {
                    if (reducedMotion) {
                        fadeIn(animationSpec = androidx.compose.animation.core.tween(0)) togetherWith
                            fadeOut(animationSpec = androidx.compose.animation.core.tween(0))
                    } else {
                        fadeIn() togetherWith fadeOut()
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = PartyDimens.SpaceXxl + bannerReservedHeight),
                label = "reveal_content",
            ) { targetContentState ->
                when (targetContentState) {
                    is RevealContentState.Player -> {
                        val currentPlayerIndex = targetContentState.playerIndex
                        val assignment = roundSession.assignments[currentPlayerIndex]
                        val nextPlayerName = roundSession.assignments
                            .getOrNull(currentPlayerIndex + 1)
                            ?.player
                            ?.name
                        RevealPeekContent(
                            playerName = assignment.player.name,
                            nextPlayerName = nextPlayerName,
                            secret = assignment.secret,
                            isReadyToPass = flowState is RevealFlowState.RevealingSecret &&
                                flowState.playerIndex == currentPlayerIndex,
                            onRevealUnlocked = {
                                if (flowState is RevealFlowState.PassingPhone && hapticsEnabled) {
                                    haptic.performHapticFeedback(
                                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress,
                                    )
                                }
                                if (flowState is RevealFlowState.PassingPhone) {
                                    onRequestReveal()
                                }
                            },
                            onContinue = {
                                if (hapticsEnabled) {
                                    haptic.performHapticFeedback(
                                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove,
                                    )
                                }
                                onHideAndPass()
                            },
                            revealKey = currentPlayerIndex,
                        )
                    }

                    RevealContentState.Ready -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(stringResource(R.string.round_ready_title))
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

private sealed interface RevealContentState {
    data class Player(val playerIndex: Int) : RevealContentState
    data object Ready : RevealContentState
}

@Composable
private fun RevealPeekContent(
    playerName: String,
    nextPlayerName: String?,
    secret: PlayerSecret,
    isReadyToPass: Boolean,
    onRevealUnlocked: () -> Unit,
    onContinue: () -> Unit,
    revealKey: Int,
) {
    var isRevealVisible by rememberSaveable(revealKey) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = PartyDimens.SpaceLg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PartySectionCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
        ) {
            AnimatedContent(
                targetState = isRevealVisible,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "reveal_card_content",
            ) { visible ->
                if (!visible) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                    ) {
                        Text(
                            text = stringResource(R.string.reveal_pass_to_player, playerName),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.reveal_keep_private),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = playerName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        when (secret) {
                            is PlayerSecret.Civilian -> {
                                Text(
                                    text = stringResource(R.string.reveal_word_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = secret.word,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                )
                            }

                            PlayerSecret.Impostor -> {
                                Text(
                                    text = stringResource(R.string.reveal_impostor_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = stringResource(R.string.reveal_impostor_hint),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(PartyDimens.SpaceMd))

        HoldToRevealButton(
            label = stringResource(R.string.reveal_hold_to_see),
            helper = stringResource(R.string.reveal_hold_hint),
            onComplete = onRevealUnlocked,
            onRevealVisibilityChanged = { isRevealVisible = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(PartyDimens.SpaceMd))

        PrimaryPartyButton(
            text = if (nextPlayerName != null) {
                stringResource(R.string.reveal_hide_and_pass_named, nextPlayerName)
            } else {
                stringResource(R.string.reveal_hide_and_finish)
            },
            onClick = onContinue,
            enabled = isReadyToPass,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reveal_hide_and_pass_button"),
        )
    }
}

