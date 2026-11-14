package com.example.util.i18n

/**
 * String resources for Navigation, TopBar, Profile, Notifications, and ViewModel feedback.
 * Preserves domain proper names: Wali Asuh, Wali Asuh Care, Sekolah Rakyat, Asrama, Kemensos RI.
 */
interface CommonNavStrings {
    // Bottom Navigation & TopBar
    val navHome: String
    val navPlans: String
    val navMedical: String
    val navStudents: String
    val navProfile: String
    val topBarNotificationsDesc: String
    val topBarProfileDesc: String
    val topBarOpenFaskesDesc: String
    val topBarAdminSubtitle: String
    val topBarSuperAdminSubtitle: String

    // Notification Drawer Sheet
    val notifSheetTitle: String
    val notifSheetSubtitle: String
    val notifWaitingPrefix: String
    val notifTodaySchedulePrefix: String
    val notifWaitingLabel: String
    val notifOnTheWayLabel: String
    val notifNeedTreatmentTitle: String
    val notifControlDueSuffix: String
    val notifCheckStatusPrompt: String
    val notifAllClearTitle: String
    val notifAllClearDesc: String
    val notifDismissBtn: String
    val notifMoreCount: (Int) -> String

    // Profile Screen
    val profileTitle: String
    val profileRoleWali: String
    val profileShiftMonitorFallback: String
    val profileUsernamePrefix: String
    val profileSchoolUnknown: String
    val profileStatsSection: String
    val profileStatStudents: String
    val profileStatMedical: String
    val profileStatReferral: String
    val profileAppSettingsSection: String
    val profileThemeTitle: String
    val profileThemeDarkActive: String
    val profileThemeLightActive: String
    val profileLanguageTitle: String
    val profileLanguageSubtitle: (String) -> String
    val profileLanguageSheetTitle: String
    val profileLanguageSheetSubtitle: String
    val profileLanguageIdDesc: String
    val profileLanguageEnDesc: String
    val profileLanguageActiveTag: String
    val profileNotifTitle: String
    val profileNotifDesc: String
    val profileAccountSecuritySection: String
    val profileChangePasswordTitle: String
    val profileChangePasswordDesc: String
    val profileDeleteAccountTitle: String
    val profileDeleteAccountDesc: String
    val profileHelpInfoSection: String
    val profileGuideTitle: String
    val profileGuideDesc: String
    val profileAboutTitle: String
    val profileAboutDesc: String
    val profilePrivacyTitle: String
    val profilePrivacyDesc: String
    val profileTermsTitle: String
    val profileTermsDesc: String
    val profileDisclaimerLabel: String
    val profileLogoutTitle: String
    val profileLogoutDesc: String
    val profileVersionFooter: String

    // Profile Dialogs: Notifications, Guide, Logout
    val notifDialogTitle: String
    val notifDialogPushTitle: String
    val notifDialogPushDesc: String
    val notifDialogPushGranted: String
    val notifDialogPushDenied: String
    val notifDialogPushGrantedToast: String
    val notifDialogPushDeniedToast: String
    val notifDialogSyncReminderTitle: String
    val notifDialogSyncReminderDesc: String
    val notifDialogTestTitle: String
    val notifDialogTestDesc: String
    val notifDialogTestToast: String
    val guideDialogTitle: String
    val guideStep1: String
    val guideStep2: String
    val guideStep3: String
    val guideStep4: String
    val guideStep5: String
    val logoutConfirmTitle: String
    val logoutConfirmBody: String
    val logoutBtn: String
    val closeBtn: String
    val cancelBtn: String
    val backBtn: String
    val searchBtn: String
    val saveBtn: String
    val close: String get() = closeBtn
    val cancel: String get() = cancelBtn
    val search: String get() = searchBtn
    val delete: String
    val all: String
    val today: String
    val tomorrow: String
    val dayAfterTomorrow: String

    // Change Wali Password Dialog
    val changePassTitle: String
    val changePassSubtitle: String
    val changePassUsernameHint: (String) -> String
    val changePassNameLabel: String
    val changePassNewLabel: String
    val changePassConfirmLabel: String
    val changePassMinError: String
    val changePassMismatchError: String
    val changePassSyncNote: String
    val changePassSaveBtn: String

