package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.example.util.TopOnAdManager
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.ui.HealthViewModel
import com.example.ui.dialogs.AddStudentDialog
import com.example.ui.dialogs.StudentDetailDialog
import com.example.util.StudentClassifier

@Composable
fun StudentsScreen(
    viewModel: HealthViewModel,
    students: List<ChildStudent>,
    records: List<MedicalRecord>,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var selectedStudentForDetail by remember { mutableStateOf<ChildStudent?>(null) }

    val filteredStudents = students.filter { student ->
        if (searchQuery.isBlank()) true
        else {
            student.name.contains(searchQuery, ignoreCase = true) ||
                    student.classGrade.contains(searchQuery, ignoreCase = true) ||
                    student.dormitory.contains(searchQuery, ignoreCase = true) ||
                    student.nis.contains(searchQuery, ignoreCase = true) ||
                    student.address.contains(searchQuery, ignoreCase = true) ||
                    student.nik.contains(searchQuery, ignoreCase = true) ||
                    student.noKk.contains(searchQuery, ignoreCase = true) ||
                    student.motherName.contains(searchQuery, ignoreCase = true) ||
                    student.jenjang.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    TopOnAdManager.onUserActionClick(context)
                    showAddStudentDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_student")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Siswa", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "Tambah Anak Asuh", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                // Search Bar (Compact)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setStudentSearchQuery(it) },
                    placeholder = { Text("Cari nama, NIK, NISN, kelas, alamat...", fontSize = 11.5.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                        .testTag("input_student_search")
                )

                // Info Count Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total: ${filteredStudents.size} Anak Asuh",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = schoolProfile.shortName.ifBlank { "Sekolah" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (filteredStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "Tidak ada anak asuh dengan nama tersebut." else "Belum ada data anak asuh.",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(filteredStudents, key = { it.id }) { student ->
                            val studentRecordsCount = records.count { it.studentId == student.id }
                            CompactStudentCardItem(
                                student = student,
                                treatmentCount = studentRecordsCount,
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    selectedStudentForDetail = student
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddStudentDialog) {
        val activeSchoolCode = schoolProfile.tenantCode.ifBlank { "SRT1-KDR" }
        AddStudentDialog(
            activeSchoolId = activeSchoolCode,
            onDismiss = { showAddStudentDialog = false },
            onConfirmStudent = { newStudent ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.addStudent(
                    name = newStudent.name,
                    nis = newStudent.nis,
                    gender = newStudent.gender,
                    age = newStudent.age,
                    dormitory = newStudent.dormitory,
                    classGrade = newStudent.classGrade,
                    allergies = newStudent.allergies,
                    bloodType = newStudent.bloodType,
                    emergencyContact = newStudent.emergencyContact,
                    specialNotes = newStudent.specialNotes,
                    nik = newStudent.nik,
                    noKk = newStudent.noKk,
                    birthPlace = newStudent.birthPlace,
                    birthDate = newStudent.birthDate,
                    motherName = newStudent.motherName,
                    address = newStudent.address,
                    roomNumber = newStudent.roomNumber,
                    jenjang = newStudent.jenjang
                )
                showAddStudentDialog = false
            }
        )
    }

    selectedStudentForDetail?.let { student ->
        val studentHistory = records.filter { it.studentId == student.id }
        StudentDetailDialog(
            student = student,
            studentRecords = studentHistory,
            schoolProfile = schoolProfile,
            onDismiss = { selectedStudentForDetail = null }
        )
    }
}

@Composable
fun CompactStudentCardItem(
    student: ChildStudent,
    treatmentCount: Int,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("card_student_${student.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student Portrait Avatar matching mockups
            com.example.ui.components.StudentAvatar(
                name = student.name,
                gender = student.gender,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // Name & Gender Row - adaptive and flexible without squishing
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val isMale = StudentClassifier.isMaleStudent(student)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isMale) Color(0xFFE0F2FE) else Color(0xFFFCE7F3)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isMale) "Putra" else "Putri",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isMale) Color(0xFF0284C7) else Color(0xFFDB2777),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = buildString {
                        if (student.jenjang.isNotBlank()) append("${student.jenjang} • ")
                        append("${student.classGrade} • ${student.dormitory}")
                        if (student.roomNumber.isNotBlank()) append(" (${student.roomNumber})")
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (student.nik.isNotBlank() || student.nis.isNotBlank()) {
                    Text(
                        text = buildString {
                            if (student.nik.isNotBlank()) append("NIK: ${student.nik}")
                            if (student.nik.isNotBlank() && student.nis.isNotBlank()) append(" • ")
                            if (student.nis.isNotBlank()) append("NISN: ${student.nis}")
                        },
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (student.allergies.isNotBlank() && !student.allergies.equals("Tidak ada", ignoreCase = true)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Alergi: ${student.allergies}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Compact Counter Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$treatmentCount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Berobat",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
