package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.example.ui.components.StudentAvatar
import com.example.ui.components.StudentLargeProfilePhoto
import com.example.ui.components.WaliTopBar
import com.example.ui.dialogs.StudentPhotoPreviewDialog
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileLogoFallbackRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `StudentAvatar and StudentLargeProfilePhoto render temporary logo and label when photoUrl is empty`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppLanguageManager.getStrings(AppLanguage.ID)) {
                Column {
                    StudentAvatar(
                        name = "Ahmad Fauzi",
                        gender = "L",
                        size = 48.dp,
                        photoUrl = "",
                        photoFallbackUrl = ""
                    )
                    StudentLargeProfilePhoto(
                        name = "Siti Aminah",
                        gender = "P",
                        photoUrl = "",
                        photoFallbackUrl = "",
                        emptyHint = "Logo sementara • Foto ananda belum diunggah",
                        modifier = Modifier.size(180.dp, 240.dp)
                    )
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Ahmad Fauzi").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Siti Aminah").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logo sementara • Foto ananda belum diunggah").assertIsDisplayed()
    }

    @Test
    fun `WaliTopBar renders Wali avatar button even when wali photo is empty`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppLanguageManager.getStrings(AppLanguage.ID)) {
                WaliTopBar(
                    waliName = "Ustadz Budi",
                    waliPhotoUrl = "",
                    waliPhotoFallbackUrl = "",
                    waliDormitory = "Asrama Putra",
                    onProfileClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("btn_top_avatar").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Ustadz Budi").assertIsDisplayed()
    }

    @Test
    fun `StudentPhotoPreviewDialog shows temporary app logo notice when photo is empty`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppLanguageManager.getStrings(AppLanguage.ID)) {
                StudentPhotoPreviewDialog(
                    studentName = "Ustadzah Rina",
                    gender = "P",
                    subtitle = "Wali Asuh • SR Kediri",
                    photoUrl = "",
                    photoFallbackUrl = "",
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("dialog_student_photo_preview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Belum ada foto profil yang diunggah (menampilkan logo aplikasi sementara).").assertIsDisplayed()
    }
}