    // Delete Account Sheet
    val deleteAccTitle: String
    val deleteAccWarning: String
    val deleteAccInfoNote: String
    val deleteAccPasswordLabel: String
    val deleteAccBtn: String

    // Legal & About Sheets
    val legalAboutTitle: String
    val legalAboutRoleBadge: String
    val legalAboutDesc: String
    val legalPrivacyTitle: String
    val legalPrivacyIntro: String
    val legalPrivacyPoint1Title: String
    val legalPrivacyPoint1Body: String
    val legalPrivacyPoint2Title: String
    val legalPrivacyPoint2Body: String
    val legalPrivacyPoint3Title: String
    val legalPrivacyPoint3Body: String
    val legalPrivacyPoint4Title: String
    val legalPrivacyPoint4Body: String
    val legalTermsTitle: String
    val legalTermsIntro: String
    val legalTermsPoint1Title: String
    val legalTermsPoint1Body: String
    val legalTermsPoint2Title: String
    val legalTermsPoint2Body: String
    val legalTermsPoint3Title: String
    val legalTermsPoint3Body: String
    val legalUnderstoodBtn: String
    val legalUnderstandTermsBtn: String

    // Reward Ad Declaration Dialog
    val rewardAdTitle: String
    val rewardAdSubtitle: String
    val rewardAdPoints: String
    val rewardAdConfirmBtn: String

    // ViewModel Feedback Messages (Wali-facing)
    val vmFastingAlreadyRegistered: (String, String) -> String
    val vmFastingRegisteredSuccess: (String, String) -> String
    val vmPasswordMinError: String
    val vmPasswordUpdatedSuccess: String
    val vmPasswordCreatedSuccess: String
    val vmDeletePassRequired: String
    val vmDeletePassMismatch: String
    val vmDeleteSuccess: String
}

object IdCommonNavStrings : CommonNavStrings {
    override val navHome = "Dashboard"
    override val navPlans = "Rencana"
    override val navMedical = "Tindakan"
    override val navStudents = "Anak Asuh"
    override val navProfile = "Profil"
    override val topBarNotificationsDesc = "Notifikasi"
    override val topBarProfileDesc = "Profil"
    override val topBarOpenFaskesDesc = "Buka Info Faskes"
    override val topBarAdminSubtitle = "Panel Administrator Sekolah"
    override val topBarSuperAdminSubtitle = "Superadmin Control Center"

    override val notifSheetTitle = "Pemberitahuan & Jadwal"
    override val notifSheetSubtitle = "Pantau antrean kepergian & pengingat kontrol"
    override val notifWaitingPrefix = "Antrean Rencana"
    override val notifTodaySchedulePrefix = "Jadwal Rencana Hari Ini"
    override val notifWaitingLabel = "Menunggu"
    override val notifOnTheWayLabel = "Diantar"
    override val notifNeedTreatmentTitle = "Waktunya Diobatkan"
    override val notifControlDueSuffix = "Kontrol Jatuh Tempo"
    override val notifCheckStatusPrompt = "Periksa status penanganan siswa ini sekarang."
    override val notifAllClearTitle = "Semua Aman & Terkendali"
    override val notifAllClearDesc = "Tidak ada antrean anak sakit atau jadwal kontrol jatuh tempo saat ini."
    override val notifDismissBtn = "Tutup Pemberitahuan"
    override val notifMoreCount: (Int) -> String = { "+ $it pemberitahuan lainnya..." }

