package com.impostorparty.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.impostorparty.app.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun home_to_setup_navigation() {
        startRoundSetupFromHome()
        composeRule.onNodeWithText(text(R.string.setup_categories_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("setup_start_button").assertIsDisplayed()
    }

    @Test
    fun home_to_how_to_play_shows_game_flow_and_tips() {
        composeRule.onNodeWithTag("home_how_to_play").performClick()
        composeRule.onNodeWithTag("how_objective_card").assertIsDisplayed()
        composeRule.onNodeWithTag("how_to_play_list")
            .performScrollToNode(hasTestTag("how_play_card"))
        composeRule.onNodeWithTag("how_play_card").assertIsDisplayed()
        composeRule.onNodeWithTag("how_to_play_list")
            .performScrollToNode(hasTestTag("how_tips_card"))
        composeRule.onNodeWithTag("how_tips_card").assertIsDisplayed()
    }

    @Test
    fun setup_valid_to_reveal_navigation() {
        startRoundFromHome()
        composeRule.onNodeWithTag("reveal_hold_button").assertIsDisplayed()
    }

    @Test
    fun setup_valid_to_round_ready_shows_basic_instructions() {
        startRoundSetupFromHome()
        composeRule.onNodeWithTag("setup_start_button").performScrollTo().performClick()

        completeRevealFlow(playerCount = 6)

        composeRule.onNodeWithText(text(R.string.round_ready_instructions)).assertIsDisplayed()
    }

    @Test
    fun reveal_multiple_players_to_round_ready_navigation() {
        startRoundFromHome()
        completeRevealFlow(playerCount = 6)
        composeRule.onNodeWithTag("round_ready_finish_button").assertIsDisplayed()
    }

    @Test
    fun round_ready_to_result_navigation() {
        startRoundFromHome()
        completeRevealFlow(playerCount = 6)
        composeRule.onNodeWithTag("round_ready_finish_button").performClick()
        composeRule.onNodeWithTag("result_rematch_button").assertIsDisplayed()
    }

    @Test
    fun result_to_rematch_navigation() {
        startRoundFromHome()
        completeRevealFlow(playerCount = 6)
        composeRule.onNodeWithTag("round_ready_finish_button").performClick()
        composeRule.onNodeWithTag("result_rematch_button").performClick()

        composeRule.onNodeWithTag("reveal_hold_button").assertIsDisplayed()
    }

    private fun startRoundSetupFromHome() {
        composeRule.onNodeWithTag("home_list")
            .performScrollToNode(hasTestTag("home_primary_cta"))
        composeRule.onNodeWithTag("home_primary_cta").performClick()
    }

    private fun startRoundFromHome() {
        startRoundSetupFromHome()
        composeRule.onNodeWithTag("setup_start_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 7_000) {
            composeRule.onAllNodesWithTag("reveal_hold_button")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun completeRevealFlow(playerCount: Int) {
        repeat(playerCount) {
            holdToReveal()
            composeRule.onNodeWithTag("reveal_hide_and_pass_button").performClick()
        }

        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("round_ready_finish_button")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun holdToReveal() {
        composeRule.onNodeWithTag("reveal_hold_button").performTouchInput {
            down(center)
            advanceEventTime(900)
            up()
        }
        composeRule.waitUntil(timeoutMillis = 6_000) {
            composeRule.onAllNodesWithTag("reveal_hide_and_pass_button")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun text(id: Int, vararg formatArgs: Any): String = composeRule.activity.getString(id, *formatArgs)
}
