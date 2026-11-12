package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalRecord
import com.example.ui.components.FaskesBadge
import com.example.ui.theme.StatusDeparted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DepartReferralDialog(
    record: MedicalRecord,
    allFaskesList: List<String> = emptyList(),
    defaultEscortName: String = "",
    onDismiss: () -> Unit,
    onConfirmDeparture: (targetFaskes: String, escortName: String) -> Unit,
    onDirectDiagnose: ((targetFaskes: String, escortName: String) -> Unit)? = null
) {
    // Faskes rujukan adalah faskes di luar UKS
    val referralFaskesOptions = remember(allFaskesList) {
        val nonUks = allFaskesList.filter { !it.contains("UKS", ignoreCase = true) }
        if (nonUks.isNotEmpty()) nonUks else listOf("Puskesmas Wates", "Puskesmas Plosoklaten")
    }

    var selectedFaskes by remember {
        val initial = if (record.faskes.contains("UKS", ignoreCase = true)) {
            referralFaskesOptions.firstOrNull() ?: "Puskesmas Wates"
        } else {
            record.faskes
        }
        mutableStateOf(initial)
    }

    var isCustomFaskes by remember {
        mutableStateOf(!referralFaskesOptions.contains(selectedFaskes))
    }
    var customFaskesText by remember {
        mutableStateOf(if (isCustomFaskes) selectedFaskes else "")
    }

    var escortName by remember {
        mutableStateOf(record.escortName.ifBlank { defaultEscortName })
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
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusDeparted.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = StatusDeparted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rujuk & Berangkatkan Siswa",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pilih faskes rujukan lanjutan di luar UKS",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Info Siswa Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = record.studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            FaskesBadge(faskes = record.faskes)
                        }
                        Text(
                            text = "${record.studentDormitory} • Keluhan: ${record.complaint}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Pilih Faskes Rujukan
                Text(
                    text = "Pilih Faskes Rujukan Tujuan *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    referralFaskesOptions.forEach { faskesName ->
                        val isSelected = !isCustomFaskes && selectedFaskes == faskesName
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustomFaskes = false
                                selectedFaskes = faskesName
                            },
                            label = {
                                Text(
                                    text = faskesName,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusDeparted.copy(alpha = 0.2f),
                                selectedLabelColor = StatusDeparted
                            )
                        )
                    }

                    // Opsi Faskes Lainnya
                    FilterChip(
                        selected = isCustomFaskes,
                        onClick = {
                            isCustomFaskes = true
                        },
                        label = {
                            Text(
                                text = "Lainnya...",
                                fontSize = 11.5.sp,
                                fontWeight = if (isCustomFaskes) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                if (isCustomFaskes) {
                    OutlinedTextField(
                        value = customFaskesText,
                        onValueChange = { customFaskesText = it },
                        label = { Text("Ketik Nama Faskes / RS Rujukan *", fontSize = 12.sp) },
                        placeholder = { Text("misal: RSUD Gambiran / RS Baptis", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_referral_faskes")
                    )
                }

                // Nama Pendamping
                OutlinedTextField(
                    value = escortName,
                    onValueChange = { escortName = it },
                    label = { Text("Nama Petugas Pendamping / Pengantar *", fontSize = 12.sp) },
                    placeholder = { Text("misal: Ustadz Ahmad / Bu Nur", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_referral_escort")
                )

                Spacer(modifier = Modifier.height(4.dp))

                val finalFaskes = if (isCustomFaskes) customFaskesText.trim() else selectedFaskes.trim()
                val isFormValid = finalFaskes.isNotBlank() && escortName.isNotBlank()

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Batal", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (isFormValid) {
                                    onConfirmDeparture(finalFaskes, escortName.trim())
                                }
                            },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDeparted),
                            modifier = Modifier.testTag("btn_confirm_referral_departure")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Berangkatkan Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (onDirectDiagnose != null) {
                        OutlinedButton(
                            onClick = {
                                if (isFormValid) {
                                    onDirectDiagnose(finalFaskes, escortName.trim())
                                }
                            },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sudah Pulang? Langsung Input Hasil Diagnosa",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
