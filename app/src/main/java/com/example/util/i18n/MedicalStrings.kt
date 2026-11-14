package com.example.util.i18n

/**
 * Strings for Dashboard, PlanScreen, MedicalRecordScreen, CompletedHistoryScreen,
 * FaskesInfoScreen, FaskesReferralSheet, StatusBadges, and Medical Dialogs.
 */
interface MedicalStrings {
    // Status Badges & Helper Labels
    val statusWaiting: String
    val statusDeparted: String
    val statusCompleted: String
    val statusCancelled: String
    val faskesUksFallback: String
    val faskesPuskesmasFallback: String
    val controlBadgeDone: String
    val controlBadgeDatePrefix: String
    val controlBadgeNeeded: String
    val controlBadgeNotNeeded: String
    val controlBadgePrefix: String

    // Quick Menu Tiles (Pages 1-4)
    val menuHeaderTitle: String
    val menuHeaderPageLabel: (Int, Int) -> String
    val menuPageDesc1: String
    val menuPageDesc2: String
    val menuPageDesc3: String
    val menuPageDesc4: String
    val tileCreatePlan: String
    val tileDirectTreatment: String
    val tileShareWa: String
    val tilePrintPdf: String
    val tileStudentList: String
    val tileMonthlyRecap: String
    val tileReferralInfo: String
    val tileHistory: String
    val tileInputHaid: String
    val tileHaidList: String
    val tileFiqihHaid: String
    val tileSuciList: String
    val tileScanStudentCard: String
    val tilePrintStudentCard: String
    val tileFastingChecklist: String
    val tileFastingRanking: String

    // Dashboard Screen
    val dashTopBarTitle: String
    val dashTodayScheduleTitle: String
    val dashSeeAllBtn: String
    val dashEmptySchedule: String
    val dashEmptyScheduleDesc: String
    val dashPlanSectionTitle: String
    val dashWaitingSuffix: String
    val dashNoPlansYet: String
    val dashControlDueTitle: String
    val dashNeedsControlNow: String
    val dashControlDateLabel: (String) -> String
    val dashControlMarkDone: String
    val dashInpatientTitle: String
    val dashInpatientBadge: String
    val dashInpatientRoomLabel: (String) -> String
    val dashInpatientMarkHome: String
    val dashQuickActionCreatePlan: String
    val dashQuickActionDirectTreat: String
    val dashStatsTitle: String
    val dashStatPlanned: String
    val dashStatOnTheWay: String
    val dashStatCompleted: String
    val dashStatMonthlyTotal: String
    val dashFaskesBreakdownTitle: String
    val dashHealthAlertBanner: (Int) -> String
    val dashFrequentSickAlertTitle: String
    val dashFrequentSickAlertDesc: (String, Int) -> String
    val dashPlanCountSuffix: (Int) -> String

    // Plan Screen
    val planTitle: String
    val planSubtitle: String
    val planAddBtn: String
    val planFilterAll: String
    val planFilterWaiting: String
    val planFilterDeparted: String
    val planFilterToday: String
    val planFilterUpcoming: String
    val planEmptyTitle: String
    val planEmptyDesc: String
    val planCardTimeLabel: (String) -> String
    val planCardEscortLabel: (String) -> String
    val planCardNotesLabel: (String) -> String
    val planActionDepart: String
    val planActionInputResult: String
    val planActionEdit: String
    val planActionCancel: String
    val planCancelConfirmTitle: String
    val planCancelConfirmBody: (String) -> String
    val planCancelConfirmBtn: String
    val planCreatedByPrefix: (String) -> String
    val planEditedByPrefix: (String) -> String

