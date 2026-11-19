package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.LeaveDayInfo
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import com.example.data.model.PinnedTaskItem
import com.example.data.model.SchoolProfile
import com.example.data.model.ShiftMessage
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.model.WaliShiftCode
import com.example.ui.dialogs.SchedulePinChip
import com.example.ui.dialogs.openJadwalExternalUrl
import com.example.ui.dialogs.parseJadwalShiftColor
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.StudentClassifier

internal fun LazyListScope.berobatCategoryItems(
    isEn: Boolean,
    todayPlans: List<MedicalRecord>,
    upcomingPlans: List<MedicalRecord>,
    inpatientCount: Int,
    onNavigateToPlan: () -> Unit,
    onOpenDirectTreatment: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onOpenFaskesInfo: () -> Unit,
    onOpenCreatePlan: () -> Unit
) {
    item(key = "hub_berobat_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Treatment Plans" else "Rencana Berobat",
                    subtitle = if (isEn) "Schedule & Queue" else "Jadwal & Antrean",
                    imageRes = R.drawable.img_menu_rencana,
                    badgeText = if (todayPlans.isNotEmpty()) "${todayPlans.size} ${if (isEn) "Today" else "Hari Ini"}" else if (isEn) "Referral" else "Rujukan",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = WaliPrimary,
                    bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFBAE6FD),
                    onClick = onNavigateToPlan,
                    testTag = "tile_rencana_berobat",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Medical Action" else "Tindakan Medis",
                    subtitle = if (isEn) "Log Treatment" else "Input Pengobatan",
                    imageRes = R.drawable.img_menu_tindakan,
                    badgeText = if (isEn) "Direct" else "Langsung",
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFD97706),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenDirectTreatment,
                    testTag = "tile_tindakan_langsung_hub",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Medical Records" else "Rekam Medis",
                    subtitle = if (isEn) "History & Diagnosis" else "Riwayat & Diagnosa",
                    imageRes = R.drawable.img_menu_rekam_medis,
                    badgeText = if (isEn) "Prescriptions" else "Resep Obat",
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF16A34A),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFBBF7D0),
                    onClick = onNavigateToRecords,
                    testTag = "tile_rekam_medis_hub",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Referral Faskes" else "Faskes Rujukan",
                    subtitle = if (isEn) "Puskesmas & ER 24h" else "Puskesmas & UGD 24J",
                    imageRes = R.drawable.img_menu_puskesmas,
                    badgeText = if (inpatientCount > 0) "$inpatientCount ${if (isEn) "Inpatient" else "Rawat Inap"}" else if (isEn) "Directory" else "Daftar Faskes",
                    badgeBg = if (inpatientCount > 0) Color(0xFFFEE2E2) else Color(0xFFE0F2FE),
                    badgeColor = if (inpatientCount > 0) Color(0xFFDC2626) else Color(0xFF0369A1),
                    bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFDCFCE7).copy(alpha = 0.45f)),
                    borderColor = Color(0xFFBAE6FD),
                    onClick = onOpenFaskesInfo,
                    testTag = "tile_faskes_hub",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    item(key = "hub_berobat_summary") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Today's Health Snapshot" else "Ringkasan Kesehatan Hari Ini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onOpenCreatePlan,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "New Plan" else "Buat Rencana", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HubStatTile("${todayPlans.size}", if (isEn) "Today" else "Hari Ini", Color(0xFFE0F2FE), Color(0xFF0369A1), Modifier.weight(1f))
                    HubStatTile("${upcomingPlans.size}", if (isEn) "Upcoming" else "Mendatang", Color(0xFFFEF3C7), Color(0xFFB45309), Modifier.weight(1f))
                    HubStatTile("$inpatientCount", if (isEn) "Inpatient" else "Rawat Inap", Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
                }
            }
        }
    }

    if (todayPlans.isNotEmpty()) {
        item(key = "hub_berobat_preview_header") {
            Text(
                text = if (isEn) "Scheduled Today (${todayPlans.size})" else "Jadwal Hari Ini (${todayPlans.size})",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(todayPlans.take(4), key = { "hub_plan_${it.id}" }) { plan ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToPlan),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plan.studentName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "${plan.faskes} • ${plan.complaint.ifBlank { "-" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

internal fun LazyListScope.puasaCategoryItems(
    isEn: Boolean,
    lang: com.example.util.i18n.AppLanguage,
    fastingSessions: List<FastingSession>,
    fastingRecords: List<FastingRecord>,
    onOpenInputPuasa: () -> Unit,
    onOpenDaftarPuasa: () -> Unit,
    onOpenChecklistPuasa: () -> Unit,
    onOpenRankingPuasa: () -> Unit,
    onOpenCreatePuasaSession: () -> Unit = {}
) {
    val activeSession = fastingSessions.firstOrNull { it.isActive } ?: fastingSessions.firstOrNull()
    val sessionRecords = if (activeSession == null) emptyList() else fastingRecords.filter { it.sessionId == activeSession.id }
    val smaCount = sessionRecords.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMA }
    val smpCount = sessionRecords.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMP }
    val sdCount = sessionRecords.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SD }
    val sahurDone = sessionRecords.count { it.isSahurChecked }
    val bukaDone = sessionRecords.count { it.isBukaChecked }

    item(key = "hub_puasa_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = "Input Puasa",
                    subtitle = if (isEn) "Scan Card / Search" else "Scan Kartu / Cari",
                    imageRes = R.drawable.img_menu_input_puasa,
                    badgeText = if (isEn) "Scan & Search" else "Scan & Cari",
                    badgeBg = Color(0xFFCCFBF1),
                    badgeColor = Color(0xFF0F766E),
                    bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.6f)),
                    borderColor = Color(0xFF99F6E4),
                    onClick = onOpenInputPuasa,
                    testTag = "tile_input_puasa",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = "Daftar Puasa",
                    subtitle = "SD • SMP • SMA & PDF",
                    imageRes = R.drawable.img_menu_daftar_puasa,
                    badgeText = if (sessionRecords.isNotEmpty()) "${sessionRecords.size} Puasa" else if (isEn) "Recap & PDF" else "Rekap & PDF",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = Color(0xFF0369A1),
                    bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFBAE6FD),
                    onClick = onOpenDaftarPuasa,
                    testTag = "tile_daftar_puasa",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Puasa Checklist" else "Checklist Puasa",
                    subtitle = "Sahur & Berbuka + PDF",
                    imageRes = R.drawable.img_menu_checklist_puasa,
                    badgeText = "$sahurDone/${sessionRecords.size} Sahur",
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenChecklistPuasa,
                    testTag = "tile_checklist_puasa",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Puasa Ranking" else "Ranking & History",
                    subtitle = if (isEn) "Top Students & Sessions" else "Siswa Rajin & Sesi",
                    imageRes = R.drawable.img_menu_ranking_puasa,
                    badgeText = if (fastingSessions.isNotEmpty()) "${fastingSessions.size} ${if (isEn) "Sessions" else "Sesi"}" else if (isEn) "Ranking" else "Peringkat",
                    badgeBg = Color(0xFFF3E8FF),
                    badgeColor = Color(0xFF7E22CE),
                    bgGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFE9D5FF),
                    onClick = onOpenRankingPuasa,
                    testTag = "tile_ranking_puasa",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    item(key = "hub_puasa_summary") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF0F766E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeSession != null) {
                                "${activeSession.title} (${DateUtils.formatShortDate(activeSession.sessionDate, lang)})"
                            } else {
                                if (isEn) "No active Puasa session yet" else "Belum ada sesi puasa aktif"
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onOpenCreatePuasaSession,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF0F766E)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_hub_create_puasa_session")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Create Session" else "Buat Sesi",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Button(
                            onClick = onOpenInputPuasa,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Scan Input" else "Scan Input",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HubStatTile("${sessionRecords.size}", if (isEn) "Total" else "Semua", Color(0xFF0F766E), Color.White, Modifier.weight(1f))
                    HubStatTile("$smaCount", "SMA", Color(0xFFF3E8FF), Color(0xFF7E22CE), Modifier.weight(1f))
                    HubStatTile("$smpCount", "SMP", Color(0xFFE0F2FE), Color(0xFF0369A1), Modifier.weight(1f))
                    HubStatTile("$sdCount", "SD", Color(0xFFDCFCE7), Color(0xFF15803D), Modifier.weight(1f))
                }

                if (sessionRecords.isNotEmpty()) {
                    val sahurPct = (sahurDone.toFloat() / sessionRecords.size).coerceIn(0f, 1f)
                    val bukaPct = (bukaDone.toFloat() / sessionRecords.size).coerceIn(0f, 1f)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sahur $sahurDone/${sessionRecords.size}", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F766E))
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { sahurPct },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF0F766E),
                                trackColor = Color(0xFFCCFBF1)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Berbuka $bukaDone/${sessionRecords.size}", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB45309))
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { bukaPct },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFB45309),
                                trackColor = Color(0xFFFEF3C7)
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun LazyListScope.haidCategoryItems(
    isEn: Boolean,
    activeHaidRecords: List<HaidRecord>,
    suciRecords: List<HaidRecord>,
    onOpenInputHaid: () -> Unit,
    onOpenDaftarHaid: () -> Unit,
    onOpenDaftarSuci: () -> Unit,
    onOpenFiqihHaid: () -> Unit
) {
    val istihadhahCount = activeHaidRecords.count { it.isIstihadhahWarning }

    item(key = "hub_haid_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = "Input Haid",
                    subtitle = if (isEn) "Scan Card / NIK" else "Scan Kartu / NIK",
                    imageRes = R.drawable.img_menu_input_haid,
                    badgeText = "Scan Barcode",
                    badgeBg = Color(0xFFFCE7F3),
                    badgeColor = Color(0xFFDB2777),
                    bgGradient = listOf(Color(0xFFFFF1F2), Color(0xFFFCE7F3).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFFBCFE8),
                    onClick = onOpenInputHaid,
                    testTag = "tile_input_haid",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = "Daftar Haid",
                    subtitle = if (activeHaidRecords.isNotEmpty()) {
                        if (isEn) "${activeHaidRecords.size} Active Haid" else "${activeHaidRecords.size} Sedang Haid"
                    } else {
                        if (isEn) "No Active Haid" else "Tidak Ada Haid"
                    },
                    imageRes = R.drawable.img_menu_daftar_haid,
                    badgeText = if (istihadhahCount > 0) {
                        if (isEn) "$istihadhahCount Day 15" else "$istihadhahCount Hari ke-15"
                    } else {
                        "${activeHaidRecords.size} ${if (isEn) "Active" else "Siswi"}"
                    },
                    badgeBg = if (istihadhahCount > 0) Color(0xFFFEE2E2) else Color(0xFFFCE7F3),
                    badgeColor = if (istihadhahCount > 0) Color(0xFFDC2626) else Color(0xFFBE185D),
                    bgGradient = listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFF472B6),
                    onClick = onOpenDaftarHaid,
                    testTag = "tile_daftar_haid",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = "Daftar Suci",
                    subtitle = if (isEn) "History & Ready for Prayer" else "Riwayat & Siap Ibadah",
                    imageRes = R.drawable.img_menu_daftar_suci,
                    badgeText = if (suciRecords.isNotEmpty()) "${suciRecords.size} Suci" else "Suci",
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF16A34A),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFBBF7D0),
                    onClick = onOpenDaftarSuci,
                    testTag = "tile_daftar_suci",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Guide & Fiqih Haid" else "Panduan & Fiqih",
                    subtitle = if (isEn) "Fiqih Haid Rules" else "Kaidah Syariat Islam",
                    imageRes = R.drawable.img_menu_fiqih_haid,
                    badgeText = "Fiqih Haid",
                    badgeBg = Color(0xFFCCFBF1),
                    badgeColor = Color(0xFF0F766E),
                    bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.6f)),
                    borderColor = Color(0xFF99F6E4),
                    onClick = onOpenFiqihHaid,
                    testTag = "tile_fiqih_haid",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    item(key = "hub_haid_summary") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HubStatTile("${activeHaidRecords.size}", if (isEn) "Active Haid" else "Sedang Haid", Color(0xFFFCE7F3), Color(0xFFBE185D), Modifier.weight(1f))
                HubStatTile("${suciRecords.size}", "Masa Suci", Color(0xFFDCFCE7), Color(0xFF15803D), Modifier.weight(1f))
                HubStatTile("$istihadhahCount", if (isEn) "Day 15 Alert" else "Hari ke-15", Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
            }
        }
    }

    if (activeHaidRecords.isNotEmpty()) {
        item(key = "hub_haid_preview_hdr") {
            Text(
                text = if (isEn) "Active Haid Students" else "Siswi Sedang Haid Saat Ini",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(activeHaidRecords.take(3), key = { "hub_haid_${it.id}" }) { rec ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDaftarHaid),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (rec.isIstihadhahWarning) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rec.studentName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "${rec.studentClass.ifBlank { "-" }} • ${rec.studentDorm}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (rec.isIstihadhahWarning) Color(0xFFFEE2E2) else Color(0xFFFCE7F3))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isEn) "Day ${rec.durationDays}" else "Hari ke-${rec.durationDays}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rec.isIstihadhahWarning) Color(0xFFDC2626) else Color(0xFFBE185D)
                        )
                    }
                }
            }
        }
    }
}

