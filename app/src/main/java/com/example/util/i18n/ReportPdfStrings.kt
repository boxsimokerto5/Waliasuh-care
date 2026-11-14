package com.example.util.i18n

/**
 * Strings for DateUtils, NotificationHelper, MonthlyReportDialog, PrintPdfDialog,
 * ShareWhatsAppDialog, and PdfReportGenerator (PDF headings, tables, WA summaries).
 */
interface ReportPdfStrings {
    // DateUtils & Notifications
    val dateTodayPrefix: (String) -> String
    val dateTomorrowPrefix: (String) -> String
    val notifChannelName: String
    val notifChannelDesc: String
    val notifRoomFormat: (String) -> String
    val notifRoomDefault: String
    val notifClassFormat: (String) -> String
    val notifClassDefault: String
    val notifFaskesDefault: String
    val notifTitleFormat: (String, String) -> String
    val notifShortFormat: (String, String, String) -> String
    val notifSchoolLine: (String) -> String
    val notifStudentLine: (String) -> String
    val notifClassRoomLine: (String, String) -> String
    val notifComplaintLine: (String) -> String
    val notifFaskesLine: (String) -> String
    val notifStatusLine: String
    val notifTestStudentName: String
    val notifTestComplaint: String

    // Monthly Report Dialog
    val monthlyTitle: String
    val monthlySubtitle: String
    val monthlyTotalRecords: String
    val monthlyBoysDorm: String
    val monthlyGirlsDorm: String
    val monthlyTopDiagnoses: String
    val monthlyShareWaBtn: String
    val monthlyPrintPdfBtn: String
    val monthlyEmptyPeriod: String

    // Print Pdf Dialog
    val printPdfTitle: String
    val printPdfSubtitle: String
    val printPdfScopeToday: String
    val printPdfScopeWeek: String
    val printPdfScopeMonth: String
    val printPdfScopePassport: String
    val printPdfSelectStudentLabel: String
    val printPdfGenerateBtn: String

    // Share WhatsApp Dialog
    val shareWaTitle: String
    val shareWaSubtitle: String
    val shareWaTypeDaily: String
    val shareWaTypePlan: String
    val shareWaTypeMonthly: String
    val shareWaPreviewLabel: String
    val shareWaCopyBtn: String
    val shareWaCopiedToast: String
    val shareWaSendBtn: String
    val shareWaChooserTitle: String
    val shareWaNotInstalledToast: String

    // PDF Report Generator (Headers, Tables, Labels, Toasts, Choosers)
    val pdfHealthDocTitle: String
    val pdfDatePrintPrefix: (String) -> String
    val pdfScopeTodayTitle: String
    val pdfScopeWeekTitle: String
    val pdfScopeMonthTitle: String
    val pdfStatsTitle: String
    val pdfStatsRow: (Int, Int, Int, Int) -> String
    val pdfColNo: String
    val pdfColStudentDorm: String
    val pdfColFaskesTime: String
    val pdfColComplaintDiag: String
    val pdfColMedicineAction: String
    val pdfColStatusControl: String
    val pdfEmptyTableMsg: String
    val pdfHealthSuccessToast: String
    val pdfHealthChooserTitle: String
    val pdfErrorToast: (String) -> String
    val pdfStatusDone: String
    val pdfStatusDeparted: String
    val pdfStatusPlanned: String
    val pdfStatusCancelled: String
    val pdfControlPrefix: (String) -> String
    val pdfControlNeeded: String
    val pdfControlDone: String
    val pdfControlNone: String

    // Student Health Passport PDF
    val pdfPassportTitle: String
    val pdfPassportPeriodMonth: (String) -> String
    val pdfPassportPeriodYear: (String) -> String
    val pdfPassportPeriodAll: String
    val pdfPassportStudentName: String
    val pdfPassportAsrama: String
    val pdfPassportNis: String
    val pdfPassportClass: String
    val pdfPassportGender: String
    val pdfPassportGenderMale: String
    val pdfPassportGenderFemale: String
    val pdfPassportBloodAge: (String, Int) -> String
    val pdfPassportFreqAlert: (Int, String) -> String
    val pdfPassportFreqNormal: (Int, String) -> String
    val pdfPassportFreqSub: (Int, Int) -> String
    val pdfPassportDetailHeader: (Int) -> String
    val pdfPassportEmptyMsg: String
    val pdfPassportVisitLine: (Int, String) -> String
    val pdfPassportFaskesPrefix: (String) -> String
    val pdfPassportComplaint: (String) -> String
    val pdfPassportDiagnosis: (String) -> String
    val pdfPassportAction: (String) -> String
    val pdfPassportPrescription: (String) -> String
    val pdfPassportControlEscort: (String, String) -> String
    val pdfPassportSuccessToast: String
    val pdfPassportChooserTitle: String

