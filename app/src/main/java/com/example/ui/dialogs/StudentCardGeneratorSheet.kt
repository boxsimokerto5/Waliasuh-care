package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.SchoolProfile
import com.example.ui.components.StudentIdCardPreview
import com.example.util.CardJenjang
import com.example.util.StudentCardPdfGenerator
import com.example.util.StudentCardRenderer
import com.example.util.StudentClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

enum class CardPrintScope(val label: String) {
    INDIVIDUAL("Per Orang"),
    ALL("Semua"),
    MALE("Putra"),
    FEMALE("Putri")
}

private enum class CardExportAction {
    PRINT, SHARE, OPEN
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentCardGeneratorSheet(
    students: List<ChildStudent>,
    schoolProfile: SchoolProfile,
    initialStudent: ChildStudent? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val resolvedSchoolName = remember(schoolProfile) {
        schoolProfile.schoolName.trim()
            .ifBlank { schoolProfile.shortName.trim() }
            .ifBlank { "Wali Asuh Care School" }
    }

    var selectedScope by remember(initialStudent) {
        mutableStateOf(if (initialStudent != null) CardPrintScope.INDIVIDUAL else CardPrintScope.INDIVIDUAL)
    }
    var selectedIndividualStudent by remember(initialStudent, students) {
        mutableStateOf(initialStudent ?: students.firstOrNull())
    }
    var searchQuery by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val maleStudents = remember(students) {
        students.filter { StudentClassifier.isMaleStudent(it) }
    }
    val femaleStudents = remember(students) {
        students.filter { StudentClassifier.isFemaleStudent(it) }
    }

    val filteredIndividualList = remember(students, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) students
        else students.filter { s ->
            s.name.lowercase().contains(q) ||
                s.nik.lowercase().contains(q) ||
                s.nis.lowercase().contains(q) ||
                s.classGrade.lowercase().contains(q) ||
                s.address.lowercase().contains(q) ||
                s.dormitory.lowercase().contains(q)
        }
    }

    val targetStudentsToPrint = remember(
        selectedScope,
        selectedIndividualStudent,
        students,
        maleStudents,
        femaleStudents
    ) {
        when (selectedScope) {
            CardPrintScope.INDIVIDUAL -> listOfNotNull(selectedIndividualStudent)
            CardPrintScope.ALL -> students
            CardPrintScope.MALE -> maleStudents
            CardPrintScope.FEMALE -> femaleStudents
        }
    }

    val filterSummaryLabel = remember(selectedScope, selectedIndividualStudent) {
        when (selectedScope) {
            CardPrintScope.INDIVIDUAL -> selectedIndividualStudent?.name?.take(28)?.ifBlank { "Individu" } ?: "Individu"
            CardPrintScope.ALL -> "Semua Anak Asuh"
            CardPrintScope.MALE -> "Semua Putra"
            CardPrintScope.FEMALE -> "Semua Putri"
        }
    }

    fun executeExport(action: CardExportAction) {
        val list = targetStudentsToPrint
        if (list.isEmpty()) {
            Toast.makeText(context, "Tidak ada data anak asuh pada pilihan ini.", Toast.LENGTH_SHORT).show()
            return
        }
        isGenerating = true
        scope.launch {
            val file = withContext(Dispatchers.Default) {
                StudentCardPdfGenerator.generateCardsPdf(
                    context = context,
                    students = list,
                    schoolName = resolvedSchoolName,
                    filterLabel = filterSummaryLabel,
                    schoolAddress = schoolProfile.address.trim().ifBlank { schoolProfile.city.trim() }
                )
            }
            isGenerating = false
            if (file == null) {
                Toast.makeText(context, "Gagal membuat PDF Kartu Anak Asuh.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            when (action) {
                CardExportAction.PRINT -> StudentCardPdfGenerator.printCardsWithSystemPrintManager(
                    context = context,
                    pdfFile = file,
                    jobName = "Kartu Anak Asuh - $filterSummaryLabel"
                )
                CardExportAction.SHARE -> StudentCardPdfGenerator.shareCardsPdf(
                    context = context,
                    pdfFile = file,
                    filterLabel = filterSummaryLabel,
                    schoolName = resolvedSchoolName
                )
                CardExportAction.OPEN -> StudentCardPdfGenerator.openCardsPdf(
                    context = context,
                    pdfFile = file
                )
            }
        }
    }

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
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 26.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("student_card_generator_sheet"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Generate Kartu Anak Asuh",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = resolvedSchoolName,
                                fontSize = 11.5.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_card_generator")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Mode Cetak Chips: Per Orang, Semua, Putra, Putri
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Pilih Mode Cetak Kartu:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CardPrintScope.entries.forEach { scopeOption ->
                            val countLabel = when (scopeOption) {
                                CardPrintScope.INDIVIDUAL -> "Per Orang"
                                CardPrintScope.ALL -> "Semua (${students.size})"
                                CardPrintScope.MALE -> "Putra (${maleStudents.size})"
                                CardPrintScope.FEMALE -> "Putri (${femaleStudents.size})"
                            }
                            val isSelected = selectedScope == scopeOption
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedScope = scopeOption },
                                label = {
                                    Text(
                                        text = countLabel,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("chip_card_scope_${scopeOption.name.lowercase()}")
                            )
                        }
                    }
                }

