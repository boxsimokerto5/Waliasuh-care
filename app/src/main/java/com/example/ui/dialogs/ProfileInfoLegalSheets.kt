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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.theme.WaliPrimary

enum class ProfileLegalSheetType {
    ABOUT,
    PRIVACY_POLICY,
    TERMS_OF_SERVICE,
    CONTACT_US
}

private const val SUPPORT_EMAIL = "eccko.w4@gmail.com"
private const val SUPPORT_PHONE_LOCAL = "085755735676"
private const val SUPPORT_PHONE_INTL = "6285755735676"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileLegalBottomSheet(
    type: ProfileLegalSheetType,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (headerTitle, headerIcon, headerAccent) = when (type) {
        ProfileLegalSheetType.ABOUT -> Triple(
            "Tentang Aplikasi",
            Icons.Default.Info,
            WaliPrimary
        )
        ProfileLegalSheetType.PRIVACY_POLICY -> Triple(
            "Kebijakan Privasi (Privacy Policy)",
            Icons.Default.PrivacyTip,
            Color(0xFF0F766E)
        )
        ProfileLegalSheetType.TERMS_OF_SERVICE -> Triple(
            "Syarat & Ketentuan (Terms of Service)",
            Icons.Default.Gavel,
            Color(0xFF7C3AED)
        )
        ProfileLegalSheetType.CONTACT_US -> Triple(
            "Hubungi Kami (Contact Us)",
            Icons.Default.SupportAgent,
            Color(0xFF16A34A)
        )
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
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("profile_legal_sheet_${type.name.lowercase()}"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(headerAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = headerAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = headerTitle,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_legal_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup"
                        )
                    }
                }

                HorizontalDivider()

                when (type) {
                    ProfileLegalSheetType.ABOUT -> AboutContent()
                    ProfileLegalSheetType.PRIVACY_POLICY -> PrivacyPolicyContent()
                    ProfileLegalSheetType.TERMS_OF_SERVICE -> TermsOfServiceContent()
                    ProfileLegalSheetType.CONTACT_US -> ContactUsContent(context = context)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = headerAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_dismiss_legal_sheet")
                ) {
                    Text(
                        text = "Tutup",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = WaliPrimary.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, WaliPrimary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_sr_care),
                    contentDescription = "Logo Wali Asuh Care",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Wali Asuh Care",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Versi ${BuildConfig.VERSION_NAME} • Platform Multi-Sekolah",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WaliPrimary
                    )
                    Text(
                        text = "Dokumentasi Kesehatan, Haid & Pembinaan Puasa Asrama",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LegalSectionCard(
            title = "Fungsi Utama Aplikasi",
            body = "Wali Asuh Care membantu Wali Asuh dan Admin Sekolah dalam mencatat rencana berobat siswa, rujukan faskes, rekam medis asrama, pemantauan siklus haid & bersuci siswi, checklist sahur/berbuka puasa, pesan operan shift, serta pembuatan laporan PDF."
        )

        LegalSectionCard(
            title = "Status Independensi & Disclaimer",
            body = stringResource(R.string.disclaimer_full),
            accent = WaliPrimary
        )

        LegalSectionCard(
            title = "Kontak Pengembang",
            body = "Email: $SUPPORT_EMAIL\nWhatsApp / HP: $SUPPORT_PHONE_LOCAL"
        )
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Terakhir diperbarui: November 2026",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LegalSectionCard(
            title = "1. Data yang Dikelola",
            body = "Aplikasi menyimpan data operasional yang diinput langsung oleh Administrator Sekolah dan Wali Asuh berwenang, meliputi:\n" +
                "• Identitas siswa (nama, NIS/NIK, kelas, asrama, kontak darurat).\n" +
                "• Catatan layanan kesehatan, diagnosa rujukan faskes, pendataan haid, dan aktivitas puasa.\n" +
                "• Akun pengelola (nama, username, nomor telepon wali asuh, dan profil sekolah)."
        )

        LegalSectionCard(
            title = "2. Tujuan Penggunaan & Penyimpanan Data",
            body = "• Data disimpan secara lokal di perangkat (Room) dan disinkronkan melalui koneksi terenkripsi (HTTPS) ke database cloud multi-tenant (Supabase) yang terisolasi per kode sekolah (school_id).\n" +
                "• Data semata-mata digunakan untuk mendukung dokumentasi perawatan, pelaporan wali asuh, dan administrasi internal sekolah terdaftar."
        )

        LegalSectionCard(
            title = "3. Izin Perangkat (Kamera & Penyimpanan)",
            body = "• Izin Kamera hanya dipakai untuk memindai barcode/QR pada kartu siswa secara lokal di perangkat. Aplikasi tidak mengunggah foto atau rekaman video kamera ke server.\n" +
                "• Akses dokumen hanya digunakan saat Anda memilih file CSV untuk impor atau menyimpan/membagikan laporan PDF secara manual."
        )

        LegalSectionCard(
            title = "4. Layanan Pihak Ketiga & Iklan",
            body = "• Aplikasi menggunakan layanan iklan (TopOn) untuk menampilkan iklan interstitial dan iklan video berhadiah opsional sebelum membuka/mengunduh Laporan Bulanan atau Laporan PDF Puasa/Kesehatan.\n" +
                "• Data identitas maupun catatan kesehatan siswa TIDAK pernah dijual, disewakan, atau dikirimkan ke jaringan pengiklan."
        )

        LegalSectionCard(
            title = "5. Retensi & Penghapusan Akun / Data",
            body = "• Wali Asuh dapat menghapus akun pribadinya secara mandiri kapan saja langsung di dalam aplikasi melalui menu Profil -> Hapus Akun Saya.\n" +
                "• Administrator Sekolah dapat mengedit/menghapus data siswa & wali asuh, atau menghapus akun Admin beserta seluruh data sekolah secara permanen melalui Portal Admin -> Hapus Akun Admin & Data Sekolah Permanen.\n" +
                "• Untuk permintaan penghapusan data manual atau pertanyaan privasi, hubungi kami di $SUPPORT_EMAIL."
        )
    }
}

