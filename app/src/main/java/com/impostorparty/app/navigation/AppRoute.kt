package com.impostorparty.app.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Setup : AppRoute("setup")
    data object Reveal : AppRoute("reveal")
    data object RoundReady : AppRoute("round_ready")
    data object Result : AppRoute("result")
    data object HowToPlay : AppRoute("how_to_play")
    data object Settings : AppRoute("settings")
    data object Feedback : AppRoute("feedback")
    data object History : AppRoute("history")
    data object Credits : AppRoute("credits")
}
