package com.impostorparty.app.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.HoldToRevealButton
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.PlayerSecret
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.usecase.RevealFlowState
import kotlinx.coroutines.delay

@Composable
fun RevealScreen(
    roundSession: RoundSession?,
    flowState: RevealFlowState,
    reducedMotion: Boolean,
    hapticsEnabled: Boolean,
    onRequestReveal: () -> Unit,
    onHideAndPass: () -> Unit,
    onExit: () -> Unit,
) {
    if (roundSession == null) {
        PartyScaffold(title = stringResource(R.string.reveal_title)) { modifier ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(vertical = PartyDimens.SpaceLg),
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
        }
        return
    }

    val haptic = LocalHapticFeedback.current
    val stateKey = when (flowState) {
        is RevealFlowState.PassingPhone -> "pass_${flowState.playerIndex}"
        is RevealFlowState.RevealingSecret -> "secret_${flowState.playerIndex}"
        RevealFlowState.RoundReady -> "ready"
    }

    if (flowState is RevealFlowState.RevealingSecret && roundSession.setup.quickMode) {
        LaunchedEffect(flowState.playerIndex) {
            delay(2200)
            onHideAndPass()
        }
    }

    PartyScaffold(
        title = stringResource(R.string.reveal_title),
        navigationIcon = {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
            }
        },
    ) { modifier ->
        AnimatedContent(
            targetState = stateKey,
            transitionSpec = {
                if (reducedMotion) {
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(0)) togetherWith
                        fadeOut(animationSpec = androidx.compose.animation.core.tween(0))
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            modifier = modifier.fillMaxSize(),
            label = "reveal_content",
        ) {
            when (flowState) {
                is RevealFlowState.PassingPhone -> {
                    val assignment = roundSession.assignments[flowState.playerIndex]
                    RevealPassContent(
                        playerName = assignment.player.name,
                        onRequestReveal = {
                            if (hapticsEnabled) {
                                haptic.performHapticFeedback(
                                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress,
                                )
                            }
                            onRequestReveal()
                        },
                    )
                }

                is RevealFlowState.RevealingSecret -> {
                    val assignment = roundSession.assignments[flowState.playerIndex]
                    RevealSecretContent(
                        playerName = assignment.player.name,
                        secret = assignment.secret,
                        onHideAndPass = {
                            if (hapticsEnabled) {
                                haptic.performHapticFeedback(
                                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove,
                                )
                            }
                            onHideAndPass()
                        },
                    )
                }

                RevealFlowState.RoundReady -> {
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
    }
}

@Composable
private fun RevealPassContent(
    playerName: String,
    onRequestReveal: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = PartyDimens.SpaceLg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PartySectionCard(modifier = Modifier.fillMaxWidth()) {
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
        }

        Spacer(Modifier.height(PartyDimens.SpaceLg))

        HoldToRevealButton(
            label = stringResource(R.string.reveal_hold_to_see),
            helper = stringResource(R.string.reveal_hold_hint),
            onComplete = onRequestReveal,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RevealSecretContent(
    playerName: String,
    secret: PlayerSecret,
    onHideAndPass: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = PartyDimens.SpaceLg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PartySectionCard(modifier = Modifier.fillMaxWidth()) {
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

        Spacer(Modifier.height(PartyDimens.SpaceLg))

        PrimaryPartyButton(
            text = stringResource(R.string.reveal_hide_and_pass),
            onClick = onHideAndPass,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reveal_hide_and_pass_button"),
        )
    }
}

