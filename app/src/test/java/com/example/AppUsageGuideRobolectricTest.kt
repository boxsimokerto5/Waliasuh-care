package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import com.example.data.model.SchoolProfile
import com.example.ui.UserRole
import com.example.ui.dialogs.AppUsageGuideSheet
import com.example.ui.dialogs.GuideRoleFilter
import com.example.ui.dialogs.buildGuideSections
import com.example.ui.theme.MyApplicationTheme
import com.example.util.i18n.IdStrings
import com.example.util.i18n.LocalAppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppUsageGuideRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `guide data covers all major modules in both languages`() {
        val sections = buildGuideSections("UKS Sekolah / Puskesmas", "School Clinic / Health Center")
        assertEquals(8, sections.size)
        val ids = sections.map { it.id }.toSet()
        assertTrue(ids.containsAll(setOf(
            "kesehatan",
            "anak_asuh",
            "kunjungan",
            "ibadah_islam",
            "agama_kristen",
            "pendampingan",
            "komunikasi_catatan",
            "portal_profil"
        )))
        assertTrue(sections.all { it.stepsId.isNotEmpty() && it.stepsEn.isNotEmpty() })
        assertTrue(sections.first { it.id == "agama_kristen" }.matches("kristen", GuideRoleFilter.PARENT, false))
        assertTrue(sections.first { it.id == "ibadah_islam" }.matches("qur'an", GuideRoleFilter.GURU_NGAJI, false))
    }

    @Test
    fun `guide sheet filters by role, searches features, and keeps 48dp targets`() {
        var tourStarted = false
        var dismissed = false

        composeRule.setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalAppStrings provides IdStrings) {
                    AppUsageGuideSheet(
                        schoolProfile = SchoolProfile(schoolName = "Sekolah Rakyat Kediri"),
                        currentRole = UserRole.WALI,
                        onDismiss = { dismissed = true },
                        onStartInteractiveTour = { tourStarted = true }
                    )
                }
            }
        }

        composeRule.onNodeWithText("Panduan Lengkap Aplikasi").assertIsDisplayed()
        composeRule.onNodeWithTag("guide_role_chip_all").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("btn_guide_start_tour").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("btn_guide_close").assertHeightIsAtLeast(48.dp)

        // Filter by Guru Ngaji
        composeRule.onNodeWithTag("guide_role_chip_guru_ngaji").performClick()
        composeRule.onNodeWithTag("guide_section_ibadah_islam").performScrollTo().assertIsDisplayed()

        // Search for Kristen after resetting to All
        composeRule.onNodeWithTag("guide_role_chip_all").performClick()
        composeRule.onNodeWithTag("guide_search_input").performTextInput("Kristen")
        composeRule.onNodeWithTag("guide_section_agama_kristen").performScrollTo().assertIsDisplayed()

        // Trigger tour action
        composeRule.onNodeWithTag("btn_guide_start_tour").performClick()
        assertTrue(dismissed)
        assertTrue(tourStarted)
    }
}