    // Haid & Fasting PDFs
    val pdfHaidTitle: String
    val pdfHaidSubtitle: (String, Int) -> String
    val pdfHaidColNameClass: String
    val pdfHaidColDormStart: String
    val pdfHaidColDuration: String
    val pdfHaidColEstSuci: String
    val pdfHaidColStatusNote: String
    val pdfHaidDayFormat: (Int) -> String
    val pdfHaidStatusOver15: String
    val pdfHaidStatusActive: String
    val pdfHaidStatusSuci: String
    val pdfHaidEmptyMsg: String
    val pdfHaidSuccessToast: String
    val pdfHaidChooserTitle: String

    val pdfPuasaSessionTitle: String
    val pdfPuasaSessionSubtitle: (String, String, Int) -> String
    val pdfPuasaColNameClass: String
    val pdfPuasaColDormJenjang: String
    val pdfPuasaColSahur: String
    val pdfPuasaColBuka: String
    val pdfPuasaColOfficer: String
    val pdfPuasaChecked: String
    val pdfPuasaUnchecked: String
    val pdfPuasaEmptyMsg: String
    val pdfPuasaSuccessToast: String
    val pdfPuasaChooserTitle: String

    val pdfPuasaRankingTitle: String
    val pdfPuasaRankingSubtitle: (String, String, Int) -> String
    val pdfPuasaRankingColRank: String
    val pdfPuasaRankingColNameClass: String
    val pdfPuasaRankingColDormJenjang: String
    val pdfPuasaRankingColSahurBuka: String
    val pdfPuasaRankingColScore: String
    val pdfPuasaRankingScoreSuffix: (Int) -> String
    val pdfPuasaRankingEmptyMsg: String
    val pdfPuasaRankingSuccessToast: String
    val pdfPuasaRankingChooserTitle: String

    // WhatsApp Summary Templates
    val waHealthHeaderDaily: String
    val waHealthHeaderPlan: String
    val waHealthHeaderMonthly: String
    val waHealthSchoolLine: (String) -> String
    val waHealthDateLine: (String) -> String
    val waHealthSectionStats: String
    val waHealthPlannedSection: String
    val waHealthCompletedSection: String
    val waHealthControlDueSection: String
    val waHealthTopDiagnosesSection: String
    val waHealthEmptySection: String
    val waHealthFooter: (String) -> String
    val waHaidHeader: String
    val waHaidEmpty: String
    val waHaidOver15Tag: String
    val waPuasaHeader: (String) -> String
    val waPuasaSummaryLine: (Int, Int, Int) -> String
    val waPuasaEmpty: String
}

object IdReportPdfStrings : ReportPdfStrings {
    override val dateTodayPrefix: (String) -> String = { "Hari ini ($it)" }
    override val dateTomorrowPrefix: (String) -> String = { "Besok ($it)" }
    override val notifChannelName = "Laporan Kesehatan Anak Asuh"
    override val notifChannelDesc = "Pemberitahuan resmi saat ada inputan anak asuh sakit atau jadwal pengobatan Puskesmas"
    override val notifRoomFormat: (String) -> String = { "Kamar $it" }
    override val notifRoomDefault = "Asrama"
    override val notifClassFormat: (String) -> String = { "Kelas $it" }
    override val notifClassDefault = "Siswa Sekolah Rakyat"
    override val notifFaskesDefault = "Puskesmas Rujukan"
    override val notifTitleFormat: (String, String) -> String = { badge, name -> "🏥 ${badge}Anak Asuh Sakit: $name" }
    override val notifShortFormat: (String, String, String) -> String = { cls, room, comp -> "$cls ($room) • Keluhan: $comp" }
    override val notifSchoolLine: (String) -> String = { "Sekolah: $it" }
    override val notifStudentLine: (String) -> String = { "Nama Siswa: $it" }
    override val notifClassRoomLine: (String, String) -> String = { cls, room -> "Jenjang/Kelas: $cls • $room" }
    override val notifComplaintLine: (String) -> String = { "Keluhan: $it" }
    override val notifFaskesLine: (String) -> String = { "Tujuan Rujukan: $it" }
    override val notifStatusLine = "Status: Terdaftar di sistem pemantauan Wali Asuh"
    override val notifTestStudentName = "Rama Aditia (Uji Coba)"
    override val notifTestComplaint = "Demam & Pusing (Tes Notifikasi Resmi)"

