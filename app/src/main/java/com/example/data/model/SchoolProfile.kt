package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class FaskesContactInfo(
    val name: String,
    val address: String = "",
    val phone: String = ""
)

data class SchoolProfile(
    val schoolName: String = "Sekolah Rakyat SRT 1 Kabupaten Kediri",
    val shortName: String = "SRT 1 Kediri",
    val adminName: String = "Admin Utama",
    val city: String = "Kabupaten Kediri",
    val tenantCode: String = "SRT1-KDR",
    val primaryFaskesName: String = "UKS Sekolah",
    val secondaryFaskesName: String = "Puskesmas Wates",
    val additionalFaskesList: List<String> = listOf("Puskesmas Plosoklaten"),
    val faskesContacts: Map<String, FaskesContactInfo> = emptyMap()
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

    /**
     * Mengembalikan informasi kontak (nama, alamat, telepon) untuk satu faskes tertentu.
     * Fallback alamat/telepon Kediri hanya berlaku untuk tenant default SRT1-KDR bila belum diubah admin.
     */
    fun getFaskesContact(faskesName: String): FaskesContactInfo {
        val cleanName = faskesName.trim()
        if (cleanName.isBlank()) return FaskesContactInfo(name = "")

        val matched = faskesContacts.entries.firstOrNull {
            it.key.trim().equals(cleanName, ignoreCase = true) ||
                it.value.name.trim().equals(cleanName, ignoreCase = true)
        }?.value

        val isDefaultKediri = tenantCode.equals("SRT1-KDR", ignoreCase = true)
        val defaultAddress = when {
            matched != null && matched.address.isNotBlank() -> matched.address.trim()
            cleanName.equals(primaryFaskesName.trim().ifBlank { "UKS Sekolah" }, ignoreCase = true) ->
                if (city.isNotBlank()) "Lingkungan $schoolName • $city" else "Lingkungan $schoolName"
            isDefaultKediri && cleanName.equals("Puskesmas Wates", ignoreCase = true) ->
                "Jl. Raya Wates No. 45, Kab. Kediri"
            isDefaultKediri && cleanName.equals("Puskesmas Plosoklaten", ignoreCase = true) ->
                "Jl. Raya Plosoklaten No. 12, Kab. Kediri"
            else -> ""
        }
        val defaultPhone = when {
            matched != null && matched.phone.isNotBlank() -> matched.phone.trim()
            isDefaultKediri && cleanName.equals("Puskesmas Wates", ignoreCase = true) -> "(0354) 441012"
            isDefaultKediri && cleanName.equals("Puskesmas Plosoklaten", ignoreCase = true) -> "(0354) 479155"
            else -> ""
        }

        return FaskesContactInfo(
            name = cleanName,
            address = defaultAddress,
            phone = defaultPhone
        )
    }

    fun getAllFaskesWithDetails(): List<FaskesContactInfo> {
        return getAllFaskes().map { getFaskesContact(it) }
    }

    fun getReferralFaskesWithDetails(): List<FaskesContactInfo> {
        return getAllFaskes().drop(1).map { getFaskesContact(it) }
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

        fun serializeFaskesContacts(contacts: Map<String, FaskesContactInfo>): String {
            val arr = JSONArray()
            contacts.values
                .filter { it.name.isNotBlank() && (it.address.isNotBlank() || it.phone.isNotBlank()) }
                .forEach { info ->
                    val obj = JSONObject()
                    obj.put("name", info.name.trim())
                    obj.put("address", info.address.trim())
                    obj.put("phone", info.phone.trim())
                    arr.put(obj)
                }
            return arr.toString()
        }

        fun parseFaskesContacts(raw: String?): Map<String, FaskesContactInfo> {
            if (raw.isNullOrBlank()) return emptyMap()
            return try {
                val arr = JSONArray(raw)
                val result = linkedMapOf<String, FaskesContactInfo>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val name = obj.optString("name", "").trim()
                    if (name.isBlank()) continue
                    val address = obj.optString("address", "").trim()
                    val phone = obj.optString("phone", "").trim()
                    result[name] = FaskesContactInfo(
                        name = name,
                        address = address,
                        phone = phone
                    )
                }
                result
            } catch (_: Exception) {
                emptyMap()
            }
        }
    }
}
