package com.impostorparty.app

import android.app.Activity
import android.content.Context
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.google.android.play.core.review.ReviewManagerFactory
import com.impostorparty.domain.usecase.RevealFlowState

@Composable
fun ImpostorPartyRoot(viewModel: GameViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val activeRound by viewModel.activeRound.collectAsStateWithLifecycle()
    val feedbackForm by viewModel.feedbackForm.collectAsStateWithLifecycle()
    val reviewPrompt by viewModel.reviewPrompt.collectAsStateWithLifecycle()
    val pendingInAppReviewRequest by viewModel.pendingInAppReviewRequest.collectAsStateWithLifecycle()
    val revealState by viewModel.revealState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()
    val reviewManager = remember(activity) {
        activity?.let(ReviewManagerFactory::create)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    ApplyLanguageEffect(appSettings.languageTag)
    SensitiveContentEffect(
        enabled = appSettings.secureScreen && route in setOf(AppRoute.Reveal.route, AppRoute.Result.route),
    )

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
                    onNewGame = { navController.navigate(AppRoute.Setup.route) },
                    onHowToPlay = { navController.navigate(AppRoute.HowToPlay.route) },
                    onSettings = { navController.navigate(AppRoute.Settings.route) },
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
                    onStartRound = viewModel::startRound,
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
                    reducedMotion = appSettings.reducedMotion,
                    hapticsEnabled = viewModel.setup.collectAsStateWithLifecycle().value.hapticsEnabled,
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
                    showQuickInstructions = appSettings.showQuickInstructions,
                    onFinishRound = { navController.navigate(AppRoute.Result.route) },
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
                        viewModel.startRematch()
                        navController.navigate(AppRoute.Reveal.route) {
                            popUpTo(AppRoute.Result.route) { inclusive = true }
                        }
                    },
                    onNewConfiguration = {
                        viewModel.persistRoundResultIfNeeded()
                        viewModel.clearCurrentRound()
                        navController.navigate(AppRoute.Setup.route) {
                            popUpTo(AppRoute.Setup.route) { inclusive = true }
                        }
                    },
                )

                LaunchedEffect(activeRound?.id) {
                    if (activeRound != null) {
                        viewModel.onResultScreenViewed()
                    }
                }
            }

            composable(AppRoute.HowToPlay.route) {
                HowToPlayScreen(onBack = { navController.popBackStack() })
            }

            composable(AppRoute.Settings.route) {
                SettingsScreen(
                    settings = appSettings,
                    onThemeModeChanged = viewModel::updateThemeMode,
                    onLanguageChanged = viewModel::updateLanguageTag,
                    onReducedMotionChanged = viewModel::updateReducedMotion,
                    onShowQuickInstructionsChanged = viewModel::updateShowQuickInstructions,
                    onSecureScreenChanged = viewModel::updateSecureScreen,
                    onHapticsChanged = viewModel::updateHaptics,
                    onAvoidRecentChanged = viewModel::updateAvoidRecentWords,
                    onRevealAnimationChanged = viewModel::updateRevealAnimation,
                    onRateApp = viewModel::launchManualReviewFlow,
                    onSendSuggestion = { navController.navigate(AppRoute.Feedback.route) },
                    onResetPreferences = viewModel::resetPreferences,
                    onClearHistory = viewModel::clearHistory,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(AppRoute.Feedback.route) {
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

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is android.content.ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
private fun ApplyLanguageEffect(languageTag: String?) {
    LaunchedEffect(languageTag) {
        val localeList = if (languageTag.isNullOrBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
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
