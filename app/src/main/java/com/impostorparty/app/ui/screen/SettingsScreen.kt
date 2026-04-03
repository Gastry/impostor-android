package com.impostorparty.app.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.impostorparty.app.R
import com.impostorparty.app.ads.RemoveAdsPurchaseMessage
import com.impostorparty.app.ads.RemoveAdsPurchaseUiState
import com.impostorparty.app.ui.components.AdMobBanner
import com.impostorparty.app.ui.components.BannerLoadState
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.domain.model.AppSettings
import com.impostorparty.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    bannerAdUnitId: String?,
    removeAdsUiState: RemoveAdsPurchaseUiState,
    highlightRemoveAds: Boolean,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (String?) -> Unit,
    onReducedMotionChanged: (Boolean) -> Unit,
    onShowQuickInstructionsChanged: (Boolean) -> Unit,
    onSecureScreenChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onAvoidRecentChanged: (Boolean) -> Unit,
    onRevealAnimationChanged: (Boolean) -> Unit,
    onRemoveAds: () -> Unit,
    onRateApp: () -> Unit,
    onSendSuggestion: () -> Unit,
    onResetPreferences: () -> Unit,
    onClearHistory: () -> Unit,
    onDismissBillingMessage: () -> Unit,
    onBack: () -> Unit,
) {
    val bannerReservedHeight = if (bannerAdUnitId != null) 88.dp else 0.dp
    val listState = rememberLazyListState()
    var languageMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var bannerState by rememberSaveable(bannerAdUnitId) {
        mutableStateOf(
            if (bannerAdUnitId == null) BannerLoadState.FAILED else BannerLoadState.LOADING,
        )
    }
    val removeAdsButtonText = when {
        removeAdsUiState.isAdsRemoved -> stringResource(R.string.settings_remove_ads_owned)
        removeAdsUiState.hasPendingPurchase -> stringResource(R.string.settings_remove_ads_pending)
        removeAdsUiState.isLoading || removeAdsUiState.isPurchaseInProgress -> {
            stringResource(R.string.settings_remove_ads_loading)
        }
        removeAdsUiState.priceLabel != null -> {
            stringResource(R.string.settings_remove_ads_price, removeAdsUiState.priceLabel)
        }
        else -> stringResource(R.string.settings_remove_ads_buy)
    }
    val removeAdsEnabled = !removeAdsUiState.isAdsRemoved &&
        !removeAdsUiState.hasPendingPurchase &&
        !removeAdsUiState.isLoading &&
        !removeAdsUiState.isPurchaseInProgress &&
        removeAdsUiState.isPurchaseAvailable
    var highlightCard by rememberSaveable(highlightRemoveAds) { mutableStateOf(highlightRemoveAds) }
    val highlightProgress by animateFloatAsState(
        targetValue = if (highlightCard) 1.02f else 1f,
        animationSpec = tween(durationMillis = 320),
        label = "remove_ads_scale",
    )
    val highlightContainerColor by animateColorAsState(
        targetValue = if (highlightCard) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = tween(durationMillis = 420),
        label = "remove_ads_container",
    )
    val highlightBorderColor by animateColorAsState(
        targetValue = if (highlightCard) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(durationMillis = 420),
        label = "remove_ads_border",
    )

    LaunchedEffect(highlightRemoveAds) {
        if (!highlightRemoveAds) return@LaunchedEffect
        listState.animateScrollToItem(2)
        highlightCard = true
        delay(950)
        highlightCard = false
    }

    PartyScaffold(
        title = stringResource(R.string.settings_title),
        navigationIcon = {
            IconButton(onClick = {
                onDismissBillingMessage()
                onBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("settings_list"),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
                contentPadding = PaddingValues(
                    top = PartyDimens.SpaceMd,
                    bottom = PartyDimens.SpaceXxl + bannerReservedHeight,
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
                            ExposedDropdownMenuBox(
                                expanded = languageMenuExpanded,
                                onExpandedChange = { languageMenuExpanded = !languageMenuExpanded },
                            ) {
                                OutlinedTextField(
                                    value = stringResource(selectedLanguageOption(settings.languageTag).labelRes),
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("settings_language_selector"),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded)
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                )
                                DropdownMenu(
                                    expanded = languageMenuExpanded,
                                    onDismissRequest = { languageMenuExpanded = false },
                                ) {
                                    LanguageOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(stringResource(option.labelRes)) },
                                            onClick = {
                                                languageMenuExpanded = false
                                                onLanguageChanged(option.tag)
                                            },
                                            modifier = Modifier.testTag(languageTag(option)),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = highlightProgress
                                scaleY = highlightProgress
                            }
                            .testTag("settings_remove_ads_card"),
                        shape = RoundedCornerShape(PartyDimens.RadiusMd),
                        colors = CardDefaults.cardColors(
                            containerColor = highlightContainerColor,
                        ),
                        border = BorderStroke(1.dp, highlightBorderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                        ) {
                            SecondaryPartyButton(
                                text = removeAdsButtonText,
                                onClick = onRemoveAds,
                                enabled = removeAdsEnabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_remove_ads"),
                            )
                            removeAdsStatusText(removeAdsUiState.message)?.let { message ->
                                Text(
                                    text = stringResource(message),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("settings_remove_ads_status"),
                                )
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

            if (bannerAdUnitId != null) {
                AdMobBanner(
                    adUnitId = bannerAdUnitId,
                    onLoadStateChanged = { bannerState = it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

private fun removeAdsStatusText(message: RemoveAdsPurchaseMessage?): Int? {
    return when (message) {
        RemoveAdsPurchaseMessage.PURCHASED -> R.string.settings_remove_ads_message_purchased
        RemoveAdsPurchaseMessage.PENDING -> R.string.settings_remove_ads_message_pending
        RemoveAdsPurchaseMessage.ERROR -> R.string.settings_remove_ads_message_error
        RemoveAdsPurchaseMessage.UNAVAILABLE -> R.string.settings_remove_ads_message_unavailable
        null -> null
    }
}

private fun selectedLanguageOption(languageTag: String?): LanguageOption {
    return LanguageOption.entries.firstOrNull { it.tag == languageTag } ?: LanguageOption.SYSTEM
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

private fun languageTag(option: LanguageOption): String {
    return "settings_language_" + (option.tag ?: "system")
}

