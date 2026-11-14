package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.util.DateUtils
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputMengajiDialog(
    students: List<ChildStudent>,
    mengajiRecords: List<MengajiRecord>,
    initialStudent: ChildStudent? = null,
    recordToEdit: MengajiRecord? = null,
    onSave: (
        student: ChildStudent,
        sessionDate: Long,
        bookType: String,
        jilidOrJuz: String,
        pageOrVerse: String,
        fluencyStatus: String,
        notes: String,
        existingRecordId: Long?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    val lang = appStrings.language

    val emeraldPrimary = Color(0xFF047857)
    val emeraldLight = Color(0xFFD1FAE5)

    val resolvedInitialStudent = remember(initialStudent, recordToEdit, students) {
        when {
            recordToEdit != null -> students.firstOrNull { MengajiHelper.matchesStudent(recordToEdit, it) }
                ?: ChildStudent(
                    id = recordToEdit.studentId,
                    name = recordToEdit.studentName,
                    nis = recordToEdit.studentNis,
                    gender = "L",
                    age = 12,
                    dormitory = recordToEdit.studentDorm,
                    classGrade = recordToEdit.studentClass,
                    nik = recordToEdit.studentNik,
                    jenjang = recordToEdit.studentJenjang,
                    schoolId = recordToEdit.schoolId
                )
            else -> initialStudent
        }
    }

    var selectedStudent by remember(resolvedInitialStudent) { mutableStateOf(resolvedInitialStudent) }
    var searchQuery by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var scannerFloatingFeedback by remember { mutableStateOf<ScannerFloatingFeedback?>(null) }

    val initialSuggestion = remember(resolvedInitialStudent, mengajiRecords, recordToEdit) {
        if (resolvedInitialStudent != null && recordToEdit == null) {
            val latest = MengajiHelper.latestRecordForStudent(mengajiRecords, resolvedInitialStudent)
            MengajiHelper.suggestNextEntry(latest)
        } else {
            null
        }
    }

    var sessionDate by remember(recordToEdit) {
        mutableLongStateOf(recordToEdit?.sessionDate ?: System.currentTimeMillis())
    }
    var bookType by remember(recordToEdit, initialSuggestion) {
        mutableStateOf(recordToEdit?.bookType ?: initialSuggestion?.bookType ?: "Iqro'")
    }
    var jilidOrJuz by remember(recordToEdit, initialSuggestion) {
        mutableStateOf(recordToEdit?.jilidOrJuz ?: initialSuggestion?.jilidOrJuz ?: "Jilid 1")
    }
    var pageOrVerse by remember(recordToEdit, initialSuggestion) {
        mutableStateOf(recordToEdit?.pageOrVerse ?: initialSuggestion?.pageOrVerse ?: "Hal. 1")
    }
    var fluencyStatus by remember(recordToEdit) {
        mutableStateOf(recordToEdit?.fluencyStatus ?: MengajiHelper.FLUENCY_LANCAR)
    }
    var notes by remember(recordToEdit) {
        mutableStateOf(recordToEdit?.notes ?: "")
    }
    var activeSuggestion by remember(initialSuggestion) { mutableStateOf(initialSuggestion) }

    fun applyStudentSelection(student: ChildStudent) {
        selectedStudent = student
        val latest = MengajiHelper.latestRecordForStudent(mengajiRecords, student)
        val suggestion = MengajiHelper.suggestNextEntry(latest)
        activeSuggestion = suggestion
        bookType = suggestion.bookType
        jilidOrJuz = suggestion.jilidOrJuz
        pageOrVerse = suggestion.pageOrVerse
        fluencyStatus = MengajiHelper.FLUENCY_LANCAR
    }

    fun stepPage(delta: Int) {
        val currentNum = MengajiHelper.extractEndingPageNumber(pageOrVerse) ?: 1
        val nextNum = (currentNum + delta).coerceAtLeast(1)
        pageOrVerse = MengajiHelper.formatPageLabel(nextNum)
    }

    val matchedStudents = remember(students, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            students.take(12)
        } else {
            students.filter { s ->
                s.name.lowercase().contains(q) ||
                    s.classGrade.lowercase().contains(q) ||
                    s.dormitory.lowercase().contains(q) ||
                    StudentCsvParser.cleanDigitsOnly(s.nik).contains(q) ||
                    s.nis.lowercase().contains(q)
            }.take(30)
        }
    }

    val jilidOptions = remember(bookType) { MengajiHelper.jilidOrJuzOptionsForBook(bookType) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header ringkas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(emeraldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = emeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    recordToEdit != null -> if (isEn) "Edit Mengaji Record" else "Edit Setoran Mengaji"
                                    else -> if (isEn) "Input Mengaji Progress" else "Input Mengaji Berkelanjutan"
                                },
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) {
                                    "Auto-continues from the student's previous reading"
                                } else {
                                    "Otomatis melanjutkan halaman terakhir bacaan anak"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_input_mengaji")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = appStrings.nav.close,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .testTag("list_input_mengaji_form"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentStudent = selectedStudent
                    if (currentStudent == null) {
                        item(key = "mengaji_scan_btn") {
                            Button(
                                onClick = { showBarcodeScanner = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_scan_barcode_mengaji")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = if (isEn) "Scan Student Card" else "Scan Kartu Siswa",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) "Scan Student Card Barcode" else "Scan Barcode Kartu Siswa",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        item(key = "mengaji_search_student") {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        if (isEn) "Search student name, class, or NIK/NIS..." else "Cari nama siswa, kelas, atau NIK/NIS...",
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = if (isEn) "Search Student" else "Cari Siswa",
                                        tint = emeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = if (isEn) "Clear" else "Bersihkan")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_search_student_mengaji")
                            )
                        }

                        item(key = "mengaji_pick_hdr") {
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    if (isEn) "Select Student (${matchedStudents.size}):" else "Pilih Siswa (${students.size} Terdaftar):"
                                } else {
                                    if (isEn) "Search Results (${matchedStudents.size}):" else "Hasil Pencarian (${matchedStudents.size} Siswa):"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(matchedStudents, key = { "mengaji_pick_${it.id}" }) { st ->
                            val latestForSt = remember(mengajiRecords, st) {
                                MengajiHelper.latestRecordForStudent(mengajiRecords, st)
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { applyStudentSelection(st) }
                                    .testTag("card_pick_mengaji_${st.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = st.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${FastingJenjangHelper.resolveJenjang(st)} • ${st.classGrade.ifBlank { "-" }} • ${st.dormitory}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (latestForSt != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = emeraldLight
                                        ) {
                                            Text(
                                                text = "${latestForSt.jilidOrJuz} • ${latestForSt.pageOrVerse}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = emeraldPrimary,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Kartu Siswa Terpilih + Banner Kelanjutan Bacaan Terakhir
                        item(key = "mengaji_selected_student") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = currentStudent.name,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF064E3B)
                                            )
                                            Text(
                                                text = "${FastingJenjangHelper.resolveJenjang(currentStudent)} • ${currentStudent.classGrade.ifBlank { "-" }} • ${currentStudent.dormitory}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                        if (recordToEdit == null) {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedStudent = null
                                                    activeSuggestion = null
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 36.dp)
                                                    .testTag("btn_change_student_mengaji")
                                            ) {
                                                Text(if (isEn) "Change" else "Ganti", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = emeraldPrimary)
                                            }
                                        }
                                    }

                                    // Banner Lanjutan Otomatis (jika bukan mode edit)
                                    val sug = activeSuggestion
                                    if (recordToEdit == null && sug != null) {
                                        val prev = sug.previousRecord
                                        val bannerBg = when {
                                            prev == null -> Color(0xFFE0F2FE)
                                            sug.isRepeatingLast -> Color(0xFFFEF3C7)
                                            else -> Color(0xFFCCFBF1)
                                        }
                                        val bannerFg = when {
                                            prev == null -> Color(0xFF0369A1)
                                            sug.isRepeatingLast -> Color(0xFF92400E)
                                            else -> Color(0xFF065F46)
                                        }
                                        val bannerText = when {
                                            prev == null -> if (isEn) {
                                                "First Mengaji record for this student — starting from Iqro' Jilid 1, Hal. 1."
                                            } else {
                                                "Setoran mengaji pertama — otomatis dimulai dari Iqro' Jilid 1 • Hal. 1."
                                            }
                                            sug.isRepeatingLast -> if (isEn) {
                                                "Previous (${DateUtils.formatShortDate(prev.sessionDate, lang)}): ${MengajiHelper.formatCompactPosition(prev)} (Needs Practice) → Suggested to repeat ${sug.pageOrVerse}."
                                            } else {
                                                "Terakhir (${DateUtils.formatShortDate(prev.sessionDate, lang)}): ${MengajiHelper.formatCompactPosition(prev)} (Tidak Lancar) → Disarankan mengulang ${sug.pageOrVerse}."
                                            }
                                            else -> if (isEn) {
                                                "Previous (${DateUtils.formatShortDate(prev.sessionDate, lang)}): ${MengajiHelper.formatCompactPosition(prev)} (Fluent) → Auto-continued to ${sug.pageOrVerse}."
                                            } else {
                                                "Terakhir (${DateUtils.formatShortDate(prev.sessionDate, lang)}): ${MengajiHelper.formatCompactPosition(prev)} (Lancar) → Otomatis lanjut ke ${sug.pageOrVerse}."
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bannerBg)
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                .testTag("banner_mengaji_suggestion")
                                        ) {
                                            Text(
                                                text = bannerText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = bannerFg,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Tanggal Setoran
                        item(key = "mengaji_date_row") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isEn) "Session Date:" else "Tanggal Mengaji:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = sessionDate }
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, day ->
                                                val chosen = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, day)
                                                }
                                                sessionDate = chosen.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.heightIn(min = 42.dp)
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(15.dp), tint = emeraldPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = DateUtils.formatShortDate(sessionDate, lang),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = emeraldPrimary
                                    )
                                }
                            }
                        }

                        // Pilih Kitab / Buku (Iqro', Qiroati, Tilawati, Al-Qur'an, dll)
                        item(key = "mengaji_book_type") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isEn) "Book / Method:" else "Kitab / Metode:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MengajiHelper.BOOK_TYPES.forEach { book ->
                                        val selected = bookType.equals(book, ignoreCase = true)
                                        FilterChip(
                                            selected = selected,
                                            onClick = {
                                                bookType = book
                                                val newOpts = MengajiHelper.jilidOrJuzOptionsForBook(book)
                                                if (jilidOrJuz !in newOpts) {
                                                    jilidOrJuz = newOpts.firstOrNull() ?: "Jilid 1"
                                                }
                                            },
                                            label = {
                                                Text(book, fontSize = 11.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = emeraldLight,
                                                selectedLabelColor = emeraldPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Pilih Jilid / Juz + Tombol "Naik Jilid"
                        item(key = "mengaji_jilid_row") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (bookType.contains("Qur", true) || bookType.contains("Juz", true)) {
                                            "Juz:"
                                        } else {
                                            if (isEn) "Volume (Jilid):" else "Jilid:"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            val (nextBook, nextJilid) = MengajiHelper.advanceJilidOrJuz(bookType, jilidOrJuz)
                                            bookType = nextBook
                                            jilidOrJuz = nextJilid
                                            pageOrVerse = "Hal. 1"
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("btn_advance_jilid_mengaji")
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(13.dp), tint = emeraldPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isEn) "Next Volume (Page 1)" else "Naik Jilid (Hal. 1)",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = emeraldPrimary
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    jilidOptions.forEach { opt ->
                                        val selected = jilidOrJuz.equals(opt, ignoreCase = true)
                                        FilterChip(
                                            selected = selected,
                                            onClick = { jilidOrJuz = opt },
                                            label = {
                                                Text(opt, fontSize = 11.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = emeraldLight,
                                                selectedLabelColor = emeraldPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Input Halaman / Ayat dengan Stepper -1 / +1
                        item(key = "mengaji_page_row") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isEn) "Page / Verse Read:" else "Halaman / Ayat Dibaca:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { stepPage(-1) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("btn_page_dec_mengaji")
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = if (isEn) "Previous Page" else "Halaman Sebelumnya")
                                    }

                                    OutlinedTextField(
                                        value = pageOrVerse,
                                        onValueChange = { pageOrVerse = it },
                                        placeholder = {
                                            Text(if (isEn) "e.g. Hal. 14 or Hal. 14-15" else "Misal: Hal. 14 atau Hal. 14-15", fontSize = 12.sp)
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_page_mengaji")
                                    )

                                    OutlinedButton(
                                        onClick = { stepPage(1) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("btn_page_inc_mengaji")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = if (isEn) "Next Page" else "Halaman Berikutnya")
                                    }
                                }
                            }
                        }

                        // Status Kelancaran: Lancar vs Tidak Lancar
                        item(key = "mengaji_fluency_row") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isEn) "Fluency Assessment:" else "Keterangan Kelancaran:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isLancar = MengajiHelper.isFluent(fluencyStatus)
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 52.dp)
                                            .clickable { fluencyStatus = MengajiHelper.FLUENCY_LANCAR }
                                            .testTag("btn_fluency_lancar"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isLancar) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            width = if (isLancar) 2.dp else 1.dp,
                                            color = if (isLancar) Color(0xFF16A34A) else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (isLancar) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isEn) "Fluent (Lancar)" else "Lancar",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLancar) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 52.dp)
                                            .clickable { fluencyStatus = MengajiHelper.FLUENCY_TIDAK_LANCAR }
                                            .testTag("btn_fluency_tidak_lancar"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (!isLancar) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            width = if (!isLancar) 2.dp else 1.dp,
                                            color = if (!isLancar) Color(0xFFDC2626) else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Replay,
                                                contentDescription = null,
                                                tint = if (!isLancar) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isEn) "Needs Practice" else "Tidak Lancar",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (!isLancar) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Catatan Tambahan Opsional
                        item(key = "mengaji_notes_row") {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text(if (isEn) "Guardian Notes (Optional)" else "Catatan Wali Asuh (Opsional)", fontSize = 12.sp) },
                                placeholder = {
                                    Text(if (isEn) "e.g. Watch makhraj / tajwid length..." else "Misal: Perbaiki makhraj / panjang pendek...", fontSize = 11.5.sp)
                                },
                                maxLines = 2,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_notes_mengaji")
                            )
                        }
                    }
                }

                // Footer Tombol Simpan
                val activeStudent = selectedStudent
                if (activeStudent != null) {
                    Button(
                        onClick = {
                            onSave(
                                activeStudent,
                                sessionDate,
                                bookType,
                                jilidOrJuz,
                                pageOrVerse.ifBlank { "Hal. 1" },
                                fluencyStatus,
                                notes,
                                recordToEdit?.id
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_save_mengaji")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (recordToEdit != null) {
                                if (isEn) "Update Mengaji Record" else "Perbarui Catatan Mengaji"
                            } else {
                                if (isEn) "Save Mengaji Record" else "Simpan Setoran Mengaji"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            femaleStudents = students,
            onScanned = { scannedCode ->
                val matched = StudentClassifier.findMatchingStudent(students, scannedCode)
                if (matched == null) {
                    scannerFloatingFeedback = ScannerFloatingFeedback(
                        id = System.nanoTime(),
                        status = ScannerFeedbackStatus.ERROR,
                        title = if (isEn) "Card not found" else "Kartu tidak ditemukan",
                        subtitle = if (isEn) "Code \"$scannedCode\" not registered" else "Kode \"$scannedCode\" belum terdaftar"
                    )
                } else {
                    applyStudentSelection(matched)
                    showBarcodeScanner = false
                    scannerFloatingFeedback = null
                }
            },
            onDismiss = {
                showBarcodeScanner = false
                scannerFloatingFeedback = null
            },
            dialogTitle = if (isEn) "Scan Student Mengaji Card" else "Scan Barcode Kartu Mengaji",
            dialogSubtitle = if (isEn) {
                "Auto-loads the student's last reading progress"
            } else {
                "Otomatis menampilkan lanjutan bacaan terakhir siswa"
            },
            quickPickTitle = if (isEn) {
                "Quick Pick Student (${students.size}):"
            } else {
                "Pilih Cepat Siswa (${students.size} Siswa):"
            },
            searchPlaceholder = if (isEn) "Search name, class, NIK..." else "Cari nama, kelas, atau NIK...",
            emptyListMessage = if (isEn) "No students registered yet." else "Belum ada data siswa terdaftar.",
            accentColor = emeraldPrimary,
            accentBgColor = emeraldLight,
            cardBgColor = Color(0xFFF0FDF4),
            avatarBgColor = Color(0xFFA7F3D0),
            avatarTextColor = emeraldPrimary,
            titleTextColor = Color(0xFF064E3B),
            continuousScan = false,
            floatingFeedback = scannerFloatingFeedback
        )
    }
}