    override val monthlyTitle = "Rekapitulasi Bulanan"
    override val monthlySubtitle = "Statistik pemeriksaan, asrama, & diagnosa per bulan"
    override val monthlyTotalRecords = "Total Berobat"
    override val monthlyBoysDorm = "Asrama Putra"
    override val monthlyGirlsDorm = "Asrama Putri"
    override val monthlyTopDiagnoses = "Diagnosa Terbanyak Bulan Ini"
    override val monthlyShareWaBtn = "Kirim Rekap WA"
    override val monthlyPrintPdfBtn = "Cetak PDF Bulanan"
    override val monthlyEmptyPeriod = "Belum ada catatan berobat pada bulan ini."

    override val printPdfTitle = "Cetak Laporan PDF"
    override val printPdfSubtitle = "Pilih cakupan laporan resmi berstandar A4 dengan QR Tanda Tangan"
    override val printPdfScopeToday = "Laporan Harian (Hari Ini)"
    override val printPdfScopeWeek = "Laporan Mingguan (7 Hari Terakhir)"
    override val printPdfScopeMonth = "Laporan Bulanan"
    override val printPdfScopePassport = "Rekap Screening & Rekam Medis Per Anak"
    override val printPdfSelectStudentLabel = "Pilih Anak Asuh:"
    override val printPdfGenerateBtn = "Buat & Bagikan PDF"

    override val shareWaTitle = "Bagikan Ringkasan ke WhatsApp"
    override val shareWaSubtitle = "Pilih format pesan siap kirim ke grup Wali Asuh / Pembina"
    override val shareWaTypeDaily = "Laporan Harian"
    override val shareWaTypePlan = "Daftar Antrean"
    override val shareWaTypeMonthly = "Rekap Bulanan"
    override val shareWaPreviewLabel = "Pratinjau Pesan WhatsApp:"
    override val shareWaCopyBtn = "Salin Teks"
    override val shareWaCopiedToast = "Teks laporan disalin ke clipboard!"
    override val shareWaSendBtn = "Kirim WhatsApp"
    override val shareWaChooserTitle = "Kirim Laporan via WhatsApp"
    override val shareWaNotInstalledToast = "Aplikasi WhatsApp tidak ditemukan. Teks telah disalin."

    override val pdfHealthDocTitle = "LAPORAN PEMANTAUAN KESEHATAN & BEROBAT ANAK ASUH"
    override val pdfDatePrintPrefix: (String) -> String = { "Dicetak: $it" }
    override val pdfScopeTodayTitle = "LAPORAN HARIAN"
    override val pdfScopeWeekTitle = "LAPORAN MINGGUAN"
    override val pdfScopeMonthTitle = "LAPORAN BULANAN"
    override val pdfStatsTitle = "RINGKASAN STATISTIK:"
    override val pdfStatsRow: (Int, Int, Int, Int) -> String = { total, done, plan, ctrl ->
        "Total Data: $total   |   Selesai Berobat: $done   |   Rencana / Jalan: $plan   |   Jadwal Kontrol: $ctrl"
    }
    override val pdfColNo = "NO"
    override val pdfColStudentDorm = "NAMA SISWA & ASRAMA"
    override val pdfColFaskesTime = "FASKES & WAKTU"
    override val pdfColComplaintDiag = "KELUHAN & DIAGNOSA"
    override val pdfColMedicineAction = "TINDAKAN & OBAT"
    override val pdfColStatusControl = "STATUS & KONTROL"
    override val pdfEmptyTableMsg = "Tidak ada data pencatatan untuk periode yang dipilih."
    override val pdfHealthSuccessToast = "Laporan PDF berhasil dibuat!"
    override val pdfHealthChooserTitle = "Bagikan / Simpan Laporan PDF Kesehatan"
    override val pdfErrorToast: (String) -> String = { "Gagal membuat PDF: $it" }
    override val pdfStatusDone = "Selesai"
    override val pdfStatusDeparted = "Siap Jalan"
    override val pdfStatusPlanned = "Menunggu"
    override val pdfStatusCancelled = "Dibatalkan"
    override val pdfControlPrefix: (String) -> String = { "Kontrol: $it" }
    override val pdfControlNeeded = "Kontrol: Perlu"
    override val pdfControlDone = "Kontrol: Selesai ✓"
    override val pdfControlNone = "Kontrol: Tidak Perlu"

