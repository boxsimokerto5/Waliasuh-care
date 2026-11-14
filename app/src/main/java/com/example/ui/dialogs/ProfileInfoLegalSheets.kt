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
import com.example.util.i18n.LocalAppStrings

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
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (headerTitle, headerIcon, headerAccent) = when (type) {
        ProfileLegalSheetType.ABOUT -> Triple(
            if (isEn) "About App" else "Tentang Aplikasi",
            Icons.Default.Info,
            WaliPrimary
        )
        ProfileLegalSheetType.PRIVACY_POLICY -> Triple(
            if (isEn) "Privacy Policy" else "Kebijakan Privasi (Privacy Policy)",
            Icons.Default.PrivacyTip,
            Color(0xFF0F766E)
        )
        ProfileLegalSheetType.TERMS_OF_SERVICE -> Triple(
            if (isEn) "Terms of Service" else "Syarat & Ketentuan (Terms of Service)",
            Icons.Default.Gavel,
            Color(0xFF7C3AED)
        )
        ProfileLegalSheetType.CONTACT_US -> Triple(
            if (isEn) "Contact Us" else "Hubungi Kami (Contact Us)",
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
                            contentDescription = s.close
                        )
                    }
                }

                HorizontalDivider()

                when (type) {
                    ProfileLegalSheetType.ABOUT -> AboutContent(isEn = isEn)
                    ProfileLegalSheetType.PRIVACY_POLICY -> PrivacyPolicyContent(isEn = isEn)
                    ProfileLegalSheetType.TERMS_OF_SERVICE -> TermsOfServiceContent(isEn = isEn)
                    ProfileLegalSheetType.CONTACT_US -> ContactUsContent(context = context, isEn = isEn)
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
                        text = s.close,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutContent(isEn: Boolean) {
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
                        text = if (isEn) {
                            "Version ${BuildConfig.VERSION_NAME} • Multi-School Platform"
                        } else {
                            "Versi ${BuildConfig.VERSION_NAME} • Platform Multi-Sekolah"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WaliPrimary
                    )
                    Text(
                        text = if (isEn) {
                            "Health Documentation, Haid & Asrama Puasa Guidance"
                        } else {
                            "Dokumentasi Kesehatan, Haid & Pembinaan Puasa Asrama"
                        },
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LegalSectionCard(
            title = if (isEn) "Main App Functions" else "Fungsi Utama Aplikasi",
            body = if (isEn) {
                "Wali Asuh Care helps Wali Asuh and School Admins record student medical plans, Faskes referrals, Asrama medical records, Haid & Suci cycle monitoring, Sahur/Berbuka Puasa checklists, shift handover messages, and PDF report exports."
            } else {
                "Wali Asuh Care membantu Wali Asuh dan Admin Sekolah dalam mencatat rencana berobat siswa, rujukan faskes, rekam medis asrama, pemantauan siklus haid & bersuci siswi, checklist sahur/berbuka puasa, pesan operan shift, serta pembuatan laporan PDF."
            }
        )

        LegalSectionCard(
            title = if (isEn) "Independence Status & Disclaimer" else "Status Independensi & Disclaimer",
            body = stringResource(R.string.disclaimer_full),
            accent = WaliPrimary
        )

        LegalSectionCard(
            title = if (isEn) "Developer Contact" else "Kontak Pengembang",
            body = if (isEn) {
                "Email: $SUPPORT_EMAIL\nWhatsApp / Phone: $SUPPORT_PHONE_LOCAL"
            } else {
                "Email: $SUPPORT_EMAIL\nWhatsApp / HP: $SUPPORT_PHONE_LOCAL"
            }
        )
    }
}

@Composable
private fun PrivacyPolicyContent(isEn: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (isEn) "Last updated: November 2026" else "Terakhir diperbarui: November 2026",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LegalSectionCard(
            title = if (isEn) "1. Data Managed" else "1. Data yang Dikelola",
            body = if (isEn) {
                "The app stores operational data entered directly by authorized School Administrators and Wali Asuh, including:\n" +
                    "• Student identity (name, NIS/NIK, grade, Asrama, emergency contacts).\n" +
                    "• Health care records, Faskes referral diagnoses, Haid logs, and Puasa activities.\n" +
                    "• Staff accounts (name, username, Wali Asuh phone number, and school profile)."
            } else {
                "Aplikasi menyimpan data operasional yang diinput langsung oleh Administrator Sekolah dan Wali Asuh berwenang, meliputi:\n" +
                    "• Identitas siswa (nama, NIS/NIK, kelas, asrama, kontak darurat).\n" +
                    "• Catatan layanan kesehatan, diagnosa rujukan faskes, pendataan haid, dan aktivitas puasa.\n" +
                    "• Akun pengelola (nama, username, nomor telepon wali asuh, dan profil sekolah)."
            }
        )

        LegalSectionCard(
            title = if (isEn) "2. Purpose & Data Storage" else "2. Tujuan Penggunaan & Penyimpanan Data",
            body = if (isEn) {
                "• Data is stored locally on the device (Room) and synced over an encrypted HTTPS connection to a multi-tenant cloud database (PocketBase) isolated by school code (school_id).\n" +
                    "• Data is used solely to support care documentation, Wali Asuh reporting, and internal administration of registered schools."
            } else {
                "• Data disimpan secara lokal di perangkat (Room) dan disinkronkan melalui koneksi terenkripsi (HTTPS) ke database cloud multi-tenant (PocketBase) yang terisolasi per kode sekolah (school_id).\n" +
                    "• Data semata-mata digunakan untuk mendukung dokumentasi perawatan, pelaporan wali asuh, dan administrasi internal sekolah terdaftar."
            }
        )

        LegalSectionCard(
            title = if (isEn) "3. Device Permissions (Camera & Storage)" else "3. Izin Perangkat (Kamera & Penyimpanan)",
            body = if (isEn) {
                "• Camera permission is only used to scan barcodes/QR on student cards locally on the device. The app never uploads camera photos or video recordings to any server.\n" +
                    "• Document access is only used when you manually pick a CSV file for import or save/share PDF reports."
            } else {
                "• Izin Kamera hanya dipakai untuk memindai barcode/QR pada kartu siswa secara lokal di perangkat. Aplikasi tidak mengunggah foto atau rekaman video kamera ke server.\n" +
                    "• Akses dokumen hanya digunakan saat Anda memilih file CSV untuk impor atau menyimpan/membagikan laporan PDF secara manual."
            }
        )

        LegalSectionCard(
            title = if (isEn) "4. Third-Party Services & Ads" else "4. Layanan Pihak Ketiga & Iklan",
            body = if (isEn) {
                "• The app uses an ad service (TopOn) to show interstitial ads and optional rewarded video ads before opening/downloading Monthly Reports or Puasa/Health PDF Reports.\n" +
                    "• Student identities and health records are NEVER sold, rented, or shared with advertising networks."
            } else {
                "• Aplikasi menggunakan layanan iklan (TopOn) untuk menampilkan iklan interstitial dan iklan video berhadiah opsional sebelum membuka/mengunduh Laporan Bulanan atau Laporan PDF Puasa/Kesehatan.\n" +
                    "• Data identitas maupun catatan kesehatan siswa TIDAK pernah dijual, disewakan, atau dikirimkan ke jaringan pengiklan."
            }
        )

        LegalSectionCard(
            title = if (isEn) "5. Retention & Account / Data Deletion" else "5. Retensi & Penghapusan Akun / Data",
            body = if (isEn) {
                "• Wali Asuh can delete their personal account anytime directly inside the app via Profile -> Delete My Account.\n" +
                    "• School Administrators can edit/delete student & Wali Asuh data, or permanently delete the Admin account along with all school data via the School Admin Portal.\n" +
                    "• For manual deletion requests or privacy questions, contact us at $SUPPORT_EMAIL."
            } else {
                "• Wali Asuh dapat menghapus akun pribadinya secara mandiri kapan saja langsung di dalam aplikasi melalui menu Profil -> Hapus Akun Saya.\n" +
                    "• Administrator Sekolah dapat mengedit/menghapus data siswa & wali asuh, atau menghapus akun Admin beserta seluruh data sekolah secara permanen melalui Portal Admin -> Hapus Akun Admin & Data Sekolah Permanen.\n" +
                    "• Untuk permintaan penghapusan data manual atau pertanyaan privasi, hubungi kami di $SUPPORT_EMAIL."
            }
        )
    }
}

