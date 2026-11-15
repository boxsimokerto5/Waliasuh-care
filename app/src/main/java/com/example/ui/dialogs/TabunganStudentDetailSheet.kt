package com.example.ui.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.util.DateUtils
import com.example.util.PasswordGenerator
import com.example.util.PdfReportGenerator
import com.example.util.i18n.LocalAppStrings
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabunganStudentDetailSheet(
    student: ChildStudent,
    studentTransactions: List<TabunganTransaction>,
    studentLogs: List<TabunganLog>,
    waliName: String,
    schoolProfile: SchoolProfile,
    onQuickInput: (String) -> Unit,
    onCancelTransaction: (TabunganTransaction, String) -> Unit,
    onDeleteTransaction: (TabunganTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish

    val summary = remember(studentTransactions, student) {
        TabunganHelper.computeSummaryForStudent(studentTransactions, student)
    }

    // 0 = Semua Mutasi, 1 = Masuk (+), 2 = Keluar (-), 3 = Log Audit
    var selectedFilter by remember { mutableIntStateOf(0) }
    var txToCancel by remember { mutableStateOf<TabunganTransaction?>(null) }
    var cancelReason by remember { mutableStateOf("") }
    var txToDelete by remember { mutableStateOf<TabunganTransaction?>(null) }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }

    val sortedTransactions = remember(studentTransactions, selectedFilter) {
        studentTransactions
            .filter { tx ->
                when (selectedFilter) {
                    1 -> TabunganHelper.isIncome(tx.transactionType)
                    2 -> !TabunganHelper.isIncome(tx.transactionType)
                    else -> true
                }
            }
            .sortedWith(compareByDescending<TabunganTransaction> { it.transactionDate }.thenByDescending { it.createdAt })
    }
    val sortedLogs = remember(studentLogs) {
        studentLogs.sortedByDescending { it.createdAt }
    }

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
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Siswa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(12.dp))
                            Text(
                                text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }} • ${if (isEn) "Private Savings" else "Tabungan Pribadi"}",
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
                            .testTag("btn_close_tabungan_detail")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close)
                    }
                }

                // Kartu Ringkasan Saldo Per Anak (Masuk, Keluar, Saldo Akhir)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_tabungan_detail_summary"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isEn) "Current Child Balance" else "Saldo Tabungan Saat Ini",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = TabunganHelper.formatRupiah(summary.balance),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Text(
                                text = if (isEn) {
                                    "${summary.activeCount} Active • ${summary.cancelledCount} Void"
                                } else {
                                    "${summary.activeCount} Aktif • ${summary.cancelledCount} Batal"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(if (isEn) "Income (+)" else "Pemasukan (+)", fontSize = 10.sp, color = Color(0xFF166534))
                                    Text(
                                        text = TabunganHelper.formatRupiah(summary.totalIn),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(if (isEn) "Expense (-)" else "Pengeluaran (-)", fontSize = 10.sp, color = Color(0xFF991B1B))
                                    Text(
                                        text = TabunganHelper.formatRupiah(summary.totalOut),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }

                // Baris Aksi Cepat: + Titip | - Keluar | Cetak PDF | Share WA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onQuickInput(TabunganHelper.TYPE_IN) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .testTag("btn_tabungan_detail_add_in")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isEn) "Deposit" else "Titip +", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = { onQuickInput(TabunganHelper.TYPE_OUT) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .testTag("btn_tabungan_detail_add_out")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isEn) "Spend" else "Keluar -", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            val pdf = PdfReportGenerator.generateTabunganStudentPdf(
                                context = context,
                                student = student,
                                transactions = studentTransactions,
                                waliName = waliName,
                                schoolProfile = schoolProfile
                            )
                            if (pdf != null) {
                                readyPdfFile = pdf
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Failed to generate student PDF." else "Gagal membuat PDF tabungan.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("btn_tabungan_student_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFF0F766E), modifier = Modifier.size(16.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            val waText = PasswordGenerator.formatTabunganStudentWaSummary(
                                student = student,
                                transactions = studentTransactions,
                                waliName = waliName,
                                schoolProfile = schoolProfile,
                                isEnglish = isEn
                            )
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, waText)
                            }
                            runCatching {
                                context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share Savings Recap" else "Bagikan Ringkasan Tabungan"))
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("btn_tabungan_student_wa")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share WA", tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                    }
                }

                // Filter Chips: Semua | Masuk (+) | Keluar (-) | Log Audit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        label = { Text(if (isEn) "All (${studentTransactions.size})" else "Semua (${studentTransactions.size})", fontSize = 11.sp) },
                        modifier = Modifier.testTag("chip_tabungan_detail_all")
                    )
                    FilterChip(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        label = { Text(if (isEn) "In (+)" else "Masuk (+)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDCFCE7),
                            selectedLabelColor = Color(0xFF15803D)
                        ),
                        modifier = Modifier.testTag("chip_tabungan_detail_in")
                    )
                    FilterChip(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        label = { Text(if (isEn) "Out (-)" else "Keluar (-)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = Color(0xFFDC2626)
                        ),
                        modifier = Modifier.testTag("chip_tabungan_detail_out")
                    )
                    FilterChip(
                        selected = selectedFilter == 3,
                        onClick = { selectedFilter = 3 },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text(if (isEn) "Audit Log (${sortedLogs.size})" else "Log Audit (${sortedLogs.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE0E7FF),
                            selectedLabelColor = Color(0xFF4338CA)
                        ),
                        modifier = Modifier.testTag("chip_tabungan_detail_logs")
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .testTag("list_tabungan_detail_items"),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedFilter == 3) {
                        if (sortedLogs.isEmpty()) {
                            item {
                                Text(
                                    text = if (isEn) "No audit logs for this student yet." else "Belum ada log aktivitas tabungan untuk siswa ini.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            items(sortedLogs, key = { "detail_log_${it.id}" }) { log ->
                                TabunganLogCard(log = log, isEn = isEn)
                            }
                        }
                    } else {
                        if (sortedTransactions.isEmpty()) {
                            item {
                                Text(
                                    text = if (isEn) "No transactions in this filter yet." else "Belum ada mutasi transaksi pada kategori ini.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            items(sortedTransactions, key = { "detail_tx_${it.id}" }) { tx ->
                                val isActive = TabunganHelper.isActive(tx.status)
                                val isIn = TabunganHelper.isIncome(tx.transactionType)
                                val sign = if (isIn) "+" else "-"
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_tabungan_tx_${tx.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            !isActive -> MaterialTheme.colorScheme.outlineVariant
                                            isIn -> Color(0xFFBBF7D0)
                                            else -> Color(0xFFFECACA)
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tx.title.ifBlank { if (isIn) "Titip Masuk" else "Pengeluaran" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = if (isActive) TextDecoration.None else TextDecoration.LineThrough,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "$sign${TabunganHelper.formatRupiah(tx.amount)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textDecoration = if (isActive) TextDecoration.None else TextDecoration.LineThrough,
                                                color = when {
                                                    !isActive -> MaterialTheme.colorScheme.outline
                                                    isIn -> Color(0xFF15803D)
                                                    else -> Color(0xFFDC2626)
                                                }
                                            )
                                        }
                                        if (tx.notes.isNotBlank()) {
                                            Text(tx.notes, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (!isActive) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFE2E8F0)
                                            ) {
                                                Text(
                                                    text = if (isEn) {
                                                        "CANCELLED: ${tx.cancelledReason.ifBlank { "-" }}"
                                                    } else {
                                                        "DIBATALKAN: ${tx.cancelledReason.ifBlank { "-" }}"
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF475569),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${DateUtils.formatDateTime(tx.transactionDate)} • ${tx.ownerWaliName}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (isActive) {
                                                    TextButton(
                                                        onClick = {
                                                            cancelReason = ""
                                                            txToCancel = tx
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .heightIn(min = 36.dp)
                                                            .testTag("btn_cancel_tabungan_tx_${tx.id}")
                                                    ) {
                                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(if (isEn) "Cancel" else "Batalkan", fontSize = 11.sp, color = Color(0xFFB45309))
                                                    }
                                                }
                                                TextButton(
                                                    onClick = { txToDelete = tx },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier
                                                        .heightIn(min = 36.dp)
                                                        .testTag("btn_delete_tabungan_tx_${tx.id}")
                                                ) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(if (isEn) "Delete" else "Hapus", fontSize = 11.sp, color = Color(0xFFDC2626))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Batalkan Transaksi (Point 1)
    txToCancel?.let { targetTx ->
        AlertDialog(
            onDismissRequest = { txToCancel = null },
            modifier = Modifier.testTag("dialog_confirm_cancel_tabungan_tx"),
            title = { Text(if (isEn) "Cancel Transaction?" else "Batalkan Transaksi?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEn) {
                            "Cancel ${targetTx.title.ifBlank { targetTx.transactionType }} (${TabunganHelper.formatRupiah(targetTx.amount)})? Balance will automatically adjust and remain in audit log."
                        } else {
                            "Batalkan transaksi \"${targetTx.title.ifBlank { targetTx.transactionType }}\" senilai ${TabunganHelper.formatRupiah(targetTx.amount)}? Saldo otomatis menyesuaikan dan tetap tercatat di log tabungan."
                        },
                        fontSize = 12.5.sp
                    )
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text(if (isEn) "Cancellation Reason (Optional)" else "Alasan Pembatalan (Opsional)") },
                        placeholder = { Text(if (isEn) "e.g. Wrong amount entered" else "Mis. Salah ketik nominal") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_cancel_tabungan_reason")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelTransaction(targetTx, cancelReason)
                        txToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                    modifier = Modifier.testTag("btn_confirm_cancel_tabungan_tx")
                ) {
                    Text(if (isEn) "Yes, Cancel" else "Ya, Batalkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToCancel = null }) {
                    Text(if (isEn) "Back" else "Kembali")
                }
            }
        )
    }

    // Dialog Konfirmasi Hapus Permanen (Point 1 — Tidak kirim notifikasi lonceng sekolah)
    txToDelete?.let { targetTx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            modifier = Modifier.testTag("dialog_confirm_delete_tabungan_tx"),
            title = { Text(if (isEn) "Delete Transaction Permanently?" else "Hapus Transaksi Permanen?") },
            text = {
                Text(
                    text = if (isEn) {
                        "Remove \"${targetTx.title.ifBlank { targetTx.transactionType }}\" (${TabunganHelper.formatRupiah(targetTx.amount)}) permanently? This action stays in your private Tabungan audit log only."
                    } else {
                        "Hapus transaksi \"${targetTx.title.ifBlank { targetTx.transactionType }}\" (${TabunganHelper.formatRupiah(targetTx.amount)}) secara permanen? Jejak penghapusan hanya tercatat di log pribadi Tabungan."
                    },
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(targetTx)
                        txToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("btn_confirm_delete_tabungan_tx")
                ) {
                    Text(if (isEn) "Delete Permanently" else "Hapus Permanen")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    readyPdfFile?.let { file ->
        AlertDialog(
            onDismissRequest = { readyPdfFile = null },
            title = { Text(if (isEn) "Student Savings PDF Ready" else "PDF Tabungan Siap") },
            text = {
                Text(
                    if (isEn) "Choose to open the private PDF report for ${student.name} or share it."
                    else "Pilih untuk membuka atau membagikan laporan PDF tabungan ${student.name}."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyPdfFile = null
                        PdfReportGenerator.openPdfFile(context, file)
                    }
                ) {
                    Text(if (isEn) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        readyPdfFile = null
                        PdfReportGenerator.shareFastingPdf(
                            context = context,
                            pdfFile = file,
                            subject = if (isEn) "Savings Ledger - ${student.name}" else "Laporan Tabungan - ${student.name}",
                            text = if (isEn) "Attached is the savings report for ${student.name}." else "Terlampir laporan tabungan ananda ${student.name}."
                        )
                    }
                ) {
                    Text(if (isEn) "Share PDF" else "Bagikan")
                }
            }
        )
    }
}

@Composable
internal fun TabunganLogCard(
    log: TabunganLog,
    isEn: Boolean,
    onStudentClick: (() -> Unit)? = null
) {
    val actionColor = when (log.actionType.trim().uppercase()) {
        TabunganHelper.LOG_INPUT_IN -> Color(0xFF15803D)
        TabunganHelper.LOG_INPUT_OUT -> Color(0xFFDC2626)
        TabunganHelper.LOG_CANCEL -> Color(0xFFB45309)
        TabunganHelper.LOG_DELETE -> Color(0xFF7F1D1D)
        else -> Color(0xFF475569)
    }
    val actionBg = when (log.actionType.trim().uppercase()) {
        TabunganHelper.LOG_INPUT_IN -> Color(0xFFDCFCE7)
        TabunganHelper.LOG_INPUT_OUT -> Color(0xFFFEE2E2)
        TabunganHelper.LOG_CANCEL -> Color(0xFFFEF3C7)
        TabunganHelper.LOG_DELETE -> Color(0xFFFEE2E2)
        else -> Color(0xFFF1F5F9)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_tabungan_log_${log.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = actionBg) {
                    Text(
                        text = TabunganHelper.logActionLabel(log.actionType, isEn),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = actionColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = DateUtils.formatDateTime(log.createdAt),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val titleContent = "${log.studentName}${if (log.studentClass.isNotBlank()) " (${log.studentClass})" else ""}"
                if (onStudentClick != null) {
                    TextButton(
                        onClick = onStudentClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = titleContent,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = titleContent,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = TabunganHelper.formatRupiah(log.amount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = actionColor
                )
            }
            if (log.summary.isNotBlank()) {
                Text(log.summary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${if (isEn) "Balance" else "Saldo"}: ${TabunganHelper.formatRupiah(log.balanceBefore)} → ${TabunganHelper.formatRupiah(log.balanceAfter)}",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB45309)
                )
                Text(
                    text = log.actorName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
