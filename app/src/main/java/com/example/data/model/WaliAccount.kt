package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wali_accounts")
data class WaliAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val username: String,
    val password: String,
    val dormitory: String = "Asrama Putra", // Asrama Putra / Asrama Putri / Umum
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val schoolId: String = "SRT1-KDR",
    val role: String = ROLE_WALI
) {
    fun isGuruNgaji(): Boolean = role.trim().equals(ROLE_GURU_NGAJI, ignoreCase = true)

    fun isAnakAdminSekolah(): Boolean = role.trim().equals(ROLE_WALI_ANAK_ADMIN, ignoreCase = true)

    fun roleLabel(isEnglish: Boolean = false): String = when {
        isGuruNgaji() -> if (isEnglish) "Mengaji Teacher" else "Guru Ngaji"
        isAnakAdminSekolah() -> if (isEnglish) "Wali Asuh • Schedule Sub-Admin" else "Wali Asuh • Anak Admin Jadwal"
        else -> "Wali Asuh"
    }

    companion object {
        const val ROLE_WALI = "WALI"
        const val ROLE_WALI_ANAK_ADMIN = "WALI_ANAK_ADMIN"
        const val ROLE_GURU_NGAJI = "GURU_NGAJI"
        const val MAX_ANAK_ADMIN_PER_SCHOOL = 7
    }
}
