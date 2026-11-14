package com.example.util.i18n

/**
 * Strings for Fiqih Haid dialogs/sheets, Barcode Scanner, and Puasa tracking sheets.
 * Preserves domain terms per Point 2: Fiqih Haid, Haid, Suci, Puasa, Sahur, Berbuka, Mandi Suci, Istihadhah.
 */
interface HaidFastingStrings {
    // Input Haid Dialog
    val inputHaidTitle: String
    val inputHaidSubtitle: String
    val inputHaidSelectGirl: String
    val inputHaidStartDateLabel: String
    val inputHaidStartTimeLabel: String
    val inputHaidNotesLabel: String
    val inputHaidNotesPlaceholder: String
    val inputHaidFiqihRuleBanner: String
    val inputHaidAlreadyActiveError: (String) -> String
    val inputHaidSaveBtn: String

    // Daftar Haid Sheet & Timeline Components
    val daftarHaidTitle: String
    val daftarHaidSubtitle: String
    val daftarHaidPrintPdfBtn: String
    val daftarHaidShareWaBtn: String
    val daftarHaidAddBtn: String
    val daftarHaidTabActive: String
    val daftarHaidTabSuciHistory: String
    val daftarHaidEmptyActive: String
    val daftarHaidEmptyActiveDesc: String
    val daftarHaidEmptyHistory: String
    val daftarHaidDayBadge: (Int) -> String
    val daftarHaidStartDateLabel: (String) -> String
    val daftarHaidEstSuciLabel: (String) -> String
    val daftarHaidMarkSuciBtn: String
    val daftarHaidOver15Warning: String
    val daftarHaidTimelineTitle: String
    val daftarHaidTimelineStartNode: String
    val daftarHaidTimelineTodayNode: (Int) -> String
    val daftarHaidTimelineMaxNode: String
    val daftarHaidBloodColorLabel: String
    val daftarHaidSuciConfirmTitle: String
    val daftarHaidSuciConfirmBody: (String, Int) -> String
    val daftarHaidSuciDateLabel: String
    val daftarHaidSuciConfirmBtn: String

    // Daftar Suci Sheet
    val daftarSuciTitle: String
    val daftarSuciSubtitle: String
    val daftarSuciSearchHint: String
    val daftarSuciEmpty: String
    val daftarSuciBadgeDone: String
    val daftarSuciBadgeAuto: String
    val daftarSuciPeriodLabel: (String, String, Int) -> String
    val daftarSuciMandiLabel: String

    // Fiqih Haid Guide Dialog
    val fiqihHaidTitle: String
    val fiqihHaidSubtitle: String
    val fiqihRule1Title: String
    val fiqihRule1Body: String
    val fiqihRule2Title: String
    val fiqihRule2Body: String
    val fiqihRule3Title: String
    val fiqihRule3Body: String
    val fiqihRule4Title: String
    val fiqihRule4Body: String
    val fiqihRule5Title: String
    val fiqihRule5Body: String

    // Barcode Scanner & Camera Preview
    val scannerTitle: String
    val scannerSubtitle: String
    val scannerManualHint: String
    val scannerManualLabel: String
    val scannerProcessBtn: String
    val scannerCameraPermTitle: String
    val scannerCameraPermDesc: String
    val scannerCameraPermBtn: String
    val scannerFlashToggleDesc: String
    val scannerSwitchModeManual: String
    val scannerSwitchModeCamera: String
    val scannerNotFoundMsg: (String) -> String
    val scannerFoundHeader: String
    val scannerRecentHistoryBtn: String
    val scannerScanAgainBtn: String

    // Fasting (Puasa, Sahur, Berbuka)
    val inputPuasaTitle: String
    val inputPuasaSubtitle: String
    val inputPuasaSessionNameLabel: String
    val inputPuasaSessionPlaceholder: String
    val inputPuasaDateLabel: String
    val inputPuasaPresetLabel: String
    val inputPuasaCreateBtn: String

