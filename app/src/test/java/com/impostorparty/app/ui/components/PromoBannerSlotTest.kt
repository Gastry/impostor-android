package com.impostorparty.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromoBannerSlotTest {

    @Test
    fun `starts with AdMob when there are no previous impressions`() {
        val option = pickPromoBannerOption(
            adMobAvailable = true,
            exposureStats = BannerExposureStats(),
        )

        assertEquals(PromoBannerOption.ADMOB, option)
    }

    @Test
    fun `does not schedule internal promo when fallback impressions already consumed the quota`() {
        assertFalse(
            shouldShowInternalPromo(
                BannerExposureStats(
                    totalShown = 20,
                    internalShown = 2,
                ),
            ),
        )
    }

    @Test
    fun `schedules one internal promo when the real exposure is below target`() {
        assertTrue(
            shouldShowInternalPromo(
                BannerExposureStats(
                    totalShown = 39,
                    internalShown = 1,
                ),
            ),
        )
    }

    @Test
    fun `uses internal promo directly when AdMob is unavailable`() {
        val option = pickPromoBannerOption(
            adMobAvailable = false,
            exposureStats = BannerExposureStats(
                totalShown = 100,
                internalShown = 5,
            ),
        )

        assertTrue(option != PromoBannerOption.ADMOB)
    }
}
