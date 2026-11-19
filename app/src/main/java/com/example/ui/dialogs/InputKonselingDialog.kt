package com.example.ui.dialogs

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingRecord
import com.example.util.DateUtils
import com.example.util.KonselingPhotoHelper
import com.example.util.KonselingPhotoSlot
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.launch
import java.util.Calendar

internal enum class KonselingVoiceField {
    TITLE,
    SUMMARY,
    INTERVENTION,
    FOLLOW_UP
}

internal fun appendVoiceTranscript(
    existingText: String,
    spokenText: String
): String {
    val cleanSpoken = spokenText.trim()
    if (cleanSpoken.isBlank()) return existingText
    if (existingText.isBlank()) return cleanSpoken
    return if (existingText.endsWith("\n")) {
        existingText + cleanSpoken
    } else {
        existingText.trimEnd { it == ' ' } + " " + cleanSpoken
    }
}

internal fun buildGoogleSpeechToTextIntent(
    promptText: String,
    languageTag: String
): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
        putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InputKonselingDialog(
    claimedStudents: List<ChildStudent>,
    defaultEntryType: String = KonselingHelper.ENTRY_SESI,
    initialStudent: ChildStudent? = null,
    recordToEdit: KonselingRecord? = null,
    onOpenPilihAnakAsuh: () -> Unit,
    onSave: (
        student: ChildStudent,
        entryType: String,
        topic: String,
        urgencyLevel: String,
        moodLevel: String,
        title: String,
        summary: String,
        intervention: String,
        followUpPlan: String,
        followUpDate: Long?,
        sessionDate: Long,
        markCompleted: Boolean,
        photoPaths: String,
        photoUrls: String,
        existingRecord: KonselingRecord?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language
    val isEditMode = recordToEdit != null

    val resolvedEditStudent = remember(recordToEdit?.id, claimedStudents, initialStudent) {
        if (recordToEdit == null) {
            initialStudent
        } else {
            initialStudent
                ?: claimedStudents.firstOrNull { KonselingHelper.matchesStudent(recordToEdit, it) }
                ?: ChildStudent(
                    id = recordToEdit.studentId,
                    name = recordToEdit.studentName,
                    nis = recordToEdit.studentNis,
                    gender = "L",
                    age = 12,
                    dormitory = recordToEdit.studentDorm,
                    classGrade = recordToEdit.studentClass,
                    assignedWaliUsername = recordToEdit.ownerWaliUsername,
                    assignedWaliName = recordToEdit.ownerWaliName,
                    schoolId = recordToEdit.schoolId
                )
        }
    }

    val selectableStudents = remember(claimedStudents, resolvedEditStudent) {
        val combined = if (resolvedEditStudent != null && claimedStudents.none { it.id == resolvedEditStudent.id }) {
            listOf(resolvedEditStudent) + claimedStudents
        } else {
            claimedStudents
        }
        combined.sortedBy { it.name.lowercase() }
    }

    var selectedStudent by remember(resolvedEditStudent?.id, recordToEdit?.id) {
        mutableStateOf(
            resolvedEditStudent ?: if (selectableStudents.size == 1) selectableStudents.first() else null
        )
    }
    var studentSearch by remember { mutableStateOf("") }
    var entryType by remember(defaultEntryType, recordToEdit?.id) {
        val raw = recordToEdit?.entryType ?: defaultEntryType
        mutableStateOf(
            if (KonselingHelper.isGrowthEntry(raw)) KonselingHelper.ENTRY_PERKEMBANGAN else KonselingHelper.ENTRY_SESI
        )
    }
    var topic by remember(recordToEdit?.id, defaultEntryType) {
        mutableStateOf(
            recordToEdit?.topic ?: if (KonselingHelper.isGrowthEntry(defaultEntryType)) {
                KonselingHelper.TOPIC_APRESIASI
            } else {
                KonselingHelper.TOPIC_ADAPTASI
            }
        )
    }
    var urgencyLevel by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.urgencyLevel ?: KonselingHelper.URGENCY_RINGAN)
    }
    var moodLevel by remember(recordToEdit?.id) {
        mutableStateOf(
            recordToEdit?.moodLevel ?: if (KonselingHelper.isGrowthEntry(defaultEntryType)) {
                KonselingHelper.MOOD_BAIK
            } else {
                KonselingHelper.MOOD_NETRAL
            }
        )
    }
    var titleText by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.title ?: "")
    }
    var summaryText by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.summary ?: "")
    }
    var interventionText by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.intervention ?: "")
    }
    var followUpPlanText by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.followUpPlan ?: "")
    }
    var sessionDate by remember(recordToEdit?.id) {
        mutableLongStateOf(recordToEdit?.sessionDate ?: System.currentTimeMillis())
    }
    var followUpDate by remember(recordToEdit?.id) {
        mutableStateOf<Long?>(recordToEdit?.followUpDate)
    }
    var markCompleted by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.let { KonselingHelper.isCompleted(it.status) } ?: false)
    }
    var showFollowUpDetails by remember(recordToEdit?.id) {
        mutableStateOf(
            recordToEdit != null && (
                recordToEdit.intervention.isNotBlank() ||
                    recordToEdit.followUpPlan.isNotBlank() ||
                    recordToEdit.followUpDate != null
                )
        )
    }
    var photoSlots by remember(recordToEdit?.id) {
        mutableStateOf(
            recordToEdit?.let { KonselingPhotoHelper.extractSlots(it) } ?: emptyList()
        )
    }
    var newlyCreatedPhotoPaths by remember(recordToEdit?.id) {
        mutableStateOf<List<String>>(emptyList())
    }
    var isProcessingPhotos by remember { mutableStateOf(false) }
    var previewPhotoSlot by remember { mutableStateOf<KonselingPhotoSlot?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val isGrowth = KonselingHelper.isGrowthEntry(entryType)
    val canSave = selectedStudent != null && (summaryText.isNotBlank() || titleText.isNotBlank())
    val quickTitles = if (isGrowth) {
        if (isEn) listOf("Better Dorm Adaptation", "Improved Study Focus", "More Cheerful & Sociable")
        else listOf("Lebih Betah di Asrama", "Semangat Belajar Meningkat", "Lebih Ceria & Komunikatif")
    } else {
        if (isEn) listOf("Personal Listening Session", "Homesickness Check-In", "Peer Conflict Mediation")
        else listOf("Pendampingan Personal", "Rindu Rumah & Adaptasi", "Mediasi Pertemanan")
    }

    // Tema warna senada induk ikon Konseling (Lavender-Ungu Royal & Mint-Teal)
    val headerGradient = if (isGrowth) {
        listOf(Color(0xFF0F766E), Color(0xFF15803D))
    } else {
        listOf(Color(0xFF4C1D95), Color(0xFF6D28D9))
    }
    val primaryAccent = if (isGrowth) Color(0xFF0F766E) else Color(0xFF6D28D9)
    val accentSoftBg = if (isGrowth) Color(0xFFF0FDF4) else Color(0xFFF5F3FF)
    val accentBorder = if (isGrowth) Color(0xFFA7F3D0) else Color(0xFFDDD6FE)
    val headerThumbRes = if (isGrowth) R.drawable.img_menu_konseling_perkembangan else R.drawable.img_menu_konseling_catat

    var activeVoiceField by remember { mutableStateOf<KonselingVoiceField?>(null) }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val targetField = activeVoiceField
        activeVoiceField = null
        if (result.resultCode == Activity.RESULT_OK && targetField != null) {
            val transcript = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (transcript.isNotBlank()) {
                when (targetField) {
                    KonselingVoiceField.TITLE -> {
                        titleText = appendVoiceTranscript(titleText, transcript)
                    }
                    KonselingVoiceField.SUMMARY -> {
                        summaryText = appendVoiceTranscript(summaryText, transcript)
                    }
                    KonselingVoiceField.INTERVENTION -> {
                        interventionText = appendVoiceTranscript(interventionText, transcript)
                    }
                    KonselingVoiceField.FOLLOW_UP -> {
                        followUpPlanText = appendVoiceTranscript(followUpPlanText, transcript)
                    }
                }
            }
        }
    }

    fun processPickedUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val remaining = (KonselingPhotoHelper.MAX_PHOTOS - photoSlots.size).coerceAtLeast(0)
        if (remaining <= 0) {
            Toast.makeText(
                context,
                if (isEn) "Maximum 3 photos per session." else "Maksimal 3 foto per sesi konseling.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val toProcess = uris.take(remaining)
        if (uris.size > remaining) {
            Toast.makeText(
                context,
                if (isEn) "Only $remaining photo slot(s) remaining (max 3)." else "Hanya $remaining slot foto tersisa (maksimal 3 foto).",
                Toast.LENGTH_SHORT
            ).show()
        }
        isProcessingPhotos = true
        coroutineScope.launch {
            val added = mutableListOf<KonselingPhotoSlot>()
            for (uri in toProcess) {
                val saved = KonselingPhotoHelper.compressAndSavePhoto(
                    context = context,
                    sourceUri = uri,
                    idHint = selectedStudent?.id ?: 0L
                )
                if (saved != null) {
                    added.add(KonselingPhotoSlot(localPath = saved.absolutePath, remoteUrl = ""))
                }
            }
            if (added.isNotEmpty()) {
                photoSlots = (photoSlots + added).take(KonselingPhotoHelper.MAX_PHOTOS)
                newlyCreatedPhotoPaths = newlyCreatedPhotoPaths + added.map { it.localPath }
            } else {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to process selected photo." else "Gagal memproses foto yang dipilih.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            isProcessingPhotos = false
        }
    }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(KonselingPhotoHelper.MAX_PHOTOS)
    ) { uris ->
        processPickedUris(uris)
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) processPickedUris(listOf(uri))
    }

    fun openPhotoPicker() {
        val remaining = KonselingPhotoHelper.MAX_PHOTOS - photoSlots.size
        if (remaining <= 0) {
            Toast.makeText(
                context,
                if (isEn) "Maximum 3 photos reached." else "Maksimal 3 foto sudah tercapai.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        if (remaining >= 2) {
            multiPhotoPickerLauncher.launch(request)
        } else {
            singlePhotoPickerLauncher.launch(request)
        }
    }

    fun cleanupUnsavedPhotosAndDismiss() {
        newlyCreatedPhotoPaths.forEach { KonselingPhotoHelper.deleteLocalPhotoFile(it) }
        onDismiss()
    }

    fun startVoiceInput(field: KonselingVoiceField) {
        activeVoiceField = field
        val prompt = when (field) {
            KonselingVoiceField.TITLE -> if (isEn) "Speak session title..." else "Ucapkan judul / tema konseling..."
            KonselingVoiceField.SUMMARY -> if (isEn) "Speak counseling summary..." else "Ucapkan ringkasan curhat / observasi..."
            KonselingVoiceField.INTERVENTION -> if (isEn) "Speak caregiver advice / support..." else "Ucapkan pendampingan / nasehat..."
            KonselingVoiceField.FOLLOW_UP -> if (isEn) "Speak follow-up plan..." else "Ucapkan rencana tindak lanjut..."
        }
        val languageTag = if (isEn) "en-US" else "id-ID"
        val intent = buildGoogleSpeechToTextIntent(promptText = prompt, languageTag = languageTag)
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

    ModalBottomSheet(
        onDismissRequest = { cleanupUnsavedPhotosAndDismiss() },
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
                    .widthIn(max = 580.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Gradient Senada Ikon Induk Konseling + Drag Pill di bagian atas
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
                                    .border(1.5.dp, accentBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = headerThumbRes),
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
                                        isEditMode -> if (isEn) "Edit Confidential Note" else "Edit Catatan Konseling"
                                        isGrowth -> if (isEn) "Log Student Growth" else "Catat Perkembangan Anak"
                                        else -> if (isEn) "Record Counseling Session" else "Catat Sesi Konseling"
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
                                        tint = Color(0xFFEDE9FE),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Confidential • Owner Wali Asuh only" else "Rahasia • Hanya Wali Asuh bersangkutan",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFEDE9FE),
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
                                .testTag("btn_close_input_konseling")
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
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    // Strip Deklarasi Privasi Mutlak 1-2 Baris Kompak
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_input_konseling_privacy"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                        border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF6D28D9),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "Strictly private: hidden from Admin, other Caregivers, Teachers & Parents."
                                } else {
                                    "Privasi mutlak: Tersembunyi dari Admin, Wali lain, Guru Ngaji & Ortu."
                                },
                                fontSize = 10.5.sp,
                                lineHeight = 13.5.sp,
                                color = Color(0xFF4C1D95),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (selectableStudents.isEmpty() && !isEditMode) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_input_konseling_no_claimed"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                            border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Text(
                                    text = if (isEn) "Select Your Foster Students First" else "Pilih Anak Binaan Anda Terlebih Dahulu",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF4C1D95)
                                )
                                Text(
                                    text = if (isEn) {
                                        "Counseling notes are private to your own foster students. Claim students under Parent Connect → Select Foster Kids."
                                    } else {
                                        "Konseling hanya dapat dicatat untuk anak asuh binaan Anda sendiri agar privasinya terjaga."
                                    },
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF5B21B6)
                                )
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onOpenPilihAnakAsuh()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_input_konseling_open_pilih")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isEn) "Select Foster Students" else "Pilih Anak Asuh", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Segmented Entry Type Toggle (Sesi Konseling | Perkembangan)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(11.dp))
                                .background(accentSoftBg)
                                .border(1.dp, accentBorder, RoundedCornerShape(11.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isGrowth) Color(0xFF6D28D9) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { entryType = KonselingHelper.ENTRY_SESI }
                                    .testTag("chip_konseling_type_sesi")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = if (!isGrowth) Color.White else Color(0xFF5B21B6),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isEn) "Counseling Session" else "Sesi Konseling",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isGrowth) Color.White else Color(0xFF5B21B6),
                                        maxLines = 1
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isGrowth) Color(0xFF0F766E) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { entryType = KonselingHelper.ENTRY_PERKEMBANGAN }
                                    .testTag("chip_konseling_type_perkembangan")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = if (isGrowth) Color.White else Color(0xFF15803D),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isEn) "Growth Note" else "Perkembangan",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGrowth) Color.White else Color(0xFF15803D),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Anak Binaan + Tanggal Catatan Sejajar 1 Baris (Hemat ruang vertikal)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Foster Student" else "Anak Binaan",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryAccent
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentSoftBg,
                                border = BorderStroke(1.dp, accentBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cal = Calendar.getInstance().apply { timeInMillis = sessionDate }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val chosen = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, y)
                                                    set(Calendar.MONTH, m)
                                                    set(Calendar.DAY_OF_MONTH, d)
                                                    set(Calendar.HOUR_OF_DAY, 10)
                                                    set(Calendar.MINUTE, 0)
                                                }
                                                sessionDate = chosen.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                    .testTag("btn_konseling_pick_date")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = primaryAccent, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = DateUtils.formatShortDate(sessionDate, lang),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryAccent
                                    )
                                }
                            }
                        }

                        if (isEditMode && selectedStudent != null) {
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = accentSoftBg,
                                border = BorderStroke(1.dp, accentBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${selectedStudent!!.name} (${selectedStudent!!.classGrade.ifBlank { "-" }})",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        } else {
                            if (selectableStudents.size > 3) {
                                OutlinedTextField(
                                    value = studentSearch,
                                    onValueChange = { studentSearch = it },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    placeholder = { Text(if (isEn) "Search student..." else "Cari nama anak...", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_konseling_student_search")
                                )
                            }
                            val filteredStudents = remember(selectableStudents, studentSearch) {
                                val q = studentSearch.trim().lowercase()
                                if (q.isEmpty()) selectableStudents
                                else selectableStudents.filter {
                                    it.name.lowercase().contains(q) || it.classGrade.lowercase().contains(q)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                filteredStudents.forEach { st ->
                                    val isSel = selectedStudent?.id == st.id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) primaryAccent else Color.Transparent,
                                        border = BorderStroke(1.dp, if (isSel) primaryAccent else accentBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedStudent = st }
                                            .testTag("chip_konseling_student_${st.id}")
                                    ) {
                                        Text(
                                            text = "${st.name} (${st.classGrade.ifBlank { "-" }})",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            if (selectedStudent == null) {
                                Text(
                                    text = if (isEn) "Select a foster student above first." else "Pilih satu anak binaan di atas terlebih dahulu.",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Fokus Topik Kompak (Tanpa rongga 48dp FilterChip)
                        Text(
                            text = if (isEn) "Focus Topic" else "Fokus Topik",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            KonselingHelper.ALL_TOPICS.forEach { t ->
                                val isSel = topic.equals(t, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) accentSoftBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, if (isSel) primaryAccent else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { topic = t }
                                        .testTag("chip_konseling_topic_${t.lowercase()}")
                                ) {
                                    Text(
                                        text = KonselingHelper.formatTopicLabel(t, isEn),
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Tingkat Perhatian & Kondisi Emosional Kompak (Label Utuh Tanpa Terpotong)
                        Text(
                            text = if (isEn) "Priority & Mood" else "Perhatian & Kondisi Emosi",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            listOf(
                                KonselingHelper.URGENCY_RINGAN,
                                KonselingHelper.URGENCY_PANTAU,
                                KonselingHelper.URGENCY_PRIORITAS
                            ).forEach { urg ->
                                val isSel = urgencyLevel.equals(urg, ignoreCase = true)
                                val (selBg, selFg, selBorder) = when (urg) {
                                    KonselingHelper.URGENCY_PRIORITAS -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), Color(0xFFFCA5A5))
                                    KonselingHelper.URGENCY_PANTAU -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFCD34D))
                                    else -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Color(0xFF86EFAC))
                                }
                                val shortLabel = when (urg) {
                                    KonselingHelper.URGENCY_PRIORITAS -> if (isEn) "Priority" else "Prioritas"
                                    KonselingHelper.URGENCY_PANTAU -> if (isEn) "Monitor" else "Pantauan"
                                    else -> if (isEn) "Light" else "Ringan"
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) selBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, if (isSel) selBorder else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { urgencyLevel = urg }
                                        .testTag("chip_konseling_urgency_${urg.lowercase()}")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = shortLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) selFg else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            listOf(
                                KonselingHelper.MOOD_BAIK,
                                KonselingHelper.MOOD_NETRAL,
                                KonselingHelper.MOOD_SEDIH,
                                KonselingHelper.MOOD_TERTEKAN
                            ).forEach { m ->
                                val isSel = moodLevel.equals(m, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Color(0xFFCCFBF1) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, if (isSel) Color(0xFF0F766E) else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { moodLevel = m }
                                        .testTag("chip_konseling_mood_${m.lowercase()}")
                                ) {
                                    Text(
                                        text = KonselingHelper.formatMoodLabel(m, isEn),
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color(0xFF0F766E) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Preset Judul + Judul Singkat + Ringkasan
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            quickTitles.forEach { preset ->
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = accentSoftBg,
                                    border = BorderStroke(1.dp, accentBorder.copy(alpha = 0.7f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .clickable { titleText = preset }
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = primaryAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            label = { Text(if (isEn) "Session Title / Subject" else "Judul Singkat / Tema", fontSize = 12.sp) },
                            trailingIcon = {
                                KonselingVoiceMicButton(
                                    isActive = activeVoiceField == KonselingVoiceField.TITLE,
                                    accentColor = primaryAccent,
                                    softBg = accentSoftBg,
                                    contentDesc = if (isEn) "Voice input for title" else "Dikte suara untuk judul",
                                    testTag = "btn_voice_konseling_title",
                                    onClick = { startVoiceInput(KonselingVoiceField.TITLE) }
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_konseling_title")
                        )

                        OutlinedTextField(
                            value = summaryText,
                            onValueChange = { summaryText = it },
                            label = { Text(if (isEn) "Observation & Conversation Summary *" else "Ringkasan Curhat / Observasi *", fontSize = 12.sp) },
                            trailingIcon = {
                                KonselingVoiceMicButton(
                                    isActive = activeVoiceField == KonselingVoiceField.SUMMARY,
                                    accentColor = primaryAccent,
                                    softBg = accentSoftBg,
                                    contentDesc = if (isEn) "Voice input for summary" else "Dikte suara untuk ringkasan curhat",
                                    testTag = "btn_voice_konseling_summary",
                                    onClick = { startVoiceInput(KonselingVoiceField.SUMMARY) }
                                )
                            },
                            supportingText = {
                                Text(
                                    text = if (isEn) {
                                        "Tap the microphone (Google Voice) to dictate and append notes."
                                    } else {
                                        "Ketuk ikon mik (Google Voice) untuk dikte & menyambung catatan."
                                    },
                                    fontSize = 10.sp,
                                    color = primaryAccent
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_konseling_summary")
                        )

                        // Bagian Opsional Pendampingan & Jadwal Pantau (Collapsible agar default tetap 1 halaman)
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = accentSoftBg.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, accentBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { showFollowUpDetails = !showFollowUpDetails }
                                .testTag("btn_toggle_konseling_advanced")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) {
                                        "Support, Follow-Up Plan & Schedule (Optional)"
                                    } else {
                                        "Pendampingan, Tindak Lanjut & Jadwal (Opsional)"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryAccent,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (showFollowUpDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = showFollowUpDetails) {
                            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                OutlinedTextField(
                                    value = interventionText,
                                    onValueChange = { interventionText = it },
                                    label = { Text(if (isEn) "Caregiver Support / Advice Given" else "Pendampingan / Nasehat yang Diberikan", fontSize = 12.sp) },
                                    trailingIcon = {
                                        KonselingVoiceMicButton(
                                            isActive = activeVoiceField == KonselingVoiceField.INTERVENTION,
                                            accentColor = primaryAccent,
                                            softBg = accentSoftBg,
                                            contentDesc = if (isEn) "Voice input for caregiver support" else "Dikte suara untuk pendampingan",
                                            testTag = "btn_voice_konseling_intervention",
                                            onClick = { startVoiceInput(KonselingVoiceField.INTERVENTION) }
                                        )
                                    },
                                    minLines = 2,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_konseling_intervention")
                                )

                                OutlinedTextField(
                                    value = followUpPlanText,
                                    onValueChange = { followUpPlanText = it },
                                    label = { Text(if (isEn) "Follow-Up Plan (Optional)" else "Rencana Tindak Lanjut (Opsional)", fontSize = 12.sp) },
                                    trailingIcon = {
                                        KonselingVoiceMicButton(
                                            isActive = activeVoiceField == KonselingVoiceField.FOLLOW_UP,
                                            accentColor = primaryAccent,
                                            softBg = accentSoftBg,
                                            contentDesc = if (isEn) "Voice input for follow-up plan" else "Dikte suara untuk rencana tindak lanjut",
                                            testTag = "btn_voice_konseling_followup",
                                            onClick = { startVoiceInput(KonselingVoiceField.FOLLOW_UP) }
                                        )
                                    },
                                    minLines = 1,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_konseling_followup")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val cal = Calendar.getInstance().apply {
                                                timeInMillis = followUpDate ?: (System.currentTimeMillis() + 3 * 86_400_000L)
                                            }
                                            DatePickerDialog(
                                                context,
                                                { _, y, m, d ->
                                                    val chosen = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, y)
                                                        set(Calendar.MONTH, m)
                                                        set(Calendar.DAY_OF_MONTH, d)
                                                        set(Calendar.HOUR_OF_DAY, 15)
                                                        set(Calendar.MINUTE, 0)
                                                    }
                                                    followUpDate = chosen.timeInMillis
                                                },
                                                cal.get(Calendar.YEAR),
                                                cal.get(Calendar.MONTH),
                                                cal.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(9.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 42.dp)
                                            .testTag("btn_konseling_pick_followup_date")
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = followUpDate?.let {
                                                "${if (isEn) "Follow-up" else "Jadwal Pantau"}: ${DateUtils.formatShortDate(it, lang)}"
                                            } ?: if (isEn) "Set Follow-Up Date (Optional)" else "Atur Jadwal Pantau (Opsional)",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (followUpDate != null) {
                                        TextButton(
                                            onClick = { followUpDate = null },
                                            modifier = Modifier.heightIn(min = 42.dp)
                                        ) {
                                            Text(if (isEn) "Clear" else "Hapus", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Dokumentasi Foto Sesi Konseling via Android Photo Picker (Opsional, 1-3 Foto)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accentSoftBg.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, accentBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_konseling_photo_picker")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = primaryAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = if (isEn) {
                                                    "Counseling Photos (${photoSlots.size}/3)"
                                                } else {
                                                    "Foto Dokumentasi Konseling (${photoSlots.size}/3)"
                                                },
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryAccent
                                            )
                                            Text(
                                                text = if (isEn) {
                                                    "Optional (1-3 photos via Photo Picker, attached to PDF)"
                                                } else {
                                                    "Opsional (1-3 foto via Photo Picker, otomatis terlampir di PDF)"
                                                },
                                                fontSize = 9.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (photoSlots.size < KonselingPhotoHelper.MAX_PHOTOS) {
                                        OutlinedButton(
                                            onClick = { openPhotoPicker() },
                                            enabled = !isProcessingPhotos,
                                            shape = RoundedCornerShape(9.dp),
                                            border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.6f)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_pick_konseling_photos")
                                        ) {
                                            if (isProcessingPhotos) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = primaryAccent
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.AddPhotoAlternate,
                                                    contentDescription = if (isEn) "Pick photos" else "Pilih foto",
                                                    tint = primaryAccent,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isEn) "Pick Photo" else "Pilih Foto",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryAccent
                                            )
                                        }
                                    }
                                }

                                if (photoSlots.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        photoSlots.forEachIndexed { idx, slot ->
                                            val model = slot.displayModel()
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1.15f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .border(
                                                        1.dp,
                                                        accentBorder,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { previewPhotoSlot = slot }
                                                    .testTag("thumb_konseling_photo_$idx")
                                            ) {
                                                if (model != null) {
                                                    AsyncImage(
                                                        model = model,
                                                        contentDescription = if (isEn) "Counseling photo ${idx + 1}" else "Foto konseling ${idx + 1}",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.matchParentSize()
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                                                    color = Color.Black.copy(alpha = 0.62f),
                                                    modifier = Modifier.align(Alignment.TopStart)
                                                ) {
                                                    Text(
                                                        text = "${idx + 1}",
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        val removedPath = slot.localPath
                                                        photoSlots = photoSlots.filterIndexed { i, _ -> i != idx }
                                                        if (removedPath in newlyCreatedPhotoPaths) {
                                                            KonselingPhotoHelper.deleteLocalPhotoFile(removedPath)
                                                            newlyCreatedPhotoPaths = newlyCreatedPhotoPaths - removedPath
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .size(28.dp)
                                                        .padding(2.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black.copy(alpha = 0.65f))
                                                        .testTag("btn_remove_konseling_photo_$idx")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = if (isEn) "Remove photo ${idx + 1}" else "Hapus foto ${idx + 1}",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                        // Spacer slot kosong agar ukuran grid konsisten untuk 1 atau 2 foto
                                        repeat((KonselingPhotoHelper.MAX_PHOTOS - photoSlots.size).coerceAtLeast(0)) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // Toggle Tandai Selesai Kompak
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { markCompleted = !markCompleted },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = markCompleted,
                                onCheckedChange = { markCompleted = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF15803D)),
                                modifier = Modifier.testTag("switch_konseling_completed")
                            )
                            Text(
                                text = if (isEn) "Mark as Resolved / Improved immediately" else "Tandai langsung Selesai / Membaik",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        HorizontalDivider(color = accentBorder.copy(alpha = 0.7f))

                        // Action Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { cleanupUnsavedPhotosAndDismiss() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(0.38f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(s.nav.cancel, fontSize = 12.5.sp)
                            }
                            Button(
                                onClick = {
                                    val st = selectedStudent ?: return@Button
                                    val (packedPaths, packedUrls) = KonselingPhotoHelper.packSlots(photoSlots)
                                    onSave(
                                        st,
                                        entryType,
                                        topic,
                                        urgencyLevel,
                                        moodLevel,
                                        titleText,
                                        summaryText,
                                        interventionText,
                                        followUpPlanText,
                                        followUpDate,
                                        sessionDate,
                                        markCompleted,
                                        packedPaths,
                                        packedUrls,
                                        recordToEdit
                                    )
                                },
                                enabled = canSave && !isProcessingPhotos,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                                modifier = Modifier
                                    .weight(0.62f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_save_konseling")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isEditMode) {
                                        if (isEn) "Save Changes" else "Simpan Perubahan"
                                    } else {
                                        if (isEn) "Save Private Note" else "Simpan Rahasia"
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

    previewPhotoSlot?.let { slot ->
        val model = slot.displayModel()
        if (model != null) {
            Dialog(onDismissRequest = { previewPhotoSlot = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = model,
                            contentDescription = if (isEn) "Counseling photo preview" else "Pratinjau foto konseling",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 420.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Button(
                            onClick = { previewPhotoSlot = null },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Text(s.nav.close)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KonselingVoiceMicButton(
    isActive: Boolean,
    accentColor: Color,
    softBg: Color,
    contentDesc: String,
    testTag: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isActive) accentColor else softBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = contentDesc,
                tint = if (isActive) Color.White else accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