    override val profileTitle = "Profil Wali Asuh"
    override val profileRoleWali = "Wali Asuh • Pembina Asrama"
    override val profileShiftMonitorFallback = "Monitoring Kesehatan Anak"
    override val profileUsernamePrefix = "Username:"
    override val profileSchoolUnknown = "Sekolah Tidak Diketahui"
    override val profileStatsSection = "Ringkasan Tugas Anda"
    override val profileStatStudents = "Anak Asuh"
    override val profileStatMedical = "Rekam Medis"
    override val profileStatReferral = "Rujukan Aktif"
    override val profileAppSettingsSection = "Pengaturan App"
    override val profileThemeTitle = "Tema Aplikasi (Gelap / Terang)"
    override val profileThemeDarkActive = "Mode Gelap Aktif"
    override val profileThemeLightActive = "Mode Terang Aktif"
    override val profileLanguageTitle = "Bahasa Aplikasi"
    override val profileLanguageSubtitle: (String) -> String = { "Aktif: $it" }
    override val profileLanguageSheetTitle = "Pilih Bahasa Aplikasi"
    override val profileLanguageSheetSubtitle = "Seluruh teks menu, tanggal, pesan WhatsApp, dan laporan PDF akan menyesuaikan bahasa yang dipilih."
    override val profileLanguageIdDesc = "Bahasa standar operasional Wali Asuh"
    override val profileLanguageEnDesc = "English interface, dates, PDF & WhatsApp reports"
    override val profileLanguageActiveTag = "Aktif"
    override val profileNotifTitle = "Notifikasi Sistem & Jadwal"
    override val profileNotifDesc = "Notifikasi bar resmi & pengingat"
    override val profileAccountSecuritySection = "Akun & Keamanan"
    override val profileChangePasswordTitle = "Ganti Password & Nama"
    override val profileChangePasswordDesc = "Ubah password login & profil Anda"
    override val profileDeleteAccountTitle = "Hapus Akun Pribadi"
    override val profileDeleteAccountDesc = "Hapus akun Wali Asuh secara permanen"
    override val profileHelpInfoSection = "Bantuan & Info"
    override val profileGuideTitle = "Panduan Singkat"
    override val profileGuideDesc = "Cara cepat menggunakan aplikasi"
    override val profileAboutTitle = "Tentang Aplikasi"
    override val profileAboutDesc = "Versi 2.4 • Informasi aplikasi"
    override val profilePrivacyTitle = "Kebijakan Privasi"
    override val profilePrivacyDesc = "Keamanan & perlindungan data"
    override val profileTermsTitle = "Syarat & Ketentuan"
    override val profileTermsDesc = "Aturan penggunaan & penafian resmi"
    override val profileDisclaimerLabel = "Penafian Independen"
    override val profileLogoutTitle = "Keluar Aplikasi"
    override val profileLogoutDesc = "Log out akun Wali Asuh ini"
    override val profileVersionFooter = "Wali Asuh Care • Versi 2.4.0"

    override val notifDialogTitle = "Pengaturan Notifikasi"
    override val notifDialogPushTitle = "Push Notifikasi Sistem"
    override val notifDialogPushDesc = "Muncul di bilah notifikasi HP saat ada siswa sakit"
    override val notifDialogPushGranted = "Izin notifikasi sistem aktif. Anda akan menerima pemberitahuan real-time saat laporan baru dibuat."
    override val notifDialogPushDenied = "Izin notifikasi belum diberikan. Klik toggle di atas untuk memunculkan dialog izin perangkat."
    override val notifDialogPushGrantedToast = "Izin Notifikasi Diaktifkan!"
    override val notifDialogPushDeniedToast = "Izin notifikasi ditolak di pengaturan HP"
    override val notifDialogSyncReminderTitle = "Pengingat Jadwal & Kontrol"
    override val notifDialogSyncReminderDesc = "Tandai rencana berobat & kontrol otomatis"
    override val notifDialogTestTitle = "Uji Coba Notifikasi"
    override val notifDialogTestDesc = "Cek tampilan pop-up notifikasi resmi"
    override val notifDialogTestToast = "Notifikasi resmi dikirim ke status bar HP!"

    override val guideDialogTitle = "Panduan Singkat Wali Asuh"
    override val guideStep1 = "1. Home / Beranda: Pantau ringkasan anak yang akan berobat & butuh pengawasan kontrol."
    override val guideStep2 = "2. Rencana Pengobatan: Daftarkan anak yang butuh diantar ke UKS / Puskesmas."
    override val guideStep3 = "3. Rekam Medis & Kontrol: Catat diagnosa dokter, obat, serta jadwal kontrol ulang."
    override val guideStep4 = "4. Share WA & Ekspor PDF: Kirim laporan ringkas ke grup atau cetak dokumen resmi."
    override val guideStep5 = "5. Daftar Siswa: Kelola data siswa asrama dan riwayat berobat per anak."
    override val logoutConfirmTitle = "Keluar dari Akun?"
    override val logoutConfirmBody = "Apakah Anda yakin ingin keluar dari sesi Wali Asuh saat ini?"
    override val logoutBtn = "Keluar"
    override val closeBtn = "Tutup"
    override val cancelBtn = "Batal"
    override val backBtn = "Kembali"
    override val searchBtn = "Cari"
    override val saveBtn = "Simpan"
    override val delete = "Hapus"
    override val all = "Semua"
    override val today = "Hari Ini"
    override val tomorrow = "Besok"
    override val dayAfterTomorrow = "Lusa"

