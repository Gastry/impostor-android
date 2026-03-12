package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.app.util.formatAsShortDateTime
import com.impostorparty.app.util.titleRes
import com.impostorparty.domain.model.RoundHistoryEntry

@Composable
fun HistoryScreen(
    history: List<RoundHistoryEntry>,
    onBack: () -> Unit,
) {
    PartyScaffold(
        title = stringResource(R.string.history_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        if (history.isEmpty()) {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@PartyScaffold
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = PartyDimens.SpaceLg),
        ) {
            items(history, key = { it.id }) { entry ->
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceXs),
                    ) {
                        Text(
                            text = entry.word,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.history_category_format, stringResource(entry.category.titleRes())),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(R.string.history_impostors_format, entry.impostorNames.joinToString()),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = entry.timestampEpochMillis.formatAsShortDateTime(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}