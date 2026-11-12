package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HaidRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarHaidSheet(
    activeHaidRecords: List<HaidRecord>,
    onCompleteHaid: (Long) -> Unit,
    onDeleteRecord: (HaidRecord) -> Unit,
    onOpenInputHaid: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var recordToComplete by remember { mutableStateOf<HaidRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<HaidRecord?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

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

    val istihadhahCount = remember(activeHaidRecords) {
        activeHaidRecords.count { it.isIstihadhahWarning }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 520.dp)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
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
                                text = "${activeHaidRecords.size} Santriwati Aktif dalam Masa Haid",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("btn_close_daftar_haid")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ringkasan Cepat & Peringatan Fiqih
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
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (istihadhahCount > 0) Color(0xFFFEE2E2) else Color(0xFFF0FDF4)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (istihadhahCount > 0) "⚠️ Peringatan Fiqih" else "Status Normal",
                                fontSize = 10.sp,
                                color = if (istihadhahCount > 0) Color(0xFFB91C1C) else Color(0xFF15803D),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (istihadhahCount > 0) "$istihadhahCount Istihadhah (>14h)" else "Semua < 15 Hari",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (istihadhahCount > 0) Color(0xFF991B1B) else Color(0xFF166534)
                            )
                        }
                    }
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
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
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
                        modifier = Modifier.testTag("btn_add_haid_from_list")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Input", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List Siswi Sedang Haid
                if (filteredRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Semua santriwati berstatus suci dan siap beribadah.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredRecords, key = { it.id }) { record ->
                            val days = record.durationDays
                            val isIstihadhah = record.isIstihadhahWarning

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("item_haid_${record.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isIstihadhah) Color(0xFFFEF2F2) else Color(0xFFFDF2F8)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isIstihadhah) Color(0xFFFCA5A5) else Color(0xFFFBCFE8)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isIstihadhah) Color(0xFFFEE2E2) else Color(0xFFFBCFE8)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = record.studentName.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (isIstihadhah) Color(0xFFB91C1C) else Color(0xFF9D174D)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = record.studentName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = if (isIstihadhah) Color(0xFF7F1D1D) else Color(0xFF831843)
                                                )
                                                Text(
                                                    text = "${record.studentClass} • ${record.studentDorm}${if (record.studentNik.isNotBlank()) " • NIK: ${record.studentNik}" else ""}",
                                                    fontSize = 10.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF9D174D).copy(alpha = 0.8f)
                                                )
                                            }
                                        }

                                        // Badge Durasi Hari
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isIstihadhah) Color(0xFFEF4444) else Color(0xFFDB2777))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Hari ke-$days",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Peringatan Fiqih Istihadhah (>14 hari)
                                    if (isIstihadhah) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFEE2E2))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Fiqih: Melebihi 15 hari dihitung Istihadhah. Wajib mandi suci & sholat.",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    // Info Mulai, Keluhan & Catatan
                                    Text(
                                        text = "Mulai: ${dateFormat.format(Date(record.startDate))}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (record.symptoms.isNotBlank() && record.symptoms != "Normal (Tanpa Keluhan)") {
                                        Text(
                                            text = "Keluhan: ${record.symptoms}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFBE185D)
                                        )
                                    }

                                    if (record.notes.isNotBlank()) {
                                        Text(
                                            text = "Catatan: ${record.notes}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action Buttons Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Oleh: ${record.waliName}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            IconButton(
                                                onClick = { recordToDelete = record },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Button(
                                                onClick = { recordToComplete = record },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Mandi Suci", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Konfirmasi Mandi Suci
    if (recordToComplete != null) {
        val rec = recordToComplete!!
        BasicAlertDialog(onDismissRequest = { recordToComplete = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp).widthIn(max = 400.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Konfirmasi Mandi Suci",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Catat bahwa ${rec.studentName} telah selesai haid (durasi ${rec.durationDays} hari) dan telah bersuci/mandi wajib? Status akan dipindahkan ke 'Daftar Suci'.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { recordToComplete = null }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onCompleteHaid(rec.id)
                                recordToComplete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Text("Ya, Sudah Suci")
                        }
                    }
                }
            }
        }
    }

    // Modal Konfirmasi Hapus
    if (recordToDelete != null) {
        val rec = recordToDelete!!
        BasicAlertDialog(onDismissRequest = { recordToDelete = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp).widthIn(max = 380.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Hapus Catatan Haid?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hapus catatan haid untuk ${rec.studentName}?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { recordToDelete = null }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onDeleteRecord(rec)
                                recordToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Hapus")
                        }
                    }
                }
            }
        }
    }
}
