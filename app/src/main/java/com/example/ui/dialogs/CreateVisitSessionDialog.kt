@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.VisitHelper
import com.example.data.model.VisitSession
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val VisitIndigo = Color(0xFF1D4ED8)
private val VisitIndigoLight = Color(0xFFDBEAFE)

@Composable
fun CreateVisitSessionDialog(
    students: List<ChildStudent>,
    initialSession: VisitSession? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        monthYear: String,
        visitDates: List<Long>,
        selectedDistricts: List<String>,
        description: String
    ) -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val locale = s.language.locale

    val defaultMonthYear = remember(initialSession) {
        initialSession?.monthYear?.ifBlank { VisitHelper.buildDefaultMonthYear() }
            ?: VisitHelper.buildDefaultMonthYear()
    }

    val monthChoices = remember(defaultMonthYear) {
        val cal = Calendar.getInstance()
        val list = mutableListOf<String>()
        cal.add(Calendar.MONTH, -1)
        repeat(5) {
            list.add(SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time))
            cal.add(Calendar.MONTH, 1)
        }
        if (defaultMonthYear.isNotBlank() && defaultMonthYear !in list) {
            listOf(defaultMonthYear) + list
        } else {
            list
        }
    }

    var selectedMonthYear by remember { mutableStateOf(defaultMonthYear) }
    var title by remember {
        mutableStateOf(
            initialSession?.title ?: VisitHelper.defaultSessionTitleForMonthYear(defaultMonthYear, locale, isEn)
        )
    }
    var hasCustomTitle by remember { mutableStateOf(initialSession != null) }

    var selectedDates by remember {
        mutableStateOf(
            initialSession?.getVisitDatesList()?.takeIf { it.isNotEmpty() }
                ?: computeDefaultVisitSundays(defaultMonthYear).take(2)
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val dynamicDistricts = remember(students, isEn) {
        VisitHelper.extractDynamicDistricts(students, isEn)
    }

    var selectedDistrictKeys by remember {
        mutableStateOf(
            initialSession?.getDistrictsList()?.toSet() ?: emptySet()
        )
    }
    var description by remember { mutableStateOf(initialSession?.description ?: "") }

    val dateFormatter = remember(locale) { SimpleDateFormat("EEE, d MMM yyyy", locale) }

    val totalScheduledCount = remember(students, selectedDistrictKeys) {
        if (selectedDistrictKeys.isEmpty()) 0
        else {
            val allowedLower = selectedDistrictKeys.map { it.lowercase(Locale.US) }.toSet()
            students.count { st ->
                VisitHelper.resolveStudentDistrictKey(st).lowercase(Locale.US) in allowedLower
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = VisitIndigo)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialSession != null) {
                        if (isEn) "Edit Visit Session" else "Edit Sesi Kunjungan"
                    } else {
                        if (isEn) "Create Monthly Visit Session" else "Buat Sesi Kunjungan Bulanan"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Pilihan Bulan Sesi
                Text(
                    text = if (isEn) "1. Session Month:" else "1. Bulan Sesi Kunjungan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VisitIndigo
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    monthChoices.forEach { ym ->
                        val selected = selectedMonthYear == ym
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedMonthYear = ym
                                if (!hasCustomTitle) {
                                    title = VisitHelper.defaultSessionTitleForMonthYear(ym, locale, isEn)
                                }
                            },
                            label = {
                                Text(
                                    text = VisitHelper.formatMonthYearLabel(ym, locale),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VisitIndigoLight,
                                selectedLabelColor = VisitIndigo
                            ),
                            modifier = Modifier.heightIn(min = 36.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        hasCustomTitle = true
                    },
                    label = { Text(if (isEn) "Session Title *" else "Nama Sesi Kunjungan *") },
                    placeholder = { Text(if (isEn) "e.g., Parent Visit November 2026" else "Contoh: Kunjungan November 2026") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_visit_session_title")
                )

                // 2. Beberapa Tanggal dalam Satu Sesi (Minggu 2, 3, 4, dll.)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEn) {
                            "2. Visit Dates in This Session (${selectedDates.size} dates):"
                        } else {
                            "2. Tanggal Jadwal dalam Sesi Ini (${selectedDates.size} tanggal):"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = VisitIndigo
                    )
                    Text(
                        text = if (isEn) {
                            "One monthly session can have multiple dates (e.g., 2nd Sunday, 3rd Sunday, 4th Sunday)."
                        } else {
                            "Satu sesi bulan dapat memiliki beberapa tanggal (mis. Minggu ke-2, ke-3, atau ke-4)."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedDates.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedDates.forEachIndexed { idx, millis ->
                                Surface(
                                    onClick = {
                                        if (selectedDates.size > 1) {
                                            selectedDates = selectedDates.filterNot { it == millis }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = VisitIndigoLight,
                                    border = BorderStroke(1.dp, VisitIndigo.copy(alpha = 0.35f)),
                                    modifier = Modifier.testTag("chip_visit_date_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.EventAvailable,
                                            contentDescription = null,
                                            tint = VisitIndigo,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = dateFormatter.format(Date(millis)),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = VisitIndigo
                                        )
                                        if (selectedDates.size > 1) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = if (isEn) "Remove date" else "Hapus tanggal",
                                                tint = VisitIndigo,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_add_visit_date")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = VisitIndigo)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Add Date" else "Tambah Tanggal",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VisitIndigo,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                val sundays = computeDefaultVisitSundays(selectedMonthYear)
                                if (sundays.isNotEmpty()) {
                                    selectedDates = (selectedDates + sundays)
                                        .map { VisitHelper.normalizeDayStart(it) }
                                        .distinct()
                                        .sorted()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_fill_sundays_visit")
                        ) {
                            Text(
                                text = if (isEn) "Sundays W2-W4" else "+ Minggu 2, 3, 4",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F766E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 3. Pilihan Kecamatan Dinamis dari Data Siswa Sekolah Ini
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) {
                                "3. Participating Districts (${selectedDistrictKeys.size}):"
                            } else {
                                "3. Pilih Kecamatan Peserta (${selectedDistrictKeys.size}):"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VisitIndigo,
                            modifier = Modifier.weight(1f)
                        )
                        if (dynamicDistricts.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    selectedDistrictKeys = if (selectedDistrictKeys.size == dynamicDistricts.size) {
                                        emptySet()
                                    } else {
                                        dynamicDistricts.map { it.normalizedKey }.toSet()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("btn_toggle_all_visit_districts")
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (selectedDistrictKeys.size == dynamicDistricts.size) {
                                        if (isEn) "Clear" else "Kosongkan"
                                    } else {
                                        if (isEn) "Select All" else "Pilih Semua"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (dynamicDistricts.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                        ) {
                            Text(
                                text = if (isEn) {
                                    "No student data in this school yet. Import/add students with their District (Kecamatan) first so districts appear automatically here."
                                } else {
                                    "Belum ada data siswa di sekolah ini. Tambahkan/impor siswa beserta kolom Kecamatan terlebih dahulu agar daftar kecamatan muncul otomatis."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dynamicDistricts.forEach { group ->
                                val isSelected = selectedDistrictKeys.any {
                                    it.equals(group.normalizedKey, ignoreCase = true)
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDistrictKeys = if (isSelected) {
                                            selectedDistrictKeys.filterNot {
                                                it.equals(group.normalizedKey, ignoreCase = true)
                                            }.toSet()
                                        } else {
                                            selectedDistrictKeys + group.normalizedKey
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = "${group.displayName} (${group.students.size})",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFCCFBF1),
                                        selectedLabelColor = Color(0xFF0F766E),
                                        selectedLeadingIconColor = Color(0xFF0F766E)
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 36.dp)
                                        .testTag("chip_admin_district_${group.normalizedKey}")
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (totalScheduledCount > 0) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (totalScheduledCount > 0) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (totalScheduledCount > 0) {
                                        if (isEn) {
                                            "Estimated participants: $totalScheduledCount students across ${selectedDistrictKeys.size} districts"
                                        } else {
                                            "Estimasi peserta terjadwal: $totalScheduledCount siswa dari ${selectedDistrictKeys.size} kecamatan"
                                        }
                                    } else {
                                        if (isEn) {
                                            "Select 1 or more districts above to assign visit participants."
                                        } else {
                                            "Pilih 1 atau lebih kecamatan di atas untuk menentukan peserta kunjungan."
                                        }
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (totalScheduledCount > 0) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isEn) "Notes (Optional)" else "Keterangan Jadwal (Opsional)") },
                    placeholder = {
                        Text(
                            if (isEn) "e.g., Visiting hours 08:00–14:00 WIB at West Hall" else "Contoh: Jam kunjung 08.00–14.00 WIB di Aula Utama"
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
                    if (title.isNotBlank() && selectedDates.isNotEmpty()) {
                        onConfirm(
                            title.trim(),
                            selectedMonthYear,
                            selectedDates,
                            selectedDistrictKeys.toList(),
                            description.trim()
                        )
                    }
                },
                enabled = title.isNotBlank() && selectedDates.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = VisitIndigo),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_save_visit_session")
            ) {
                Text(
                    text = if (initialSession != null) {
                        if (isEn) "Save Changes" else "Simpan Perubahan"
                    } else {
                        if (isEn) "Create Session" else "Simpan Sesi"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(s.cancel)
            }
        }
    )

    if (showDatePicker) {
        val initialPickerDate = selectedDates.lastOrNull() ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialPickerDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { picked ->
                        val norm = VisitHelper.normalizeDayStart(picked)
                        selectedDates = (selectedDates + norm).distinct().sorted()
                    }
                    showDatePicker = false
                }) {
                    Text(if (isEn) "Add Date" else "Tambahkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(s.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun computeDefaultVisitSundays(monthYear: String): List<Long> {
    val parts = monthYear.trim().split("-")
    val cal = Calendar.getInstance()
    if (parts.size == 2) {
        val y = parts[0].toIntOrNull()
        val m = parts[1].toIntOrNull()
        if (y != null && m != null && m in 1..12) {
            cal.set(Calendar.YEAR, y)
            cal.set(Calendar.MONTH, m - 1)
        }
    }
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    val targetMonth = cal.get(Calendar.MONTH)
    val sundays = mutableListOf<Long>()
    while (cal.get(Calendar.MONTH) == targetMonth) {
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            sundays.add(cal.timeInMillis)
        }
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    // Ambil Minggu ke-2, ke-3, dan ke-4 bila tersedia (indeks 1, 2, 3)
    val week2To4 = sundays.drop(1).take(3)
    return week2To4.ifEmpty { sundays.take(2) }
}
