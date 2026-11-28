package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.VisitConfirmation
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.ui.components.ParentMenuPdfHeaderCard
import com.example.ui.components.ParentReadyPdfDialogData
import com.example.util.ParentPortalPdfHelper
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date

private fun isVisitSessionCompleted(session: VisitSession, todayStartMillis: Long): Boolean {
    if (!session.isActive) return true
    val dates = session.getVisitDatesList()
    if (dates.isEmpty()) return false
    val latestDayStart = dates.maxOf { VisitHelper.normalizeDayStart(it) }
    return latestDayStart < todayStartMillis
}

@Composable
fun ParentVisitContent(
    student: ChildStudent?,
    sessions: List<VisitSession>,
    confirmations: List<VisitConfirmation>,
    visitRecords: List<VisitRecord>,
    allVisitSessions: List<VisitSession> = sessions,
    visitSessionSwaps: List<VisitSessionSwap> = emptyList(),
    defaultParentName: String = "",
    schoolProfile: SchoolProfile = SchoolProfile(),
    waliName: String = "Wali Asuh",
    onSaveConfirmation: (
        session: VisitSession,
        student: ChildStudent,
        visitDate: Long,
        visitorName: String,
        visitorRelation: String,
        attendanceStatus: String,
        estimatedArrivalTime: String,
        notes: String
    ) -> Unit,
    onPdfReady: ((ParentReadyPdfDialogData) -> Unit)? = null
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val locale = s.language.locale

    val tealPrimary = Color(0xFF0F766E)
    val tealLight = Color(0xFFF0FDFA)
    val tealBorder = Color(0xFF99F6E4)

    val studentDistrictKey = remember(student) {
        student?.let { VisitHelper.resolveStudentDistrictKey(it) } ?: VisitHelper.UNASSIGNED_DISTRICT
    }
    val studentDistrictLabel = remember(studentDistrictKey, isEn) {
        VisitHelper.displayDistrict(studentDistrictKey, isEn)
    }

    val todayStartMillis = remember { VisitHelper.normalizeDayStart(System.currentTimeMillis()) }

    val matchedSessions = remember(sessions, allVisitSessions, visitSessionSwaps, student) {
        if (student == null) emptyList()
        else {
            val allSess = allVisitSessions.ifEmpty { sessions }
            sessions
                .filter {
                    it.publishedToParent &&
                        VisitHelper.isStudentEffectiveInSession(it, student, visitSessionSwaps, allSess)
                }
        }
    }

    val upcomingSessions = remember(matchedSessions, todayStartMillis) {
        matchedSessions
            .filterNot { isVisitSessionCompleted(it, todayStartMillis) }
            .sortedWith(
                compareBy<VisitSession> { it.getVisitDatesList().firstOrNull() ?: Long.MAX_VALUE }
                    .thenByDescending { it.createdAt }
            )
    }

    val completedSessions = remember(matchedSessions, todayStartMillis) {
        matchedSessions
            .filter { isVisitSessionCompleted(it, todayStartMillis) }
            .sortedWith(
                compareByDescending<VisitSession> { it.getVisitDatesList().maxOrNull() ?: 0L }
                    .thenByDescending { it.createdAt }
            )
    }

    val dateFmt = remember(locale) { SimpleDateFormat("EEEE, d MMMM yyyy", locale) }
    val compactDateFmt = remember(locale) { SimpleDateFormat("EEE, d MMM yyyy", locale) }
    val timeFmt = remember(locale) { SimpleDateFormat("HH:mm", locale) }

    var selectedSessionId by remember { mutableStateOf<Long?>(null) }
    val selectedSession = remember(selectedSessionId, matchedSessions) {
        selectedSessionId?.let { id -> matchedSessions.firstOrNull { it.id == id } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_visit_matched_sessions_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "visit_pdf_header") {
            ParentMenuPdfHeaderCard(
                title = if (isEn) "Parent Visit Schedule & Pass" else "Jadwal Kunjungan & Tiket Barcode",
                subtitle = if (isEn) {
                    "Download official visit schedule & QR entry pass PDF"
                } else {
                    "Unduh jadwal resmi kunjungan & kartu barcode hadir (PDF)"
                },
                accentColor = tealPrimary,
                containerColor = tealLight,
                borderColor = tealBorder,
                isEn = isEn,
                buttonLabel = if (isEn) "Visit Pass PDF" else "PDF Tiket",
                pdfButtonTestTag = "btn_parent_visit_pdf",
                onDownloadPdf = {
                    val target = student
                    if (target == null) {
                        Toast.makeText(
                            context,
                            if (isEn) "Student profile not available yet." else "Data ananda belum termuat.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val orderedForPdf = upcomingSessions + completedSessions
                        val file = ParentPortalPdfHelper.generateStudentVisitPassPdf(
                            context = context,
                            student = target,
                            matchedSessions = orderedForPdf,
                            confirmations = confirmations,
                            visitRecords = visitRecords,
                            waliName = waliName,
                            schoolProfile = schoolProfile
                        )
                        if (file != null) {
                            onPdfReady?.invoke(
                                ParentReadyPdfDialogData(
                                    file = file,
                                    title = if (isEn) "Visit Pass PDF Ready" else "PDF Jadwal & Tiket Kunjungan Siap",
                                    description = if (isEn) {
                                        "Open or share the visit notification and QR pass for ${target.name}."
                                    } else {
                                        "Buka PDF untuk melihat atau mencetak tiket barcode kunjungan ${target.name}."
                                    },
                                    shareSubject = if (isEn) "Visit Pass: ${target.name}" else "Tiket Kunjungan: ${target.name}",
                                    shareText = if (isEn) {
                                        "Attached parent visit schedule & barcode pass for ${target.name}."
                                    } else {
                                        "Terlampir jadwal dan kartu barcode kunjungan untuk ${target.name}."
                                    }
                                )
                            )
                        } else {
                            Toast.makeText(
                                context,
                                if (isEn) "Unable to create visit PDF" else "Gagal membuat PDF kunjungan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }

        item(key = "visit_domicile_banner") {
            ParentVisitDomicileCompactCard(
                studentDistrictKey = studentDistrictKey,
                studentDistrictLabel = studentDistrictLabel,
                tealPrimary = tealPrimary,
                isEn = isEn
            )
        }

        if (matchedSessions.isEmpty()) {
            item(key = "visit_empty") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_parent_visit_empty"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = tealPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (isEn) "No Visit Session Scheduled Yet" else "Belum Ada Jadwal Kunjungan untuk Kecamatan Anda",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isEn) {
                                "When the school admin opens a visit session for $studentDistrictLabel, the definite date and confirmation form will appear automatically."
                            } else {
                                "Ketika Admin menjadwalkan sesi kunjungan untuk wilayah $studentDistrictLabel, tanggal pasti kunjungan dan tombol konfirmasi akan otomatis muncul di sini."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item(key = "header_upcoming") {
                ParentVisitSectionHeader(
                    title = if (isEn) "Upcoming Visits" else "Jadwal Mendatang",
                    count = upcomingSessions.size,
                    icon = Icons.Default.EventAvailable,
                    accentColor = tealPrimary,
                    isEn = isEn,
                    modifier = Modifier.testTag("header_parent_visit_upcoming")
                )
            }

            if (upcomingSessions.isEmpty()) {
                item(key = "upcoming_empty") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_parent_visit_upcoming_empty"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "No upcoming visit schedules right now."
                                } else {
                                    "Belum ada jadwal kunjungan mendatang saat ini."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(upcomingSessions, key = { "upcoming_${it.id}" }) { sess ->
                    val targetStudent = student ?: return@items
                    val sessionDate = sess.getVisitDatesList().firstOrNull() ?: 0L
                    val existingConf = VisitHelper.findConfirmationForStudent(
                        confirmations = confirmations,
                        sessionId = sess.id,
                        student = targetStudent,
                        targetDayStart = if (sessionDate > 0L) sessionDate else null
                    )
                    val checkInRec = visitRecords
                        .filter { it.sessionId == sess.id }
                        .let { recs ->
                            VisitHelper.findVisitForStudent(
                                recs,
                                targetStudent,
                                if (sessionDate > 0L) sessionDate else null
                            )
                        }
                    val incomingSwap = remember(sess.id, targetStudent.id, visitSessionSwaps) {
                        VisitHelper.findIncomingSwapToSession(sess.id, targetStudent, visitSessionSwaps)
                    }

                    ParentVisitSessionListCard(
                        session = sess,
                        sessionDates = sess.getVisitDatesList(),
                        studentDistrictKey = studentDistrictKey,
                        existingConf = existingConf,
                        checkInRec = checkInRec,
                        incomingSwap = incomingSwap,
                        isCompletedHistory = false,
                        tealPrimary = tealPrimary,
                        isEn = isEn,
                        compactDateFmt = compactDateFmt,
                        onClick = { selectedSessionId = sess.id }
                    )
                }
            }

            if (completedSessions.isNotEmpty()) {
                item(key = "header_completed") {
                    Spacer(modifier = Modifier.height(4.dp))
                    ParentVisitSectionHeader(
                        title = if (isEn) "Completed History" else "Riwayat Selesai",
                        count = completedSessions.size,
                        icon = Icons.Default.History,
                        accentColor = Color(0xFF475569),
                        isEn = isEn,
                        modifier = Modifier.testTag("header_parent_visit_completed")
                    )
                }

                items(completedSessions, key = { "completed_${it.id}" }) { sess ->
                    val targetStudent = student ?: return@items
                    val sessionDate = sess.getVisitDatesList().firstOrNull() ?: 0L
                    val existingConf = VisitHelper.findConfirmationForStudent(
                        confirmations = confirmations,
                        sessionId = sess.id,
                        student = targetStudent,
                        targetDayStart = if (sessionDate > 0L) sessionDate else null
                    )
                    val checkInRec = visitRecords
                        .filter { it.sessionId == sess.id }
                        .let { recs ->
                            VisitHelper.findVisitForStudent(
                                recs,
                                targetStudent,
                                if (sessionDate > 0L) sessionDate else null
                            )
                        }
                    val incomingSwap = remember(sess.id, targetStudent.id, visitSessionSwaps) {
                        VisitHelper.findIncomingSwapToSession(sess.id, targetStudent, visitSessionSwaps)
                    }

                    ParentVisitSessionListCard(
                        session = sess,
                        sessionDates = sess.getVisitDatesList(),
                        studentDistrictKey = studentDistrictKey,
                        existingConf = existingConf,
                        checkInRec = checkInRec,
                        incomingSwap = incomingSwap,
                        isCompletedHistory = true,
                        tealPrimary = tealPrimary,
                        isEn = isEn,
                        compactDateFmt = compactDateFmt,
                        onClick = { selectedSessionId = sess.id }
                    )
                }
            }
        }
    }

    val activeSheetSession = selectedSession
    val targetStudent = student
    if (activeSheetSession != null && targetStudent != null) {
        val sessionDate = activeSheetSession.getVisitDatesList().firstOrNull() ?: 0L
        val existingConf = VisitHelper.findConfirmationForStudent(
            confirmations = confirmations,
            sessionId = activeSheetSession.id,
            student = targetStudent,
            targetDayStart = if (sessionDate > 0L) sessionDate else null
        )
        val checkInRec = visitRecords
            .filter { it.sessionId == activeSheetSession.id }
            .let { recs ->
                VisitHelper.findVisitForStudent(
                    recs,
                    targetStudent,
                    if (sessionDate > 0L) sessionDate else null
                )
            }
        val incomingSwap = remember(activeSheetSession.id, targetStudent.id, visitSessionSwaps) {
            VisitHelper.findIncomingSwapToSession(activeSheetSession.id, targetStudent, visitSessionSwaps)
        }
        val isCompleted = isVisitSessionCompleted(activeSheetSession, todayStartMillis)

        ParentVisitDetailBottomSheet(
            session = activeSheetSession,
            student = targetStudent,
            studentDistrictKey = studentDistrictKey,
            existingConf = existingConf,
            checkInRec = checkInRec,
            incomingSwap = incomingSwap,
            defaultParentName = defaultParentName,
            isCompletedHistory = isCompleted,
            isEn = isEn,
            dateFmt = dateFmt,
            timeFmt = timeFmt,
            onDismiss = { selectedSessionId = null },
            onSaveConfirmation = onSaveConfirmation
        )
    }
}

@Composable
private fun ParentVisitDomicileCompactCard(
    studentDistrictKey: String,
    studentDistrictLabel: String,
    tealPrimary: Color,
    isEn: Boolean
) {
    val isUnassigned = studentDistrictKey == VisitHelper.UNASSIGNED_DISTRICT
    val accent = if (isUnassigned) Color(0xFFB45309) else tealPrimary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_parent_visit_district"),
        shape = RoundedCornerShape(14.dp),
        color = if (isUnassigned) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isUnassigned) Color(0xFFFCD34D) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accent.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isEn) "Domicile:" else "Domisili:",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = studentDistrictLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (isUnassigned) {
                        if (isEn) {
                            "District not set yet. Contact Wali Asuh / Admin."
                        } else {
                            "Kecamatan belum diisi. Hubungi Admin/Wali Asuh."
                        }
                    } else {
                        if (isEn) {
                            "Sessions matching $studentDistrictLabel appear automatically"
                        } else {
                            "Jadwal wilayah Kec. $studentDistrictLabel otomatis masuk ke daftar"
                        }
                    },
                    fontSize = 11.sp,
                    color = if (isUnassigned) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ParentVisitSectionHeader(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = accentColor.copy(alpha = 0.12f)
        ) {
            Text(
                text = if (isEn) "$count sessions" else "$count sesi",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParentVisitSessionListCard(
    session: VisitSession,
    sessionDates: List<Long>,
    studentDistrictKey: String,
    existingConf: VisitConfirmation?,
    checkInRec: VisitRecord?,
    incomingSwap: VisitSessionSwap?,
    isCompletedHistory: Boolean,
    tealPrimary: Color,
    isEn: Boolean,
    compactDateFmt: SimpleDateFormat,
    onClick: () -> Unit
) {
    val accentColor = when {
        checkInRec != null -> Color(0xFF16A34A)
        existingConf?.isHadir == true -> tealPrimary
        existingConf != null -> Color(0xFFD97706)
        !isCompletedHistory -> Color(0xFF0369A1)
        else -> Color(0xFF94A3B8)
    }

    val statusBgColor = when {
        checkInRec != null -> Color(0xFFDCFCE7)
        existingConf?.isHadir == true -> tealPrimary.copy(alpha = 0.14f)
        existingConf != null -> Color(0xFFFEF3C7)
        !isCompletedHistory -> Color(0xFFE0F2FE)
        else -> Color(0xFFF1F5F9)
    }

    val statusTextColor = when {
        checkInRec != null -> Color(0xFF15803D)
        existingConf?.isHadir == true -> tealPrimary
        existingConf != null -> Color(0xFFB45309)
        !isCompletedHistory -> Color(0xFF0369A1)
        else -> Color(0xFF64748B)
    }

    val statusLabel = when {
        checkInRec != null -> if (isEn) "Checked In" else "Sudah Hadir"
        existingConf?.isHadir == true -> if (isEn) "Confirmed" else "Terkonfirmasi"
        existingConf != null -> if (isEn) "Absent" else "Berhalangan"
        !isCompletedHistory -> if (isEn) "Scheduled" else "Terjadwal"
        else -> if (isEn) "Completed" else "Selesai"
    }

    val firstDateText = sessionDates.firstOrNull()?.let { compactDateFmt.format(Date(it)) } ?: "-"
    val extraDatesCount = (sessionDates.size - 1).coerceAtLeast(0)
    val dateSummary = if (extraDatesCount > 0) {
        "$firstDateText (+$extraDatesCount)"
    } else {
        firstDateText
    }

    val districts = session.getDistrictsList()
    val hasMyDistrict = districts.any { it.equals(studentDistrictKey, ignoreCase = true) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("card_parent_visit_session_${session.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompletedHistory) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (!isCompletedHistory && existingConf == null) 1.4.dp else 1.dp,
            color = if (isCompletedHistory) Color(0xFFE2E8F0) else accentColor.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (checkInRec != null) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = session.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBgColor
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = Color(0xFF0369A1),
                            modifier = Modifier.size(13.5.dp)
                        )
                        Text(
                            text = dateSummary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0369A1)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = tealPrimary,
                            modifier = Modifier.size(13.5.dp)
                        )
                        Text(
                            text = session.displayTimeRange(),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = tealPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (session.updatedNoticeAfterParentLaunch) {
                            MiniBadgeChip(
                                text = if (isEn) "Updated" else "Diperbarui",
                                bgColor = Color(0xFFFEF3C7),
                                textColor = Color(0xFF92400E)
                            )
                        }
                        if (incomingSwap != null) {
                            MiniBadgeChip(
                                text = if (isEn) "Moved" else "Jadwal Dipindah",
                                icon = Icons.Default.SwapHoriz,
                                bgColor = Color(0xFFEEF2FF),
                                textColor = Color(0xFF4338CA)
                            )
                        }
                        if (existingConf?.isHadir == true) {
                            MiniBadgeChip(
                                text = if (isEn) "QR Ready" else "Barcode Siap",
                                icon = Icons.Default.QrCode2,
                                bgColor = tealPrimary.copy(alpha = 0.12f),
                                textColor = tealPrimary
                            )
                        } else if (!isCompletedHistory && existingConf == null) {
                            MiniBadgeChip(
                                text = if (isEn) "Tap to confirm" else "Perlu konfirmasi",
                                bgColor = Color(0xFFE0F2FE),
                                textColor = Color(0xFF0369A1)
                            )
                        }
                        if (hasMyDistrict) {
                            MiniBadgeChip(
                                text = if (districts.size > 1) {
                                    "${VisitHelper.displayDistrict(studentDistrictKey, isEn)} (+${districts.size - 1})"
                                } else {
                                    VisitHelper.displayDistrict(studentDistrictKey, isEn)
                                },
                                bgColor = Color(0xFFF1F5F9),
                                textColor = Color(0xFF475569)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = if (isEn) "Detail" else "Detail",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = if (isEn) "Open visit detail" else "Buka detail kunjungan",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniBadgeChip(
    text: String,
    bgColor: Color,
    textColor: Color,
    icon: ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(11.dp)
                )
            }
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
