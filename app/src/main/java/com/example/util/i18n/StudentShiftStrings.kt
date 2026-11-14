package com.example.util.i18n

/**
 * Strings for StudentsScreen, AddStudentDialog, StudentDetailDialog,
 * StudentCardGeneratorSheet, StudentCardRenderer/PdfGenerator/Preview,
 * ShiftMessagesSheet, CreateShiftMessageDialog, ShiftHistorySheet.
 */
interface StudentShiftStrings {
    // Students Screen
    val studentsTitle: String
    val studentsSubtitle: String
    val studentsSearchPlaceholder: String
    val studentsFilterAll: String
    val studentsFilterBoys: String
    val studentsFilterGirls: String
    val studentsEmptyTitle: String
    val studentsEmptyDesc: String
    val studentsAddFabDesc: String
    val studentsCountSuffix: (Int) -> String
    val studentsDormPrefix: String
    val studentsRoomPrefix: String
    val studentsGenderMale: String
    val studentsGenderFemale: String

    // Add / Edit Student Dialog
    val addStudentTitle: String
    val editStudentTitle: String
    val studentNameLabel: String
    val studentNisLabel: String
    val studentNikLabel: String
    val studentGenderLabel: String
    val studentGenderMaleFull: String
    val studentGenderFemaleFull: String
    val studentAgeLabel: String
    val studentClassLabel: String
    val studentDormLabel: String
    val studentRoomLabel: String
    val studentBloodTypeLabel: String
    val studentParentPhoneLabel: String
    val studentAddressLabel: String
    val studentValidationRequired: String
    val saveStudentBtn: String

    // Student Detail Dialog
    val studentDetailTitle: String
    val studentDetailNis: String
    val studentDetailNik: String
    val studentDetailClassDorm: String
    val studentDetailRoom: String
    val studentDetailAge: (Int) -> String
    val studentDetailBloodType: String
    val studentDetailParentPhone: String
    val studentDetailAddress: String
    val studentDetailHaidHistoryTitle: String
    val studentDetailMedHistoryTitle: String
    val studentDetailNoMedHistory: String
    val studentDetailNoHaidHistory: String
    val studentDetailCreatePlanBtn: String
    val studentDetailDirectTreatBtn: String
    val studentDetailHealthBookBtn: String
    val studentDetailPrintCardBtn: String
    val studentDetailEditBtn: String

    // Student ID Card Generator / Renderer / Pdf / Preview
    val cardSheetTitle: String
    val cardSheetSubtitle: String
    val cardFilterAll: String
    val cardSearchPlaceholder: String
    val cardSelectAllBtn: String
    val cardClearSelectionBtn: String
    val cardSelectedCount: (Int) -> String
    val cardSinglePrintBtn: String
    val cardBatchPrintBtn: (Int) -> String
    val cardPreviewTitle: String
    val cardLabelHeaderSub: String
    val cardLabelStudentCardTitle: String
    val cardLabelName: String
    val cardLabelNis: String
    val cardLabelNik: String
    val cardLabelClassDorm: String
    val cardLabelOrigin: String
    val cardLabelGenderMale: String
    val cardLabelGenderFemale: String
    val cardPdfHeaderTitle: (String) -> String
    val cardPdfHeaderSubtitle: (Int) -> String
    val cardPdfSuccessToast: (Int) -> String
    val cardPdfEmptyToast: String
    val cardPdfChooserTitle: String