@Composable
private fun TermsOfServiceContent(isEn: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (isEn) "Applies to all users (School Admin & Wali Asuh)" else "Berlaku untuk seluruh pengguna (Admin Sekolah & Wali Asuh)",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LegalSectionCard(
            title = if (isEn) "1. Eligibility & User Authorization" else "1. Kelayakan & Otorisasi Pengguna",
            body = if (isEn) {
                "Wali Asuh Care is intended for school managers, Asrama admins, and adult Wali Asuh (18+) who have been officially authorized by their respective schools to manage Asrama operational data."
            } else {
                "Aplikasi Wali Asuh Care ditujukan bagi pengelola sekolah, admin asrama, dan wali asuh dewasa (18+) yang telah diberi otorisasi resmi oleh sekolah masing-masing untuk mengelola data operasional asrama."
            }
        )

        LegalSectionCard(
            title = if (isEn) "2. Medical Disclaimer (Important)" else "2. Disclaimer Medis (Penting)",
            body = if (isEn) {
                "Wali Asuh Care is a daily Asrama documentation and administration aid. This app is NOT a medical diagnostic tool, NOT a clinical health device, and does NOT replace medical advice, examinations, or decisions by licensed doctors or official Faskes."
            } else {
                "Wali Asuh Care adalah alat bantu dokumentasi dan administrasi harian asrama. Aplikasi ini BUKAN alat diagnosa medis, bukan perangkat kesehatan klinis, dan tidak menggantikan saran, pemeriksaan, atau keputusan dokter maupun fasilitas kesehatan resmi."
            }
        )

        LegalSectionCard(
            title = if (isEn) "3. Account Security & Confidentiality" else "3. Keamanan Akun & Kerahasiaan Data",
            body = if (isEn) {
                "School managers and Wali Asuh are fully responsible for keeping their School Code, Username, and Password confidential, and ensuring student personal data is never misused outside caregiving duties."
            } else {
                "Pengelola sekolah dan wali asuh bertanggung jawab penuh untuk menjaga kerahasiaan Kode Sekolah, Username, dan Kata Sandi masing-masing, serta memastikan data pribadi siswa tidak disalahgunakan di luar kepentingan pengasuhan."
            }
        )

        LegalSectionCard(
            title = if (isEn) "4. Feature Usage & Ads" else "4. Penggunaan Fitur & Iklan",
            body = if (isEn) {
                "Core daily recording features remain available without a subscription fee. Certain export/report features may show a rewarded video ad with clear notice beforehand. Modifying the app, scraping, or unauthorized database server access is prohibited."
            } else {
                "Fitur inti pencatatan harian tetap dapat digunakan tanpa biaya langganan. Beberapa fitur ekspor/laporan tertentu dapat menampilkan iklan video berhadiah dengan pemberitahuan jelas sebelum diputar. Dilarang melakukan modifikasi aplikasi, scraping, atau akses tidak sah ke database server."
            }
        )

        LegalSectionCard(
            title = if (isEn) "5. Service Changes" else "5. Perubahan Layanan",
            body = if (isEn) {
                "The developer reserves the right to update features, synchronization schemas, and terms & conditions at any time for security and service improvements. Continued use implies agreement to the latest terms."
            } else {
                "Pengembang berhak memperbarui fitur, skema sinkronisasi, serta syarat & ketentuan sewaktu-waktu demi keamanan dan peningkatan layanan. Penggunaan aplikasi secara berkelanjutan berarti menyetujui ketentuan terbaru."
            }
        )
    }
}

