package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.StudentClassifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputPuasaDialog(
    sessions: List<FastingSession>,
    students: List<ChildStudent>,
    fastingRecords: List<FastingRecord>,
    onRegisterStudent: (FastingSession, ChildStudent, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeSessions = remember(sessions) {
        sessions.filter { it.isActive }.sortedByDescending { it.sessionDate }
    }
    val availableSessions = remember(sessions, activeSessions) {
        if (activeSessions.isNotEmpty()) activeSessions else sessions.sortedByDescending { it.sessionDate }
    }

    var selectedSessionId by remember(availableSessions) {
        mutableStateOf(availableSessions.firstOrNull()?.id)
    }
    val selectedSession = remember(availableSessions, selectedSessionId) {
        availableSessions.firstOrNull { it.id == selectedSessionId } ?: availableSessions.firstOrNull()
    }

    var searchQuery by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var lastFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var scannerFloatingFeedback by remember { mutableStateOf<ScannerFloatingFeedback?>(null) }

    LaunchedEffect(availableSessions) {
        if (selectedSessionId == null && availableSessions.isNotEmpty()) {
            selectedSessionId = availableSessions.first().id
        }
    }

    val sessionRecords = remember(fastingRecords, selectedSession) {
        if (selectedSession == null) emptyList()
        else fastingRecords.filter { it.sessionId == selectedSession.id }
    }

    val registeredStudentIds = remember(sessionRecords) {
        sessionRecords.map { it.studentId }.filter { it > 0L }.toSet()
    }
    val registeredStudentNiks = remember(sessionRecords) {
        sessionRecords.mapNotNull { it.studentNik.trim().lowercase().takeIf(String::isNotBlank) }.toSet()
    }
    val registeredStudentNiss = remember(sessionRecords) {
        sessionRecords.mapNotNull { it.studentNis.trim().lowercase().takeIf(String::isNotBlank) }.toSet()
    }

    fun isStudentRegistered(student: ChildStudent): Boolean {
        if (student.id > 0L && student.id in registeredStudentIds) return true
        if (student.nik.isNotBlank() && student.nik.trim().lowercase() in registeredStudentNiks) return true
        if (student.nis.isNotBlank() && student.nis.trim().lowercase() in registeredStudentNiss) return true
        return false
    }

    // Daftar siswa HANYA muncul setelah mengetik di kolom cari siswa
    val matchedStudents = remember(students, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            emptyList()
        } else {
            students.filter { s ->
                s.name.lowercase().contains(q) ||
                    s.classGrade.lowercase().contains(q) ||
                    s.dormitory.lowercase().contains(q) ||
                    s.nik.lowercase().contains(q) ||
                    s.nis.lowercase().contains(q)
            }.take(40)
        }
    }

    val tealPrimary = Color(0xFF0F766E)
    val tealLight = Color(0xFFCCFBF1)

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
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(tealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = tealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Input Puasa Siswa",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Scan barcode langsung terinput atau cari nama siswa",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_input_puasa")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup"
                        )
                    }
                }

                if (availableSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Belum Ada Sesi Puasa Tersedia",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Pembuatan sesi puasa (misal: Puasa Senin Kamis) dilakukan melalui akun Admin Sekolah di tab 'Sesi Puasa'. Hubungi Admin Sekolah untuk membuat sesi terlebih dahulu.",
                                fontSize = 12.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                } else {
                    // Pilih Sesi Puasa
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pilih Sesi Puasa:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            selectedSession?.let { sess ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(tealLight)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${sessionRecords.size} Terdaftar",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tealPrimary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSessions.forEach { session ->
                                val selected = selectedSession?.id == session.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedSessionId = session.id },
                                    label = {
                                        val dateStr = DateUtils.formatShortDate(session.sessionDate)
                                        Text(
                                            text = "${session.title} ($dateStr)${if (!session.isActive) " • Selesai" else ""}",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.EventNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tealLight,
                                        selectedLabelColor = tealPrimary,
                                        selectedLeadingIconColor = tealPrimary
                                    ),
                                    modifier = Modifier.testTag("chip_session_input_${session.id}")
                                )
                            }
                        }
                    }

                    // Banner info sesi aktif
                    selectedSession?.let { sess ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                            border = BorderStroke(1.dp, Color(0xFF99F6E4))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = sess.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tealPrimary
                                )
                                Text(
                                    text = "${DateUtils.formatFullDate(sess.sessionDate)}${if (sess.description.isNotBlank()) " • ${sess.description}" else ""}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF115E59)
                                )
                            }
                        }
                    }

                    // Tombol Scan Barcode Kartu Siswa -> Langsung terinput!
                    Button(
                        onClick = { showBarcodeScanner = true },
                        enabled = selectedSession != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("btn_scan_barcode_puasa")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode Kartu Siswa",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Barcode Kartu Siswa (Langsung Terinput)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Banner Feedback Scan / Input Terakhir
                    lastFeedback?.let { (isSuccess, msg) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSuccess) Color(0xFF86EFAC) else Color(0xFFFCD34D)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isSuccess) Color(0xFF16A34A) else Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSuccess) Color(0xFF166534) else Color(0xFF92400E)
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showBarcodeScanner = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Scan Lagi", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = tealPrimary)
                                }
                            }
                        }
                    }

                    // Baris Cari Siswa (Daftar HANYA tampil setelah pengguna mengetik)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ketik nama, kelas, atau NIK/NIS siswa...", fontSize = 12.5.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari Siswa",
                                tint = tealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Bersihkan")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_student_puasa")
                    )

                    if (searchQuery.isBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Text(
                                text = "Ketik nama atau NIS siswa di kolom pencarian di atas untuk menampilkan pilihan siswa, atau gunakan tombol Scan Barcode agar langsung terinput.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else if (matchedStudents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ditemukan siswa dengan kata kunci \"$searchQuery\".",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Hasil Pencarian (${matchedStudents.size} Siswa) — Ketuk untuk input puasa:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(matchedStudents, key = { "puasa_pick_${it.id}" }) { student ->
                                val already = isStudentRegistered(student)
                                val jenjang = FastingJenjangHelper.resolveJenjang(student)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !already && selectedSession != null) {
                                            selectedSession?.let { sess ->
                                                onRegisterStudent(sess, student) { added, message ->
                                                    lastFeedback = added to message
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                        .testTag("card_pick_puasa_${student.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (already) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (already) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(if (already) Color(0xFFDCFCE7) else tealLight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = student.name.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (already) Color(0xFF15803D) else tealPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = student.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "$jenjang • ${student.classGrade.ifBlank { "-" }} • ${student.dormitory}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (already) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Sudah Terdaftar",
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Terdaftar",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircleOutline,
                                                    contentDescription = "Input Puasa",
                                                    tint = tealPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Input",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tealPrimary
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
        }
    }

    if (showBarcodeScanner && selectedSession != null) {
        BarcodeScannerDialog(
            femaleStudents = students,
            onScanned = { scannedCode ->
                val matched = StudentClassifier.findMatchingStudent(students, scannedCode)
                if (matched == null) {
                    val err = "Kartu siswa dengan kode \"$scannedCode\" tidak ditemukan di data sekolah."
                    lastFeedback = false to err
                    scannerFloatingFeedback = ScannerFloatingFeedback(
                        id = System.nanoTime(),
                        status = ScannerFeedbackStatus.ERROR,
                        title = "Kartu tidak ditemukan",
                        subtitle = "Kode \"$scannedCode\" belum terdaftar"
                    )
                } else {
                    onRegisterStudent(selectedSession, matched) { added, message ->
                        lastFeedback = added to message
                        val jenjang = FastingJenjangHelper.resolveJenjang(matched)
                        val infoLine = listOf(jenjang, matched.classGrade.trim(), matched.dormitory.trim())
                            .filter { it.isNotBlank() }
                            .joinToString(" • ")
                        scannerFloatingFeedback = if (added) {
                            ScannerFloatingFeedback(
                                id = System.nanoTime(),
                                status = ScannerFeedbackStatus.SUCCESS,
                                title = "Telah terinput puasa anak ${matched.name}",
                                subtitle = infoLine.ifBlank { selectedSession.title }
                            )
                        } else {
                            ScannerFloatingFeedback(
                                id = System.nanoTime(),
                                status = ScannerFeedbackStatus.WARNING,
                                title = "${matched.name} sudah terinput",
                                subtitle = "Sudah terdaftar pada sesi ${selectedSession.title}"
                            )
                        }
                    }
                }
            },
            onDismiss = {
                showBarcodeScanner = false
                scannerFloatingFeedback = null
            },
            dialogTitle = "Scan Barcode Puasa Siswa",
            dialogSubtitle = "Sesi: ${selectedSession.title} • Kamera tetap aktif untuk banyak kartu",
            quickPickTitle = "Pilih Cepat Kartu Siswa (${students.size} Siswa):",
            searchPlaceholder = "Cari nama, kelas, kamar, atau NIK siswa...",
            emptyListMessage = "Belum ada data siswa terdaftar di sekolah ini.",
            accentColor = WaliPrimary,
            accentBgColor = Color(0xFFCCFBF1),
            cardBgColor = Color(0xFFF0FDFA),
            avatarBgColor = Color(0xFF99F6E4),
            avatarTextColor = Color(0xFF0F766E),
            titleTextColor = Color(0xFF134E4A),
            continuousScan = true,
            sessionParticipantCount = sessionRecords.size,
            floatingFeedback = scannerFloatingFeedback
        )
    }
}