    // Medical Record Screen
    val medTitle: String
    val medSubtitle: String
    val medSearchPlaceholder: String
    val medTabRecords: String
    val medTabInpatient: String
    val medTabControls: String
    val medTabPassports: String
    val medFilterAll: String
    val medFilterToday: String
    val medFilterThisMonth: String
    val medSortNewest: String
    val medSortFrequent: String
    val medEmptyTitle: String
    val medEmptyDesc: String
    val medInpatientEmptyTitle: String
    val medInpatientEmptyDesc: String
    val medControlEmptyTitle: String
    val medControlEmptyDesc: String
    val medDiagnosisPrefix: String
    val medActionsPrefix: String
    val medPrescriptionPrefix: String
    val medEscortPrefix: String
    val medDischargeDialogTitle: String
    val medDischargeDialogBody: (String, String) -> String
    val medDischargeFinalDiagLabel: String
    val medDischargeFinalPrescLabel: String
    val medDischargeFinalPrescPlaceholder: String
    val medDischargeNeedControlSwitch: String
    val medDischargeIn3Days: String
    val medDischargeIn7Days: String
    val medDischargeNotesLabel: String
    val medDischargeNotesPlaceholder: String
    val medDischargeConfirmBtn: String
    val passportBloodTypeLabel: (String, String) -> String
    val passportAlertHighFreq: String
    val passportAlertNormal: String
    val passportMonthlyFreqText: (Int) -> String
    val passportTotalRecordsText: (Int) -> String
    val passportPrintPdfBtn: String
    val passportHistoryHeader: (Int) -> String
    val passportHistoryEmpty: String
    val passportVisitTitle: (Int, String) -> String
    val passportComplaintPrefix: String
    val passportWaReportBtn: String

    // Completed History Screen
    val historyTitle: String
    val historySubtitle: String
    val historyEmptyTitle: String
    val historyEmptyDesc: String
    val historyStatTotal: String
    val historyStatThisMonth: String

    // Faskes Info & Referral Sheet
    val faskesTitle: String
    val faskesSubtitle: String
    val faskesPrimaryTag: String
    val faskesPartnerTag: String
    val faskesAdditionalTag: String
    val faskesAddressLabel: String
    val faskesPhoneNotSet: String
    val faskesCallWaBtn: String
    val faskesCopyPhoneToast: (String) -> String
    val faskesGuideTitle: String
    val faskesGuideStep1: String
    val faskesGuideStep2: String
    val faskesGuideStep3: String
    val faskesReferralTitle: String
    val faskesReferralSubtitle: String
    val faskesOpenInMapHint: String

    // Medical Dialogs (CreatePlan, DepartReferral, DirectTreatment, InputResult, SearchStudent)
    val createPlanTitle: String
    val editPlanTitle: String
    val selectStudentLabel: String
    val selectStudentPlaceholder: String
    val searchStudentHint: String
    val faskesDestLabel: String
    val complaintLabel: String
    val complaintPlaceholder: String
    val planDateLabel: String
    val departureTimeLabel: String
    val escortShiftLabel: String
    val shiftMorningLabel: String
    val shiftAfternoonLabel: String
    val shiftNightLabel: String
    val shiftAllLabel: String
    val notesOptionalLabel: String
    val notesPlanPlaceholder: String
    val savePlanBtn: String
    val updatePlanBtn: String
    val validationSelectStudentAndComplaint: String

    val departDialogTitle: String
    val departDialogSubtitle: (String) -> String
    val departConfirmBtn: String

    val directTreatTitle: String
    val directTreatSubtitle: String
    val diagnosisLabel: String
    val diagnosisPlaceholder: String
    val actionsTakenLabel: String
    val actionsTakenPlaceholder: String
    val prescriptionLabel: String
    val prescriptionPlaceholder: String
    val isInpatientSwitch: String
    val inpatientRoomLabel: String
    val inpatientRoomPlaceholder: String
    val needsControlSwitch: String
    val controlDateLabel: String
    val saveDirectTreatBtn: String

    val inputResultTitle: String
    val inputResultSubtitle: (String, String) -> String
    val saveResultBtn: String

    val searchStudentDialogTitle: String
    val searchStudentEmpty: String
    val searchStudentInputHint: String
    val studentClassDormFormat: (String, String) -> String
}

object IdMedicalStrings : MedicalStrings {
    override val statusWaiting = "MENUNGGU"
    override val statusDeparted = "SIAP JALAN"
    override val statusCompleted = "SELESAI"
    override val statusCancelled = "DIBATALKAN"
    override val faskesUksFallback = "UKS"
    override val faskesPuskesmasFallback = "Puskesmas"
    override val controlBadgeDone = "Kontrol: Selesai ✓"
    override val controlBadgeDatePrefix = "Kontrol:"
    override val controlBadgeNeeded = "Perlu Kontrol"
    override val controlBadgeNotNeeded = "Tidak Perlu Kontrol"
    override val controlBadgePrefix = "Kontrol:"

