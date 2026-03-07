package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
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
    onResetPreferences: () -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
) {
    PartyScaffold(
        title = stringResource(R.string.settings_title),
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
            Text(stringResource(R.string.settings_theme_title), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = mode == settings.themeMode,
                        onClick = { onThemeModeChanged(mode) },
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

            Text(stringResource(R.string.settings_language_title), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageOption.entries.forEach { option ->
                    FilterChip(
                        selected = option.tag == settings.languageTag,
                        onClick = { onLanguageChanged(option.tag) },
                        label = { Text(stringResource(option.labelRes)) },
                    )
                }
            }

            ToggleRow(
                title = stringResource(R.string.settings_haptics),
                checked = settings.hapticsEnabled,
                onCheckedChanged = onHapticsChanged,
            )
            ToggleRow(
                title = stringResource(R.string.settings_reduced_motion),
                checked = settings.reducedMotion,
                onCheckedChanged = onReducedMotionChanged,
            )
            ToggleRow(
                title = stringResource(R.string.settings_show_instructions),
                checked = settings.showQuickInstructions,
                onCheckedChanged = onShowQuickInstructionsChanged,
            )
            ToggleRow(
                title = stringResource(R.string.settings_avoid_recent),
                checked = settings.avoidRecentWords,
                onCheckedChanged = onAvoidRecentChanged,
            )
            ToggleRow(
                title = stringResource(R.string.settings_show_animation),
                checked = settings.showRevealAnimation,
                onCheckedChanged = onRevealAnimationChanged,
            )
            ToggleRow(
                title = stringResource(R.string.settings_secure_screen),
                checked = settings.secureScreen,
                onCheckedChanged = onSecureScreenChanged,
            )

            OutlinedButton(onClick = onClearHistory, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_clear_history))
            }
            Button(onClick = onResetPreferences, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_reset_preferences))
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChanged)
    }
}

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