    override val changePassTitle = "Ganti Password & Nama"
    override val changePassSubtitle = "Perubahan ini akan otomatis tersinkronisasi ke data Admin Sekolah Anda."
    override val changePassUsernameHint: (String) -> String = { "Username login Anda tetap: @$it" }
    override val changePassNameLabel = "Nama Tampilan Wali Asuh"
    override val changePassNewLabel = "Kata Sandi Baru (Min. 6 karakter)"
    override val changePassConfirmLabel = "Konfirmasi Kata Sandi Baru"
    override val changePassMinError = "Kata sandi minimal 6 karakter!"
    override val changePassMismatchError = "Konfirmasi kata sandi tidak cocok!"
    override val changePassSyncNote = "Admin Sekolah tetap dapat melihat atau me-reset password Anda apabila sewaktu-waktu Anda lupa."
    override val changePassSaveBtn = "Simpan Perubahan"

    override val deleteAccTitle = "Hapus Akun Wali Asuh?"
    override val deleteAccWarning = "Tindakan ini akan menghapus akun login Wali Asuh Anda secara permanen dari perangkat dan server cloud sekolah. Data siswa, rekam medis, dan laporan sekolah tetap tersimpan aman."
    override val deleteAccInfoNote = "Pastikan Anda tidak memiliki jadwal pengantaran yang sedang berlangsung sebelum menghapus akun."
    override val deleteAccPasswordLabel = "Masukkan Password untuk Konfirmasi"
    override val deleteAccBtn = "Hapus Permanen"

    override val legalAboutTitle = "Tentang Aplikasi"
    override val legalAboutRoleBadge = "Dokumentasi Kesehatan & Pembinaan Asrama"
    override val legalAboutDesc = "Membantu Wali Asuh dalam mencatat rencana berobat, memantau anak sakit, pencatatan haid santri putri, pemantauan puasa, hingga cetak laporan PDF & berbagi ringkasan WhatsApp dengan rapi."
    override val legalPrivacyTitle = "Kebijakan Privasi"
    override val legalPrivacyIntro = "Komitmen perlindungan data di Wali Asuh Care:"
    override val legalPrivacyPoint1Title = "1. Data Operasional Terbatas"
    override val legalPrivacyPoint1Body = "Data siswa, catatan keluhan kesehatan, data haid, dan ibadah hanya digunakan untuk dokumentasi internal pembinaan asrama di sekolah terdaftar."
    override val legalPrivacyPoint2Title = "2. Penyimpanan Lokal & Cloud Sekolah"
    override val legalPrivacyPoint2Body = "Data disimpan secara aman di perangkat dan disinkronkan khusus ke ruang kerja sekolah (tenant) yang terotorisasi oleh Admin Sekolah."
    override val legalPrivacyPoint3Title = "3. Kontrol Pengguna & Penghapusan"
    override val legalPrivacyPoint3Body = "Wali Asuh atau Admin Sekolah dapat memperbarui atau menghapus akun serta data kapan saja melalui menu Profil atau Panel Admin."
    override val legalPrivacyPoint4Title = "4. Tidak Dibagikan ke Pihak Ketiga Umum"
    override val legalPrivacyPoint4Body = "Kami tidak menjual atau membagikan data pribadi siswa kepada pihak komersial mana pun."
    override val legalTermsTitle = "Syarat & Ketentuan"
    override val legalTermsIntro = "Ketentuan penggunaan Wali Asuh Care:"
    override val legalTermsPoint1Title = "1. Alat Bantu Operasional"
    override val legalTermsPoint1Body = "Aplikasi ini berfungsi sebagai alat bantu pencatatan harian Wali Asuh dan bukan pengganti diagnosa medis dokter atau sistem rekam medis resmi rumah sakit."
    override val legalTermsPoint2Title = "2. Tanggung Jawab Data Sekolah"
    override val legalTermsPoint2Body = "Kebenaran data siswa, keamanan kata sandi akun, dan distribusi laporan PDF/WhatsApp merupakan tanggung jawab masing-masing Wali Asuh dan Admin Sekolah."
    override val legalTermsPoint3Title = "3. Status Independen"
    override val legalTermsPoint3Body = "Wali Asuh Care adalah inisiatif alat bantu independen dan bukan aplikasi resmi dari Kementerian Sosial RI ataupun instansi Sekolah Rakyat."
    override val legalUnderstoodBtn = "Saya Mengerti"
    override val legalUnderstandTermsBtn = "Setuju & Tutup"