    override val pdfPassportTitle = "LEMBAR REKAPITULASI SCREENING & REKAM MEDIS ANAK ASUH"
    override val pdfPassportPeriodMonth: (String) -> String = { "PERIODE LAPORAN: ${it.uppercase()}" }
    override val pdfPassportPeriodYear: (String) -> String = { "PERIODE LAPORAN: TAHUN $it" }
    override val pdfPassportPeriodAll: String = "PERIODE LAPORAN: SELURUH RIWAYAT TERCATAT"
    override val pdfPassportStudentName = "Nama Anak Asuh :"
    override val pdfPassportAsrama = "Asrama Santri :"
    override val pdfPassportNis = "NIS / ID Siswa :"
    override val pdfPassportClass = "Kelas Pendidikan:"
    override val pdfPassportGender = "Jenis Kelamin :"
    override val pdfPassportGenderMale = "Laki-laki (Putra)"
    override val pdfPassportGenderFemale = "Perempuan (Putri)"
    override val pdfPassportBloodAge: (String, Int) -> String = { blood, age -> "Golongan Darah: $blood  |  Usia: $age Th" }
    override val pdfPassportFreqAlert: (Int, String) -> String = { cnt, period ->
        "PERHATIAN: Anak asuh ini tercatat $cnt KALI SAKIT / BEROBAT dalam $period."
    }
    override val pdfPassportFreqNormal: (Int, String) -> String = { cnt, period ->
        "RINGKASAN SCREENING: Anak asuh ini tercatat $cnt kali sakit / berobat dalam $period."
    }
    override val pdfPassportFreqSub: (Int, Int) -> String = { total, ctrl ->
        "Total Riwayat Screening Terdaftar: $total Data  •  Jadwal Kontrol Terbit: $ctrl Kali"
    }
    override val pdfPassportDetailHeader: (Int) -> String = { "RINCIAN PEMERIKSAAN & TINDAKAN MEDIS ($it Kunjungan):" }
    override val pdfPassportEmptyMsg = "Belum ada catatan riwayat berobat / screening untuk periode yang dipilih."
    override val pdfPassportVisitLine: (Int, String) -> String = { no, date -> "Kunjungan #$no  •  $date" }
    override val pdfPassportFaskesPrefix: (String) -> String = { "Faskes: $it" }
    override val pdfPassportComplaint: (String) -> String = { "• Keluhan Awal : $it" }
    override val pdfPassportDiagnosis: (String) -> String = { "• Diagnosa Dokter: $it" }
    override val pdfPassportAction: (String) -> String = { "• Tindakan Medis: $it" }
    override val pdfPassportPrescription: (String) -> String = { "• Resep / Obat : $it" }
    override val pdfPassportControlEscort: (String, String) -> String = { ctrl, escort ->
        "• Status Kontrol : $ctrl | Pendamping: $escort"
    }
    override val pdfPassportSuccessToast = "PDF Rekap Screening Kesehatan Anak berhasil dibuat!"
    override val pdfPassportChooserTitle = "Bagikan / Simpan Rekap Screening Kesehatan"

    override val pdfHaidTitle = "LAPORAN PEMANTAUAN HAID & BERSUCI SANTRI PUTRI"
    override val pdfHaidSubtitle: (String, Int) -> String = { date, count -> "Periode Cetak: $date  •  Total Catatan: $count Santri" }
    override val pdfHaidColNameClass = "NAMA & KELAS"
    override val pdfHaidColDormStart = "ASRAMA & MULAI"
    override val pdfHaidColDuration = "DURASI"
    override val pdfHaidColEstSuci = "ESTIMASI SUCI"
    override val pdfHaidColStatusNote = "STATUS & CATATAN"
    override val pdfHaidDayFormat: (Int) -> String = { "Hari ke-$it" }
    override val pdfHaidStatusOver15 = "Melebihi 15 Hari (Istihadhah)"
    override val pdfHaidStatusActive = "Sedang Haid"
    override val pdfHaidStatusSuci = "Sudah Suci"
    override val pdfHaidEmptyMsg = "Tidak ada data pemantauan haid pada periode ini."
    override val pdfHaidSuccessToast = "Laporan PDF Haid berhasil dibuat!"
    override val pdfHaidChooserTitle = "Bagikan / Simpan Laporan PDF Haid"

