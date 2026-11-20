package com.example.ui.components

import android.content.Intent
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.StudentReligionHelper
import com.example.util.DateUtils
import com.example.util.i18n.AppLanguage

internal fun LazyListScope.kristenCategoryItems(
    isEn: Boolean,
    lang: AppLanguage,
    allStudents: List<ChildStudent>,
    claimedStudents: List<ChildStudent>,
    christianWorshipRecords: List<ChristianWorshipRecord>,
    waliName: String,
    onOpenClaimChristianStudents: () -> Unit,
    onOpenInputChristianWorship: (initialTab: Int, defaultType: String, targetStudent: ChildStudent?) -> Unit,
    onOpenChristianReport: (initialTab: Int) -> Unit,
    onOpenBible: () -> Unit,
    onOpenSundayChurch: () -> Unit
) {
    val christianStudents = allStudents
        .filter { StudentReligionHelper.isChristian(it) }
        .distinctBy { it.id }
        .sortedBy { it.name.lowercase() }

    val activeRecords = christianWorshipRecords
        .filter { !it.isDeleted }
        .sortedWith(compareByDescending<ChristianWorshipRecord> { it.activityDate }.thenByDescending { it.createdAt })

    val todayKey = ChristianWorshipHelper.todayDateKey()
    val todayRecords = activeRecords.filter {
        it.dateKey == todayKey || DateUtils.isToday(it.activityDate)
    }
    val pagiCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_PAGI }
    val malamCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_MALAM }
    val gerejaCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA }

    // 1. Kartu Info Kebijakan & Sinkronisasi Otomatis Portal Ortu + Pengecualian Sholat/Mengaji
    item(key = "hub_kristen_info") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_kristen_hub_info"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
            border = BorderStroke(1.dp, Color(0xFFC7D2FE))
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = if (isEn) "Christian Worship Sync" else "Sinkronisasi Agama Kristen",
                        tint = Color(0xFF4338CA),
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
                            color = Color(0xFF4338CA)
                        ) {
                            Text(
                                text = if (isEn) "AUTO PORTAL & EXCLUSION" else "OTOMATIS PORTAL & FILTER",
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
                                text = if (isEn) "FASTING KEPT" else "PUASA TETAP IKUT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isEn) {
                            "Students claimed as Christian are automatically excluded from Muslim Sholat & Mengaji lists (Fasting stays active), and Christian Worship automatically appears on their Parent Portal."
                        } else {
                            "Anak yang diklaim beragama Kristen otomatis tidak masuk daftar Absen Sholat & Mengaji (Puasa tetap ikut), serta otomatis mengaktifkan menu Agama Kristen & laporannya di Portal Orang Tua."
                        },
                        fontSize = 11.sp,
                        lineHeight = 15.5.sp,
                        color = Color(0xFF312E81)
                    )
                }
            }
        }
    }

    // 2. Ringkasan KPI Ibadah Kristen
    item(key = "hub_kristen_stats") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_kristen_stats"),
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HubStatTile(
                        value = "${christianStudents.size}",
                        label = if (isEn) "Christian" else "Siswa Kristen",
                        bg = Color(0xFFE0E7FF),
                        fg = Color(0xFF3730A3),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = "$pagiCount",
                        label = if (isEn) "Morning" else "Ibadah Pagi",
                        bg = Color(0xFFFEF3C7),
                        fg = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = "$malamCount",
                        label = if (isEn) "Evening" else "Ibadah Malam",
                        bg = Color(0xFFEDE9FE),
                        fg = Color(0xFF6D28D9),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = "$gerejaCount",
                        label = if (isEn) "Church" else "Ke Gereja",
                        bg = Color(0xFFDCFCE7),
                        fg = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                }
                val claimedNote = if (claimedStudents.isNotEmpty()) {
                    val myChristian = claimedStudents.count { StudentReligionHelper.isChristian(it) }
                    if (isEn) {
                        "$myChristian in your foster group • ${todayRecords.size} activity logs today"
                    } else {
                        "$myChristian di kelompok binaan Anda • ${todayRecords.size} kegiatan hari ini"
                    }
                } else {
                    if (isEn) "${todayRecords.size} activity logs recorded today"
                    else "${todayRecords.size} kegiatan ibadah tercatat hari ini"
                }
                Text(
                    text = claimedNote,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 3. Grid 2x3 Kartu Menu Kartun (Senada Hub Lain)
    item(key = "hub_kristen_grid") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Claim Students" else "Klaim Anak Kristen",
                    subtitle = if (isEn) "Tag Religion & Filter" else "Tandai Siswa & Auto-Portal",
                    imageRes = R.drawable.img_menu_klaim_anak_asuh,
                    badgeText = "${christianStudents.size} ${if (isEn) "Kids" else "Anak"}",
                    badgeBg = Color(0xFFE0E7FF),
                    badgeColor = Color(0xFF3730A3),
                    bgGradient = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF).copy(alpha = 0.65f)),
                    borderColor = Color(0xFFC7D2FE),
                    onClick = onOpenClaimChristianStudents,
                    testTag = "tile_klaim_kristen",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Worship & Church" else "Input Ibadah Harian",
                    subtitle = if (isEn) "Morning, Evening, Church" else "Pagi, Malam & Ke Gereja + Foto",
                    imageRes = R.drawable.img_menu_kristen,
                    badgeText = if (todayRecords.isNotEmpty()) "${todayRecords.size} Hari Ini" else "+ Input",
                    badgeBg = Color(0xFFDCFCE7),
                    badgeColor = Color(0xFF15803D),
                    bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.65f)),
                    borderColor = Color(0xFFA7F3D0),
                    onClick = { onOpenInputChristianWorship(0, ChristianWorshipRecord.TYPE_DOA_PAGI, null) },
                    testTag = "tile_input_ibadah_kristen",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Verse, Prayer & Leave" else "Pembinaan Rohani",
                    subtitle = if (isEn) "Memory Verse, Prayer Notes" else "Ayat Hafalan, Pokok Doa & Izin",
                    imageRes = R.drawable.img_menu_konseling,
                    badgeText = if (isEn) "Nurture" else "Pembinaan",
                    badgeBg = Color(0xFFEDE9FE),
                    badgeColor = Color(0xFF6D28D9),
                    bgGradient = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE).copy(alpha = 0.65f)),
                    borderColor = Color(0xFFDDD6FE),
                    onClick = { onOpenInputChristianWorship(0, ChristianWorshipRecord.TYPE_AYAT_HAFALAN, null) },
                    testTag = "tile_pembinaan_kristen",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Reports & Gallery" else "Laporan & Galeri",
                    subtitle = if (isEn) "PDF, Photos & Share WA" else "Cetak PDF, Foto 90h & WA",
                    imageRes = R.drawable.img_menu_daftar_puasa,
                    badgeText = "${activeRecords.size} Log",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = Color(0xFF0369A1),
                    bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.65f)),
                    borderColor = Color(0xFFBAE6FD),
                    onClick = { onOpenChristianReport(0) },
                    testTag = "tile_laporan_kristen",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonMenuCard(
                    title = if (isEn) "Digital Bible" else "Alkitab Digital",
                    subtitle = if (isEn) "Devotionals & Audio 66 Books" else "66 Kitab, Renungan & Audio",
                    imageRes = R.drawable.img_menu_bible,
                    badgeText = "IND/ENG",
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFB45309),
                    bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.65f)),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onOpenBible,
                    testTag = "tile_alkitab_kristen",
                    modifier = Modifier.weight(1f)
                )
                CartoonMenuCard(
                    title = if (isEn) "Sunday Church" else "Jadwal Gereja Minggu",
                    subtitle = if (isEn) "Chaperone & Departure Roster" else "Daftar Hadir & Pendampingan",
                    imageRes = R.drawable.img_menu_kunjungan,
                    badgeText = if (isEn) "Sunday" else "Minggu",
                    badgeBg = Color(0xFFCCFBF1),
                    badgeColor = Color(0xFF0F766E),
                    bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.65f)),
                    borderColor = Color(0xFF99F6E4),
                    onClick = onOpenSundayChurch,
                    testTag = "tile_jadwal_gereja_minggu",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // 4. Tombol Share Ringkasan Harian WA
    if (christianStudents.isNotEmpty()) {
        item(key = "hub_kristen_wa_cta") {
            val ctx = LocalContext.current
            OutlinedButton(
                onClick = {
                    val waText = ChristianWorshipHelper.buildDailyWhatsappSummary(
                        records = activeRecords,
                        students = christianStudents,
                        filterTitle = if (isEn) "Daily / Period Summary" else "Rekap Ibadah Agama Kristen",
                        waliName = waliName,
                        isEn = isEn
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, waText)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        ctx.startActivity(
                            Intent.createChooser(
                                intent,
                                if (isEn) "Share Christian Worship Recap" else "Bagikan Rekap Ibadah Kristen"
                            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF16A34A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("btn_kristen_share_wa_summary")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEn) "Share Worship Recap via WhatsApp" else "Bagikan Rekap Ibadah ke WhatsApp",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        }
    }

    // 5. Daftar Anak Asuh Kristen & Status Terkini
    item(key = "hub_kristen_students_hdr") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "Christian Students (Tap to Log)" else "Daftar Anak Asuh Kristen (Ketuk untuk Catat)",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${christianStudents.size} ${if (isEn) "Kids" else "Anak"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4338CA)
            )
        }
    }

    if (christianStudents.isEmpty()) {
        item(key = "hub_kristen_empty") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_kristen_students"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Church,
                        contentDescription = null,
                        tint = Color(0xFF4338CA),
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = if (isEn) "No Christian Students Claimed Yet" else "Belum Ada Anak Beragama Kristen Diklaim",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEn) {
                            "Claim Christian students to unlock worship logs, exclude them from Sholat/Mengaji, and auto-activate Christian menus on their Parent Portal."
                        } else {
                            "Klaim anak asuh yang beragama Kristen agar terhubung ke Ibadah Pagi/Malam/Gereja, otomatis dikecualikan dari Sholat/Mengaji, dan langsung tampil di Portal Orang Tua."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenClaimChristianStudents,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_kristen_empty_claim")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Claim Christian Students Now" else "Klaim Anak Agama Kristen Sekarang",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else {
        items(christianStudents, key = { "hub_kristen_st_${it.id}" }) { student ->
            val stRecords = ChristianWorshipHelper.recordsForStudent(activeRecords, student)
            val latest = stRecords.firstOrNull()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onOpenInputChristianWorship(0, ChristianWorshipRecord.TYPE_DOA_PAGI, student)
                    }
                    .testTag("card_kristen_student_${student.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFC7D2FE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StudentAvatar(
                        name = student.name,
                        gender = student.gender,
                        size = 40.dp,
                        photoUrl = student.photoUrl,
                        photoFallbackUrl = student.photoFallbackUrl
                    )
                    Spacer(modifier = Modifier.width(10.dp))
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
                        if (latest != null) {
                            Text(
                                text = "${ChristianWorshipHelper.activityShortLabel(latest.activityType, isEn)}: ${latest.title.ifBlank { "-" }} (${DateUtils.formatShortDate(latest.activityDate, lang)})",
                                fontSize = 10.5.sp,
                                color = Color(0xFF4338CA),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = if (isEn) "Tap to record worship activity" else "Ketuk untuk catat ibadah harian",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0E7FF)
                    ) {
                        Text(
                            text = "${stRecords.size} Log",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3730A3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