    override val rewardAdTitle = "Pencapaian & Dukungan Operasional"
    override val rewardAdSubtitle = "Anda dapat mengekspor PDF secara gratis. Menonton tayangan sponsor singkat opsional membantu biaya server cloud Wali Asuh Care."
    override val rewardAdPoints = "• Cetak PDF tetap 100% gratis tanpa syarat\n• Membantu pemeliharaan server sinkronisasi sekolah\n• Tidak ada data siswa yang dibagikan ke pengiklan"
    override val rewardAdConfirmBtn = "Lanjutkan Cetak PDF"

    override val vmFastingAlreadyRegistered: (String, String) -> String = { name, title -> "$name sudah terdaftar pada sesi $title." }
    override val vmFastingRegisteredSuccess: (String, String) -> String = { name, jenjang -> "$name ($jenjang) berhasil didaftarkan berpuasa." }
    override val vmPasswordMinError = "Kata sandi baru minimal harus 6 karakter."
    override val vmPasswordUpdatedSuccess = "Kata sandi berhasil diperbarui dan tersinkronisasi ke Admin Sekolah."
    override val vmPasswordCreatedSuccess = "Kata sandi berhasil dibuat dan tersinkronisasi ke Admin Sekolah."
    override val vmDeletePassRequired = "Masukkan password akun Anda untuk konfirmasi."
    override val vmDeletePassMismatch = "Password tidak sesuai. Penghapusan akun dibatalkan."
    override val vmDeleteSuccess = "Akun Wali Asuh Anda telah dihapus permanen."
}

object EnCommonNavStrings : CommonNavStrings {
    override val navHome = "Home"
    override val navPlans = "Plans"
    override val navMedical = "Records"
    override val navStudents = "Students"
    override val navProfile = "Profile"
    override val topBarNotificationsDesc = "Notifications"
    override val topBarProfileDesc = "Profile"
    override val topBarOpenFaskesDesc = "Open Faskes Info"
    override val topBarAdminSubtitle = "School Administrator Panel"
    override val topBarSuperAdminSubtitle = "Superadmin Control Center"

    override val notifSheetTitle = "Notifications & Schedules"
    override val notifSheetSubtitle = "Monitor departure queues & follow-up reminders"
    override val notifWaitingPrefix = "Planned Queue"
    override val notifTodaySchedulePrefix = "Today's Planned Schedule"
    override val notifWaitingLabel = "Waiting"
    override val notifOnTheWayLabel = "Escorted"
    override val notifNeedTreatmentTitle = "Ready for Treatment"
    override val notifControlDueSuffix = "Follow-up Due"
    override val notifCheckStatusPrompt = "Check this student's treatment status now."
    override val notifAllClearTitle = "All Clear & Under Control"
    override val notifAllClearDesc = "No sick student queues or due follow-up checkups right now."
    override val notifDismissBtn = "Close Notifications"
    override val notifMoreCount: (Int) -> String = { "+ $it more notifications..." }