    override val pdfPuasaSessionTitle = "DAFTAR HADIR SAHUR & BERBUKA PUASA SANTRI"
    override val pdfPuasaSessionSubtitle: (String, String, Int) -> String = { title, date, count -> "$title • $date • Total: $count Peserta" }
    override val pdfPuasaColNameClass = "NAMA & KELAS"
    override val pdfPuasaColDormJenjang = "ASRAMA & JENJANG"
    override val pdfPuasaColSahur = "SAHUR"
    override val pdfPuasaColBuka = "BERBUKA"
    override val pdfPuasaColOfficer = "PENCATAT"
    override val pdfPuasaChecked = "✓ Sudah"
    override val pdfPuasaUnchecked = "Belum"
    override val pdfPuasaEmptyMsg = "Belum ada peserta puasa terdaftar pada sesi ini."
    override val pdfPuasaSuccessToast = "PDF Absen Puasa berhasil dibuat!"
    override val pdfPuasaChooserTitle = "Bagikan / Simpan PDF Absen Puasa"

    override val pdfPuasaRankingTitle = "LAPORAN PERINGKAT KERAJINAN PUASA SANTRI"
    override val pdfPuasaRankingSubtitle: (String, String, Int) -> String = { scope, date, count -> "$scope • Dicetak: $date • Total: $count Santri" }
    override val pdfPuasaRankingColRank = "NO"
    override val pdfPuasaRankingColNameClass = "NAMA SANTRI & KELAS"
    override val pdfPuasaRankingColDormJenjang = "ASRAMA & JENJANG"
    override val pdfPuasaRankingColSahurBuka = "SAHUR & BERBUKA"
    override val pdfPuasaRankingColScore = "TOTAL TUNTAS"
    override val pdfPuasaRankingScoreSuffix: (Int) -> String = { "$it Hari Tuntas" }
    override val pdfPuasaRankingEmptyMsg = "Belum ada data peringkat puasa pada filter ini."
    override val pdfPuasaRankingSuccessToast = "PDF Ranking Puasa berhasil dibuat!"
    override val pdfPuasaRankingChooserTitle = "Bagikan / Simpan PDF Ranking Puasa"

    override val waHealthHeaderDaily = "*LAPORAN HARIAN KESEHATAN ANAK ASUH*"
    override val waHealthHeaderPlan = "*DAFTAR RENCANA ANTREAN BEROBAT*"
    override val waHealthHeaderMonthly = "*REKAPITULASI BULANAN KESEHATAN*"
    override val waHealthSchoolLine: (String) -> String = { "Sekolah: *$it*" }
    override val waHealthDateLine: (String) -> String = { "Tanggal: $it" }
    override val waHealthSectionStats = "📊 *Statistik Ringkas:*"
    override val waHealthPlannedSection = "🚑 *Rencana & Sedang Jalan:*"
    override val waHealthCompletedSection = "✅ *Selesai Pemeriksaan:*"
    override val waHealthControlDueSection = "🔔 *Jadwal Kontrol Ulang:*"
    override val waHealthTopDiagnosesSection = "🩺 *Diagnosa Terbanyak:*"
    override val waHealthEmptySection = "_Tidak ada data pada bagian ini._"
    override val waHealthFooter: (String) -> String = { "_Dilaporkan oleh $it via Wali Asuh Care_" }
    override val waHaidHeader = "*PEMANTAUAN HAID SANTRI PUTRI*"
    override val waHaidEmpty = "Saat ini seluruh santri putri dalam keadaan suci."
    override val waHaidOver15Tag = "⚠️ >15 Hari (Istihadhah / Wajib Mandi Suci)"
    override val waPuasaHeader: (String) -> String = { "*LAPORAN ABSENSI PUASA: $it*" }
    override val waPuasaSummaryLine: (Int, Int, Int) -> String = { total, sahur, buka ->
        "Peserta: *$total* • Sahur: *$sahur* • Berbuka: *$buka*"
    }
    override val waPuasaEmpty = "_Belum ada peserta puasa tercatat pada sesi ini._"
}

