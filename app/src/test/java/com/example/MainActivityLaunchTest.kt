package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.BazmoAuthManager
import com.example.ui.theme.BazmoThemeManager
import com.example.ui.theme.BazmoThemeMode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivityLaunchTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        BazmoAuthManager.resetInMemoryForTest()
        ctx.getSharedPreferences("bazmo_auth_session", Context.MODE_PRIVATE).edit().clear().commit()
        BazmoThemeManager.resetInMemoryForTest()
    }

    @Test
    fun `main activity launches login screen and opens registration dialog without crashing`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("login_email_input").assertExists()
        composeRule.onNodeWithTag("login_password_input").assertExists()
        composeRule.onNodeWithTag("password_signin_button").assertExists()
        composeRule.onNodeWithTag("open_registration_button")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("registration_verification_notice").assertExists()
    }

    @Test
    fun `main activity survives dark theme toggle without crashing`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        composeRule.waitForIdle()
        composeRule.runOnUiThread {
            BazmoThemeManager.setMode(ctx, BazmoThemeMode.DARK)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("login_email_input").assertExists()
    }
}
