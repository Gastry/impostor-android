package com.impostorparty.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.impostorparty.app.ads.AdPlacement
import com.impostorparty.app.ads.AdsConfig
import com.impostorparty.app.navigation.AppRoute
import com.impostorparty.app.ui.screen.CreditsScreen
import com.impostorparty.app.ui.screen.FeedbackScreen
import com.impostorparty.app.ui.screen.HistoryScreen
import com.impostorparty.app.ui.screen.HomeScreen
import com.impostorparty.app.ui.screen.HowToPlayScreen
import com.impostorparty.app.ui.screen.ResultScreen
import com.impostorparty.app.ui.screen.RevealScreen
import com.impostorparty.app.ui.screen.RoundReadyScreen
import com.impostorparty.app.ui.screen.SettingsScreen
import com.impostorparty.app.ui.screen.SetupScreen
import com.impostorparty.app.ui.theme.ImpostorPartyTheme
import com.impostorparty.app.viewmodel.GameViewModel
import com.impostorparty.app.viewmodel.RemoveAdsBillingViewModel
import com.google.android.play.core.review.ReviewManagerFactory
import com.impostorparty.domain.usecase.RevealFlowState

@Composable
fun ImpostorPartyRoot(
    viewModel: GameViewModel = hiltViewModel(),
    billingViewModel: RemoveAdsBillingViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val adsRemoved by viewModel.adsRemoved.collectAsStateWithLifecycle()
    val activeRound by viewModel.activeRound.collectAsStateWithLifecycle()
    val feedbackForm by viewModel.feedbackForm.collectAsStateWithLifecycle()
    val reviewPrompt by viewModel.reviewPrompt.collectAsStateWithLifecycle()
    val pendingInAppReviewRequest by viewModel.pendingInAppReviewRequest.collectAsStateWithLifecycle()
    val revealState by viewModel.revealState.collectAsStateWithLifecycle()
    val removeAdsUiState by billingViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()
    val currentAppLanguageTag = context.currentAppLanguageTag()
    val lifecycleOwner = LocalLifecycleOwner.current
    val reviewManager = remember(activity) {
        activity?.let(ReviewManagerFactory::create)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    ApplyLanguageEffect(appSettings.languageTag)
    SensitiveContentEffect(
        enabled = route in setOf(AppRoute.Reveal.route, AppRoute.Result.route),
    )

    LaunchedEffect(Unit) {
        billingViewModel.refresh()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                billingViewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(pendingInAppReviewRequest) {
        pendingInAppReviewRequest ?: return@LaunchedEffect
        val currentActivity = activity
        val currentReviewManager = reviewManager

        if (currentActivity == null || currentReviewManager == null) {
            viewModel.onInAppReviewRequestHandled()
            return@LaunchedEffect
        }

        currentReviewManager.requestReviewFlow()
            .addOnCompleteListener { task ->
                val reviewInfo = task.result
                if (task.isSuccessful && reviewInfo != null) {
                    currentReviewManager.launchReviewFlow(currentActivity, reviewInfo)
                        .addOnCompleteListener {
                            viewModel.onInAppReviewRequestHandled()
                        }
                } else {
                    viewModel.onInAppReviewRequestHandled()
                }
            }
    }

    ImpostorPartyTheme(themeMode = appSettings.themeMode) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
        ) {
            composable(AppRoute.Home.route) {
                HomeScreen(
                    stats = viewModel.stats.collectAsStateWithLifecycle().value,
                    homeBannerAdUnitId = AdsConfig.adUnitIdFor(
                        placement = AdPlacement.HOME_BANNER,
                        adsRemoved = adsRemoved,
                    ),
                    removeAdsPriceLabel = removeAdsUiState.priceLabel,
                    onNewGame = { navController.navigate(AppRoute.Setup.route) },
                    onHowToPlay = { navController.navigate(AppRoute.HowToPlay.route) },
                    onSettings = { navController.navigate(AppRoute.Settings.route) },
                    onOpenRemoveAdsSettings = {
                        navController.navigate(AppRoute.Settings.createRoute(highlightRemoveAds = true))
                    },
                    onHistory = { navController.navigate(AppRoute.History.route) },
                    onCredits = { navController.navigate(AppRoute.Credits.route) },
                )
            }

            composable(AppRoute.Setup.route) {
                SetupScreen(
                    setup = viewModel.setup.collectAsStateWithLifecycle().value,
                    isLoading = viewModel.isStartingRound.collectAsStateWithLifecycle().value,
                    message = viewModel.message.collectAsStateWithLifecycle().value,
                    onDismissMessage = viewModel::dismissMessage,
                    onPlayerCountChanged = viewModel::updatePlayerCount,
                    onImpostorCountChanged = viewModel::updateImpostorCount,
                    onToggleCategory = viewModel::toggleCategory,
                    onRoundMinutesChanged = viewModel::updateRoundMinutes,
                    onClueRoundsChanged = viewModel::updateClueRounds,
                    onNoExtraHintsChanged = viewModel::updateNoExtraHints,
                    onQuickModeChanged = viewModel::updateQuickMode,
                    onRevealAnimationChanged = viewModel::updateRevealAnimation,
                    onHapticsChanged = viewModel::updateHaptics,
                    onAvoidRecentWordsChanged = viewModel::updateAvoidRecentWords,
                    onCustomPlayerNameChanged = viewModel::updateCustomPlayerName,
                    onClearCustomNames = viewModel::clearCustomNames,
                    onStartRound = { viewModel.startRound(currentAppLanguageTag) },
                    onBack = { navController.popBackStack() },
                )

                LaunchedEffect(activeRound?.id) {
                    if (activeRound != null) {
                        navController.navigate(AppRoute.Reveal.route) {
                            popUpTo(AppRoute.Setup.route) { inclusive = true }
                        }
                    }
                }
            }

            composable(AppRoute.Reveal.route) {
                RevealScreen(
                    roundSession = activeRound,
                    flowState = revealState,
                    reducedMotion = false,
                    hapticsEnabled = true,
                    onRequestReveal = viewModel::requestReveal,
                    onHideAndPass = viewModel::hideAndPass,
                    onExit = {
                        viewModel.clearCurrentRound()
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )

                LaunchedEffect(revealState) {
                    if (revealState is RevealFlowState.RoundReady) {
                        navController.navigate(AppRoute.RoundReady.route) {
                            popUpTo(AppRoute.Reveal.route) { inclusive = true }
                        }
                    }
                }
            }

            composable(AppRoute.RoundReady.route) {
                RoundReadyScreen(
                    setup = activeRound?.setup,
                    showQuickInstructions = true,
                    onFinishRound = {
                        navController.navigate(AppRoute.Result.route) {
                            popUpTo(AppRoute.RoundReady.route) { inclusive = true }
                        }
                    },
                    onNewConfiguration = {
                        viewModel.clearCurrentRound()
                        navController.navigate(AppRoute.Setup.route) {
                            popUpTo(AppRoute.Setup.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(AppRoute.Result.route) {
                ResultScreen(
                    roundSession = activeRound,
                    winnerSelection = viewModel.winnerSelection.collectAsStateWithLifecycle().value,
                    reviewPrompt = reviewPrompt,
                    onWinnerSelected = viewModel::selectWinner,
                    onReviewNow = viewModel::onReviewNowSelected,
                    onReviewLater = viewModel::dismissReviewPrompt,
                    onSendSuggestion = {
                        viewModel.onSendSuggestionSelected()
                        navController.navigate(AppRoute.Feedback.route)
                    },
                    onPlayAgain = {
                        viewModel.persistRoundResultIfNeeded()
                        viewModel.startRematch(currentAppLanguageTag)
                        navController.navigate(AppRoute.Reveal.route) {
                            popUpTo(AppRoute.Result.route) { inclusive = true }
                        }
                    },
                    onBackToMenu = {
                        viewModel.persistRoundResultIfNeeded()
                        viewModel.clearCurrentRound()
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )

                LaunchedEffect(activeRound?.id) {
                    if (activeRound == null) {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    } else {
                        viewModel.onResultScreenViewed()
                    }
                }
            }

            composable(AppRoute.HowToPlay.route) {
                HowToPlayScreen(
                    bannerAdUnitId = AdsConfig.adUnitIdFor(
                        placement = AdPlacement.HOW_TO_PLAY_BANNER,
                        adsRemoved = adsRemoved,
                    ),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = AppRoute.Settings.RoutePattern,
                arguments = listOf(
                    navArgument(AppRoute.Settings.HighlightRemoveAdsArg) {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
            ) { entry ->
                val highlightRemoveAds = entry.arguments?.getBoolean(AppRoute.Settings.HighlightRemoveAdsArg) == true
                SettingsScreen(
                    settings = appSettings,
                    bannerAdUnitId = AdsConfig.adUnitIdFor(
                        placement = AdPlacement.SETTINGS_BANNER,
                        adsRemoved = adsRemoved,
                    ),
                    removeAdsUiState = removeAdsUiState,
                    highlightRemoveAds = highlightRemoveAds,
                    onThemeModeChanged = viewModel::updateThemeMode,
                    onLanguageChanged = viewModel::updateLanguageTag,
                    onReducedMotionChanged = viewModel::updateReducedMotion,
                    onShowQuickInstructionsChanged = viewModel::updateShowQuickInstructions,
                    onSecureScreenChanged = viewModel::updateSecureScreen,
                    onHapticsChanged = viewModel::updateHaptics,
                    onAvoidRecentChanged = viewModel::updateAvoidRecentWords,
                    onRevealAnimationChanged = viewModel::updateRevealAnimation,
                    onRemoveAds = {
                        activity?.let(billingViewModel::launchPurchase)
                    },
                    onRateApp = { context.openPlayStoreListing() },
                    onSendSuggestion = { navController.navigate(AppRoute.Feedback.route) },
                    onResetPreferences = viewModel::resetPreferences,
                    onClearHistory = viewModel::clearHistory,
                    onDismissBillingMessage = billingViewModel::clearMessage,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.Feedback.route) {
                LaunchedEffect(Unit) {
                    viewModel.clearFeedbackStatus()
                }
                FeedbackScreen(
                    state = feedbackForm,
                    onBack = { navController.popBackStack() },
                    onTypeChanged = viewModel::updateFeedbackType,
                    onMessageChanged = viewModel::updateFeedbackMessage,
                    onEmailChanged = viewModel::updateFeedbackEmail,
                    onSubmit = viewModel::submitFeedback,
                    onRetry = viewModel::retryFeedbackSubmission,
                )
            }

            composable(AppRoute.History.route) {
                HistoryScreen(
                    history = viewModel.history.collectAsStateWithLifecycle().value,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.Credits.route) {
                CreditsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun Context.openPlayStoreListing() {
    val packageName = BuildConfig.APPLICATION_ID.removeSuffix(".debug")
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    runCatching { startActivity(marketIntent) }
        .recoverCatching { startActivity(webIntent) }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is android.content.ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.currentAppLanguageTag(): String {
    return resources.configuration.locales[0]?.toLanguageTag().orEmpty()
}

@Composable
private fun ApplyLanguageEffect(languageTag: String?) {
    LaunchedEffect(languageTag) {
        val desiredLocales = if (languageTag.isNullOrBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.toLanguageTags() != desiredLocales.toLanguageTags()) {
            AppCompatDelegate.setApplicationLocales(desiredLocales)
        }
    }
}

@Composable
private fun SensitiveContentEffect(enabled: Boolean) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(activity, enabled) {
        val window = activity.window
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
