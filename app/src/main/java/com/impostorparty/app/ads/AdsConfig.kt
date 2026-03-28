package com.impostorparty.app.ads

import com.impostorparty.app.BuildConfig

enum class AdPlacement {
    HOME_BANNER,
}

object AdsConfig {
    fun adUnitIdFor(
        placement: AdPlacement,
        adsRemoved: Boolean = false,
    ): String? {
        if (adsRemoved || !BuildConfig.ADS_ENABLED) return null

        return when (placement) {
            AdPlacement.HOME_BANNER -> BuildConfig.HOME_BANNER_AD_UNIT_ID.takeIf { it.isNotBlank() }
        }
    }
}
