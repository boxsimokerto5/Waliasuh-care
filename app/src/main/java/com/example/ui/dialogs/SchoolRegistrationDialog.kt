package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolRegistration
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils

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

    fun sendToSuperadminWhatsApp(reg: SchoolRegistration? = null) {
        val sName = reg?.schoolName ?: schoolName.trim()
        val sCity = reg?.city ?: city.trim()
        val sAddr = reg?.address ?: address.trim()
        val sWali = reg?.estimatedWaliCount ?: (estimatedWaliText.toIntOrNull() ?: 6)
        val sPic = reg?.picName ?: picName.trim()
        val sEmail = reg?.picEmail ?: picEmail.trim()
        val sPhone = reg?.picPhone ?: picPhone.trim()

        val text = """
            *PENGAJUAN PENDAFTARAN SEKOLAH BARU - WALIASUH CARE*
            --------------------------------------------------
            🏫 *Nama Sekolah/Yayasan:* $sName
            📍 *Alamat:* $sAddr
            🏙️ *Kota/Kabupaten:* $sCity
            👥 *Estimasi Jumlah Wali Asuh:* $sWali orang
            
            👤 *Nama Penanggung Jawab:* $sPic
            ✉️ *Email:* $sEmail
            📱 *No. WhatsApp:* $sPhone
            📅 *Tanggal:* ${DateUtils.formatFullDate(System.currentTimeMillis())}
            --------------------------------------------------
            Halo Superadmin Wali Asuh Care, saya telah melengkapi formulir pendaftaran sekolah di atas dan melampirkan screenshot bukti pendaftaran. Mohon untuk diverifikasi dan diterbitkan akun akses admin sekolah kami. Terima kasih!
        """.trimIndent()

        val superadminWaNumber = "6285755735676"
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$superadminWaNumber&text=${Uri.encode(text)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Pendaftaran Sekolah", text))
            Toast.makeText(context, "Format pesan telah disalin. Silakan kirim ke WA 085755735676", Toast.LENGTH_LONG).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (submittedRegistration == null) "Pendaftaran Sekolah Baru" else "Pengajuan Berhasil Dikirim!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (submittedRegistration == null)
                                "Ajukan sekolah atau yayasan asrama Anda untuk bergabung"
                            else
                                "Ikuti petunjuk di bawah untuk verifikasi Superadmin",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (submittedRegistration != null) {
                    // TAMPILAN BUKTI TIKET SETELAH DIKIRIM (TAMPILAN SCREENSHOT RESMI)
                    val reg = submittedRegistration!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, WaliPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FORMULIR BUKTI PENDAFTARAN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = WaliPrimary
                                )
                            }

                            Text(
                                text = "Sekolah: ${reg.schoolName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kota/Kabupaten: ${reg.city}\nAlamat: ${reg.address}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "PJ: ${reg.picName} (${reg.picPhone})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Estimasi Wali Asuh: ${reg.estimatedWaliCount} Orang",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Status: MENUNGGU PERSETUJUAN SUPERADMIN",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }

                    // KOTAK INSTRUKSI SCREENSHOT & WA WAJIB SESUAI REQUEST USER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "INSTRUKSI PENTING",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Text(
                                text = "Silakan SCREENSHOT halaman ini, dan kirim ke nomor WhatsApp 085755735676 untuk diproses dan disetujui oleh Superadmin.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF78350F)
                            )
                        }
                    }

                    // Tombol Kirim ke WhatsApp Otomatis
                    Button(
                        onClick = { sendToSuperadminWhatsApp(reg) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_send_wa_superadmin"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim ke WA Superadmin (085755735676)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Tutup & Kembali ke Login")
                    }
                } else {
                    // FORMULIR PENGISIAN
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = {
                            schoolName = it
                            errorMessage = null
                        },
                        label = { Text("Nama Sekolah / Yayasan Lengkap *") },
                        placeholder = { Text("Contoh: SMP IT Nurul Fikri") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = WaliPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_school_name")
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = {
                            city = it
                            errorMessage = null
                        },
                        label = { Text("Kota / Kabupaten *") },
                        placeholder = { Text("Contoh: Kabupaten Kediri / Kota Malang") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = WaliPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_city")
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            errorMessage = null
                        },
                        label = { Text("Alamat Lengkap Sekolah / Panti *") },
                        placeholder = { Text("Contoh: Jl. Merdeka No. 12, Kec. Wates") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_address")
                    )

                    OutlinedTextField(
                        value = estimatedWaliText,
                        onValueChange = {
                            estimatedWaliText = it.filter { ch -> ch.isDigit() }
                            errorMessage = null
                        },
                        label = { Text("Jumlah Wali Asuh (Estimasi) *") },
                        placeholder = { Text("Contoh: 6") },
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = WaliPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_wali_count")
                    )

                    OutlinedTextField(
                        value = picName,
                        onValueChange = {
                            picName = it
                            errorMessage = null
                        },
                        label = { Text("Nama Penanggung Jawab (PJ) *") },
                        placeholder = { Text("Contoh: Ustadz Ahmad Fauzi, S.Pd") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WaliPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pic_name")
                    )

                    OutlinedTextField(
                        value = picEmail,
                        onValueChange = {
                            picEmail = it
                            errorMessage = null
                        },
                        label = { Text("Email Penanggung Jawab *") },
                        placeholder = { Text("Contoh: ahmad.fauzi@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WaliPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pic_email")
                    )

                    OutlinedTextField(
                        value = picPhone,
                        onValueChange = {
                            picPhone = it
                            errorMessage = null
                        },
                        label = { Text("Nomor Telepon / WhatsApp Aktif *") },
                        placeholder = { Text("Contoh: 081234567890") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WaliPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pic_phone")
                    )

                    // Petunjuk Screenshot & WA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Setelah mengisi formulir, silakan screenshot halaman ini dan kirim ke nomor WhatsApp 085755735676 untuk percepatan persetujuan.",
                                fontSize = 12.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 17.sp
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                if (schoolName.isBlank() || city.isBlank() || address.isBlank() || picName.isBlank() || picPhone.isBlank()) {
                                    errorMessage = "Mohon lengkapi seluruh kolom bertanda bintang (*)"
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
                                    Toast.makeText(context, "Pengajuan berhasil dikirim ke antrean Superadmin!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("btn_submit_registration"),
                            colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary)
                        ) {
                            Text("Kirim Pengajuan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
