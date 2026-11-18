package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.ui.components.StudentAvatar
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddStudentDialog(
    initialStudent: ChildStudent? = null,
    activeSchoolId: String = "SRT1-KDR",
    onDismiss: () -> Unit,
    onConfirmStudent: (ChildStudent) -> Unit = {},
    onConfirmStudentWithPhoto: ((ChildStudent, Uri?, Boolean) -> Unit)? = null
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val isEditMode = initialStudent != null
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var nis by remember { mutableStateOf(initialStudent?.nis ?: "") }
    var gender by remember {
        mutableStateOf(
            if (initialStudent != null) {
                if (StudentClassifier.isFemaleStudent(initialStudent)) "P" else "L"
            } else "L"
        )
    }
    var ageText by remember { mutableStateOf(initialStudent?.age?.toString() ?: "12") }
    var jenjang by remember {
        mutableStateOf(
            initialStudent?.jenjang?.ifBlank {
                StudentCsvParser.deriveJenjang(initialStudent.classGrade)
            } ?: "SMP"
        )
    }
    var classGrade by remember { mutableStateOf(initialStudent?.classGrade ?: "VII-1") }
    var dormitory by remember { mutableStateOf(initialStudent?.dormitory ?: "Asrama Putra") }
    var roomNumber by remember { mutableStateOf(initialStudent?.roomNumber ?: "") }

    // Data Kependudukan Lengkap
    var nik by remember { mutableStateOf(StudentCsvParser.cleanDigitsOnly(initialStudent?.nik)) }
    var noKk by remember { mutableStateOf(StudentCsvParser.cleanDigitsOnly(initialStudent?.noKk)) }
    var birthPlace by remember { mutableStateOf(initialStudent?.birthPlace ?: "") }
    var birthDate by remember { mutableStateOf(initialStudent?.birthDate ?: "") }
    var motherName by remember { mutableStateOf(initialStudent?.motherName ?: "") }
    var parentPhone by remember { mutableStateOf(initialStudent?.parentPhone ?: "") }
    var address by remember { mutableStateOf(initialStudent?.address ?: "") }
    var district by remember { mutableStateOf(initialStudent?.district ?: "") }

    // Kesehatan & Kontak
    var bloodType by remember { mutableStateOf(initialStudent?.bloodType ?: "-") }
    var allergies by remember { mutableStateOf(initialStudent?.allergies?.takeIf { !it.equals("Tidak ada", ignoreCase = true) } ?: "") }
    var emergencyContact by remember { mutableStateOf(initialStudent?.emergencyContact ?: "Pengasuh Asrama") }
    var specialNotes by remember { mutableStateOf(initialStudent?.specialNotes ?: "") }

    var showHealthSection by remember { mutableStateOf(isEditMode) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var removeExistingPhoto by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingPhotoUri = uri
            removeExistingPhoto = false
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditMode) {
                            if (isEn) "Edit Student Profile" else "Edit Profil Siswa"
                        } else {
                            if (isEn) "Add Foster Student" else "Tambah Anak Asuh"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val previewPhotoUrl = pendingPhotoUri?.toString()
                    ?: if (removeExistingPhoto) "" else initialStudent?.photoUrl.orEmpty()
                val previewFallbackUrl = if (pendingPhotoUri != null || removeExistingPhoto) {
                    ""
                } else {
                    initialStudent?.photoFallbackUrl.orEmpty()
                }
                val hasFormPhoto = previewPhotoUrl.isNotBlank() || previewFallbackUrl.isNotBlank()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_add_student_photo_picker"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StudentAvatar(
                            name = name.ifBlank { initialStudent?.name ?: "Siswa" },
                            gender = gender,
                            size = 58.dp,
                            photoUrl = previewPhotoUrl,
                            photoFallbackUrl = previewFallbackUrl
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isEn) "Student Profile Photo" else "Foto Profil Siswa",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    pendingPhotoUri != null -> if (isEn) "New photo selected (will upload on save)" else "Foto baru dipilih (otomatis terunggah saat simpan)"
                                    hasFormPhoto -> if (isEn) "Current photo shown on Parent Portal" else "Foto tampil di profil Wali Asuh & Orang Tua"
                                    else -> if (isEn) "Optional • Shown to parents & caregivers" else "Opsional • Tampil di profil anak & Portal Orang Tua"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .heightIn(min = 36.dp)
                                        .testTag("btn_pick_student_photo_form")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (hasFormPhoto) {
                                            if (isEn) "Change Photo" else "Ganti Foto"
                                        } else {
                                            if (isEn) "Pick Photo" else "Pilih Foto"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (hasFormPhoto) {
                                    TextButton(
                                        onClick = {
                                            pendingPhotoUri = null
                                            removeExistingPhoto = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .heightIn(min = 36.dp)
                                            .testTag("btn_clear_student_photo_form")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isEn) "Remove" else "Hapus",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isEn) "Student Full Name *" else "Nama Lengkap Siswa *", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g. ACHMAD DEVA ADIANSA" else "misal: ACHMAD DEVA ADIANSA", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = nis,
                        onValueChange = { nis = it },
                        label = { Text("NISN / NIS", fontSize = 12.sp) },
                        placeholder = { Text("95427948", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text(if (isEn) "Age (Yrs)" else "Usia (Th)", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.6f)
                    )
                }

                Text(
                    if (isEn) "Gender & Education Level:" else "Jenis Kelamin & Jenjang:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = gender == "L",
                        onClick = {
                            gender = "L"
                            if (dormitory.contains("Putri", ignoreCase = true)) {
                                dormitory = dormitory.replace("Putri", "Putra", ignoreCase = true)
                            }
                        },
                        label = { Text("Putra (L)", fontSize = 11.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                    FilterChip(
                        selected = gender == "P",
                        onClick = {
                            gender = "P"
                            if (dormitory.contains("Putra", ignoreCase = true)) {
                                dormitory = dormitory.replace("Putra", "Putri", ignoreCase = true)
                            }
                        },
                        label = { Text("Putri (P)", fontSize = 11.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                    listOf("SD", "SMP", "SMA").forEach { j ->
                        FilterChip(
                            selected = jenjang.equals(j, ignoreCase = true) || jenjang.uppercase().startsWith(j),
                            onClick = { jenjang = j },
                            label = { Text(j, fontSize = 11.sp) },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = classGrade,
                        onValueChange = { classGrade = it },
                        label = { Text(if (isEn) "Class / Grade" else "Kelas", fontSize = 12.sp) },
                        placeholder = { Text("XI-3 (IPS) / VII-1", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text(if (isEn) "Room" else "Kamar", fontSize = 12.sp) },
                        placeholder = { Text("PL_103 / PP_101", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = dormitory,
                    onValueChange = { dormitory = it },
                    label = { Text("Asrama *", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g. Asrama Putra" else "misal: Asrama Putra", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                Text(
                    text = if (isEn) "Civil Identity (per KK / CSV)" else "Identitas Kependudukan (Sesuai KK/CSV)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = nik,
                        onValueChange = { raw -> nik = raw.filter { it.isDigit() } },
                        label = { Text(if (isEn) "NIK (16 Digits)" else "NIK (16 Digit)", fontSize = 12.sp) },
                        placeholder = { Text("3506xxxxxxxxxxxx", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_student_nik")
                    )
                    OutlinedTextField(
                        value = noKk,
                        onValueChange = { raw -> noKk = raw.filter { it.isDigit() } },
                        label = { Text(if (isEn) "KK No." else "No. KK", fontSize = 12.sp) },
                        placeholder = { Text("3506xxxxxxxxxxxx", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_student_no_kk")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text(if (isEn) "Birth Place" else "Tempat Lahir", fontSize = 12.sp) },
                        placeholder = { Text("KEDIRI", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { newVal ->
                            birthDate = newVal
                            if (newVal.length >= 8) {
                                ageText = StudentCsvParser.estimateAge(newVal).toString()
                            }
                        },
                        label = { Text(if (isEn) "Birth Date (DD-MM-YYYY)" else "Tgl Lahir (DD-MM-YYYY)", fontSize = 12.sp) },
                        placeholder = { Text("25-12-2009", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = motherName,
                    onValueChange = { motherName = it },
                    label = { Text(if (isEn) "Biological Mother's / Parent Name" else "Nama Ibu Kandung / Orang Tua", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g. DWI MASRUROH" else "misal: DWI MASRUROH", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { raw -> parentPhone = raw.filter { it.isDigit() || it == '+' } },
                    label = { Text(if (isEn) "Parent WhatsApp / Phone No." else "No. HP / WhatsApp Orang Tua", fontSize = 12.sp) },
                    placeholder = { Text("0812xxxxxxxx", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_parent_phone")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isEn) "Full Address" else "Alamat Lengkap", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "Hamlet, RT/RW, Village, District, Regency" else "Dusun, RT/RW, Desa, Kec, Kab", fontSize = 12.sp) },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                val suggestedDistrict = remember(address, district) {
                    if (district.isBlank()) StudentCsvParser.extractDistrictFromAddress(address) else ""
                }
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text(if (isEn) "District (Kecamatan)" else "Kecamatan", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g. Banyakan / Kras / Semen" else "misal: Banyakan / Kras / Semen", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_district")
                )
                if (suggestedDistrict.isNotBlank()) {
                    TextButton(
                        onClick = { district = suggestedDistrict },
                        modifier = Modifier.testTag("btn_fill_district_from_address")
                    ) {
                        Text(
                            text = if (isEn) {
                                "Use from address: $suggestedDistrict"
                            } else {
                                "Isi otomatis dari alamat: Kec. $suggestedDistrict"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                TextButton(
                    onClick = { showHealthSection = !showHealthSection },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Health, Blood Type & Guardian Contact" else "Kesehatan, Golongan Darah & Kontak Wali",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (showHealthSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                if (showHealthSection) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = bloodType,
                            onValueChange = { bloodType = it },
                            label = { Text(if (isEn) "Blood Type" else "Gol. Darah", fontSize = 12.sp) },
                            placeholder = { Text("A / B / AB / O / -", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.7f)
                        )
                        OutlinedTextField(
                            value = emergencyContact,
                            onValueChange = { emergencyContact = it },
                            label = { Text(if (isEn) "Guardian / Caregiver Contact" else "Kontak Pengasuh / Wali", fontSize = 12.sp) },
                            placeholder = { Text("0812-xxxx-xxxx", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text(if (isEn) "Allergy History (if any)" else "Riwayat Alergi (jika ada)", fontSize = 12.sp) },
                        placeholder = { Text(if (isEn) "e.g. Cold allergy, amoxicillin" else "misal: Alergi dingin, amoxicillin", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = specialNotes,
                        onValueChange = { specialNotes = it },
                        label = { Text(if (isEn) "Special Notes" else "Catatan Khusus", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(s.nav.cancel, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val age = ageText.toIntOrNull()
                                ?: StudentCsvParser.estimateAge(birthDate)
                            if (name.isNotBlank() && dormitory.isNotBlank()) {
                                val resolvedNis = nis.trim().ifBlank {
                                    "${activeSchoolId}-${System.currentTimeMillis() % 10000}"
                                }
                                val resolvedContact = emergencyContact.trim().ifBlank {
                                    if (motherName.isNotBlank()) "Ibu: ${motherName.trim()}" else "Pengasuh ${dormitory.trim()}"
                                }
                                val updated = (initialStudent ?: ChildStudent(
                                    id = 0L,
                                    name = "",
                                    nis = "",
                                    gender = gender,
                                    age = age,
                                    dormitory = dormitory.trim(),
                                    classGrade = classGrade.trim(),
                                    schoolId = activeSchoolId
                                )).copy(
                                    name = name.trim(),
                                    nis = resolvedNis,
                                    gender = gender,
                                    age = age,
                                    dormitory = dormitory.trim(),
                                    classGrade = classGrade.trim(),
                                    allergies = if (allergies.isBlank()) "Tidak ada" else allergies.trim(),
                                    bloodType = if (bloodType.isBlank()) "-" else bloodType.trim(),
                                    emergencyContact = resolvedContact,
                                    specialNotes = specialNotes.trim(),
                                    nik = StudentCsvParser.cleanDigitsOnly(nik),
                                    noKk = StudentCsvParser.cleanDigitsOnly(noKk),
                                    birthPlace = birthPlace.trim(),
                                    birthDate = birthDate.trim(),
                                    motherName = motherName.trim(),
                                    parentPhone = StudentCsvParser.cleanPhoneNumber(parentPhone),
                                    address = address.trim(),
                                    district = district.trim(),
                                    roomNumber = roomNumber.trim(),
                                    jenjang = jenjang.trim().ifBlank { StudentCsvParser.deriveJenjang(classGrade) },
                                    schoolId = initialStudent?.schoolId?.ifBlank { activeSchoolId } ?: activeSchoolId,
                                    photoUrl = if (removeExistingPhoto && pendingPhotoUri == null) "" else (initialStudent?.photoUrl ?: ""),
                                    photoFallbackUrl = if (removeExistingPhoto && pendingPhotoUri == null) "" else (initialStudent?.photoFallbackUrl ?: "")
                                )
                                if (onConfirmStudentWithPhoto != null) {
                                    onConfirmStudentWithPhoto(updated, pendingPhotoUri, removeExistingPhoto && pendingPhotoUri == null)
                                } else {
                                    onConfirmStudent(updated)
                                }
                            }
                        },
                        enabled = name.isNotBlank() && dormitory.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_add_student_button")
                    ) {
                        Text(
                            if (isEditMode) {
                                if (isEn) "Save Changes" else "Simpan Perubahan"
                            } else {
                                if (isEn) "Add Student" else "Tambah Siswa"
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
