package com.impostorparty.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
class LocaleLayoutSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun spanish_locale_renders_home_setup_and_reveal() {
        verifyLanguageFlow("settings_language_es")
    }

    @Test
    fun english_locale_renders_home_setup_and_reveal() {
        verifyLanguageFlow("settings_language_en")
    }

    @Test
    fun french_locale_renders_home_setup_and_reveal() {
        verifyLanguageFlow("settings_language_fr")
    }

    @Test
    fun german_locale_renders_home_setup_and_reveal() {
        verifyLanguageFlow("settings_language_de")
    }

    @Test
    fun italian_locale_renders_home_setup_and_reveal() {
        verifyLanguageFlow("settings_language_it")
    }

    @Test
    fun portuguese_locale_renders_home_setup_and_reveal() {
        verifyLanguageFlow("settings_language_pt")
    }

    @Test
    fun japanese_locale_renders_home_setup_and_reveal() {
        verifyLanguageFlow("settings_language_ja")
    }

    private fun verifyLanguageFlow(languageTag: String) {
        switchLanguage(languageTag)

        composeRule.onNodeWithTag("home_list")
            .performScrollToNode(hasTestTag("home_primary_cta"))
        composeRule.onNodeWithTag("home_primary_cta").assertIsDisplayed()
        composeRule.onNodeWithTag("home_primary_cta").performClick()

        composeRule.onNodeWithTag("setup_start_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("setup_start_button").performClick()

        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("reveal_hold_button").fetchSemanticsNodes().isNotEmpty()
        }

        holdToReveal()

        composeRule.onNodeWithTag("reveal_hide_and_pass_button").performClick()
        composeRule.onNodeWithContentDescription(text(R.string.cd_close)).performClick()

        composeRule.onNodeWithTag("home_primary_cta").assertIsDisplayed()
    }

    private fun switchLanguage(languageTag: String) {
        composeRule.onNodeWithTag("home_list")
            .performScrollToNode(hasTestTag("home_settings"))
        composeRule.onNodeWithTag("home_settings").performClick()

        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag(languageTag))
        composeRule.onNodeWithTag(languageTag).performClick()

        composeRule.onNodeWithContentDescription(text(R.string.cd_back)).performClick()
        composeRule.waitUntil(timeoutMillis = 6_000) {
            composeRule.onAllNodesWithTag("home_primary_cta").fetchSemanticsNodes().isNotEmpty()
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

    private fun text(id: Int): String = composeRule.activity.getString(id)
}