    val daftarPuasaTitle: String
    val daftarPuasaSubtitle: String
    val daftarPuasaAddSessionBtn: String
    val daftarPuasaEmpty: String
    val daftarPuasaEmptyDesc: String
    val daftarPuasaParticipantsCount: (Int) -> String
    val daftarPuasaSahurCount: (Int, Int) -> String
    val daftarPuasaBukaCount: (Int, Int) -> String
    val daftarPuasaOpenChecklistBtn: String
    val daftarPuasaPrintPdfBtn: String
    val daftarPuasaShareWaBtn: String

    val checklistPuasaTitle: (String) -> String
    val checklistPuasaSessionDate: (String) -> String
    val checklistPuasaScanBtn: String
    val checklistPuasaAddManualBtn: String
    val checklistPuasaFilterAll: String
    val checklistPuasaSearchHint: String
    val checklistPuasaEmpty: String
    val checklistPuasaSahurLabel: String
    val checklistPuasaBukaLabel: String
    val checklistPuasaSahurDoneBy: (String) -> String
    val checklistPuasaBukaDoneBy: (String) -> String
    val checklistPuasaSelectStudentTitle: String

    val rankingPuasaTitle: String
    val rankingPuasaSubtitle: String
    val rankingPuasaFilterAll: String
    val rankingPuasaPeriodAll: String
    val rankingPuasaPeriodMonth: String
    val rankingPuasaEmpty: String
    val rankingPuasaScoreFormat: (Int, Int, Int) -> String
    val rankingPuasaPrintPdfBtn: String
}

object IdHaidFastingStrings : HaidFastingStrings {
    override val inputHaidTitle = "Input Catatan Haid Santri"
    override val inputHaidSubtitle = "Pencatatan awal siklus haid sesuai panduan Fiqih Haid"
    override val inputHaidSelectGirl = "Pilih Santri Putri"
    override val inputHaidStartDateLabel = "Tanggal Mulai Haid"
    override val inputHaidStartTimeLabel = "Jam Mulai (WIB)"
    override val inputHaidNotesLabel = "Catatan Warna / Keluhan (Opsional)"
    override val inputHaidNotesPlaceholder = "misal: Warna merah tua, nyeri perut ringan"
    override val inputHaidFiqihRuleBanner = "Minimal Haid: 24 jam (1 hari 1 malam) • Maksimal Haid: 15 hari 15 malam (360 jam). Lewat batas 15 hari dihitung Istihadhah / Suci."
    override val inputHaidAlreadyActiveError: (String) -> String = { "$it masih memiliki status Haid aktif." }
    override val inputHaidSaveBtn = "Simpan Catatan Haid"

    override val daftarHaidTitle = "Daftar Santri Haid Aktif"
    override val daftarHaidSubtitle = "Pemantauan durasi siklus haid & estimasi suci (Max 15 hari)"
    override val daftarHaidPrintPdfBtn = "Cetak PDF"
    override val daftarHaidShareWaBtn = "Kirim WA"
    override val daftarHaidAddBtn = "Input Haid"
    override val daftarHaidTabActive = "Sedang Haid"
    override val daftarHaidTabSuciHistory = "Riwayat Suci"
    override val daftarHaidEmptyActive = "Tidak ada santri putri yang sedang haid"
    override val daftarHaidEmptyActiveDesc = "Seluruh santri putri dalam keadaan suci & aktif sholat."
    override val daftarHaidEmptyHistory = "Belum ada riwayat selesai haid."
    override val daftarHaidDayBadge: (Int) -> String = { "Hari ke-$it" }
    override val daftarHaidStartDateLabel: (String) -> String = { "Mulai: $it" }
    override val daftarHaidEstSuciLabel: (String) -> String = { "Maks 15 Hari: $it" }
    override val daftarHaidMarkSuciBtn = "Tandai Suci"
    override val daftarHaidOver15Warning = "⚠️ Melebihi 15 hari (Istihadhah) - Wajib Mandi Suci"
    override val daftarHaidTimelineTitle = "Linimasa Siklus Haid (Standar 15 Hari)"
    override val daftarHaidTimelineStartNode = "Mulai"
    override val daftarHaidTimelineTodayNode: (Int) -> String = { "Hari ke-$it" }
    override val daftarHaidTimelineMaxNode = "Batas 15 Hari"
    override val daftarHaidBloodColorLabel = "Indikator Warna & Fase Fiqih:"
    override val daftarHaidSuciConfirmTitle = "Konfirmasi Sudah Suci & Mandi Wajib"
    override val daftarHaidSuciConfirmBody: (String, Int) -> String = { name, days ->
        "Tandai $name telah suci setelah menjalani siklus selama $days hari?"
    }
    override val daftarHaidSuciDateLabel = "Tanggal Suci"
    override val daftarHaidSuciConfirmBtn = "Konfirmasi Suci"

