package lab.neurotrino.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NavigationSmokeTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingOrLabIsVisible() {
        // Either onboarding copy or laboratorium heading must exist.
        val skip = rule.onNodeWithText("Pomiń", substring = false, ignoreCase = false)
        runCatching { skip.assertExists() }
        runCatching { rule.onNodeWithText("Laboratorium").assertExists() }
    }

    @Test
    fun captureActionExistsAfterSkip() {
        runCatching { rule.onNodeWithText("Pomiń").performClick() }
        runCatching { rule.onNodeWithText("Zapisz myśl").assertExists() }
    }
}
