package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
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
import com.impostorparty.app.util.labelRes
import com.impostorparty.domain.model.PlayerSecret
import com.impostorparty.domain.model.RoundSession
import com.impostorparty.domain.model.WinnerSide

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    roundSession: RoundSession?,
    winnerSelection: WinnerSide,
    onWinnerSelected: (WinnerSide) -> Unit,
    onPlayAgain: () -> Unit,
    onNewConfiguration: () -> Unit,
) {
    PartyScaffold(title = stringResource(R.string.result_title)) { modifier ->
        if (roundSession == null) {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.error_generic), textAlign = TextAlign.Center)
            }
            return@PartyScaffold
        }

        val impostors = roundSession.assignments
            .filter { it.secret is PlayerSecret.Impostor }
            .joinToString { it.player.name }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = PartyDimens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
        ) {
            PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                    Text(
                        text = stringResource(R.string.result_word_label),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = roundSession.word.text,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = stringResource(R.string.result_impostor_label, impostors),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.result_winner_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(PartyDimens.SpaceSm))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                ) {
                    WinnerSide.entries.forEach { side ->
                        FilterChip(
                            selected = side == winnerSelection,
                            onClick = { onWinnerSelected(side) },
                            label = { Text(stringResource(side.labelRes())) },
                        )
                    }
                }
            }

            PrimaryPartyButton(
                text = stringResource(R.string.result_rematch_button),
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_rematch_button"),
            )
            SecondaryPartyButton(
                text = stringResource(R.string.result_new_setup_button),
                onClick = onNewConfiguration,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_new_setup_button"),
            )
        }
    }
}