    override val menuHeaderTitle = "Menu Cepat"
    override val menuHeaderPageLabel: (Int, Int) -> String = { p, total -> "Hal $p/$total" }
    override val menuPageDesc1 = "Layanan Kesehatan & Rencana"
    override val menuPageDesc2 = "Data Siswa & Laporan"
    override val menuPageDesc3 = "Pemantauan Fiqih Haid"
    override val menuPageDesc4 = "Kartu Santri & Ibadah Puasa"
    override val tileCreatePlan = "Buat Rencana"
    override val tileDirectTreatment = "Input Berobat"
    override val tileShareWa = "Share WA"
    override val tilePrintPdf = "Cetak PDF"
    override val tileStudentList = "Daftar Siswa"
    override val tileMonthlyRecap = "Rekap Bulan"
    override val tileReferralInfo = "Info Faskes"
    override val tileHistory = "Riwayat"
    override val tileInputHaid = "Input Haid"
    override val tileHaidList = "Daftar Haid"
    override val tileFiqihHaid = "Fiqih Haid"
    override val tileSuciList = "Daftar Suci"
    override val tileScanStudentCard = "Scan Kartu"
    override val tilePrintStudentCard = "Cetak Kartu"
    override val tileFastingChecklist = "Absen Puasa"
    override val tileFastingRanking = "Ranking Puasa"

    override val dashTopBarTitle = "Beranda Kesehatan"
    override val dashTodayScheduleTitle = "Jadwal Hari Ini"
    override val dashSeeAllBtn = "Lihat Semua"
    override val dashEmptySchedule = "Belum ada antrean berobat hari ini"
    override val dashEmptyScheduleDesc = "Semua anak asuh dalam kondisi sehat atau sudah tertangani."
    override val dashPlanSectionTitle = "Rencana Pengantaran"
    override val dashWaitingSuffix = "Menunggu"
    override val dashNoPlansYet = "Belum ada rencana pengantaran."
    override val dashControlDueTitle = "Jadwal Kontrol Ulang"
    override val dashNeedsControlNow = "Jatuh Tempo"
    override val dashControlDateLabel: (String) -> String = { "Jadwal Kontrol: $it" }
    override val dashControlMarkDone = "Tandai Selesai"
    override val dashInpatientTitle = "Sedang Rawat Inap"
    override val dashInpatientBadge = "Rawat Inap"
    override val dashInpatientRoomLabel: (String) -> String = { "Ruang: $it" }
    override val dashInpatientMarkHome = "Konfirmasi Pulang"
    override val dashQuickActionCreatePlan = "Rencana Berobat"
    override val dashQuickActionDirectTreat = "Input Berobat Langsung"
    override val dashStatsTitle = "Statistik Kesehatan"
    override val dashStatPlanned = "Direncanakan"
    override val dashStatOnTheWay = "Siap Jalan"
    override val dashStatCompleted = "Selesai"
    override val dashStatMonthlyTotal = "Total Bulan Ini"
    override val dashFaskesBreakdownTitle = "Distribusi Fasilitas Kesehatan"
    override val dashHealthAlertBanner: (Int) -> String = { "$it anak tercatat berobat >= 2x bulan ini" }
    override val dashFrequentSickAlertTitle = "Perlu Perhatian Khusus"
    override val dashFrequentSickAlertDesc: (String, Int) -> String = { name, count -> "$name tercatat sakit $count kali bulan ini" }
    override val dashPlanCountSuffix: (Int) -> String = { "$it Antrean" }

    override val planTitle = "Rencana Berobat"
    override val planSubtitle = "Kelola antrean & pengantaran ke Faskes"
    override val planAddBtn = "Buat Rencana"
    override val planFilterAll = "Semua"
    override val planFilterWaiting = "Menunggu"
    override val planFilterDeparted = "Siap Jalan"
    override val planFilterToday = "Hari Ini"
    override val planFilterUpcoming = "Mendatang"
    override val planEmptyTitle = "Tidak ada rencana sesuai filter"
    override val planEmptyDesc = "Tambahkan rencana pengantaran baru dengan tombol + di atas."
    override val planCardTimeLabel: (String) -> String = { "Berangkat $it WIB" }
    override val planCardEscortLabel: (String) -> String = { "Pengantar: $it" }
    override val planCardNotesLabel: (String) -> String = { "Catatan: $it" }
    override val planActionDepart = "Berangkat"
    override val planActionInputResult = "Input Hasil"
    override val planActionEdit = "Ubah"
    override val planActionCancel = "Batalkan"
    override val planCancelConfirmTitle = "Batalkan Rencana Berobat?"
    override val planCancelConfirmBody: (String) -> String = { "Rencana pengantaran untuk $it akan dibatalkan." }
    override val planCancelConfirmBtn = "Ya, Batalkan"
    override val planCreatedByPrefix: (String) -> String = { "Dibuat: $it" }
    override val planEditedByPrefix: (String) -> String = { "Diedit: $it" }