                // Legenda Desain Warna Jenjang & Garis Pemanis Gender
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ColorLegendPill("Header SMA (10-12): Abu-abu", Color(0xFF475569))
                            ColorLegendPill("Header SMP (7-9): Biru", Color(0xFF1D4ED8))
                            ColorLegendPill("Header SD (1-5): Merah", Color(0xFFDC2626))
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ColorLegendPill("Aksen Putra: Hijau Emerald", Color(0xFF10B981))
                            ColorLegendPill("Aksen Putri: Pink Rose", Color(0xFFEC4899))
                        }
                    }
                }

                // Pratinjau Kartu Langsung
                if (selectedScope == CardPrintScope.INDIVIDUAL) {
                    val activeStudent = selectedIndividualStudent
                    if (activeStudent != null) {
                        Text(
                            text = "Pratinjau Kartu Anak Asuh (QR NIK):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StudentIdCardPreview(
                            student = activeStudent,
                            schoolName = resolvedSchoolName,
                            modifier = Modifier.testTag("card_preview_individual")
                        )
                    } else {
                        EmptyCardTargetBox("Belum ada data siswa/siswi yang dapat dicetak.")
                    }

                    // Pemilih Siswa Individu (jika daftar > 1)
                    if (students.size > 1) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari nama, NIK, NISN, kelas...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_card_student_search")
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp, max = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredIndividualList, key = { it.id }) { item ->
                                val isChosen = selectedIndividualStudent?.id == item.id
                                val palette = StudentCardRenderer.resolvePalette(item)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedIndividualStudent = item }
                                        .border(
                                            width = if (isChosen) 1.5.dp else 0.5.dp,
                                            color = if (isChosen) Color(palette.trimNeonArgb) else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isChosen) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (palette.jenjang) {
                                                        CardJenjang.SMA -> Color(0xFF475569)
                                                        CardJenjang.SMP -> Color(0xFF1D4ED8)
                                                        CardJenjang.SD -> Color(0xFFDC2626)
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${palette.jenjang.label} (${item.classGrade}) • NIK: ${item.nik.ifBlank { "-" }}",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = palette.genderLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (palette.isMale) Color(0xFF15803D) else Color(0xFFDB2777)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val count = targetStudentsToPrint.size
                    val estimatedPages = ceil(count.toDouble() / 8.0).toInt().coerceAtLeast(1)
                    if (count == 0) {
                        EmptyCardTargetBox("Tidak ada anak asuh untuk kategori $filterSummaryLabel.")
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "$count Kartu Anak Asuh Siap Cetak",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tersusun rapi 8 kartu/halaman A4 ($estimatedPages halaman) lengkap dengan garis potong.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Sampel Pratinjau (${minOf(count, 10)} dari $count kartu):",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(targetStudentsToPrint.take(10), key = { it.id }) { previewStudent ->
                                StudentIdCardPreview(
                                    student = previewStudent,
                                    schoolName = resolvedSchoolName,
                                    modifier = Modifier.width(305.dp)
                                )
                            }
                        }
                    }
                }

                // Action Buttons (Cetak, Bagikan, Buka PDF)
                if (isGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Menyusun ${targetStudentsToPrint.size} kartu QR ke PDF A4...",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { executeExport(CardExportAction.PRINT) },
                            enabled = targetStudentsToPrint.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_print_student_cards")
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cetak / Simpan PDF (${targetStudentsToPrint.size} Kartu)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { executeExport(CardExportAction.OPEN) },
                                enabled = targetStudentsToPrint.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_open_student_cards_pdf")
                            ) {
                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka PDF", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { executeExport(CardExportAction.SHARE) },
                                enabled = targetStudentsToPrint.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_share_student_cards_pdf")
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan PDF", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorLegendPill(label: String, dotColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyCardTargetBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
