package com.example.data.model

import org.json.JSONArray

enum class DashboardIconKey(val key: String) {
    BEROBAT("BEROBAT"),
    KUNJUNGAN("KUNJUNGAN"),
    ORANG_TUA("ORANG_TUA"),
    ANAK_ASUH("ANAK_ASUH"),
    PUASA("PUASA"),
    HAID("HAID"),
    PESAN("PESAN"),
    MENGAJI("MENGAJI"),
    TABUNGAN("TABUNGAN"),
    KONSELING("KONSELING"),
    JADWAL_WALI("JADWAL_WALI"),
    KLAIM_ANAK_ASUH("KLAIM_ANAK_ASUH");

    companion object {
        val DEFAULT_ORDER: List<DashboardIconKey> = entries.toList()

        fun fromKey(raw: String?): DashboardIconKey? {
            val clean = raw?.trim()?.uppercase().orEmpty()
            return entries.firstOrNull { it.key == clean }
        }

        fun normalizeOrder(rawKeys: List<String>?): List<DashboardIconKey> {
            if (rawKeys.isNullOrEmpty()) return DEFAULT_ORDER
            val result = mutableListOf<DashboardIconKey>()
            for (raw in rawKeys) {
                val parsed = fromKey(raw)
                if (parsed != null && parsed !in result) {
                    result.add(parsed)
                }
            }
            for (fallback in DEFAULT_ORDER) {
                if (fallback !in result) {
                    result.add(fallback)
                }
            }
            return result
        }

        fun serializeJson(order: List<DashboardIconKey>): String {
            val normalized = normalizeOrder(order.map { it.key })
            val arr = JSONArray()
            normalized.forEach { arr.put(it.key) }
            return arr.toString()
        }

        fun parseJson(raw: String?): List<DashboardIconKey>? {
            if (raw.isNullOrBlank()) return null
            return try {
                val arr = JSONArray(raw)
                val keys = buildList {
                    for (i in 0 until arr.length()) {
                        val item = arr.optString(i, "").trim()
                        if (item.isNotBlank()) add(item)
                    }
                }
                if (keys.isEmpty()) null else normalizeOrder(keys)
            } catch (_: Exception) {
                null
            }
        }
    }
}

data class DashboardIconLayoutConfig(
    val order: List<DashboardIconKey> = DashboardIconKey.DEFAULT_ORDER,
    val updatedAt: Long = 0L
)
