package com.impostorparty.app.ads

import com.impostorparty.app.BuildConfig
import com.impostorparty.app.RuntimeFlags

enum class AdPlacement {
    HOME_BANNER,
    SETUP_BANNER,
    SETTINGS_BANNER,
    HOW_TO_PLAY_BANNER,
    HISTORY_BANNER,
    CREDITS_BANNER,
    REVEAL_BANNER,
}

object AdsConfig {
    fun adUnitIdFor(
        placement: AdPlacement,
        adsRemoved: Boolean = false,
    ): String? {
        if (adsRemoved || !BuildConfig.ADS_ENABLED || !RuntimeFlags.externalServicesEnabled) return null

        return when (placement) {
            AdPlacement.HOME_BANNER -> BuildConfig.HOME_BANNER_AD_UNIT_ID.takeIf { it.isNotBlank() }
            AdPlacement.SETUP_BANNER -> BuildConfig.HOME_BANNER_AD_UNIT_ID.takeIf { it.isNotBlank() }
            AdPlacement.SETTINGS_BANNER -> BuildConfig.HOME_BANNER_AD_UNIT_ID.takeIf { it.isNotBlank() }
            AdPlacement.HOW_TO_PLAY_BANNER -> BuildConfig.HOME_BANNER_AD_UNIT_ID.takeIf { it.isNotBlank() }
            AdPlacement.HISTORY_BANNER -> BuildConfig.HOME_BANNER_AD_UNIT_ID.takeIf { it.isNotBlank() }
            AdPlacement.CREDITS_BANNER -> BuildConfig.HOME_BANNER_AD_UNIT_ID.takeIf { it.isNotBlank() }
            AdPlacement.REVEAL_BANNER -> BuildConfig.HOME_BANNER_AD_UNIT_ID.takeIf { it.isNotBlank() }
        }
    }
}