object EnReportPdfStrings : ReportPdfStrings {
    override val dateTodayPrefix: (String) -> String = { "Today ($it)" }
    override val dateTomorrowPrefix: (String) -> String = { "Tomorrow ($it)" }
    override val notifChannelName = "Student Health Alerts"
    override val notifChannelDesc = "Official alerts when a student is reported sick or scheduled for Puskesmas visits"
    override val notifRoomFormat: (String) -> String = { "Room $it" }
    override val notifRoomDefault = "Asrama"
    override val notifClassFormat: (String) -> String = { "Class $it" }
    override val notifClassDefault = "Sekolah Rakyat Student"
    override val notifFaskesDefault = "Referral Puskesmas"
    override val notifTitleFormat: (String, String) -> String = { badge, name -> "🏥 ${badge}Student Sick Report: $name" }
    override val notifShortFormat: (String, String, String) -> String = { cls, room, comp -> "$cls ($room) • Complaint: $comp" }
    override val notifSchoolLine: (String) -> String = { "School: $it" }
    override val notifStudentLine: (String) -> String = { "Student: $it" }
    override val notifClassRoomLine: (String, String) -> String = { cls, room -> "Grade/Class: $cls • $room" }
    override val notifComplaintLine: (String) -> String = { "Complaint: $it" }
    override val notifFaskesLine: (String) -> String = { "Referral Destination: $it" }
    override val notifStatusLine = "Status: Registered in Wali Asuh monitoring system"
    override val notifTestStudentName = "Rama Aditia (Sample)"
    override val notifTestComplaint = "Fever & Dizziness (Official Notification Test)"

    override val monthlyTitle = "Monthly Health Recap"
    override val monthlySubtitle = "Treatment statistics, Asrama breakdown, & top diagnoses"
    override val monthlyTotalRecords = "Total Visits"
    override val monthlyBoysDorm = "Asrama Putra"
    override val monthlyGirlsDorm = "Asrama Putri"
    override val monthlyTopDiagnoses = "Top Diagnoses This Month"
    override val monthlyShareWaBtn = "Share WA Recap"
    override val monthlyPrintPdfBtn = "Print Monthly PDF"
    override val monthlyEmptyPeriod = "No medical records recorded for this month."

    override val printPdfTitle = "Print PDF Report"
    override val printPdfSubtitle = "Choose A4 official report scope with Wali Asuh QR Signature"
    override val printPdfScopeToday = "Daily Report (Today)"
    override val printPdfScopeWeek = "Weekly Report (Last 7 Days)"
    override val printPdfScopeMonth = "Monthly Report"
    override val printPdfScopePassport = "Per-Student Screening & Medical Recap"
    override val printPdfSelectStudentLabel = "Select Student:"
    override val printPdfGenerateBtn = "Generate & Share PDF"

    override val shareWaTitle = "Share Summary to WhatsApp"
    override val shareWaSubtitle = "Choose ready-to-send report format for Wali Asuh / Mentor groups"
    override val shareWaTypeDaily = "Daily Report"
    override val shareWaTypePlan = "Queue List"
    override val shareWaTypeMonthly = "Monthly Recap"
    override val shareWaPreviewLabel = "WhatsApp Message Preview:"
    override val shareWaCopyBtn = "Copy Text"
    override val shareWaCopiedToast = "Report text copied to clipboard!"
    override val shareWaSendBtn = "Send WhatsApp"
    override val shareWaChooserTitle = "Send Report via WhatsApp"
    override val shareWaNotInstalledToast = "WhatsApp is not installed. Text has been copied instead."

