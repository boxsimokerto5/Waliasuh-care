package com.example

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.util.FlexibleUpdatePromptBanner
import com.example.util.InAppUpdateCoordinator
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import com.google.android.play.core.install.model.InstallStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InAppUpdateFlexibleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `InAppUpdateCoordinator tracks downloading downloaded and dismissal states`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val coordinator = InAppUpdateCoordinator(app)

        coordinator.handleInstallStatus(
            status = InstallStatus.DOWNLOADING,
            bytesDownloaded = 50L,
            totalBytesToDownload = 100L
        )
        assertTrue(coordinator.uiState.value.isDownloading)
        assertEquals(0.5f, coordinator.uiState.value.downloadProgress ?: 0f, 0.01f)
        assertFalse(coordinator.uiState.value.isDownloadedReadyToInstall)

        coordinator.handleInstallStatus(status = InstallStatus.DOWNLOADED)
        assertFalse(coordinator.uiState.value.isDownloading)
        assertTrue(coordinator.uiState.value.isDownloadedReadyToInstall)
        assertFalse(coordinator.uiState.value.isBannerDismissed)

        coordinator.dismissBanner()
        assertTrue(coordinator.uiState.value.isBannerDismissed)

        coordinator.onUpdateFlowResult(Activity.RESULT_CANCELED)
        assertFalse(coordinator.uiState.value.isDownloading)

        coordinator.handleInstallStatus(status = InstallStatus.INSTALLED)
        assertFalse(coordinator.uiState.value.isDownloadedReadyToInstall)
    }

    @Test
    fun `FlexibleUpdatePromptBanner displays download progress and restart action then dismisses`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val coordinator = InAppUpdateCoordinator(app)
        var restartTriggered = false

        composeTestRule.setContent {
            val state by coordinator.uiState.collectAsState()
            CompositionLocalProvider(LocalAppStrings provides AppLanguageManager.getStrings(AppLanguage.ID)) {
                FlexibleUpdatePromptBanner(
                    state = state,
                    onRestartClick = { restartTriggered = true },
                    onDismissClick = { coordinator.dismissBanner() }
                )
            }
        }

        coordinator.handleInstallStatus(
            status = InstallStatus.DOWNLOADING,
            bytesDownloaded = 25L,
            totalBytesToDownload = 100L
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("banner_flexible_in_app_update").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mengunduh pembaruan aplikasi...").assertIsDisplayed()
        composeTestRule.onNodeWithTag("progress_flexible_update_download").assertIsDisplayed()

        coordinator.handleInstallStatus(status = InstallStatus.DOWNLOADED)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Pembaruan aplikasi siap dipasang").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_flexible_update_restart").performClick()
        assertTrue(restartTriggered)

        composeTestRule.onNodeWithTag("btn_flexible_update_dismiss").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("banner_flexible_in_app_update").assertDoesNotExist()
    }
}