    override val medTitle = "Rekam Medis & Kontrol"
    override val medSubtitle = "Riwayat diagnosa, rawat inap, & buku sehat santri"
    override val medSearchPlaceholder = "Cari nama siswa, keluhan, atau diagnosa..."
    override val medTabRecords = "Semua Rekam"
    override val medTabInpatient = "Rawat Inap"
    override val medTabControls = "Jadwal Kontrol"
    override val medTabPassports = "Buku Sehat"
    override val medFilterAll = "Semua"
    override val medFilterToday = "Hari Ini"
    override val medFilterThisMonth = "Bulan Ini"
    override val medSortNewest = "Terbaru"
    override val medSortFrequent = "Sering Sakit"
    override val medEmptyTitle = "Belum ada rekam medis"
    override val medEmptyDesc = "Catatan hasil pemeriksaan UKS / Puskesmas akan tampil di sini."
    override val medInpatientEmptyTitle = "Tidak ada siswa rawat inap"
    override val medInpatientEmptyDesc = "Semua siswa sedang berada di asrama dalam kondisi stabil."
    override val medControlEmptyTitle = "Tidak ada jadwal kontrol aktif"
    override val medControlEmptyDesc = "Jadwal kontrol ulang dari dokter akan tercatat otomatis di sini."
    override val medDiagnosisPrefix = "Diagnosa:"
    override val medActionsPrefix = "Tindakan:"
    override val medPrescriptionPrefix = "Obat:"
    override val medEscortPrefix = "Pendamping:"
    override val medDischargeDialogTitle = "Konfirmasi Siswa Pulang Rawat Inap"
    override val medDischargeDialogBody: (String, String) -> String = { name, faskes ->
        "Konfirmasi $name telah pulang dari $faskes. Anda dapat melengkapi diagnosa akhir atau resep pulang jika diperlukan."
    }
    override val medDischargeFinalDiagLabel = "Diagnosa Akhir"
    override val medDischargeFinalPrescLabel = "Resep Obat Pulang (Opsional)"
    override val medDischargeFinalPrescPlaceholder = "misal: Paracetamol 3x1, Vitamin"
    override val medDischargeNeedControlSwitch = "Perlu Kontrol Ulang?"
    override val medDischargeIn3Days = "+3 Hari"
    override val medDischargeIn7Days = "+1 Minggu"
    override val medDischargeNotesLabel = "Catatan Pulang (Opsional)"
    override val medDischargeNotesPlaceholder = "misal: Istirahat 2 hari di asrama"
    override val medDischargeConfirmBtn = "Konfirmasi Pulang"
    override val passportBloodTypeLabel: (String, String) -> String = { dorm, blood -> "Asrama: $dorm • Gol. Darah: $blood" }
    override val passportAlertHighFreq = "⚠️ Perhatian Kesehatan Anak:"
    override val passportAlertNormal = "🩺 Ringkasan Screening:"
    override val passportMonthlyFreqText: (Int) -> String = { "Anak asuh ini tercatat $it kali sakit / berobat dalam bulan berjalan." }
    override val passportTotalRecordsText: (Int) -> String = { "Total seluruh riwayat screening kesehatan terikat: $it catatan." }
    override val passportPrintPdfBtn = "Cetak Rekap Screening PDF (Bulan / Tahun)"
    override val passportHistoryHeader: (Int) -> String = { "Riwayat Screening & Pengobatan Terikat ($it Kunjungan):" }
    override val passportHistoryEmpty = "Belum ada catatan rekam medis atau screening untuk anak asuh ini."
    override val passportVisitTitle: (Int, String) -> String = { no, date -> "Kunjungan #$no • $date" }
    override val passportComplaintPrefix = "Keluhan:"
    override val passportWaReportBtn = "Laporan WA"

    override val historyTitle = "Riwayat Selesai"
    override val historySubtitle = "Arsip pemeriksaan & rujukan yang telah tuntas"
    override val historyEmptyTitle = "Belum ada riwayat selesai"
    override val historyEmptyDesc = "Semua pemeriksaan yang telah diselesaikan akan tersimpan di sini."
    override val historyStatTotal = "Total Tuntas"
    override val historyStatThisMonth = "Bulan Ini"

