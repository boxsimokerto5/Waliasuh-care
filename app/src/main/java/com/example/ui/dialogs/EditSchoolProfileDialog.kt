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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolProfile
import com.example.ui.theme.WaliPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSchoolProfileDialog(
    currentProfile: SchoolProfile,
    onDismiss: () -> Unit,
    onSave: (SchoolProfile) -> Unit
) {
    var schoolName by remember { mutableStateOf(currentProfile.schoolName) }
    var shortName by remember { mutableStateOf(currentProfile.shortName) }
    var adminName by remember { mutableStateOf(currentProfile.adminName) }
    var city by remember { mutableStateOf(currentProfile.city) }
    var tenantCode by remember { mutableStateOf(currentProfile.tenantCode) }
    var primaryFaskesName by remember { mutableStateOf(currentProfile.primaryFaskesName) }
    var secondaryFaskesName by remember { mutableStateOf(currentProfile.secondaryFaskesName) }
    val additionalFaskes = remember {
        mutableStateListOf<String>().apply {
            addAll(currentProfile.additionalFaskesList)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Profil Sekolah & Faskes",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sesuaikan identitas sekolah dan faskes rujukan",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("Nama Lengkap Sekolah / Yayasan") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("school_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = shortName,
                        onValueChange = { shortName = it },
                        label = { Text("Nama Singkat") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("short_name_input")
                    )

                    OutlinedTextField(
                        value = tenantCode,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Kode Tenant (Terkunci)") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Kode Tenant Terkunci",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tenant_code_input")
                    )
                }

                // Helper Text pencegah error database
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kode Tenant dikunci oleh sistem untuk mencegah error relasi database siswa & wali.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Kota / Kabupaten") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("city_input")
                    )

                    OutlinedTextField(
                        value = adminName,
                        onValueChange = { adminName = it },
                        label = { Text("Nama Admin Utama") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_name_input")
                    )
                }

                // Section: Fasilitas Kesehatan (Faskes)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daftar Fasilitas Kesehatan (Faskes)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Faskes utama adalah UKS Sekolah untuk penanganan internal, sedangkan faskes rujukan selanjutnya dapat ditambah sesuai kebutuhan sekolah.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Faskes 1: Utama (UKS Sekolah)
                    OutlinedTextField(
                        value = primaryFaskesName,
                        onValueChange = { primaryFaskesName = it },
                        label = { Text("Faskes 1: Utama (UKS Sekolah)") },
                        placeholder = { Text("UKS Sekolah") },
                        leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null, tint = WaliPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("primary_faskes_input")
                    )

                    // Faskes 2: Rujukan Pertama
                    OutlinedTextField(
                        value = secondaryFaskesName,
                        onValueChange = { secondaryFaskesName = it },
                        label = { Text("Faskes 2: Rujukan Pertama (misal: Puskesmas)") },
                        placeholder = { Text("misal: Puskesmas Wates") },
                        leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("secondary_faskes_input")
                    )

                    // Faskes Tambahan (Faskes 3, Faskes 4, dst.)
                    additionalFaskes.forEachIndexed { index, faskesVal ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = faskesVal,
                                onValueChange = { additionalFaskes[index] = it },
                                label = { Text("Faskes ${index + 3}: Rujukan Tambahan") },
                                placeholder = { Text("misal: RSUD / Puskesmas 2 / Klinik") },
                                leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("additional_faskes_input_$index")
                            )

                            IconButton(
                                onClick = { additionalFaskes.removeAt(index) },
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Faskes",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Tombol Tambah Faskes Rujukan
                    OutlinedButton(
                        onClick = { additionalFaskes.add("") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_add_faskes_field"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Tambah Faskes Rujukan (${additionalFaskes.size + 3})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (schoolName.isNotBlank() && shortName.isNotBlank()) {
                                onSave(
                                    currentProfile.copy(
                                        schoolName = schoolName.trim(),
                                        shortName = shortName.trim(),
                                        adminName = adminName.trim().ifBlank { "Admin Utama" },
                                        city = city.trim().ifBlank { "Kabupaten Kediri" },
                                        tenantCode = currentProfile.tenantCode.ifBlank { tenantCode.trim() },
                                        primaryFaskesName = primaryFaskesName.trim().ifBlank { "UKS Sekolah" },
                                        secondaryFaskesName = secondaryFaskesName.trim().ifBlank { "Puskesmas Wates" },
                                        additionalFaskesList = additionalFaskes.map { it.trim() }.filter { it.isNotBlank() }
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_school_profile_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
