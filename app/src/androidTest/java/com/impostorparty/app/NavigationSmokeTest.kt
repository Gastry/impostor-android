package com.impostorparty.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.new_game)).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.setup_title)).assertIsDisplayed()
    }
}