    override val profileTitle = "Wali Asuh Profile"
    override val profileRoleWali = "Wali Asuh • Asrama Mentor"
    override val profileShiftMonitorFallback = "Student Health Monitoring"
    override val profileUsernamePrefix = "Username:"
    override val profileSchoolUnknown = "Unknown School"
    override val profileStatsSection = "Your Duty Summary"
    override val profileStatStudents = "Students"
    override val profileStatMedical = "Medical Records"
    override val profileStatReferral = "Active Referrals"
    override val profileAppSettingsSection = "App Settings"
    override val profileThemeTitle = "App Theme (Dark / Light)"
    override val profileThemeDarkActive = "Dark Mode Active"
    override val profileThemeLightActive = "Light Mode Active"
    override val profileLanguageTitle = "App Language"
    override val profileLanguageSubtitle: (String) -> String = { "Active: $it" }
    override val profileLanguageSheetTitle = "Choose App Language"
    override val profileLanguageSheetSubtitle = "All menus, dates, WhatsApp messages, and PDF reports will switch to the selected language."
    override val profileLanguageIdDesc = "Default Indonesian operational interface"
    override val profileLanguageEnDesc = "English interface, dates, PDF & WhatsApp reports"
    override val profileLanguageActiveTag = "Active"
    override val profileNotifTitle = "System & Schedule Notifications"
    override val profileNotifDesc = "Official status bar alerts & reminders"
    override val profileAccountSecuritySection = "Account & Security"
    override val profileChangePasswordTitle = "Change Password & Name"
    override val profileChangePasswordDesc = "Update your login password & profile"
    override val profileDeleteAccountTitle = "Delete Personal Account"
    override val profileDeleteAccountDesc = "Permanently remove this Wali Asuh account"
    override val profileHelpInfoSection = "Help & Info"
    override val profileGuideTitle = "Quick Guide"
    override val profileGuideDesc = "How to use the app quickly"
    override val profileAboutTitle = "About App"
    override val profileAboutDesc = "Version 2.4 • Application info"
    override val profilePrivacyTitle = "Privacy Policy"
    override val profilePrivacyDesc = "Data security & protection"
    override val profileTermsTitle = "Terms & Conditions"
    override val profileTermsDesc = "Usage rules & official disclaimer"
    override val profileDisclaimerLabel = "Independent Disclaimer"
    override val profileLogoutTitle = "Log Out"
    override val profileLogoutDesc = "Sign out of this Wali Asuh account"
    override val profileVersionFooter = "Wali Asuh Care • Version 2.4.0"

    override val notifDialogTitle = "Notification Settings"
    override val notifDialogPushTitle = "System Push Notifications"
    override val notifDialogPushDesc = "Show in device status bar when a student is reported sick"
    override val notifDialogPushGranted = "System notification permission is active. You will receive real-time alerts when new reports are created."
    override val notifDialogPushDenied = "Notification permission is not granted yet. Toggle above to open the device permission dialog."
    override val notifDialogPushGrantedToast = "Notification Permission Enabled!"
    override val notifDialogPushDeniedToast = "Notification permission denied in device settings"
    override val notifDialogSyncReminderTitle = "Schedule & Follow-up Reminders"
    override val notifDialogSyncReminderDesc = "Automatically flag treatment plans & follow-up visits"
    override val notifDialogTestTitle = "Test Notification"
    override val notifDialogTestDesc = "Preview how an official alert looks"
    override val notifDialogTestToast = "Official test notification sent to status bar!"

    override val guideDialogTitle = "Wali Asuh Quick Guide"
    override val guideStep1 = "1. Home: Monitor daily student treatments & follow-up schedules at a glance."
    override val guideStep2 = "2. Treatment Plans: Register students scheduled for UKS / Puskesmas visits."
    override val guideStep3 = "3. Medical Records & Follow-up: Record doctor diagnoses, prescriptions, and control visits."
    override val guideStep4 = "4. Share WA & Export PDF: Send concise summaries to groups or print official reports."
    override val guideStep5 = "5. Students List: Manage Asrama student data and individual medical history."
    override val logoutConfirmTitle = "Sign Out of Account?"
    override val logoutConfirmBody = "Are you sure you want to log out of the current Wali Asuh session?"
    override val logoutBtn = "Log Out"
    override val closeBtn = "Close"
    override val cancelBtn = "Cancel"
    override val backBtn = "Back"
    override val searchBtn = "Search"
    override val saveBtn = "Save"
    override val delete = "Delete"
    override val all = "All"
    override val today = "Today"
    override val tomorrow = "Tomorrow"
    override val dayAfterTomorrow = "In 2 Days"

    override val changePassTitle = "Change Password & Name"
    override val changePassSubtitle = "Changes automatically sync with your School Admin account."
    override val changePassUsernameHint: (String) -> String = { "Your login username remains: @$it" }
    override val changePassNameLabel = "Wali Asuh Display Name"
    override val changePassNewLabel = "New Password (Min. 6 characters)"
    override val changePassConfirmLabel = "Confirm New Password"
    override val changePassMinError = "Password must be at least 6 characters!"
    override val changePassMismatchError = "Password confirmation does not match!"
    override val changePassSyncNote = "Your School Admin can still view or reset your password if you forget it."
    override val changePassSaveBtn = "Save Changes"

