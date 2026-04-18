package com.impostorparty.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.HideSource
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val FermentationBookUrl = "https://amzn.to/48F6DRr"
private const val RuleOfThreeUrl =
    "https://play.google.com/store/apps/details?id=com.aplicaciones.gastry.regladetres"
private const val AdMobFallbackTimeoutMillis = 3500L
private const val PromoBannerStatsPrefs = "promo_banner_stats"
private const val TotalShownKey = "total_shown"
private const val InternalShownKey = "internal_shown"
private const val InternalSharePermille = 50L
private const val SharePermilleBase = 1000L

internal enum class PromoBannerOption {
    ADMOB,
    REMOVE_ADS,
    FERMENTATION_BOOK,
    RULE_OF_THREE,
}

internal data class BannerExposureStats(
    val totalShown: Long = 0,
    val internalShown: Long = 0,
)

@Composable
fun PromoBannerSlot(
    adsEnabled: Boolean,
    adUnitId: String?,
    removeAdsPriceLabel: String?,
    onRemoveAdsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!adsEnabled) return

    val context = LocalContext.current
    val exposureStore = remember(context.applicationContext) {
        BannerExposureStore(context.applicationContext.getSharedPreferences(PromoBannerStatsPrefs, Context.MODE_PRIVATE))
    }
    val adMobAvailable = !adUnitId.isNullOrBlank()
    var selectedOptionName by rememberSaveable(adUnitId, removeAdsPriceLabel) {
        mutableStateOf(pickPromoBannerOption(adMobAvailable, exposureStore.snapshot()).name)
    }
    var bannerLoadState by rememberSaveable(adUnitId) {
        mutableStateOf(BannerLoadState.LOADING.name)
    }
    var recordedImpressionKey by rememberSaveable(adUnitId, removeAdsPriceLabel) {
        mutableStateOf<String?>(null)
    }

    val selectedOption = selectedOptionName.toPromoBannerOption()
    val currentBannerLoadState = bannerLoadState.toBannerLoadState()
    val impressionKey = when {
        selectedOption == PromoBannerOption.ADMOB && currentBannerLoadState == BannerLoadState.LOADED -> "admob_loaded"
        selectedOption != PromoBannerOption.ADMOB -> "internal_${selectedOption.name}"
        else -> null
    }

    LaunchedEffect(selectedOption, currentBannerLoadState, adMobAvailable) {
        if (!adMobAvailable || selectedOption != PromoBannerOption.ADMOB || currentBannerLoadState != BannerLoadState.LOADING) {
            return@LaunchedEffect
        }

        delay(AdMobFallbackTimeoutMillis)
        if (bannerLoadState.toBannerLoadState() == BannerLoadState.LOADING) {
            selectedOptionName = pickSelfPromoBannerOption().name
        }
    }

    LaunchedEffect(impressionKey) {
        val key = impressionKey ?: return@LaunchedEffect
        if (recordedImpressionKey == key) return@LaunchedEffect

        if (key == "admob_loaded") {
            exposureStore.recordAdMobShown()
        } else {
            exposureStore.recordInternalShown()
        }
        recordedImpressionKey = key
    }

    if (selectedOption == PromoBannerOption.ADMOB && adMobAvailable) {
        AdMobBanner(
            adUnitId = adUnitId,
            onLoadStateChanged = { state ->
                bannerLoadState = state.name
                if (state == BannerLoadState.FAILED) {
                    selectedOptionName = pickSelfPromoBannerOption().name
                }
            },
            modifier = modifier,
        )
        return
    }

    PromoBannerCard(
        option = if (selectedOption == PromoBannerOption.ADMOB) pickSelfPromoBannerOption() else selectedOption,
        removeAdsPriceLabel = removeAdsPriceLabel,
        onRemoveAdsClick = onRemoveAdsClick,
        modifier = modifier,
    )
}

