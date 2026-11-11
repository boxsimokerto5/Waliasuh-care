package com.example.util

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
    fun formatWhatsAppMessage(account: WaliAccount): String {
        val effectiveUser = account.username.ifBlank {
            account.name.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
        }
        return """
            *KARTU AKSES LOGIN APLIKASI WALI ASUH CARE*
            _Sekolah Rakyat SRT 1 Kabupaten Kediri_
            
            Yth. Bapak/Ibu *${account.name}*,
            Berikut adalah akun akses resmi untuk login ke aplikasi Wali Asuh Care:
            
            👤 *Username:* $effectiveUser
            🔑 *Password:* ${account.password}
            🏢 *Tugas Asrama:* ${account.dormitory}
            
            Gunakan akun di atas untuk memantau kesehatan anak asuh dan rujukan ke Puskesmas Wates / Puskesmas Plosoklaten.
            
            _Pesan ini dikirim otomatis oleh Administrator Utama (Eko Wahyudi)._
        """.trimIndent()
    }
}
