package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
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
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.round_ready_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (showQuickInstructions) {
                Text(
                    text = stringResource(R.string.round_ready_instructions),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            setup?.let {
                Text(
                    text = stringResource(R.string.round_ready_suggested_time, it.suggestedRoundMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onFinishRound,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text(stringResource(R.string.round_ready_finish_button))
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onNewConfiguration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text(stringResource(R.string.round_ready_new_config_button))
            }
        }
    }
}