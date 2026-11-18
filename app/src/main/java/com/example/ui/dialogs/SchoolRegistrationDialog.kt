package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolRegistration
import com.example.ui.theme.WaliPrimary
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

    // Sweet pastel accents harmonizing with WaliPrimary
    val indigoAccent = Color(0xFF4F46E5)
    val tealAccent = Color(0xFF0D9488)
    val roseAccent = Color(0xFFDB2777)
    val sheetBg = Color(0xFFF7F8FC)

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
            *NEW SCHOOL REGISTRATION REQUEST - WALIASUH CARE*
            --------------------------------------------------
            🏫 *School/Foundation Name:* $sName
            📍 *Address:* $sAddr
            🏙️ *City/Regency:* $sCity
            👥 *Estimated Wali Asuh Count:* $sWali people
            
            👤 *Person in Charge (PIC):* $sPic
            ✉️ *Email:* $sEmail
            📱 *WhatsApp No.:* $sPhone
            📅 *Date:* $formattedDate
            --------------------------------------------------
            Hello Superadmin Wali Asuh Care, I have completed the school registration form above and attached a screenshot of the registration proof. Please verify and issue our school admin access account. Thank you!
            """.trimIndent()
        } else {
            """
            *PENGAJUAN PENDAFTARAN SEKOLAH BARU - WALIASUH CARE*
            --------------------------------------------------
            🏫 *Nama Sekolah/Yayasan:* $sName
            📍 *Alamat:* $sAddr
            🏙️ *Kota/Kabupaten:* $sCity
            👥 *Estimasi Jumlah Wali Asuh:* $sWali orang
            
            👤 *Nama Penanggung Jawab:* $sPic
            ✉️ *Email:* $sEmail
            📱 *No. WhatsApp:* $sPhone
            📅 *Tanggal:* $formattedDate
            --------------------------------------------------
            Halo Superadmin Wali Asuh Care, saya telah melengkapi formulir pendaftaran sekolah di atas dan melampirkan screenshot bukti pendaftaran. Mohon untuk diverifikasi dan diterbitkan akun akses admin sekolah kami. Terima kasih!
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
                    if (isEn) "School Registration" else "Pendaftaran Sekolah",
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
        dragHandle = { BottomSheetDefaults.DragHandle() },
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
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Gradien Pastel Manis
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (submittedRegistration == null) Color(0xFFC7D2FE) else Color(0xFF99F6E4)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    if (submittedRegistration == null) {
                                        listOf(Color(0xFFEEF2FF), Color(0xFFFDF2F8), Color(0xFFECFDF5))
                                    } else {
                                        listOf(Color(0xFFECFDF5), Color(0xFFF0FDFA))
                                    }
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    Brush.linearGradient(
                                        if (submittedRegistration == null) {
                                            listOf(indigoAccent, roseAccent)
                                        } else {
                                            listOf(tealAccent, Color(0xFF059669))
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (submittedRegistration == null) Icons.Default.School else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (submittedRegistration == null) {
                                    if (isEn) "New School Registration" else "Pendaftaran Sekolah Baru"
                                } else {
                                    if (isEn) "Registration Submitted!" else "Pengajuan Berhasil Dikirim!"
                                },
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1B4B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (submittedRegistration == null) {
                                    if (isEn) "Register your school or boarding foundation neatly" else "Lengkapi profil sekolah & kontak penanggung jawab"
                                } else {
                                    if (isEn) "Screenshot & send via WhatsApp for verification" else "Kirim bukti ke WA Superadmin untuk verifikasi akun"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_registration")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isEn) "Close" else "Tutup",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (submittedRegistration != null) {
                    // TAMPILAN BUKTI TIKET SETELAH DIKIRIM
                    val reg = submittedRegistration!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.2.dp, Color(0xFF6EE7B7), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFD1FAE5)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isEn) "REGISTRATION PROOF" else "BUKTI PENDAFTARAN",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = if (isEn) "PENDING" else "MENUNGGU",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = reg.schoolName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${reg.city} • ${reg.address}",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFF475569)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isEn) "PIC: ${reg.picName}" else "PJ: ${reg.picName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = reg.picPhone,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE0E7FF)
                                ) {
                                    Text(
                                        text = if (isEn) "${reg.estimatedWaliCount} Wali" else "${reg.estimatedWaliCount} Wali",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3730A3),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // KOTAK INSTRUKSI SCREENSHOT & WA
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (isEn) {
                                    "Please SCREENSHOT this page and send it to WhatsApp 085755735676 for Superadmin approval."
                                } else {
                                    "Silakan SCREENSHOT halaman ini dan kirim ke WhatsApp 085755735676 untuk verifikasi Superadmin."
                                },
                                fontSize = 11.5.sp,
                                lineHeight = 15.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF78350F)
                            )
                        }
                    }

                    Button(
                        onClick = { sendToSuperadminWhatsApp(reg) },
                        shape = RoundedCornerShape(12.dp),
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
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = if (isEn) "Close & Back to Login" else "Tutup & Kembali ke Login",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // KARTU 1: IDENTITAS SEKOLAH (Nuansa Mint & Sky Pastel yang Manis & Rapat)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCCFBF1)),
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFCCFBF1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HomeWork,
                                        contentDescription = null,
                                        tint = tealAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = if (isEn) "1. School / Foundation Identity" else "1. Identitas Sekolah / Panti",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }

                            CompactRegTextField(
                                value = schoolName,
                                onValueChange = {
                                    schoolName = it
                                    errorMessage = null
                                },
                                label = if (isEn) "School / Foundation *" else "Nama Sekolah / Yayasan *",
                                placeholder = if (isEn) "e.g. SMP IT Nurul Fikri" else "Contoh: SMP IT Nurul Fikri",
                                leadingIcon = Icons.Default.School,
                                iconTint = Color(0xFF0EA5E9),
                                accentBorderColor = tealAccent,
                                testTag = "reg_school_name"
                            )

                            // Kota & Jumlah Wali sejajar 1 baris agar sangat rapat & rapi
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
                                    label = if (isEn) "City / Reg. *" else "Kota / Kab. *",
                                    placeholder = if (isEn) "e.g. Kediri" else "Contoh: Kediri",
                                    leadingIcon = Icons.Default.LocationOn,
                                    iconTint = Color(0xFFF43F5E),
                                    accentBorderColor = tealAccent,
                                    modifier = Modifier.weight(1.25f),
                                    testTag = "reg_city"
                                )
                                CompactRegTextField(
                                    value = estimatedWaliText,
                                    onValueChange = {
                                        estimatedWaliText = it.filter { ch -> ch.isDigit() }
                                        errorMessage = null
                                    },
                                    label = if (isEn) "Wali Est. *" else "Jml. Wali *",
                                    placeholder = "6",
                                    leadingIcon = Icons.Default.Group,
                                    iconTint = Color(0xFF14B8A6),
                                    accentBorderColor = tealAccent,
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
                                label = if (isEn) "Full Address *" else "Alamat Lengkap Sekolah *",
                                placeholder = if (isEn) "Jl. Merdeka No. 12, Wates" else "Jl. Merdeka No. 12, Kec. Wates",
                                leadingIcon = Icons.Default.HomeWork,
                                iconTint = Color(0xFF10B981),
                                accentBorderColor = tealAccent,
                                singleLine = false,
                                minLines = 1,
                                maxLines = 2,
                                testTag = "reg_address"
                            )
                        }
                    }

                    // KARTU 2: PENANGGUNG JAWAB (Nuansa Lavender & Rose Pastel)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E7FF)),
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEDE9FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = indigoAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = if (isEn) "2. Person in Charge (PIC)" else "2. Penanggung Jawab (PJ)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4338CA)
                                )
                            }

                            CompactRegTextField(
                                value = picName,
                                onValueChange = {
                                    picName = it
                                    errorMessage = null
                                },
                                label = if (isEn) "Full PIC Name *" else "Nama Penanggung Jawab *",
                                placeholder = if (isEn) "Ustadz Ahmad Fauzi, S.Pd" else "Ustadz Ahmad Fauzi, S.Pd",
                                leadingIcon = Icons.Default.Person,
                                iconTint = Color(0xFF8B5CF6),
                                accentBorderColor = indigoAccent,
                                testTag = "reg_pic_name"
                            )

                            CompactRegTextField(
                                value = picEmail,
                                onValueChange = {
                                    picEmail = it
                                    errorMessage = null
                                },
                                label = if (isEn) "PIC Email *" else "Email Penanggung Jawab *",
                                placeholder = "ahmad.fauzi@gmail.com",
                                leadingIcon = Icons.Default.Email,
                                iconTint = Color(0xFFEC4899),
                                accentBorderColor = indigoAccent,
                                keyboardType = KeyboardType.Email,
                                testTag = "reg_pic_email"
                            )

                            CompactRegTextField(
                                value = picPhone,
                                onValueChange = {
                                    picPhone = it
                                    errorMessage = null
                                },
                                label = if (isEn) "Active Tel / WhatsApp *" else "No. Telp / WhatsApp Aktif *",
                                placeholder = "081234567890",
                                leadingIcon = Icons.Default.Phone,
                                iconTint = Color(0xFF16A34A),
                                accentBorderColor = indigoAccent,
                                keyboardType = KeyboardType.Phone,
                                testTag = "reg_pic_phone"
                            )
                        }
                    }

                    // Tip Screenshot Ringkas & Manis (Peach/Amber Pastel)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "After submitting, screenshot & send to WA 085755735676 for fast approval."
                                } else {
                                    "Setelah mengisi, screenshot halaman bukti & kirim ke WA 085755735676 agar cepat disetujui."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 14.5.sp
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (isEn) "Cancel" else "Batal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
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
                                val count = estimatedWaliText.toIntOrNull() ?: 1
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
                                        if (isEn) "Registration request sent to Superadmin queue!" else "Pengajuan berhasil dikirim ke antrean Superadmin!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_submit_registration"),
                            colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary)
                        ) {
                            Text(
                                text = if (isEn) "Submit Request" else "Kirim Pengajuan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
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
                fontSize = 11.5.sp,
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
                color = Color(0xFF94A3B8)
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
            }
        },
        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentBorderColor,
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedLabelColor = accentBorderColor,
            unfocusedLabelColor = Color(0xFF64748B),
            focusedContainerColor = Color(0xFFFAFBFF),
            unfocusedContainerColor = Color(0xFFF8FAFC)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
