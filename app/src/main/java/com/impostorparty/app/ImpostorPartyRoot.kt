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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.impostorparty.app.ui.screen.PlayerNamesScreen
import com.impostorparty.app.ui.screen.ResultScreen
import com.impostorparty.app.ui.screen.RevealScreen
import com.impostorparty.app.ui.screen.RoundReadyScreen
import com.impostorparty.app.ui.screen.SettingsScreen
import com.impostorparty.app.ui.screen.SetupScreen
import com.impostorparty.app.ui.theme.ImpostorPartyTheme
import com.impostorparty.app.viewmodel.FeedbackViewModel
import com.impostorparty.app.viewmodel.GameSessionViewModel
import com.impostorparty.app.viewmodel.RemoveAdsBillingViewModel
import com.impostorparty.app.viewmodel.SettingsViewModel
import com.impostorparty.app.viewmodel.SetupViewModel
import com.google.android.play.core.review.ReviewManagerFactory
import com.impostorparty.domain.usecase.CreateRoundResult
import com.impostorparty.domain.usecase.RevealFlowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImpostorPartyRoot(
    setupViewModel: SetupViewModel = hiltViewModel(),
    sessionViewModel: GameSessionViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    feedbackViewModel: FeedbackViewModel = hiltViewModel(),
    billingViewModel: RemoveAdsBillingViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val appSettings by settingsViewModel.appSettings.collectAsStateWithLifecycle()
    val adsRemoved by settingsViewModel.adsRemoved.collectAsStateWithLifecycle()
    val activeRound by sessionViewModel.activeRound.collectAsStateWithLifecycle()
    val feedbackForm by feedbackViewModel.feedbackForm.collectAsStateWithLifecycle()
    val reviewPrompt by sessionViewModel.reviewPrompt.collectAsStateWithLifecycle()
    val pendingInAppReviewRequest by sessionViewModel.pendingInAppReviewRequest.collectAsStateWithLifecycle()
    val revealState by sessionViewModel.revealState.collectAsStateWithLifecycle()
    val removeAdsUiState by billingViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()
    val currentAppLanguageTag = context.currentAppLanguageTag()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
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
            sessionViewModel.onInAppReviewRequestHandled()
            return@LaunchedEffect
        }

        currentReviewManager.requestReviewFlow()
            .addOnCompleteListener { task ->
                val reviewInfo = task.result
                if (task.isSuccessful && reviewInfo != null) {
                    currentReviewManager.launchReviewFlow(currentActivity, reviewInfo)
                        .addOnCompleteListener {
                            sessionViewModel.onInAppReviewRequestHandled()
                        }
                } else {
                    sessionViewModel.onInAppReviewRequestHandled()
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
                    stats = settingsViewModel.stats.collectAsStateWithLifecycle().value,
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
                    setup = setupViewModel.setup.collectAsStateWithLifecycle().value,
                    bannerAdUnitId = AdsConfig.adUnitIdFor(
                        placement = AdPlacement.SETUP_BANNER,
                        adsRemoved = adsRemoved,
                    ),
                    removeAdsPriceLabel = removeAdsUiState.priceLabel,
                    onOpenRemoveAdsSettings = {
                        navController.navigate(AppRoute.Settings.createRoute(highlightRemoveAds = true))
                    },
                    isLoading = setupViewModel.isStartingRound.collectAsStateWithLifecycle().value,
                    message = setupViewModel.message.collectAsStateWithLifecycle().value,
                    onDismissMessage = setupViewModel::dismissMessage,
                    onPlayerCountChanged = setupViewModel::updatePlayerCount,
                    onImpostorCountChanged = setupViewModel::updateImpostorCount,
                    onToggleCategory = setupViewModel::toggleCategory,
                    onStartRound = {
                        setupViewModel.preparePlayerNames(currentAppLanguageTag)
                        navController.navigate(AppRoute.PlayerNames.route)
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.PlayerNames.route) {
                PlayerNamesScreen(
                    setup = setupViewModel.setup.collectAsStateWithLifecycle().value,
                    bannerAdUnitId = AdsConfig.adUnitIdFor(
                        placement = AdPlacement.SETUP_BANNER,
                        adsRemoved = adsRemoved,
                    ),
                    removeAdsPriceLabel = removeAdsUiState.priceLabel,
                    onOpenRemoveAdsSettings = {
                        navController.navigate(AppRoute.Settings.createRoute(highlightRemoveAds = true))
                    },
                    isLoading = setupViewModel.isStartingRound.collectAsStateWithLifecycle().value,
                    message = setupViewModel.message.collectAsStateWithLifecycle().value,
                    onDismissMessage = setupViewModel::dismissMessage,
                    onNameChanged = setupViewModel::updatePlayerName,
                    onClearName = setupViewModel::clearPlayerName,
                    onClearAll = setupViewModel::clearPlayerNames,
                    playerNamesAsWordsAvailable = setupViewModel.canUsePlayerNamesAsWords(currentAppLanguageTag),
                    onPlayerNamesAsWordsChanged = {
                        setupViewModel.setPlayerNamesAsWordsEnabled(it, currentAppLanguageTag)
                    },
                    onStartRound = {
                        scope.launch {
                            val result = setupViewModel.createRound(currentAppLanguageTag)
                            if (result is CreateRoundResult.Success) {
                                withContext(Dispatchers.Main.immediate) {
                                    sessionViewModel.activateRound(result.session)
                                    navController.navigate(AppRoute.Reveal.route) {
                                        popUpTo(AppRoute.Setup.route) { inclusive = true }
                                    }
                                }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.Reveal.route) {
                RevealScreen(
                    roundSession = activeRound,
                    flowState = revealState,
                    reducedMotion = appSettings.reducedMotion,
                    hapticsEnabled = appSettings.hapticsEnabled,
                    onRequestReveal = sessionViewModel::requestReveal,
                    onHideAndPass = sessionViewModel::hideAndPass,
                    onExit = {
                        sessionViewModel.clearCurrentRound()
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
                    showQuickInstructions = appSettings.showQuickInstructions,
                    onFinishRound = {
                        navController.navigate(AppRoute.Result.route) {
                            popUpTo(AppRoute.RoundReady.route) { inclusive = true }
                        }
                    },
                    onNewConfiguration = {
                        sessionViewModel.clearCurrentRound()
                        navController.navigate(AppRoute.Setup.route) {
                            popUpTo(AppRoute.Setup.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(AppRoute.Result.route) {
                ResultScreen(
                    roundSession = activeRound,
                    winnerSelection = sessionViewModel.winnerSelection.collectAsStateWithLifecycle().value,
                    reviewPrompt = reviewPrompt,
                    onWinnerSelected = sessionViewModel::selectWinner,
                    onReviewNow = sessionViewModel::onReviewNowSelected,
                    onReviewLater = sessionViewModel::dismissReviewPrompt,
                    onSendSuggestion = {
                        sessionViewModel.onSendSuggestionSelected()
                        navController.navigate(AppRoute.Feedback.route)
                    },
                    onPlayAgain = {
                        sessionViewModel.persistRoundResultIfNeeded()
                        val rematchSetup = activeRound?.setup
                        scope.launch {
                            val result = setupViewModel.createRound(
                                currentAppLanguageTag = currentAppLanguageTag,
                                sourceSetup = rematchSetup,
                            )
                            if (result is CreateRoundResult.Success) {
                                withContext(Dispatchers.Main.immediate) {
                                    sessionViewModel.activateRound(result.session)
                                    navController.navigate(AppRoute.Reveal.route) {
                                        popUpTo(AppRoute.Result.route) { inclusive = true }
                                    }
                                }
                            }
                        }
                    },
                    onBackToMenu = {
                        sessionViewModel.persistRoundResultIfNeeded()
                        sessionViewModel.clearCurrentRound()
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
                        sessionViewModel.onResultScreenViewed()
                    }
                }
            }

            composable(AppRoute.HowToPlay.route) {
                HowToPlayScreen(
                    bannerAdUnitId = AdsConfig.adUnitIdFor(
                        placement = AdPlacement.HOW_TO_PLAY_BANNER,
                        adsRemoved = adsRemoved,
                    ),
                    removeAdsPriceLabel = removeAdsUiState.priceLabel,
                    onOpenRemoveAdsSettings = {
                        navController.navigate(AppRoute.Settings.createRoute(highlightRemoveAds = true))
                    },
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
                    onThemeModeChanged = settingsViewModel::updateThemeMode,
                    onLanguageChanged = settingsViewModel::updateLanguageTag,
                    onReducedMotionChanged = settingsViewModel::updateReducedMotion,
                    onShowQuickInstructionsChanged = settingsViewModel::updateShowQuickInstructions,
                    onSecureScreenChanged = settingsViewModel::updateSecureScreen,
                    onHapticsChanged = settingsViewModel::updateHaptics,
                    onRevealAnimationChanged = settingsViewModel::updateRevealAnimation,
                    onRemoveAds = {
                        activity?.let(billingViewModel::launchPurchase)
                    },
                    onRateApp = { context.openPlayStoreListing() },
                    onSendSuggestion = { navController.navigate(AppRoute.Feedback.route) },
                    onResetPreferences = {
                        settingsViewModel.resetPreferences()
                        setupViewModel.resetToDefaults()
                    },
                    onClearHistory = settingsViewModel::clearHistory,
                    onDismissBillingMessage = billingViewModel::clearMessage,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.Feedback.route) {
                LaunchedEffect(Unit) {
                    feedbackViewModel.clearFeedbackStatus()
                }
                FeedbackScreen(
                    state = feedbackForm,
                    onBack = { navController.popBackStack() },
                    onTypeChanged = feedbackViewModel::updateFeedbackType,
                    onMessageChanged = feedbackViewModel::updateFeedbackMessage,
                    onEmailChanged = feedbackViewModel::updateFeedbackEmail,
                    onSubmit = {
                        feedbackViewModel.submitFeedback(sessionViewModel.getFeedbackContextHint())
                    },
                    onRetry = {
                        feedbackViewModel.retryFeedbackSubmission(sessionViewModel.getFeedbackContextHint())
                    },
                )
            }

            composable(AppRoute.History.route) {
                HistoryScreen(
                    history = settingsViewModel.history.collectAsStateWithLifecycle().value,
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
            withContext(Dispatchers.Main.immediate) {
                AppCompatDelegate.setApplicationLocales(desiredLocales)
            }
        }
    }
}

@Composable
private fun SensitiveContentEffect(enabled: Boolean) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(activity, enabled) {
        val window = activity.window
        if (enabled && !BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
