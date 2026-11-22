package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.util.StudentCardRenderer
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
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

    val matchedSessions = remember(sessions, allVisitSessions, visitSessionSwaps, student) {
        if (student == null) emptyList()
        else {
            val allSess = allVisitSessions.ifEmpty { sessions }
            sessions
                .filter {
                    it.publishedToParent &&
                        VisitHelper.isStudentEffectiveInSession(it, student, visitSessionSwaps, allSess)
                }
                .sortedWith(
                    compareByDescending<VisitSession> { it.isActive }
                        .thenBy { it.getVisitDatesList().firstOrNull() ?: Long.MAX_VALUE }
                        .thenByDescending { it.createdAt }
                )
        }
    }

    val dateFmt = remember(locale) { SimpleDateFormat("EEEE, d MMMM yyyy", locale) }
    val timeFmt = remember(locale) { SimpleDateFormat("HH:mm", locale) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_visit_matched_sessions_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
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
                        val file = ParentPortalPdfHelper.generateStudentVisitPassPdf(
                            context = context,
                            student = target,
                            matchedSessions = matchedSessions,
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

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_parent_visit_district"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tealPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = tealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Registered Home District (Kecamatan)" else "Kecamatan Domisili Anak Asuh",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = studentDistrictLabel,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (studentDistrictKey == VisitHelper.UNASSIGNED_DISTRICT) {
                                Color(0xFFB45309)
                            } else {
                                tealPrimary
                            }
                        )
                        Text(
                            text = if (studentDistrictKey == VisitHelper.UNASSIGNED_DISTRICT) {
                                if (isEn) {
                                    "Your child's district is not filled in yet. Contact Wali Asuh / Admin so automatic visit dates appear."
                                } else {
                                    "Data kecamatan ananda belum diisi Admin/Wali Asuh. Hubungi pembina agar jadwal otomatis muncul."
                                }
                            } else {
                                if (isEn) {
                                    "Sessions selecting $studentDistrictLabel automatically notify and appear here."
                                } else {
                                    "Sesi kunjungan yang memilih Kec. $studentDistrictLabel otomatis tampil sebagai pemberitahuan jadwal Anda."
                                }
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        if (matchedSessions.isEmpty()) {
            item {
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
            items(matchedSessions, key = { it.id }) { sess ->
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

                var isEditing by remember(sess.id, existingConf?.updatedAt) {
                    mutableStateOf(existingConf == null)
                }
                var statusChoice by remember(sess.id, existingConf?.attendanceStatus) {
                    mutableStateOf(existingConf?.attendanceStatus ?: VisitConfirmation.STATUS_HADIR)
                }
                var relationChoice by remember(sess.id, existingConf?.visitorRelation) {
                    mutableStateOf(existingConf?.visitorRelation?.ifBlank { "Ibu" } ?: "Ibu")
                }
                var visitorNameInput by remember(sess.id, existingConf?.visitorName, defaultParentName) {
                    mutableStateOf(existingConf?.visitorName?.ifBlank { defaultParentName } ?: defaultParentName)
                }
                var etaInput by remember(sess.id, existingConf?.estimatedArrivalTime) {
                    mutableStateOf(existingConf?.estimatedArrivalTime.orEmpty())
                }
                var notesInput by remember(sess.id, existingConf?.notes) {
                    mutableStateOf(existingConf?.notes.orEmpty())
                }

                val isHadirSelected = statusChoice.equals(VisitConfirmation.STATUS_HADIR, ignoreCase = true)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_parent_visit_session_${sess.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = if (sess.isActive) 1.5.dp else 1.dp,
                        color = when {
                            checkInRec != null -> Color(0xFF16A34A)
                            existingConf?.isHadir == true -> tealPrimary
                            existingConf != null -> Color(0xFFD97706)
                            else -> Color(0xFFCBD5E1)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = tealPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = sess.title,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (sess.updatedNoticeAfterParentLaunch) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                        ) {
                                            Text(
                                                text = if (isEn) "Updated" else "Diperbarui",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EventAvailable,
                                        contentDescription = null,
                                        tint = Color(0xFF0369A1),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (sessionDate > 0L) dateFmt.format(Date(sessionDate)) else "-",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = tealPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isEn) {
                                            "Visiting Hours: ${sess.displayTimeRange()}"
                                        } else {
                                            "Jam Kunjungan: ${sess.displayTimeRange()}"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = tealPrimary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    checkInRec != null -> Color(0xFFDCFCE7)
                                    existingConf?.isHadir == true -> tealPrimary.copy(alpha = 0.14f)
                                    existingConf != null -> Color(0xFFFEF3C7)
                                    sess.isActive -> Color(0xFFE0F2FE)
                                    else -> Color(0xFFF1F5F9)
                                }
                            ) {
                                Text(
                                    text = when {
                                        checkInRec != null -> if (isEn) "Checked In" else "Sudah Hadir"
                                        existingConf?.isHadir == true -> if (isEn) "Confirmed" else "Terkonfirmasi"
                                        existingConf != null -> if (isEn) "Absent" else "Berhalangan"
                                        sess.isActive -> if (isEn) "Scheduled" else "Terjadwal"
                                        else -> if (isEn) "Closed" else "Selesai"
                                    },
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        checkInRec != null -> Color(0xFF15803D)
                                        existingConf?.isHadir == true -> tealPrimary
                                        existingConf != null -> Color(0xFFB45309)
                                        sess.isActive -> Color(0xFF0369A1)
                                        else -> Color(0xFF64748B)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (sess.description.isNotBlank()) {
                            Text(
                                text = sess.description,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sess.getDistrictsList().forEach { dist ->
                                val isMine = dist.equals(studentDistrictKey, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isMine) tealPrimary.copy(alpha = 0.14f) else Color(0xFFF1F5F9),
                                    border = if (isMine) BorderStroke(1.dp, tealPrimary) else null
                                ) {
                                    Text(
                                        text = VisitHelper.displayDistrict(dist, isEn),
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isMine) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isMine) tealPrimary else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        val incomingSwap = remember(sess.id, targetStudent.id, visitSessionSwaps) {
                            VisitHelper.findIncomingSwapToSession(sess.id, targetStudent, visitSessionSwaps)
                        }
                        if (incomingSwap != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("banner_parent_visit_swapped_${sess.id}"),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEEF2FF),
                                border = BorderStroke(1.dp, Color(0xFFA5B4FC))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = Color(0xFF4338CA),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = if (isEn) {
                                                "Visit Schedule Moved to This Session"
                                            } else {
                                                "Jadwal Kunjungan Dipindahkan ke Sesi Ini"
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF3730A3)
                                        )
                                        val originLabel = incomingSwap.fromSessionTitle.ifBlank {
                                            if (isEn) "previous session" else "sesi sebelumnya"
                                        }
                                        Text(
                                            text = if (isEn) {
                                                "Adjusted by Wali Asuh from \"$originLabel\"."
                                            } else {
                                                "Disesuaikan oleh Wali Asuh dari sesi \"$originLabel\"."
                                            },
                                            fontSize = 11.sp,
                                            color = Color(0xFF312E81)
                                        )
                                        if (incomingSwap.reason.isNotBlank()) {
                                            Text(
                                                text = (if (isEn) "Reason: " else "Alasan: ") + incomingSwap.reason,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF4338CA)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (checkInRec != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("badge_parent_visit_checkedin_${sess.id}"),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF15803D),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEn) {
                                            "Arrival checked in at ${timeFmt.format(Date(checkInRec.checkedInAt))} (${checkInRec.visitorRelation})"
                                        } else {
                                            "Sudah check-in di asrama pukul ${timeFmt.format(Date(checkInRec.checkedInAt))} (${checkInRec.visitorRelation})"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }

                        if (existingConf != null && !isEditing) {
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            if (existingConf.isHadir) {
                                val qrPayload = existingConf.qrPayload()
                                val qrBitmap = remember(qrPayload) {
                                    StudentCardRenderer.generateQrBitmap(qrPayload, 512)?.asImageBitmap()
                                }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_parent_visit_barcode_${sess.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = tealLight,
                                    border = BorderStroke(1.dp, tealBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode2,
                                                contentDescription = null,
                                                tint = tealPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isEn) "Official Visit QR Pass" else "Barcode Kehadiran Kunjungan",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = tealPrimary
                                            )
                                        }
                                        if (qrBitmap != null) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                            ) {
                                                Image(
                                                    bitmap = qrBitmap,
                                                    contentDescription = if (isEn) "Visit Barcode QR" else "Barcode QR Kunjungan",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier
                                                        .size(176.dp)
                                                        .padding(10.dp)
                                                        .testTag("img_parent_visit_qrcode_${sess.id}")
                                                )
                                            }
                                        }
                                        Text(
                                            text = existingConf.barcodeToken,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        val visitorSummary = listOfNotNull(
                                            existingConf.visitorRelation.takeIf { it.isNotBlank() },
                                            existingConf.visitorName.takeIf { it.isNotBlank() }
                                        ).joinToString(" — ")
                                        if (visitorSummary.isNotBlank()) {
                                            Text(
                                                text = (if (isEn) "Visitor: " else "Pengunjung: ") + visitorSummary,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E293B),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        if (existingConf.estimatedArrivalTime.isNotBlank()) {
                                            Text(
                                                text = (if (isEn) "Est. Arrival: " else "Perkiraan Tiba: ") + existingConf.estimatedArrivalTime,
                                                fontSize = 11.sp,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                        Text(
                                            text = if (isEn) {
                                                "Show this QR code to Wali Asuh upon arrival for fast check-in."
                                            } else {
                                                "Tunjukkan QR Code ini ke Wali Asuh saat tiba di sekolah untuk scan daftar hadir."
                                            },
                                            fontSize = 11.sp,
                                            color = tealPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_parent_visit_absent_${sess.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFFBEB),
                                    border = BorderStroke(1.dp, Color(0xFFFCD34D))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EventBusy,
                                            contentDescription = null,
                                            tint = Color(0xFFB45309),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (isEn) "Marked Unable to Attend" else "Tercatat Berhalangan Hadir",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E)
                                            )
                                            if (existingConf.notes.isNotBlank()) {
                                                Text(
                                                    text = existingConf.notes,
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF78350F)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (sess.isActive) {
                                OutlinedButton(
                                    onClick = { isEditing = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_parent_visit_edit_${sess.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "Update Confirmation" else "Ubah Konfirmasi Kehadiran",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else if (sess.isActive || existingConf == null) {
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Text(
                                text = if (isEn) "Visit Attendance Confirmation" else "Konfirmasi Kehadiran Kunjungan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = isHadirSelected,
                                    onClick = { statusChoice = VisitConfirmation.STATUS_HADIR },
                                    label = {
                                        Text(
                                            text = if (isEn) "Attending" else "Akan Hadir",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tealPrimary.copy(alpha = 0.16f),
                                        selectedLabelColor = tealPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_parent_visit_hadir_${sess.id}")
                                )
                                FilterChip(
                                    selected = !isHadirSelected,
                                    onClick = { statusChoice = VisitConfirmation.STATUS_BERHALANGAN },
                                    label = {
                                        Text(
                                            text = if (isEn) "Unable to Attend" else "Berhalangan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFEF3C7),
                                        selectedLabelColor = Color(0xFFB45309)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_parent_visit_berhalangan_${sess.id}")
                                )
                            }

                            if (isHadirSelected) {
                                Text(
                                    text = if (isEn) "Visitor Relation" else "Hubungan Pengunjung",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val relationOptions = listOf("Ibu", "Ayah", "Wali / Keluarga")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    relationOptions.forEach { rel ->
                                        FilterChip(
                                            selected = relationChoice == rel,
                                            onClick = { relationChoice = rel },
                                            label = { Text(rel, fontSize = 11.5.sp) },
                                            modifier = Modifier.testTag("chip_parent_visit_rel_${sess.id}_${rel.replace(" ", "_").replace("/", "_")}")
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = visitorNameInput,
                                    onValueChange = { visitorNameInput = it },
                                    label = {
                                        Text(if (isEn) "Visitor Name" else "Nama Pengunjung")
                                    },
                                    placeholder = {
                                        Text(if (isEn) "e.g. Mrs. Siti Aminah" else "Contoh: Ibu Siti Aminah")
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_parent_visit_visitor_name_${sess.id}")
                                )
                                OutlinedTextField(
                                    value = etaInput,
                                    onValueChange = { etaInput = it },
                                    label = {
                                        Text(if (isEn) "Estimated Arrival Time (Optional)" else "Perkiraan Jam Tiba (Opsional)")
                                    },
                                    placeholder = {
                                        Text("09:30 WIB")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_parent_visit_eta_${sess.id}")
                                )
                            }

                            OutlinedTextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                label = {
                                    Text(
                                        if (isHadirSelected) {
                                            if (isEn) "Notes for Caregiver (Optional)" else "Pesan / Catatan (Opsional)"
                                        } else {
                                            if (isEn) "Reason / Message for Caregiver" else "Alasan / Keterangan Berhalangan"
                                        }
                                    )
                                },
                                maxLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_parent_visit_notes_${sess.id}")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (existingConf != null) {
                                    OutlinedButton(
                                        onClick = { isEditing = false },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_parent_visit_cancel_edit_${sess.id}")
                                    ) {
                                        Text(if (isEn) "Cancel" else "Batal")
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (isHadirSelected && visitorNameInput.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Please enter visitor name first." else "Mohon isi nama pengunjung terlebih dahulu.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }
                                        val effectiveDate = sessionDate.takeIf { it > 0L } ?: System.currentTimeMillis()
                                        onSaveConfirmation(
                                            sess,
                                            targetStudent,
                                            effectiveDate,
                                            visitorNameInput.trim(),
                                            relationChoice,
                                            statusChoice,
                                            etaInput.trim(),
                                            notesInput.trim()
                                        )
                                        isEditing = false
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isHadirSelected) tealPrimary else Color(0xFFD97706)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_parent_visit_confirm_${sess.id}")
                                ) {
                                    Text(
                                        text = if (isHadirSelected) {
                                            if (isEn) "Confirm & Generate QR Pass" else "Konfirmasi Hadir & Buat Barcode"
                                        } else {
                                            if (isEn) "Save Absence Notice" else "Simpan Pemberitahuan"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
