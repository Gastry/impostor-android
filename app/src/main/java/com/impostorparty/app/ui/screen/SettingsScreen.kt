package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.ThemeMode

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (String?) -> Unit,
    onReducedMotionChanged: (Boolean) -> Unit,
    onShowQuickInstructionsChanged: (Boolean) -> Unit,
    onSecureScreenChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onAvoidRecentChanged: (Boolean) -> Unit,
    onRevealAnimationChanged: (Boolean) -> Unit,
    onRateApp: () -> Unit,
    onSendSuggestion: () -> Unit,
    onResetPreferences: () -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
) {
    PartyScaffold(
        title = stringResource(R.string.settings_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("settings_list"),
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
            contentPadding = PaddingValues(
                top = PartyDimens.SpaceMd,
                bottom = PartyDimens.SpaceXxl,
            ),
        ) {
            item {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                        Text(
                            text = stringResource(R.string.settings_theme_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ThemeMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = mode == settings.themeMode,
                                    onClick = { onThemeModeChanged(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = ThemeMode.entries.size,
                                    ),
                                    label = {
                                        Text(
                                            text = when (mode) {
                                                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                            },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                        Text(
                            text = stringResource(R.string.settings_language_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                        ) {
                            LanguageOption.entries.forEach { option ->
                                FilterChip(
                                    modifier = Modifier.testTag(languageTag(option)),
                                    selected = option.tag == settings.languageTag,
                                    onClick = { onLanguageChanged(option.tag) },
                                    label = { Text(stringResource(option.labelRes)) },
                                )
                            }
                        }
                    }
                }
            }

            item {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceXs)) {
                        val rows = listOf(
                            ToggleItem(stringResource(R.string.settings_haptics), settings.hapticsEnabled, onHapticsChanged),
                            ToggleItem(stringResource(R.string.settings_reduced_motion), settings.reducedMotion, onReducedMotionChanged),
                            ToggleItem(stringResource(R.string.settings_show_instructions), settings.showQuickInstructions, onShowQuickInstructionsChanged),
                            ToggleItem(stringResource(R.string.settings_avoid_recent), settings.avoidRecentWords, onAvoidRecentChanged),
                            ToggleItem(stringResource(R.string.settings_show_animation), settings.showRevealAnimation, onRevealAnimationChanged),
                            ToggleItem(stringResource(R.string.settings_secure_screen), settings.secureScreen, onSecureScreenChanged),
                        )

                        rows.forEachIndexed { index, item ->
                            ToggleRow(
                                title = item.title,
                                checked = item.checked,
                                onCheckedChanged = item.onCheckedChanged,
                            )
                            if (index < rows.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }

            item {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                        SecondaryPartyButton(
                            text = stringResource(R.string.settings_rate_app),
                            onClick = onRateApp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_rate_app"),
                        )
                        SecondaryPartyButton(
                            text = stringResource(R.string.settings_send_feedback),
                            onClick = onSendSuggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_send_feedback"),
                        )
                        SecondaryPartyButton(
                            text = stringResource(R.string.settings_clear_history),
                            onClick = onClearHistory,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        PrimaryPartyButton(
                            text = stringResource(R.string.settings_reset_preferences),
                            onClick = onResetPreferences,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_reset_cta"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PartyDimens.SpaceXs),
        horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChanged)
    }
}

private data class ToggleItem(
    val title: String,
    val checked: Boolean,
    val onCheckedChanged: (Boolean) -> Unit,
)

enum class LanguageOption(val tag: String?, val labelRes: Int) {
    SYSTEM(null, R.string.language_system),
    SPANISH("es", R.string.language_spanish),
    ENGLISH("en", R.string.language_english),
    FRENCH("fr", R.string.language_french),
    GERMAN("de", R.string.language_german),
    ITALIAN("it", R.string.language_italian),
    PORTUGUESE("pt", R.string.language_portuguese),
    JAPANESE("ja", R.string.language_japanese),
}

private fun languageTag(option: LanguageOption): String {
    return "settings_language_" + (option.tag ?: "system")
}