    override val daftarSuciTitle = "Daftar Santri Sudah Suci"
    override val daftarSuciSubtitle = "Arsip santri putri yang telah selesai masa haid & siap sholat"
    override val daftarSuciSearchHint = "Cari nama atau kelas santri..."
    override val daftarSuciEmpty = "Belum ada riwayat santri yang selesai haid."
    override val daftarSuciBadgeDone = "Suci (Mandi Wajib)"
    override val daftarSuciBadgeAuto = "Suci Otomatis (>15 Hari)"
    override val daftarSuciPeriodLabel: (String, String, Int) -> String = { start, end, days -> "$start s.d $end ($days hari)" }
    override val daftarSuciMandiLabel = "Status: Sudah bersuci / sholat kembali"

    override val fiqihHaidTitle = "Panduan Ringkas Fiqih Haid"
    override val fiqihHaidSubtitle = "Rujukan cepat Wali Asuh Asrama Putri (Mazhab Syafi'i)"
    override val fiqihRule1Title = "1. Usia Minimal Haid"
    override val fiqihRule1Body = "Usia minimal perempuan mengeluarkan darah haid adalah sekitar 9 tahun qomariyah."
    override val fiqihRule2Title = "2. Durasi Minimal & Maksimal Haid"
    override val fiqihRule2Body = "Minimal haid adalah sehari semalam (24 jam) secara terus menerus atau terakumulasi. Maksimalnya 15 hari 15 malam (360 jam)."
    override val fiqihRule3Title = "3. Masa Suci Antar Dua Haid"
    override val fiqihRule3Body = "Minimal masa suci antara dua periode haid adalah 15 hari 15 malam."
    override val fiqihRule4Title = "4. Darah Melebihi 15 Hari (Istihadhah)"
    override val fiqihRule4Body = "Jika darah masih keluar melewati 15 hari 15 malam, maka selebihnya dianggap darah Istihadhah; santri wajib mandi suci dan tetap menunaikan sholat."
    override val fiqihRule5Title = "5. Kewajiban Mandi Wajib & Qadha"
    override val fiqihRule5Body = "Santri yang telah berhenti darahnya wajib segera mandi besar. Puasa wajib yang ditinggalkan selama haid wajib diqadha, sedangkan sholat tidak."

    override val scannerTitle = "Scan QR Kartu Santri"
    override val scannerSubtitle = "Arahkan kamera ke QR Code pada Kartu Identitas Santri"
    override val scannerManualHint = "Atau masukkan NIS / NIK secara manual:"
    override val scannerManualLabel = "Masukkan NIS atau NIK Santri"
    override val scannerProcessBtn = "Cari Data Santri"
    override val scannerCameraPermTitle = "Izin Kamera Diperlukan"
    override val scannerCameraPermDesc = "Izinkan akses kamera untuk memindai QR Code pada Kartu Santri secara instan."
    override val scannerCameraPermBtn = "Berikan Izin Kamera"
    override val scannerFlashToggleDesc = "Nyalakan / Matikan Lampu Flash"
    override val scannerSwitchModeManual = "Input Manual"
    override val scannerSwitchModeCamera = "Mode Kamera"
    override val scannerNotFoundMsg: (String) -> String = { "Data santri dengan kode '$it' tidak ditemukan di sekolah ini." }
    override val scannerFoundHeader = "Santri Teridentifikasi ✓"
    override val scannerRecentHistoryBtn = "Lihat Buku Sehat"
    override val scannerScanAgainBtn = "Scan Ulang"