internal fun LazyListScope.pesanCategoryItems(
    isEn: Boolean,
    shiftMessages: List<ShiftMessage>,
    onOpenShiftMessages: () -> Unit,
    onOpenCreateShiftMessage: () -> Unit,
    onOpenShiftHistory: () -> Unit
) {
    val activeToday = shiftMessages.filter { !it.isCompleted && DateUtils.isToday(it.followUpDate) }
    val urgentCount = activeToday.count { it.priority == "URGENT" }
    val missedPast = shiftMessages.count { !it.isCompleted && DateUtils.isBeforeToday(it.followUpDate) }

    item(key = "hub_pesan_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Shift Messages" else "Pesan Shift",
                    subtitle = if (isEn) "Duty Handover" else "Operan Jaga Asrama",
                    imageRes = R.drawable.img_menu_pesan_shift,
                    badgeText = when {
                        urgentCount > 0 -> "$urgentCount URGENT"
                        activeToday.isNotEmpty() -> "${activeToday.size} ${if (isEn) "Notes" else "Pesan"}"
                        else -> if (isEn) "Shift Duty" else "Operan Jaga"
                    },
                    badgeBg = if (urgentCount > 0) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                    badgeColor = if (urgentCount > 0) Color(0xFFDC2626) else Color(0xFFD97706),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenShiftMessages,
                    testTag = "tile_pesan_shift_berikutnya",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Write Note" else "Tulis Pesan",
                    subtitle = if (isEn) "Create Handover Note" else "Buat Pesan Shift",
                    imageRes = R.drawable.img_doctor_panda,
                    badgeText = if (isEn) "Quick Add" else "Tulis Baru",
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF16A34A),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFBBF7D0),
                    onClick = onOpenCreateShiftMessage,
                    testTag = "tile_tulis_pesan_shift_hub",
                    modifier = Modifier.weight(1f)
                )
            }
            CartoonMenuCard(
                title = if (isEn) "Handover History & Missed Archive" else "Riwayat Operan & Arsip Lewat Hari Ini",
                subtitle = if (isEn) "Review past days handover & completion status" else "Cek arsip operan jaga hari sebelumnya & status tindak lanjut",
                imageRes = R.drawable.img_menu_rekam_medis,
                badgeText = if (missedPast > 0) {
                    "$missedPast ${if (isEn) "Uncompleted" else "Belum Selesai"}"
                } else {
                    if (isEn) "Archive" else "Arsip"
                },
                badgeBg = if (missedPast > 0) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                badgeColor = if (missedPast > 0) Color(0xFFD97706) else Color(0xFF475569),
                bgGradient = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
                borderColor = Color(0xFFCBD5E1),
                onClick = onOpenShiftHistory,
                testTag = "tile_riwayat_operan",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    item(key = "hub_pesan_summary") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HubStatTile("${activeToday.size}", if (isEn) "Active Today" else "Aktif Hari Ini", Color(0xFFFEF3C7), Color(0xFFB45309), Modifier.weight(1f))
                HubStatTile("$urgentCount", "Urgent", Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
                HubStatTile("$missedPast", if (isEn) "Missed" else "Terlewat", Color(0xFFF1F5F9), Color(0xFF475569), Modifier.weight(1f))
            }
        }
    }

    if (activeToday.isNotEmpty()) {
        item(key = "hub_pesan_preview_hdr") {
            Text(
                text = if (isEn) "Active Handover Notes" else "Pesan Shift Aktif Hari Ini",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(activeToday.take(3), key = { "hub_msg_${it.id}" }) { msg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenShiftMessages),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (msg.priority == "URGENT") Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(msg.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "${msg.shift} • ${msg.senderName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = if (msg.priority == "URGENT") Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (msg.priority == "URGENT") Color(0xFFDC2626) else Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HubStatTile(
    value: String,
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = fg, maxLines = 1)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = fg.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

internal fun LazyListScope.mengajiCategoryItems(
    isEn: Boolean,
    lang: com.example.util.i18n.AppLanguage,
    mengajiRecords: List<MengajiRecord>,
    onOpenInputMengaji: () -> Unit,
    onOpenRiwayatMengaji: () -> Unit,
    onOpenKartuMengaji: () -> Unit,
    onOpenRekapMengaji: () -> Unit
) {
    val todayRecords = mengajiRecords.filter { DateUtils.isToday(it.sessionDate) }
    val fluentCount = mengajiRecords.count { MengajiHelper.isFluent(it.fluencyStatus) }
    val repeatCount = mengajiRecords.size - fluentCount
    val recentRecords = mengajiRecords.sortedWith(
        compareByDescending<MengajiRecord> { it.sessionDate }.thenByDescending { it.createdAt }
    )

    item(key = "hub_mengaji_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Input Mengaji" else "Input Mengaji",
                    subtitle = if (isEn) "Auto-Continue Page" else "Lanjut Halaman Anak",
                    imageRes = R.drawable.img_menu_mengaji,
                    badgeText = if (isEn) "Scan & Input" else "Scan & Lanjut",
                    badgeBg = Color(0xFFD1FAE5),
                    badgeColor = Color(0xFF047857),
                    bgGradient = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFA7F3D0),
                    onClick = onOpenInputMengaji,
                    testTag = "tile_input_mengaji",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Print Mengaji Card" else "Cetak Kartu Mengaji",
                    subtitle = if (isEn) "Monthly PDF & Sign" else "Tabel Bulanan & TTD",
                    imageRes = R.drawable.img_menu_daftar_puasa,
                    badgeText = if (isEn) "Print & PDF" else "Print & PDF",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = Color(0xFF0369A1),
                    bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFBAE6FD),
                    onClick = onOpenKartuMengaji,
                    testTag = "tile_kartu_mengaji",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Daily History" else "Riwayat Mengaji",
                    subtitle = if (isEn) "Daily Student Logs" else "Setoran Harian Siswa",
                    imageRes = R.drawable.img_menu_checklist_puasa,
                    badgeText = if (todayRecords.isNotEmpty()) {
                        "${todayRecords.size} ${if (isEn) "Today" else "Hari Ini"}"
                    } else {
                        "${mengajiRecords.size} Log"
                    },
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenRiwayatMengaji,
                    testTag = "tile_riwayat_mengaji",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Volume Recap" else "Rekap Jilid Anak",
                    subtitle = if (isEn) "Last Page Per Student" else "Posisi Terakhir Anak",
                    imageRes = R.drawable.img_menu_ranking_puasa,
                    badgeText = if (repeatCount > 0) {
                        "$repeatCount ${if (isEn) "Repeat" else "Ulang"}"
                    } else {
                        if (isEn) "Per Student" else "Per Anak"
                    },
                    badgeBg = if (repeatCount > 0) Color(0xFFFEE2E2) else Color(0xFFF3E8FF),
                    badgeColor = if (repeatCount > 0) Color(0xFFDC2626) else Color(0xFF7E22CE),
                    bgGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFE9D5FF),
                    onClick = onOpenRekapMengaji,
                    testTag = "tile_rekap_mengaji",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    item(key = "hub_mengaji_summary") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HubStatTile("${todayRecords.size}", if (isEn) "Today" else "Hari Ini", Color(0xFFD1FAE5), Color(0xFF047857), Modifier.weight(1f))
                HubStatTile("$fluentCount", if (isEn) "Fluent" else "Lancar", Color(0xFFDCFCE7), Color(0xFF15803D), Modifier.weight(1f))
                HubStatTile("$repeatCount", if (isEn) "Repeat" else "Tdk Lancar", Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
            }
        }
    }

    if (recentRecords.isNotEmpty()) {
        item(key = "hub_mengaji_recent_hdr") {
            Text(
                text = if (isEn) "Latest Mengaji Entries" else "Setoran Mengaji Terbaru",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(recentRecords.take(4), key = { "hub_mengaji_${it.id}" }) { rec ->
            val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenRiwayatMengaji),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (isFluent) Color(0xFFBBF7D0) else Color(0xFFFCA5A5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rec.studentName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "${MengajiHelper.formatCompactPosition(rec)} • ${DateUtils.formatShortDate(rec.sessionDate, lang)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFluent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFluent) Color(0xFF15803D) else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}

internal fun LazyListScope.kunjunganCategoryItems(
    isEn: Boolean,
    lang: com.example.util.i18n.AppLanguage,
    students: List<ChildStudent>,
    visitSessions: List<VisitSession>,
    visitRecords: List<VisitRecord>,
    isAdmin: Boolean,
    onOpenJadwalKunjungan: () -> Unit,
    onOpenDaftarHadirKunjungan: (Int) -> Unit,
    onOpenLaporanKunjungan: () -> Unit,
    onOpenSiswaPerKecamatan: () -> Unit,
    onOpenCreateVisitSession: () -> Unit
) {
    val activeSession = visitSessions.firstOrNull { it.isActive } ?: visitSessions.firstOrNull()
    val scheduledStudents = if (activeSession == null) {
        emptyList()
    } else {
        VisitHelper.scheduledStudentsForSession(activeSession, students)
    }
    val sessionRecords = if (activeSession == null) {
        emptyList()
    } else {
        visitRecords.filter { it.sessionId == activeSession.id }
    }
    val presentCount = if (activeSession == null) 0 else {
        scheduledStudents.count { st -> VisitHelper.findVisitForStudent(sessionRecords, st) != null }
    }
    val absentCount = (scheduledStudents.size - presentCount).coerceAtLeast(0)
    val dynamicDistrictCount = VisitHelper.extractDynamicDistricts(students, isEn).size

    item(key = "hub_kunjungan_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Visit Schedule" else "Jadwal Kunjungan",
                    subtitle = if (isEn) "Monthly & Multi-Date" else "Per Sesi & Kecamatan",
                    imageRes = R.drawable.img_menu_kunjungan,
                    badgeText = if (activeSession != null) {
                        "${activeSession.getVisitDatesList().size} ${if (isEn) "Dates" else "Tgl"}"
                    } else {
                        if (isEn) "Monthly" else "Per Bulan"
                    },
                    badgeBg = Color(0xFFDBEAFE),
                    badgeColor = Color(0xFF1D4ED8),
                    bgGradient = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFBFDBFE),
                    onClick = onOpenJadwalKunjungan,
                    testTag = "tile_jadwal_kunjungan",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Attendance" else "Daftar Hadir",
                    subtitle = if (isEn) "Barcode & Manual PDF" else "Barcode & Cetak PDF",
                    imageRes = R.drawable.img_menu_daftar_puasa,
                    badgeText = if (isEn) "Scan & PDF" else "Scan & PDF",
                    badgeBg = Color(0xFFCCFBF1),
                    badgeColor = Color(0xFF0F766E),
                    bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.6f)),
                    borderColor = Color(0xFF99F6E4),
                    onClick = { onOpenDaftarHadirKunjungan(0) },
                    testTag = "tile_daftar_hadir_kunjungan",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Visit Report" else "Laporan Kehadiran",
                    subtitle = if (isEn) "Attended vs Pending" else "Hadir vs Belum Hadir",
                    imageRes = R.drawable.img_menu_checklist_puasa,
                    badgeText = if (activeSession != null) "$presentCount/${scheduledStudents.size}" else if (isEn) "Recap" else "Rekap",
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF15803D),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFA7F3D0),
                    onClick = onOpenLaporanKunjungan,
                    testTag = "tile_laporan_kunjungan",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "By District" else "Siswa Kecamatan",
                    subtitle = if (isEn) "District Student List" else "Daftar per Kecamatan",
                    imageRes = R.drawable.img_menu_anak_asuh,
                    badgeText = if (dynamicDistrictCount > 0) "$dynamicDistrictCount Kec." else if (isEn) "Districts" else "Dinamis",
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenSiswaPerKecamatan,
                    testTag = "tile_siswa_per_kecamatan",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    item(key = "hub_kunjungan_active_session") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activeSession == null) {
                    Text(
                        text = if (isEn) {
                            "No monthly visit session configured yet. School Admin picks scheduled dates and districts (Kecamatan) in Admin → Visit tab."
                        } else {
                            "Belum ada sesi kunjungan aktif. Admin Sekolah dapat membuat sesi kunjungan per bulan beserta multi-tanggal dan kecamatan di Halaman Admin → Tab Kunjungan."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isAdmin) {
                        Button(
                            onClick = onOpenCreateVisitSession,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_hub_create_visit_session")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEn) "Create Visit Session" else "Buat Sesi Kunjungan Baru", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val dateFmt = java.text.SimpleDateFormat("d MMM", lang.locale)
                    val dateBadges = activeSession.getVisitDatesList().joinToString(" • ") { dateFmt.format(java.util.Date(it)) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activeSession.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                            Text(
                                text = "${VisitHelper.formatMonthYearLabel(activeSession.monthYear, lang.locale)}${if (dateBadges.isNotBlank()) " • $dateBadges" else ""}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HubStatTile("${scheduledStudents.size}", if (isEn) "Scheduled" else "Terjadwal", Color(0xFFDBEAFE), Color(0xFF1D4ED8), Modifier.weight(1f))
                        HubStatTile("$presentCount", if (isEn) "Attended" else "Sudah Hadir", Color(0xFFDCFCE7), Color(0xFF15803D), Modifier.weight(1f))
                        HubStatTile("$absentCount", if (isEn) "Pending" else "Belum Hadir", Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenDaftarHadirKunjungan(0) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEn) "Scan Check-in" else "Scan Hadir", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onOpenDaftarHadirKunjungan(1) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                if (isEn) "Manual PDF" else "Cetak PDF Manual",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun LazyListScope.orangTuaCategoryItems(
    isEn: Boolean,
    students: List<ChildStudent>,
    parentAccessLinks: List<ParentAccessLink>,
    parentMessages: List<ParentMessage>,
    parentExpectations: List<ParentExpectation> = emptyList(),
    myFosterStudentsCount: Int,
    onOpenPilihAnakAsuh: () -> Unit,
    onOpenKartuAksesOrtu: () -> Unit,
    onOpenPilihanAksesOrtu: () -> Unit,
    onOpenPesanOrtu: () -> Unit,
    onOpenHarapanOrtu: () -> Unit = {}
) {
    val unreadParentCount = parentMessages.count { !it.isReadByWali && it.senderRole.equals("PARENT", ignoreCase = true) }
    val unreadExpectationCount = parentExpectations.count { !it.isReadByWali }
    val cardsCount = parentAccessLinks.size
    val pendingDeletionCount = parentAccessLinks.count { it.deletionRequested }
    val unclaimedCount = students.count { it.assignedWaliUsername.isBlank() && it.assignedWaliName.isBlank() }

    item(key = "hub_orang_tua_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Claim Foster Kids" else "Klaim Anak Asuh",
                    subtitle = if (isEn) "Claim & Search Students" else "Cari & Klaim Binaan",
                    imageRes = R.drawable.img_menu_klaim_anak_asuh,
                    badgeText = if (myFosterStudentsCount > 0) {
                        "$myFosterStudentsCount ${if (isEn) "Kids" else "Anak"}"
                    } else {
                        if (isEn) "Pick" else "Pilih"
                    },
                    badgeBg = Color(0xFFE0E7FF),
                    badgeColor = Color(0xFF4338CA),
                    bgGradient = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFC7D2FE),
                    onClick = onOpenPilihAnakAsuh,
                    testTag = "tile_pilih_anak_asuh",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Parent Cards" else "Kartu Akses Ortu",
                    subtitle = if (isEn) "ID Card & Share WA" else "Akun Login & Share WA",
                    imageRes = R.drawable.img_menu_orang_tua,
                    badgeText = when {
                        pendingDeletionCount > 0 -> "$pendingDeletionCount ${if (isEn) "Delete Req" else "Hapus Akun"}"
                        cardsCount > 0 -> "$cardsCount ${if (isEn) "Cards" else "Kartu"}"
                        else -> if (isEn) "Auto" else "Otomatis"
                    },
                    badgeBg = if (pendingDeletionCount > 0) Color(0xFFFEE2E2) else Color(0xFFEDE9FE),
                    badgeColor = if (pendingDeletionCount > 0) Color(0xFFDC2626) else Color(0xFF6D28D9),
                    bgGradient = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFDDD6FE),
                    onClick = onOpenKartuAksesOrtu,
                    testTag = "tile_kartu_akses_ortu",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Access Options" else "Pilihan Akses",
                    subtitle = if (isEn) "Profile, Mengaji, Sick, Fasting, Haid & Savings" else "Profil, Mengaji, Sakit, Puasa, Haid & Tabungan",
                    imageRes = R.drawable.img_menu_checklist_puasa,
                    badgeText = if (isEn) "Controls" else "Kontrol",
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF15803D),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFBBF7D0),
                    onClick = onOpenPilihanAksesOrtu,
                    testTag = "tile_pilihan_akses_ortu",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Parent Chat" else "Pesan Orang Tua",
                    subtitle = if (isEn) "2-Way Parent Chat" else "Chat 2 Arah Orang Tua",
                    imageRes = R.drawable.img_menu_pesan_shift,
                    badgeText = if (unreadParentCount > 0) {
                        "$unreadParentCount ${if (isEn) "New" else "Baru"}"
                    } else {
                        "${parentMessages.size} Chat"
                    },
                    badgeBg = if (unreadParentCount > 0) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                    badgeColor = if (unreadParentCount > 0) Color(0xFFDC2626) else Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenPesanOrtu,
                    testTag = "tile_pesan_ortu",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Parent Hopes" else "Harapan Orang Tua",
                    subtitle = if (isEn) "Character Expectations & Shared Advice" else "Harapan Karakter & Ikhtiar Nasehat",
                    imageRes = R.drawable.img_menu_konseling,
                    badgeText = when {
                        unreadExpectationCount > 0 -> "$unreadExpectationCount ${if (isEn) "New" else "Baru"}"
                        parentExpectations.isNotEmpty() -> "${parentExpectations.size} ${if (isEn) "Notes" else "Catatan"}"
                        else -> if (isEn) "Shared" else "Ikhtiar"
                    },
                    badgeBg = if (unreadExpectationCount > 0) Color(0xFFFEE2E2) else Color(0xFFD1FAE5),
                    badgeColor = if (unreadExpectationCount > 0) Color(0xFFDC2626) else Color(0xFF047857),
                    bgGradient = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFA7F3D0),
                    onClick = onOpenHarapanOrtu,
                    testTag = "tile_harapan_ortu",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    item(key = "hub_orang_tua_summary") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HubStatTile("$myFosterStudentsCount", if (isEn) "My Foster" else "Binaan Saya", Color(0xFFE0E7FF), Color(0xFF4338CA), Modifier.weight(1f))
                HubStatTile("$cardsCount", if (isEn) "Cards Active" else "Kartu Akses", Color(0xFFEDE9FE), Color(0xFF6D28D9), Modifier.weight(1f))
                HubStatTile("${parentExpectations.size}", if (isEn) "Hopes" else "Harapan", Color(0xFFD1FAE5), Color(0xFF047857), Modifier.weight(1f))
                HubStatTile("${unreadParentCount + unreadExpectationCount}", if (isEn) "Unread" else "Baru", Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
            }
        }
    }
}

