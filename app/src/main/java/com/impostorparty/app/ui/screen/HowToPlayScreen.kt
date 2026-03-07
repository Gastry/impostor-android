package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    PartyScaffold(
        title = stringResource(R.string.how_to_play_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = stringResource(R.string.how_step_1),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.how_step_2),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.how_step_3),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = stringResource(R.string.how_faq_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(text = stringResource(R.string.how_faq_q1), style = MaterialTheme.typography.titleSmall)
            Text(text = stringResource(R.string.how_faq_a1), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.how_faq_q2), style = MaterialTheme.typography.titleSmall)
            Text(text = stringResource(R.string.how_faq_a2), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.how_faq_q3), style = MaterialTheme.typography.titleSmall)
            Text(text = stringResource(R.string.how_faq_a3), style = MaterialTheme.typography.bodyMedium)
        }
    }
}