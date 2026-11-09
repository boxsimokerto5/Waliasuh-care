package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalRecord
import com.example.ui.components.FaskesBadge
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun InputTreatmentResultDialog(
    record: MedicalRecord,
    onDismiss: () -> Unit,
    onConfirm: (
        diagnosis: String,
        actionsTaken: String,
        prescription: String,
        needsControl: Boolean,
        controlDate: Long?,
        notes: String
    ) -> Unit
) {
    var diagnosis by remember { mutableStateOf("") }
    var actionsTaken by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    var needsControl by remember { mutableStateOf(false) }

    val calIn3Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.timeInMillis
    }
    val calIn5Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 5) }.timeInMillis
    }
    val calIn7Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
    }

    var selectedControlDateMillis by remember { mutableLongStateOf(calIn3Days) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 500.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Input Diagnosa & Hasil Berobat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info Banner (Compact)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = record.studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            FaskesBadge(faskes = record.faskes)
                        }
                        Text(
                            text = "${record.studentDormitory} • Keluhan: ${record.complaint}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Diagnosa
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Diagnosa Dokter / Nakes *", fontSize = 12.sp) },
                    placeholder = { Text("misal: ISPA / Faringitis Akut", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_diagnosis")
                )

                // Tindakan
                OutlinedTextField(
                    value = actionsTaken,
                    onValueChange = { actionsTaken = it },
                    label = { Text("Tindakan Medis di Puskesmas", fontSize = 12.sp) },
                    placeholder = { Text("misal: Pemeriksaan fisik, pembersihan luka", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Resep Obat
                OutlinedTextField(
                    value = prescription,
                    onValueChange = { prescription = it },
                    label = { Text("Resep Obat & Aturan Minum *", fontSize = 12.sp) },
                    placeholder = { Text("misal: Paracetamol 3x1, Amoxicillin 3x1", fontSize = 12.sp) },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_prescription")
                )

                // Pilihan Kontrol Kembali (Compact Card)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (needsControl) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Jadwal Kontrol Kembali",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (needsControl) "Anak wajib periksa ulang" else "Centang/aktifkan jika perlu kontrol",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = needsControl,
                                onCheckedChange = { needsControl = it },
                                modifier = Modifier.testTag("switch_needs_control")
                            )
                        }

                        if (needsControl) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                FilterChip(
                                    selected = selectedControlDateMillis == calIn3Days,
                                    onClick = { selectedControlDateMillis = calIn3Days },
                                    label = { Text("+3 Hari", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                                FilterChip(
                                    selected = selectedControlDateMillis == calIn5Days,
                                    onClick = { selectedControlDateMillis = calIn5Days },
                                    label = { Text("+5 Hari", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                                FilterChip(
                                    selected = selectedControlDateMillis == calIn7Days,
                                    onClick = { selectedControlDateMillis = calIn7Days },
                                    label = { Text("+1 Minggu", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Tanggal Kontrol: ${DateUtils.formatFullDate(selectedControlDateMillis)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tidak perlu kontrol kembali (Tuntas)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Catatan
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = { Text("Catatan Dokter / Pantauan Asrama", fontSize = 12.sp) },
                    placeholder = { Text("misal: Istirahat di kamar UKS", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (diagnosis.isNotBlank() && prescription.isNotBlank()) {
                        onConfirm(
                            diagnosis.trim(),
                            actionsTaken.trim(),
                            prescription.trim(),
                            needsControl,
                            if (needsControl) selectedControlDateMillis else null,
                            additionalNotes.trim()
                        )
                    }
                },
                enabled = diagnosis.isNotBlank() && prescription.isNotBlank(),
                modifier = Modifier.testTag("confirm_submit_treatment_button")
            ) {
                Text("Simpan & Selesaikan", fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal", fontSize = 12.sp)
            }
        }
    )
}
