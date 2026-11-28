package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentMessage
import com.example.data.model.SholatRecord
import com.example.ui.dialogs.ParentNotificationSheet
import com.example.util.ParentNotifCategory
import com.example.util.ParentNotificationCenter
import com.example.util.ParentNotificationItem
import com.example.util.ParentStudentSnapshot
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
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
class ParentPortalNotificationRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun baseSnapshot(
        medical: List<MedicalRecord> = emptyList(),
        messages: List<ParentMessage> = emptyList(),
        sholat: List<SholatRecord> = emptyList(),
        link: ParentAccessLink? = ParentAccessLink(
            id = 1L,
            studentId = 101L,
            studentName = "Ahmad Fauzi",
            username = "ortu101",
            password = "123",
            canViewMedical = true,
            canViewSholat = true,
            canViewMengaji = true,
            canViewFasting = true,
            canViewTabungan = true
        )
    ): ParentStudentSnapshot {
        val student = ChildStudent(
            id = 101L,
            name = "Ahmad Fauzi",
            nis = "2026101",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra",
            classGrade = "7 SMP"
        )
        return ParentStudentSnapshot(
            schoolId = "SRT1-TEST",
            schoolShortName = "SRT1",
            studentId = 101L,
            studentName = "Ahmad Fauzi",
            student = student,
            link = link,
            medicalRecords = medical,
            mengajiRecords = emptyList(),
            sholatRecords = sholat,
            christianWorshipRecords = emptyList(),
            fastingRecords = emptyList(),
            haidRecords = emptyList(),
            tabunganTransactions = emptyList(),
            visitSessions = emptyList(),
            visitRecords = emptyList(),
            waliGalleryPosts = emptyList(),
            parentMessages = messages
        )
    }

    @Test
    fun `first login baseline seeds silently and only new updates trigger notifications`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        ParentNotificationCenter.resetBaseline(app, "SRT1-TEST", 101L)
        ParentNotificationCenter.clearHistory(app, "SRT1-TEST", 101L)

        val oldMedical = MedicalRecord(
            id = 10L,
            studentId = 101L,
            studentName = "Ahmad Fauzi",
            studentDormitory = "Asrama Putra",
            faskes = "Puskesmas Wates",
            complaint = "Demam lama",
            planDate = 1000L,
            status = "COMPLETED"
        )

        val firstRun = ParentNotificationCenter.evaluateAndNotify(
            context = app,
            snapshot = baseSnapshot(medical = listOf(oldMedical)),
            triggerSystemNotification = false
        )
        assertTrue(firstRun.isEmpty())

        val newMedical = oldMedical.copy(id = 11L, complaint = "Sakit kepala baru", planDate = 2000L, status = "PLANNED")
        val parentSentMsg = ParentMessage(
            id = 20L,
            studentId = 101L,
            senderRole = "PARENT",
            senderName = "Ibu Fauzi",
            messageContent = "Terima kasih",
            createdAt = 2100L
        )
        val waliSentMsg = ParentMessage(
            id = 21L,
            studentId = 101L,
            senderRole = "WALI",
            senderName = "Pak Budi",
            messageContent = "Ananda sudah minum obat",
            createdAt = 2200L
        )

        val secondRun = ParentNotificationCenter.evaluateAndNotify(
            context = app,
            snapshot = baseSnapshot(
                medical = listOf(oldMedical, newMedical),
                messages = listOf(parentSentMsg, waliSentMsg)
            ),
            triggerSystemNotification = false
        )
        assertEquals(2, secondRun.size)
        assertTrue(secondRun.any { it.category == ParentNotifCategory.MESSAGE && it.bodyId.contains("minum obat") })
        assertTrue(secondRun.any { it.category == ParentNotifCategory.MEDICAL && it.bodyId.contains("Sakit kepala baru") })
        assertFalse(secondRun.any { it.bodyId.contains("Terima kasih") })
        assertTrue(secondRun.all { !it.isRead })

        val msgKey = secondRun.first { it.category == ParentNotifCategory.MESSAGE }.key
        val afterMarkOne = ParentNotificationCenter.markAsRead(app, "SRT1-TEST", 101L, msgKey)
        assertEquals(1, afterMarkOne.count { !it.isRead })

        val afterMarkAll = ParentNotificationCenter.markAllAsRead(app, "SRT1-TEST", 101L)
        assertEquals(0, afterMarkAll.count { !it.isRead })

        val afterClear = ParentNotificationCenter.clearHistory(app, "SRT1-TEST", 101L)
        assertTrue(afterClear.isEmpty())

        // Re-evaluating the same snapshot after clearing must not re-fire already-known items
        val thirdRun = ParentNotificationCenter.evaluateAndNotify(
            context = app,
            snapshot = baseSnapshot(
                medical = listOf(oldMedical, newMedical),
                messages = listOf(parentSentMsg, waliSentMsg)
            ),
            triggerSystemNotification = false
        )
        assertTrue(thirdRun.isEmpty())
    }

    @Test
    fun `ParentNotificationSheet renders updates and triggers read clear permission and toggle callbacks`() {
        var clickedItem: ParentNotificationItem? = null
        var markedAll = false
        var cleared = false
        var permissionRequested = false
        var bgToggled: Boolean? = null

        val items = listOf(
            ParentNotificationItem(
                key = "MED:11:PLANNED:false",
                category = ParentNotifCategory.MEDICAL,
                titleId = "Rencana Berobat Baru • Ahmad Fauzi",
                titleEn = "New Medical Plan • Ahmad Fauzi",
                bodyId = "Demam ringan • Puskesmas Wates",
                bodyEn = "Mild fever • Puskesmas Wates",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides AppLanguageManager.getStrings(AppLanguage.ID)) {
                ParentNotificationSheet(
                    notifications = items,
                    unreadCount = 1,
                    backgroundAlertsEnabled = true,
                    hasSystemNotificationPermission = false,
                    onRequestSystemNotificationPermission = { permissionRequested = true },
                    onToggleBackgroundAlerts = { bgToggled = it },
                    onNotificationClick = { clickedItem = it },
                    onMarkAllRead = { markedAll = true },
                    onClearAll = { cleared = true },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sheet_parent_notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rencana Berobat Baru • Ahmad Fauzi").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_parent_enable_notif_perm").performClick()
        assertTrue(permissionRequested)

        composeTestRule.onNodeWithTag("switch_parent_bg_notifs").performClick()
        assertEquals(false, bgToggled)

        composeTestRule.onNodeWithTag("btn_parent_mark_all_notifs_read").performClick()
        assertTrue(markedAll)

        composeTestRule.onNodeWithTag("btn_parent_clear_notifs").performClick()
        assertTrue(cleared)

        composeTestRule.onNodeWithTag("card_parent_notif_medical").performClick()
        assertEquals("MED:11:PLANNED:false", clickedItem?.key)
    }
}
