package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.model.VisitConfirmation
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.util.StudentCardRenderer
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ParentVisitDetailBottomSheet(
    session: VisitSession,
    student: ChildStudent,
    studentDistrictKey: String,
    existingConf: VisitConfirmation?,
    checkInRec: VisitRecord?,
    incomingSwap: VisitSessionSwap?,
    defaultParentName: String,
    isCompletedHistory: Boolean,
    isEn: Boolean,
    dateFmt: SimpleDateFormat,
    timeFmt: SimpleDateFormat,
    onDismiss: () -> Unit,
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
    onPrintSessionTicket: ((VisitConfirmation) -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tealPrimary = Color(0xFF0F766E)
    val tealLight = Color(0xFFF0FDFA)
    val tealBorder = Color(0xFF99F6E4)

    val sessionDates = remember(session.id, session.sessionDatesCsv) {
        session.getVisitDatesList()
    }
    val primarySessionDate = sessionDates.firstOrNull() ?: 0L

    var isEditing by remember(session.id, existingConf?.updatedAt) {
        mutableStateOf(existingConf == null)
    }
    var statusChoice by remember(session.id, existingConf?.attendanceStatus) {
        mutableStateOf(existingConf?.attendanceStatus ?: VisitConfirmation.STATUS_HADIR)
    }
    var relationChoice by remember(session.id, existingConf?.visitorRelation) {
        mutableStateOf(existingConf?.visitorRelation?.ifBlank { "Ibu" } ?: "Ibu")
    }
    var visitorNameInput by remember(session.id, existingConf?.visitorName, defaultParentName) {
        mutableStateOf(existingConf?.visitorName?.ifBlank { defaultParentName } ?: defaultParentName)
    }
    var etaInput by remember(session.id, existingConf?.estimatedArrivalTime) {
        mutableStateOf(existingConf?.estimatedArrivalTime.orEmpty())
    }
    var notesInput by remember(session.id, existingConf?.notes) {
        mutableStateOf(existingConf?.notes.orEmpty())
    }

    val isHadirSelected = statusChoice.equals(VisitConfirmation.STATUS_HADIR, ignoreCase = true)

    val statusBgColor = when {
        checkInRec != null -> Color(0xFFDCFCE7)
        existingConf?.isHadir == true -> tealPrimary.copy(alpha = 0.14f)
        existingConf != null -> Color(0xFFFEF3C7)
        isCompletedHistory -> Color(0xFFF1F5F9)
        else -> Color(0xFFE0F2FE)
    }
    val statusTextColor = when {
        checkInRec != null -> Color(0xFF15803D)
        existingConf?.isHadir == true -> tealPrimary
        existingConf != null -> Color(0xFFB45309)
        isCompletedHistory -> Color(0xFF64748B)
        else -> Color(0xFF0369A1)
    }
    val statusLabel = when {
        checkInRec != null -> if (isEn) "Checked In" else "Sudah Hadir"
        existingConf?.isHadir == true -> if (isEn) "Confirmed" else "Terkonfirmasi"
        existingConf != null -> if (isEn) "Absent" else "Berhalangan"
        isCompletedHistory -> if (isEn) "Completed" else "Selesai"
        else -> if (isEn) "Scheduled" else "Terjadwal"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("sheet_parent_visit_detail_${session.id}"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = session.title,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusBgColor
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (session.updatedNoticeAfterParentLaunch) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Text(
                                    text = if (isEn) "Updated" else "Diperbarui",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("btn_parent_visit_sheet_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isEn) "Close detail" else "Tutup detail",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (sessionDates.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "-",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    } else {
                        sessionDates.forEach { d ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EventAvailable,
                                    contentDescription = null,
                                    tint = Color(0xFF0369A1),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateFmt.format(Date(d)),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = tealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) {
                                "Visiting Hours: ${session.displayTimeRange()}"
                            } else {
                                "Jam Kunjungan: ${session.displayTimeRange()}"
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tealPrimary
                        )
                    }

                    if (session.description.isNotBlank()) {
                        Text(
                            text = session.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }

                    val districts = session.getDistrictsList()
                    if (districts.isNotEmpty()) {
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Covered Districts" else "Cakupan Kecamatan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            districts.forEach { dist ->
                                val isMine = dist.equals(studentDistrictKey, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isMine) tealPrimary.copy(alpha = 0.14f) else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isMine) tealPrimary else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Text(
                                        text = VisitHelper.displayDistrict(dist, isEn),
                                        fontSize = 11.sp,
                                        fontWeight = if (isMine) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isMine) tealPrimary else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (incomingSwap != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("banner_parent_visit_swapped_${session.id}"),
                    shape = RoundedCornerShape(12.dp),
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
                        .testTag("badge_parent_visit_checkedin_${session.id}"),
                    shape = RoundedCornerShape(12.dp),
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
                            .testTag("card_parent_visit_barcode_${session.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = tealLight,
                        border = BorderStroke(1.dp, tealBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
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
                                            .testTag("img_parent_visit_qrcode_${session.id}")
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
                            if (existingConf.notes.isNotBlank()) {
                                Text(
                                    text = (if (isEn) "Notes: " else "Catatan: ") + existingConf.notes,
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center
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
                            if (onPrintSessionTicket != null) {
                                Button(
                                    onClick = { onPrintSessionTicket(existingConf) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_parent_visit_print_ticket_${session.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEn) "Print Session Ticket (PDF)" else "Cetak Tiket Sesi (PDF)",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_parent_visit_absent_${session.id}"),
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

                if (session.isActive) {
                    OutlinedButton(
                        onClick = { isEditing = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_parent_visit_edit_${session.id}")
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
            } else if (session.isActive || existingConf == null) {
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
                            .testTag("chip_parent_visit_hadir_${session.id}")
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
                            .testTag("chip_parent_visit_berhalangan_${session.id}")
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
                                modifier = Modifier.testTag(
                                    "chip_parent_visit_rel_${session.id}_${rel.replace(" ", "_").replace("/", "_")}"
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = visitorNameInput,
                        onValueChange = { visitorNameInput = it },
                        label = { Text(if (isEn) "Visitor Name" else "Nama Pengunjung") },
                        placeholder = { Text(if (isEn) "e.g. Mrs. Siti Aminah" else "Contoh: Ibu Siti Aminah") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_parent_visit_visitor_name_${session.id}")
                    )
                    OutlinedTextField(
                        value = etaInput,
                        onValueChange = { etaInput = it },
                        label = {
                            Text(if (isEn) "Estimated Arrival Time (Optional)" else "Perkiraan Jam Tiba (Opsional)")
                        },
                        placeholder = { Text("09:30 WIB") },
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
                            .testTag("input_parent_visit_eta_${session.id}")
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
                        .testTag("input_parent_visit_notes_${session.id}")
                )

                if (isHadirSelected) {
                    Text(
                        text = if (isEn) {
                            "QR barcode & printable PDF e-ticket will unlock after confirming attendance."
                        } else {
                            "Barcode QR & e-tiket PDF resmi akan otomatis aktif setelah konfirmasi hadir."
                        },
                        fontSize = 11.sp,
                        color = tealPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

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
                                .testTag("btn_parent_visit_cancel_edit_${session.id}")
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
                            val effectiveDate = primarySessionDate.takeIf { it > 0L } ?: System.currentTimeMillis()
                            onSaveConfirmation(
                                session,
                                student,
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
                            .testTag("btn_parent_visit_confirm_${session.id}")
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

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
