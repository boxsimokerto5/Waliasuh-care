package com.example.util

import com.example.data.model.SchoolProfile
import com.example.data.model.WaliAccount
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
        schoolProfile: SchoolProfile = SchoolProfile()
    ): String {
        val effectiveUser = account.username.ifBlank {
            account.name.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
        }
        val schoolDisplay = schoolProfile.schoolName.ifBlank { schoolProfile.shortName }.ifBlank { "Sekolah" }
        val referrals = schoolProfile.getAllFaskes().drop(1)
        val faskesText = if (referrals.isNotEmpty()) {
            referrals.joinToString(" / ")
        } else {
            schoolProfile.primaryFaskesName.ifBlank { "UKS Sekolah" }
        }
        val adminDisplay = schoolProfile.adminName.ifBlank { "Administrator Sekolah" }
        return """
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
