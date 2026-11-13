package com.example.data.model

data class SchoolProfile(
    val schoolName: String = "Sekolah Rakyat SRT 1 Kabupaten Kediri",
    val shortName: String = "SRT 1 Kediri",
    val adminName: String = "Admin Utama",
    val city: String = "Kabupaten Kediri",
    val tenantCode: String = "SRT1-KDR",
    val primaryFaskesName: String = "UKS Sekolah",
    val secondaryFaskesName: String = "Puskesmas Wates",
    val additionalFaskesList: List<String> = listOf("Puskesmas Plosoklaten")
) {
    /**
     * Mengembalikan seluruh daftar faskes terdaftar:
     * Faskes 1 (Utama): UKS Sekolah
     * Faskes 2: Rujukan kedua (misal: Puskesmas Wates)
     * Faskes 3+: Rujukan ketiga dan seterusnya (misal: Puskesmas Plosoklaten, RSUD, dsb.)
     */
    fun getAllFaskes(): List<String> {
        val list = mutableListOf<String>()
        val primary = primaryFaskesName.trim().ifBlank { "UKS Sekolah" }
        list.add(primary)

        val sec = secondaryFaskesName.trim()
        if (sec.isNotBlank() && !list.any { it.equals(sec, ignoreCase = true) }) {
            list.add(sec)
        }

        additionalFaskesList.forEach { f ->
            val clean = f.trim()
            if (clean.isNotBlank() && !list.any { it.equals(clean, ignoreCase = true) }) {
                list.add(clean)
            }
        }
        return list
    }

    fun getReferralFaskesSummary(): String {
        val referrals = getAllFaskes().drop(1)
        return when {
            referrals.isNotEmpty() && city.isNotBlank() -> "${referrals.joinToString(" • ")} ($city)"
            referrals.isNotEmpty() -> referrals.joinToString(" • ")
            city.isNotBlank() -> "${primaryFaskesName.ifBlank { "UKS Sekolah" }} • $city"
            else -> primaryFaskesName.ifBlank { "UKS Sekolah" }
        }
    }

    companion object {
        fun sanitizeShortName(schoolName: String, savedShortName: String?): String {
            val cleanFull = schoolName.trim().ifBlank { "Sekolah Rakyat SRT 1 Kabupaten Kediri" }
            val cleanShort = savedShortName?.trim().orEmpty()
            if (cleanShort.isBlank()) return cleanFull
            if (cleanFull.length > 15 && cleanShort.equals(cleanFull.take(15).trim(), ignoreCase = true)) {
                return cleanFull
            }
            return cleanShort
        }
    }
}
