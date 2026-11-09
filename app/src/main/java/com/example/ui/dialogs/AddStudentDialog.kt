package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        nis: String,
        gender: String,
        age: Int,
        dormitory: String,
        classGrade: String,
        allergies: String,
        bloodType: String,
        emergencyContact: String,
        specialNotes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nis by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("L") }
    var ageText by remember { mutableStateOf("12") }
    var dormitory by remember { mutableStateOf("Asrama Putra Wates") }
    var classGrade by remember { mutableStateOf("Kelas 7 SMP") }
    var allergies by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("O") }
    var emergencyContact by remember { mutableStateOf("Ust. Ilham") }
    var specialNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 500.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tambah Anak Asuh",
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Siswa *", fontSize = 12.sp) },
                    placeholder = { Text("misal: Fauzan Abdullah", fontSize = 12.sp) },
                    singleLine = true,
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
                        label = { Text("NIS", fontSize = 12.sp) },
                        placeholder = { Text("SRT1-2024-xx", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Usia (Th)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }

                Text("Jenis Kelamin:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = gender == "L",
                        onClick = { gender = "L" },
                        label = { Text("Putra (L)", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = gender == "P",
                        onClick = { gender = "P" },
                        label = { Text("Putri (P)", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }

                OutlinedTextField(
                    value = dormitory,
                    onValueChange = { dormitory = it },
                    label = { Text("Asrama / Kamar *", fontSize = 12.sp) },
                    placeholder = { Text("misal: Asrama Putra Al-Fatih", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = classGrade,
                    onValueChange = { classGrade = it },
                    label = { Text("Tingkat Kelas", fontSize = 12.sp) },
                    placeholder = { Text("misal: Kelas 7 SMP", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Riwayat Alergi (jika ada)", fontSize = 12.sp) },
                    placeholder = { Text("misal: Alergi dingin, amoxicillin", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Kontak Pengasuh", fontSize = 12.sp) },
                    placeholder = { Text("0812-xxxx-xxxx (Ust. Ilham)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val age = ageText.toIntOrNull() ?: 12
                    if (name.isNotBlank() && dormitory.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            if (nis.isBlank()) "SRT1-${System.currentTimeMillis() % 1000}" else nis.trim(),
                            gender,
                            age,
                            dormitory.trim(),
                            classGrade.trim(),
                            allergies.trim(),
                            bloodType,
                            emergencyContact.trim(),
                            specialNotes.trim()
                        )
                    }
                },
                enabled = name.isNotBlank() && dormitory.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_student_button")
            ) {
                Text("Tambah", fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal", fontSize = 12.sp)
            }
        }
    )
}
