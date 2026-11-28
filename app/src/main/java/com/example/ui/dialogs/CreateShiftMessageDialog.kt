package com.example.ui.dialogs

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateShiftMessageDialog(
    viewModel: HealthViewModel,
    waliAccounts: List<WaliAccount>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    var title by remember { mutableStateOf("") }
    var followUpDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedShift by remember { mutableStateOf("Semua Shift") }
    val shiftOptions = listOf("Semua Shift", "Shift Pagi (07-14)", "Shift Siang (14-21)", "Shift Malam (21-07)")

    var targetWali by remember { mutableStateOf("Semua Wali Asuh") }
    var showWaliDropdown by remember { mutableStateOf(false) }

    var messageContent by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("BIASA") } // URGENT, SEDANG, BIASA
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Palet warna pastel lembut Emerald-Teal + Lavender + Peach
    val sheetBg = if (isLight) Color(0xFFF2FBF9) else MaterialTheme.colorScheme.surface
    val tealPrimary = if (isLight) Color(0xFF0F766E) else Color(0xFF5EEAD4)
    val indigoAccent = if (isLight) Color(0xFF4F46E5) else Color(0xFFA5B4FC)

    // Voice-to-Text Speech Recognition Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenResults?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                messageContent = if (messageContent.isBlank()) {
                    recognizedText
                } else {
                    "$messageContent $recognizedText"
                }
                Toast.makeText(
                    context,
                    if (isEn) "Voice converted to text!" else "Suara berhasil diubah ke teks!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isEn) "en-US" else "id-ID")
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (isEn) "Speak now to dictate the shift message..." else "Bicara sekarang untuk mendiktekan isi pesan shift..."
            )
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                if (isEn) "Google voice input is not available on this device." else "Fitur suara Google tidak tersedia pada perangkat ini.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        containerColor = sheetBg
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
                    .widthIn(max = 600.dp)
            ) {
                // Header Gradien Emerald–Teal Hangat
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF065F46),
                                    Color(0xFF0D9488),
                                    Color(0xFF0284C7)
                                )
                            )
                        )
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.48f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFF99F6E4), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Leave Shift Message" else "Tinggalkan Pesan Shift",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isEn) {
                                        "Duty handover & important notes for the next shift"
                                    } else {
                                        "Operan jaga & catatan penting untuk shift berikutnya"
                                    },
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFCCFBF1)
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = s.nav.cancel,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Isi Form Kartu Pastel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(bottom = 14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Kartu 1: Judul, Tanggal & Shift (Nuansa Mint-Sky Pastel)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isLight) Color.White else Color(0xFF132220),
                        border = BorderStroke(
                            1.dp,
                            if (isLight) Color(0xFF99F6E4) else Color(0xFF0D9488).copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    errorMessage = null
                                },
                                label = { Text(if (isEn) "Message Title *" else "Judul Pesan *") },
                                placeholder = {
                                    Text(
                                        if (isEn) "Example: Monitor student Ahmad fever / Night medication"
                                        else "Contoh: Pantau santri Ahmad demam / Minum obat malam"
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = tealPrimary,
                                    unfocusedBorderColor = if (isLight) Color(0xFFBAE6FD) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    focusedLabelColor = tealPrimary,
                                    focusedContainerColor = if (isLight) Color(0xFFF9FFFE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    unfocusedContainerColor = if (isLight) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_shift_title")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    text = if (isEn) "Follow-Up Date:" else "Tanggal Tindak Lanjut:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tealPrimary
                                )
                                OutlinedButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isLight) Color(0xFF7DD3FC) else Color(0xFF0284C7).copy(alpha = 0.45f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isLight) Color(0xFFF0F9FF) else Color(0xFF11212E)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = DateUtils.formatFullDate(followUpDate),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    text = if (isEn) "Duty Shift Selection:" else "Pilihan Shift Bertugas:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tealPrimary
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    shiftOptions.forEachIndexed { idx, opt ->
                                        val isSelected = selectedShift == opt
                                        val optLabel = if (isEn && opt == "Semua Shift") "All Shifts" else opt
                                        val (chipBg, chipFg, chipBorder) = when (idx) {
                                            0 -> Triple(
                                                if (isLight) Color(0xFFCCFBF1) else Color(0xFF134E4A).copy(alpha = 0.7f),
                                                if (isLight) Color(0xFF0F766E) else Color(0xFF5EEAD4),
                                                if (isLight) Color(0xFF5EEAD4) else Color(0xFF0F766E).copy(alpha = 0.5f)
                                            )
                                            1 -> Triple(
                                                if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.7f),
                                                if (isLight) Color(0xFFB45309) else Color(0xFFFDE68A),
                                                if (isLight) Color(0xFFFCD34D) else Color(0xFFD97706).copy(alpha = 0.5f)
                                            )
                                            2 -> Triple(
                                                if (isLight) Color(0xFFE0F2FE) else Color(0xFF0C4A6E).copy(alpha = 0.7f),
                                                if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC),
                                                if (isLight) Color(0xFF7DD3FC) else Color(0xFF0284C7).copy(alpha = 0.5f)
                                            )
                                            else -> Triple(
                                                if (isLight) Color(0xFFEDE9FE) else Color(0xFF4C1D95).copy(alpha = 0.7f),
                                                if (isLight) Color(0xFF6D28D9) else Color(0xFFC4B5FD),
                                                if (isLight) Color(0xFFC4B5FD) else Color(0xFF7C3AED).copy(alpha = 0.5f)
                                            )
                                        }
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedShift = opt },
                                            label = {
                                                Text(
                                                    text = optLabel,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = chipBorder,
                                                selectedBorderColor = chipFg.copy(alpha = 0.65f)
                                            ),
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                selectedContainerColor = chipBg,
                                                selectedLabelColor = chipFg
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Kartu 2: Penerima & Isi Instruksi (Nuansa Lavender Pastel)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isLight) Color.White else Color(0xFF1A192D),
                        border = BorderStroke(
                            1.dp,
                            if (isLight) Color(0xFFDDD6FE) else Color(0xFF6D28D9).copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    text = if (isEn) "To (Recipient):" else "Kepada (Penerima):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = indigoAccent
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { showWaliDropdown = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isLight) Color(0xFFC4B5FD) else Color(0xFF7C3AED).copy(alpha = 0.45f)
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isLight) Color(0xFFF5F3FF) else Color(0xFF231F3D)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.People,
                                            contentDescription = null,
                                            tint = indigoAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isEn && targetWali == "Semua Wali Asuh") "All Wali Asuh" else targetWali,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = indigoAccent
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showWaliDropdown,
                                        onDismissRequest = { showWaliDropdown = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (isEn) "All Wali Asuh (Broadcast)" else "Semua Wali Asuh (Broadcast)",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            onClick = {
                                                targetWali = "Semua Wali Asuh"
                                                showWaliDropdown = false
                                            }
                                        )
                                        waliAccounts.forEach { acc ->
                                            DropdownMenuItem(
                                                text = { Text("${acc.name} (${acc.dormitory})") },
                                                onClick = {
                                                    targetWali = acc.name
                                                    showWaliDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isEn) "Message / Handover Instructions *" else "Isi Pesan / Instruksi Operan *",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = indigoAccent,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Tombol Mikrofon Voice to Text
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .heightIn(min = 38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D).copy(alpha = 0.7f)
                                            )
                                            .border(
                                                0.8.dp,
                                                if (isLight) Color(0xFF86EFAC) else Color(0xFF16A34A).copy(alpha = 0.5f),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { startVoiceRecognition() }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("btn_voice_to_text")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice to Text",
                                            tint = if (isLight) Color(0xFF15803D) else Color(0xFF86EFAC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isEn) "Speak (Voice)" else "Bicara (Suara)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) Color(0xFF15803D) else Color(0xFF86EFAC),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = messageContent,
                                    onValueChange = {
                                        messageContent = it
                                        errorMessage = null
                                    },
                                    placeholder = {
                                        Text(
                                            if (isEn) "Write instructions or tap Speak (Voice to Text)..."
                                            else "Tuliskan instruksi atau gunakan tombol Bicara (Voice to Text)..."
                                        )
                                    },
                                    minLines = 3,
                                    maxLines = 6,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = indigoAccent,
                                        unfocusedBorderColor = if (isLight) Color(0xFFDDD6FE) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedContainerColor = if (isLight) Color(0xFFFCFBFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                        unfocusedContainerColor = if (isLight) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_shift_content")
                                )
                            }
                        }
                    }

                    // Kartu 3: Prioritas Pesan (Nuansa Krem-Amber Lembut)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isLight) Color(0xFFFFFCF3) else Color(0xFF282012),
                        border = BorderStroke(
                            1.dp,
                            if (isLight) Color(0xFFFDE68A) else Color(0xFFD97706).copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isEn) "Message Priority:" else "Sifat / Prioritas Pesan:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Color(0xFFB45309) else Color(0xFFFDE68A)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Urgent
                                val urgentSel = selectedPriority == "URGENT"
                                FilterChip(
                                    selected = urgentSel,
                                    onClick = { selectedPriority = "URGENT" },
                                    label = {
                                        Text(
                                            text = if (isEn) "🔴 Urgent (Emergency)" else "🔴 Urgent (Darurat)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = urgentSel,
                                        borderColor = if (isLight) Color(0xFFFDA4AF) else Color(0xFFE11D48).copy(alpha = 0.5f),
                                        selectedBorderColor = if (isLight) Color(0xFFBE123C) else Color(0xFFFDA4AF)
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        selectedContainerColor = if (isLight) Color(0xFFFFE4E6) else Color(0xFF881337).copy(alpha = 0.7f),
                                        selectedLabelColor = if (isLight) Color(0xFFBE123C) else Color(0xFFFFE4E6)
                                    )
                                )

                                // Sedang
                                val sedangSel = selectedPriority == "SEDANG"
                                FilterChip(
                                    selected = sedangSel,
                                    onClick = { selectedPriority = "SEDANG" },
                                    label = {
                                        Text(
                                            text = if (isEn) "🟠 Medium" else "🟠 Sedang",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = sedangSel,
                                        borderColor = if (isLight) Color(0xFFFCD34D) else Color(0xFFD97706).copy(alpha = 0.5f),
                                        selectedBorderColor = if (isLight) Color(0xFFB45309) else Color(0xFFFCD34D)
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        selectedContainerColor = if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.7f),
                                        selectedLabelColor = if (isLight) Color(0xFFB45309) else Color(0xFFFEF3C7)
                                    )
                                )

                                // Biasa
                                val biasaSel = selectedPriority == "BIASA"
                                FilterChip(
                                    selected = biasaSel,
                                    onClick = { selectedPriority = "BIASA" },
                                    label = {
                                        Text(
                                            text = if (isEn) "🟢 Normal" else "🟢 Biasa",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = biasaSel,
                                        borderColor = if (isLight) Color(0xFF86EFAC) else Color(0xFF16A34A).copy(alpha = 0.5f),
                                        selectedBorderColor = if (isLight) Color(0xFF15803D) else Color(0xFF86EFAC)
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        selectedContainerColor = if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D).copy(alpha = 0.7f),
                                        selectedLabelColor = if (isLight) Color(0xFF15803D) else Color(0xFFDCFCE7)
                                    )
                                )
                            }
                        }
                    }

                    // Tombol Aksi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSubmitting,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(s.nav.cancel, maxLines = 1, softWrap = false)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val cleanTitle = title.trim()
                                val cleanContent = messageContent.trim()

                                if (cleanTitle.isBlank()) {
                                    errorMessage = if (isEn) "Message title is required." else "Judul pesan wajib diisi."
                                    return@Button
                                }

                                if (cleanContent.isBlank()) {
                                    errorMessage = if (isEn) {
                                        "Message content is required or use the voice button."
                                    } else {
                                        "Isi pesan wajib diisi atau gunakan tombol suara."
                                    }
                                    return@Button
                                }

                                isSubmitting = true
                                viewModel.addShiftMessage(
                                    title = cleanTitle,
                                    followUpDate = followUpDate,
                                    shift = selectedShift,
                                    targetWali = targetWali,
                                    content = cleanContent,
                                    priority = selectedPriority
                                ) {
                                    isSubmitting = false
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Shift message saved!" else "Pesan shift berhasil ditinggalkan!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F766E),
                                contentColor = Color.White
                            ),
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_submit_shift_message")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSubmitting) {
                                    if (isEn) "Saving..." else "Menyimpan..."
                                } else {
                                    if (isEn) "Leave Message" else "Tinggalkan Pesan"
                                },
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = followUpDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        followUpDate = DateUtils.utcDateMillisToLocalNoon(it)
                    }
                    showDatePicker = false
                }) {
                    Text(if (isEn) "Select" else "Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(s.nav.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
