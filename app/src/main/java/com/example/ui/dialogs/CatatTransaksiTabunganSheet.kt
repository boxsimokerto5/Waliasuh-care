package com.example.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.SavingRetentionHelper
import com.example.data.model.SavingTransaction
import com.example.util.DateUtils
import com.example.util.SavingProofPhotoHelper
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatTransaksiTabunganSheet(
    students: List<ChildStudent>,
    savingTransactions: List<SavingTransaction>,
    initialStudent: ChildStudent? = null,
    initialType: String = SavingTransaction.TYPE_EXPENSE,
    onSave: (
        student: ChildStudent,
        transactionType: String,
        amount: Long,
        transactionDate: Long,
        purpose: String,
        notes: String,
        proofPhotoLocalPath: String,
        autoShareToWa: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var transactionType by remember { mutableStateOf(initialType) }
    var selectedStudent by remember { mutableStateOf(initialStudent ?: students.firstOrNull()) }
    var showStudentPicker by remember { mutableStateOf(false) }
    var transactionDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    var purposeText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    var proofPhotoLocalPath by remember { mutableStateOf("") }
    var isCompressingPhoto by remember { mutableStateOf(false) }
    var autoShareToWa by remember { mutableStateOf(true) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val currentBalance = remember(selectedStudent, savingTransactions) {
        val st = selectedStudent ?: return@remember 0L
        SavingRetentionHelper.computeStudentBalance(
            transactions = savingTransactions,
            studentId = st.id,
            studentNik = st.nik,
            studentName = st.name
        )
    }

    val amountValue = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val isExpense = transactionType == SavingTransaction.TYPE_EXPENSE
    val exceedsBalance = isExpense && selectedStudent != null && amountValue > currentBalance

    val processSelectedPhotoUri: (Uri) -> Unit = { uri ->
        isCompressingPhoto = true
        scope.launch {
            val savedFile = SavingProofPhotoHelper.compressAndSaveProofPhoto(
                context = context,
                sourceUri = uri,
                idHint = selectedStudent?.id ?: 0L
            )
            isCompressingPhoto = false
            if (savedFile != null) {
                if (proofPhotoLocalPath.isNotBlank()) {
                    SavingProofPhotoHelper.deleteLocalProofFile(proofPhotoLocalPath)
                }
                proofPhotoLocalPath = savedFile.absolutePath
            } else {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to process selected image" else "Gagal memproses foto bukti",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            processSelectedPhotoUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            processSelectedPhotoUri(uri)
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, _) = SavingProofPhotoHelper.createTempCameraImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                if (isEn) "Camera permission is required to capture proof" else "Izin kamera diperlukan untuk memotret bukti nota",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
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
                    .testTag("catat_transaksi_tabungan_sheet")
            ) {
                // Header gradien hijau elegan sesuai desain Tabungan Anak Asuh
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0F766E))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_menu_tabungan),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column {
                                Text(
                                    text = if (isEn) "Record Savings Transaction" else "Catat Transaksi Tabungan",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFFDE68A),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Private • Wali Asuh & Admin" else "Pribadi • Wali Asuh & Admin Sekolah",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_catat_tabungan")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = s.nav.close,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Toggle Segmented Titip Masuk (+) vs Pengeluaran (-)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEFCE8),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val depositSelected = !isExpense
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (depositSelected) Color(0xFF047857) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { transactionType = SavingTransaction.TYPE_DEPOSIT }
                                    .testTag("tab_tabungan_deposit")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = null,
                                        tint = if (depositSelected) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "Deposit (+)" else "Titip Masuk (+)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (depositSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isExpense) Color(0xFFDC2626) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { transactionType = SavingTransaction.TYPE_EXPENSE }
                                    .testTag("tab_tabungan_expense")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RemoveCircle,
                                        contentDescription = null,
                                        tint = if (isExpense) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "Expense (-)" else "Pengeluaran (-)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpense) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }

                    // Baris Anak Asuh Binaan + Pemilih Tanggal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Foster Student" else "Anak Asuh Binaan",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF78350F)
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                            modifier = Modifier
                                .heightIn(min = 38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showDatePicker = true }
                                .testTag("btn_tabungan_date")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = DateUtils.formatShortDate(transactionDate, s.language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }

                    // Kartu Anak Terpilih + Saldo + Tombol Ganti
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEFCE8),
                        border = BorderStroke(1.dp, Color(0xFFFDE047)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedStudent?.name ?: if (isEn) "Select student" else "Pilih anak asuh",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF78350F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selectedStudent != null) {
                                        val cls = selectedStudent!!.classGrade.ifBlank { selectedStudent!!.dormitory }
                                        "$cls • Saldo: ${SavingRetentionHelper.formatRupiah(currentBalance)}"
                                    } else {
                                        if (isEn) "Tap Change to pick a student" else "Tekan Ganti untuk memilih siswa"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                            OutlinedButton(
                                onClick = { showStudentPicker = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("btn_ganti_siswa_tabungan")
                            ) {
                                Text(
                                    text = if (isEn) "Change" else "Ganti",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }

                    // Input Nominal + Chip Cepat
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { raw -> amountText = raw.filter { it.isDigit() }.take(9) },
                        label = { Text(if (isEn) "Amount (Rp)" else "Nominal Uang (Rp)") },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_nominal_tabungan")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5000L, 10000L, 20000L, 50000L, 100000L).forEach { nominal ->
                            FilterChip(
                                selected = amountValue == nominal,
                                onClick = { amountText = nominal.toString() },
                                label = { Text(SavingRetentionHelper.formatRupiah(nominal), fontSize = 11.5.sp) }
                            )
                        }
                    }

                    if (exceedsBalance) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Warning: Expense exceeds current balance (${SavingRetentionHelper.formatRupiah(currentBalance)})."
                                } else {
                                    "Perhatian: Nominal melebihi saldo anak saat ini (${SavingRetentionHelper.formatRupiah(currentBalance)})."
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Input Keterangan / Keperluan + Chip Cepat
                    OutlinedTextField(
                        value = purposeText,
                        onValueChange = { purposeText = it },
                        label = { Text(if (isEn) "Description / Purpose" else "Keterangan / Keperluan") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_keperluan_tabungan")
                    )

                    val purposePresets = if (isExpense) {
                        listOf(
                            "Jajan / Kebutuhan Harian",
                            "Alat Tulis & Buku",
                            "Perlengkapan Mandi",
                            "Kesehatan / Vitamin",
                            "Kegiatan Sekolah"
                        )
                    } else {
                        listOf(
                            "Titipan Orang Tua",
                            "Uang Saku Bulanan",
                            "Sisa Belanja",
                            "Hadiah / Beasiswa"
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        purposePresets.forEach { preset ->
                            FilterChip(
                                selected = purposeText.equals(preset, ignoreCase = true),
                                onClick = { purposeText = preset },
                                label = { Text(preset, fontSize = 11.5.sp) }
                            )
                        }
                    }

                    // BUKTI FOTO PENGELUARAN (Khusus Pengeluaran, Retensi 3 Bulan + Kirim WA)
                    AnimatedVisibility(visible = isExpense) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("section_bukti_foto_pengeluaran")
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
                                    Text(
                                        text = if (isEn) "Expense Photo Proof" else "Bukti Foto Pengeluaran",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF14532D)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = if (isEn) "Stored 90 Days" else "Aktif 3 Bulan (DB)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (isCompressingPhoto) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Text(
                                            text = if (isEn) "Compressing photo..." else "Mengompresi foto bukti...",
                                            fontSize = 12.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                } else if (proofPhotoLocalPath.isNotBlank()) {
                                    val file = File(proofPhotoLocalPath)
                                    val kbSize = (file.length() / 1024L).coerceAtLeast(1L)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            AsyncImage(
                                                model = file,
                                                contentDescription = if (isEn) "Proof preview" else "Pratinjau bukti foto",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(10.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF16A34A),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Text(
                                                        text = if (isEn) "Photo attached (~$kbSize KB)" else "Foto siap (~$kbSize KB)",
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF14532D)
                                                    )
                                                }
                                                Text(
                                                    text = if (isEn) {
                                                        "Auto-deletes from DB after 3 months"
                                                    } else {
                                                        "Bertahan 90 hari di DB lalu dibersihkan otomatis"
                                                    },
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    SavingProofPhotoHelper.deleteLocalProofFile(proofPhotoLocalPath)
                                                    proofPhotoLocalPath = ""
                                                },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .testTag("btn_remove_proof_photo")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = if (isEn) "Remove photo" else "Hapus foto",
                                                    tint = Color(0xFFDC2626)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val hasCam = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasCam) {
                                                val (uri, _) = SavingProofPhotoHelper.createTempCameraImageUri(context)
                                                pendingCameraUri = uri
                                                cameraLauncher.launch(uri)
                                            } else {
                                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF16A34A)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_tabungan_camera")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) "Camera" else "Kamera",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            galleryPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF0284C7)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_tabungan_gallery")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = Color(0xFF0369A1),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) "Gallery" else "Galeri",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0369A1)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = if (isEn) "Send to Parent WhatsApp" else "Kirim ke WA Orang Tua setelah simpan",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF14532D)
                                            )
                                            Text(
                                                text = if (isEn) {
                                                    "Receipt photo auto-saves to parent's WA Gallery"
                                                } else {
                                                    "Struk foto otomatis masuk Galeri WA orang tua saat terkirim"
                                                },
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = autoShareToWa,
                                        onCheckedChange = { autoShareToWa = it },
                                        modifier = Modifier.testTag("switch_auto_share_wa_tabungan")
                                    )
                                }
                            }
                        }
                    }

                    // Catatan Tambahan
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text(if (isEn) "Additional Notes (Optional)" else "Catatan Tambahan (Opsional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_catatan_tabungan")
                    )

                    // Tombol Aksi Bawah
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (proofPhotoLocalPath.isNotBlank()) {
                                    SavingProofPhotoHelper.deleteLocalProofFile(proofPhotoLocalPath)
                                }
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .heightIn(min = 50.dp)
                        ) {
                            Text(
                                text = if (isEn) "Cancel" else "Batal",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val canSubmit = selectedStudent != null && amountValue > 0L && !exceedsBalance && !isCompressingPhoto
                        Button(
                            onClick = {
                                val st = selectedStudent ?: return@Button
                                val finalPurpose = purposeText.trim().ifBlank {
                                    if (isExpense) "Keperluan Anak Asuh" else "Titipan Tabungan"
                                }
                                onSave(
                                    st,
                                    transactionType,
                                    amountValue,
                                    transactionDate,
                                    finalPurpose,
                                    notesText.trim(),
                                    if (isExpense) proofPhotoLocalPath else "",
                                    isExpense && autoShareToWa
                                )
                            },
                            enabled = canSubmit,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isExpense) Color(0xFFDC2626) else Color(0xFF047857)
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .heightIn(min = 50.dp)
                                .testTag("btn_submit_tabungan")
                        ) {
                            Text(
                                text = if (isExpense) {
                                    if (isEn) "Save Expense (-)" else "Catat Pengeluaran (-)"
                                } else {
                                    if (isEn) "Save Deposit (+)" else "Catat Titip Masuk (+)"
                                },
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = transactionDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { transactionDate = it }
                    showDatePicker = false
                }) {
                    Text(if (isEn) "Select" else "Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    if (showStudentPicker) {
        var searchQuery by remember { mutableStateOf("") }
        val filtered = remember(students, searchQuery) {
            if (searchQuery.isBlank()) students
            else students.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.classGrade.contains(searchQuery, ignoreCase = true) ||
                    it.dormitory.contains(searchQuery, ignoreCase = true)
            }
        }
        AlertDialog(
            onDismissRequest = { showStudentPicker = false },
            title = {
                Text(
                    text = if (isEn) "Select Foster Student" else "Pilih Anak Asuh",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isEn) "Search name or class..." else "Cari nama atau kelas...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered, key = { it.id }) { st ->
                            val stBal = SavingRetentionHelper.computeStudentBalance(
                                savingTransactions, st.id, st.nik, st.name
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedStudent?.id == st.id) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedStudent?.id == st.id) Color(0xFF10B981) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedStudent = st
                                        showStudentPicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(st.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${st.classGrade} • ${st.dormitory}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                    Text(
                                        text = SavingRetentionHelper.formatRupiah(stBal),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudentPicker = false }) {
                    Text(if (isEn) "Close" else "Tutup")
                }
            }
        )
    }
}
