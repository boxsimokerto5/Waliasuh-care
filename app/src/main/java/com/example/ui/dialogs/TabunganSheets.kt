package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChildStudent
import com.example.data.model.SavingRetentionHelper
import com.example.data.model.SavingTransaction
import com.example.data.model.SchoolProfile
import com.example.ui.components.HubStatTile
import com.example.util.DateUtils
import com.example.util.SavingProofPhotoHelper
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.launch

internal fun LazyListScope.tabunganCategoryItems(
    isEn: Boolean,
    students: List<ChildStudent>,
    savingTransactions: List<SavingTransaction>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenCatatTabungan: (String, ChildStudent?) -> Unit,
    onOpenStudentLedger: (ChildStudent) -> Unit,
    onOpenTransactionDetail: (SavingTransaction) -> Unit
) {
    val totalIn = savingTransactions
        .filter { it.transactionType == SavingTransaction.TYPE_DEPOSIT }
        .sumOf { it.amount }
    val totalOut = savingTransactions
        .filter { it.transactionType == SavingTransaction.TYPE_EXPENSE }
        .sumOf { it.amount }
    val totalSaldo = totalIn - totalOut
    val activeProofCount = savingTransactions.count { SavingRetentionHelper.isProofActive(it) }

    item(key = "tabungan_summary_card") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Total Foster Savings" else "Total Saldo Tabungan Anak",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = SavingRetentionHelper.formatRupiah(totalSaldo),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857)
                        )
                    }
                    Button(
                        onClick = { onOpenCatatTabungan(SavingTransaction.TYPE_EXPENSE, students.firstOrNull()) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .testTag("btn_catat_tabungan_hub")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Record" else "Catat Transaksi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HubStatTile(
                        value = SavingRetentionHelper.formatRupiah(totalIn),
                        label = if (isEn) "Deposits" else "Titip Masuk",
                        bg = Color(0xFFDCFCE7),
                        fg = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = SavingRetentionHelper.formatRupiah(totalOut),
                        label = if (isEn) "Expenses" else "Pengeluaran",
                        bg = Color(0xFFFEE2E2),
                        fg = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    HubStatTile(
                        value = "$activeProofCount",
                        label = if (isEn) "90d Photos" else "Foto (3 Bln)",
                        bg = Color(0xFFE0F2FE),
                        fg = Color(0xFF0369A1),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenCatatTabungan(SavingTransaction.TYPE_DEPOSIT, students.firstOrNull()) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .testTag("btn_quick_deposit_tabungan")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Deposit (+)" else "Titip Masuk (+)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                    }
                    OutlinedButton(
                        onClick = { onOpenCatatTabungan(SavingTransaction.TYPE_EXPENSE, students.firstOrNull()) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .testTag("btn_quick_expense_tabungan")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Expense + Photo" else "Keluar + Foto", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }
            }
        }
    }

    if (savingTransactions.isNotEmpty()) {
        item(key = "tabungan_recent_header") {
            Text(
                text = if (isEn) "Recent Transactions & Photo Proofs" else "Transaksi Terbaru & Bukti Foto",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(savingTransactions.take(6), key = { "tab_tx_${it.id}" }) { tx ->
            SavingTransactionRowCard(
                tx = tx,
                onClick = { onOpenTransactionDetail(tx) }
            )
        }
    }

    item(key = "tabungan_students_header") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isEn) "Student Balances (${students.size})" else "Saldo Per Anak Asuh (${students.size})",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(if (isEn) "Search student name or class..." else "Cari nama anak atau kelas...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_tabungan_student")
            )
        }
    }

    val filteredStudents = if (searchQuery.isBlank()) students else students.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.classGrade.contains(searchQuery, ignoreCase = true) ||
            it.dormitory.contains(searchQuery, ignoreCase = true)
    }

    if (filteredStudents.isEmpty()) {
        item(key = "tabungan_empty_students") {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isEn) "No matching students." else "Belum ada anak asuh yang cocok.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    } else {
        items(filteredStudents.take(40), key = { "tab_stu_${it.id}" }) { st ->
            val bal = SavingRetentionHelper.computeStudentBalance(savingTransactions, st.id, st.nik, st.name)
            val txCount = savingTransactions.count {
                (st.id > 0L && it.studentId == st.id) ||
                    (st.nik.isNotBlank() && it.studentNik == st.nik) ||
                    it.studentName.equals(st.name, ignoreCase = true)
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenStudentLedger(st) }
                    .testTag("card_tabungan_student_${st.id}"),
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
                        Text(st.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "${st.classGrade} • ${st.dormitory} • $txCount transaksi",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = SavingRetentionHelper.formatRupiah(bal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavingTransactionRowCard(
    tx: SavingTransaction,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val isExpense = tx.transactionType == SavingTransaction.TYPE_EXPENSE
    val hasActivePhoto = SavingRetentionHelper.isProofActive(tx)
    val isExpired = SavingRetentionHelper.isProofExpired(tx)
    val photoModel = remember(tx) { SavingProofPhotoHelper.resolvePhotoModel(context, tx) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("card_saving_tx_${tx.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (hasActivePhoto && photoModel != null) {
                AsyncImage(
                    model = photoModel,
                    contentDescription = if (isEn) "Proof thumbnail" else "Thumbnail bukti",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isExpense) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpense) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isExpense) Color(0xFFDC2626) else Color(0xFF15803D)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = tx.studentName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${tx.purpose.ifBlank { "-" }} • ${DateUtils.formatShortDate(tx.transactionDate)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasActivePhoto) {
                    val remDays = SavingRetentionHelper.remainingDays(tx)
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (isEn) "Photo • ${remDays}d left" else "Bukti Foto • Sisa ${remDays}h",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }
                } else if (isExpired) {
                    Text(
                        text = if (isEn) "Photo proof expired (>3 months)" else "Foto bukti > 3 bulan (sudah dibersihkan)",
                        fontSize = 9.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Text(
                text = (if (isExpense) "-" else "+") + SavingRetentionHelper.formatRupiah(tx.amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isExpense) Color(0xFFDC2626) else Color(0xFF15803D)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuktiFotoTabunganDetailSheet(
    tx: SavingTransaction,
    student: ChildStudent?,
    currentBalance: Long,
    schoolProfile: SchoolProfile,
    onDelete: (SavingTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEn = LocalAppStrings.current.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isExpense = tx.transactionType == SavingTransaction.TYPE_EXPENSE
    val hasActivePhoto = SavingRetentionHelper.isProofActive(tx)
    val isExpired = SavingRetentionHelper.isProofExpired(tx)
    val photoModel = remember(tx) { SavingProofPhotoHelper.resolvePhotoModel(context, tx) }
    val schoolTitle = schoolProfile.schoolName.ifBlank { "Wali Asuh Care" }

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
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 24.dp)
                    .testTag("sheet_detail_bukti_tabungan"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isExpense) {
                                if (isEn) "Expense Transaction Detail" else "Detail Pengeluaran & Bukti Foto"
                            } else {
                                if (isEn) "Deposit Transaction Detail" else "Detail Titip Masuk Tabungan"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${tx.studentName} • ${DateUtils.formatFullDate(tx.transactionDate)}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                // Nominal & Keterangan
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isExpense) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, if (isExpense) Color(0xFFFECACA) else Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = (if (isExpense) "-" else "+") + SavingRetentionHelper.formatRupiah(tx.amount),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isExpense) Color(0xFFDC2626) else Color(0xFF15803D)
                        )
                        Text(
                            text = "${if (isEn) "Purpose" else "Keperluan"}: ${tx.purpose.ifBlank { "-" }}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (tx.notes.isNotBlank()) {
                            Text(
                                text = "${if (isEn) "Notes" else "Catatan"}: ${tx.notes}",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                        Text(
                            text = "${if (isEn) "Current student balance" else "Saldo anak saat ini"}: ${SavingRetentionHelper.formatRupiah(currentBalance)} • Dicatat: ${tx.waliName}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }

                // Status Retensi 3 Bulan & Pratinjau Foto
                if (hasActivePhoto && photoModel != null) {
                    val remDays = SavingRetentionHelper.remainingDays(tx)
                    val expDateStr = tx.proofExpiresAt?.let { DateUtils.formatShortDate(it) } ?: "-"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF0369A1), modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isEn) {
                                    "Database retention: $remDays days remaining (expires $expDateStr)"
                                } else {
                                    "Retensi 3 Bulan Aktif: Sisa $remDays hari di database (s/d $expDateStr)"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = photoModel,
                            contentDescription = if (isEn) "Expense proof photo" else "Foto bukti pengeluaran",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 360.dp)
                                .testTag("img_detail_bukti_tabungan"),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else if (isExpired) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isEn) {
                                "Photo proof exceeded 90-day database retention and was automatically purged. Financial record remains intact."
                            } else {
                                "Masa simpan foto bukti 3 bulan (90 hari) telah berakhir dan otomatis dibersihkan dari database agar penyimpanan tetap ringan. Catatan nominal & keperluan tetap tersimpan permanen."
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isEn) "No photo proof attached to this transaction." else "Transaksi ini tidak memiliki lampiran foto bukti.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                HorizontalDivider()

                // Tombol Kirim WA Orang Tua (Auto-Simpan di Galeri WA Orang Tua)
                Button(
                    onClick = {
                        scope.launch {
                            val ok = SavingProofPhotoHelper.shareProofToWhatsApp(
                                context = context,
                                tx = tx,
                                currentBalance = currentBalance,
                                schoolName = schoolTitle,
                                parentContactHint = student?.emergencyContact.orEmpty(),
                                isEn = isEn
                            )
                            if (!ok) {
                                Toast.makeText(
                                    context,
                                    if (isEn) "No sharing app found" else "Aplikasi WhatsApp / berbagi tidak ditemukan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("btn_share_tabungan_wa")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) {
                            "Send Proof & Receipt to Parent WhatsApp"
                        } else {
                            "Kirim Bukti ke WA Orang Tua (Auto-Simpan Galeri)"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val saved = SavingProofPhotoHelper.saveProofToDeviceGallery(
                                    context = context,
                                    tx = tx,
                                    currentBalance = currentBalance,
                                    schoolName = schoolTitle,
                                    isEn = isEn
                                )
                                Toast.makeText(
                                    context,
                                    if (saved) {
                                        if (isEn) "Saved to Pictures/WaliAsuhCare" else "Tersimpan ke Galeri (Pictures/WaliAsuhCare)"
                                    } else {
                                        if (isEn) "Failed saving to gallery" else "Gagal menyimpan ke galeri"
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_save_tabungan_gallery")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEn) "Save to Gallery" else "Simpan Galeri", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            onDelete(tx)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .weight(0.8f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_delete_tabungan_tx")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Delete" else "Hapus", fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSavingLedgerSheet(
    student: ChildStudent,
    savingTransactions: List<SavingTransaction>,
    onCatatForStudent: (String) -> Unit,
    onOpenTransactionDetail: (SavingTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var filterMode by remember { mutableStateOf("ALL") } // ALL, DEPOSIT, EXPENSE, PHOTO

    val studentTxs = remember(savingTransactions, student) {
        savingTransactions.filter {
            (student.id > 0L && it.studentId == student.id) ||
                (student.nik.isNotBlank() && it.studentNik == student.nik) ||
                it.studentName.equals(student.name, ignoreCase = true)
        }
    }
    val totalIn = studentTxs.filter { it.transactionType == SavingTransaction.TYPE_DEPOSIT }.sumOf { it.amount }
    val totalOut = studentTxs.filter { it.transactionType == SavingTransaction.TYPE_EXPENSE }.sumOf { it.amount }
    val balance = totalIn - totalOut

    val displayedTxs = remember(studentTxs, filterMode) {
        when (filterMode) {
            "DEPOSIT" -> studentTxs.filter { it.transactionType == SavingTransaction.TYPE_DEPOSIT }
            "EXPENSE" -> studentTxs.filter { it.transactionType == SavingTransaction.TYPE_EXPENSE }
            "PHOTO" -> studentTxs.filter { SavingRetentionHelper.isProofActive(it) }
            else -> studentTxs
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
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 24.dp)
                    .testTag("sheet_student_tabungan_ledger"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(student.name, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${student.classGrade} • ${student.dormitory}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isEn) "Current Balance" else "Saldo Tabungan Anak", fontSize = 11.5.sp, color = Color(0xFF047857))
                            Text(SavingRetentionHelper.formatRupiah(balance), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("+${SavingRetentionHelper.formatRupiah(totalIn)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text("-${SavingRetentionHelper.formatRupiah(totalOut)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onCatatForStudent(SavingTransaction.TYPE_DEPOSIT) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                    ) {
                        Text(if (isEn) "+ Deposit" else "+ Titip Masuk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onCatatForStudent(SavingTransaction.TYPE_EXPENSE) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                    ) {
                        Text(if (isEn) "- Expense" else "- Pengeluaran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = filterMode == "ALL", onClick = { filterMode = "ALL" }, label = { Text("Semua (${studentTxs.size})", fontSize = 11.sp) })
                    FilterChip(selected = filterMode == "EXPENSE", onClick = { filterMode = "EXPENSE" }, label = { Text("Keluar", fontSize = 11.sp) })
                    FilterChip(selected = filterMode == "DEPOSIT", onClick = { filterMode = "DEPOSIT" }, label = { Text("Masuk", fontSize = 11.sp) })
                    FilterChip(selected = filterMode == "PHOTO", onClick = { filterMode = "PHOTO" }, label = { Text("Ada Foto", fontSize = 11.sp) })
                }

                if (displayedTxs.isEmpty()) {
                    Text(
                        text = if (isEn) "No transactions in this filter yet." else "Belum ada transaksi pada kategori ini.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    displayedTxs.forEach { tx ->
                        SavingTransactionRowCard(tx = tx, onClick = { onOpenTransactionDetail(tx) })
                    }
                }
            }
        }
    }
}