    // Shift Messages Sheet & Create Shift Message Dialog & History
    val shiftTitle: String
    val shiftSubtitle: String
    val shiftNewMessageBtn: String
    val shiftHistoryBtn: String
    val shiftAllTargetLabel: String
    val shiftEmptyTitle: String
    val shiftEmptyDesc: String
    val shiftMarkDoneBtn: String
    val shiftDoneTag: String
    val shiftShareWaBtn: String
    val shiftPriorityUrgent: String
    val shiftPriorityMedium: String
    val shiftPriorityNormal: String
    val shiftFromToFormat: (String, String) -> String
    val shiftTargetAllOption: String
    val shiftCreateTitle: String
    val shiftCreateFromLabel: (String) -> String
    val shiftCreateTargetWaliLabel: String
    val shiftCreateShiftLabel: String
    val shiftCreatePriorityLabel: String
    val shiftCreateStudentOptLabel: String
    val shiftCreateMessageLabel: String
    val shiftCreateMessagePlaceholder: String
    val shiftCreateVoiceBtn: String
    val shiftCreateVoiceListening: String
    val shiftCreateVoiceError: String
    val shiftCreateSubmitBtn: String
    val shiftHistoryTitle: String
    val shiftHistoryFilterAll: String
    val shiftHistoryFilterPending: String
    val shiftHistoryFilterDone: String
    val shiftHistoryEmpty: String
    val shiftMorningWindow: String
    val shiftAfternoonWindow: String
    val shiftNightWindow: String
}

object IdStudentShiftStrings : StudentShiftStrings {
    override val studentsTitle = "Daftar Anak Asuh"
    override val studentsSubtitle = "Data induk santri & akses buku kesehatan"
    override val studentsSearchPlaceholder = "Cari nama, NIS, NIK, atau kelas..."
    override val studentsFilterAll = "Semua"
    override val studentsFilterBoys = "Putra"
    override val studentsFilterGirls = "Putri"
    override val studentsEmptyTitle = "Belum ada data siswa"
    override val studentsEmptyDesc = "Tambahkan data anak asuh baru atau impor dari Admin."
    override val studentsAddFabDesc = "Tambah Siswa Baru"
    override val studentsCountSuffix: (Int) -> String = { "$it Anak Asuh" }
    override val studentsDormPrefix = "Asrama"
    override val studentsRoomPrefix = "Kamar"
    override val studentsGenderMale = "Laki-laki (Putra)"
    override val studentsGenderFemale = "Perempuan (Putri)"

    override val addStudentTitle = "Tambah Data Siswa"
    override val editStudentTitle = "Ubah Data Siswa"
    override val studentNameLabel = "Nama Lengkap Siswa *"
    override val studentNisLabel = "NIS *"
    override val studentNikLabel = "NIK"
    override val studentGenderLabel = "Jenis Kelamin"
    override val studentGenderMaleFull = "Laki-laki"
    override val studentGenderFemaleFull = "Perempuan"
    override val studentAgeLabel = "Usia (Tahun)"
    override val studentClassLabel = "Kelas *"
    override val studentDormLabel = "Asrama"
    override val studentRoomLabel = "No. Kamar"
    override val studentBloodTypeLabel = "Gol. Darah"
    override val studentParentPhoneLabel = "No. HP Orang Tua / Wali"
    override val studentAddressLabel = "Alamat Asal"
    override val studentValidationRequired = "Nama, NIS, dan Kelas wajib diisi!"
    override val saveStudentBtn = "Simpan Data Siswa"

    override val studentDetailTitle = "Profil Anak Asuh"
    override val studentDetailNis = "NIS"
    override val studentDetailNik = "NIK"
    override val studentDetailClassDorm = "Kelas & Asrama"
    override val studentDetailRoom = "Kamar"
    override val studentDetailAge: (Int) -> String = { "$it Tahun" }
    override val studentDetailBloodType = "Golongan Darah"
    override val studentDetailParentPhone = "Kontak Orang Tua"
    override val studentDetailAddress = "Alamat Asal"
    override val studentDetailHaidHistoryTitle = "Riwayat Haid Terbaru"
    override val studentDetailMedHistoryTitle = "Riwayat Berobat Terbaru"
    override val studentDetailNoMedHistory = "Belum ada riwayat berobat."
    override val studentDetailNoHaidHistory = "Belum ada catatan haid."
    override val studentDetailCreatePlanBtn = "Buat Rencana"
    override val studentDetailDirectTreatBtn = "Input Berobat"
    override val studentDetailHealthBookBtn = "Buku Sehat"
    override val studentDetailPrintCardBtn = "Cetak Kartu"
    override val studentDetailEditBtn = "Edit Data"

