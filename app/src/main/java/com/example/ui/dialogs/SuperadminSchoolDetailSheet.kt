package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolRegistration
import com.example.data.model.WaliAccount
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SuperadminSchoolDetailSheet(
    registration: SchoolRegistration,
    students: List<ChildStudent>,
    waliAccounts: List<WaliAccount>,
    onCopyCredentials: () -> Unit,
    onSendWaCard: () -> Unit,
    onContactWa: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val schoolCode = registration.generatedSchoolCode.trim()
    val schoolStudents = remember(students, schoolCode) {
        if (schoolCode.isBlank()) emptyList()
        else students
            .filter { it.schoolId.equals(schoolCode, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
    }
    val schoolWalies = remember(waliAccounts, schoolCode) {
        if (schoolCode.isBlank()) emptyList()
        else waliAccounts
            .filter { it.schoolId.equals(schoolCode, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
    }

    val maleCount = remember(schoolStudents) {
        schoolStudents.count { StudentClassifier.isMaleStudent(it) }
    }
    val femaleCount = remember(schoolStudents, maleCount) {
        schoolStudents.size - maleCount
    }

    val studentsByDormitory = remember(schoolStudents, isEn) {
        schoolStudents.groupBy { it.dormitory.ifBlank { if (isEn) "Not assigned" else "Belum diatur" } }
            .toList()
            .sortedByDescending { it.second.size }
    }
    val studentsByJenjang = remember(schoolStudents, isEn) {
        schoolStudents.groupBy { student ->
            student.jenjang.ifBlank { StudentCsvParser.deriveJenjang(student.classGrade) }
                .ifBlank { if (isEn) "Other" else "Lainnya" }
        }.toList().sortedByDescending { it.second.size }
    }
    val waliesByDormitory = remember(schoolWalies, isEn) {
        schoolWalies.groupBy {
            val d = it.dormitory.ifBlank { "Umum" }
            if (isEn && d.equals("Umum", ignoreCase = true)) "General" else d
        }.toList().sortedByDescending { it.second.size }
    }

    var passwordVisible by remember(registration.id) { mutableStateOf(false) }
    var isWaliListExpanded by remember(registration.id) { mutableStateOf(false) }
    var isStudentListExpanded by remember(registration.id) { mutableStateOf(false) }
    var studentFilterQuery by remember(registration.id) { mutableStateOf("") }

    val filteredStudents = remember(schoolStudents, studentFilterQuery) {
        if (studentFilterQuery.isBlank()) schoolStudents
        else schoolStudents.filter {
            it.name.contains(studentFilterQuery, ignoreCase = true) ||
                it.nis.contains(studentFilterQuery, ignoreCase = true) ||
                it.classGrade.contains(studentFilterQuery, ignoreCase = true) ||
                it.dormitory.contains(studentFilterQuery, ignoreCase = true)
        }
    }

    val statusColor = when (registration.status) {
        "APPROVED" -> Color(0xFF16A34A)
        "SUSPENDED" -> Color(0xFFEA580C)
        "REJECTED" -> Color(0xFFDC2626)
        else -> Color(0xFFD97706)
    }
    val statusLabel = when (registration.status) {
        "APPROVED" -> if (isEn) "APPROVED & ACTIVE" else "DISETUJUI & AKTIF"
        "SUSPENDED" -> if (isEn) "SUSPENDED" else "DITANGGUHKAN / SUSPENDED"
        "REJECTED" -> if (isEn) "REJECTED" else "DITOLAK"
        else -> if (isEn) "PENDING APPROVAL" else "MENUNGGU PERSETUJUAN"
    }

    fun copyPlainText(label: String, value: String) {
        if (value.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(
            context,
            if (isEn) "$label copied successfully" else "$label berhasil disalin",
            Toast.LENGTH_SHORT
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("superadmin_school_detail_sheet"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. HEADER SEKOLAH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(WaliPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = registration.schoolName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (schoolCode.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(WaliPrimary.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = schoolCode,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = WaliPrimary
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.12f))
                                        .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_school_detail_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close school details" else "Tutup detail sekolah"
                        )
                    }
                }

                // 2. KARTU RINGKASAN JUMLAH SISWA & WALI ASUH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SchoolDetailMetricCard(
                        title = if (isEn) "Total Foster Students" else "Total Siswa Asuh",
                        mainValue = "${schoolStudents.size}",
                        subtitle = if (schoolStudents.isEmpty()) {
                            if (isEn) "No student data yet" else "Belum ada data siswa"
                        } else {
                            if (isEn) "Boys: $maleCount • Girls: $femaleCount" else "Putra: $maleCount • Putri: $femaleCount"
                        },
                        accentColor = Color(0xFF2563EB),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_total_students_card")
                    )
                    SchoolDetailMetricCard(
                        title = if (isEn) "Wali Asuh Accounts" else "Akun Wali Asuh",
                        mainValue = "${schoolWalies.size}",
                        subtitle = if (isEn) {
                            "Est. registered: ${registration.estimatedWaliCount} Wali Asuh"
                        } else {
                            "Estimasi daftar: ${registration.estimatedWaliCount} wali"
                        },
                        accentColor = Color(0xFF16A34A),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_total_walies_card")
                    )
                }

                // 3. SEBARAN ASRAMA & JENJANG (Jika data siswa/wali sudah ada)
                if (schoolStudents.isNotEmpty() || schoolWalies.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isEn) "School Data Distribution" else "Sebaran Data Sekolah",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (studentsByDormitory.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (isEn) "Students per Asrama" else "Siswa per Asrama",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        studentsByDormitory.forEach { (dorm, list) ->
                                            DistributionChip(
                                                label = if (isEn) "$dorm: ${list.size} students" else "$dorm: ${list.size} siswa",
                                                color = Color(0xFF2563EB)
                                            )
                                        }
                                    }
                                }
                            }

                            if (studentsByJenjang.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (isEn) "Students per Education Level" else "Siswa per Jenjang",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        studentsByJenjang.forEach { (jenjang, list) ->
                                            DistributionChip(
                                                label = if (isEn) "$jenjang: ${list.size} students" else "$jenjang: ${list.size} siswa",
                                                color = Color(0xFF7C3AED)
                                            )
                                        }
                                    }
                                }
                            }

                            if (waliesByDormitory.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (isEn) "Wali Asuh per Asrama" else "Wali Asuh per Asrama",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        waliesByDormitory.forEach { (dorm, list) ->
                                            DistributionChip(
                                                label = if (isEn) "$dorm: ${list.size} Wali Asuh" else "$dorm: ${list.size} wali",
                                                color = Color(0xFF16A34A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. PROFIL SEKOLAH & PENANGGUNG JAWAB (PJ)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isEn) "School & Person in Charge Information" else "Informasi Sekolah & Penanggung Jawab",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        DetailInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = if (isEn) "Region / City" else "Wilayah / Kota",
                            value = registration.city.ifBlank { "-" },
                            isEn = isEn
                        )
                        DetailInfoRow(
                            icon = Icons.Default.School,
                            label = if (isEn) "Full Address" else "Alamat Lengkap",
                            value = registration.address.ifBlank { "-" },
                            isEn = isEn
                        )
                        DetailInfoRow(
                            icon = Icons.Default.Person,
                            label = if (isEn) "Person in Charge (PIC)" else "Penanggung Jawab",
                            value = registration.picName.ifBlank { "-" },
                            isEn = isEn
                        )
                        val picPhoneLabel = if (isEn) "PIC Phone" else "Nomor PJ"
                        DetailInfoRow(
                            icon = Icons.Default.Phone,
                            label = if (isEn) "Phone / WhatsApp No." else "No. HP / WhatsApp",
                            value = registration.picPhone.ifBlank { "-" },
                            isEn = isEn,
                            onCopy = if (registration.picPhone.isNotBlank()) {
                                { copyPlainText(picPhoneLabel, registration.picPhone) }
                            } else null
                        )
                        val picEmailLabel = if (isEn) "PIC Email" else "Email PJ"
                        DetailInfoRow(
                            icon = Icons.Default.Email,
                            label = picEmailLabel,
                            value = registration.picEmail.ifBlank { "-" },
                            isEn = isEn,
                            onCopy = if (registration.picEmail.isNotBlank()) {
                                { copyPlainText(picEmailLabel, registration.picEmail) }
                            } else null
                        )
                        DetailInfoRow(
                            icon = Icons.Default.CalendarToday,
                            label = if (isEn) "Submission Date" else "Tanggal Pengajuan",
                            value = DateUtils.formatFullDate(registration.submissionDate, appStrings.language),
                            isEn = isEn
                        )
                        if (registration.approvedDate > 0L) {
                            DetailInfoRow(
                                icon = Icons.Default.Verified,
                                label = if (isEn) "Approved Date" else "Tanggal Disetujui",
                                value = DateUtils.formatFullDate(registration.approvedDate, appStrings.language),
                                isEn = isEn
                            )
                        }

                        if (registration.picPhone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Button(
                                onClick = onContactWa,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Contact PIC via WhatsApp" else "Hubungi Penanggung Jawab via WhatsApp",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 5. ALASAN PENOLAKAN / STATUS SUSPEND (jika ada)
                if (registration.status == "SUSPENDED") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, Color(0xFFFDBA74), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) {
                                    "This school is SUSPENDED. All Admin & Wali Asuh users under code $schoolCode are temporarily blocked from logging in."
                                } else {
                                    "Sekolah ini berstatus SUSPENDED. Seluruh user Admin & Wali Asuh pada kode $schoolCode diblokir sementara untuk login."
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF9A3412)
                            )
                        }
                    }
                } else if (registration.status == "REJECTED" && registration.rejectionReason.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isEn) "Rejection Reason" else "Alasan Penolakan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                            Text(
                                text = registration.rejectionReason,
                                fontSize = 12.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }

                // 6. KARTU AKSES ADMIN SEKOLAH (Jika sudah terbit kode & kredensial)
                if (schoolCode.isNotBlank() && registration.generatedAdminUsername.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(1.5.dp, Color(0xFF86EFAC), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "SCHOOL ADMIN CREDENTIALS" else "KREDENSIAL ADMIN SEKOLAH",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF15803D)
                                    )
                                }
                                Text(
                                    text = schoolCode,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(0xFF15803D)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Username:", fontSize = 12.5.sp, color = Color(0xFF166534))
                                Text(
                                    text = registration.generatedAdminUsername,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF14532D)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Password:", fontSize = 12.5.sp, color = Color(0xFF166534))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (passwordVisible) registration.generatedAdminPassword else "••••••••",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF14532D)
                                    )
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) {
                                                if (isEn) "Hide password" else "Sembunyikan password"
                                            } else {
                                                if (isEn) "Show password" else "Tampilkan password"
                                            },
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onCopyCredentials,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 42.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isEn) "Copy Account" else "Salin Akun", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = onSendWaCard,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .heightIn(min = 42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (isEn) "Send Card via WA" else "Kirim Kartu ke WA",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. DAFTAR LENGKAP WALI ASUH TERDAFTAR (Bisa dibuka/tutup)
                if (schoolWalies.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isWaliListExpanded = !isWaliListExpanded }
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                                    .testTag("sheet_toggle_wali_list"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEn) {
                                            "Full Wali Asuh List (${schoolWalies.size})"
                                        } else {
                                            "Daftar Lengkap Wali Asuh (${schoolWalies.size})"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isWaliListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isWaliListExpanded) {
                                        if (isEn) "Hide Wali Asuh list" else "Sembunyikan daftar wali asuh"
                                    } else {
                                        if (isEn) "Show Wali Asuh list" else "Tampilkan daftar wali asuh"
                                    },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = isWaliListExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider()
                                    schoolWalies.forEachIndexed { index, wali ->
                                        val dormDisplay = wali.dormitory.ifBlank { "Umum" }.let { d ->
                                            if (isEn && d.equals("Umum", ignoreCase = true)) "General" else d
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${index + 1}. ${wali.name}",
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "$dormDisplay • User: ${wali.username}" +
                                                            if (wali.phone.isNotBlank()) " • ${wali.phone}" else "",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                // 8. DAFTAR LENGKAP SISWA ASUH (Bisa dibuka/tutup + pencarian)
                if (schoolStudents.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isStudentListExpanded = !isStudentListExpanded }
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                                    .testTag("sheet_toggle_student_list"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEn) {
                                            "Full Student List (${schoolStudents.size})"
                                        } else {
                                            "Daftar Lengkap Siswa (${schoolStudents.size})"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isStudentListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isStudentListExpanded) {
                                        if (isEn) "Hide student list" else "Sembunyikan daftar siswa"
                                    } else {
                                        if (isEn) "Show student list" else "Tampilkan daftar siswa"
                                    },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = isStudentListExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider()
                                    OutlinedTextField(
                                        value = studentFilterQuery,
                                        onValueChange = { studentFilterQuery = it },
                                        placeholder = {
                                            Text(
                                                if (isEn) "Search student name, NIS, class, or Asrama..." else "Cari nama siswa, NIS, kelas, atau asrama...",
                                                fontSize = 12.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                        },
                                        trailingIcon = {
                                            if (studentFilterQuery.isNotBlank()) {
                                                IconButton(onClick = { studentFilterQuery = "" }) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = if (isEn) "Clear student search" else "Hapus pencarian siswa",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (filteredStudents.isEmpty()) {
                                        Text(
                                            text = if (isEn) {
                                                "No students match \"$studentFilterQuery\"."
                                            } else {
                                                "Tidak ada siswa yang cocok dengan \"$studentFilterQuery\"."
                                            },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        filteredStudents.forEachIndexed { index, student ->
                                            val isMale = StudentClassifier.isMaleStudent(student)
                                            val genderLabel = if (isEn) {
                                                if (isMale) "M" else "F"
                                            } else {
                                                if (isMale) "L" else "P"
                                            }
                                            val jenjangLabel = student.jenjang.ifBlank {
                                                StudentCsvParser.deriveJenjang(student.classGrade)
                                            }
                                            val classInfo = listOfNotNull(
                                                jenjangLabel.takeIf { it.isNotBlank() },
                                                student.classGrade.takeIf { it.isNotBlank() }
                                            ).joinToString(" • ")

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "${index + 1}. ${student.name}",
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = buildString {
                                                                append("NIS: ${student.nis.ifBlank { "-" }}")
                                                                if (classInfo.isNotBlank()) append(" • $classInfo")
                                                                if (student.dormitory.isNotBlank()) append(" • ${student.dormitory}")
                                                                if (student.roomNumber.isNotBlank()) {
                                                                    append(if (isEn) " (Room ${student.roomNumber})" else " (Kamar ${student.roomNumber})")
                                                                }
                                                            },
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isMale) Color(0xFFDBEAFE)
                                                                else Color(0xFFFCE7F3)
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = genderLabel,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isMale) Color(0xFF1D4ED8) else Color(0xFFBE185D)
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

                if (onDelete != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_detail_delete_school_${registration.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isEn) "Delete School Permanently" else "Hapus Sekolah Permanen",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SchoolDetailMetricCard(
    title: String,
    mainValue: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = mainValue,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DistributionChip(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEn: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WaliPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (onCopy != null) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = if (isEn) "Copy $label" else "Salin $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
