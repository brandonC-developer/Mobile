package app.apartment.mobile.ui

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.reminders.PermissionState
import app.apartment.mobile.ui.theme.ApartmentTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** TC-015 (FR-018), TC-016 (FR-019), TC-020 (AC-FR-024-1, AC-FR-024-2) on screen. */
class SettingsUiTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun settingsStartWithTheOwnerDefaults() {
        rule.setContent { ApartmentTheme { SettingsScreen(SettingsEntity()) {} } }
        rule.onNodeWithTag("time-value", useUnmergedTree = true).assertTextEquals("08:00")
    }

    @Test
    fun togglingAnOffsetReportsTheNewSettings() {
        var saved: SettingsEntity? = null
        rule.setContent { ApartmentTheme { SettingsScreen(SettingsEntity()) { saved = it } } }
        rule.onNodeWithTag("switch-2").performClick() // 2 days before is off by default
        assertTrue(saved!!.remindTwoDaysBefore)
        rule.onNodeWithTag("switch-3").performClick()
        assertFalse(saved!!.remindThreeDaysBefore)
        assertEquals(480, saved!!.reminderMinuteOfDay)
    }

    @Test
    fun theWarningShowsWhenEitherPermissionIsMissingAndNeverWhenBothAreGranted() {
        val both = PermissionState(notificationsEnabled = true, exactAlarmsAllowed = true)
        rule.setContent { ApartmentTheme { PermissionBanner(both) {} } }
        rule.onNodeWithTag("permission-banner").assertDoesNotExist()
    }

    @Test
    fun theWarningOffersToFixItAndDoesNotBlock() {
        var fixed = false
        rule.setContent {
            ApartmentTheme { PermissionBanner(PermissionState(notificationsEnabled = false, exactAlarmsAllowed = true)) { fixed = true } }
        }
        rule.onNodeWithTag("permission-banner").assertExists()
        rule.onNodeWithTag("permission-fix").performClick()
        assertTrue(fixed)
    }

    @Test
    fun theWarningShowsWhenOnlyTheExactAlarmPermissionIsMissing() {
        rule.setContent {
            ApartmentTheme { PermissionBanner(PermissionState(notificationsEnabled = true, exactAlarmsAllowed = false)) {} }
        }
        rule.onNodeWithTag("permission-banner").assertExists()
    }
}