    override val cardSheetTitle = "Cetak Kartu Identitas Santri"
    override val cardSheetSubtitle = "Pilih siswa untuk mencetak Kartu Kesehatan & QR Barcode (10 kartu/halaman A4)"
    override val cardFilterAll = "Semua"
    override val cardSearchPlaceholder = "Cari nama / NIS / kelas..."
    override val cardSelectAllBtn = "Pilih Semua"
    override val cardClearSelectionBtn = "Batal Pilih"
    override val cardSelectedCount: (Int) -> String = { "$it siswa terpilih" }
    override val cardSinglePrintBtn = "Cetak Kartu Ini"
    override val cardBatchPrintBtn: (Int) -> String = { "Cetak PDF ($it Kartu)" }
    override val cardPreviewTitle = "Pratinjau Kartu Santri"
    override val cardLabelHeaderSub = "KARTU IDENTITAS & KESEHATAN SANTRI"
    override val cardLabelStudentCardTitle = "KARTU SANTRI"
    override val cardLabelName = "NAMA"
    override val cardLabelNis = "NIS"
    override val cardLabelNik = "NIK"
    override val cardLabelClassDorm = "KELAS / ASRAMA"
    override val cardLabelOrigin = "ALAMAT ASAL"
    override val cardLabelGenderMale = "PUTRA"
    override val cardLabelGenderFemale = "PUTRI"
    override val cardPdfHeaderTitle: (String) -> String = { "KARTU IDENTITAS SANTRI — $it" }
    override val cardPdfHeaderSubtitle: (Int) -> String = { "Dicetak otomatis oleh Wali Asuh Care • Total $it Kartu (Skala Asli KTP 85.6 × 54 mm)" }
    override val cardPdfSuccessToast: (Int) -> String = { "Berhasil membuat PDF $it kartu santri" }
    override val cardPdfEmptyToast = "Pilih minimal 1 siswa untuk mencetak kartu"
    override val cardPdfChooserTitle = "Bagikan / Simpan PDF Kartu Santri"

    override val shiftTitle = "Pesan Operan Shift"
    override val shiftSubtitle = "Catatan estafet antar Wali Asuh & Pembina Asrama"
    override val shiftNewMessageBtn = "Tulis Operan"
    override val shiftHistoryBtn = "Riwayat"
    override val shiftAllTargetLabel = "Semua Wali Asuh"
    override val shiftEmptyTitle = "Belum ada pesan operan aktif"
    override val shiftEmptyDesc = "Semua pesan tugas lintas shift telah terselesaikan."
    override val shiftMarkDoneBtn = "Selesai"
    override val shiftDoneTag = "Selesai ✓"
    override val shiftShareWaBtn = "Kirim WA"
    override val shiftPriorityUrgent = "URGENT"
    override val shiftPriorityMedium = "SEDANG"
    override val shiftPriorityNormal = "BIASA"
    override val shiftFromToFormat: (String, String) -> String = { from, to -> "Dari: $from → Untuk: $to" }
    override val shiftTargetAllOption = "Semua Wali Asuh"
    override val shiftCreateTitle = "Tulis Pesan Operan Shift"
    override val shiftCreateFromLabel: (String) -> String = { "Pengirim: $it" }
    override val shiftCreateTargetWaliLabel = "Ditujukan Kepada"
    override val shiftCreateShiftLabel = "Shift Jaga"
    override val shiftCreatePriorityLabel = "Prioritas"
    override val shiftCreateStudentOptLabel = "Terkait Siswa (Opsional)"
    override val shiftCreateMessageLabel = "Isi Pesan Operan *"
    override val shiftCreateMessagePlaceholder = "Tulis instruksi obat, pemantauan suhu, atau titipan tugas..."
    override val shiftCreateVoiceBtn = "Dikte Suara"
    override val shiftCreateVoiceListening = "Mendengarkan suara..."
    override val shiftCreateVoiceError = "Pengenalan suara tidak tersedia atau dibatalkan"
    override val shiftCreateSubmitBtn = "Kirim Operan"
    override val shiftHistoryTitle = "Riwayat Operan Shift"
    override val shiftHistoryFilterAll = "Semua"
    override val shiftHistoryFilterPending = "Belum Selesai"
    override val shiftHistoryFilterDone = "Selesai"
    override val shiftHistoryEmpty = "Tidak ada riwayat pesan pada filter ini."
    override val shiftMorningWindow = "Shift Pagi (07-14)"
    override val shiftAfternoonWindow = "Shift Sore (14-21)"
    override val shiftNightWindow = "Shift Malam (21-07)"
}

