package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.example.R
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.ShiftMessage
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils

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
    onOpenRankingPuasa: () -> Unit
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeSession != null) {
                                "${activeSession.title} (${DateUtils.formatShortDate(activeSession.sessionDate, lang)})"
                            } else {
                                if (isEn) "No active Puasa session yet" else "Belum ada sesi puasa aktif"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = onOpenInputPuasa,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Scan Input" else "Scan Input", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
