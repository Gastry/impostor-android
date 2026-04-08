package com.impostorparty.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.impostorparty.app.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeAndSettingsLayoutTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun home_shows_branding_and_primary_cta() {
        composeRule.onNodeWithTag("home_title").assertIsDisplayed()
        composeRule.onNodeWithTag("home_primary_cta").assertIsDisplayed()
    }

    @Test
    fun settings_is_scrollable_and_reset_cta_is_reachable() {
        composeRule.onNodeWithText(text(R.string.settings_title)).performClick()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_reset_cta"))
        composeRule.onNodeWithTag("settings_reset_cta").assertIsDisplayed()
    }

    @Test
    fun settings_feedback_stub_is_reachable() {
        composeRule.onNodeWithText(text(R.string.settings_title)).performClick()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_send_feedback"))
        composeRule.onNodeWithTag("settings_send_feedback").performClick()
        composeRule.onNodeWithTag("feedback_send").assertIsDisplayed()
    }

    @Test
    fun feedback_reopen_clears_transient_error_and_keeps_draft() {
        composeRule.onNodeWithText(text(R.string.settings_title)).performClick()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_send_feedback"))
        composeRule.onNodeWithTag("settings_send_feedback").performClick()

        composeRule.onNodeWithTag("feedback_message").performTextInput("Enough detail for feedback")
        composeRule.onNodeWithTag("feedback_email").performTextInput("bad-email")
        composeRule.onNodeWithTag("feedback_send").performClick()
        composeRule.onNodeWithTag("feedback_email").assertTextContains("bad-email")

        composeRule.activity.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_send_feedback"))
        composeRule.onNodeWithTag("settings_send_feedback").performClick()

        composeRule.onNodeWithTag("feedback_email").assertTextContains("bad-email")
        composeRule.onNodeWithTag("feedback_message").assertTextContains("Enough detail for feedback")
        composeRule.onNodeWithTag("feedback_send").assertIsDisplayed()
    }

    private fun text(id: Int): String = composeRule.activity.getString(id)
}