@Composable
private fun PromoBannerCard(
    option: PromoBannerOption,
    removeAdsPriceLabel: String?,
    onRemoveAdsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val title = when (option) {
        PromoBannerOption.REMOVE_ADS -> stringResource(R.string.home_banner_fallback_title)
        PromoBannerOption.FERMENTATION_BOOK -> stringResource(R.string.promo_book_title)
        PromoBannerOption.RULE_OF_THREE -> stringResource(R.string.promo_rule_of_three_title)
        PromoBannerOption.ADMOB -> error("AdMob should not be rendered as a self-promo card")
    }
    val subtitle = when (option) {
        PromoBannerOption.REMOVE_ADS -> removeAdsPromoSubtitle(removeAdsPriceLabel)
        PromoBannerOption.FERMENTATION_BOOK -> stringResource(R.string.promo_book_subtitle)
        PromoBannerOption.RULE_OF_THREE -> stringResource(R.string.promo_rule_of_three_subtitle)
        PromoBannerOption.ADMOB -> error("AdMob should not be rendered as a self-promo card")
    }
    val icon = when (option) {
        PromoBannerOption.REMOVE_ADS -> Icons.Rounded.HideSource
        PromoBannerOption.FERMENTATION_BOOK -> Icons.Rounded.MenuBook
        PromoBannerOption.RULE_OF_THREE -> Icons.Rounded.Calculate
        PromoBannerOption.ADMOB -> Icons.Rounded.HideSource
    }
    val onClick = when (option) {
        PromoBannerOption.REMOVE_ADS -> onRemoveAdsClick
        PromoBannerOption.FERMENTATION_BOOK -> { { context.openExternalUrl(FermentationBookUrl) } }
        PromoBannerOption.RULE_OF_THREE -> { { context.openExternalUrl(RuleOfThreeUrl) } }
        PromoBannerOption.ADMOB -> onRemoveAdsClick
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .height(64.dp)
            .testTag("promo_banner_${option.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.74f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = stringResource(R.string.home_banner_fallback_cta),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
    }
}

internal fun pickPromoBannerOption(
    adMobAvailable: Boolean,
    exposureStats: BannerExposureStats,
): PromoBannerOption {
    if (!adMobAvailable) return pickSelfPromoBannerOption()
    return if (shouldShowInternalPromo(exposureStats)) pickSelfPromoBannerOption() else PromoBannerOption.ADMOB
}

private fun pickSelfPromoBannerOption(): PromoBannerOption {
    return when (Random.nextInt(200)) {
        in 0..49 -> PromoBannerOption.REMOVE_ADS
        in 50..124 -> PromoBannerOption.FERMENTATION_BOOK
        else -> PromoBannerOption.RULE_OF_THREE
    }
}

internal fun shouldShowInternalPromo(exposureStats: BannerExposureStats): Boolean {
    val targetInternalShownAfterNext = ((exposureStats.totalShown + 1) * InternalSharePermille) / SharePermilleBase
    return exposureStats.internalShown < targetInternalShownAfterNext
}

private fun String.toPromoBannerOption(): PromoBannerOption {
    return runCatching { PromoBannerOption.valueOf(this) }
        .getOrDefault(PromoBannerOption.REMOVE_ADS)
}

private fun String.toBannerLoadState(): BannerLoadState {
    return runCatching { BannerLoadState.valueOf(this) }
        .getOrDefault(BannerLoadState.LOADING)
}

@Composable
private fun removeAdsPromoSubtitle(priceLabel: String?): String {
    return if (priceLabel.isNullOrBlank()) {
        stringResource(R.string.home_banner_fallback_subtitle)
    } else {
        stringResource(R.string.home_banner_fallback_subtitle_price, priceLabel)
    }
}

private fun Context.openExternalUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}

private class BannerExposureStore(
    private val sharedPreferences: SharedPreferences,
) {
    fun snapshot(): BannerExposureStats {
        return BannerExposureStats(
            totalShown = sharedPreferences.getLong(TotalShownKey, 0L),
            internalShown = sharedPreferences.getLong(InternalShownKey, 0L),
        )
    }

    fun recordAdMobShown() {
        val stats = snapshot()
        sharedPreferences.edit()
            .putLong(TotalShownKey, stats.totalShown + 1)
            .apply()
    }

    fun recordInternalShown() {
        val stats = snapshot()
        sharedPreferences.edit()
            .putLong(TotalShownKey, stats.totalShown + 1)
            .putLong(InternalShownKey, stats.internalShown + 1)
            .apply()
    }
}
