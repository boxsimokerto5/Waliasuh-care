package com.example.ui.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.UserRole

enum class GuideRoleFilter(
    val id: String,
    val labelId: String,
    val labelEn: String
) {
    ALL("all", "Semua", "All"),
    WALI("wali", "Wali Asuh", "Wali Asuh"),
    GURU_NGAJI("guru_ngaji", "Guru Ngaji", "Quran Teacher"),
    NAKES("nakes", "Nakes", "Health Officer"),
    PARENT("parent", "Orang Tua", "Parent"),
    ADMIN("admin", "Admin", "Admin");

    companion object {
        fun fromUserRole(role: UserRole): GuideRoleFilter = when (role) {
            UserRole.WALI -> WALI
            UserRole.GURU_NGAJI -> GURU_NGAJI
            UserRole.PETUGAS_KESEHATAN -> NAKES
            UserRole.PARENT -> PARENT
            UserRole.ADMIN, UserRole.SUPERADMIN -> ADMIN
            UserRole.NOT_LOGGED_IN -> ALL
        }
    }
}

data class GuideRoleOverview(
    val role: GuideRoleFilter,
    val titleId: String,
    val titleEn: String,
    val summaryId: String,
    val summaryEn: String,
    val icon: ImageVector,
    val accent: Color
)

data class GuideSection(
    val id: String,
    val titleId: String,
    val titleEn: String,
    val subtitleId: String,
    val subtitleEn: String,
    val roles: Set<GuideRoleFilter>,
    val icon: ImageVector,
    val accent: Color,
    val stepsId: List<String>,
    val stepsEn: List<String>,
    val tipId: String? = null,
    val tipEn: String? = null
) {
    fun matches(query: String, roleFilter: GuideRoleFilter, isEn: Boolean): Boolean {
        if (roleFilter != GuideRoleFilter.ALL && roleFilter !in roles) return false
        val q = query.trim().lowercase()
        if (q.isBlank()) return true
        val title = (if (isEn) titleEn else titleId).lowercase()
        val subtitle = (if (isEn) subtitleEn else subtitleId).lowercase()
        val steps = if (isEn) stepsEn else stepsId
        val tip = (if (isEn) tipEn else tipId).orEmpty().lowercase()
        return title.contains(q) ||
            subtitle.contains(q) ||
            tip.contains(q) ||
            steps.any { it.lowercase().contains(q) }
    }
}

fun buildGuideRoleOverviews(): List<GuideRoleOverview> = listOf(
    GuideRoleOverview(
        role = GuideRoleFilter.WALI,
        titleId = "Wali Asuh",
        titleEn = "Wali Asuh",
        summaryId = "Kelola anak binaan, berobat, ibadah, haid, tabungan, konseling, kunjungan & jurnal.",
        summaryEn = "Manage assigned students, medical care, worship, menstruation, savings, counseling & visits.",
        icon = Icons.Default.Person,
        accent = Color(0xFF1976D2)
    ),
    GuideRoleOverview(
        role = GuideRoleFilter.GURU_NGAJI,
        titleId = "Guru Ngaji",
        titleEn = "Quran Teacher",
        summaryId = "Fokus pencatatan setoran/capaian mengaji siswa dan akses Al-Qur'an Digital.",
        summaryEn = "Focus on Quran recitation logs, student progress, and Digital Al-Qur'an reader.",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        accent = Color(0xFF0F766E)
    ),
    GuideRoleOverview(
        role = GuideRoleFilter.NAKES,
        titleId = "Petugas Kesehatan (Nakes)",
        titleEn = "Health Officer",
        summaryId = "Pantau antrean UKS/faskes, input diagnosa & obat, jadwal kontrol, serta shift tugas.",
        summaryEn = "Monitor UKS/clinic queues, record diagnosis & meds, follow-ups, and shift schedule.",
        icon = Icons.Default.LocalHospital,
        accent = Color(0xFFDC2626)
    ),
    GuideRoleOverview(
        role = GuideRoleFilter.PARENT,
        titleId = "Portal Orang Tua",
        titleEn = "Parent Portal",
        summaryId = "Pantau kesehatan, ibadah Islam/Kristen, kunjungan, tabungan, kirim pesan & unduh PDF.",
        summaryEn = "Monitor health, Islamic/Christian worship, visits, savings, send notes & download PDF.",
        icon = Icons.Default.FamilyRestroom,
        accent = Color(0xFF7C3AED)
    ),
    GuideRoleOverview(
        role = GuideRoleFilter.ADMIN,
        titleId = "Admin & Superadmin",
        titleEn = "Admin & Superadmin",
        summaryId = "Kelola akun Wali/Nakes/Orang Tua, data siswa CSV, jadwal faskes & pengaturan sekolah.",
        summaryEn = "Manage staff/parent accounts, CSV student imports, referral facilities & school settings.",
        icon = Icons.Default.AdminPanelSettings,
        accent = Color(0xFFB45309)
    )
)