    override val inputPuasaTitle = "Buat Sesi Absen Puasa"
    override val inputPuasaSubtitle = "Buka pendaftaran & checklist Sahur / Berbuka santri"
    override val inputPuasaSessionNameLabel = "Nama / Jenis Puasa *"
    override val inputPuasaSessionPlaceholder = "misal: Puasa Sunnah Senin, Puasa Ramadhan Hari ke-1"
    override val inputPuasaDateLabel = "Tanggal Pelaksanaan"
    override val inputPuasaPresetLabel = "Pilihan Cepat:"
    override val inputPuasaCreateBtn = "Buat Sesi Puasa"

    override val daftarPuasaTitle = "Daftar Sesi Puasa Santri"
    override val daftarPuasaSubtitle = "Kelola sesi puasa, checklist Sahur & Berbuka, serta laporan"
    override val daftarPuasaAddSessionBtn = "Buat Sesi"
    override val daftarPuasaEmpty = "Belum ada sesi puasa dibuat"
    override val daftarPuasaEmptyDesc = "Ketuk 'Buat Sesi' untuk memulai absen puasa Senin-Kamis, Arafah, atau Ramadhan."
    override val daftarPuasaParticipantsCount: (Int) -> String = { "$it Peserta" }
    override val daftarPuasaSahurCount: (Int, Int) -> String = { done, total -> "Sahur: $done/$total" }
    override val daftarPuasaBukaCount: (Int, Int) -> String = { done, total -> "Berbuka: $done/$total" }
    override val daftarPuasaOpenChecklistBtn = "Buka Absen"
    override val daftarPuasaPrintPdfBtn = "PDF"
    override val daftarPuasaShareWaBtn = "WA"

    override val checklistPuasaTitle: (String) -> String = { "Absen: $it" }
    override val checklistPuasaSessionDate: (String) -> String = { "Tanggal: $it" }
    override val checklistPuasaScanBtn = "Scan QR"
    override val checklistPuasaAddManualBtn = "Tambah Siswa"
    override val checklistPuasaFilterAll = "Semua"
    override val checklistPuasaSearchHint = "Cari nama peserta puasa..."
    override val checklistPuasaEmpty = "Belum ada peserta terdaftar di sesi ini."
    override val checklistPuasaSahurLabel = "Sahur"
    override val checklistPuasaBukaLabel = "Berbuka"
    override val checklistPuasaSahurDoneBy: (String) -> String = { "Sahur ✓ ($it)" }
    override val checklistPuasaBukaDoneBy: (String) -> String = { "Berbuka ✓ ($it)" }
    override val checklistPuasaSelectStudentTitle = "Daftarkan Santri Puasa"

    override val rankingPuasaTitle = "Ranking Santri Terajin Puasa"
    override val rankingPuasaSubtitle = "Peringkat berdasarkan keaktifan Sahur & Berbuka tuntas"
    override val rankingPuasaFilterAll = "Semua"
    override val rankingPuasaPeriodAll = "Semua Waktu"
    override val rankingPuasaPeriodMonth = "Bulan Ini"
    override val rankingPuasaEmpty = "Belum ada data keaktifan puasa untuk dihitung."
    override val rankingPuasaScoreFormat: (Int, Int, Int) -> String = { full, sahur, buka ->
        "$full Tuntas • $sahur Sahur • $buka Berbuka"
    }
    override val rankingPuasaPrintPdfBtn = "Cetak PDF Ranking"
}

object EnHaidFastingStrings : HaidFastingStrings {
    override val inputHaidTitle = "Input Student Haid Log"
    override val inputHaidSubtitle = "Record start of Haid cycle per Fiqih Haid guidelines"
    override val inputHaidSelectGirl = "Select Female Student"
    override val inputHaidStartDateLabel = "Haid Start Date"
    override val inputHaidStartTimeLabel = "Start Time (WIB)"
    override val inputHaidNotesLabel = "Notes / Symptoms (Optional)"
    override val inputHaidNotesPlaceholder = "e.g., Dark red flow, mild cramps"
    override val inputHaidFiqihRuleBanner = "Minimum Haid: 24 hours (1 day & night) • Maximum Haid: 15 days & nights (360 hours). Exceeding 15 days is classified as Istihadhah / Suci."
    override val inputHaidAlreadyActiveError: (String) -> String = { "$it already has an active Haid status." }
    override val inputHaidSaveBtn = "Save Haid Record"

