package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaliPrimary
import com.example.util.PasswordGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWaliAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, username: String, dormitory: String, phone: String, password: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var userEditedUsername by remember { mutableStateOf(false) }
    var selectedDormitory by remember { mutableStateOf("Asrama Putra") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf(PasswordGenerator.generateRandomPassword()) }
    var passwordVisible by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tambah Akun Wali Asuh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Daftarkan akun login wali asuh untuk Sekolah Rakyat SRT 1 Kediri. Username & password akan ditampilkan di Kartu Akses untuk login ke aplikasi.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Nama Lengkap Wali Asuh
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (!userEditedUsername) {
                            username = it.lowercase().replace(" ", "").filter { ch -> ch.isLetterOrDigit() }
                        }
                        errorMessage = null
                    },
                    label = { Text("Nama Lengkap Wali Asuh *") },
                    placeholder = { Text("Contoh: Sari Devi, M.Pd") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = WaliPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_wali_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Username Login
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        userEditedUsername = true
                        errorMessage = null
                    },
                    label = { Text("Username Login Aplikasi *") },
                    placeholder = { Text("Contoh: saridevi") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = WaliPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_wali_username"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Pilihan Asrama Binaan
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Tugas Asrama:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Asrama Putra", "Asrama Putri", "Umum").forEach { dorm ->
                            FilterChip(
                                selected = selectedDormitory == dorm,
                                onClick = { selectedDormitory = dorm },
                                label = { Text(dorm, fontSize = 11.sp) },
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }

                // Nomor WhatsApp (Opsional)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor WhatsApp (Opsional)") },
                    placeholder = { Text("Contoh: 08123456789") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = WaliPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_wali_phone"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Password Acak Otomatis
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Password Login Aplikasi:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { password = PasswordGenerator.generateRandomPassword() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Acak Ulang Password",
                                tint = WaliPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password Acak Otomatis") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = WaliPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Sembunyikan" else "Lihat",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_wali_password"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Nama wali asuh tidak boleh kosong."
                            } else if (username.isBlank()) {
                                errorMessage = "Username tidak boleh kosong."
                            } else if (password.isBlank()) {
                                errorMessage = "Password tidak boleh kosong."
                            } else {
                                onConfirm(name.trim(), username.trim().lowercase(), selectedDormitory, phone.trim(), password.trim())
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_save_wali_account")
                    ) {
                        Text("Simpan & Buat Kartu")
                    }
                }
            }
        }
    }
}