    override val faskesTitle = "Info Fasilitas Kesehatan"
    override val faskesSubtitle = "Daftar faskes rujukan & kontak cepat sekolah"
    override val faskesPrimaryTag = "Faskes Tingkat 1 (Internal)"
    override val faskesPartnerTag = "Faskes Mitra Utama"
    override val faskesAdditionalTag = "Faskes Rujukan Tambahan"
    override val faskesAddressLabel = "Wilayah Layanan Sekolah"
    override val faskesPhoneNotSet = "Nomor kontak belum diatur oleh Admin"
    override val faskesCallWaBtn = "Salin Nomor"
    override val faskesCopyPhoneToast: (String) -> String = { "Nomor $it disalin ke clipboard" }
    override val faskesGuideTitle = "Alur Rujukan Cepat Wali Asuh"
    override val faskesGuideStep1 = "1. Lakukan pemeriksaan awal di UKS Sekolah bila keluhan ringan."
    override val faskesGuideStep2 = "2. Buat Rencana Berobat atau pilih Berangkat ke Puskesmas mitra."
    override val faskesGuideStep3 = "3. Input hasil diagnosa & obat setelah kembali ke asrama."
    override val faskesReferralTitle = "Direktori Rujukan Kesehatan"
    override val faskesReferralSubtitle = "Kontak cepat & daftar fasilitas rujukan terdaftar"
    override val faskesOpenInMapHint = "Ketuk untuk menyalin nomor atau lihat detail"

    override val createPlanTitle = "Buat Rencana Pengobatan"
    override val editPlanTitle = "Ubah Rencana Pengobatan"
    override val selectStudentLabel = "Pilih Anak Asuh"
    override val selectStudentPlaceholder = "Ketuk untuk memilih siswa..."
    override val searchStudentHint = "Cari nama atau NIS siswa..."
    override val faskesDestLabel = "Tujuan Faskes"
    override val complaintLabel = "Keluhan Awal"
    override val complaintPlaceholder = "misal: Demam tinggi sejak malam, pusing..."
    override val planDateLabel = "Tanggal Rencana"
    override val departureTimeLabel = "Jam Berangkat (WIB)"
    override val escortShiftLabel = "Petugas Pengantar / Shift"
    override val shiftMorningLabel = "Shift Pagi"
    override val shiftAfternoonLabel = "Shift Sore"
    override val shiftNightLabel = "Shift Malam"
    override val shiftAllLabel = "Semua Shift"
    override val notesOptionalLabel = "Catatan Tambahan (Opsional)"
    override val notesPlanPlaceholder = "misal: Sudah diberi kompres hangat"
    override val savePlanBtn = "Simpan Rencana"
    override val updatePlanBtn = "Perbarui Rencana"
    override val validationSelectStudentAndComplaint = "Pilih siswa dan isi keluhan terlebih dahulu."

    override val departDialogTitle = "Konfirmasi Keberangkatan"
    override val departDialogSubtitle: (String) -> String = { "Antar $it menuju fasilitas kesehatan tujuan:" }
    override val departConfirmBtn = "Konfirmasi Berangkat"

    override val directTreatTitle = "Input Berobat Langsung"
    override val directTreatSubtitle = "Catat siswa yang langsung diperiksa & selesai berobat"
    override val diagnosisLabel = "Diagnosa Dokter / Nakes"
    override val diagnosisPlaceholder = "misal: Febris / Infeksi saluran napas ringan"
    override val actionsTakenLabel = "Tindakan Medis"
    override val actionsTakenPlaceholder = "misal: Pemberian obat oral & istirahat"
    override val prescriptionLabel = "Resep / Obat Diberikan"
    override val prescriptionPlaceholder = "misal: Paracetamol 500mg 3x1, Vitamin C"
    override val isInpatientSwitch = "Siswa Dirawat Inap (Opname)?"
    override val inpatientRoomLabel = "Ruang / Kamar Rawat Inap"
    override val inpatientRoomPlaceholder = "misal: Ruang Mawar No. 4"
    override val needsControlSwitch = "Perlu Jadwal Kontrol Ulang?"
    override val controlDateLabel = "Tanggal Kontrol Ulang"
    override val saveDirectTreatBtn = "Simpan Rekam Berobat"

