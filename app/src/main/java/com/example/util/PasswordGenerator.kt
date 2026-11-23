package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.data.model.SchoolProfile
import com.example.data.model.StudentReligionHelper
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.data.model.WaliAccount
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import kotlin.random.Random

object PasswordGenerator {
    private val prefixes = listOf("Wali", "Asuh", "SRT", "Sehat", "Siaga", "Peduli", "Ceria", "Bakti")
    private val symbols = listOf("#", "@", "!")

    /**
     * Menghasilkan password acak yang unik, aman, namun mudah diketik oleh wali asuh.
     * Contoh: Wali#7284, Asuh@3912, SRT#6105
     */
    fun generateRandomPassword(): String {
        val prefix = prefixes[Random.nextInt(prefixes.size)]
        val symbol = symbols[Random.nextInt(symbols.size)]
        val digits = Random.nextInt(1000, 9999)
        return "$prefix$symbol$digits"
    }

    /**
     * Format pesan WhatsApp resmi untuk membagikan akun login kepada Wali Asuh.
     */
    fun formatWhatsAppMessage(
        account: WaliAccount,
        schoolProfile: SchoolProfile = SchoolProfile(),
        isEnglish: Boolean = (AppLanguageManager.currentLanguage == AppLanguage.ENGLISH)
    ): String {
        val effectiveUser = account.username.ifBlank {
            account.name.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
        }
        val schoolDisplay = schoolProfile.schoolName.ifBlank { schoolProfile.shortName }
            .ifBlank { if (isEnglish) "School" else "Sekolah" }
        val referrals = schoolProfile.getAllFaskes().drop(1)
        val faskesText = if (referrals.isNotEmpty()) {
            referrals.joinToString(" / ")
        } else {
            schoolProfile.primaryFaskesName.ifBlank { "UKS Sekolah" }
        }
        val adminDisplay = schoolProfile.adminName.ifBlank {
            if (isEnglish) "School Administrator" else "Administrator Sekolah"
        }
        if (account.isGuruNgaji()) {
            return if (isEnglish) {
                """
                    *MENGAJI TEACHER ACCESS CARD — WALI ASUH CARE*
                    _${schoolDisplay}_
                    
                    Dear *${account.name}*,
                    Here is your official login account (Mengaji Input Portal):
                    
                    👤 *Username:* $effectiveUser
                    🔑 *Password:* ${account.password}
                    📖 *Access Scope:* Student Mengaji Progress & Card (All Students)
                    
                    Use the account above to record and monitor student Mengaji sessions.
                    
                    _Sent by Administrator ($adminDisplay)._
                """.trimIndent()
            } else {
                """
                    *KARTU AKSES LOGIN GURU NGAJI — WALI ASUH CARE*
                    _${schoolDisplay}_
                    
                    Yth. Bapak/Ibu *${account.name}*,
                    Berikut adalah akun akses resmi khusus untuk **Halaman Mengaji** di aplikasi Wali Asuh Care:
                    
                    👤 *Username:* $effectiveUser
                    🔑 *Password:* ${account.password}
                    📖 *Hak Akses:* Input Setoran Mengaji & Kartu Mengaji (Semua Siswa)
                    
                    Gunakan akun di atas untuk mencatat setoran mengaji santri secara langsung.
                    
                    _Pesan ini dikirim oleh Administrator ($adminDisplay)._
                """.trimIndent()
            }
        }
        if (account.isPetugasKesehatan()) {
            return if (isEnglish) {
                """
                    *HEALTH OFFICER ACCESS CARD — WALI ASUH CARE*
                    _${schoolDisplay}_
                    
                    Dear *${account.name}*,
                    Here is your official login account (*Health Officer Portal*):
                    
                    👤 *Username:* $effectiveUser
                    🔑 *Password:* ${account.password}
                    🩺 *Access Scope:* Treatment Plans, Medical Actions, Monthly Health Reports & Student Medical History
                    
                    Use the account above to log medical plans, treatments, and monthly school health recaps.
                    
                    _Sent by Administrator ($adminDisplay)._
                """.trimIndent()
            } else {
                """
                    *KARTU AKSES LOGIN PETUGAS KESEHATAN — WALI ASUH CARE*
                    _${schoolDisplay}_
                    
                    Yth. Bapak/Ibu *${account.name}*,
                    Berikut adalah akun akses resmi khusus untuk **Portal Petugas Kesehatan** di aplikasi Wali Asuh Care:
                    
                    👤 *Username:* $effectiveUser
                    🔑 *Password:* ${account.password}
                    🩺 *Hak Akses:* Rencana & Tindakan Kesehatan, Laporan Bulanan, serta Profil & Riwayat Kesehatan Anak Asuh
                    
                    Gunakan akun di atas untuk mencatat pemeriksaan, rencana berobat, dan memantau laporan kesehatan siswa.
                    
                    _Pesan ini dikirim oleh Administrator ($adminDisplay)._
                """.trimIndent()
            }
        }
        return if (isEnglish) {
            """
                *WALI ASUH CARE APP LOGIN ACCESS CARD*
                _${schoolDisplay}_
                
                Dear *${account.name}*,
                Here is your official login account for the Wali Asuh Care app:
                
                👤 *Username:* $effectiveUser
                🔑 *Password:* ${account.password}
                🏢 *Asrama Assignment:* ${account.dormitory}
                
                Use the account above to monitor student health and referrals to $faskesText.
                
                _Sent by Administrator ($adminDisplay)._
            """.trimIndent()
        } else {
            """
                *KARTU AKSES LOGIN APLIKASI WALI ASUH CARE*
                _${schoolDisplay}_
                
                Yth. Bapak/Ibu *${account.name}*,
                Berikut adalah akun akses resmi untuk login ke aplikasi Wali Asuh Care:
                
                👤 *Username:* $effectiveUser
                🔑 *Password:* ${account.password}
                🏢 *Tugas Asrama:* ${account.dormitory}
                
                Gunakan akun di atas untuk memantau kesehatan anak asuh dan rujukan ke $faskesText.
                
                _Pesan ini dikirim oleh Administrator ($adminDisplay)._
            """.trimIndent()
        }
    }