fun buildGuideSections(
    faskesSummaryId: String,
    faskesSummaryEn: String
): List<GuideSection> = listOf(
    GuideSection(
        id = "kesehatan",
        titleId = "Kesehatan, Berobat & Petugas Nakes",
        titleEn = "Health, Medical Care & Health Officers",
        subtitleId = "Rencana berobat, tindakan medis, kontrol & laporan WA/PDF",
        subtitleEn = "Treatment plans, direct actions, follow-ups & WA/PDF reports",
        roles = setOf(GuideRoleFilter.WALI, GuideRoleFilter.NAKES, GuideRoleFilter.ADMIN),
        icon = Icons.Default.LocalHospital,
        accent = Color(0xFFDC2626),
        stepsId = listOf(
            "Tambah anak sakit lewat menu Berobat → Rencana Berobat atau tombol (+) di Dashboard/Rencana.",
            "Pilih faskes tujuan ($faskesSummaryId) dan jam berangkat, atau gunakan Tindakan Medis Langsung untuk penanganan cepat di UKS.",
            "Gunakan Scan Barcode/QR ID siswa untuk memilih anak lebih cepat saat pemeriksaan.",
            "Setelah pemeriksaan, Nakes atau Wali mencatat hasil diagnosa, resep obat, status rawat inap, serta tanggal kontrol (atau centang tidak perlu kontrol).",
            "Bagikan laporan ke grup WhatsApp pengasuh lewat tombol Laporan WA atau cetak Laporan PDF."
        ),
        stepsEn = listOf(
            "Add a sick student via Medical Care → Treatment Plans or the (+) button on Dashboard/Plans.",
            "Select the destination facility ($faskesSummaryEn) and departure time, or use Direct Medical Action for fast UKS care.",
            "Use Student Barcode/QR Scan to identify students rapidly during checkups.",
            "After examination, Health Officers or Wali record diagnosis, medication, inpatient status, and follow-up date (or mark no follow-up needed).",
            "Share updates to the caregiver WhatsApp group via WA Report or export a PDF Report."
        ),
        tipId = "Petugas Kesehatan (Nakes) memiliki layar shift khusus untuk memantau antrean rujukan, rekam medis, dan pembagian jadwal jaga UKS.",
        tipEn = "Health Officers (Nakes) get a dedicated shift view to track clinic queues, medical records, and UKS duty rosters."
    ),
    GuideSection(
        id = "anak_asuh",
        titleId = "Anak Asuh, Klaim Binaan & Tata Letak",
        titleEn = "Students, Care Claims & Layout",
        subtitleId = "Klaim anak asuh binaan, profil lengkap & atur ikon Dashboard",
        subtitleEn = "Claim assigned students, view unified profiles & customize Dashboard icons",
        roles = setOf(GuideRoleFilter.WALI, GuideRoleFilter.GURU_NGAJI, GuideRoleFilter.ADMIN),
        icon = Icons.Default.Groups,
        accent = Color(0xFF1976D2),
        stepsId = listOf(
            "Buka menu Klaim Anak Asuh untuk menandai siswa yang menjadi binaan Anda agar muncul lebih ringkas.",
            "Buka menu Anak Asuh untuk mencari siswa, melihat riwayat alergi, golongan darah, kontak orang tua, dan catatan terpadu anak.",
            "Gunakan fitur atur tata letak ikon Dashboard untuk memindahkan menu yang paling sering dipakai ke posisi teratas."
        ),
        stepsEn = listOf(
            "Open Care Claims to assign students under your supervision so your daily views stay focused.",
            "Open Students to search profiles, check allergies, blood type, parent contacts, and unified student history.",
            "Use Dashboard icon layout customization to pin your most frequently used menus to the top."
        ),
        tipId = "Admin/Superadmin dapat menambah atau memperbarui data siswa massal melalui fitur Impor/Ekspor CSV di menu pengelolaan data.",
        tipEn = "Admins/Superadmins can bulk-import or export student records via CSV in the data management panel."
    ),
    GuideSection(
        id = "kunjungan",
        titleId = "Kunjungan Orang Tua & Edaran",
        titleEn = "Parent Visits & Circular Letters",
        subtitleId = "Sesi kunjungan, edaran PDF/WA, daftar hadir & tukar sesi",
        subtitleEn = "Visit sessions, PDF/WA circulars, attendance & session swaps",
        roles = setOf(GuideRoleFilter.WALI, GuideRoleFilter.PARENT, GuideRoleFilter.ADMIN),
        icon = Icons.Default.FamilyRestroom,
        accent = Color(0xFF0284C7),
        stepsId = listOf(
            "Buat sesi dan jadwal kunjungan orang tua dari menu Kunjungan.",
            "Terbitkan Edaran Orang Tua dalam format teks WhatsApp maupun dokumen PDF resmi yang siap dibagikan.",
            "Pantau Daftar Hadir dan konfirmasi kedatangan orang tua secara real-time saat hari kunjungan.",
            "Jika jadwal berhalangan, gunakan fitur Tukar Sesi Kunjungan antar wali/sesi agar pelayanan tetap terjaga."
        ),
        stepsEn = listOf(
            "Create and schedule parent visit sessions from the Visits hub.",
            "Generate Official Parent Circulars as shareable WhatsApp text or formatted PDF documents.",
            "Track real-time Attendance Lists and parent arrival confirmations on visiting day.",
            "Use Visit Session Swap whenever a schedule conflict requires reassigning a session."
        )
    ),
    GuideSection(
        id = "ibadah_islam",
        titleId = "Sholat, Mengaji, Al-Qur'an Digital & Puasa",
        titleEn = "Prayer, Quran Recitation, Digital Qur'an & Fasting",
        subtitleId = "Absensi ibadah harian, jadwal sholat & Mushaf Al-Qur'an kaligrafi",
        subtitleEn = "Daily worship logs, live prayer times & calligraphic Digital Qur'an",
        roles = setOf(GuideRoleFilter.WALI, GuideRoleFilter.GURU_NGAJI, GuideRoleFilter.PARENT),
        icon = Icons.Default.Mosque,
        accent = Color(0xFF0F766E),
        stepsId = listOf(
            "Sholat: Catat kehadiran sholat 5 waktu siswa, pantau grafik rekap, dan lihat jadwal waktu sholat langsung di kartu atas Dashboard.",
            "Mengaji & Al-Qur'an Digital: Wali Asuh & Guru Ngaji dapat mencatat capaian surah/juz/jilid serta membuka Mushaf Al-Qur'an lengkap dengan audio per ayat dan penanda bacaan.",
            "Puasa: Catat pelaksanaan sahur/buka puasa beserta keterangan pengecualian (sakit, haid, atau jenjang usia).",
            "Bagikan laporan Sholat, Mengaji, atau Puasa ke WhatsApp atau PDF kapan saja."
        ),
        stepsEn = listOf(
            "Prayer: Log 5 daily prayers, review summary charts, and check live prayer schedules on the top Dashboard card.",
            "Recitation & Digital Qur'an: Wali Asuh and Quran Teachers can record Surah/Juz progress and open the calligraphic Digital Mushaf with per-ayah audio and bookmarks.",
            "Fasting: Record suhoor/iftar participation with exemption notes (illness, menstruation, or age tier).",
            "Export and share Prayer, Recitation, or Fasting summaries via WhatsApp or PDF."
        ),
        tipId = "Al-Qur'an Digital juga tersedia langsung di Hub Mengaji dan Portal Orang Tua.",
        tipEn = "The Digital Al-Qur'an is directly accessible from both the Mengaji hub and the Parent Portal."
    ),
    GuideSection(
        id = "agama_kristen",
        titleId = "Menu Agama Kristen",
        titleEn = "Christian Worship & Ministry",
        subtitleId = "Klaim anak Kristen, ibadah pagi/malam, gereja, Alkitab & laporan",
        subtitleEn = "Christian student claims, morning/evening devotions, church & reports",
        roles = setOf(GuideRoleFilter.WALI, GuideRoleFilter.PARENT, GuideRoleFilter.ADMIN),
        icon = Icons.Default.Church,
        accent = Color(0xFF4F46E5),
        stepsId = listOf(
            "Buka menu Agama Kristen lalu lakukan Klaim Anak Beragama Kristen agar siswa tercatat dalam pembinaan rohani Kristen.",
            "Catat kegiatan Ibadah Pagi, Ibadah Malam, serta kehadiran Ibadah Gereja beserta catatan pembinaan.",
            "Akses bacaan Alkitab & menu rohani pendukung langsung dari panel Agama Kristen.",
            "Lihat rekap laporan di menu Laporan Agama Kristen dan bagikan ke orang tua atau pengasuh."
        ),
        stepsEn = listOf(
            "Open Christian Worship and claim Christian students so they are grouped under Christian spiritual care.",
            "Record Morning Worship, Evening Worship, and Church Service attendance with pastoral notes.",
            "Access Bible readings and devotional tools directly inside the Christian Worship hub.",
            "Review summaries in Christian Reports and share them with parents or caregivers."
        ),
        tipId = "Anak yang diklaim beragama Kristen otomatis menampilkan menu Agama Kristen beserta laporannya di Portal Orang Tua.",
        tipEn = "Students claimed as Christian automatically unlock the Christian Worship menu and reports in their Parent Portal."
    ),
    GuideSection(
        id = "pendampingan",
        titleId = "Haid, Tabungan & Konseling",
        titleEn = "Menstruation, Savings & Counseling",
        subtitleId = "Kesehatan remaja putri, keuangan saku & bimbingan anak asuh",
        subtitleEn = "Girls' cycle tracking, student allowance ledger & counseling notes",
        roles = setOf(GuideRoleFilter.WALI, GuideRoleFilter.NAKES, GuideRoleFilter.PARENT),
        icon = Icons.Default.AccountBalanceWallet,
        accent = Color(0xFFD97706),
        stepsId = listOf(
            "Haid: Catat tanggal mulai dan selesai haid siswi; sistem membantu memantau durasi normal vs kondisi istihadhah.",
            "Tabungan: Catat uang masuk (setoran) dan penarikan siswa secara transparan lengkap dengan saldo berjalan dan riwayat log.",
            "Konseling: Dokumentasikan sesi bimbingan pribadi, isu yang dibahas, serta rencana tindak lanjut antar wali asuh."
        ),
        stepsEn = listOf(
            "Menstruation: Log start and end dates for female students to monitor normal cycles vs prolonged bleeding.",
            "Savings: Record deposits and withdrawals with transparent running balances and transaction logs.",
            "Counseling: Document personal guidance sessions, key topics, and follow-up plans across caregivers."
        )
    ),
    GuideSection(
        id = "komunikasi_catatan",
        titleId = "Jadwal Wali, Pesan, Info Penting & Jurnal",
        titleEn = "Staff Schedule, Shift Notes, Notices & Journal",
        subtitleId = "Jadwal piket, pesan antar shift, aturan bersama & catatan pribadi",
        subtitleEn = "Duty rosters, shift handovers, shared rules & personal notes",
        roles = setOf(GuideRoleFilter.WALI, GuideRoleFilter.NAKES, GuideRoleFilter.GURU_NGAJI, GuideRoleFilter.ADMIN),
        icon = Icons.Default.EditNote,
        accent = Color(0xFF0369A1),
        stepsId = listOf(
            "Jadwal Wali Asuh: Lihat jadwal piket harian/bulanan, pengajuan cuti/libur, serta tukar jadwal shift.",
            "Pesan & Info Penting: Kirim pesan serah-terima antar shift dan baca pengumuman resmi asrama.",
            "Jurnal & Aturan Bersama: Kelola aturan bersama (dilengkapi Mode Baca dan berbagi pesan) serta Catatan Pribadi dengan dikte suara (ikon mikrofon) dan lampiran gambar."
        ),
        stepsEn = listOf(
            "Staff Schedule: View daily/monthly duty rosters, leave days, and shift swap requests.",
            "Messages & Notices: Send handover notes across shifts and review important dormitory announcements.",
            "Shared Journal & Rules: Maintain shared rules (with Read Mode and sharing) plus Private Notes supporting voice dictation and image attachments."
        )
    ),
    GuideSection(
        id = "portal_profil",
        titleId = "Portal Orang Tua, Laporan PDF & Pengaturan",
        titleEn = "Parent Portal, PDF Reports & Settings",
        subtitleId = "Akses wali murid, unduh laporan PDF, bahasa ID/EN & akun",
        subtitleEn = "Parent access, PDF exports, ID/EN language & account settings",
        roles = setOf(GuideRoleFilter.PARENT, GuideRoleFilter.WALI, GuideRoleFilter.ADMIN, GuideRoleFilter.GURU_NGAJI, GuideRoleFilter.NAKES),
        icon = Icons.Default.CalendarMonth,
        accent = Color(0xFF6D28D9),
        stepsId = listOf(
            "Portal Orang Tua: Orang tua login menggunakan akun khusus untuk melihat perkembangan kesehatan, ibadah (Mengaji/Al-Qur'an atau Agama Kristen), tabungan, kunjungan, serta mengirim Ekspektasi Orang Tua.",
            "Laporan Bulanan & PDF: Tersedia di menu Profil maupun Portal Orang Tua untuk mengunduh rekap aktivitas anak dalam format PDF.",
            "Pengaturan Profil: Ubah bahasa (Indonesia / English), tema Terang / Gelap, kata sandi akun, dan lakukan sinkronisasi data server."
        ),
        stepsEn = listOf(
            "Parent Portal: Parents sign in with dedicated credentials to track health, worship (Quran/Islamic or Christian), savings, visits, and submit Parent Expectations.",
            "Monthly & PDF Reports: Available in Profile and Parent Portal to export formatted activity summaries.",
            "Profile Settings: Switch language (Indonesian / English), toggle Light / Dark theme, update passwords, and sync server data."
        ),
        tipId = "Gunakan tombol 'Putar Tur Fitur' di bawah jika ingin melihat sorotan langsung tiap ikon pada layar Dashboard.",
        tipEn = "Tap 'Interactive Tour' below anytime to replay the step-by-step spotlight tour on the Dashboard."
    )
)