@Composable
private fun ContactUsContent(context: Context, isEn: Boolean) {
    fun copyText(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        val msg = if (isEn) "$label copied: $value" else "$label disalin: $value"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (isEn) {
                "Need help with school registration, PocketBase sync issues, bug reports, or data deletion requests? Contact the developer team via these official channels:"
            } else {
                "Butuh bantuan pendaftaran sekolah, kendala sinkronisasi PocketBase, laporan bug, atau permintaan penghapusan data? Hubungi tim pengembang melalui kanal resmi berikut:"
            },
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )

        // Kartu Email Resmi
        ContactChannelCard(
            title = if (isEn) "Official Support Email" else "Email Dukungan Resmi",
            value = SUPPORT_EMAIL,
            subtitle = if (isEn) {
                "Best for formal inquiries, bug reports, & data privacy requests"
            } else {
                "Cocok untuk pengajuan resmi, laporan bug, & permintaan privasi data"
            },
            icon = Icons.Default.Email,
            accentColor = WaliPrimary,
            primaryActionLabel = if (isEn) "Send Email" else "Kirim Email",
            primaryActionIcon = Icons.Default.Email,
            onPrimaryAction = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$SUPPORT_EMAIL")
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        if (isEn) "Wali Asuh Care App Support" else "Dukungan Aplikasi Wali Asuh Care"
                    )
                }
                runCatching {
                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            if (isEn) "Send Email to Developer" else "Kirim Email ke Pengembang"
                        )
                    )
                }.onFailure {
                    copyText("Email", SUPPORT_EMAIL)
                }
            },
            onCopy = { copyText("Email", SUPPORT_EMAIL) },
            copyLabelPrefix = if (isEn) "Copy" else "Salin",
            testTagPrefix = "contact_email"
        )

        // Kartu WhatsApp / Nomor HP Resmi
        ContactChannelCard(
            title = if (isEn) "Developer WhatsApp / Phone" else "WhatsApp / Telepon Pengembang",
            value = SUPPORT_PHONE_LOCAL,
            subtitle = if (isEn) {
                "Quick response for admin technical support & multi-school coordination"
            } else {
                "Respon cepat untuk bantuan teknis admin & koordinasi multi-sekolah"
            },
            icon = Icons.Default.Phone,
            accentColor = Color(0xFF16A34A),
            primaryActionLabel = if (isEn) "WhatsApp Chat" else "Chat WhatsApp",
            primaryActionIcon = Icons.Default.Send,
            onPrimaryAction = {
                val msg = if (isEn) {
                    "Hello Wali Asuh Care Admin/Developer, I would like to ask about the app."
                } else {
                    "Halo Admin/Pengembang Wali Asuh Care, saya ingin berkonsultasi mengenai aplikasi."
                }
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$SUPPORT_PHONE_INTL&text=${Uri.encode(msg)}")
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }.onFailure {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE_LOCAL"))
                    runCatching { context.startActivity(dialIntent) }
                        .onFailure { copyText(if (isEn) "Phone" else "Nomor HP", SUPPORT_PHONE_LOCAL) }
                }
            },
            secondaryActionLabel = if (isEn) "Call" else "Telepon",
            onSecondaryAction = {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE_LOCAL"))
                runCatching {
                    context.startActivity(dialIntent)
                }.onFailure {
                    copyText(if (isEn) "Phone" else "Nomor HP", SUPPORT_PHONE_LOCAL)
                }
            },
            onCopy = { copyText(if (isEn) "Phone" else "Nomor HP", SUPPORT_PHONE_LOCAL) },
            copyLabelPrefix = if (isEn) "Copy" else "Salin",
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
    copyLabelPrefix: String = "Salin",
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
                        contentDescription = "$copyLabelPrefix $title",
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