@Composable
private fun TermsOfServiceContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Berlaku untuk seluruh pengguna (Admin Sekolah & Wali Asuh)",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LegalSectionCard(
            title = "1. Kelayakan & Otorisasi Pengguna",
            body = "Aplikasi Wali Asuh Care ditujukan bagi pengelola sekolah, admin asrama, dan wali asuh dewasa (18+) yang telah diberi otorisasi resmi oleh sekolah masing-masing untuk mengelola data operasional asrama."
        )

        LegalSectionCard(
            title = "2. Disclaimer Medis (Penting)",
            body = "Wali Asuh Care adalah alat bantu dokumentasi dan administrasi harian asrama. Aplikasi ini BUKAN alat diagnosa medis, bukan perangkat kesehatan klinis, dan tidak menggantikan saran, pemeriksaan, atau keputusan dokter maupun fasilitas kesehatan resmi."
        )

        LegalSectionCard(
            title = "3. Keamanan Akun & Kerahasiaan Data",
            body = "Pengelola sekolah dan wali asuh bertanggung jawab penuh untuk menjaga kerahasiaan Kode Sekolah, Username, dan Kata Sandi masing-masing, serta memastikan data pribadi siswa tidak disalahgunakan di luar kepentingan pengasuhan."
        )

        LegalSectionCard(
            title = "4. Penggunaan Fitur & Iklan",
            body = "Fitur inti pencatatan harian tetap dapat digunakan tanpa biaya langganan. Beberapa fitur ekspor/laporan tertentu dapat menampilkan iklan video berhadiah dengan pemberitahuan jelas sebelum diputar. Dilarang melakukan modifikasi aplikasi, scraping, atau akses tidak sah ke database server."
        )

        LegalSectionCard(
            title = "5. Perubahan Layanan",
            body = "Pengembang berhak memperbarui fitur, skema sinkronisasi, serta syarat & ketentuan sewaktu-waktu demi keamanan dan peningkatan layanan. Penggunaan aplikasi secara berkelanjutan berarti menyetujui ketentuan terbaru."
        )
    }
}

@Composable
private fun ContactUsContent(context: Context) {
    fun copyText(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(context, "$label disalin: $value", Toast.LENGTH_SHORT).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Butuh bantuan pendaftaran sekolah, kendala sinkronisasi Supabase, laporan bug, atau permintaan penghapusan data? Hubungi tim pengembang melalui kanal resmi berikut:",
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )

        // Kartu Email Resmi
        ContactChannelCard(
            title = "Email Dukungan Resmi",
            value = SUPPORT_EMAIL,
            subtitle = "Cocok untuk pengajuan resmi, laporan bug, & permintaan privasi data",
            icon = Icons.Default.Email,
            accentColor = WaliPrimary,
            primaryActionLabel = "Kirim Email",
            primaryActionIcon = Icons.Default.Email,
            onPrimaryAction = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$SUPPORT_EMAIL")
                    putExtra(Intent.EXTRA_SUBJECT, "Dukungan Aplikasi Wali Asuh Care")
                }
                runCatching {
                    context.startActivity(Intent.createChooser(intent, "Kirim Email ke Pengembang"))
                }.onFailure {
                    copyText("Email", SUPPORT_EMAIL)
                }
            },
            onCopy = { copyText("Email", SUPPORT_EMAIL) },
            testTagPrefix = "contact_email"
        )

        // Kartu WhatsApp / Nomor HP Resmi
        ContactChannelCard(
            title = "WhatsApp / Telepon Pengembang",
            value = SUPPORT_PHONE_LOCAL,
            subtitle = "Respon cepat untuk bantuan teknis admin & koordinasi multi-sekolah",
            icon = Icons.Default.Phone,
            accentColor = Color(0xFF16A34A),
            primaryActionLabel = "Chat WhatsApp",
            primaryActionIcon = Icons.Default.Send,
            onPrimaryAction = {
                val msg = "Halo Admin/Pengembang Wali Asuh Care, saya ingin berkonsultasi mengenai aplikasi."
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$SUPPORT_PHONE_INTL&text=${Uri.encode(msg)}")
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }.onFailure {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE_LOCAL"))
                    runCatching { context.startActivity(dialIntent) }
                        .onFailure { copyText("Nomor HP", SUPPORT_PHONE_LOCAL) }
                }
            },
            secondaryActionLabel = "Telepon",
            onSecondaryAction = {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE_LOCAL"))
                runCatching {
                    context.startActivity(dialIntent)
                }.onFailure {
                    copyText("Nomor HP", SUPPORT_PHONE_LOCAL)
                }
            },
            onCopy = { copyText("Nomor HP", SUPPORT_PHONE_LOCAL) },
            testTagPrefix = "contact_phone"
        )
    }
}

@Composable
private fun LegalSectionCard(
    title: String,
    body: String,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = body,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ContactChannelCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    primaryActionLabel: String,
    primaryActionIcon: ImageVector,
    onPrimaryAction: () -> Unit,
    onCopy: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    testTagPrefix: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("${testTagPrefix}_copy")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin $title",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPrimaryAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("${testTagPrefix}_primary")
                ) {
                    Icon(
                        imageVector = primaryActionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = primaryActionLabel,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .heightIn(min = 48.dp)
                            .testTag("${testTagPrefix}_secondary")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = secondaryActionLabel,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