    override val inputResultTitle = "Input Hasil Pemeriksaan"
    override val inputResultSubtitle: (String, String) -> String = { name, faskes -> "$name • $faskes" }
    override val saveResultBtn = "Simpan Hasil Medis"

    override val searchStudentDialogTitle = "Pilih Anak Asuh"
    override val searchStudentEmpty = "Tidak ada siswa yang sesuai pencarian."
    override val searchStudentInputHint = "Ketik nama, NIS, atau kamar..."
    override val studentClassDormFormat: (String, String) -> String = { cls, dorm -> "$cls • $dorm" }
}

object EnMedicalStrings : MedicalStrings {
    override val statusWaiting = "WAITING"
    override val statusDeparted = "READY TO GO"
    override val statusCompleted = "COMPLETED"
    override val statusCancelled = "CANCELLED"
    override val faskesUksFallback = "UKS"
    override val faskesPuskesmasFallback = "Puskesmas"
    override val controlBadgeDone = "Follow-up: Done ✓"
    override val controlBadgeDatePrefix = "Follow-up:"
    override val controlBadgeNeeded = "Follow-up Needed"
    override val controlBadgeNotNeeded = "No Follow-up Needed"
    override val controlBadgePrefix = "Follow-up:"

    override val menuHeaderTitle = "Quick Menu"
    override val menuHeaderPageLabel: (Int, Int) -> String = { p, total -> "Page $p/$total" }
    override val menuPageDesc1 = "Health Services & Plans"
    override val menuPageDesc2 = "Student Data & Reports"
    override val menuPageDesc3 = "Fiqih Haid Monitoring"
    override val menuPageDesc4 = "Student ID Cards & Puasa"
    override val tileCreatePlan = "Create Plan"
    override val tileDirectTreatment = "Log Treatment"
    override val tileShareWa = "Share WA"
    override val tilePrintPdf = "Print PDF"
    override val tileStudentList = "Students"
    override val tileMonthlyRecap = "Monthly Recap"
    override val tileReferralInfo = "Faskes Info"
    override val tileHistory = "History"
    override val tileInputHaid = "Input Haid"
    override val tileHaidList = "Daftar Haid"
    override val tileFiqihHaid = "Fiqih Haid"
    override val tileSuciList = "Daftar Suci"
    override val tileScanStudentCard = "Scan Card"
    override val tilePrintStudentCard = "Print Cards"
    override val tileFastingChecklist = "Puasa Check"
    override val tileFastingRanking = "Puasa Rank"

    override val dashTopBarTitle = "Health Dashboard"
    override val dashTodayScheduleTitle = "Today's Schedule"
    override val dashSeeAllBtn = "See All"
    override val dashEmptySchedule = "No treatment queue today"
    override val dashEmptyScheduleDesc = "All students are healthy or already treated."
    override val dashPlanSectionTitle = "Escort Plans"
    override val dashWaitingSuffix = "Waiting"
    override val dashNoPlansYet = "No treatment plans yet."
    override val dashControlDueTitle = "Follow-up Visits"
    override val dashNeedsControlNow = "Due Today"
    override val dashControlDateLabel: (String) -> String = { "Follow-up Date: $it" }
    override val dashControlMarkDone = "Mark Done"
    override val dashInpatientTitle = "Currently Hospitalized"
    override val dashInpatientBadge = "Inpatient"
    override val dashInpatientRoomLabel: (String) -> String = { "Room: $it" }
    override val dashInpatientMarkHome = "Confirm Discharge"
    override val dashQuickActionCreatePlan = "Treatment Plan"
    override val dashQuickActionDirectTreat = "Log Direct Treatment"
    override val dashStatsTitle = "Health Statistics"
    override val dashStatPlanned = "Planned"
    override val dashStatOnTheWay = "Ready to Go"
    override val dashStatCompleted = "Completed"
    override val dashStatMonthlyTotal = "Total This Month"
    override val dashFaskesBreakdownTitle = "Health Facility Distribution"
    override val dashHealthAlertBanner: (Int) -> String = { "$it students treated >= 2x this month" }
    override val dashFrequentSickAlertTitle = "Needs Special Attention"
    override val dashFrequentSickAlertDesc: (String, Int) -> String = { name, count -> "$name was treated $count times this month" }
    override val dashPlanCountSuffix: (Int) -> String = { "$it Queued" }

