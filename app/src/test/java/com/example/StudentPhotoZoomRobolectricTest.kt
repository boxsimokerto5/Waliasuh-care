package com.example

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.dialogs.StudentPhotoPreviewDialog
import com.example.ui.screens.ParentPortalScreen
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudentPhotoZoomRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTempPhotoFile(app: Application): File {
        val file = File(app.cacheDir, "test_student_zoom_${System.currentTimeMillis()}.png")
        val bmp = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(android.graphics.Color.CYAN)
        file.outputStream().use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bmp.recycle()
        return file
    }

    private suspend fun waitUntil(maxTicks: Int = 150, condition: () -> Boolean) {
        var ticks = 0
        while (ticks < maxTicks && !condition()) {
            ShadowLooper.idleMainLooper()
            delay(35)
            ticks++
        }
    }

    @Test
    fun `StudentPhotoPreviewDialog supports 2-finger pinch zoom pan double-tap and reset controls`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val photoFile = createTempPhotoFile(app)
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        try {
            composeTestRule.setContent {
                CompositionLocalProvider(LocalAppStrings provides strings) {
                    StudentPhotoPreviewDialog(
                        studentName = "Alya Putri",
                        gender = "P",
                        subtitle = "VIII-A • Asrama Putri",
                        photoUrl = photoFile.absolutePath,
                        onDismiss = {}
                    )
                }
            }
            composeTestRule.waitForIdle()

            // Initial state: 100%, zoom controls & hint visible, zoom-out/reset disabled
            composeTestRule.onNodeWithTag("dialog_student_photo_preview").assertIsDisplayed()
            composeTestRule.onNodeWithTag("box_student_photo_zoom_canvas").assertIsDisplayed()
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_in")
                .assertIsDisplayed()
                .assertHeightIsAtLeast(48.dp)
                .assertIsEnabled()
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_out")
                .assertIsDisplayed()
                .assertHeightIsAtLeast(48.dp)
                .assertIsNotEnabled()
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_reset")
                .assertIsDisplayed()
                .assertHeightIsAtLeast(48.dp)
                .assertIsNotEnabled()
                .assertTextContains("100%", substring = true)
            composeTestRule.onNodeWithTag("text_student_photo_zoom_hint").assertIsDisplayed()
            composeTestRule.onNodeWithTag("badge_student_photo_zoom").assertDoesNotExist()

            // 1. 2-finger pinch outward zooms in above 100%
            composeTestRule.onNodeWithTag("box_student_photo_zoom_canvas").performTouchInput {
                val c = center
                pinch(
                    start0 = c - Offset(24f, 0f),
                    end0 = c - Offset(120f, 0f),
                    start1 = c + Offset(24f, 0f),
                    end1 = c + Offset(120f, 0f),
                    durationMillis = 300L
                )
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("badge_student_photo_zoom").assertIsDisplayed()
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_out").assertIsEnabled()
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_reset")
                .assertIsEnabled()
                .assertTextContains("Reset", substring = true)

            // Pan gesture while zoomed keeps canvas stable & zoomed
            composeTestRule.onNodeWithTag("box_student_photo_zoom_canvas").performTouchInput {
                swipeLeft(startX = centerX + 40f, endX = centerX - 40f, durationMillis = 150L)
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("badge_student_photo_zoom").assertIsDisplayed()

            // Reset button returns to 100%
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_reset").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("badge_student_photo_zoom").assertDoesNotExist()
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_reset")
                .assertIsNotEnabled()
                .assertTextContains("100%", substring = true)

            // 2. Double-tap zooms to 250%, and double-tap again resets to 100%
            composeTestRule.onNodeWithTag("box_student_photo_zoom_canvas").performTouchInput {
                doubleClick(center)
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("badge_student_photo_zoom")
                .assertIsDisplayed()
                .assertTextContains("250%", substring = true)

            composeTestRule.onNodeWithTag("box_student_photo_zoom_canvas").performTouchInput {
                doubleClick(center)
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("badge_student_photo_zoom").assertDoesNotExist()

            // 3. + / - buttons increment & decrement zoom smoothly
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_in").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("badge_student_photo_zoom")
                .assertIsDisplayed()
                .assertTextContains("150%", substring = true)
            composeTestRule.onNodeWithTag("btn_student_photo_zoom_out").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("badge_student_photo_zoom").assertDoesNotExist()
        } finally {
            photoFile.delete()
        }
    }

    @Test
    fun `StudentPhotoPreviewDialog hides zoom toolbar when no profile photo exists`() {
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                StudentPhotoPreviewDialog(
                    studentName = "Budi Tanpa Foto",
                    gender = "L",
                    subtitle = "VII-B",
                    photoUrl = "",
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_student_photo_preview").assertIsDisplayed()
        composeTestRule.onNodeWithTag("box_student_photo_zoom_canvas").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_student_photo_zoom_in").assertDoesNotExist()
        composeTestRule.onNodeWithTag("text_student_photo_zoom_hint").assertDoesNotExist()
    }

    @Test
    fun `ParentPortalScreen opens student photo preview with working pinch zoom`(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val photoFile = createTempPhotoFile(app)

        val student = ChildStudent(
            id = 6801L,
            name = "Kirana Dewi",
            nis = "6801",
            nik = "3506680100010001",
            gender = "P",
            age = 13,
            dormitory = "Asrama Putri",
            classGrade = "VII-A",
            district = "Wates",
            photoUrl = photoFile.absolutePath,
            assignedWaliUsername = "ust_zoom",
            assignedWaliName = "Ustadzah Zoom",
            schoolId = schoolId
        )
        val parentLink = ParentAccessLink(
            id = 6891L,
            username = "ortu_kirana_6801",
            password = "Pass#6801",
            studentId = 6801L,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            studentDistrict = student.district,
            parentName = "Ibu Kirana",
            assignedWaliUsername = "ust_zoom",
            assignedWaliName = "Ustadzah Zoom",
            schoolId = schoolId,
            canViewProfile = true
        )

        db.childStudentDao().insert(student)
        db.parentAccessLinkDao().insert(parentLink)

        try {
            assertTrue(vm.executeLogin("ortu_kirana_6801", "Pass#6801") is LoginResult.SuccessParent)
            waitUntil { vm.activeParentAccessLink.value != null && vm.childStudents.value.any { it.id == 6801L } }

            val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
            composeTestRule.setContent {
                CompositionLocalProvider(LocalAppStrings provides strings) {
                    ParentPortalScreen(viewModel = vm, onLogout = {})
                }
            }
            composeTestRule.waitForIdle()

            // Navigate to Profile tab and open child's profile photo preview
            composeTestRule.onNodeWithTag("tab_parent_PROFILE").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("btn_parent_preview_student_photo")
                .performScrollTo()
                .performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("dialog_student_photo_preview").assertIsDisplayed()
            composeTestRule.onNodeWithTag("box_student_photo_zoom_canvas").performTouchInput {
                val c = center
                pinch(
                    start0 = c - Offset(20f, 0f),
                    end0 = c - Offset(110f, 0f),
                    start1 = c + Offset(20f, 0f),
                    end1 = c + Offset(110f, 0f),
                    durationMillis = 250L
                )
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("badge_student_photo_zoom").assertIsDisplayed()
            composeTestRule.onNodeWithTag("btn_close_student_photo_preview").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("dialog_student_photo_preview").assertDoesNotExist()
        } finally {
            vm.logout()
            db.parentAccessLinkDao().delete(parentLink)
            db.childStudentDao().delete(student)
            photoFile.delete()
        }
    }
}
