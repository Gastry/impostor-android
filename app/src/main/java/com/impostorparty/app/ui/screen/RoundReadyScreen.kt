package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.GameSetup

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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                ) {
                    Text(
                        text = stringResource(R.string.round_ready_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )

                    if (showQuickInstructions) {
                        setup?.let {
                            Text(
                                text = stringResource(R.string.round_ready_clue_rounds, it.clueRounds),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(R.string.round_ready_word_per_turn),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.round_ready_vote_after_rounds, it.clueRounds),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } ?: Text(
                            text = stringResource(R.string.round_ready_instructions),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    setup?.let {
                        Text(
                            text = stringResource(R.string.round_ready_suggested_time, it.suggestedRoundMinutes),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }

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


