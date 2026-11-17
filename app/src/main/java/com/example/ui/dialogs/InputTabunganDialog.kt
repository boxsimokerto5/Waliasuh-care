package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
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

    // Tema warna senada induk ikon Tabungan (Amber-Emas ke Hijau Zamrud)
    val headerGradient = listOf(Color(0xFFB45309), Color(0xFF047857))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
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
                    .widthIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Gradient Senada Ikon Induk Tabungan + Drag Pill di bagian atas
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(headerGradient))
                        .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.45f))
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFFFDE68A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_menu_tabungan),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        isEditMode -> if (isEn) "Edit Savings Transaction" else "Edit Transaksi Tabungan"
                                        isEn -> "Record Student Savings"
                                        else -> "Catat Transaksi Tabungan"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFFEF3C7),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Private ledger • Caregiver & AnakSuperAdmin" else "Pribadi • Wali Asuh & AnakSuperAdmin",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFFEF3C7),
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_input_tabungan")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = s.nav.close,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Isi Form Rapat & Rapi 1 Halaman
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = if (isEn) "No Claimed Foster Students" else "Belum Ada Anak Asuh Binaan",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = if (isEn) {
                                        "You can only record Tabungan for foster students claimed under Parent Connect → Select Foster Kids."
                                    } else {
                                        "Anda hanya dapat mencatat tabungan untuk anak asuh yang sudah diklaim di menu Hubungan Ortu → Pilih Anak Asuh."
                                    },
                                    fontSize = 11.5.sp,
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
                        // Toggle Tipe Transaksi Kompak: Titip Masuk (+) vs Pengeluaran (-)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFFBEB))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val inBg = if (isIncome) Color(0xFF047857) else Color.Transparent
                            val inFg = if (isIncome) Color.White else Color(0xFF78350F)
                            Button(
                                onClick = { txType = TabunganHelper.TYPE_IN },
                                colors = ButtonDefaults.buttonColors(containerColor = inBg, contentColor = inFg),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(9.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 40.dp)
                                    .testTag("btn_tabungan_type_in")
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isEn) "Deposit (+)" else "Titip Masuk (+)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            val outBg = if (!isIncome) Color(0xFFDC2626) else Color.Transparent
                            val outFg = if (!isIncome) Color.White else Color(0xFF78350F)
                            Button(
                                onClick = { txType = TabunganHelper.TYPE_OUT },
                                colors = ButtonDefaults.buttonColors(containerColor = outBg, contentColor = outFg),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(9.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 40.dp)
                                    .testTag("btn_tabungan_type_out")
                            ) {
                                Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isEn) "Expense (-)" else "Pengeluaran (-)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Header Anak Binaan + Pemilih Tanggal Sejajar 1 Baris (Hemat ruang vertikal)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Foster Student" else "Anak Asuh Binaan",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
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
                                    }
                                    .testTag("btn_tabungan_pick_date")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = DateUtils.formatShortDate(transactionDate, lang),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        if (selectedStudent != null) {
                            val st = selectedStudent!!
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                border = BorderStroke(1.dp, Color(0xFFFCD34D))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = st.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF78350F),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${st.classGrade.ifBlank { "-" }} • ${if (isEn) "Balance" else "Saldo"}: ${TabunganHelper.formatRupiah(currentStudentBalance)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF047857),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (!isEditMode && initialStudent == null && selectableStudents.size > 1) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedStudent = null }
                                        ) {
                                            Text(
                                                text = if (isEn) "Change" else "Ganti",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = studentSearch,
                                onValueChange = { studentSearch = it },
                                placeholder = {
                                    Text(if (isEn) "Search student name or class..." else "Cari nama atau kelas anak...", fontSize = 12.sp)
                                },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(17.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
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
                                    .heightIn(max = 114.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                filteredPick.forEach { st ->
                                    val stBal = TabunganHelper.computeSummaryForStudent(transactions, st).balance
                                    Surface(
                                        shape = RoundedCornerShape(9.dp),
                                        color = Color(0xFFFFFBEB).copy(alpha = 0.6f),
                                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedStudent = st }
                                            .testTag("tabungan_pick_student_${st.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(st.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    "${st.classGrade.ifBlank { "-" }} • ${st.dormitory.ifBlank { "-" }}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = TabunganHelper.formatRupiah(stBal),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Nominal Uang + Chip Cepat Kompak
                        OutlinedTextField(
                            value = amountDigits,
                            onValueChange = { raw ->
                                amountDigits = raw.filter { it.isDigit() }.take(10)
                            },
                            label = { Text(if (isEn) "Amount (Rp)" else "Nominal Uang (Rp)", fontSize = 12.sp) },
                            placeholder = { Text("25000", fontSize = 12.sp) },
                            prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = Color(0xFFB45309)) },
                            supportingText = if (isOverdraft || parsedAmount > 0L) {
                                {
                                    Text(
                                        text = when {
                                            isOverdraft -> if (isEn) {
                                                "Balance insufficient (${TabunganHelper.formatRupiah(projectedNetAfter)})!"
                                            } else {
                                                "Saldo tidak mencukupi (${TabunganHelper.formatRupiah(projectedNetAfter)})!"
                                            }
                                            isEditMode -> {
                                                val label = if (isEn) "Projected Balance" else "Proyeksi Saldo"
                                                "${TabunganHelper.formatRupiah(parsedAmount)} • $label: ${TabunganHelper.formatRupiah(projectedNetAfter.coerceAtLeast(0L))}"
                                            }
                                            else -> TabunganHelper.formatRupiah(parsedAmount)
                                        },
                                        fontSize = 10.5.sp,
                                        color = if (isOverdraft) MaterialTheme.colorScheme.error else Color(0xFF047857)
                                    )
                                }
                            } else {
                                null
                            },
                            isError = isOverdraft,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_tabungan_amount")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            quickAmounts.forEach { preset ->
                                val isSel = parsedAmount == preset
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, if (isSel) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { amountDigits = preset.toString() }
                                ) {
                                    Text(
                                        text = TabunganHelper.formatRupiah(preset),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Keterangan + Chip Preset Kompak
                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            label = {
                                Text(if (isEn) "Description / Purpose" else "Keterangan / Keperluan", fontSize = 12.sp)
                            },
                            placeholder = {
                                Text(
                                    text = if (isIncome) {
                                        if (isEn) "e.g. Weekly pocket money" else "Mis. Titipan uang saku orang tua"
                                    } else {
                                        if (isEn) "e.g. School supplies & snacks" else "Mis. Beli buku, jajan, perlengkapan"
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_tabungan_title")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            quickTitles.forEach { presetTitle ->
                                val isSel = titleText.equals(presetTitle, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, if (isSel) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { titleText = presetTitle }
                                ) {
                                    Text(
                                        text = presetTitle,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text(if (isEn) "Additional Notes (Optional)" else "Catatan Tambahan (Opsional)", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_tabungan_notes")
                        )

                        HorizontalDivider(color = Color(0xFFFDE68A).copy(alpha = 0.7f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(0.38f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(if (isEn) "Cancel" else "Batal", fontSize = 12.5.sp)
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
                                    containerColor = if (isIncome) Color(0xFF047857) else Color(0xFFDC2626),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(0.62f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_save_tabungan_tx")
                            ) {
                                Text(
                                    text = when {
                                        isEditMode -> if (isEn) "Save Changes" else "Simpan Perubahan"
                                        isIncome -> if (isEn) "Save Deposit (+)" else "Simpan Titipan (+)"
                                        else -> if (isEn) "Save Expense (-)" else "Catat Pengeluaran (-)"
                                    },
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
