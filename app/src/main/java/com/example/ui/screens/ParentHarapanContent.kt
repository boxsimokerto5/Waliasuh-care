package com.example.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentExpectationHelper
import com.example.data.model.SchoolProfile
import com.example.ui.components.ParentPdfReadyDialog
import com.example.ui.components.ParentReadyPdfDialogData
import com.example.ui.dialogs.appendVoiceTranscript
import com.example.ui.dialogs.buildGoogleSpeechToTextIntent
import com.example.util.DateUtils
import com.example.util.ParentPortalPdfHelper
import com.example.util.i18n.LocalAppStrings

private enum class ParentHarapanVoiceField {
    CHARACTER,
    ADVICE,
    PRAYER
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentHarapanContent(
    student: ChildStudent?,
    expectations: List<ParentExpectation>,
    onSaveExpectation: (
        focusTags: List<String>,
        characterExpectation: String,
        adviceFocusNotes: String,
        parentPrayerMessage: String,
        existing: ParentExpectation?
    ) -> Unit,
    onDeleteExpectation: (ParentExpectation) -> Unit,
    schoolProfile: SchoolProfile = SchoolProfile(),
    waliName: String = "Wali Asuh"
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    var editingItem by remember { mutableStateOf<ParentExpectation?>(null) }
    var selectedFocusTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var characterText by remember { mutableStateOf("") }
    var adviceNotesText by remember { mutableStateOf("") }
    var prayerText by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<ParentExpectation?>(null) }
    var readyPdfData by remember { mutableStateOf<ParentReadyPdfDialogData?>(null) }