    /**
     * Format pesan WhatsApp / Share resmi untuk membagikan Kartu Akses Orang Tua.
     */
    fun formatParentAccessWhatsAppMessage(
        link: ParentAccessLink,
        schoolProfile: SchoolProfile = SchoolProfile(),
        isEnglish: Boolean = (AppLanguageManager.currentLanguage == AppLanguage.ENGLISH),
        isFemaleStudent: Boolean = false
    ): String {
        val schoolDisplay = schoolProfile.schoolName.ifBlank { schoolProfile.shortName }
            .ifBlank { if (isEnglish) "School" else "Sekolah" }
        val parentDisplay = link.parentName.ifBlank {
            if (isEnglish) "Parent / Guardian of ${link.studentName}" else "Bapak/Ibu Orang Tua dari ${link.studentName}"
        }
        val waliDisplay = link.assignedWaliName.ifBlank {
            if (isEnglish) "Foster Caregiver" else "Wali Asuh"
        }
        val isChristian = StudentReligionHelper.isChristian(link.studentReligion)
        val enabledPermissions = buildList {
            if (link.canViewProfile) add(if (isEnglish) "Child Profile" else "Profil Anak")
            if (isChristian) {
                add(if (isEnglish) "Christian Worship & Reports" else "Ibadah Kristen & Laporan")
            } else {
                if (link.canViewSholat) add(if (isEnglish) "5 Daily Prayers" else "Pantauan Sholat")
                if (link.canViewMengaji) add(if (isEnglish) "Mengaji History" else "Riwayat Mengaji")
            }
            if (link.canViewMedical) add(if (isEnglish) "Medical History" else "Riwayat Sakit")
            if (link.canViewFasting) add(if (isEnglish) "Fasting History" else "Riwayat Puasa")
            if (isFemaleStudent && link.canViewHaid) add(if (isEnglish) "Haid History" else "Riwayat Haid")
            if (link.canViewTabungan) add(if (isEnglish) "Child Savings Ledger" else "Tabungan Anak")
        }.ifEmpty {
            listOf(if (isEnglish) "Messages with Caregiver" else "Pesan dengan Wali Asuh")
        }.joinToString(", ")

        return if (isEnglish) {
            """
                *PARENT ACCESS CARD — WALI ASUH CARE*
                _${schoolDisplay}_

                Dear *$parentDisplay*,
                Here is your official parent login account to monitor *${link.studentName}* (${link.studentClass.ifBlank { "-" }} • ${link.studentDorm.ifBlank { "-" }}):

                👤 *Username:* ${link.username}
                🔑 *Password:* ${link.password}
                🏫 *School Code:* ${link.schoolId}
                👨‍👩‍👧 *Foster Caregiver:* $waliDisplay
                📋 *Available Access:* $enabledPermissions

                Please open the Wali Asuh Care app and sign in using the username & password above.
            """.trimIndent()
        } else {
            """
                *KARTU AKSES ORANG TUA — WALI ASUH CARE*
                _${schoolDisplay}_

                Yth. *$parentDisplay*,
                Berikut adalah kartu akses login resmi untuk memantau perkembangan ananda *${link.studentName}* (${link.studentClass.ifBlank { "-" }} • ${link.studentDorm.ifBlank { "-" }}):

                👤 *Username:* ${link.username}
                🔑 *Password:* ${link.password}
                🏫 *Kode Sekolah:* ${link.schoolId}
                👨‍👩‍👧 *Wali Asuh Pembina:* $waliDisplay
                📋 *Akses Dibuka:* $enabledPermissions

                Silakan buka aplikasi Wali Asuh Care dan login menggunakan username & password di atas.
            """.trimIndent()
        }
    }

