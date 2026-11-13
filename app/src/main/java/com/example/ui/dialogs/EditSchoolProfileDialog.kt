package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FaskesContactInfo
import com.example.data.model.SchoolProfile
import com.example.ui.theme.WaliPrimary

private data class FaskesDraft(
    val name: String = "",
    val address: String = "",
    val phone: String = ""
)

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

    val initialPrimary = remember(currentProfile) {
        currentProfile.getFaskesContact(currentProfile.primaryFaskesName.ifBlank { "UKS Sekolah" })
    }
    var primaryFaskesName by remember { mutableStateOf(currentProfile.primaryFaskesName) }
    var primaryFaskesAddress by remember {
        mutableStateOf(
            currentProfile.faskesContacts.entries
                .firstOrNull { it.key.equals(currentProfile.primaryFaskesName.trim(), ignoreCase = true) }
                ?.value?.address
                ?: ""
        )
    }
    var primaryFaskesPhone by remember { mutableStateOf(initialPrimary.phone) }

    val initialSecondary = remember(currentProfile) {
        if (currentProfile.secondaryFaskesName.isNotBlank()) {
            currentProfile.getFaskesContact(currentProfile.secondaryFaskesName)
        } else {
            FaskesContactInfo("")
        }
    }
    var secondaryFaskesName by remember { mutableStateOf(currentProfile.secondaryFaskesName) }
    var secondaryFaskesAddress by remember { mutableStateOf(initialSecondary.address) }
    var secondaryFaskesPhone by remember { mutableStateOf(initialSecondary.phone) }

    val additionalFaskes = remember {
        mutableStateListOf<FaskesDraft>().apply {
            currentProfile.additionalFaskesList.forEach { name ->
                val info = currentProfile.getFaskesContact(name)
                add(FaskesDraft(name = name, address = info.address, phone = info.phone))
            }
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
                            text = "Sesuaikan identitas sekolah, faskes rujukan, alamat & telepon",
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
                            text = "Nama, alamat, dan nomor telepon faskes akan otomatis tampil pada menu Puskesmas Saya & Info Faskes untuk seluruh Wali Asuh di sekolah ini.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Faskes 1: Utama (UKS Sekolah)
                    FaskesEditorCard(
                        title = "Faskes 1 • Utama (Internal)",
                        nameLabel = "Nama Faskes Utama",
                        namePlaceholder = "UKS Sekolah",
                        name = primaryFaskesName,
                        onNameChange = { primaryFaskesName = it },
                        address = primaryFaskesAddress,
                        onAddressChange = { primaryFaskesAddress = it },
                        phone = primaryFaskesPhone,
                        onPhoneChange = { primaryFaskesPhone = it },
                        nameTestTag = "primary_faskes_input",
                        addressTestTag = "primary_faskes_address_input",
                        phoneTestTag = "primary_faskes_phone_input"
                    )

                    // Faskes 2: Rujukan Pertama
                    FaskesEditorCard(
                        title = "Faskes 2 • Rujukan Pertama",
                        nameLabel = "Nama Puskesmas / Klinik Rujukan",
                        namePlaceholder = "misal: Puskesmas Kecamatan",
                        name = secondaryFaskesName,
                        onNameChange = { secondaryFaskesName = it },
                        address = secondaryFaskesAddress,
                        onAddressChange = { secondaryFaskesAddress = it },
                        phone = secondaryFaskesPhone,
                        onPhoneChange = { secondaryFaskesPhone = it },
                        nameTestTag = "secondary_faskes_input",
                        addressTestTag = "secondary_faskes_address_input",
                        phoneTestTag = "secondary_faskes_phone_input"
                    )

                    // Faskes Tambahan (Faskes 3, Faskes 4, dst.)
                    additionalFaskes.forEachIndexed { index, draft ->
                        FaskesEditorCard(
                            title = "Faskes ${index + 3} • Rujukan Tambahan",
                            nameLabel = "Nama Faskes Rujukan",
                            namePlaceholder = "misal: RSUD / Puskesmas 2 / Klinik",
                            name = draft.name,
                            onNameChange = { additionalFaskes[index] = draft.copy(name = it) },
                            address = draft.address,
                            onAddressChange = { additionalFaskes[index] = draft.copy(address = it) },
                            phone = draft.phone,
                            onPhoneChange = { additionalFaskes[index] = draft.copy(phone = it) },
                            nameTestTag = "additional_faskes_input_$index",
                            addressTestTag = "additional_faskes_address_input_$index",
                            phoneTestTag = "additional_faskes_phone_input_$index",
                            onRemove = { additionalFaskes.removeAt(index) }
                        )
                    }

                    // Tombol Tambah Faskes Rujukan
                    OutlinedButton(
                        onClick = { additionalFaskes.add(FaskesDraft()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_add_faskes_field"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tambah Faskes Rujukan (${additionalFaskes.size + 3})",
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
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (schoolName.isNotBlank() && shortName.isNotBlank()) {
                                val resolvedPrimary = primaryFaskesName.trim().ifBlank { "UKS Sekolah" }
                                val resolvedSecondary = secondaryFaskesName.trim()
                                val cleanedAdditional = additionalFaskes
                                    .map { it.copy(name = it.name.trim(), address = it.address.trim(), phone = it.phone.trim()) }
                                    .filter { it.name.isNotBlank() }

                                val contactMap = linkedMapOf<String, FaskesContactInfo>()
                                if (primaryFaskesAddress.isNotBlank() || primaryFaskesPhone.isNotBlank()) {
                                    contactMap[resolvedPrimary] = FaskesContactInfo(
                                        name = resolvedPrimary,
                                        address = primaryFaskesAddress.trim(),
                                        phone = primaryFaskesPhone.trim()
                                    )
                                }
                                if (resolvedSecondary.isNotBlank() &&
                                    (secondaryFaskesAddress.isNotBlank() || secondaryFaskesPhone.isNotBlank())
                                ) {
                                    contactMap[resolvedSecondary] = FaskesContactInfo(
                                        name = resolvedSecondary,
                                        address = secondaryFaskesAddress.trim(),
                                        phone = secondaryFaskesPhone.trim()
                                    )
                                }
                                cleanedAdditional.forEach { draft ->
                                    if (draft.address.isNotBlank() || draft.phone.isNotBlank()) {
                                        contactMap[draft.name] = FaskesContactInfo(
                                            name = draft.name,
                                            address = draft.address,
                                            phone = draft.phone
                                        )
                                    }
                                }

                                onSave(
                                    currentProfile.copy(
                                        schoolName = schoolName.trim(),
                                        shortName = shortName.trim(),
                                        adminName = adminName.trim().ifBlank { "Admin Utama" },
                                        city = city.trim().ifBlank { "Indonesia" },
                                        tenantCode = currentProfile.tenantCode.ifBlank { tenantCode.trim() },
                                        primaryFaskesName = resolvedPrimary,
                                        secondaryFaskesName = resolvedSecondary,
                                        additionalFaskesList = cleanedAdditional.map { it.name },
                                        faskesContacts = contactMap
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("save_school_profile_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
private fun FaskesEditorCard(
    title: String,
    nameLabel: String,
    namePlaceholder: String,
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    nameTestTag: String,
    addressTestTag: String,
    phoneTestTag: String,
    onRemove: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaliPrimary
                )
                if (onRemove != null) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus $title",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(nameLabel, fontSize = 12.sp) },
                placeholder = { Text(namePlaceholder, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(nameTestTag)
            )

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("Alamat Faskes (opsional)", fontSize = 12.sp) },
                placeholder = { Text("Jl. ..., Kec. ..., Kab./Kota ...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(addressTestTag)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("No. Telepon / IGD (opsional)", fontSize = 12.sp) },
                placeholder = { Text("misal: (0354) 441012 / 08...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(phoneTestTag)
            )
        }
    }
}