    override val pdfHealthDocTitle = "STUDENT HEALTH MONITORING & TREATMENT REPORT"
    override val pdfDatePrintPrefix: (String) -> String = { "Printed: $it" }
    override val pdfScopeTodayTitle = "DAILY REPORT"
    override val pdfScopeWeekTitle = "WEEKLY REPORT"
    override val pdfScopeMonthTitle = "MONTHLY REPORT"
    override val pdfStatsTitle = "SUMMARY STATISTICS:"
    override val pdfStatsRow: (Int, Int, Int, Int) -> String = { total, done, plan, ctrl ->
        "Total Records: $total   |   Completed: $done   |   Planned / Escort: $plan   |   Follow-ups: $ctrl"
    }
    override val pdfColNo = "NO"
    override val pdfColStudentDorm = "STUDENT & ASRAMA"
    override val pdfColFaskesTime = "FASKES & TIME"
    override val pdfColComplaintDiag = "COMPLAINT & DIAGNOSIS"
    override val pdfColMedicineAction = "ACTION & MEDICATION"
    override val pdfColStatusControl = "STATUS & CONTROL"
    override val pdfEmptyTableMsg = "No treatment records found for the selected period."
    override val pdfHealthSuccessToast = "PDF report generated successfully!"
    override val pdfHealthChooserTitle = "Share / Save Health PDF Report"
    override val pdfErrorToast: (String) -> String = { "Failed to create PDF: $it" }
    override val pdfStatusDone = "Completed"
    override val pdfStatusDeparted = "Ready to Go"
    override val pdfStatusPlanned = "Waiting"
    override val pdfStatusCancelled = "Cancelled"
    override val pdfControlPrefix: (String) -> String = { "Control: $it" }
    override val pdfControlNeeded = "Control: Needed"
    override val pdfControlDone = "Control: Done ✓"
    override val pdfControlNone = "Control: Not Needed"

    override val pdfPassportTitle = "STUDENT SCREENING & MEDICAL HISTORY SUMMARY SHEET"
    override val pdfPassportPeriodMonth: (String) -> String = { "REPORT PERIOD: ${it.uppercase()}" }
    override val pdfPassportPeriodYear: (String) -> String = { "REPORT PERIOD: YEAR $it" }
    override val pdfPassportPeriodAll: String = "REPORT PERIOD: ALL RECORDED HISTORY"
    override val pdfPassportStudentName = "Student Name :"
    override val pdfPassportAsrama = "Asrama :"
    override val pdfPassportNis = "NIS / Student ID :"
    override val pdfPassportClass = "Class / Grade:"
    override val pdfPassportGender = "Gender :"
    override val pdfPassportGenderMale = "Male (Putra)"
    override val pdfPassportGenderFemale = "Female (Putri)"
    override val pdfPassportBloodAge: (String, Int) -> String = { blood, age -> "Blood Type: $blood  |  Age: $age yrs" }
    override val pdfPassportFreqAlert: (Int, String) -> String = { cnt, period ->
        "ATTENTION: This student was recorded sick / treated $cnt TIMES in $period."
    }
    override val pdfPassportFreqNormal: (Int, String) -> String = { cnt, period ->
        "SCREENING SUMMARY: This student was recorded sick / treated $cnt times in $period."
    }
    override val pdfPassportFreqSub: (Int, Int) -> String = { total, ctrl ->
        "Total Linked Screening Records: $total Entries  •  Issued Follow-ups: $ctrl"
    }
    override val pdfPassportDetailHeader: (Int) -> String = { "EXAMINATION & MEDICAL TREATMENT DETAILS ($it Visits):" }
    override val pdfPassportEmptyMsg = "No screening or medical treatment records found for the selected period."
    override val pdfPassportVisitLine: (Int, String) -> String = { no, date -> "Visit #$no  •  $date" }
    override val pdfPassportFaskesPrefix: (String) -> String = { "Faskes: $it" }
    override val pdfPassportComplaint: (String) -> String = { "• Initial Complaint : $it" }
    override val pdfPassportDiagnosis: (String) -> String = { "• Doctor Diagnosis: $it" }
    override val pdfPassportAction: (String) -> String = { "• Medical Action: $it" }
    override val pdfPassportPrescription: (String) -> String = { "• Prescription / Meds : $it" }
    override val pdfPassportControlEscort: (String, String) -> String = { ctrl, escort ->
        "• Control Status : $ctrl | Escort: $escort"
    }
    override val pdfPassportSuccessToast = "Student Screening Recap PDF generated!"
    override val pdfPassportChooserTitle = "Share / Save Student Screening Recap PDF"