    var activeVoiceField by remember { mutableStateOf<ParentHarapanVoiceField?>(null) }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val targetField = activeVoiceField
        activeVoiceField = null
        if (result.resultCode == Activity.RESULT_OK && targetField != null) {
            val transcript = result.data
                ?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (transcript.isNotBlank()) {
                when (targetField) {
                    ParentHarapanVoiceField.CHARACTER -> {
                        characterText = appendVoiceTranscript(characterText, transcript)
                    }
                    ParentHarapanVoiceField.ADVICE -> {
                        adviceNotesText = appendVoiceTranscript(adviceNotesText, transcript)
                    }
                    ParentHarapanVoiceField.PRAYER -> {
                        prayerText = appendVoiceTranscript(prayerText, transcript)
                    }
                }
            }
        }
    }

    fun startVoiceInput(field: ParentHarapanVoiceField) {
        activeVoiceField = field
        val prompt = when (field) {
            ParentHarapanVoiceField.CHARACTER -> if (isEn) {
                "Speak your character expectations for your child..."
            } else {
                "Ucapkan harapan karakter & sikap ananda..."
            }
            ParentHarapanVoiceField.ADVICE -> if (isEn) {
                "Speak habits or attitudes for Wali Asuh to advise..."
            } else {
                "Ucapkan hal yang perlu dinasehatkan Wali Asuh..."
            }
            ParentHarapanVoiceField.PRAYER -> if (isEn) {
                "Speak your prayer or encouragement..."
            } else {
                "Ucapkan doa atau pesan penyemangat..."
            }
        }
        val intent = buildGoogleSpeechToTextIntent(
            promptText = prompt,
            languageTag = if (isEn) "en-US" else "id-ID"
        )
        try {
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            activeVoiceField = null
            Toast.makeText(
                context,
                if (isEn) {
                    "Google Voice-to-Text service is not available on this device."
                } else {
                    "Layanan Suara ke Teks Google belum tersedia di perangkat ini."
                },
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            activeVoiceField = null
            Toast.makeText(
                context,
                if (isEn) {
                    "Unable to launch Google Voice input: ${e.message ?: "Error"}"
                } else {
                    "Gagal membuka Suara ke Teks Google: ${e.message ?: "Error"}"
                },
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun resetForm() {
        editingItem = null
        selectedFocusTags = emptySet()
        characterText = ""
        adviceNotesText = ""
        prayerText = ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_harapan_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Kartu Petunjuk Ikhtiar Bersama (3 Tahun atau Lebih di Sekolah Rakyat)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_harapan_guidance"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFECFDF5), Color(0xFFEEF2FF))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF047857)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Character Expectations for Your Child" else "Harapan Karakter pada Anak",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF064E3B)
                                )
                                Text(
                                    text = if (isEn) "Shared Effort • Parents & Wali Asuh" else "Ikhtiar Bersama • Orang Tua & Wali Asuh",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color(0xFFD1FAE5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(16.dp)
                                )
                                Text(
                                    text = if (isEn) {
                                        "For 3 years or more, your child spends most of their time at Sekolah Rakyat. Please write your expectations about your child's character and attitude, so Wali Asuh can advise and guide them directly — all of this is our shared effort (ikhtiar bersama)."
                                    } else {
                                        "Selama 3 tahun atau lebih, waktu anak lebih banyak dihabiskan di Sekolah Rakyat. Maka tuliskan harapan Anda tentang karakter anak Anda, agar nanti harapan sikap ini coba dinasehatkan oleh Wali Asuh kepada anak. Semua ini merupakan ikhtiar bersama."
                                    },
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Formulir Harapan Karakter (dengan Google Voice-to-Text)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_form_parent_harapan"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (editingItem != null) Color(0xFF4F46E5) else MaterialTheme.colorScheme.outlineVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editingItem != null) {
                                    if (isEn) "Edit Character Expectation" else "Ubah Harapan Karakter"
                                } else {
                                    if (isEn) "Write Your Expectation Form" else "Formulir Harapan Karakter"
                                },
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) {
                                    "Tap the microphone icon to dictate faster via Google Voice-to-Text."
                                } else {
                                    "Tekan ikon mikrofon untuk mengetik cepat dengan Suara ke Teks Google."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (editingItem != null) {
                            TextButton(
                                onClick = { resetForm() },
                                modifier = Modifier.testTag("btn_cancel_edit_parent_harapan")
                            ) {
                                Text(if (isEn) "Cancel" else "Batal")
                            }
                        }
                    }

                    // Pilihan Fokus Karakter
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isEn) "Character Focus Pillars (Optional)" else "Fokus Nilai Karakter (Opsional)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ParentExpectationHelper.FOCUS_OPTIONS.forEach { option ->
                                val isSelected = option.key in selectedFocusTags
                                val label = if (isEn) option.labelEn else option.labelId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedFocusTags = if (isSelected) {
                                            selectedFocusTags - option.key
                                        } else {
                                            selectedFocusTags + option.key
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFD1FAE5),
                                        selectedLabelColor = Color(0xFF065F46),
                                        selectedLeadingIconColor = Color(0xFF065F46)
                                    ),
                                    modifier = Modifier.testTag("chip_harapan_focus_${option.key}")
                                )
                            }
                        }
                    }

                    // Input 1: Harapan Karakter & Sikap Ananda (Wajib) + Voice
                    OutlinedTextField(
                        value = characterText,
                        onValueChange = { characterText = it },
                        label = {
                            Text(
                                if (isEn) "Character & Attitude Expectations *" else "Harapan Karakter & Sikap Ananda *"
                            )
                        },
                        placeholder = {
                            Text(
                                if (isEn) {
                                    "Example: I hope my child becomes more honest, disciplined in prayers, and respectful to elders..."
                                } else {
                                    "Contoh: Semoga ananda tumbuh lebih jujur, disiplin sholat berjamaah, dan ramah kepada teman..."
                                },
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { startVoiceInput(ParentHarapanVoiceField.CHARACTER) },
                                modifier = Modifier
                                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    .testTag("btn_voice_parent_harapan_karakter")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = if (isEn) "Voice input for character expectation" else "Suara ke teks harapan karakter",
                                    tint = Color(0xFF047857)
                                )
                            }
                        },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_parent_harapan_karakter")
                    )

                    // Input 2: Kebiasaan / Sikap yang Perlu Dinasehatkan Wali Asuh + Voice
                    OutlinedTextField(
                        value = adviceNotesText,
                        onValueChange = { adviceNotesText = it },
                        label = {
                            Text(
                                if (isEn) "Habits / Areas for Wali Asuh to Advise" else "Hal / Kebiasaan yang Perlu Dinasehatkan Wali Asuh"
                            )
                        },
                        placeholder = {
                            Text(
                                if (isEn) {
                                    "Example: Often forgets to tidy up clothes or tends to hold back emotions..."
                                } else {
                                    "Contoh: Mohon sesekali diingatkan agar lebih rajin merapikan barang dan sabar saat berbeda pendapat..."
                                },
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { startVoiceInput(ParentHarapanVoiceField.ADVICE) },
                                modifier = Modifier
                                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    .testTag("btn_voice_parent_harapan_nasehat")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = if (isEn) "Voice input for advice focus" else "Suara ke teks hal yang dinasehatkan",
                                    tint = Color(0xFF047857)
                                )
                            }
                        },
                        minLines = 2,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_parent_harapan_nasehat")
                    )

                    // Input 3: Doa & Pesan Penyemangat + Voice
                    OutlinedTextField(
                        value = prayerText,
                        onValueChange = { prayerText = it },
                        label = {
                            Text(
                                if (isEn) "Parent's Prayer / Encouragement (Optional)" else "Doa & Pesan Penyemangat Orang Tua (Opsional)"
                            )
                        },
                        placeholder = {
                            Text(
                                if (isEn) {
                                    "Example: Stay strong at Sekolah Rakyat, Mom and Dad always pray for you..."
                                } else {
                                    "Contoh: Tetap semangat belajar di Sekolah Rakyat ya Nak, ayah & bunda selalu mendoakan..."
                                },
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { startVoiceInput(ParentHarapanVoiceField.PRAYER) },
                                modifier = Modifier
                                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    .testTag("btn_voice_parent_harapan_doa")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = if (isEn) "Voice input for parent prayer" else "Suara ke teks doa orang tua",
                                    tint = Color(0xFF047857)
                                )
                            }
                        },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_parent_harapan_doa")
                    )

                    Button(
                        onClick = {
                            val cleanChar = characterText.trim()
                            if (cleanChar.isBlank()) {
                                Toast.makeText(
                                    context,
                                    if (isEn) {
                                        "Please fill in your character expectations first."
                                    } else {
                                        "Mohon isi harapan karakter ananda terlebih dahulu."
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            onSaveExpectation(
                                selectedFocusTags.toList(),
                                cleanChar,
                                adviceNotesText.trim(),
                                prayerText.trim(),
                                editingItem
                            )
                            resetForm()
                        },
                        enabled = student != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF047857),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                            .testTag("btn_save_parent_harapan")
                    ) {
                        Text(
                            text = if (editingItem != null) {
                                if (isEn) "Save Expectation Changes" else "Simpan Perubahan Harapan"
                            } else {
                                if (isEn) "Send Character Expectation" else "Kirim Harapan Karakter"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }

        // 3. Daftar Harapan yang Sudah Dikirim
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) "Submitted Expectations (${expectations.size})" else "Daftar Harapan Tersimpan (${expectations.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (student != null) {
                    OutlinedButton(
                        onClick = {
                            val pdf = ParentPortalPdfHelper.generateStudentHarapanReportPdf(
                                context = context,
                                student = student,
                                expectations = expectations,
                                waliName = waliName.ifBlank { "Wali Asuh" },
                                schoolProfile = schoolProfile
                            )
                            if (pdf != null) {
                                readyPdfData = ParentReadyPdfDialogData(
                                    file = pdf,
                                    title = if (isEn) "Character Expectations PDF Ready" else "PDF Harapan Karakter Siap",
                                    description = if (isEn) {
                                        "Open or share your submitted character expectations & prayers for ${student.name}."
                                    } else {
                                        "Pilih buka PDF atau bagikan catatan harapan karakter & doa ananda ${student.name}."
                                    },
                                    shareSubject = if (isEn) "Character Expectations - ${student.name}" else "Harapan Karakter - ${student.name}",
                                    shareText = if (isEn) {
                                        "Attached character expectations & prayers PDF for ${student.name}."
                                    } else {
                                        "Terlampir PDF catatan harapan karakter & doa orang tua untuk ${student.name}."
                                    }
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Unable to create expectations PDF" else "Gagal membuat PDF harapan karakter",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF047857)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_parent_harapan_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isEn) "PDF" else "Unduh PDF",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }
        }

        if (expectations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_parent_harapan_list"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isEn) {
                                "No character expectations written yet."
                            } else {
                                "Belum ada harapan karakter yang dituliskan."
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Use the form above to share your hopes with Wali Asuh."
                            } else {
                                "Gunakan formulir di atas untuk menyampaikan harapan kepada Wali Asuh."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(expectations, key = { it.id }) { item ->
                val focusList = remember(item.characterFocusCsv) {
                    ParentExpectationHelper.parseFocusList(item.characterFocusCsv)
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_parent_harapan_item_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (item.waliFollowUpNote.isNotBlank()) Color(0xFF6EE7B7)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Baris status & tanggal + tombol Edit/Hapus
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (item.isReadByWali) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isReadByWali) Icons.Default.MarkChatRead else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (item.isReadByWali) Color(0xFF15803D) else Color(0xFFB45309),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (item.isReadByWali) {
                                            if (isEn) "Read by Wali Asuh" else "Sudah Dibaca Wali Asuh"
                                        } else {
                                            if (isEn) "Waiting for Wali Asuh" else "Menunggu Dibaca Wali Asuh"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isReadByWali) Color(0xFF15803D) else Color(0xFFB45309)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = DateUtils.formatShortDate(item.updatedAt, lang),
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = {
                                        editingItem = item
                                        selectedFocusTags = ParentExpectationHelper.parseFocusList(item.characterFocusCsv).toSet()
                                        characterText = item.characterExpectation
                                        adviceNotesText = item.adviceFocusNotes
                                        prayerText = item.parentPrayerMessage
                                    },
                                    modifier = Modifier
                                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                        .testTag("btn_edit_parent_harapan_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = if (isEn) "Edit expectation" else "Ubah harapan",
                                        tint = Color(0xFF4338CA),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { itemToDelete = item },
                                    modifier = Modifier
                                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                        .testTag("btn_delete_parent_harapan_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = if (isEn) "Delete expectation" else "Hapus harapan",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (focusList.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                focusList.forEach { tagKey ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEEF2FF)
                                    ) {
                                        Text(
                                            text = ParentExpectationHelper.displayFocusLabel(tagKey, isEn),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF3730A3),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = item.characterExpectation,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (item.adviceFocusNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = if (isEn) "Advice Requested for Wali Asuh:" else "Hal untuk Dinasehatkan Wali Asuh:",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.adviceFocusNotes,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (item.parentPrayerMessage.isNotBlank()) {
                            Text(
                                text = (if (isEn) "Prayer & Note: " else "Doa & Pesan: ") + item.parentPrayerMessage,
                                fontSize = 12.sp,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (item.waliFollowUpNote.isNotBlank()) {
                            HorizontalDivider()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isEn) {
                                            "Wali Asuh Mentoring Note (${item.waliName.ifBlank { "Wali Asuh" }})"
                                        } else {
                                            "Catatan Ikhtiar & Nasehat Wali Asuh (${item.waliName.ifBlank { "Wali Asuh" }})"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF065F46),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.waliFollowUpNote,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (item.waliRespondedAt > 0L) {
                                        Text(
                                            text = DateUtils.formatDateTime(item.waliRespondedAt, lang),
                                            fontSize = 10.sp,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    itemToDelete?.let { pendingDelete ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(if (isEn) "Delete Expectation?" else "Hapus Harapan Karakter?")
            },
            text = {
                Text(
                    if (isEn) {
                        "Are you sure you want to delete this expectation entry?"
                    } else {
                        "Apakah Anda yakin ingin menghapus catatan harapan karakter ini?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingItem?.id == pendingDelete.id) {
                            resetForm()
                        }
                        onDeleteExpectation(pendingDelete)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isEn) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    readyPdfData?.let { dialogData ->
        ParentPdfReadyDialog(
            data = dialogData,
            isEn = isEn,
            onDismiss = { readyPdfData = null }
        )
    }
}