    /**
     * Membagikan kartu akses orang tua:
     * - Jika nomor HP orang tua tersedia (>= 8 digit), buka WhatsApp langsung ke nomor tujuan.
     * - Jika nomor HP belum diisi, gunakan dialog share standar (share WA biasa).
     */
    fun shareParentAccessCard(
        context: Context,
        link: ParentAccessLink,
        schoolProfile: SchoolProfile = SchoolProfile(),
        isEnglish: Boolean = (AppLanguageManager.currentLanguage == AppLanguage.ENGLISH),
        isFemaleStudent: Boolean = false
    ) {
        val message = formatParentAccessWhatsAppMessage(link, schoolProfile, isEnglish, isFemaleStudent)
        val waDigits = StudentCsvParser.formatWhatsAppDigits(link.parentPhone)
        if (waDigits.length >= 9) {
            val waUrl = "https://wa.me/$waDigits?text=${Uri.encode(message)}"
            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val started = runCatching { context.startActivity(waIntent) }.isSuccess
            if (started) return
        }
        val chooserTitle = if (isEnglish) "Share Parent Access Card" else "Bagikan Kartu Akses Orang Tua"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }

    fun formatTabunganStudentWaSummary(
        student: ChildStudent,
        transactions: List<TabunganTransaction>,
        waliName: String,
        schoolProfile: SchoolProfile = SchoolProfile(),
        isEnglish: Boolean = (AppLanguageManager.currentLanguage == AppLanguage.ENGLISH)
    ): String {
        val summary = TabunganHelper.computeSummaryForStudent(transactions, student)
        val schoolDisplay = schoolProfile.schoolName.ifBlank { schoolProfile.shortName }
            .ifBlank { if (isEnglish) "School" else "Sekolah" }
        val activeTxs = transactions
            .filter { TabunganHelper.matchesStudent(it, student) && TabunganHelper.isActive(it.status) }
            .sortedByDescending { it.transactionDate }
            .take(8)
        val dateFmt = java.text.SimpleDateFormat("d MMM", if (isEnglish) java.util.Locale.ENGLISH else java.util.Locale("id", "ID"))

        return buildString {
            appendLine(if (isEnglish) "*PERSONAL SAVINGS REPORT (TABUNGAN)*" else "*LAPORAN TABUNGAN TITIPAN ANAK*")
            appendLine("_${schoolDisplay}_")
            appendLine()
            appendLine(if (isEnglish) "Student: *${student.name}* (${student.classGrade.ifBlank { "-" }})" else "Ananda: *${student.name}* (${student.classGrade.ifBlank { "-" }})")
            appendLine(if (isEnglish) "Caregiver (Holder): *$waliName*" else "Wali Asuh Pemegang Titipan: *$waliName*")
            appendLine("• ${if (isEnglish) "Total Deposit (+)" else "Total Titip Masuk (+)"}: *${TabunganHelper.formatRupiah(summary.totalIn)}*")
            appendLine("• ${if (isEnglish) "Total Expense (-)" else "Total Pengeluaran (-)"}: *${TabunganHelper.formatRupiah(summary.totalOut)}*")
            appendLine("• ${if (isEnglish) "Current Balance" else "Saldo Tersisa"}: *${TabunganHelper.formatRupiah(summary.balance)}*")
            if (activeTxs.isNotEmpty()) {
                appendLine()
                appendLine(if (isEnglish) "*Latest Transactions:*" else "*Mutasi Terakhir:*")
                activeTxs.forEachIndexed { idx, tx ->
                    val sign = if (TabunganHelper.isIncome(tx.transactionType)) "+" else "-"
                    val dLabel = dateFmt.format(java.util.Date(tx.transactionDate))
                    val title = tx.title.ifBlank { if (TabunganHelper.isIncome(tx.transactionType)) "Titip Masuk" else "Pengeluaran" }
                    appendLine("${idx + 1}. $dLabel • $sign${TabunganHelper.formatRupiah(tx.amount)} ($title)")
                }
            }
            appendLine()
            append(if (isEnglish) "_Private record managed by Foster Caregiver._" else "_Catatan pribadi ini hanya dikelola oleh Wali Asuh pembina._")
        }
    }
}
