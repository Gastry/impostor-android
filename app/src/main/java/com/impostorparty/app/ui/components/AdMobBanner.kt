package com.impostorparty.app.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.math.roundToInt

enum class BannerLoadState {
    LOADING,
    LOADED,
    FAILED,
}

@Composable
fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier = Modifier,
    onLoadStateChanged: (BannerLoadState) -> Unit = {},
) {
    if (LocalInspectionMode.current) return

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val currentOnLoadStateChanged = rememberUpdatedState(onLoadStateChanged)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val constrainedWidthDp = maxWidth.value.roundToInt()
        val screenFallbackWidthDp = (configuration.screenWidthDp - 32).coerceAtLeast(1)
        val adWidthDp = constrainedWidthDp.takeIf { it > 0 } ?: screenFallbackWidthDp

        val adView = remember(context, adUnitId, adWidthDp) {
            AdView(context).apply {
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        currentOnLoadStateChanged.value(BannerLoadState.LOADED)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        currentOnLoadStateChanged.value(BannerLoadState.FAILED)
                    }
                }
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp))
                loadAd(AdRequest.Builder().build())
            }
        }

        DisposableEffect(adUnitId) {
            currentOnLoadStateChanged.value(BannerLoadState.LOADING)
            onDispose { }
        }

        DisposableEffect(adView) {
            onDispose {
                adView.destroy()
            }
        }

        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth(),
            update = {},
        )
    }
}