    override val planTitle = "Treatment Plans"
    override val planSubtitle = "Manage health facility queues & escorts"
    override val planAddBtn = "Create Plan"
    override val planFilterAll = "All"
    override val planFilterWaiting = "Waiting"
    override val planFilterDeparted = "Ready to Go"
    override val planFilterToday = "Today"
    override val planFilterUpcoming = "Upcoming"
    override val planEmptyTitle = "No plans match filter"
    override val planEmptyDesc = "Add a new escort plan using the + button above."
    override val planCardTimeLabel: (String) -> String = { "Depart $it WIB" }
    override val planCardEscortLabel: (String) -> String = { "Escort: $it" }
    override val planCardNotesLabel: (String) -> String = { "Notes: $it" }
    override val planActionDepart = "Depart"
    override val planActionInputResult = "Log Result"
    override val planActionEdit = "Edit"
    override val planActionCancel = "Cancel"
    override val planCancelConfirmTitle = "Cancel Treatment Plan?"
    override val planCancelConfirmBody: (String) -> String = { "The escort plan for $it will be cancelled." }
    override val planCancelConfirmBtn = "Yes, Cancel"
    override val planCreatedByPrefix: (String) -> String = { "Created: $it" }
    override val planEditedByPrefix: (String) -> String = { "Edited: $it" }

    override val medTitle = "Medical Records & Follow-up"
    override val medSubtitle = "Diagnosis history, inpatient status, & health passport"
    override val medSearchPlaceholder = "Search student name, complaint, or diagnosis..."
    override val medTabRecords = "All Records"
    override val medTabInpatient = "Inpatient"
    override val medTabControls = "Follow-ups"
    override val medTabPassports = "Health Book"
    override val medFilterAll = "All"
    override val medFilterToday = "Today"
    override val medFilterThisMonth = "This Month"
    override val medSortNewest = "Newest"
    override val medSortFrequent = "Frequent Visits"
    override val medEmptyTitle = "No medical records yet"
    override val medEmptyDesc = "UKS / Puskesmas examination records will appear here."
    override val medInpatientEmptyTitle = "No students currently hospitalized"
    override val medInpatientEmptyDesc = "All students are at Asrama in stable condition."
    override val medControlEmptyTitle = "No active follow-up schedules"
    override val medControlEmptyDesc = "Doctor follow-up schedules will automatically appear here."
    override val medDiagnosisPrefix = "Diagnosis:"
    override val medActionsPrefix = "Treatment:"
    override val medPrescriptionPrefix = "Medication:"
    override val medEscortPrefix = "Escort:"
    override val medDischargeDialogTitle = "Confirm Inpatient Discharge"
    override val medDischargeDialogBody: (String, String) -> String = { name, faskes ->
        "Confirm $name has returned from $faskes. You may add a final diagnosis or discharge prescription if needed."
    }
    override val medDischargeFinalDiagLabel = "Final Diagnosis"
    override val medDischargeFinalPrescLabel = "Discharge Prescription (Optional)"
    override val medDischargeFinalPrescPlaceholder = "e.g., Paracetamol 3x1, Vitamins"
    override val medDischargeNeedControlSwitch = "Needs Follow-up Checkup?"
    override val medDischargeIn3Days = "+3 Days"
    override val medDischargeIn7Days = "+1 Week"
    override val medDischargeNotesLabel = "Discharge Notes (Optional)"
    override val medDischargeNotesPlaceholder = "e.g., Rest 2 days at Asrama"
    override val medDischargeConfirmBtn = "Confirm Discharge"
    override val passportBloodTypeLabel: (String, String) -> String = { dorm, blood -> "Asrama: $dorm • Blood Type: $blood" }
    override val passportAlertHighFreq = "⚠️ Student Health Attention:"
    override val passportAlertNormal = "🩺 Screening Summary:"
    override val passportMonthlyFreqText: (Int) -> String = { "This student was recorded sick / treated $it times this month." }
    override val passportTotalRecordsText: (Int) -> String = { "Total linked health screening records: $it entries." }
    override val passportPrintPdfBtn = "Print Screening PDF Recap (Month / Year)"
    override val passportHistoryHeader: (Int) -> String = { "Linked Screening & Treatment History ($it Visits):" }
    override val passportHistoryEmpty = "No medical records or screenings recorded for this student yet."
    override val passportVisitTitle: (Int, String) -> String = { no, date -> "Visit #$no • $date" }
    override val passportComplaintPrefix = "Complaint:"
    override val passportWaReportBtn = "WA Report"

