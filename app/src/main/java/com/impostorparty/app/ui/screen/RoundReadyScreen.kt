package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.GameSetup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoundReadyScreen(
    setup: GameSetup?,
    showQuickInstructions: Boolean,
    onFinishRound: () -> Unit,
    onNewConfiguration: () -> Unit,
) {
    PartyScaffold(title = stringResource(R.string.round_ready_title)) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = PartyDimens.SpaceLg, bottom = PartyDimens.SpaceSm),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceLg),
                    ) {
                        Text(
                            text = stringResource(R.string.round_ready_title),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                        )

                        if (showQuickInstructions) {
                            Text(
                                text = stringResource(R.string.round_ready_instructions),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        setup?.let {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                                verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                            ) {
                                RoundReadyInfoPill(
                                    text = stringResource(R.string.round_ready_clue_rounds, it.clueRounds),
                                )
                                RoundReadyInfoPill(
                                    text = stringResource(R.string.round_ready_suggested_time, it.suggestedRoundMinutes),
                                )
                            }
                            Text(
                                text = stringResource(R.string.round_ready_word_per_turn),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(R.string.round_ready_vote_after_rounds, it.clueRounds),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
            ) {
                PrimaryPartyButton(
                    text = stringResource(R.string.round_ready_finish_button),
                    onClick = onFinishRound,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("round_ready_finish_button"),
                )

                SecondaryPartyButton(
                    text = stringResource(R.string.round_ready_new_config_button),
                    onClick = onNewConfiguration,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("round_ready_new_config_button"),
                )
            }
        }
    }
}

@Composable
private fun RoundReadyInfoPill(text: String) {
    Surface(
        shape = RoundedCornerShape(PartyDimens.RadiusSm),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            textAlign = TextAlign.Center,
        )
    }
}