internal fun LazyListScope.tabunganCategoryItems(
    isEn: Boolean,
    allStudents: List<ChildStudent>,
    claimedStudents: List<ChildStudent>,
    transactions: List<TabunganTransaction>,
    logs: List<TabunganLog>,
    isAnakSuperAdmin: Boolean,
    onOpenInputTabungan: (String, ChildStudent?) -> Unit,
    onOpenStudentList: () -> Unit,
    onOpenStudentDetail: (ChildStudent) -> Unit,
    onOpenHistoryLogs: () -> Unit,
    onOpenPdfSummary: () -> Unit,
    onOpenPilihAnakAsuh: () -> Unit,
    onOpenPilihanAksesOrtu: () -> Unit
) {
    val totals = TabunganHelper.computeAggregateTotals(transactions)
    val claimedIds = claimedStudents.map { it.id }.toSet()
    val extraSchoolStudentsForSuper = if (isAnakSuperAdmin) {
        val txStudentIds = transactions.map { it.studentId }.filter { it > 0L && it !in claimedIds }.toSet()
        txStudentIds.map { sid ->
            allStudents.firstOrNull { it.id == sid } ?: run {
                val sampleTx = transactions.first { it.studentId == sid }
                ChildStudent(
                    id = sid,
                    name = sampleTx.studentName,
                    nis = sampleTx.studentNis,
                    gender = "L",
                    age = 12,
                    dormitory = sampleTx.studentDorm,
                    classGrade = sampleTx.studentClass,
                    assignedWaliUsername = sampleTx.ownerWaliUsername,
                    assignedWaliName = sampleTx.ownerWaliName,
                    schoolId = sampleTx.schoolId
                )
            }
        }
    } else {
        emptyList()
    }
    val displayedStudents = (claimedStudents + extraSchoolStudentsForSuper)
        .distinctBy { it.id }
        .sortedBy { it.name.lowercase() }

    // 1. Deklarasi Sifat Pribadi & Privasi Ketat
    item(key = "hub_tabungan_privacy") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_tabungan_privacy_declaration"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
            border = BorderStroke(1.dp, Color(0xFFFCD34D))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = if (isEn) "Private Ledger" else "Privasi Tabungan",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD97706)
                        ) {
                            Text(
                                text = if (isEn) "PRIVATE & ONLINE SYNCED" else "BERSIFAT PRIBADI",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (isAnakSuperAdmin) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF047857)
                            ) {
                                Text(
                                    text = "SUPER ACCESS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isEn) {
                            "This savings ledger is strictly PRIVATE: only visible to you as the managing Foster Caregiver (and AnakSuperAdmin), never published to other caregivers or school admins even in the same school. Parents only see it if you check their permission."
                        } else {
                            "Catatan tabungan ini bersifat PRIBADI: hanya dapat dilihat oleh Anda sebagai Wali Asuh pemegang titipan (dan AnakSuperAdmin), tidak dipublikasikan ke Wali Asuh maupun admin lain meski satu sekolah."
                        },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }
    }

    // 2. Ringkasan Total Saldo Keseluruhan
    item(key = "hub_tabungan_totals") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_tabungan_totals"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEn) "Total Held Balance" else "Total Saldo Titipan Saya",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = TabunganHelper.formatRupiah(totals.totalBalance),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${totals.holdersCount} ${if (isEn) "Holders" else "Anak"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HubStatTile(
                        value = TabunganHelper.formatRupiah(totals.totalIn),
                        label = if (isEn) "Total In (+)" else "Pemasukan (+)",
                        bg = Color(0xFFDCFCE7),
                        fg = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = TabunganHelper.formatRupiah(totals.totalOut),
                        label = if (isEn) "Total Out (-)" else "Pengeluaran (-)",
                        bg = Color(0xFFFEE2E2),
                        fg = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = "${totals.activeCount}",
                        label = if (isEn) "Active Tx" else "Transaksi",
                        bg = Color(0xFFFEF3C7),
                        fg = Color(0xFFB45309),
                        modifier = Modifier.weight(0.65f)
                    )
                }
            }
        }
    }

    // 3. Ikon Menu Di Dalam Tabungan (Gaya Seragam dengan Puasa & Mengaji)
    item(key = "hub_tabungan_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Deposit (+)" else "Titip Masuk (+)",
                    subtitle = if (isEn) "Add Allowance / Savings" else "Catat Uang Titipan",
                    imageRes = R.drawable.img_menu_tabungan,
                    badgeText = if (isEn) "Income" else "Pemasukan",
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF15803D),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFA7F3D0),
                    onClick = { onOpenInputTabungan(TabunganHelper.TYPE_IN, null) },
                    testTag = "tile_tabungan_masuk",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Expense (-)" else "Pengeluaran (-)",
                    subtitle = if (isEn) "Withdraw / Buy Needs" else "Ambil & Jajan Anak",
                    imageRes = R.drawable.img_menu_tindakan,
                    badgeText = if (isEn) "Expense" else "Ambil Uang",
                    badgeBg = Color(0xFFFEE2E2),
                    badgeColor = Color(0xFFDC2626),
                    bgGradient = listOf(Color(0xFFFEF2F2), Color(0xFFFEE2E2).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFFECACA),
                    onClick = { onOpenInputTabungan(TabunganHelper.TYPE_OUT, null) },
                    testTag = "tile_tabungan_keluar",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Balance / Child" else "Saldo Per Anak",
                    subtitle = if (isEn) "Claimed Foster Kids Only" else "Daftar Anak Binaan Saya",
                    imageRes = R.drawable.img_menu_anak_asuh,
                    badgeText = "${displayedStudents.size} ${if (isEn) "Kids" else "Anak"}",
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenStudentList,
                    testTag = "tile_tabungan_per_anak",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "History & Audit Log" else "Riwayat & Log",
                    subtitle = if (isEn) "Transactions & Audit Trail" else "Mutasi, Batal & Hapus",
                    imageRes = R.drawable.img_menu_daftar_puasa,
                    badgeText = "${logs.size} Log",
                    badgeBg = Color(0xFFE0E7FF),
                    badgeColor = Color(0xFF4338CA),
                    bgGradient = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFC7D2FE),
                    onClick = onOpenHistoryLogs,
                    testTag = "tile_tabungan_riwayat",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Print Recap PDF" else "Cetak Rekap PDF",
                    subtitle = if (isEn) "Personal Balance Sheet" else "Laporan Saldo Binaan",
                    imageRes = R.drawable.img_menu_ranking_puasa,
                    badgeText = "PDF",
                    badgeBg = Color(0xFFCCFBF1),
                    badgeColor = Color(0xFF0F766E),
                    bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.6f)),
                    borderColor = Color(0xFF99F6E4),
                    onClick = onOpenPdfSummary,
                    testTag = "tile_tabungan_laporan",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Parent Access" else "Izin Akses Ortu",
                    subtitle = if (isEn) "Toggle Savings Visibility" else "Centang Akses Portal Ortu",
                    imageRes = R.drawable.img_menu_orang_tua,
                    badgeText = if (isEn) "Checkbox" else "Centang",
                    badgeBg = Color(0xFFEDE9FE),
                    badgeColor = Color(0xFF6D28D9),
                    bgGradient = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFDDD6FE),
                    onClick = onOpenPilihanAksesOrtu,
                    testTag = "tile_tabungan_akses_ortu",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // 4. Daftar Saldo Anak Binaan (Klik untuk Pop-Up Detail Pemasukan, Pengeluaran & Log)
    item(key = "hub_tabungan_students_hdr") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "My Foster Students Balance (Tap for Detail)" else "Saldo Anak Binaan Saya (Ketuk untuk Detail)",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (displayedStudents.isNotEmpty()) {
                Text(
                    text = "${displayedStudents.size} ${if (isEn) "Kids" else "Anak"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )
            }
        }
    }

    if (displayedStudents.isEmpty()) {
        item(key = "hub_tabungan_empty_claimed") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_tabungan_no_claimed"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isEn) "No Claimed Foster Students Yet" else "Belum Ada Anak Asuh Binaan Dipilih",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEn) {
                            "Tabungan strictly lists students you claimed under Parent Connect → Select Foster Kids."
                        } else {
                            "Menu Tabungan hanya menampilkan anak asuh yang sudah Anda klaim di Hubungan Ortu → Pilih Anak Asuh."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenPilihAnakAsuh,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_tabungan_open_pilih_anak")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Select Foster Students Now" else "Pilih Anak Asuh (Hubungan Ortu)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else {
        items(displayedStudents, key = { "hub_tab_st_${it.id}" }) { student ->
            val stSummary = TabunganHelper.computeSummaryForStudent(transactions, student)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenStudentDetail(student) }
                    .testTag("card_tabungan_student_${student.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (stSummary.balance > 0L) Color(0xFFFCD34D) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = student.name,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isAnakSuperAdmin && student.id !in claimedIds && student.assignedWaliName.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color(0xFFE0E7FF)
                                ) {
                                    Text(
                                        text = student.assignedWaliName,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4338CA),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "+${TabunganHelper.formatRupiah(stSummary.totalIn)}  •  -${TabunganHelper.formatRupiah(stSummary.totalOut)}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (stSummary.balance > 0L) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = TabunganHelper.formatRupiah(stSummary.balance),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (stSummary.balance > 0L) Color(0xFFB45309) else Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (stSummary.activeCount > 0) {
                            Text(
                                text = "${stSummary.activeCount} ${if (isEn) "tx" else "mutasi"}",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun LazyListScope.konselingCategoryItems(
    isEn: Boolean,
    claimedStudents: List<ChildStudent>,
    records: List<KonselingRecord>,
    logs: List<KonselingLog>,
    onOpenInputKonseling: (String, ChildStudent?) -> Unit,
    onOpenFollowUpSheet: () -> Unit,
    onOpenStudentDetail: (ChildStudent) -> Unit,
    onOpenHistoryLogs: (Int) -> Unit,
    onOpenGuideDialog: () -> Unit,
    onOpenPdfSummary: () -> Unit,
    onOpenPilihAnakAsuh: () -> Unit
) {
    val agg = KonselingHelper.computeAggregateStats(records)
    val displayedStudents = claimedStudents
        .distinctBy { it.id }
        .sortedBy { it.name.lowercase() }

    // 1. Deklarasi Kerahasiaan Mutlak (HANYA dilihat Wali Asuh bersangkutan)
    item(key = "hub_konseling_privacy") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_konseling_privacy_declaration"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
            border = BorderStroke(1.dp, Color(0xFFDDD6FE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDE9FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = if (isEn) "Confidential Counseling" else "Kerahasiaan Konseling",
                        tint = Color(0xFF6D28D9),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF6D28D9)
                    ) {
                        Text(
                            text = if (isEn) {
                                "CONFIDENTIAL • OWNER WALI ASUH ONLY"
                            } else {
                                "RAHASIA • HANYA DILIHAT WALI ASUH BERSANGKUTAN"
                            },
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = if (isEn) {
                            "Every counseling and student growth note here is strictly CONFIDENTIAL: only visible to you as the owning Foster Caregiver. School Admins, Guru Ngaji, other Wali Asuh, and Parents cannot view these notes."
                        } else {
                            "Catatan konseling & perkembangan anak ini bersifat RAHASIA MUTLAK: hanya dapat dilihat oleh Anda sebagai Wali Asuh bersangkutan. Admin Sekolah, Guru Ngaji, Wali Asuh lain, maupun Orang Tua tidak memiliki akses ke catatan ini."
                        },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF4C1D95)
                    )
                }
            }
        }
    }

    // 2. Ringkasan Statistik Konseling
    item(key = "hub_konseling_totals") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_konseling_totals"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HubStatTile("${agg.totalEntries}", if (isEn) "Notes" else "Total Catatan", Color(0xFFEDE9FE), Color(0xFF5B21B6), Modifier.weight(1f))
                HubStatTile("${agg.followUpCount}", if (isEn) "Monitor" else "Perlu Pantau", Color(0xFFFEF3C7), Color(0xFFB45309), Modifier.weight(1f))
                HubStatTile("${agg.highPriorityCount}", if (isEn) "Priority" else "Prioritas", Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
                HubStatTile("${agg.growthCount}", if (isEn) "Growth" else "Tumbuh", Color(0xFFDCFCE7), Color(0xFF15803D), Modifier.weight(1f))
            }
        }
    }

    // 3. 6 Sub-Menu Ikon Konseling Lengkap
    item(key = "hub_konseling_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Log Session" else "Catat Sesi",
                    subtitle = if (isEn) "1-on-1 Confidential Note" else "Sesi Curhat & Bimbingan",
                    imageRes = R.drawable.img_menu_konseling_catat,
                    badgeText = if (isEn) "Private" else "Sesi Baru",
                    badgeBg = Color(0xFFEDE9FE),
                    badgeColor = Color(0xFF5B21B6),
                    bgGradient = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE).copy(alpha = 0.65f)),
                    borderColor = Color(0xFFDDD6FE),
                    onClick = { onOpenInputKonseling(KonselingHelper.ENTRY_SESI, null) },
                    testTag = "tile_konseling_catat",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Follow-Up" else "Pantauan & Jadwal",
                    subtitle = if (isEn) "Active Care Monitoring" else "Tindak Lanjut & Prioritas",
                    imageRes = R.drawable.img_menu_konseling_followup,
                    badgeText = "${agg.followUpCount} ${if (isEn) "Active" else "Pantau"}",
                    badgeBg = if (agg.highPriorityCount > 0) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                    badgeColor = if (agg.highPriorityCount > 0) Color(0xFFDC2626) else Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenFollowUpSheet,
                    testTag = "tile_konseling_followup",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Growth Note" else "Perkembangan Anak",
                    subtitle = if (isEn) "Positive Shifts & Mood" else "Progres Positif & Apresiasi",
                    imageRes = R.drawable.img_menu_konseling_perkembangan,
                    badgeText = "${agg.growthCount} ${if (isEn) "Growth" else "Progres"}",
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF15803D),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFA7F3D0),
                    onClick = { onOpenInputKonseling(KonselingHelper.ENTRY_PERKEMBANGAN, null) },
                    testTag = "tile_konseling_perkembangan",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "History & Audit" else "Riwayat & Log Audit",
                    subtitle = if (isEn) "Full Private Timeline" else "Semua Catatan & Riwayat",
                    imageRes = R.drawable.img_menu_konseling,
                    badgeText = "${logs.size} Log",
                    badgeBg = Color(0xFFE0E7FF),
                    badgeColor = Color(0xFF4338CA),
                    bgGradient = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFC7D2FE),
                    onClick = { onOpenHistoryLogs(0) },
                    testTag = "tile_konseling_riwayat",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Ethics & Guide" else "Panduan & Etika",
                    subtitle = if (isEn) "Empathetic Mentoring" else "SOP Konseling & Kerahasiaan",
                    imageRes = R.drawable.img_menu_konseling_panduan,
                    badgeText = if (isEn) "Guide" else "Panduan",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = Color(0xFF0369A1),
                    bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFBAE6FD),
                    onClick = onOpenGuideDialog,
                    testTag = "tile_konseling_panduan",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Print Recap PDF" else "Cetak Rekap PDF",
                    subtitle = if (isEn) "Confidential Dossier" else "Laporan Rahasia Wali Asuh",
                    imageRes = R.drawable.img_menu_konseling_pdf,
                    badgeText = "PDF",
                    badgeBg = Color(0xFFCCFBF1),
                    badgeColor = Color(0xFF0F766E),
                    bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.6f)),
                    borderColor = Color(0xFF99F6E4),
                    onClick = onOpenPdfSummary,
                    testTag = "tile_konseling_pdf",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // 4. Daftar Anak Binaan Saya (Hanya Anak Binaan Milik Wali Asuh Ini)
    item(key = "hub_konseling_students_hdr") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "My Foster Students (Tap for Counseling File)" else "Anak Binaan Saya (Ketuk untuk Catatan Rahasia)",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (displayedStudents.isNotEmpty()) {
                Text(
                    text = "${displayedStudents.size} ${if (isEn) "Kids" else "Anak"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6D28D9)
                )
            }
        }
    }

    if (displayedStudents.isEmpty()) {
        item(key = "hub_konseling_empty_claimed") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_konseling_no_claimed"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color(0xFF6D28D9),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isEn) "No Claimed Foster Students Yet" else "Belum Ada Anak Asuh Binaan Dipilih",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEn) {
                            "Counseling strictly lists your own claimed foster students under Parent Connect → Select Foster Kids."
                        } else {
                            "Menu Konseling hanya menampilkan anak asuh binaan Anda yang sudah dipilih di Hubungan Ortu → Pilih Anak Asuh."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenPilihAnakAsuh,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_konseling_open_pilih_anak")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Select Foster Students Now" else "Pilih Anak Asuh (Hubungan Ortu)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else {
        items(displayedStudents, key = { "hub_kon_st_${it.id}" }) { student ->
            val stStats = KonselingHelper.computeStudentStats(records, student)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenStudentDetail(student) }
                    .testTag("card_konseling_student_${student.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    when {
                        stStats.highPriorityCount > 0 -> Color(0xFFFCA5A5)
                        stStats.followUpCount > 0 -> Color(0xFFDDD6FE)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = student.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val latest = stStats.latestRecord
                        Text(
                            text = if (latest != null) {
                                "${KonselingHelper.formatTopicLabel(latest.topic, isEn)}: ${latest.title}"
                            } else {
                                if (isEn) "No confidential counseling notes yet" else "Belum ada catatan konseling rahasia"
                            },
                            fontSize = 10.5.sp,
                            color = if (latest != null) Color(0xFF5B21B6) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                stStats.highPriorityCount > 0 -> Color(0xFFFEE2E2)
                                stStats.followUpCount > 0 -> Color(0xFFEDE9FE)
                                stStats.totalEntries > 0 -> Color(0xFFDCFCE7)
                                else -> Color(0xFFF1F5F9)
                            }
                        ) {
                            Text(
                                text = when {
                                    stStats.highPriorityCount > 0 -> "${stStats.highPriorityCount} ${if (isEn) "Priority" else "Prioritas"}"
                                    stStats.followUpCount > 0 -> "${stStats.followUpCount} ${if (isEn) "Monitor" else "Pantau"}"
                                    stStats.totalEntries > 0 -> "${stStats.totalEntries} ${if (isEn) "Notes" else "Catatan"}"
                                    else -> if (isEn) "New" else "Baru"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    stStats.highPriorityCount > 0 -> Color(0xFFDC2626)
                                    stStats.followUpCount > 0 -> Color(0xFF6D28D9)
                                    stStats.totalEntries > 0 -> Color(0xFF15803D)
                                    else -> Color(0xFF64748B)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (stStats.growthCount > 0) {
                            Text(
                                text = "${stStats.growthCount} ${if (isEn) "growth" else "perkembangan"}",
                                fontSize = 9.5.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.jadwalWaliCategoryItems(
    isEn: Boolean,
    schoolProfile: SchoolProfile,
    monthKey: String,
    selectedDay: Int,
    shiftCodes: List<WaliShiftCode>,
    monthEntries: List<WaliScheduleEntry>,
    canManageSchedule: Boolean,
    isAnakAdminSekolah: Boolean = false,
    scheduleWaliAccounts: List<com.example.data.model.WaliAccount> = emptyList(),
    currentUsername: String?,
    currentWaliName: String,
    searchQuery: String,
    selectedGroup: String,
    onSearchChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
    onChangeMonth: (String) -> Unit,
    onChangeDay: (Int) -> Unit,
    onOpenMatriks: () -> Unit,
    onOpenPersonal: (WaliScheduleEntry) -> Unit,
    onOpenPetugasJamIni: () -> Unit,
    onOpenRekapIzin: () -> Unit,
    onOpenKodeSop: () -> Unit,
    onOpenPinTugas: () -> Unit,
    onOpenManageShiftCodes: () -> Unit,
    onOpenImportMatriks: () -> Unit,
    onOpenUploadCsv: () -> Unit = onOpenImportMatriks,
    onEditCell: (WaliScheduleEntry, Int) -> Unit,
    onShareDailyWhatsapp: () -> Unit,
    onExportMatriksPdf: () -> Unit
) {
    val effectiveCodes = shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
    val shiftMap = WaliScheduleHelper.buildShiftMap(effectiveCodes, schoolProfile.tenantCode)
    val daysInMonth = WaliScheduleHelper.daysInMonth(monthKey)
    val safeDay = selectedDay.coerceIn(1, daysInMonth)

    val leavePeersToday = monthEntries.sortedBy { it.sortOrder }.mapNotNull { entry ->
        val info = WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson)[safeDay] ?: return@mapNotNull null
        entry to info
    }
    val myEntry = monthEntries.firstOrNull { WaliScheduleHelper.matchesWaliEntry(it, currentUsername, currentWaliName) }
    val dayPinsTotal = monthEntries.sumOf { WaliScheduleHelper.parseDayPins(it.dayPinsJson)[safeDay]?.size ?: 0 }
    val currentHour = WaliScheduleHelper.currentHourOfDay()
    val activeNowCount = monthEntries.count { entry ->
        val code = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[safeDay]?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
        val def = shiftMap[code] ?: return@count false
        WaliScheduleHelper.isShiftActiveAtHour(def, currentHour)
    }

    // 1. Rekan Izin Strip + Link Drive + Rekap Izin (match reference layout)
    if (leavePeersToday.isNotEmpty()) {
        item(key = "jadwal_leave_peers_banner") {
            val context = LocalContext.current
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFF1F2),
                border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_jadwal_rekan_izin")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isEn) {
                                "Colleagues on Leave (${leavePeersToday.size}):"
                            } else {
                                "Rekan Izin Hari Ini (${leavePeersToday.size}):"
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9F1239)
                        )
                    }
                    leavePeersToday.forEach { (peerEntry, leaveInfo) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = peerEntry.waliName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEE2E2)) {
                                        Text(
                                            text = leaveInfo.status,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (leaveInfo.driveUrl.isNotBlank()) {
                                    Button(
                                        onClick = { openJadwalExternalUrl(context, leaveInfo.driveUrl, isEn) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .heightIn(min = 36.dp)
                                            .testTag("btn_buka_link_drive_${peerEntry.id}")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isEn) "Open Drive" else "Buka Link Drive", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        onClick = onOpenRekapIzin,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("btn_rekap_izin_jadwal")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEn) "Leave Recap" else "Rekap Izin", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 2. Penugasan Personal Hero Card (Tanpa tombol Alarm / Notifikasi — sesuai instruksi Point 4)
    item(key = "jadwal_personal_hero") {
        val myCodes = rememberLikeMap(myEntry?.dayCodesJson)
        val myPins = myEntry?.let { WaliScheduleHelper.parseDayPins(it.dayPinsJson)[safeDay].orEmpty() }.orEmpty()
        val myCode = myCodes[safeDay]?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
        val myShift = shiftMap[myCode]
        val nextDay = if (safeDay < daysInMonth) safeDay + 1 else null
        val tomorrowCode = nextDay?.let { myCodes[it]?.let { raw -> WaliScheduleHelper.normalizeCode(raw) } }.orEmpty()
        val tomorrowShift = shiftMap[tomorrowCode]

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_jadwal_personal_hero")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F766E),
                                Color(0xFF0369A1),
                                Color(0xFF312E81)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = if (myEntry != null) {
                                if (isEn) "Assignment: ${myEntry.waliName}" else "Penugasan Wali Asuh: ${myEntry.waliName}"
                            } else {
                                if (isEn) "School Roster Monitor • ${schoolProfile.shortName}" else "Pemantauan Jadwal • ${schoolProfile.shortName}"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (myEntry != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when {
                                    myShift != null -> "${myShift.name} (${myShift.code})"
                                    myCode.isNotBlank() -> myCode
                                    else -> if (isEn) "Unassigned on Day $safeDay" else "Belum Ada Jadwal Tgl $safeDay"
                                },
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (myCode.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = parseJadwalShiftColor(myShift?.colorHex ?: "#7C3AED")
                                ) {
                                    Text(
                                        text = "${if (isEn) "Code" else "Kode"}: $myCode",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (myPins.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                myPins.forEach { pin ->
                                    SchedulePinChip(pin = pin)
                                }
                            }
                        }

                        if (myShift?.sopNotes?.isNotBlank() == true) {
                            Text(
                                text = myShift.sopNotes,
                                fontSize = 12.sp,
                                lineHeight = 16.5.sp,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (myShift != null && myShift.timeRange.isNotBlank() && myShift.timeRange != "-") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "⏱ ${if (isEn) "Hours" else "Jam"}: ${myShift.timeRange} (${myShift.workHours} ${if (isEn) "Hrs" else "Jam"})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDE047),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (nextDay != null && tomorrowCode.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "📅 ${if (isEn) "Tomorrow (Day $nextDay)" else "Besok (Tgl $nextDay)"}: ${tomorrowShift?.name ?: tomorrowCode} ($tomorrowCode)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.95f),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isEn) {
                                "$activeNowCount caregivers active now (${String.format("%02d:00", currentHour)} WIB) • ${monthEntries.size} listed"
                            } else {
                                "$activeNowCount petugas aktif jam ini (${String.format("%02d:00", currentHour)} WIB) • ${monthEntries.size} personel terdaftar"
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenPetugasJamIni,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.22f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_jadwal_petugas_jam_ini")
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEn) "On Duty Now" else "Petugas Jam Ini", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (myEntry != null) {
                            Button(
                                onClick = { onOpenPersonal(myEntry) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.22f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_jadwal_personal_hero")
                            ) {
                                Text(if (isEn) "My Schedule" else "Jadwal Personal →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = onOpenMatriks,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.22f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(if (isEn) "View Matrix →" else "Matriks Bulan →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. 2x2 Quick Hub Menu Cards (Matriks Bulanan, Pin Tugas, Petugas Jam Ini, Kode & SOP)
    item(key = "jadwal_hub_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Monthly Matrix" else "Matriks Bulanan",
                    subtitle = WaliScheduleHelper.formatMonthTitle(monthKey, isEn),
                    imageRes = R.drawable.img_jadwal_matriks,
                    badgeText = "${monthEntries.size} ${if (isEn) "Staff" else "Org"}",
                    badgeBg = Color(0xFFCCFBF1),
                    badgeColor = Color(0xFF0F766E),
                    bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.55f)),
                    borderColor = Color(0xFF99F6E4),
                    onClick = onOpenMatriks,
                    testTag = "card_jadwal_matriks",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Task Pins" else "Penyematan Tugas",
                    subtitle = if (isEn) "Tasks & Facilities" else "Pin Tugas & Faskes",
                    imageRes = R.drawable.img_menu_pesan_shift,
                    badgeText = "$dayPinsTotal Pin",
                    badgeBg = Color(0xFFE0E7FF),
                    badgeColor = Color(0xFF3730A3),
                    bgGradient = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFC7D2FE),
                    onClick = onOpenPinTugas,
                    testTag = "card_jadwal_pin_tugas",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "On Duty Hour" else "Petugas Jam Ini",
                    subtitle = String.format("%02d:00 WIB", currentHour),
                    imageRes = R.drawable.img_menu_jadwal,
                    badgeText = "$activeNowCount ${if (isEn) "Active" else "Aktif"}",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = Color(0xFF0369A1),
                    bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFBAE6FD),
                    onClick = onOpenPetugasJamIni,
                    testTag = "card_jadwal_jam_ini",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Shift Codes & SOP" else "Kode & SOP Shift",
                    subtitle = if (isEn) "Rules per Shift" else "Aturan & Zona Tugas",
                    imageRes = R.drawable.img_menu_kunjungan,
                    badgeText = "${effectiveCodes.size} Kode",
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.55f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenKodeSop,
                    testTag = "card_jadwal_kode_sop",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // 4. Share WA + Export PDF + Admin Controls (Admin Sekolah only for mutations)
    item(key = "jadwal_actions_and_admin_bar") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShareDailyWhatsapp,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("btn_jadwal_share_wa")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEn) "Share Roster WA" else "Bagikan WA", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onExportMatriksPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("btn_jadwal_export_pdf")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEn) "Matrix PDF" else "Cetak PDF Resmi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (canManageSchedule) {
                val context = LocalContext.current
                val downloadCsvLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/csv")
                ) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val csvContent = WaliScheduleHelper.buildScheduleTemplateCsv(
                                monthKey = monthKey,
                                existingEntries = monthEntries,
                                waliAccounts = scheduleWaliAccounts,
                                includeExistingShifts = true
                            )
                            context.contentResolver.openOutputStream(uri)?.use { os ->
                                os.write("\uFEFF$csvContent".toByteArray(Charsets.UTF_8))
                            }
                            Toast.makeText(
                                context,
                                if (isEn) {
                                    "Schedule CSV template downloaded (${WaliScheduleHelper.formatMonthTitle(monthKey, true)})!"
                                } else {
                                    "Template CSV jadwal berhasil diunduh (${WaliScheduleHelper.formatMonthTitle(monthKey, false)})!"
                                },
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                if (isEn) "Failed to download CSV template: ${e.message}"
                                else "Gagal mengunduh template CSV: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDFA),
                    border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_jadwal_admin_controls")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isAnakAdminSekolah) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.testTag("badge_anak_admin_jadwal_mode")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color(0xFF4338CA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isEn) {
                                        "Schedule Sub-Admin Active • Tap any row/cell to edit shifts & tasks, or use CSV"
                                    } else {
                                        "Mode Anak Admin Jadwal • Klik wali untuk ubah shift/pin tugas, atau kelola CSV"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF312E81)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onOpenManageShiftCodes,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_jadwal_admin_shift_codes")
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Shift Codes" else "Atur Kode Shift", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onOpenImportMatriks,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_jadwal_admin_import")
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Members / Pattern" else "Anggota / Pola", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val safeCode = schoolProfile.tenantCode.ifBlank { "SRT1" }
                                        .replace(Regex("[^A-Za-z0-9_-]"), "_")
                                    downloadCsvLauncher.launch("Template_Jadwal_Wali_${safeCode}_${monthKey}.csv")
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_jadwal_hub_download_csv")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF0F766E))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isEn) "Download CSV" else "Template CSV",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }
                            Button(
                                onClick = onOpenUploadCsv,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_jadwal_hub_upload_csv")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isEn) "Upload CSV" else "Upload CSV",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Date Switcher + Filter Chips + Search Bar (Rapat)
    item(key = "jadwal_date_and_filter_controls") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (safeDay > 1) {
                                onChangeDay(safeDay - 1)
                            } else {
                                val prevMonth = WaliScheduleHelper.shiftMonth(monthKey, -1)
                                onChangeMonth(prevMonth)
                                onChangeDay(WaliScheduleHelper.daysInMonth(prevMonth))
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_jadwal_prev_day")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = if (isEn) "Previous Day" else "Hari Sebelumnya",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = WaliScheduleHelper.formatDateChipLabel(monthKey, safeDay, isEn),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F766E),
                            modifier = Modifier.testTag("text_jadwal_selected_date")
                        )
                        Text(
                            text = if (canManageSchedule) {
                                if (isEn) "Tap any person to edit shift & pins" else "Ketuk nama wali untuk atur shift & pin tugas"
                            } else {
                                if (isEn) "Tap any person for monthly schedule" else "Ketuk nama wali untuk lihat jadwal personal"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            if (safeDay < daysInMonth) {
                                onChangeDay(safeDay + 1)
                            } else {
                                val nextMonth = WaliScheduleHelper.shiftMonth(monthKey, 1)
                                onChangeMonth(nextMonth)
                                onChangeDay(1)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_jadwal_next_day")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = if (isEn) "Next Day" else "Hari Berikutnya",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(if (isEn) "Search caregiver or pinned task..." else "Cari nama Wali Asuh atau tugas pin...", fontSize = 11.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(17.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_jadwal_hub_search")
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                val filterChips = listOf(
                    "ALL" to if (isEn) "All" else "Semua",
                    WaliScheduleHelper.GROUP_PAGI to if (isEn) "Morning" else "Pagi",
                    WaliScheduleHelper.GROUP_SORE to if (isEn) "Afternoon" else "Sore",
                    WaliScheduleHelper.GROUP_MALAM to if (isEn) "Night" else "Malam",
                    WaliScheduleHelper.GROUP_LEPAS to if (isEn) "Off / Rest" else "Lepas & Libur",
                    WaliScheduleHelper.GROUP_IZIN to if (isEn) "Leave" else "Izin & Cuti"
                )
                items(filterChips) { (key, lbl) ->
                    val selected = selectedGroup == key
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) Color(0xFF0F766E) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) Color(0xFF0F766E) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.clickable { onGroupChange(key) }
                    ) {
                        Text(
                            text = lbl,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }

    // 6. Daily Roster Grouped Cards (Jaga Pagi, Jaga Sore, Jaga Malam, Lepas & Libur, Izin & Cuti)
    if (monthEntries.isEmpty()) {
        item(key = "jadwal_empty_state") {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isEn) {
                            "No roster created for ${WaliScheduleHelper.formatMonthTitle(monthKey, true)} yet."
                        } else {
                            "Jadwal Wali Asuh bulan ${WaliScheduleHelper.formatMonthTitle(monthKey, false)} belum dibuat oleh Admin Sekolah."
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (canManageSchedule) {
                        Button(
                            onClick = onOpenImportMatriks,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(if (isEn) "Setup / Sync Wali Accounts" else "Atur / Sinkron Akun Wali")
                        }
                    }
                }
            }
        }
    } else {
        val q = searchQuery.trim().lowercase()
        val groupSpecs = listOf(
            Triple(WaliScheduleHelper.GROUP_PAGI, if (isEn) "Morning Shift (P1-P5)" else "Jaga Pagi (P1-P5)", "07:00 - 15:00/16:00 (P4 s.d 20:00)"),
            Triple(WaliScheduleHelper.GROUP_SORE, if (isEn) "Afternoon Shift (S)" else "Jaga Sore (S)", "15:00 - 23:00"),
            Triple(WaliScheduleHelper.GROUP_MALAM, if (isEn) "Night Shift (M)" else "Jaga Malam (M)", "15:00 - 07:00 (Pagi)"),
            Triple(WaliScheduleHelper.GROUP_LEPAS, if (isEn) "Post-Night & Off (LP/L)" else "Lepas & Libur", if (isEn) "Rest / Off" else "Istirahat / Off"),
            Triple(WaliScheduleHelper.GROUP_IZIN, if (isEn) "Leave & Sick (IZIN/C)" else "Izin & Cuti", if (isEn) "Official Leave" else "Izin / Sakit / Cuti")
        )

        groupSpecs.forEach { (grpKey, grpTitle, grpTime) ->
            if (selectedGroup == "ALL" || selectedGroup == grpKey) {
                val matchedMembers = monthEntries.sortedBy { it.sortOrder }.mapNotNull { entry ->
                    val code = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[safeDay]?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
                    val resolvedGrp = if (code.isBlank()) "" else WaliScheduleHelper.resolveShiftGroup(code, shiftMap)
                    if (resolvedGrp != grpKey) return@mapNotNull null
                    val pins = WaliScheduleHelper.parseDayPins(entry.dayPinsJson)[safeDay].orEmpty()
                    val leave = WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson)[safeDay]
                    val matchesSearch = q.isBlank() ||
                        entry.waliName.lowercase().contains(q) ||
                        code.lowercase().contains(q) ||
                        pins.any { it.label.lowercase().contains(q) } ||
                        (shiftMap[code]?.zoneLabel?.lowercase()?.contains(q) == true)
                    if (!matchesSearch) return@mapNotNull null
                    Triple(entry, code, pins to leave)
                }

                if (matchedMembers.isNotEmpty()) {
                    item(key = "jadwal_group_$grpKey") {
                        DailyRosterGroupCard(
                            groupKey = grpKey,
                            title = grpTitle,
                            timeSubtitle = grpTime,
                            members = matchedMembers,
                            shiftMap = shiftMap,
                            canManageSchedule = canManageSchedule,
                            isEn = isEn,
                            onMemberClick = { entry ->
                                if (canManageSchedule) onEditCell(entry, safeDay)
                                else onOpenPersonal(entry)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun rememberLikeMap(dayCodesJson: String?): Map<Int, String> =
    WaliScheduleHelper.parseDayCodes(dayCodesJson)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyRosterGroupCard(
    groupKey: String,
    title: String,
    timeSubtitle: String,
    members: List<Triple<WaliScheduleEntry, String, Pair<List<PinnedTaskItem>, LeaveDayInfo?>>>,
    shiftMap: Map<String, WaliShiftCode>,
    canManageSchedule: Boolean,
    isEn: Boolean,
    onMemberClick: (WaliScheduleEntry) -> Unit
) {
    val context = LocalContext.current
    val innerScrollState = rememberScrollState()
    val (accent, borderColor, iconVector) = when (groupKey) {
        WaliScheduleHelper.GROUP_PAGI -> Triple(Color(0xFF0284C7), Color(0xFFBAE6FD), Icons.Default.WbSunny)
        WaliScheduleHelper.GROUP_SORE -> Triple(Color(0xFFD97706), Color(0xFFFDE68A), Icons.Default.WbTwilight)
        WaliScheduleHelper.GROUP_MALAM -> Triple(Color(0xFF6D28D9), Color(0xFFDDD6FE), Icons.Default.NightsStay)
        WaliScheduleHelper.GROUP_LEPAS -> Triple(Color(0xFF0F766E), Color(0xFF99F6E4), Icons.Default.Coffee)
        else -> Triple(Color(0xFFBE123C), Color(0xFFFECDD3), Icons.Default.WarningAmber)
    }

    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, borderColor, RoundedCornerShape(14.dp))
            .testTag("card_jadwal_group_$groupKey")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Strip Ringkas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accent.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.16f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(iconVector, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = timeSubtitle,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (members.size > 5) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            border = BorderStroke(0.7.dp, borderColor)
                        ) {
                            Text(
                                text = if (isEn) "↕ Scroll" else "↕ Gulir",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${members.size} Org",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }

            // Frame Dalam Berbatas Tinggi dengan Scroll Mandiri untuk Daftar Nama Wali Asuh
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, borderColor.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 216.dp)
                        .verticalScroll(innerScrollState)
                        .testTag("frame_jadwal_group_scroll_$groupKey")
                ) {
                    members.forEachIndexed { index, (entry, code, pinsAndLeave) ->
                        val (pins, leave) = pinsAndLeave
                        val shift = shiftMap[code]
                        val codeColor = parseJadwalShiftColor(shift?.colorHex ?: "#64748B", accent)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMemberClick(entry) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("row_jadwal_member_${entry.id}"),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = entry.waliName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    val zone = shift?.zoneLabel.orEmpty()
                                    if (zone.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = accent.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = "($zone)",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = accent,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (canManageSchedule) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = if (isEn) "Edit" else "Atur",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = codeColor) {
                                        Text(
                                            text = code,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                        )
                                    }
                                }
                            }

                            if (pins.isNotEmpty() || leave != null) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    pins.forEach { pin ->
                                        SchedulePinChip(pin = pin)
                                    }
                                    if (leave != null && leave.driveUrl.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFDCFCE7),
                                            border = BorderStroke(0.8.dp, Color(0xFF86EFAC)),
                                            modifier = Modifier.clickable {
                                                openJadwalExternalUrl(context, leave.driveUrl, isEn)
                                            }
                                        ) {
                                            Text(
                                                text = "📄 Drive ${leave.status}",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF166534),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (index < members.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.6.dp,
                                color = borderColor.copy(alpha = 0.65f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun LazyListScope.sholatCategoryItems(
    isEn: Boolean,
    allStudents: List<ChildStudent>,
    claimedStudents: List<ChildStudent>,
    sholatRecords: List<SholatRecord>,
    haidRecords: List<HaidRecord>,
    onOpenAbsenSholatSheet: (tabIndex: Int, autoScanner: Boolean, scopeFilter: String, targetStudent: ChildStudent?) -> Unit,
    onOpenPilihAnakAsuh: () -> Unit,
    onOpenPilihanAksesOrtu: () -> Unit
) {
    val todayKey = SholatHelper.todayDateKey()
    val activeWaktu = SholatHelper.detectActiveWaktu()
    val dailyMatrix = SholatHelper.buildDailyMatrix(sholatRecords, todayKey)

    val displayedStudents = claimedStudents
        .distinctBy { it.id }
        .sortedBy { it.name.lowercase() }

    val activeWaktuRecords = allStudents.mapNotNull { st -> dailyMatrix[st.id]?.get(activeWaktu) }
    val activeJamaah = activeWaktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH }
    val activeMunfarid = activeWaktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.MUNFARID }
    val activeUdzurIzin = activeWaktuRecords.count {
        val status = SholatStatus.fromCode(it.status)
        status == SholatStatus.UDZUR_HAID || status == SholatStatus.SAKIT || status == SholatStatus.IZIN
    }
    val activeAlfa = activeWaktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.ALFA }
    val activeBelum = (allStudents.size - activeWaktuRecords.size).coerceAtLeast(0)
    val activeLoggedTotal = activeWaktuRecords.size
    val activeUdzurHaidCount = activeWaktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.UDZUR_HAID }
    val activeWajibDenom = (activeLoggedTotal - activeUdzurHaidCount).coerceAtLeast(0)
    val activePct = if (activeWajibDenom > 0) {
        ((activeJamaah * 100f) / activeWajibDenom).toInt().coerceIn(0, 100)
    } else {
        0
    }

    // 1. Kartu Info Sinkronisasi Orang Tua & Auto-Haid (Senada dengan Kartu Info Tabungan)
    item(key = "hub_sholat_info") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_sholat_hub_info"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = if (isEn) "Sholat Tracking Sync" else "Sinkronisasi Sholat",
                        tint = Color(0xFF047857),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF059669)
                        ) {
                            Text(
                                text = if (isEn) "AUTO-HAID & PARENT SYNC" else "AUTO-HAID & TERHUBUNG ORTU",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F766E)
                        ) {
                            Text(
                                text = activeWaktu.label(isEn).uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isEn) {
                            "5 daily prayer logs sync to the Parent Portal (when enabled in Parent Access). Female students currently on active Haid are automatically marked Udzur Haid."
                        } else {
                            "Absensi sholat 5 waktu otomatis tersinkron ke Portal Orang Tua (bila dicentang di Izin Akses Ortu). Siswi yang sedang masa haid otomatis ditandai Udzur Haid."
                        },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF065F46)
                    )
                }
            }
        }
    }

    // 2. Ringkasan Capaian Sholat Hari Ini (Senada dengan Kartu Total Saldo Tabungan)
    item(key = "hub_sholat_totals") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_sholat_totals"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEn) {
                                "Congregation Rate — ${activeWaktu.labelEn}"
                            } else {
                                "Capaian Jamaah — ${activeWaktu.labelId}"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (activeLoggedTotal > 0) "$activePct% ($activeJamaah/$activeLoggedTotal)" else "0% (0/${allStudents.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        modifier = Modifier.clickable {
                            onOpenAbsenSholatSheet(0, false, "ALL", null)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${activeLoggedTotal}/${allStudents.size} ${if (isEn) "Logged" else "Terabsen"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HubStatTile(
                        value = "$activeJamaah",
                        label = if (isEn) "Jamaah" else "Jamaah",
                        bg = Color(0xFFDCFCE7),
                        fg = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = "${activeUdzurIzin + activeMunfarid}",
                        label = if (isEn) "Udzur / Excused" else "Udzur / Izin",
                        bg = Color(0xFFFEF3C7),
                        fg = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = if (activeAlfa > 0) "$activeAlfa / $activeBelum" else "$activeBelum",
                        label = if (activeAlfa > 0) {
                            if (isEn) "Alfa / Unmarked" else "Alfa / Belum"
                        } else {
                            if (isEn) "Unmarked" else "Belum Absen"
                        },
                        bg = if (activeAlfa > 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                        fg = if (activeAlfa > 0) Color(0xFFDC2626) else Color(0xFF475569),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Strip ringkasan 5 waktu hari ini
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SholatWaktu.entries.forEach { waktu ->
                        val wCount = allStudents.count { st -> dailyMatrix[st.id]?.get(waktu) != null }
                        val isCurrent = waktu == activeWaktu
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) Color(0xFF059669) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) Color(0xFF047857) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenAbsenSholatSheet(0, false, "ALL", null) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = waktu.label(isEn),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.White else Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$wCount/${allStudents.size}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isCurrent) Color(0xFFD1FAE5) else Color(0xFF64748B),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Grid 2x3 Kartu Menu Kartun (Gaya Seragam dengan Tabungan Anak Asuh)
    item(key = "hub_sholat_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Quick Check" else "Absen Cepat",
                    subtitle = if (isEn) "5 Prayers & Bulk Jamaah" else "Tandai Jamaah & 5 Waktu",
                    imageRes = R.drawable.img_menu_sholat,
                    badgeText = activeWaktu.label(isEn),
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF15803D),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFA7F3D0),
                    onClick = { onOpenAbsenSholatSheet(0, false, "ALL", null) },
                    testTag = "tile_sholat_absen_cepat",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Scan Card QR" else "Scan Kartu QR",
                    subtitle = if (isEn) "Instant Barcode Check-in" else "Scan Barcode di Mushola",
                    imageRes = R.drawable.img_menu_input_puasa,
                    badgeText = "Scan QR",
                    badgeBg = Color(0xFFCCFBF1),
                    badgeColor = Color(0xFF0F766E),
                    bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.6f)),
                    borderColor = Color(0xFF99F6E4),
                    onClick = { onOpenAbsenSholatSheet(0, true, "ALL", null) },
                    testTag = "tile_sholat_scan_qr",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "My Foster Kids" else "Anak Binaan Saya",
                    subtitle = if (isEn) "Filter My Claimed Group" else "Absen Khusus Anak Binaan",
                    imageRes = R.drawable.img_menu_anak_asuh,
                    badgeText = "${displayedStudents.size} ${if (isEn) "Kids" else "Anak"}",
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = {
                        if (displayedStudents.isNotEmpty()) {
                            onOpenAbsenSholatSheet(0, false, "MINE", null)
                        } else {
                            onOpenPilihAnakAsuh()
                        }
                    },
                    testTag = "tile_sholat_anak_binaan",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Monitor & Praise" else "Pantauan & Pujian",
                    subtitle = if (isEn) "Front Row & Follow-up" else "Pejuang Subuh & Pembinaan",
                    imageRes = R.drawable.img_menu_ranking_puasa,
                    badgeText = if (isEn) "Analytics" else "Pantauan",
                    badgeBg = Color(0xFFE0E7FF),
                    badgeColor = Color(0xFF4338CA),
                    bgGradient = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFC7D2FE),
                    onClick = { onOpenAbsenSholatSheet(1, false, "ALL", null) },
                    testTag = "tile_sholat_pantauan",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Recap & Print PDF" else "Rekap & Cetak PDF",
                    subtitle = if (isEn) "Daily, Monthly & Card PDF" else "Laporan Harian & Bulanan",
                    imageRes = R.drawable.img_menu_daftar_puasa,
                    badgeText = "PDF",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = Color(0xFF0369A1),
                    bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFBAE6FD),
                    onClick = { onOpenAbsenSholatSheet(2, false, "ALL", null) },
                    testTag = "tile_sholat_rekap_pdf",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Parent Access" else "Izin Akses Ortu",
                    subtitle = if (isEn) "Toggle Prayer Visibility" else "Centang Akses Portal Ortu",
                    imageRes = R.drawable.img_menu_orang_tua,
                    badgeText = if (isEn) "Checkbox" else "Centang",
                    badgeBg = Color(0xFFEDE9FE),
                    badgeColor = Color(0xFF6D28D9),
                    bgGradient = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE).copy(alpha = 0.6f)),
                    borderColor = Color(0xFFDDD6FE),
                    onClick = onOpenPilihanAksesOrtu,
                    testTag = "tile_sholat_akses_ortu",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // 4. Daftar Sholat Anak Binaan Saya (Ketuk untuk Absen / Detail)
    item(key = "hub_sholat_students_hdr") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "My Foster Kids Today (Tap to Mark)" else "Sholat Anak Binaan Saya (Ketuk untuk Absen)",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (displayedStudents.isNotEmpty()) {
                Text(
                    text = "${displayedStudents.size} ${if (isEn) "Kids" else "Anak"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857)
                )
            }
        }
    }

    if (displayedStudents.isEmpty()) {
        item(key = "hub_sholat_empty_claimed") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_sholat_no_claimed"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color(0xFF047857),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isEn) "No Claimed Foster Students Yet" else "Belum Ada Anak Asuh Binaan Dipilih",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEn) {
                            "Claim your foster students in Parent Connect to show their daily 5-prayer checklist here, or open all school students."
                        } else {
                            "Klaim anak binaan Anda di Hubungan Ortu agar daftar sholat 5 waktunya langsung tampil di sini, atau buka semua siswa sekolah."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenPilihAnakAsuh,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_sholat_open_pilih_anak")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Select Foster Students Now" else "Pilih Anak Asuh (Hubungan Ortu)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (allStudents.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { onOpenAbsenSholatSheet(0, false, "ALL", null) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF047857)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_sholat_open_all_students")
                        ) {
                            Text(
                                text = if (isEn) "Open All Students (${allStudents.size})" else "Buka Absen Semua Siswa (${allStudents.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }
        }
    } else {
        items(displayedStudents, key = { "hub_sholat_st_${it.id}" }) { student ->
            val dayMap = dailyMatrix[student.id].orEmpty()
            val activeRecord = dayMap[activeWaktu]
            val activeStatus = activeRecord?.let { SholatStatus.fromCode(it.status) }
            val activeHaid = SholatHelper.findActiveHaidOnDate(student, haidRecords, todayKey)

            val (pillBg, pillFg, pillText) = when {
                activeStatus == SholatStatus.JAMAAH -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), if (isEn) "Jamaah" else "Jamaah")
                activeStatus == SholatStatus.MUNFARID -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "Munfarid")
                activeStatus == SholatStatus.UDZUR_HAID || activeHaid != null -> Triple(Color(0xFFFCE7F3), Color(0xFFBE185D), if (isEn) "Udzur Haid" else "Udzur Haid")
                activeStatus == SholatStatus.SAKIT -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), if (isEn) "Sick" else "Sakit")
                activeStatus == SholatStatus.IZIN -> Triple(Color(0xFFEDE9FE), Color(0xFF6D28D9), if (isEn) "Excused" else "Izin")
                activeStatus == SholatStatus.ALFA -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Alfa")
                else -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), if (isEn) "Unmarked" else "Belum")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAbsenSholatSheet(0, false, "MINE", student) }
                    .testTag("card_sholat_student_${student.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (activeStatus == SholatStatus.JAMAAH) Color(0xFF86EFAC)
                    else if (activeStatus == SholatStatus.ALFA) Color(0xFFFCA5A5)
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = student.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Mini status 5 waktu (S D A M I) hari ini
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            SholatWaktu.entries.forEach { waktu ->
                                val wRec = dayMap[waktu]
                                val wStatus = wRec?.let { SholatStatus.fromCode(it.status) }
                                val (dotBg, dotFg) = when (wStatus) {
                                    SholatStatus.JAMAAH -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                    SholatStatus.MUNFARID -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
                                    SholatStatus.UDZUR_HAID -> Color(0xFFFCE7F3) to Color(0xFFBE185D)
                                    SholatStatus.SAKIT -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                    SholatStatus.IZIN -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
                                    SholatStatus.ALFA -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                                    null -> Color(0xFFF1F5F9) to Color(0xFF94A3B8)
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = dotBg,
                                    border = if (waktu == activeWaktu) BorderStroke(1.dp, Color(0xFF047857)) else null
                                ) {
                                    Text(
                                        text = if (wStatus != null) "${waktu.shortCode}:${wStatus.shortCode}" else waktu.shortCode,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dotFg,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = pillBg
                        ) {
                            Text(
                                text = pillText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = pillFg,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = activeWaktu.label(isEn),
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