    override val daftarHaidTitle = "Active Daftar Haid"
    override val daftarHaidSubtitle = "Monitor Haid cycle duration & estimated Suci (Max 15 days)"
    override val daftarHaidPrintPdfBtn = "Print PDF"
    override val daftarHaidShareWaBtn = "Share WA"
    override val daftarHaidAddBtn = "Input Haid"
    override val daftarHaidTabActive = "Active Haid"
    override val daftarHaidTabSuciHistory = "Suci History"
    override val daftarHaidEmptyActive = "No female students currently on Haid"
    override val daftarHaidEmptyActiveDesc = "All female students are currently Suci & active for prayer."
    override val daftarHaidEmptyHistory = "No completed Haid / Suci history yet."
    override val daftarHaidDayBadge: (Int) -> String = { "Day $it" }
    override val daftarHaidStartDateLabel: (String) -> String = { "Started: $it" }
    override val daftarHaidEstSuciLabel: (String) -> String = { "15-Day Limit: $it" }
    override val daftarHaidMarkSuciBtn = "Mark Suci"
    override val daftarHaidOver15Warning = "⚠️ Exceeds 15 days (Istihadhah) - Mandi Suci required"
    override val daftarHaidTimelineTitle = "Haid Cycle Timeline (15-Day Standard)"
    override val daftarHaidTimelineStartNode = "Start"
    override val daftarHaidTimelineTodayNode: (Int) -> String = { "Day $it" }
    override val daftarHaidTimelineMaxNode = "15-Day Cap"
    override val daftarHaidBloodColorLabel = "Cycle Color & Fiqih Phase Indicator:"
    override val daftarHaidSuciConfirmTitle = "Confirm Suci & Mandi Wajib"
    override val daftarHaidSuciConfirmBody: (String, Int) -> String = { name, days ->
        "Mark $name as Suci after completing a $days-day cycle?"
    }
    override val daftarHaidSuciDateLabel = "Suci Date"
    override val daftarHaidSuciConfirmBtn = "Confirm Suci"

    override val daftarSuciTitle = "Daftar Suci (Completed Cycles)"
    override val daftarSuciSubtitle = "Archive of female students who completed Haid & resumed prayer"
    override val daftarSuciSearchHint = "Search student name or class..."
    override val daftarSuciEmpty = "No completed Haid / Suci history yet."
    override val daftarSuciBadgeDone = "Suci (Mandi Wajib)"
    override val daftarSuciBadgeAuto = "Auto Suci (>15 Days)"
    override val daftarSuciPeriodLabel: (String, String, Int) -> String = { start, end, days -> "$start to $end ($days days)" }
    override val daftarSuciMandiLabel = "Status: Purified / resumed prayer"

    override val fiqihHaidTitle = "Fiqih Haid Quick Guide"
    override val fiqihHaidSubtitle = "Quick reference for Asrama Putri Wali Asuh (Shafi'i School)"
    override val fiqihRule1Title = "1. Minimum Age for Haid"
    override val fiqihRule1Body = "The minimum age for menstruation (Haid) is approximately 9 lunar years."
    override val fiqihRule2Title = "2. Minimum & Maximum Haid Duration"
    override val fiqihRule2Body = "Minimum duration is 24 hours (1 day and 1 night). Maximum duration is 15 days and 15 nights (360 hours)."
    override val fiqihRule3Title = "3. Minimum Suci Interval"
    override val fiqihRule3Body = "The minimum clean (Suci) interval between two Haid cycles is 15 days and 15 nights."
    override val fiqihRule4Title = "4. Bleeding Beyond 15 Days (Istihadhah)"
    override val fiqihRule4Body = "Flow continuing past 15 days and 15 nights is Istihadhah; the student must perform Mandi Suci and resume obligatory prayers."
    override val fiqihRule5Title = "5. Mandi Wajib & Qadha Obligation"
    override val fiqihRule5Body = "Once flow stops, Mandi Wajib is required. Missed obligatory Puasa days must be made up (Qadha), while missed prayers are excused."

