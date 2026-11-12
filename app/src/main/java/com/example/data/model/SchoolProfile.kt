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
}
