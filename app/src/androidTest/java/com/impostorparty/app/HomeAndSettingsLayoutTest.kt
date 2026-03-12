package com.impostorparty.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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
        composeRule.onNodeWithTag("home_logo").assertIsDisplayed()
        composeRule.onNodeWithTag("home_primary_cta").assertIsDisplayed()
    }

    @Test
    fun settings_is_scrollable_and_reset_cta_is_reachable() {
        composeRule.onNodeWithText(text(R.string.settings_title)).performClick()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_reset_cta"))
        composeRule.onNodeWithTag("settings_reset_cta").assertIsDisplayed()
    }

    private fun text(id: Int): String = composeRule.activity.getString(id)
}
