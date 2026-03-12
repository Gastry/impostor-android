package com.impostorparty.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.theme.PartyDimens

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    PartyScaffold(
        title = stringResource(R.string.how_to_play_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                )
            }
        },
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .testTag("how_to_play_list"),
            contentPadding = PaddingValues(
                top = PartyDimens.SpaceLg,
                bottom = PartyDimens.SpaceXl,
            ),
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
        ) {
            item {
                PartySectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_logo_mark),
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                        )
                        Text(
                            text = stringResource(R.string.how_intro),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            item {
                HowSectionCard(
                    modifier = Modifier.testTag("how_objective_card"),
                    icon = Icons.Filled.Flag,
                    title = stringResource(R.string.how_objective_title),
                    lines = listOf(
                        stringResource(R.string.how_objective_line_1),
                        stringResource(R.string.how_objective_line_2),
                        stringResource(R.string.how_objective_line_3),
                    ),
                )
            }

            item {
                HowSectionCard(
                    modifier = Modifier.testTag("how_preparation_card"),
                    icon = Icons.Filled.Smartphone,
                    title = stringResource(R.string.how_preparation_title),
                    lines = listOf(
                        stringResource(R.string.how_preparation_line_1),
                        stringResource(R.string.how_preparation_line_2),
                        stringResource(R.string.how_preparation_line_3),
                    ),
                )
            }

            item {
                HowSectionCard(
                    modifier = Modifier.testTag("how_play_card"),
                    icon = Icons.Filled.ChatBubbleOutline,
                    title = stringResource(R.string.how_play_title),
                    lines = listOf(
                        stringResource(R.string.how_play_line_1),
                        stringResource(R.string.how_play_line_2),
                        stringResource(R.string.how_play_line_3),
                        stringResource(R.string.how_play_line_4),
                    ),
                )
            }

            item {
                HowSectionCard(
                    modifier = Modifier.testTag("how_win_card"),
                    icon = Icons.Filled.EmojiEvents,
                    title = stringResource(R.string.how_win_title),
                    lines = listOf(
                        stringResource(R.string.how_win_line_1),
                        stringResource(R.string.how_win_line_2),
                        stringResource(R.string.how_win_line_3),
                        stringResource(R.string.how_win_line_4),
                    ),
                )
            }

            item {
                HowSectionCard(
                    modifier = Modifier.testTag("how_tips_card"),
                    icon = Icons.Filled.Lightbulb,
                    title = stringResource(R.string.how_tips_title),
                    lines = listOf(
                        stringResource(R.string.how_tip_1),
                        stringResource(R.string.how_tip_2),
                        stringResource(R.string.how_tip_3),
                    ),
                )
            }
        }
    }
}

@Composable
private fun HowSectionCard(
    icon: ImageVector,
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    PartySectionCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(22.dp),
            )

            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                lines.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}


