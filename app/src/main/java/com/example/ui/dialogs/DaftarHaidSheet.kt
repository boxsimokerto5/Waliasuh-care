package com.example.ui.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HaidDateCalculator
import com.example.data.model.HaidRecord
import com.example.data.model.SchoolProfile
import com.example.util.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarHaidSheet(
    activeHaidRecords: List<HaidRecord>,
    schoolProfile: SchoolProfile = SchoolProfile(),
    waliName: String = "Wali Asuh",
    onCompleteHaid: (Long) -> Unit,
    onUpdateRecord: (HaidRecord) -> Unit = {},
    onDeleteRecord: (HaidRecord) -> Unit,
    onOpenInputHaid: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedDetailRecord by remember { mutableStateOf<HaidRecord?>(null) }
    var recordToComplete by remember { mutableStateOf<HaidRecord?>(null) }
    var recordToEdit by remember { mutableStateOf<HaidRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<HaidRecord?>(null) }
    var showDailyReportSheet by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    val filteredRecords = remember(searchQuery, activeHaidRecords) {
        if (searchQuery.isBlank()) {
            activeHaidRecords
        } else {
            val q = searchQuery.trim().lowercase()
            activeHaidRecords.filter {
                it.studentName.lowercase().contains(q) ||
                    it.studentClass.lowercase().contains(q) ||
                    it.studentDorm.lowercase().contains(q) ||
                    it.studentNik.contains(q)
            }
        }
    }

    val maxDayCount = remember(activeHaidRecords) {
        activeHaidRecords.count { it.isIstihadhahWarning }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE7F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFFDB2777),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Daftar Siswi Sedang Haid",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${activeHaidRecords.size} Santriwati Aktif (Maks 15 Hari • Auto Suci)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_daftar_haid")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ringkasan Cepat & Status Batas 15 Hari
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Sedang Haid", fontSize = 10.sp, color = Color(0xFF9D174D))
                            Text(
                                text = "${activeHaidRecords.size} Siswi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF831843)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1.25f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (maxDayCount > 0) Color(0xFFFEE2E2) else Color(0xFFF0FDF4)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (maxDayCount > 0) "Batas Hari ke-15" else "Aturan Fleksibel 15h",
                                fontSize = 10.sp,
                                color = if (maxDayCount > 0) Color(0xFFB91C1C) else Color(0xFF15803D),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (maxDayCount > 0) "$maxDayCount Siswi Hari ke-15" else "Auto Suci > 15 Hari",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (maxDayCount > 0) Color(0xFF991B1B) else Color(0xFF166534)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Cepat: Laporan Haid Hari Ini (PDF berkop resmi multi-tenant + Ringkasan WA)
                OutlinedButton(
                    onClick = { showDailyReportSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFF472B6).copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFDF2F8),
                        contentColor = Color(0xFF9D174D)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_daily_haid_report")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFDB2777),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Laporan Haid Hari Ini (PDF Berkop & WA)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF9D174D),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar + Tombol Tambah Cepat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama siswi...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_search_daftar_haid")
                    )

                    Button(
                        onClick = onOpenInputHaid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_add_haid_from_list")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Input", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List Siswi Sedang Haid
                if (filteredRecords.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Female,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (activeHaidRecords.isEmpty()) "Tidak ada siswi yang sedang haid saat ini" else "Tidak ada siswi yang cocok dengan pencarian",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Semua santriwati berada di Daftar Suci dan siap beribadah.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 430.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredRecords, key = { it.id }) { record ->
                            val days = record.durationDays
                            val isMaxDay = record.isIstihadhahWarning
                            val clampedDay = days.coerceIn(1, HaidDateCalculator.MAX_HAID_DAYS)
                            val accentColor = when {
                                isMaxDay -> Color(0xFFEF4444)
                                days >= 12 -> Color(0xFFD97706)
                                else -> Color(0xFFDB2777)
                            }
                            val trackColor = when {
                                isMaxDay -> Color(0xFFFEE2E2)
                                days >= 12 -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFCE7F3)
                            }

                            Card(
                                onClick = { selectedDetailRecord = record },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("item_haid_${record.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMaxDay) Color(0xFFFEF2F2) else Color(0xFFFDF2F8)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isMaxDay) Color(0xFFFCA5A5) else Color(0xFFFBCFE8)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isMaxDay) Color(0xFFFEE2E2) else Color(0xFFFBCFE8)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = record.studentName.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (isMaxDay) Color(0xFFB91C1C) else Color(0xFF9D174D)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = record.studentName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = if (isMaxDay) Color(0xFF7F1D1D) else Color(0xFF831843),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = record.studentClass.ifBlank { record.studentDorm.ifBlank { "-" } },
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF9D174D).copy(alpha = 0.85f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(accentColor)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    if (isMaxDay) {
                                                        Icon(
                                                            imageVector = Icons.Default.Warning,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "Hari ke-$days",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "Lihat detail ${record.studentName}",
                                                tint = Color(0xFF9D174D).copy(alpha = 0.65f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Garis progress tipis (tanpa memenuhi layar)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(trackColor)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(clampedDay / HaidDateCalculator.MAX_HAID_DAYS.toFloat())
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(accentColor)
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

    // Pop-up Detail Lengkap saat kartu slim diklik
    selectedDetailRecord?.let { rec ->
        val days = rec.durationDays
        val isMaxDay = rec.isIstihadhahWarning
        ModalBottomSheet(
            onDismissRequest = { selectedDetailRecord = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isMaxDay) Color(0xFFFEE2E2) else Color(0xFFFCE7F3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rec.studentName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (isMaxDay) Color(0xFFB91C1C) else Color(0xFF9D174D)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.studentName,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${rec.studentClass} • ${rec.studentDorm}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (rec.studentNik.isNotBlank()) {
                                    Text(
                                        text = "NIK: ${rec.studentNik}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { selectedDetailRecord = null },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup detail")
                        }
                    }

                    // Timeline Berjalan Lengkap (Hari 1 s.d. 15)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMaxDay) Color(0xFFFEF2F2) else Color(0xFFFDF2F8)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isMaxDay) Color(0xFFFCA5A5) else Color(0xFFFBCFE8)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            HaidTimelineBar(currentDay = days)
                        }
                    }

                    if (isMaxDay) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hari ke-15 (Maksimal Haid): Wajib mandi suci. Lewat hari ke-15 otomatis pindah ke Daftar Suci.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Mulai Hari ke-1: ${dateFormat.format(Date(rec.startDate))}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Dicatat oleh: ${rec.waliName}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (rec.notes.isNotBlank()) {
                                Text(
                                    text = "Catatan: ${rec.notes}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF831843),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Aksi Lengkap: Edit & Hapus + Mandi Suci
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedDetailRecord = null
                                recordToEdit = rec
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_detail_edit_haid_${rec.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFFBE185D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ubah Hari", fontSize = 12.sp, color = Color(0xFFBE185D), fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedDetailRecord = null
                                recordToDelete = rec
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_detail_delete_haid_${rec.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hapus", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = {
                            selectedDetailRecord = null
                            recordToComplete = rec
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_detail_complete_haid_${rec.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mandi Suci (Pindah ke Daftar Suci)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Pop-up Laporan Haid Hari Ini (Cetak/Bagikan PDF Berkop Multi-Tenant & Teks WA)
    if (showDailyReportSheet) {
        val todayFormatted = remember {
            SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date())
        }
        ModalBottomSheet(
            onDismissRequest = { showDailyReportSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFCE7F3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFDB2777),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Laporan Haid Hari Ini",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = todayFormatted,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { showDailyReportSheet = false },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFDF2F8),
                        border = BorderStroke(1.dp, Color(0xFFFBCFE8))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF831843)
                            )
                            Text(
                                text = "Total terdata hari ini: ${activeHaidRecords.size} Siswi sedang haid",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9D174D)
                            )
                            Text(
                                text = "PDF dilengkapi kop surat sekolah, logo Kemensos & Sekolah Rakyat, serta daftar lengkap siswi sedang haid.",
                                fontSize = 11.sp,
                                color = Color(0xFF9D174D).copy(alpha = 0.85f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val pdfFile = PdfReportGenerator.generateDailyHaidReportPdf(
                                context = context,
                                activeHaidRecords = activeHaidRecords,
                                waliName = waliName,
                                schoolProfile = schoolProfile
                            )
                            if (pdfFile != null) {
                                showDailyReportSheet = false
                                PdfReportGenerator.openPdfFile(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Gagal membuat PDF laporan haid.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_open_daily_haid_pdf")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka / Pratinjau PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val pdfFile = PdfReportGenerator.generateDailyHaidReportPdf(
                                context = context,
                                activeHaidRecords = activeHaidRecords,
                                waliName = waliName,
                                schoolProfile = schoolProfile
                            )
                            if (pdfFile != null) {
                                showDailyReportSheet = false
                                PdfReportGenerator.shareHaidReportPdf(context, pdfFile, schoolProfile)
                            } else {
                                Toast.makeText(context, "Gagal membuat PDF laporan haid.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_share_daily_haid_pdf")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan File PDF (WhatsApp / Lainnya)", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            val summaryText = PdfReportGenerator.buildDailyHaidSummaryText(
                                activeHaidRecords = activeHaidRecords,
                                waliName = waliName,
                                schoolProfile = schoolProfile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, summaryText)
                            }
                            runCatching {
                                showDailyReportSheet = false
                                context.startActivity(Intent.createChooser(intent, "Kirim Ringkasan Haid Hari Ini"))
                            }.onFailure {
                                Toast.makeText(context, "Tidak dapat membuka aplikasi berbagi.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_share_daily_haid_text")
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim Ringkasan Teks ke WhatsApp",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet Edit Hari ke-... / Catatan
    if (recordToEdit != null) {
        val rec = recordToEdit!!
        var editDay by remember(rec.id) { mutableIntStateOf(rec.durationDays.coerceIn(1, HaidDateCalculator.MAX_HAID_DAYS)) }
        var editNotes by remember(rec.id) { mutableStateOf(rec.notes) }

        ModalBottomSheet(
            onDismissRequest = { recordToEdit = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sesuaikan Hari Haid & Catatan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = rec.studentName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDB2777)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDF2F8))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { if (editDay > 1) editDay-- },
                            enabled = editDay > 1,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurangi Hari")
                        }
                        Text(
                            text = "Haid Hari ke-$editDay",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDB2777)
                        )
                        FilledTonalIconButton(
                            onClick = { if (editDay < HaidDateCalculator.MAX_HAID_DAYS) editDay++ },
                            enabled = editDay < HaidDateCalculator.MAX_HAID_DAYS,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Hari")
                        }
                    }

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Catatan") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { recordToEdit = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                val updatedStart = HaidDateCalculator.calculateStartDateForHaidDay(editDay)
                                onUpdateRecord(
                                    rec.copy(
                                        startDate = updatedStart,
                                        notes = editNotes.trim()
                                    )
                                )
                                recordToEdit = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                            modifier = Modifier
                                .weight(1.3f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet Konfirmasi Mandi Suci (Pindah ke Daftar Suci di hari berapa pun, misal hari ke-8)
    if (recordToComplete != null) {
        val rec = recordToComplete!!
        ModalBottomSheet(
            onDismissRequest = { recordToComplete = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Konfirmasi Mandi Suci",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Catat bahwa ${rec.studentName} telah selesai haid di Hari ke-${rec.durationDays} dan sudah bersuci/mandi wajib? Siswi akan langsung dipindahkan ke 'Daftar Suci' (Suci Hari ke-1).",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { recordToComplete = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                onCompleteHaid(rec.id)
                                recordToComplete = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier
                                .weight(1.3f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Ya, Pindah ke Daftar Suci", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet Konfirmasi Hapus
    if (recordToDelete != null) {
        val rec = recordToDelete!!
        ModalBottomSheet(
            onDismissRequest = { recordToDelete = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Hapus Catatan Haid?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        text = "Hapus catatan haid untuk ${rec.studentName}?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { recordToDelete = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                onDeleteRecord(rec)
                                recordToDelete = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Hapus", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
