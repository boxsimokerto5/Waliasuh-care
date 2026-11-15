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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentMessage
import com.example.data.model.ShiftMessage
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
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
    myFosterStudentsCount: Int,
    onOpenPilihAnakAsuh: () -> Unit,
    onOpenKartuAksesOrtu: () -> Unit,
    onOpenPilihanAksesOrtu: () -> Unit,
    onOpenPesanOrtu: () -> Unit
) {
    val unreadParentCount = parentMessages.count { !it.isReadByWali && it.senderRole.equals("PARENT", ignoreCase = true) }
    val cardsCount = parentAccessLinks.size
    val unclaimedCount = students.count { it.assignedWaliUsername.isBlank() && it.assignedWaliName.isBlank() }

    item(key = "hub_orang_tua_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Select Foster Kids" else "Pilih Anak Asuh",
                    subtitle = if (isEn) "Claim & Search Students" else "Cari & Klaim Binaan",
                    imageRes = R.drawable.img_menu_anak_asuh,
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
                    badgeText = if (cardsCount > 0) "$cardsCount ${if (isEn) "Cards" else "Kartu"}" else if (isEn) "Auto" else "Otomatis",
                    badgeBg = Color(0xFFEDE9FE),
                    badgeColor = Color(0xFF6D28D9),
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
                HubStatTile("$unclaimedCount", if (isEn) "Unassigned" else "Belum Wali", Color(0xFFFEF3C7), Color(0xFFB45309), Modifier.weight(1f))
                HubStatTile("$unreadParentCount", if (isEn) "Unread" else "Chat Baru", Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
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



