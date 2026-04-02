package com.impostorparty.app.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Setup : AppRoute("setup")
    data object Reveal : AppRoute("reveal")
    data object RoundReady : AppRoute("round_ready")
    data object Result : AppRoute("result")
    data object HowToPlay : AppRoute("how_to_play")
    data object Settings : AppRoute("settings") {
        const val HighlightRemoveAdsArg = "highlight_remove_ads"
        const val RoutePattern = "settings?$HighlightRemoveAdsArg={$HighlightRemoveAdsArg}"

        fun createRoute(highlightRemoveAds: Boolean = false): String {
            return if (highlightRemoveAds) {
                "$route?$HighlightRemoveAdsArg=true"
            } else {
                route
            }
        }
    }
    data object Feedback : AppRoute("feedback")
    data object History : AppRoute("history")
    data object Credits : AppRoute("credits")
}
