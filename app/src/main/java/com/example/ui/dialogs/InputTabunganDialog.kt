package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChildStudent
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar

@Composable
fun InputTabunganDialog(
    claimedStudents: List<ChildStudent>,
    allStudents: List<ChildStudent> = emptyList(),
    transactions: List<TabunganTransaction>,
    defaultType: String = TabunganHelper.TYPE_IN,
    initialStudent: ChildStudent? = null,
    txToEdit: TabunganTransaction? = null,
    isAnakSuperAdmin: Boolean = false,
    onOpenPilihAnakAsuh: () -> Unit,
    onSave: (
        student: ChildStudent,
        type: String,
        amount: Long,
        title: String,
        notes: String,
        transactionDate: Long,
        existingTx: TabunganTransaction?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language
    val isEditMode = txToEdit != null

    val resolvedEditStudent = remember(txToEdit?.id, claimedStudents, allStudents, initialStudent) {
        if (txToEdit == null) {
            initialStudent
        } else {
            initialStudent
                ?: claimedStudents.firstOrNull { TabunganHelper.matchesStudent(txToEdit, it) }
                ?: allStudents.firstOrNull { TabunganHelper.matchesStudent(txToEdit, it) }
                ?: ChildStudent(
                    id = txToEdit.studentId,
                    name = txToEdit.studentName,
                    nis = txToEdit.studentNis,
                    gender = "L",
                    age = 12,
                    dormitory = txToEdit.studentDorm,
                    classGrade = txToEdit.studentClass,
                    assignedWaliUsername = txToEdit.ownerWaliUsername,
                    assignedWaliName = txToEdit.ownerWaliName,
                    schoolId = txToEdit.schoolId
                )
        }
    }

    val selectableStudents = remember(claimedStudents, allStudents, isAnakSuperAdmin, resolvedEditStudent) {
        val base = when {
            claimedStudents.isNotEmpty() -> claimedStudents
            isAnakSuperAdmin -> allStudents
            else -> emptyList()
        }
        val combined = if (resolvedEditStudent != null && base.none { it.id == resolvedEditStudent.id }) {
            listOf(resolvedEditStudent) + base
        } else {
            base
        }
        combined.sortedBy { it.name.lowercase() }
    }

    var selectedStudent by remember(resolvedEditStudent?.id, txToEdit?.id) {
        mutableStateOf(
            resolvedEditStudent
                ?: if (selectableStudents.size == 1) selectableStudents.first() else null
        )
    }
    var studentSearch by remember { mutableStateOf("") }
    var txType by remember(defaultType, txToEdit?.id) {
        val rawType = txToEdit?.transactionType ?: defaultType
        mutableStateOf(
            if (TabunganHelper.isIncome(rawType)) TabunganHelper.TYPE_IN else TabunganHelper.TYPE_OUT
        )
    }
    var amountDigits by remember(txToEdit?.id) {
        mutableStateOf(txToEdit?.amount?.takeIf { it > 0L }?.toString() ?: "")
    }
    var titleText by remember(txToEdit?.id) {
        mutableStateOf(txToEdit?.title ?: "")
    }
    var notesText by remember(txToEdit?.id) {
        mutableStateOf(txToEdit?.notes ?: "")
    }
    var transactionDate by remember(txToEdit?.id) {
        mutableLongStateOf(txToEdit?.transactionDate ?: System.currentTimeMillis())
    }

    val parsedAmount = amountDigits.toLongOrNull() ?: 0L
    val currentStudentBalance = remember(selectedStudent, transactions) {
        val target = selectedStudent ?: return@remember 0L
        TabunganHelper.computeSummaryForStudent(transactions, target).balance
    }
    val isIncome = TabunganHelper.isIncome(txType)
    val projectedNetAfter = remember(selectedStudent, transactions, txToEdit, isIncome, parsedAmount) {
        val target = selectedStudent ?: return@remember 0L
        val studentActiveTxs = transactions.filter {
            TabunganHelper.matchesStudent(it, target) &&
                TabunganHelper.isActive(it.status) &&
                it.id != txToEdit?.id
        }
        val baseIn = studentActiveTxs.filter { TabunganHelper.isIncome(it.transactionType) }.sumOf { it.amount }
        val baseOut = studentActiveTxs.filter { !TabunganHelper.isIncome(it.transactionType) }.sumOf { it.amount }
        if (isIncome) (baseIn + parsedAmount) - baseOut else baseIn - (baseOut + parsedAmount)
    }
    val isOverdraft = selectedStudent != null && parsedAmount > 0L && projectedNetAfter < 0L
    val canSave = selectedStudent != null && parsedAmount > 0L && !isOverdraft

    val quickAmounts = listOf(5_000L, 10_000L, 20_000L, 50_000L, 100_000L)
    val quickTitles = if (isIncome) {
        if (isEn) listOf("Parent Allowance", "Weekly Pocket Money", "Gift / Bonus")
        else listOf("Titip Uang Saku", "Kiriman Orang Tua", "Hadiah / Sisa Jajan")
    } else {
        if (isEn) listOf("School Supplies", "Snacks / Canteen", "Laundry / Toiletries", "Cash Withdrawal")
        else listOf("Jajan / Kebutuhan Harian", "Alat Tulis & Buku", "Perlengkapan Mandi", "Ambil Tunai Anak")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .widthIn(max = 540.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isEditMode -> if (isEn) "Edit Savings Transaction" else "Edit Transaksi Tabungan"
                                isEn -> "Record Student Savings"
                                else -> "Catat Transaksi Tabungan"
                            },
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isEn) "Private ledger • Caregiver & AnakSuperAdmin only" else "Catatan Pribadi • Hanya untuk Wali Asuh & AnakSuperAdmin",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_input_tabungan")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close)
                    }
                }

                if (selectableStudents.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_input_tabungan_no_claimed"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = if (isEn) "No Claimed Foster Students" else "Belum Ada Anak Asuh Binaan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = if (isEn) {
                                    "You can only record Tabungan for foster students claimed under Parent Connect → Select Foster Kids."
                                } else {
                                    "Anda hanya dapat mencatat tabungan untuk anak asuh yang sudah diklaim di menu Hubungan Ortu → Pilih Anak Asuh."
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenPilihAnakAsuh()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_input_tabungan_open_pilih")
                            ) {
                                Text(
                                    text = if (isEn) "Select Foster Students Now" else "Pilih Anak Asuh Sekarang",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Toggle Tipe Transaksi: Titip Masuk (+) vs Pengeluaran (-)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val inBg = if (isIncome) Color(0xFF15803D) else MaterialTheme.colorScheme.surfaceVariant
                        val inFg = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        Button(
                            onClick = { txType = TabunganHelper.TYPE_IN },
                            colors = ButtonDefaults.buttonColors(containerColor = inBg, contentColor = inFg),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_tabungan_type_in")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Deposit (+)" else "Titip Masuk (+)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        val outBg = if (!isIncome) Color(0xFFDC2626) else MaterialTheme.colorScheme.surfaceVariant
                        val outFg = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        Button(
                            onClick = { txType = TabunganHelper.TYPE_OUT },
                            colors = ButtonDefaults.buttonColors(containerColor = outBg, contentColor = outFg),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_tabungan_type_out")
                        ) {
                            Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Expense (-)" else "Pengeluaran (-)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // Pemilih Siswa (khusus siswa klaim / superadmin fallback)
                    Text(
                        text = if (isEn) "Foster Student" else "Pilih Anak Asuh Binaan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedStudent != null) {
                        val st = selectedStudent!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = st.name,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F)
                                    )
                                    Text(
                                        text = "${st.classGrade.ifBlank { "-" }} • ${st.dormitory.ifBlank { "-" }}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Current Balance: ${TabunganHelper.formatRupiah(currentStudentBalance)}"
                                        } else {
                                            "Saldo Saat Ini: ${TabunganHelper.formatRupiah(currentStudentBalance)}"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                if (!isEditMode && initialStudent == null && selectableStudents.size > 1) {
                                    OutlinedButton(
                                        onClick = { selectedStudent = null },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text(if (isEn) "Change" else "Ganti", fontSize = 11.5.sp, color = Color(0xFF92400E))
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = studentSearch,
                            onValueChange = { studentSearch = it },
                            placeholder = {
                                Text(if (isEn) "Search student name or class..." else "Cari nama atau kelas anak asuh...", fontSize = 12.5.sp)
                            },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_tabungan_student_search")
                        )
                        val q = studentSearch.trim().lowercase()
                        val filteredPick = selectableStudents.filter {
                            q.isEmpty() || it.name.lowercase().contains(q) || it.classGrade.lowercase().contains(q)
                        }.take(12)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filteredPick.forEach { st ->
                                val stBal = TabunganHelper.computeSummaryForStudent(transactions, st).balance
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedStudent = st }
                                        .testTag("tabungan_pick_student_${st.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(st.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${st.classGrade.ifBlank { "-" }} • ${st.dormitory.ifBlank { "-" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(
                                            text = TabunganHelper.formatRupiah(stBal),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Pemilih Tanggal Transaksi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) "Transaction Date:" else "Tanggal Transaksi:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = transactionDate }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val chosen = Calendar.getInstance().apply {
                                            timeInMillis = transactionDate
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, day)
                                        }
                                        transactionDate = chosen.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_tabungan_pick_date")
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFB45309))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DateUtils.formatShortDate(transactionDate, lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    // Nominal + Tombol Cepat
                    OutlinedTextField(
                        value = amountDigits,
                        onValueChange = { raw ->
                            amountDigits = raw.filter { it.isDigit() }.take(10)
                        },
                        label = { Text(if (isEn) "Amount (Rp)" else "Nominal Uang (Rp)") },
                        placeholder = { Text("20000") },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                        supportingText = {
                            Text(
                                text = when {
                                    isOverdraft -> if (isEn) {
                                        "Balance would become negative (${TabunganHelper.formatRupiah(projectedNetAfter)})!"
                                    } else {
                                        "Saldo tidak mencukupi (proyeksi: ${TabunganHelper.formatRupiah(projectedNetAfter)})!"
                                    }
                                    parsedAmount > 0L -> if (isEditMode) {
                                        val label = if (isEn) "Projected Balance" else "Proyeksi Saldo"
                                        "${TabunganHelper.formatRupiah(parsedAmount)} • $label: ${TabunganHelper.formatRupiah(projectedNetAfter.coerceAtLeast(0L))}"
                                    } else {
                                        TabunganHelper.formatRupiah(parsedAmount)
                                    }
                                    else -> if (isEn) "Enter digits only (e.g. 25000)" else "Ketik angka saja (mis. 25000)"
                                },
                                color = if (isOverdraft) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        isError = isOverdraft,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tabungan_amount")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickAmounts.forEach { preset ->
                            FilterChip(
                                selected = parsedAmount == preset,
                                onClick = { amountDigits = preset.toString() },
                                label = { Text(TabunganHelper.formatRupiah(preset), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFEF3C7),
                                    selectedLabelColor = Color(0xFFB45309)
                                )
                            )
                        }
                    }

                    // Keterangan / Judul Transaksi
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = {
                            Text(if (isEn) "Description / Purpose" else "Keterangan / Keperluan")
                        },
                        placeholder = {
                            Text(
                                if (isIncome) {
                                    if (isEn) "e.g. Weekly pocket money from parents" else "Mis. Titipan uang saku dari orang tua"
                                } else {
                                    if (isEn) "e.g. Bought school books & snacks" else "Mis. Beli buku, jajan, atau perlengkapan"
                                }
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tabungan_title")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTitles.forEach { presetTitle ->
                            FilterChip(
                                selected = titleText.equals(presetTitle, ignoreCase = true),
                                onClick = { titleText = presetTitle },
                                label = { Text(presetTitle, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text(if (isEn) "Additional Notes (Optional)" else "Catatan Tambahan (Opsional)") },
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tabungan_notes")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(if (isEn) "Cancel" else "Batal")
                        }
                        Button(
                            onClick = {
                                val target = selectedStudent ?: return@Button
                                onSave(
                                    target,
                                    txType,
                                    parsedAmount,
                                    titleText.trim(),
                                    notesText.trim(),
                                    transactionDate,
                                    txToEdit
                                )
                            },
                            enabled = canSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncome) Color(0xFF15803D) else Color(0xFFDC2626),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_save_tabungan_tx")
                        ) {
                            Text(
                                text = when {
                                    isEditMode -> if (isEn) "Save Changes" else "Simpan Perubahan"
                                    isIncome -> if (isEn) "Save Deposit (+)" else "Simpan Titipan (+)"
                                    else -> if (isEn) "Save Expense (-)" else "Catat Pengeluaran (-)"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