    override val scannerTitle = "Scan Student QR Card"
    override val scannerSubtitle = "Point camera at the QR Code on the Student ID Card"
    override val scannerManualHint = "Or enter NIS / NIK manually:"
    override val scannerManualLabel = "Enter Student NIS or NIK"
    override val scannerProcessBtn = "Find Student"
    override val scannerCameraPermTitle = "Camera Permission Required"
    override val scannerCameraPermDesc = "Allow camera access to scan QR Codes on Student ID Cards instantly."
    override val scannerCameraPermBtn = "Grant Camera Permission"
    override val scannerFlashToggleDesc = "Toggle Flash Light"
    override val scannerSwitchModeManual = "Manual Input"
    override val scannerSwitchModeCamera = "Camera Mode"
    override val scannerNotFoundMsg: (String) -> String = { "No student found matching code '$it' in this school." }
    override val scannerFoundHeader = "Student Identified ✓"
    override val scannerRecentHistoryBtn = "Open Health Book"
    override val scannerScanAgainBtn = "Scan Again"

    override val inputPuasaTitle = "Create Puasa Attendance Session"
    override val inputPuasaSubtitle = "Open registration & Sahur / Berbuka checklist"
    override val inputPuasaSessionNameLabel = "Session / Puasa Name *"
    override val inputPuasaSessionPlaceholder = "e.g., Puasa Sunnah Monday, Puasa Ramadhan Day 1"
    override val inputPuasaDateLabel = "Session Date"
    override val inputPuasaPresetLabel = "Quick Presets:"
    override val inputPuasaCreateBtn = "Create Puasa Session"

    override val daftarPuasaTitle = "Daftar Puasa Sessions"
    override val daftarPuasaSubtitle = "Manage Puasa sessions, Sahur & Berbuka checklists, and reports"
    override val daftarPuasaAddSessionBtn = "New Session"
    override val daftarPuasaEmpty = "No Puasa sessions created yet"
    override val daftarPuasaEmptyDesc = "Tap 'New Session' to start tracking Monday-Thursday, Arafah, or Ramadhan Puasa."
    override val daftarPuasaParticipantsCount: (Int) -> String = { "$it Participants" }
    override val daftarPuasaSahurCount: (Int, Int) -> String = { done, total -> "Sahur: $done/$total" }
    override val daftarPuasaBukaCount: (Int, Int) -> String = { done, total -> "Berbuka: $done/$total" }
    override val daftarPuasaOpenChecklistBtn = "Open Checklist"
    override val daftarPuasaPrintPdfBtn = "PDF"
    override val daftarPuasaShareWaBtn = "WA"

    override val checklistPuasaTitle: (String) -> String = { "Checklist: $it" }
    override val checklistPuasaSessionDate: (String) -> String = { "Date: $it" }
    override val checklistPuasaScanBtn = "Scan QR"
    override val checklistPuasaAddManualBtn = "Add Student"
    override val checklistPuasaFilterAll = "All"
    override val checklistPuasaSearchHint = "Search participant name..."
    override val checklistPuasaEmpty = "No students registered in this session yet."
    override val checklistPuasaSahurLabel = "Sahur"
    override val checklistPuasaBukaLabel = "Berbuka"
    override val checklistPuasaSahurDoneBy: (String) -> String = { "Sahur ✓ ($it)" }
    override val checklistPuasaBukaDoneBy: (String) -> String = { "Berbuka ✓ ($it)" }
    override val checklistPuasaSelectStudentTitle = "Register Student for Puasa"

    override val rankingPuasaTitle = "Top Puasa Ranking"
    override val rankingPuasaSubtitle = "Ranked by completed Sahur & Berbuka attendance"
    override val rankingPuasaFilterAll = "All"
    override val rankingPuasaPeriodAll = "All Time"
    override val rankingPuasaPeriodMonth = "This Month"
    override val rankingPuasaEmpty = "No Puasa attendance records available yet."
    override val rankingPuasaScoreFormat: (Int, Int, Int) -> String = { full, sahur, buka ->
        "$full Full Days • $sahur Sahur • $buka Berbuka"
    }
    override val rankingPuasaPrintPdfBtn = "Print Ranking PDF"
}