    override val deleteAccTitle = "Delete Wali Asuh Account?"
    override val deleteAccWarning = "This action permanently deletes your Wali Asuh login from this device and the school cloud server. Student data, medical records, and school reports remain safe."
    override val deleteAccInfoNote = "Make sure you have no active escort schedules before deleting your account."
    override val deleteAccPasswordLabel = "Enter Password to Confirm"
    override val deleteAccBtn = "Delete Permanently"

    override val legalAboutTitle = "About App"
    override val legalAboutRoleBadge = "Health Documentation & Asrama Mentoring"
    override val legalAboutDesc = "Helps Wali Asuh record treatment plans, monitor sick students, track female student Haid logs, monitor Puasa sessions, export PDF reports, and share clean WhatsApp summaries."
    override val legalPrivacyTitle = "Privacy Policy"
    override val legalPrivacyIntro = "Data protection commitment at Wali Asuh Care:"
    override val legalPrivacyPoint1Title = "1. Limited Operational Data"
    override val legalPrivacyPoint1Body = "Student records, health complaints, Haid logs, and worship attendance are strictly used for internal Asrama mentoring documentation at registered schools."
    override val legalPrivacyPoint2Title = "2. Local & School Cloud Storage"
    override val legalPrivacyPoint2Body = "Data is stored securely on-device and synchronized strictly to the school workspace (tenant) authorized by the School Admin."
    override val legalPrivacyPoint3Title = "3. User Control & Deletion"
    override val legalPrivacyPoint3Body = "Wali Asuh or the School Admin can update or permanently delete accounts and records anytime from Profile or Admin Panel."
    override val legalPrivacyPoint4Title = "4. No Third-Party Commercial Sharing"
    override val legalPrivacyPoint4Body = "We never sell or share student personal data with any commercial third party."
    override val legalTermsTitle = "Terms & Conditions"
    override val legalTermsIntro = "Wali Asuh Care terms of use:"
    override val legalTermsPoint1Title = "1. Operational Documentation Tool"
    override val legalTermsPoint1Body = "This app serves as a daily recording aid for Wali Asuh and does not replace a physician's medical diagnosis or official hospital records."
    override val legalTermsPoint2Title = "2. School Data Responsibility"
    override val legalTermsPoint2Body = "Accuracy of student records, account password security, and distribution of PDF/WhatsApp reports are the responsibility of each Wali Asuh and School Admin."
    override val legalTermsPoint3Title = "3. Independent Status"
    override val legalTermsPoint3Body = "Wali Asuh Care is an independent documentation tool and is not an official app of Kemensos RI or Sekolah Rakyat institutions."
    override val legalUnderstoodBtn = "I Understand"
    override val legalUnderstandTermsBtn = "Agree & Close"

    override val rewardAdTitle = "Support & Operational Sustainability"
    override val rewardAdSubtitle = "Exporting PDFs is 100% free. Watching an optional short sponsor clip helps support Wali Asuh Care cloud server costs."
    override val rewardAdPoints = "• PDF printing remains 100% free with no conditions\n• Supports school synchronization server maintenance\n• No student data is ever shared with advertisers"
    override val rewardAdConfirmBtn = "Continue to Print PDF"

    override val vmFastingAlreadyRegistered: (String, String) -> String = { name, title -> "$name is already registered in session $title." }
    override val vmFastingRegisteredSuccess: (String, String) -> String = { name, jenjang -> "$name ($jenjang) successfully registered for Puasa." }
    override val vmPasswordMinError = "New password must be at least 6 characters."
    override val vmPasswordUpdatedSuccess = "Password updated and synchronized with School Admin."
    override val vmPasswordCreatedSuccess = "Password created and synchronized with School Admin."
    override val vmDeletePassRequired = "Enter your account password to confirm."
    override val vmDeletePassMismatch = "Incorrect password. Account deletion cancelled."
    override val vmDeleteSuccess = "Your Wali Asuh account has been permanently deleted."
}
