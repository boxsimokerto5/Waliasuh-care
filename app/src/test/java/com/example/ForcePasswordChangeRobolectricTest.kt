package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.data.model.SchoolRegistration
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.screens.ForcePasswordChangeScreen
import com.example.util.PasswordGenerator
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ForcePasswordChangeRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private suspend fun waitUntil(maxLoops: Int = 40, condition: () -> Boolean) {
        var loops = 0
        while (loops < maxLoops && !condition()) {
            ShadowLooper.idleMainLooper()
            delay(40)
            loops++
        }
    }

    @Test
    fun `isSystemGeneratedPassword detects system formats and ignores custom personal passwords`() {
        repeat(20) {
            val generated = PasswordGenerator.generateRandomPassword()
            assertTrue(
                "Generated password '$generated' must be detected as system password",
                PasswordGenerator.isSystemGeneratedPassword(generated)
            )
        }
        assertTrue(PasswordGenerator.isSystemGeneratedPassword("Woyowoyo12@"))
        assertFalse(PasswordGenerator.isSystemGeneratedPassword("Pribadi99"))
        assertFalse(PasswordGenerator.isSystemGeneratedPassword("secret6"))
        assertFalse(PasswordGenerator.isSystemGeneratedPassword(""))
    }

    @Test
    fun `superadmin and anaksuperadmin are excluded from forced password change`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)

        assertTrue(vm.executeLogin("superadmin", "Woyowoyo12@") is LoginResult.SuccessSuperAdmin)
        ShadowLooper.idleMainLooper()
        assertFalse(vm.forcedPasswordChangeState.value.required)
        vm.logout()

        assertTrue(vm.executeLogin("anaksuperadmin", "Woyowoyo12@") is LoginResult.SuccessWali)
        ShadowLooper.idleMainLooper()
        assertFalse(vm.forcedPasswordChangeState.value.required)
        vm.logout()
    }

    @Test
    fun `existing Wali with system password is forced to change password with min 6 chars via UI`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)

        val wali = WaliAccount(
            id = 8401L,
            name = "Ustadz Farhan",
            username = "farhan_8401",
            password = "Wali#8401",
            dormitory = "Asrama Putra",
            schoolId = "SRT1-KDR"
        )
        db.waliAccountDao().insert(wali)

        assertTrue(vm.executeLogin("farhan_8401", "Wali#8401") is LoginResult.SuccessWali)
        waitUntil { vm.forcedPasswordChangeState.value.required }
        assertTrue(vm.forcedPasswordChangeState.value.required)

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            val state by vm.forcedPasswordChangeState.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalAppStrings provides strings) {
                if (state.required) {
                    ForcePasswordChangeScreen(
                        state = state,
                        viewModel = vm,
                        onLogout = { vm.logout() }
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("screen_force_password_change").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_force_save_password").assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_force_logout").assertHeightIsAtLeast(48.dp)

        // Short password (< 6 chars) must show error
        composeTestRule.onNodeWithTag("input_force_new_password").performTextInput("12345")
        composeTestRule.onNodeWithTag("input_force_confirm_password").performTextInput("12345")
        composeTestRule.onNodeWithTag("btn_force_save_password").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("banner_force_password_error").assertIsDisplayed()
        assertTrue(vm.forcedPasswordChangeState.value.required)

        // System-generated password format must also be rejected
        composeTestRule.onNodeWithTag("input_force_new_password").performTextReplacement("Asuh@1234")
        composeTestRule.onNodeWithTag("input_force_confirm_password").performTextReplacement("Asuh@1234")
        composeTestRule.onNodeWithTag("btn_force_save_password").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("banner_force_password_error").assertIsDisplayed()
        assertTrue(vm.forcedPasswordChangeState.value.required)

        // Valid 6+ char personal password succeeds and unlocks portal
        composeTestRule.onNodeWithTag("input_force_new_password").performTextReplacement("Aman99")
        composeTestRule.onNodeWithTag("input_force_confirm_password").performTextReplacement("Aman99")
        composeTestRule.onNodeWithTag("btn_force_save_password").performScrollTo().performClick()

        waitUntil { !vm.forcedPasswordChangeState.value.required }
        composeTestRule.waitForIdle()
        assertFalse(vm.forcedPasswordChangeState.value.required)
        composeTestRule.onNodeWithTag("screen_force_password_change").assertDoesNotExist()

        val updated = db.waliAccountDao().getByUsername("farhan_8401")
        assertEquals("Aman99", updated?.password)

        vm.logout()
        db.waliAccountDao().delete(updated!!)
    }

    @Test
    fun `Admin and Parent with system password must change password and unlock after update`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)

        val reg = SchoolRegistration(
            id = 8402L,
            schoolName = "SRT Test Force",
            address = "Kediri",
            city = "Kediri",
            estimatedWaliCount = 3,
            picName = "Pak Admin",
            picEmail = "admin@test.id",
            picPhone = "0812345678",
            status = "APPROVED",
            generatedSchoolCode = "SRT-FRC84",
            generatedAdminUsername = "admin_frc84",
            generatedAdminPassword = "SRT#8402"
        )
        db.schoolRegistrationDao().insert(reg)

        assertTrue(vm.executeLogin("admin_frc84", "SRT#8402") is LoginResult.SuccessAdmin)
        waitUntil { vm.forcedPasswordChangeState.value.required }
        assertTrue(vm.forcedPasswordChangeState.value.required)

        var adminDone = false
        vm.submitForcedPasswordChange("AdminKu99", "AdminKu99") { ok, _ ->
            adminDone = ok
        }
        waitUntil { adminDone && !vm.forcedPasswordChangeState.value.required }
        assertTrue(adminDone)
        assertFalse(vm.forcedPasswordChangeState.value.required)
        vm.logout()

        val student = ChildStudent(
            id = 8403L,
            name = "Budi Santoso",
            nis = "8403",
            nik = "3506840300010001",
            gender = "L",
            age = 12,
            dormitory = "Asrama Putra",
            classGrade = "VII-A",
            schoolId = "SRT-FRC84"
        )
        val link = ParentAccessLink(
            id = 8404L,
            studentId = 8403L,
            studentName = student.name,
            studentNis = student.nis,
            parentName = "Ibu Budi",
            username = "ortu_budi_8403",
            password = "Ceria!8404",
            schoolId = "SRT-FRC84"
        )
        db.childStudentDao().insert(student)
        db.parentAccessLinkDao().insert(link)

        assertTrue(vm.executeLogin("ortu_budi_8403", "Ceria!8404") is LoginResult.SuccessParent)
        waitUntil { vm.forcedPasswordChangeState.value.required }
        assertTrue(vm.forcedPasswordChangeState.value.required)

        var parentDone = false
        vm.submitForcedPasswordChange("OrtuAman1", "OrtuAman1") { ok, _ ->
            parentDone = ok
        }
        waitUntil { parentDone && !vm.forcedPasswordChangeState.value.required }
        assertTrue(parentDone)
        assertFalse(vm.forcedPasswordChangeState.value.required)

        vm.logout()
        db.parentAccessLinkDao().delete(db.parentAccessLinkDao().getByUsername("ortu_budi_8403")!!)
        db.childStudentDao().delete(student)
        db.schoolRegistrationDao().delete(db.schoolRegistrationDao().getBySchoolCode("SRT-FRC84")!!)
    }
}