    override val historyTitle = "Completed History"
    override val historySubtitle = "Archive of completed examinations & referrals"
    override val historyEmptyTitle = "No completed history yet"
    override val historyEmptyDesc = "All completed medical visits will be archived here."
    override val historyStatTotal = "Total Completed"
    override val historyStatThisMonth = "This Month"

    override val faskesTitle = "Health Facility Info"
    override val faskesSubtitle = "Referral health facilities & school quick contacts"
    override val faskesPrimaryTag = "Primary Facility (Internal)"
    override val faskesPartnerTag = "Main Partner Facility"
    override val faskesAdditionalTag = "Additional Referral Facility"
    override val faskesAddressLabel = "School Service Area"
    override val faskesPhoneNotSet = "Contact number not configured by Admin"
    override val faskesCallWaBtn = "Copy Number"
    override val faskesCopyPhoneToast: (String) -> String = { "$it copied to clipboard" }
    override val faskesGuideTitle = "Wali Asuh Quick Referral Flow"
    override val faskesGuideStep1 = "1. Perform initial check at UKS Sekolah for mild complaints."
    override val faskesGuideStep2 = "2. Create a Treatment Plan or depart to partner Puskesmas."
    override val faskesGuideStep3 = "3. Log diagnosis & prescription after returning to Asrama."
    override val faskesReferralTitle = "Health Referral Directory"
    override val faskesReferralSubtitle = "Quick contacts & registered referral facilities"
    override val faskesOpenInMapHint = "Tap to copy phone number or view details"

    override val createPlanTitle = "Create Treatment Plan"
    override val editPlanTitle = "Edit Treatment Plan"
    override val selectStudentLabel = "Select Student"
    override val selectStudentPlaceholder = "Tap to choose a student..."
    override val searchStudentHint = "Search student name or NIS..."
    override val faskesDestLabel = "Destination Facility"
    override val complaintLabel = "Initial Complaint"
    override val complaintPlaceholder = "e.g., High fever since last night, dizziness..."
    override val planDateLabel = "Planned Date"
    override val departureTimeLabel = "Departure Time (WIB)"
    override val escortShiftLabel = "Escort Officer / Shift"
    override val shiftMorningLabel = "Morning Shift"
    override val shiftAfternoonLabel = "Afternoon Shift"
    override val shiftNightLabel = "Night Shift"
    override val shiftAllLabel = "All Shifts"
    override val notesOptionalLabel = "Additional Notes (Optional)"
    override val notesPlanPlaceholder = "e.g., Warm compress applied"
    override val savePlanBtn = "Save Plan"
    override val updatePlanBtn = "Update Plan"
    override val validationSelectStudentAndComplaint = "Please select a student and enter the complaint first."

    override val departDialogTitle = "Confirm Departure"
    override val departDialogSubtitle: (String) -> String = { "Escort $it to destination health facility:" }
    override val departConfirmBtn = "Confirm Departure"

    override val directTreatTitle = "Log Direct Treatment"
    override val directTreatSubtitle = "Record a student examined and treated directly"
    override val diagnosisLabel = "Doctor / Medic Diagnosis"
    override val diagnosisPlaceholder = "e.g., Febrile / Mild upper respiratory infection"
    override val actionsTakenLabel = "Medical Action"
    override val actionsTakenPlaceholder = "e.g., Oral medication & bed rest"
    override val prescriptionLabel = "Prescription / Medication"
    override val prescriptionPlaceholder = "e.g., Paracetamol 500mg 3x1, Vitamin C"
    override val isInpatientSwitch = "Student Admitted (Inpatient)?"
    override val inpatientRoomLabel = "Inpatient Ward / Room"
    override val inpatientRoomPlaceholder = "e.g., Mawar Ward Room 4"
    override val needsControlSwitch = "Needs Follow-up Schedule?"
    override val controlDateLabel = "Follow-up Date"
    override val saveDirectTreatBtn = "Save Treatment Record"

    override val inputResultTitle = "Log Examination Result"
    override val inputResultSubtitle: (String, String) -> String = { name, faskes -> "$name • $faskes" }
    override val saveResultBtn = "Save Medical Result"

    override val searchStudentDialogTitle = "Select Student"
    override val searchStudentEmpty = "No students match your search."
    override val searchStudentInputHint = "Type name, NIS, or room..."
    override val studentClassDormFormat: (String, String) -> String = { cls, dorm -> "$cls • $dorm" }
}