object EnStudentShiftStrings : StudentShiftStrings {
    override val studentsTitle = "Students List"
    override val studentsSubtitle = "Master student directory & health books"
    override val studentsSearchPlaceholder = "Search name, NIS, NIK, or class..."
    override val studentsFilterAll = "All"
    override val studentsFilterBoys = "Boys"
    override val studentsFilterGirls = "Girls"
    override val studentsEmptyTitle = "No students yet"
    override val studentsEmptyDesc = "Add a new student or import roster from School Admin."
    override val studentsAddFabDesc = "Add New Student"
    override val studentsCountSuffix: (Int) -> String = { "$it Students" }
    override val studentsDormPrefix = "Asrama"
    override val studentsRoomPrefix = "Room"
    override val studentsGenderMale = "Male (Putra)"
    override val studentsGenderFemale = "Female (Putri)"

    override val addStudentTitle = "Add Student Record"
    override val editStudentTitle = "Edit Student Record"
    override val studentNameLabel = "Full Name *"
    override val studentNisLabel = "NIS *"
    override val studentNikLabel = "NIK"
    override val studentGenderLabel = "Gender"
    override val studentGenderMaleFull = "Male"
    override val studentGenderFemaleFull = "Female"
    override val studentAgeLabel = "Age (Years)"
    override val studentClassLabel = "Class *"
    override val studentDormLabel = "Asrama"
    override val studentRoomLabel = "Room No."
    override val studentBloodTypeLabel = "Blood Type"
    override val studentParentPhoneLabel = "Parent / Guardian Phone"
    override val studentAddressLabel = "Home Address"
    override val studentValidationRequired = "Name, NIS, and Class are required!"
    override val saveStudentBtn = "Save Student"

    override val studentDetailTitle = "Student Profile"
    override val studentDetailNis = "NIS"
    override val studentDetailNik = "NIK"
    override val studentDetailClassDorm = "Class & Asrama"
    override val studentDetailRoom = "Room"
    override val studentDetailAge: (Int) -> String = { "$it Years" }
    override val studentDetailBloodType = "Blood Type"
    override val studentDetailParentPhone = "Parent Contact"
    override val studentDetailAddress = "Home Address"
    override val studentDetailHaidHistoryTitle = "Recent Haid History"
    override val studentDetailMedHistoryTitle = "Recent Medical History"
    override val studentDetailNoMedHistory = "No medical history yet."
    override val studentDetailNoHaidHistory = "No Haid logs yet."
    override val studentDetailCreatePlanBtn = "Create Plan"
    override val studentDetailDirectTreatBtn = "Log Treatment"
    override val studentDetailHealthBookBtn = "Health Book"
    override val studentDetailPrintCardBtn = "Print Card"
    override val studentDetailEditBtn = "Edit Data"

