package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SchoolRegistration
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolRegistrationDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        schoolName: String,
        address: String,
        city: String,
        estimatedWaliCount: Int,
        picName: String,
        picEmail: String,
        picPhone: String,
        onSubmitted: (SchoolRegistration) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var schoolName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var estimatedWaliText by remember { mutableStateOf("6") }
    var picName by remember { mutableStateOf("") }
    var picEmail by remember { mutableStateOf("") }
    var picPhone by remember { mutableStateOf("") }

    var submittedRegistration by remember { mutableStateOf<SchoolRegistration?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Palet warna lembut serasi dengan Login & modul utama aplikasi
    val oceanPrimary = if (isLight) Color(0xFF0A6C82) else Color(0xFF38BDF8)
    val indigoAccent = if (isLight) Color(0xFF4F46E5) else Color(0xFFA5B4FC)
    val tealAccent = if (isLight) Color(0xFF0F766E) else Color(0xFF5EEAD4)
    val sheetBg = if (isLight) Color(0xFFF3FAF8) else MaterialTheme.colorScheme.surface

    val schoolCardBg = if (isLight) Color.White else Color(0xFF132224)
    val schoolCardBorder = if (isLight) Color(0xFF99F6E4) else Color(0xFF0F766E).copy(alpha = 0.5f)
    val picCardBg = if (isLight) Color.White else Color(0xFF1B1D2E)
    val picCardBorder = if (isLight) Color(0xFFC7D2FE) else Color(0xFF4F46E5).copy(alpha = 0.5f)

    fun sendToSuperadminWhatsApp(reg: SchoolRegistration? = null) {
        val sName = reg?.schoolName ?: schoolName.trim()
        val sCity = reg?.city ?: city.trim()
        val sAddr = reg?.address ?: address.trim()
        val sWali = reg?.estimatedWaliCount ?: (estimatedWaliText.toIntOrNull() ?: 6)
        val sPic = reg?.picName ?: picName.trim()
        val sEmail = reg?.picEmail ?: picEmail.trim()
        val sPhone = reg?.picPhone ?: picPhone.trim()
        val formattedDate = DateUtils.formatFullDate(System.currentTimeMillis(), appStrings.language)

        val text = if (isEn) {
            """
            *NEW SEKOLAH RAKYAT REGISTRATION - WALIASUH CARE*
            --------------------------------------------------
            🏫 *Sekolah Rakyat Name:* $sName
            📍 *Address:* $sAddr
            🏙️ *City/Regency:* $sCity
            👥 *Estimated Wali Asuh Count:* $sWali people
            
            👤 *Person in Charge (PIC):* $sPic
            ✉️ *Email:* ${sEmail.ifBlank { "-" }}
            📱 *WhatsApp No.:* $sPhone
            📅 *Date:* $formattedDate
            --------------------------------------------------
            Hello Superadmin Wali Asuh Care, I have completed the Sekolah Rakyat registration form above and attached a screenshot of the registration proof. Please verify and issue our school admin access account. Thank you!
            """.trimIndent()
        } else {
            """
            *PENGAJUAN PENDAFTARAN SEKOLAH RAKYAT BARU - WALIASUH CARE*
            --------------------------------------------------
            🏫 *Nama Sekolah Rakyat:* $sName
            📍 *Alamat:* $sAddr
            🏙️ *Kota/Kabupaten:* $sCity
            👥 *Estimasi Jumlah Wali Asuh:* $sWali orang
            
            👤 *Nama Penanggung Jawab:* $sPic
            ✉️ *Email:* ${sEmail.ifBlank { "-" }}
            📱 *No. WhatsApp:* $sPhone
            📅 *Tanggal:* $formattedDate
            --------------------------------------------------
            Halo Superadmin Wali Asuh Care, saya telah melengkapi formulir pendaftaran Sekolah Rakyat di atas dan melampirkan screenshot bukti pendaftaran. Mohon untuk diverifikasi dan diterbitkan akun akses admin sekolah kami. Terima kasih!
            """.trimIndent()
        }

        val superadminWaNumber = "6285755735676"
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$superadminWaNumber&text=${Uri.encode(text)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    if (isEn) "Sekolah Rakyat Registration" else "Pendaftaran Sekolah Rakyat",
                    text
                )
            )
            Toast.makeText(
                context,
                if (isEn) {
                    "Message copied. Please send to WhatsApp 085755735676"
                } else {
                    "Format pesan telah disalin. Silakan kirim ke WA 085755735676"
                },
                Toast.LENGTH_LONG
            ).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("sheet_school_registration")
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
            ) {
                // Header Gradien Edge-to-Edge Senada Halaman Login & Tabungan
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                if (submittedRegistration == null) {
                                    listOf(
                                        Color(0xFF064E78),
                                        Color(0xFF0A6C82),
                                        Color(0xFF0F766E)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFF064E3B),
                                        Color(0xFF047857),
                                        Color(0xFF0D9488)
                                    )
                                }
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.5.dp, Color(0xFF99F6E4)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            if (submittedRegistration == null) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_sr_care),
                                    contentDescription = if (isEn) "Sekolah Rakyat Emblem" else "Logo Sekolah Rakyat",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (submittedRegistration == null) {
                                    if (isEn) "New Sekolah Rakyat Registration" else "Pendaftaran Sekolah Rakyat"
                                } else {
                                    if (isEn) "Registration Submitted!" else "Pengajuan Berhasil Dikirim!"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (submittedRegistration == null) {
                                    if (isEn) {
                                        "Apply for an official School Admin access account"
                                    } else {
                                        "Ajukan akun akses Admin untuk unit Sekolah Rakyat Anda"
                                    }
                                } else {
                                    if (isEn) {
                                        "Screenshot this proof & send to Superadmin WA"
                                    } else {
                                        "Screenshot bukti ini & kirim ke WA Superadmin untuk aktivasi"
                                    }
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFFCCFBF1),
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_registration")
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
                                    contentDescription = if (isEn) "Close" else "Tutup",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Konten Scrollable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (submittedRegistration != null) {
                        val reg = submittedRegistration!!

                        // TAMPILAN BUKTI TIKET PENGAJUAN
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.2.dp,
                                    if (isLight) Color(0xFF6EE7B7) else Color(0xFF059669).copy(alpha = 0.6f),
                                    RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLight) Color.White else Color(0xFF132220)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isLight) Color(0xFFD1FAE5) else Color(0xFF065F46).copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (isLight) Color(0xFF047857) else Color(0xFF6EE7B7),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isEn) "REGISTRATION PROOF" else "BUKTI PENGAJUAN",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.5.sp,
                                                color = if (isLight) Color(0xFF047857) else Color(0xFF6EE7B7)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.55f)
                                    ) {
                                        Text(
                                            text = if (isEn) "WAITING VERIFICATION" else "MENUNGGU VERIFIKASI",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) Color(0xFFB45309) else Color(0xFFFCD34D),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = if (isEn) "Sekolah Rakyat Name" else "Nama Sekolah Rakyat",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = reg.schoolName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${reg.city} • ${reg.address}",
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isLight) Color(0xFFF1F8F6) else Color(0xFF1C2D2B),
                                    modifier = Modifier.fillMaxWidth()
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
                                                text = if (isEn) "PIC: ${reg.picName}" else "PJ: ${reg.picName}",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = reg.picPhone + if (reg.picEmail.isNotBlank()) " • ${reg.picEmail}" else "",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isLight) Color(0xFFE0E7FF) else Color(0xFF312E81).copy(alpha = 0.7f)
                                        ) {
                                            Text(
                                                text = "${reg.estimatedWaliCount} Wali Asuh",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLight) Color(0xFF3730A3) else Color(0xFFC7D2FE),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // KOTAK INSTRUKSI SCREENSHOT & KIRIM WA
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isLight) Color(0xFFFFFBEB) else Color(0xFF282115),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFFCD34D) else Color(0xFFD97706).copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = if (isLight) Color(0xFFB45309) else Color(0xFFFBBF24),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (isEn) "Next Step: Verification" else "Langkah Terakhir: Verifikasi Akun",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) Color(0xFF92400E) else Color(0xFFFDE68A)
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Please SCREENSHOT this proof and tap the WhatsApp button below (085755735676) so Superadmin can activate your account."
                                        } else {
                                            "Silakan SCREENSHOT halaman bukti ini lalu tekan tombol WhatsApp di bawah (085755735676) agar akun Admin Sekolah Rakyat segera diaktifkan."
                                        },
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        color = if (isLight) Color(0xFF78350F) else Color(0xFFFCD34D).copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { sendToSuperadminWhatsApp(reg) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_send_wa_superadmin"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "Send to Superadmin WA (085755735676)" else "Kirim ke WA Superadmin (085755735676)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (isEn) "Close & Back to Login" else "Tutup & Kembali ke Login",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // PANDUAN SINGKAT 3 LANGKAH AGAR SANGAT JELAS
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isLight) Color(0xFFE6F4F7) else Color(0xFF11262D),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFBAE6FD) else Color(0xFF0EA5E9).copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = oceanPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Quick Registration Steps" else "Alur Pendaftaran Sekolah Rakyat",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) Color(0xFF0C4A6E) else Color(0xFFBAE6FD)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    RegistrationStepChip(
                                        stepNumber = "1",
                                        label = if (isEn) "Fill Form" else "Isi Formulir",
                                        isLight = isLight,
                                        modifier = Modifier.weight(1f)
                                    )
                                    RegistrationStepChip(
                                        stepNumber = "2",
                                        label = if (isEn) "Submit Data" else "Kirim Data",
                                        isLight = isLight,
                                        modifier = Modifier.weight(1f)
                                    )
                                    RegistrationStepChip(
                                        stepNumber = "3",
                                        label = if (isEn) "WA Verify" else "Verifikasi WA",
                                        isLight = isLight,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // KARTU 1: IDENTITAS SEKOLAH RAKYAT
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = schoolCardBg,
                            border = BorderStroke(1.dp, schoolCardBorder),
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isLight) Color(0xFFCCFBF1) else Color(0xFF134E4A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = tealAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isEn) "1. Sekolah Rakyat Identity" else "1. Identitas Sekolah Rakyat",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) Color(0xFF0F766E) else Color(0xFF5EEAD4)
                                        )
                                        Text(
                                            text = if (isEn) {
                                                "Official unit name, city, Wali Asuh estimate & location"
                                            } else {
                                                "Isi nama unit resmi, lokasi kota/kabupaten & alamat lengkap"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                CompactRegTextField(
                                    value = schoolName,
                                    onValueChange = {
                                        schoolName = it
                                        errorMessage = null
                                    },
                                    label = if (isEn) "Sekolah Rakyat Name *" else "Nama Sekolah Rakyat *",
                                    placeholder = if (isEn) {
                                        "e.g. Sekolah Rakyat SRT 1 Kabupaten Kediri"
                                    } else {
                                        "Contoh: Sekolah Rakyat SRT 1 Kabupaten Kediri"
                                    },
                                    leadingIcon = Icons.Default.School,
                                    iconTint = Color(0xFF0EA5E9),
                                    accentBorderColor = tealAccent,
                                    isLight = isLight,
                                    testTag = "reg_school_name"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CompactRegTextField(
                                        value = city,
                                        onValueChange = {
                                            city = it
                                            errorMessage = null
                                        },
                                        label = if (isEn) "City / Regency *" else "Kota / Kab. *",
                                        placeholder = if (isEn) "e.g. Kediri" else "Contoh: Kediri",
                                        leadingIcon = Icons.Default.LocationOn,
                                        iconTint = Color(0xFFF43F5E),
                                        accentBorderColor = tealAccent,
                                        isLight = isLight,
                                        modifier = Modifier.weight(1.25f),
                                        testTag = "reg_city"
                                    )
                                    CompactRegTextField(
                                        value = estimatedWaliText,
                                        onValueChange = {
                                            estimatedWaliText = it.filter { ch -> ch.isDigit() }
                                            errorMessage = null
                                        },
                                        label = if (isEn) "Wali Asuh *" else "Jml. Wali *",
                                        placeholder = "6",
                                        leadingIcon = Icons.Default.Group,
                                        iconTint = Color(0xFF14B8A6),
                                        accentBorderColor = tealAccent,
                                        isLight = isLight,
                                        keyboardType = KeyboardType.Number,
                                        modifier = Modifier.weight(0.85f),
                                        testTag = "reg_wali_count"
                                    )
                                }

                                CompactRegTextField(
                                    value = address,
                                    onValueChange = {
                                        address = it
                                        errorMessage = null
                                    },
                                    label = if (isEn) "Full Sekolah Rakyat Address *" else "Alamat Lengkap Sekolah Rakyat *",
                                    placeholder = if (isEn) {
                                        "e.g. Jl. Merdeka No. 12, Kec. Plosoklaten"
                                    } else {
                                        "Contoh: Jl. Merdeka No. 12, Kec. Plosoklaten"
                                    },
                                    leadingIcon = Icons.Default.HomeWork,
                                    iconTint = Color(0xFF10B981),
                                    accentBorderColor = tealAccent,
                                    isLight = isLight,
                                    singleLine = false,
                                    minLines = 2,
                                    maxLines = 3,
                                    testTag = "reg_address"
                                )
                            }
                        }

                        // KARTU 2: PENANGGUNG JAWAB (ADMIN SEKOLAH)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = picCardBg,
                            border = BorderStroke(1.dp, picCardBorder),
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isLight) Color(0xFFEDE9FE) else Color(0xFF312E81)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = indigoAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isEn) "2. Person in Charge (School Admin)" else "2. Penanggung Jawab (Admin Sekolah)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) Color(0xFF4338CA) else Color(0xFFC7D2FE)
                                        )
                                        Text(
                                            text = if (isEn) {
                                                "Admin login credentials will be sent to this contact"
                                            } else {
                                                "Akun admin sekolah akan dikirimkan ke kontak aktif di bawah"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                CompactRegTextField(
                                    value = picName,
                                    onValueChange = {
                                        picName = it
                                        errorMessage = null
                                    },
                                    label = if (isEn) "Full PIC Name *" else "Nama Penanggung Jawab *",
                                    placeholder = if (isEn) "e.g. Budi Santoso, S.Pd" else "Contoh: Budi Santoso, S.Pd",
                                    leadingIcon = Icons.Default.Person,
                                    iconTint = Color(0xFF8B5CF6),
                                    accentBorderColor = indigoAccent,
                                    isLight = isLight,
                                    testTag = "reg_pic_name"
                                )

                                CompactRegTextField(
                                    value = picPhone,
                                    onValueChange = {
                                        picPhone = it
                                        errorMessage = null
                                    },
                                    label = if (isEn) "Active Tel / WhatsApp *" else "No. Telp / WhatsApp Aktif *",
                                    placeholder = "Contoh: 081234567890",
                                    leadingIcon = Icons.Default.Phone,
                                    iconTint = Color(0xFF16A34A),
                                    accentBorderColor = indigoAccent,
                                    isLight = isLight,
                                    keyboardType = KeyboardType.Phone,
                                    testTag = "reg_pic_phone"
                                )

                                CompactRegTextField(
                                    value = picEmail,
                                    onValueChange = {
                                        picEmail = it
                                        errorMessage = null
                                    },
                                    label = if (isEn) "PIC Email (Optional)" else "Email Penanggung Jawab (Opsional)",
                                    placeholder = "Contoh: admin.sr1@sekolah.id",
                                    leadingIcon = Icons.Default.Email,
                                    iconTint = Color(0xFFEC4899),
                                    accentBorderColor = indigoAccent,
                                    isLight = isLight,
                                    keyboardType = KeyboardType.Email,
                                    testTag = "reg_pic_email"
                                )
                            }
                        }

                        // CATATAN PENGINGAT & KONFIRMASI WA
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isLight) Color(0xFFFFFBEB) else Color(0xFF282115),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFFDE68A) else Color(0xFFD97706).copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = if (isLight) Color(0xFFB45309) else Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isEn) {
                                        "Fields marked (*) are required. After submitting, screenshot the proof page & send it to WA 085755735676 for fast approval."
                                    } else {
                                        "Kolom bertanda (*) wajib diisi. Setelah menekan Kirim Pengajuan, silakan screenshot halaman bukti & kirim ke WA 085755735676 agar cepat disetujui."
                                    },
                                    fontSize = 11.5.sp,
                                    color = if (isLight) Color(0xFF92400E) else Color(0xFFFDE68A),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isLight) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.outline
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                modifier = Modifier
                                    .weight(0.85f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = if (isEn) "Cancel" else "Batal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    if (schoolName.isBlank() || city.isBlank() || address.isBlank() || picName.isBlank() || picPhone.isBlank()) {
                                        errorMessage = if (isEn) {
                                            "Please complete all required fields marked with (*)"
                                        } else {
                                            "Mohon lengkapi seluruh kolom bertanda bintang (*)"
                                        }
                                        return@Button
                                    }
                                    val count = (estimatedWaliText.toIntOrNull() ?: 1).coerceAtLeast(1)
                                    onSubmit(
                                        schoolName.trim(),
                                        address.trim(),
                                        city.trim(),
                                        count,
                                        picName.trim(),
                                        picEmail.trim(),
                                        picPhone.trim()
                                    ) { saved ->
                                        submittedRegistration = saved
                                        Toast.makeText(
                                            context,
                                            if (isEn) {
                                                "Sekolah Rakyat registration sent to Superadmin queue!"
                                            } else {
                                                "Pengajuan Sekolah Rakyat berhasil dikirim ke antrean Superadmin!"
                                            },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                modifier = Modifier
                                    .weight(1.45f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_submit_registration"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0A6C82),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = if (isEn) "Submit Registration" else "Kirim Pengajuan",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationStepChip(
    stepNumber: String,
    label: String,
    isLight: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isLight) Color.White else Color(0xFF17323B),
        border = BorderStroke(
            0.8.dp,
            if (isLight) Color(0xFFBAE6FD) else Color(0xFF38BDF8).copy(alpha = 0.3f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color(0xFF0A6C82) else Color(0xFF38BDF8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isLight) Color.White else Color(0xFF082F49)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompactRegTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    iconTint: Color,
    accentBorderColor: Color,
    isLight: Boolean,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconTint.copy(alpha = if (isLight) 0.13f else 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        textStyle = TextStyle(
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentBorderColor,
            unfocusedBorderColor = if (isLight) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedLabelColor = accentBorderColor,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = if (isLight) Color(0xFFFAFFFE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = if (isLight) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
