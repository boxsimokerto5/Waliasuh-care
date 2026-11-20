package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.data.model.SchoolProfile
import com.example.util.DateUtils
import com.example.util.PasswordGenerator
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KartuAksesOrangTuaSheet(
    students: List<ChildStudent>,
    parentAccessLinks: List<ParentAccessLink>,
    schoolProfile: SchoolProfile,
    isStudentClaimedByCurrentWali: (ChildStudent) -> Boolean,
    isAdmin: Boolean,
    onOpenPilihAnakAsuh: (() -> Unit)? = null,
    onGenerateAccessCard: (ChildStudent) -> Unit,
    onResetPassword: (ParentAccessLink) -> Unit,
    onEditStudentProfile: (ChildStudent) -> Unit,
    onApproveDeleteParentAccount: (ParentAccessLink) -> Unit = {},
    onRejectDeleteParentAccount: (ParentAccessLink) -> Unit = {},
    onDismiss: () -> Unit,
    isPrimaryWaliForStudent: (ChildStudent) -> Boolean = { true }
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish

    val claimedStudents = students.filter { isStudentClaimedByCurrentWali(it) }
    var searchQuery by remember { mutableStateOf("") }
    var linkPendingApproveDialog by remember { mutableStateOf<ParentAccessLink?>(null) }
    // Untuk Wali non-Admin: fokus hanya ke Anak Asuh Saya (0). Admin dapat memilih Semua Siswa (1) atau Anak Asuh Saya (0).
    var selectedScope by remember(isAdmin) {
        mutableStateOf(if (!isAdmin) 0 else 1)
    }

    val linkByStudentId = remember(parentAccessLinks) {
        parentAccessLinks.associateBy { it.studentId }
    }

    val effectiveScope = if (!isAdmin) 0 else selectedScope
    val baseScopeStudents = if (effectiveScope == 0) claimedStudents else students
    val pendingDeletionCount = remember(baseScopeStudents, linkByStudentId) {
        baseScopeStudents.count { linkByStudentId[it.id]?.deletionRequested == true }
    }

    val filteredStudents = remember(baseScopeStudents, searchQuery, linkByStudentId) {
        val q = searchQuery.trim().lowercase()
        baseScopeStudents.filter { st ->
            val link = linkByStudentId[st.id]
            q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.motherName.lowercase().contains(q) ||
                st.parentPhone.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                st.dormitory.lowercase().contains(q) ||
                (link?.username?.lowercase()?.contains(q) == true) ||
                (link?.parentName?.lowercase()?.contains(q) == true)
        }.sortedWith(
            compareByDescending<ChildStudent> { linkByStudentId[it.id]?.deletionRequested == true }
                .thenByDescending { linkByStudentId.containsKey(it.id) }
                .thenBy { it.name }
        )
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
                    .widthIn(max = 600.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDE9FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color(0xFF6D28D9),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Parent Access Cards" else "Kartu Akses Orang Tua",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Auto-linked to student data & shareable via WhatsApp"
                                } else {
                                    "Otomatis terhubung data siswa & bisa dibagikan ke WA"
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                // Search
                if (baseScopeStudents.isNotEmpty() || isAdmin) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(if (isEn) "Search student, parent name, or phone..." else "Cari siswa, nama orang tua, atau no. HP...", fontSize = 12.5.sp)
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Clear" else "Bersihkan", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_kartu_ortu")
                    )
                }

                // Scope Chips (Admin) / Count Pill (Wali)
                if (isAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedScope == 0,
                            onClick = { selectedScope = 0 },
                            label = { Text(if (isEn) "My Foster (${claimedStudents.size})" else "Anak Asuh Saya (${claimedStudents.size})", fontSize = 11.5.sp) }
                        )
                        FilterChip(
                            selected = selectedScope == 1,
                            onClick = { selectedScope = 1 },
                            label = { Text(if (isEn) "All Students (${students.size})" else "Semua Siswa (${students.size})", fontSize = 11.5.sp) }
                        )
                    }
                } else if (claimedStudents.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDE9FE),
                        border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                    ) {
                        Text(
                            text = if (isEn) {
                                "My Selected Foster Kids: ${claimedStudents.size}"
                            } else {
                                "Anak Asuh Terpilih: ${claimedStudents.size} siswa"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6D28D9),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                if (pendingDeletionCount > 0) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_pending_parent_deletions"),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "$pendingDeletionCount parent account deletion request(s) need Wali Asuh approval."
                                } else {
                                    "$pendingDeletionCount pengajuan hapus akun orang tua menunggu persetujuan Wali Asuh."
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp, max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (effectiveScope == 0 && claimedStudents.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("empty_kartu_ortu_no_claimed"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEDE9FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = Color(0xFF6D28D9),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isEn) "No Foster Students Selected Yet" else "Belum Ada Anak Asuh Dipilih",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Select your foster students in Select Foster Kids first to view and share their parent access cards."
                                        } else {
                                            "Pilih siswa binaan di menu Pilih Anak Asuh terlebih dahulu agar kartu akses orang tua langsung tampil di sini."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    if (onOpenPilihAnakAsuh != null) {
                                        Button(
                                            onClick = onOpenPilihAnakAsuh,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_open_pilih_anak_asuh_from_cards")
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isEn) "Select Foster Students" else "Pilih Anak Asuh Sekarang",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (filteredStudents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) "No matching student data." else "Data siswa tidak ditemukan.",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredStudents, key = { "parent_card_${it.id}" }) { student ->
                            val link = linkByStudentId[student.id]
                            if (link == null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_parent_unlinked_${student.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(student.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (isEn) {
                                                    "Parent: ${student.motherName.ifBlank { "-" }} • ${student.classGrade.ifBlank { "-" }}"
                                                } else {
                                                    "Orang Tua: ${student.motherName.ifBlank { "-" }} • ${student.classGrade.ifBlank { "-" }}"
                                                },
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { onGenerateAccessCard(student) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .heightIn(min = 40.dp)
                                                .testTag("btn_generate_parent_card_${student.id}")
                                        ) {
                                            Text(
                                                text = if (isEn) "Create Card" else "Buat Kartu",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                ParentDigitalIdCardItem(
                                    student = student,
                                    link = link,
                                    schoolProfile = schoolProfile,
                                    onCopy = { value, label ->
                                        clipboard.setText(AnnotatedString(value))
                                        Toast.makeText(
                                            context,
                                            if (isEn) "$label copied!" else "$label disalin!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onResetPassword = { onResetPassword(link) },
                                    onEditStudentProfile = { onEditStudentProfile(student) },
                                    onApproveDeleteAccount = { linkPendingApproveDialog = link },
                                    onRejectDeleteAccount = { onRejectDeleteParentAccount(link) },
                                    canManageDeletionApproval = isAdmin || isPrimaryWaliForStudent(student),
                                    onShare = {
                                        val effectivePhone = student.parentPhone.ifBlank { link.parentPhone }
                                        val effectiveLink = link.copy(
                                            parentName = student.motherName.ifBlank { link.parentName },
                                            parentPhone = effectivePhone,
                                            studentName = student.name,
                                            studentClass = student.classGrade,
                                            studentDorm = student.dormitory
                                        )
                                        PasswordGenerator.shareParentAccessCard(
                                            context = context,
                                            link = effectiveLink,
                                            schoolProfile = schoolProfile,
                                            isEnglish = isEn,
                                            isFemaleStudent = StudentClassifier.isFemaleStudent(student)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val confirmTarget = linkPendingApproveDialog
    if (confirmTarget != null) {
        AlertDialog(
            onDismissRequest = { linkPendingApproveDialog = null },
            modifier = Modifier.testTag("dialog_confirm_approve_parent_delete"),
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
            },
            title = {
                Text(
                    text = if (isEn) "Approve Account Deletion?" else "Setujui Hapus Akun Orang Tua?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Account '${confirmTarget.username}' for ${confirmTarget.studentName}'s parent will be permanently deleted and logged out. The student's school records will stay intact."
                    } else {
                        "Akun '${confirmTarget.username}' milik orang tua ${confirmTarget.studentName} akan dihapus permanen dan otomatis logout. Data kesehatan serta riwayat siswa tetap tersimpan aman."
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApproveDeleteParentAccount(confirmTarget)
                        linkPendingApproveDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_confirm_approve_parent_delete")
                ) {
                    Text(
                        text = if (isEn) "Approve & Delete" else "Setujui & Hapus",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { linkPendingApproveDialog = null },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_cancel_approve_parent_delete")
                ) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}

@Composable
private fun ParentDigitalIdCardItem(
    student: ChildStudent,
    link: ParentAccessLink,
    schoolProfile: SchoolProfile,
    onCopy: (String, String) -> Unit,
    onResetPassword: () -> Unit,
    onEditStudentProfile: () -> Unit,
    onApproveDeleteAccount: () -> Unit,
    onRejectDeleteAccount: () -> Unit,
    onShare: () -> Unit,
    canManageDeletionApproval: Boolean = true
) {
    val isEn = LocalAppStrings.current.isEnglish
    var passwordVisible by remember { mutableStateOf(false) }

    val displayParentName = student.motherName.trim()
        .ifBlank { link.parentName.trim() }
        .ifBlank { if (isEn) "Parent of ${student.name}" else "Orang Tua ${student.name}" }
    val effectivePhone = student.parentPhone.trim().ifBlank { link.parentPhone.trim() }
    val hasValidPhone = StudentCsvParser.formatWhatsAppDigits(effectivePhone).length >= 9

    val accentColor = Color(0xFF5B21B6)
    val bgTint = Color(0xFFF5F3FF)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_parent_access_${student.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Digital ID Card Header matching AdminScreen WaliAccountCardItem
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))))
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "PARENT DIGITAL ACCESS CARD" else "KARTU AKSES DIGITAL ORANG TUA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = schoolProfile.shortName.ifBlank { schoolProfile.schoolName },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = student.classGrade.ifBlank { student.dormitory.ifBlank { "Siswa" } },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Parent & Student Identity
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = displayParentName,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEn) {
                            "Parent of ${student.name} (${student.dormitory.ifBlank { "-" }})"
                        } else {
                            "Orang tua dari ${student.name} (${student.dormitory.ifBlank { "-" }})"
                        },
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = if (hasValidPhone) Color(0xFF15803D) else Color(0xFFD97706),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (hasValidPhone) {
                                "WhatsApp: $effectivePhone"
                            } else {
                                if (isEn) "Phone empty — add via Edit Profile or share via regular WA"
                                else "No. HP belum diisi — tambah lewat Edit Profil atau Share WA biasa"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (hasValidPhone) Color(0xFF15803D) else Color(0xFFB45309)
                        )
                    }
                }

                // Credentials Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = bgTint),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Username:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(link.username, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            }
                            IconButton(
                                onClick = { onCopy(link.username, "Username") },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Username", tint = accentColor, modifier = Modifier.size(15.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Password:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (passwordVisible) link.password else "••••••••",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onCopy(link.password, "Password") },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = accentColor, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }

                if (link.deletionRequested) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_parent_deletion_approval_${student.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Parent Account Deletion Request" else "Permintaan Hapus Akun Orang Tua",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            if (link.deletionReason.isNotBlank()) {
                                Text(
                                    text = "${if (isEn) "Reason:" else "Alasan:"} ${link.deletionReason}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                            if (link.deletionRequestedAt > 0L) {
                                Text(
                                    text = "${if (isEn) "Requested:" else "Diajukan:"} ${DateUtils.formatDateTime(link.deletionRequestedAt, LocalAppStrings.current.language)}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            if (canManageDeletionApproval) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                OutlinedButton(
                                    onClick = onRejectDeleteAccount,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF6B7280)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_reject_parent_delete_${student.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFF374151),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Decline" else "Tolak",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF374151)
                                    )
                                }
                                Button(
                                    onClick = onApproveDeleteAccount,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1.25f)
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_approve_parent_delete_${student.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Approve & Delete" else "Setujui & Hapus",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            } else {
                                Text(
                                    text = if (isEn) {
                                        "Approval for parent account deletion is restricted to Lead Caregiver or School Admin."
                                    } else {
                                        "Persetujuan hapus akun orang tua khusus Wali Utama atau Admin Sekolah."
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                    }
                }

                // Bottom Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onEditStudentProfile,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .heightIn(min = 38.dp)
                                .testTag("btn_edit_parent_phone_${student.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = accentColor)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasValidPhone) {
                                    if (isEn) "Profile" else "Edit Profil"
                                } else {
                                    if (isEn) "+ Phone" else "+ No. HP"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }

                        IconButton(
                            onClick = onResetPassword,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_reset_parent_pass_${student.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = if (isEn) "Reset Password" else "Acak Password",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onShare,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .testTag("btn_share_parent_card_${student.id}")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (hasValidPhone) {
                                if (isEn) "Send WA" else "Kirim WA"
                            } else {
                                if (isEn) "Share WA" else "Share WA"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
