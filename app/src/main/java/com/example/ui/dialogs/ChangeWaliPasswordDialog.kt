package com.example.ui.dialogs

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeWaliPasswordDialog(
    viewModel: HealthViewModel,
    currentAccount: WaliAccount?,
    currentWaliName: String,
    schoolName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val isUploadingPhoto by viewModel.isUploadingWaliPhoto.collectAsStateWithLifecycle()

    var nameInput by remember(currentAccount?.id) {
        mutableStateOf(currentAccount?.name?.ifBlank { currentWaliName } ?: currentWaliName)
    }
    var phoneInput by remember(currentAccount?.id) {
        mutableStateOf(currentAccount?.phone.orEmpty())
    }
    var titleInput by remember(currentAccount?.id) {
        mutableStateOf(currentAccount?.titlePosition.orEmpty())
    }
    var employeeCodeInput by remember(currentAccount?.id) {
        mutableStateOf(currentAccount?.employeeCode.orEmpty())
    }
    var bioInput by remember(currentAccount?.id) {
        mutableStateOf(currentAccount?.bio.orEmpty())
    }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showPhotoPreview by remember { mutableStateOf(false) }
    var showConfirmRemovePhoto by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadWaliProfilePhoto(uri) { _, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val activeUsername = currentAccount?.username?.ifBlank {
        (currentAccount.name.ifBlank { currentWaliName }).lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
    } ?: currentWaliName.lowercase().replace(" ", "").filter { it.isLetterOrDigit() }.ifBlank { "wali" }

    val currentExistingPassword = currentAccount?.password ?: ""
    val avatarGender = if (currentAccount?.dormitory?.contains("putri", ignoreCase = true) == true) "P" else "L"
    val photoUrl = currentAccount?.photoUrl.orEmpty()
    val photoFallbackUrl = currentAccount?.photoFallbackUrl.orEmpty()
    val hasPhoto = photoUrl.isNotBlank() || photoFallbackUrl.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = { if (!isUploadingPhoto && !isSubmitting) onDismiss() },
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WaliPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = WaliPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEn) "Caregiver Profile & Password" else "Profil Pembina & Kata Sandi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Update your official photo, profile & optional password"
                            } else {
                                "Lengkapi foto resmi, biodata & kata sandi akun Anda"
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bagian Foto Profil Resmi Wali Asuh
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, WaliPrimary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, WaliPrimary.copy(alpha = 0.35f), CircleShape)
                                .let { mod ->
                                    if (hasPhoto && !isUploadingPhoto) {
                                        mod.clickable { showPhotoPreview = true }
                                    } else mod
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            StudentAvatar(
                                name = nameInput.ifBlank { currentWaliName },
                                gender = avatarGender,
                                size = 90.dp,
                                photoUrl = photoUrl,
                                photoFallbackUrl = photoFallbackUrl
                            )
                            if (isUploadingPhoto) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            } else if (hasPhoto) {
                                Surface(
                                    shape = CircleShape,
                                    color = WaliPrimary,
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = if (isEn) "Zoom photo" else "Perbesar foto",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (isUploadingPhoto) {
                            Text(
                                text = if (isEn) "Compressing & uploading photo..." else "Mengompresi & mengunggah foto...",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WaliPrimary
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    enabled = !isSubmitting && currentAccount != null,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .heightIn(min = 44.dp)
                                        .testTag("btn_pick_wali_photo")
                                ) {
                                    Icon(
                                        imageVector = if (hasPhoto) Icons.Default.PhotoLibrary else Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (hasPhoto) {
                                            if (isEn) "Change Photo" else "Ganti Foto"
                                        } else {
                                            if (isEn) "Upload Photo" else "Pilih Foto"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (hasPhoto) {
                                    OutlinedButton(
                                        onClick = { showPhotoPreview = true },
                                        enabled = !isSubmitting,
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .heightIn(min = 44.dp)
                                            .testTag("btn_preview_wali_photo")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = if (isEn) "Preview" else "Lihat",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showConfirmRemovePhoto = true },
                                        enabled = !isSubmitting,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .heightIn(min = 44.dp)
                                            .testTag("btn_remove_wali_photo")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = if (isEn) "Remove photo" else "Hapus foto",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            if (currentAccount == null) {
                                Text(
                                    text = if (isEn) "Save your profile first before uploading a photo." else "Simpan profil terlebih dahulu sebelum mengunggah foto.",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = if (isEn) {
                                        "Your photo & official profile appear on the Parent Portal to verify caregiver legitimacy."
                                    } else {
                                        "Foto & identitas resmi ini tampil di Portal Orang Tua untuk memperkuat legalitas akun."
                                    },
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // Kotak Pemberitahuan Transparansi Admin Sekolah & Portal Orang Tua
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier
                                .size(22.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (isEn) "Synced to Admin & Parent Portal" else "Tersinkron ke Admin & Portal Orang Tua",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                            Text(
                                text = if (isEn) {
                                    "Profile details (name, photo, title, ID, contact & bio) appear on School Admin and Parent Portal. Your login username & password stay hidden from parents."
                                } else {
                                    "Foto, nama, jabatan, NIP/ID, kontak & bio otomatis tampil di Admin Sekolah dan Portal Orang Tua. Username & kata sandi login tetap aman tersembunyi dari orang tua."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF0C4A6E),
                                lineHeight = 15.5.sp
                            )
                        }
                    }
                }

                // Ringkasan Akun Wali Asuh
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Login Username:" else "Username Login:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = activeUsername,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WaliPrimary
                            )
                        }

                        if (currentAccount?.dormitory?.isNotBlank() == true) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "Asrama Duty:" else "Tugas Asrama:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currentAccount.dormitory,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "School:" else "Sekolah:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = schoolName,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (currentExistingPassword.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "Current Password:" else "Kata Sandi Saat Ini:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (currentPasswordVisible) currentExistingPassword else "••••••••",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { currentPasswordVisible = !currentPasswordVisible },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (currentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (currentPasswordVisible) {
                                                if (isEn) "Hide" else "Sembunyikan"
                                            } else {
                                                if (isEn) "Show" else "Lihat"
                                            },
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                // Input Nama Wali Asuh
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        errorMessage = null
                    },
                    label = { Text(if (isEn) "Caregiver Full Name & Title *" else "Nama Lengkap & Gelar Pembina *") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = WaliPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_wali_name")
                )

                // Input Jabatan / Peran Resmi
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = {
                        titleInput = it
                        errorMessage = null
                    },
                    label = { Text(if (isEn) "Official Role / Title (Optional)" else "Jabatan / Peran Resmi (Opsional)") },
                    placeholder = { Text(if (isEn) "Example: Senior Dorm Caregiver" else "Contoh: Pembina Asrama Putra / Musyrif") },
                    leadingIcon = {
                        Icon(Icons.Default.WorkOutline, contentDescription = null, tint = WaliPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_wali_title")
                )

                // Input Nomor WhatsApp & NIP/ID Pembina
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = {
                            phoneInput = it
                            errorMessage = null
                        },
                        label = { Text(if (isEn) "WhatsApp No." else "No. WhatsApp") },
                        placeholder = { Text("08xxxxxxxxxx") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = WaliPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_edit_wali_phone")
                    )

                    OutlinedTextField(
                        value = employeeCodeInput,
                        onValueChange = {
                            employeeCodeInput = it
                            errorMessage = null
                        },
                        label = { Text(if (isEn) "NIP / ID" else "NIP / ID") },
                        placeholder = { Text("Opsional") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = WaliPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_edit_wali_employee_code")
                    )
                }

                // Input Bio / Sambutan Singkat
                OutlinedTextField(
                    value = bioInput,
                    onValueChange = {
                        bioInput = it
                        errorMessage = null
                    },
                    label = { Text(if (isEn) "Short Bio / Message for Parents" else "Profil Singkat / Sambutan ke Orang Tua") },
                    placeholder = {
                        Text(
                            if (isEn) "Example: Dedicated to mentoring students' health & character."
                            else "Contoh: Siap mendampingi kesehatan, hafalan & akhlak ananda."
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = WaliPrimary)
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_wali_bio")
                )

                // Seksi Kata Sandi (Opsional jika akun sudah ada)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (currentAccount != null) {
                                if (isEn) "Change Login Password (Optional)" else "Ganti Kata Sandi Login (Opsional)"
                            } else {
                                if (isEn) "Set Login Password *" else "Buat Kata Sandi Login *"
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentAccount != null) {
                                if (isEn) "Leave blank if you only want to update your profile." else "Kosongkan jika hanya ingin mengubah foto atau biodata profil."
                            } else {
                                if (isEn) "Minimum 6 characters for your next login." else "Minimal 6 karakter untuk login berikutnya."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    if (currentAccount != null) {
                                        if (isEn) "New Password (Optional)" else "Kata Sandi Baru (Opsional)"
                                    } else {
                                        if (isEn) "New Password *" else "Kata Sandi Baru *"
                                    }
                                )
                            },
                            placeholder = { Text(if (isEn) "Min. 6 characters" else "Minimal 6 karakter") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = WaliPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (newPasswordVisible) {
                                            if (isEn) "Hide" else "Sembunyikan"
                                        } else {
                                            if (isEn) "Show" else "Lihat"
                                        },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_wali_new_password")
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            label = { Text(if (isEn) "Confirm New Password" else "Ulangi Kata Sandi Baru") },
                            placeholder = { Text(if (isEn) "Retype new password" else "Ketik ulang kata sandi baru") },
                            leadingIcon = {
                                Icon(Icons.Default.LockReset, contentDescription = null, tint = WaliPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (confirmPasswordVisible) {
                                            if (isEn) "Hide" else "Sembunyikan"
                                        } else {
                                            if (isEn) "Show" else "Lihat"
                                        },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_wali_confirm_password")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tombol Aksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSubmitting && !isUploadingPhoto,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(s.cancel)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val cleanName = nameInput.trim()
                            val cleanNewPass = newPassword.trim()
                            val cleanConfirm = confirmPassword.trim()

                            if (cleanName.isBlank()) {
                                errorMessage = if (isEn) {
                                    "Caregiver name cannot be empty."
                                } else {
                                    "Nama wali / pengasuh tidak boleh kosong."
                                }
                                return@Button
                            }

                            val wantsPasswordChange = cleanNewPass.isNotEmpty() || cleanConfirm.isNotEmpty() || currentAccount == null
                            if (wantsPasswordChange) {
                                if (cleanNewPass.isBlank()) {
                                    errorMessage = if (isEn) {
                                        "Please enter a new password."
                                    } else {
                                        "Silakan masukkan kata sandi baru."
                                    }
                                    return@Button
                                }
                                if (cleanNewPass.length < 6) {
                                    errorMessage = if (isEn) {
                                        "New password must be at least 6 characters."
                                    } else {
                                        "Kata sandi baru minimal 6 karakter."
                                    }
                                    return@Button
                                }
                                if (cleanNewPass != cleanConfirm) {
                                    errorMessage = if (isEn) {
                                        "Password confirmation does not match. Please check again."
                                    } else {
                                        "Konfirmasi kata sandi tidak cocok. Silakan periksa kembali."
                                    }
                                    return@Button
                                }
                            }

                            isSubmitting = true
                            viewModel.updateWaliSelfProfile(
                                newName = cleanName,
                                newPhone = phoneInput.trim(),
                                newTitlePosition = titleInput.trim(),
                                newEmployeeCode = employeeCodeInput.trim(),
                                newBio = bioInput.trim(),
                                newPassword = if (wantsPasswordChange) cleanNewPass else null
                            ) { success, msg ->
                                isSubmitting = false
                                if (success) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                        enabled = !isSubmitting && !isUploadingPhoto,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_save_wali_password")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isSubmitting) {
                                if (isEn) "Saving..." else "Menyimpan..."
                            } else {
                                if (isEn) "Save Profile" else "Simpan Profil"
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPhotoPreview && hasPhoto) {
        StudentPhotoPreviewDialog(
            studentName = nameInput.ifBlank { currentWaliName },
            gender = avatarGender,
            subtitle = "${titleInput.ifBlank { currentAccount?.roleLabel(isEn) ?: "Wali Asuh" }} • $schoolName",
            photoUrl = photoUrl,
            photoFallbackUrl = photoFallbackUrl,
            onDismiss = { showPhotoPreview = false }
        )
    }

    if (showConfirmRemovePhoto) {
        AlertDialog(
            onDismissRequest = { showConfirmRemovePhoto = false },
            title = {
                Text(
                    text = if (isEn) "Remove Profile Photo?" else "Hapus Foto Profil?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Your profile avatar will revert to initials on both Admin and Parent Portal."
                    } else {
                        "Foto profil Anda akan dihapus dan dikembalikan ke inisial di Admin maupun Portal Orang Tua."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRemovePhoto = false
                        viewModel.removeWaliProfilePhoto { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("btn_confirm_remove_wali_photo")
                ) {
                    Text(if (isEn) "Remove" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRemovePhoto = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}