    override val cardSheetTitle = "Print Student ID Cards"
    override val cardSheetSubtitle = "Select students to generate Health & QR Barcode ID Cards (10 cards/A4 sheet)"
    override val cardFilterAll = "All"
    override val cardSearchPlaceholder = "Search name / NIS / class..."
    override val cardSelectAllBtn = "Select All"
    override val cardClearSelectionBtn = "Clear"
    override val cardSelectedCount: (Int) -> String = { "$it students selected" }
    override val cardSinglePrintBtn = "Print This Card"
    override val cardBatchPrintBtn: (Int) -> String = { "Generate PDF ($it Cards)" }
    override val cardPreviewTitle = "Student Card Preview"
    override val cardLabelHeaderSub = "STUDENT IDENTITY & HEALTH CARD"
    override val cardLabelStudentCardTitle = "STUDENT CARD"
    override val cardLabelName = "NAME"
    override val cardLabelNis = "NIS"
    override val cardLabelNik = "NIK"
    override val cardLabelClassDorm = "CLASS / ASRAMA"
    override val cardLabelOrigin = "HOME ADDRESS"
    override val cardLabelGenderMale = "PUTRA"
    override val cardLabelGenderFemale = "PUTRI"
    override val cardPdfHeaderTitle: (String) -> String = { "STUDENT ID CARDS — $it" }
    override val cardPdfHeaderSubtitle: (Int) -> String = { "Generated by Wali Asuh Care • Total $it Cards (ISO ID-1 Size 85.6 × 54 mm)" }
    override val cardPdfSuccessToast: (Int) -> String = { "Generated PDF with $it student cards" }
    override val cardPdfEmptyToast = "Select at least 1 student to print cards"
    override val cardPdfChooserTitle = "Share / Save Student ID Cards PDF"

    override val shiftTitle = "Shift Handover Messages"
    override val shiftSubtitle = "Handover notes between Wali Asuh & Asrama mentors"
    override val shiftNewMessageBtn = "New Handover"
    override val shiftHistoryBtn = "History"
    override val shiftAllTargetLabel = "All Wali Asuh"
    override val shiftEmptyTitle = "No active handover messages"
    override val shiftEmptyDesc = "All cross-shift tasks have been completed."
    override val shiftMarkDoneBtn = "Complete"
    override val shiftDoneTag = "Completed ✓"
    override val shiftShareWaBtn = "Share WA"
    override val shiftPriorityUrgent = "URGENT"
    override val shiftPriorityMedium = "MEDIUM"
    override val shiftPriorityNormal = "NORMAL"
    override val shiftFromToFormat: (String, String) -> String = { from, to -> "From: $from → To: $to" }
    override val shiftTargetAllOption = "All Wali Asuh"
    override val shiftCreateTitle = "Write Shift Handover"
    override val shiftCreateFromLabel: (String) -> String = { "Sender: $it" }
    override val shiftCreateTargetWaliLabel = "Recipient"
    override val shiftCreateShiftLabel = "Duty Shift"
    override val shiftCreatePriorityLabel = "Priority"
    override val shiftCreateStudentOptLabel = "Related Student (Optional)"
    override val shiftCreateMessageLabel = "Handover Note *"
    override val shiftCreateMessagePlaceholder = "Write medication reminders, fever checks, or follow-up tasks..."
    override val shiftCreateVoiceBtn = "Voice Input"
    override val shiftCreateVoiceListening = "Listening..."
    override val shiftCreateVoiceError = "Voice recognition unavailable or cancelled"
    override val shiftCreateSubmitBtn = "Send Handover"
    override val shiftHistoryTitle = "Shift Handover History"
    override val shiftHistoryFilterAll = "All"
    override val shiftHistoryFilterPending = "Pending"
    override val shiftHistoryFilterDone = "Completed"
    override val shiftHistoryEmpty = "No handover history matches this filter."
    override val shiftMorningWindow = "Morning Shift (07-14)"
    override val shiftAfternoonWindow = "Afternoon Shift (14-21)"
    override val shiftNightWindow = "Night Shift (21-07)"
}
