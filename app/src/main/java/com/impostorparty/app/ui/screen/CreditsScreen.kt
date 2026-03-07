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
fun CreditsScreen(onBack: () -> Unit) {
    PartyScaffold(
        title = stringResource(R.string.credits_title),
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.credits_app_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.credits_app_body), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.credits_licenses_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.credits_licenses_body), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.credits_privacy_hint), style = MaterialTheme.typography.bodyMedium)
        }
    }
}