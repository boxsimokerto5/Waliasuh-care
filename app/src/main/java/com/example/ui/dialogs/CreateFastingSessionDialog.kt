package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFastingSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dateMillis: Long, description: String) -> Unit
) {
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    var title by remember { mutableStateOf("Puasa Senin Kamis") }
    var description by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val quickTitles = listOf(
        "Puasa Senin Kamis",
        "Puasa Ayyamul Bidh",
        "Puasa Ramadhan",
        "Puasa Daud",
        "Puasa Sunnah"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = Color(0xFF0F766E))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEn) "Create New Puasa Session" else "Buat Sesi Puasa Baru",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isEn) "Quick select Puasa type:" else "Pilih cepat jenis puasa:",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTitles.forEach { qt ->
                        FilterChip(
                            selected = title.equals(qt, ignoreCase = true),
                            onClick = { title = qt },
                            label = { Text(qt, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isEn) "Puasa Session Name *" else "Nama Sesi Puasa *") },
                    placeholder = { Text(if (isEn) "Example: Puasa Senin Kamis" else "Contoh: Puasa Senin Kamis") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fasting_session_title")
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_pick_fasting_session_date")
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF0F766E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${if (isEn) "Date" else "Tanggal"}: ${DateUtils.formatFullDate(selectedDateMillis, appStrings.language)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isEn) "Notes (Optional)" else "Keterangan (Opsional)") },
                    placeholder = {
                        Text(
                            if (isEn) "Example: Sahur at 03:30 WIB in common kitchen" else "Contoh: Sahur pukul 03.30 WIB di dapur umum"
                        )
                    },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), selectedDateMillis, description.trim())
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_save_fasting_session")
            ) {
                Text(if (isEn) "Create Session" else "Buat Sesi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(appStrings.cancel)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text(if (isEn) "Select" else "Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(appStrings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