    override val pdfHaidTitle = "FEMALE STUDENT HAID & SUCI MONITORING REPORT"
    override val pdfHaidSubtitle: (String, Int) -> String = { date, count -> "Printed: $date  •  Total Records: $count Students" }
    override val pdfHaidColNameClass = "NAME & CLASS"
    override val pdfHaidColDormStart = "ASRAMA & START"
    override val pdfHaidColDuration = "DURATION"
    override val pdfHaidColEstSuci = "ESTIMATED SUCI"
    override val pdfHaidColStatusNote = "STATUS & NOTES"
    override val pdfHaidDayFormat: (Int) -> String = { "Day $it" }
    override val pdfHaidStatusOver15 = "Over 15 Days (Istihadhah)"
    override val pdfHaidStatusActive = "Active Haid"
    override val pdfHaidStatusSuci = "Suci"
    override val pdfHaidEmptyMsg = "No Haid monitoring data found for this period."
    override val pdfHaidSuccessToast = "Haid PDF report generated!"
    override val pdfHaidChooserTitle = "Share / Save Haid PDF Report"

    override val pdfPuasaSessionTitle = "STUDENT PUASA ATTENDANCE (SAHUR & BERBUKA)"
    override val pdfPuasaSessionSubtitle: (String, String, Int) -> String = { title, date, count -> "$title • $date • Total: $count Participants" }
    override val pdfPuasaColNameClass = "NAME & CLASS"
    override val pdfPuasaColDormJenjang = "ASRAMA & LEVEL"
    override val pdfPuasaColSahur = "SAHUR"
    override val pdfPuasaColBuka = "BERBUKA"
    override val pdfPuasaColOfficer = "RECORDED BY"
    override val pdfPuasaChecked = "✓ Checked"
    override val pdfPuasaUnchecked = "Pending"
    override val pdfPuasaEmptyMsg = "No students registered in this Puasa session yet."
    override val pdfPuasaSuccessToast = "Puasa attendance PDF generated!"
    override val pdfPuasaChooserTitle = "Share / Save Puasa Attendance PDF"

    override val pdfPuasaRankingTitle = "STUDENT PUASA CONSISTENCY RANKING REPORT"
    override val pdfPuasaRankingSubtitle: (String, String, Int) -> String = { scope, date, count -> "$scope • Printed: $date • Total: $count Students" }
    override val pdfPuasaRankingColRank = "NO"
    override val pdfPuasaRankingColNameClass = "STUDENT & CLASS"
    override val pdfPuasaRankingColDormJenjang = "ASRAMA & LEVEL"
    override val pdfPuasaRankingColSahurBuka = "SAHUR & BERBUKA"
    override val pdfPuasaRankingColScore = "FULL DAYS"
    override val pdfPuasaRankingScoreSuffix: (Int) -> String = { "$it Full Days" }
    override val pdfPuasaRankingEmptyMsg = "No Puasa ranking data found for this filter."
    override val pdfPuasaRankingSuccessToast = "Puasa Ranking PDF generated!"
    override val pdfPuasaRankingChooserTitle = "Share / Save Puasa Ranking PDF"

    override val waHealthHeaderDaily = "*DAILY STUDENT HEALTH REPORT*"
    override val waHealthHeaderPlan = "*TREATMENT ESCORT QUEUE LIST*"
    override val waHealthHeaderMonthly = "*MONTHLY HEALTH RECAP*"
    override val waHealthSchoolLine: (String) -> String = { "School: *$it*" }
    override val waHealthDateLine: (String) -> String = { "Date: $it" }
    override val waHealthSectionStats = "📊 *Quick Statistics:*"
    override val waHealthPlannedSection = "🚑 *Planned & Ready to Go:*"
    override val waHealthCompletedSection = "✅ *Completed Examinations:*"
    override val waHealthControlDueSection = "🔔 *Follow-up Schedules:*"
    override val waHealthTopDiagnosesSection = "🩺 *Top Diagnoses:*"
    override val waHealthEmptySection = "_No records in this section._"
    override val waHealthFooter: (String) -> String = { "_Reported by $it via Wali Asuh Care_" }
    override val waHaidHeader = "*FEMALE STUDENT HAID MONITORING*"
    override val waHaidEmpty = "All female students are currently Suci."
    override val waHaidOver15Tag = "⚠️ >15 Days (Istihadhah / Mandi Suci Required)"
    override val waPuasaHeader: (String) -> String = { "*PUASA ATTENDANCE REPORT: $it*" }
    override val waPuasaSummaryLine: (Int, Int, Int) -> String = { total, sahur, buka ->
        "Participants: *$total* • Sahur: *$sahur* • Berbuka: *$buka*"
    }
    override val waPuasaEmpty = "_No Puasa participants recorded for this session yet._"
}
