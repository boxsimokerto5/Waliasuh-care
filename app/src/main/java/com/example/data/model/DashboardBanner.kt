package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class DashboardBannerActionType(val key: String) {
    NONE("NONE"),
    INTERNAL_FEATURE("INTERNAL_FEATURE"),
    EXTERNAL_LINK("EXTERNAL_LINK");

    companion object {
        fun fromKey(raw: String?): DashboardBannerActionType {
            return entries.firstOrNull { it.key.equals(raw?.trim(), ignoreCase = true) }
                ?: NONE
        }
    }
}

enum class DashboardBannerFeatureTarget(
    val key: String,
    val labelId: String,
    val labelEn: String,
    val groupId: String,
    val groupEn: String
) {
    // Berobat & Kesehatan
    FEATURE_FASKES_INFO(
        key = "FEATURE_FASKES_INFO",
        labelId = "Info Faskes & Puskesmas Rujukan",
        labelEn = "Referral Faskes & Puskesmas Info",
        groupId = "Berobat",
        groupEn = "Treatment"
    ),
    FEATURE_BEROBAT_HUB(
        key = "FEATURE_BEROBAT_HUB",
        labelId = "Menu Kategori Berobat",
        labelEn = "Treatment Category Hub",
        groupId = "Berobat",
        groupEn = "Treatment"
    ),
    FEATURE_CREATE_PLAN(
        key = "FEATURE_CREATE_PLAN",
        labelId = "Buat Rencana Berobat Baru",
        labelEn = "Create New Treatment Plan",
        groupId = "Berobat",
        groupEn = "Treatment"
    ),
    FEATURE_DIRECT_TREATMENT(
        key = "FEATURE_DIRECT_TREATMENT",
        labelId = "Input Berobat Langsung",
        labelEn = "Log Direct Treatment",
        groupId = "Berobat",
        groupEn = "Treatment"
    ),
    FEATURE_PLAN_PAGE(
        key = "FEATURE_PLAN_PAGE",
        labelId = "Halaman Daftar Rencana Berobat",
        labelEn = "Treatment Plans Screen",
        groupId = "Berobat",
        groupEn = "Treatment"
    ),
    FEATURE_RECORDS_PAGE(
        key = "FEATURE_RECORDS_PAGE",
        labelId = "Halaman Catatan Tindakan & Riwayat",
        labelEn = "Medical Actions & History Screen",
        groupId = "Berobat",
        groupEn = "Treatment"
    ),

    // Puasa
    FEATURE_PUASA_HUB(
        key = "FEATURE_PUASA_HUB",
        labelId = "Menu Kategori Puasa",
        labelEn = "Fasting Category Hub",
        groupId = "Puasa",
        groupEn = "Fasting"
    ),
    FEATURE_INPUT_PUASA(
        key = "FEATURE_INPUT_PUASA",
        labelId = "Input Peserta Puasa (Barcode/Cari)",
        labelEn = "Log Fasting Participants",
        groupId = "Puasa",
        groupEn = "Fasting"
    ),
    FEATURE_CREATE_PUASA_SESSION(
        key = "FEATURE_CREATE_PUASA_SESSION",
        labelId = "Buat Sesi Puasa Baru",
        labelEn = "Create New Fasting Session",
        groupId = "Puasa",
        groupEn = "Fasting"
    ),
    FEATURE_DAFTAR_PUASA(
        key = "FEATURE_DAFTAR_PUASA",
        labelId = "Daftar Siswa Berpuasa & PDF",
        labelEn = "Fasting Students List & PDF",
        groupId = "Puasa",
        groupEn = "Fasting"
    ),
    FEATURE_CHECKLIST_PUASA(
        key = "FEATURE_CHECKLIST_PUASA",
        labelId = "Checklist Sahur & Berbuka",
        labelEn = "Sahur & Iftar Checklist",
        groupId = "Puasa",
        groupEn = "Fasting"
    ),
    FEATURE_RANKING_PUASA(
        key = "FEATURE_RANKING_PUASA",
        labelId = "Ranking & Rekap Puasa",
        labelEn = "Fasting Ranking & Summary",
        groupId = "Puasa",
        groupEn = "Fasting"
    ),

    // Haid
    FEATURE_HAID_HUB(
        key = "FEATURE_HAID_HUB",
        labelId = "Menu Kategori Haid",
        labelEn = "Period Category Hub",
        groupId = "Haid",
        groupEn = "Period"
    ),
    FEATURE_INPUT_HAID(
        key = "FEATURE_INPUT_HAID",
        labelId = "Input Catatan Haid Siswi",
        labelEn = "Log Student Period",
        groupId = "Haid",
        groupEn = "Period"
    ),
    FEATURE_DAFTAR_HAID(
        key = "FEATURE_DAFTAR_HAID",
        labelId = "Daftar Siswi Sedang Haid",
        labelEn = "Active Period List",
        groupId = "Haid",
        groupEn = "Period"
    ),
    FEATURE_DAFTAR_SUCI(
        key = "FEATURE_DAFTAR_SUCI",
        labelId = "Daftar Siswi Masa Suci",
        labelEn = "Clean Period History",
        groupId = "Haid",
        groupEn = "Period"
    ),
    FEATURE_FIQIH_HAID(
        key = "FEATURE_FIQIH_HAID",
        labelId = "Panduan Fiqih Haid Wanita",
        labelEn = "Fiqih Period Guide",
        groupId = "Haid",
        groupEn = "Period"
    ),

    // Mengaji
    FEATURE_MENGAJI_HUB(
        key = "FEATURE_MENGAJI_HUB",
        labelId = "Menu Kategori Mengaji",
        labelEn = "Recitation Category Hub",
        groupId = "Mengaji",
        groupEn = "Recitation"
    ),
    // Tabungan
    FEATURE_TABUNGAN_HUB(
        key = "FEATURE_TABUNGAN_HUB",
        labelId = "Menu Tabungan Anak Asuh",
        labelEn = "Student Savings Center",
        groupId = "Tabungan",
        groupEn = "Savings"
    ),

    // Konseling Rahasia
    FEATURE_KONSELING_HUB(
        key = "FEATURE_KONSELING_HUB",
        labelId = "Menu Konseling Rahasia Anak",
        labelEn = "Private Student Counseling",
        groupId = "Konseling",
        groupEn = "Counseling"
    ),
    FEATURE_INPUT_MENGAJI(
        key = "FEATURE_INPUT_MENGAJI",
        labelId = "Input Setoran Mengaji",
        labelEn = "Log Recitation Session",
        groupId = "Mengaji",
        groupEn = "Recitation"
    ),
    FEATURE_RIWAYAT_MENGAJI(
        key = "FEATURE_RIWAYAT_MENGAJI",
        labelId = "Riwayat & Rekap Mengaji",
        labelEn = "Recitation History & Recap",
        groupId = "Mengaji",
        groupEn = "Recitation"
    ),
    FEATURE_KARTU_MENGAJI(
        key = "FEATURE_KARTU_MENGAJI",
        labelId = "Kartu Mengaji Bulanan & PDF",
        labelEn = "Monthly Recitation Card & PDF",
        groupId = "Mengaji",
        groupEn = "Recitation"
    ),

    // Pesan Shift
    FEATURE_PESAN_HUB(
        key = "FEATURE_PESAN_HUB",
        labelId = "Menu Kategori Pesan Shift",
        labelEn = "Shift Notes Category Hub",
        groupId = "Pesan Shift",
        groupEn = "Shift Notes"
    ),
    FEATURE_DAFTAR_PESAN(
        key = "FEATURE_DAFTAR_PESAN",
        labelId = "Daftar Pesan Operan Shift",
        labelEn = "Shift Handover Messages",
        groupId = "Pesan Shift",
        groupEn = "Shift Notes"
    ),
    FEATURE_BUAT_PESAN(
        key = "FEATURE_BUAT_PESAN",
        labelId = "Buat Pesan Shift Baru",
        labelEn = "Create New Shift Note",
        groupId = "Pesan Shift",
        groupEn = "Shift Notes"
    ),
    FEATURE_RIWAYAT_PESAN(
        key = "FEATURE_RIWAYAT_PESAN",
        labelId = "Riwayat Pesan Shift Lampau",
        labelEn = "Past Shift Notes History",
        groupId = "Pesan Shift",
        groupEn = "Shift Notes"
    ),

    // Hubungan Orang Tua
    FEATURE_ORTU_HUB(
        key = "FEATURE_ORTU_HUB",
        labelId = "Menu Kategori Hubungan Ortu",
        labelEn = "Parents Category Hub",
        groupId = "Hubungan Ortu",
        groupEn = "Parents"
    ),
    FEATURE_PILIH_ANAK_ASUH(
        key = "FEATURE_PILIH_ANAK_ASUH",
        labelId = "Pilih / Klaim Anak Asuh Binaan",
        labelEn = "Assign Foster Students",
        groupId = "Hubungan Ortu",
        groupEn = "Parents"
    ),
    FEATURE_KARTU_AKSES_ORTU(
        key = "FEATURE_KARTU_AKSES_ORTU",
        labelId = "Kartu Akses Portal Orang Tua",
        labelEn = "Parent Access Cards",
        groupId = "Hubungan Ortu",
        groupEn = "Parents"
    ),
    FEATURE_PILIHAN_AKSES_ORTU(
        key = "FEATURE_PILIHAN_AKSES_ORTU",
        labelId = "Pengaturan Hak Akses Ortu",
        labelEn = "Parent Access Permissions",
        groupId = "Hubungan Ortu",
        groupEn = "Parents"
    ),
    FEATURE_CHAT_ORTU(
        key = "FEATURE_CHAT_ORTU",
        labelId = "Pesan / Chat Orang Tua",
        labelEn = "Parent Two-Way Chat",
        groupId = "Hubungan Ortu",
        groupEn = "Parents"
    ),

    // Kunjungan
    FEATURE_KUNJUNGAN_HUB(
        key = "FEATURE_KUNJUNGAN_HUB",
        labelId = "Menu Kategori Kunjungan",
        labelEn = "Visits Category Hub",
        groupId = "Kunjungan",
        groupEn = "Visits"
    ),
    FEATURE_JADWAL_KUNJUNGAN(
        key = "FEATURE_JADWAL_KUNJUNGAN",
        labelId = "Jadwal Sesi Kunjungan",
        labelEn = "Visit Sessions Schedule",
        groupId = "Kunjungan",
        groupEn = "Visits"
    ),
    FEATURE_DAFTAR_HADIR_KUNJUNGAN(
        key = "FEATURE_DAFTAR_HADIR_KUNJUNGAN",
        labelId = "Daftar Hadir & Scan Kunjungan",
        labelEn = "Visit Check-In & Attendance",
        groupId = "Kunjungan",
        groupEn = "Visits"
    ),
    FEATURE_LAPORAN_KUNJUNGAN(
        key = "FEATURE_LAPORAN_KUNJUNGAN",
        labelId = "Laporan Kehadiran Kunjungan",
        labelEn = "Visit Attendance Report",
        groupId = "Kunjungan",
        groupEn = "Visits"
    ),
    FEATURE_SISWA_PER_KECAMATAN(
        key = "FEATURE_SISWA_PER_KECAMATAN",
        labelId = "Daftar Siswa per Kecamatan",
        labelEn = "Students by District",
        groupId = "Kunjungan",
        groupEn = "Visits"
    ),

    // Data & Navigasi
    FEATURE_STUDENTS_PAGE(
        key = "FEATURE_STUDENTS_PAGE",
        labelId = "Halaman Data Anak Asuh",
        labelEn = "Foster Students Screen",
        groupId = "Data & Navigasi",
        groupEn = "Data & Navigation"
    ),
    FEATURE_PROFILE_PAGE(
        key = "FEATURE_PROFILE_PAGE",
        labelId = "Halaman Profil & Pengaturan",
        labelEn = "Profile & Settings Screen",
        groupId = "Data & Navigasi",
        groupEn = "Data & Navigation"
    );

    fun label(isEn: Boolean): String = if (isEn) labelEn else labelId
    fun group(isEn: Boolean): String = if (isEn) groupEn else groupId

    companion object {
        fun fromKey(raw: String?): DashboardBannerFeatureTarget? {
            return entries.firstOrNull { it.key.equals(raw?.trim(), ignoreCase = true) }
        }
    }
}

data class UploadedBannerMedia(
    val pbRecordId: String,
    val pbCollectionId: String,
    val pbFilename: String,
    val cdnImageUrl: String,
    val fallbackApiFileUrl: String
)

data class DashboardBannerItem(
    val id: String,
    val title: String = "",
    val subtitle: String = "",
    val badgeText: String = "",
    val imageUrl: String = "",
    val fallbackImageUrl: String = "",
    val pbRecordId: String = "",
    val pbCollectionId: String = "",
    val pbFilename: String = "",
    val actionType: DashboardBannerActionType = DashboardBannerActionType.NONE,
    val actionTarget: String = "",
    val isDefaultFaskes: Boolean = false,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0
) {
    fun resolvePrimaryImageUrl(): String {
        val explicit = imageUrl.trim()
        if (explicit.isNotBlank()) return explicit
        return buildR2CdnUrl(pbCollectionId, pbRecordId, pbFilename)
    }

    fun resolveFallbackImageUrl(pbBaseUrl: String = DEFAULT_POCKETBASE_BASE_URL): String {
        val explicitFallback = fallbackImageUrl.trim()
        if (explicitFallback.isNotBlank()) return explicitFallback
        return buildPbApiFileUrl(pbBaseUrl, pbCollectionId, pbRecordId, pbFilename)
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("subtitle", subtitle)
        put("badgeText", badgeText)
        put("imageUrl", imageUrl)
        put("fallbackImageUrl", fallbackImageUrl)
        put("pbRecordId", pbRecordId)
        put("pbCollectionId", pbCollectionId)
        put("pbFilename", pbFilename)
        put("actionType", actionType.key)
        put("actionTarget", actionTarget)
        put("isDefaultFaskes", isDefaultFaskes)
        put("isEnabled", isEnabled)
        put("sortOrder", sortOrder)
    }

    companion object {
        const val DEFAULT_FASKES_BANNER_ID = "default_faskes_banner"
        const val R2_CDN_BASE_URL = "https://waliasuhcareapp.areksurabaya.my.id"
        const val DEFAULT_POCKETBASE_BASE_URL = "https://waliasuh.areksurabaya.my.id"

        fun buildR2CdnUrl(collectionId: String, recordId: String, filename: String): String {
            val col = collectionId.trim().trim('/')
            val rec = recordId.trim().trim('/')
            val file = filename.trim().trimStart('/')
            if (col.isBlank() || rec.isBlank() || file.isBlank()) return ""
            return "${R2_CDN_BASE_URL.trimEnd('/')}/$col/$rec/$file"
        }

        fun buildPbApiFileUrl(
            pbBaseUrl: String,
            collectionId: String,
            recordId: String,
            filename: String
        ): String {
            val base = pbBaseUrl.trim().trimEnd('/').ifBlank { DEFAULT_POCKETBASE_BASE_URL }
            val col = collectionId.trim().trim('/')
            val rec = recordId.trim().trim('/')
            val file = filename.trim().trimStart('/')
            if (col.isBlank() || rec.isBlank() || file.isBlank()) return ""
            return "$base/api/files/$col/$rec/$file"
        }

        fun defaultFaskes(): DashboardBannerItem = DashboardBannerItem(
            id = DEFAULT_FASKES_BANNER_ID,
            title = "",
            subtitle = "",
            badgeText = "",
            imageUrl = "",
            actionType = DashboardBannerActionType.INTERNAL_FEATURE,
            actionTarget = DashboardBannerFeatureTarget.FEATURE_FASKES_INFO.key,
            isDefaultFaskes = true,
            isEnabled = true,
            sortOrder = 0
        )

        fun fromJson(obj: JSONObject, fallbackIndex: Int = 0): DashboardBannerItem {
            val rawId = obj.optString("id", "").trim().ifBlank {
                if (obj.optBoolean("isDefaultFaskes", false)) DEFAULT_FASKES_BANNER_ID
                else "banner_${System.currentTimeMillis()}_$fallbackIndex"
            }
            return DashboardBannerItem(
                id = rawId,
                title = obj.optString("title", ""),
                subtitle = obj.optString("subtitle", ""),
                badgeText = obj.optString("badgeText", ""),
                imageUrl = obj.optString("imageUrl", ""),
                fallbackImageUrl = obj.optString("fallbackImageUrl", ""),
                pbRecordId = obj.optString("pbRecordId", ""),
                pbCollectionId = obj.optString("pbCollectionId", ""),
                pbFilename = obj.optString("pbFilename", ""),
                actionType = DashboardBannerActionType.fromKey(obj.optString("actionType", "NONE")),
                actionTarget = obj.optString("actionTarget", ""),
                isDefaultFaskes = obj.optBoolean("isDefaultFaskes", rawId == DEFAULT_FASKES_BANNER_ID),
                isEnabled = obj.optBoolean("isEnabled", true),
                sortOrder = obj.optInt("sortOrder", fallbackIndex)
            )
        }
    }
}

data class DashboardBannerConfig(
    val items: List<DashboardBannerItem> = listOf(DashboardBannerItem.defaultFaskes()),
    val slideIntervalSeconds: Int = DEFAULT_INTERVAL_SECONDS,
    val updatedAt: Long = 0L
) {
    fun normalized(): DashboardBannerConfig {
        val reindexed = items
            .sortedBy { it.sortOrder }
            .distinctBy { it.id }
            .mapIndexed { idx, item -> item.copy(sortOrder = idx) }
        return copy(
            items = reindexed,
            slideIntervalSeconds = clampIntervalSeconds(slideIntervalSeconds)
        )
    }

    fun toJsonString(): String {
        val clean = normalized()
        val arr = JSONArray()
        clean.items.forEach { arr.put(it.toJson()) }
        return JSONObject().apply {
            put("slideIntervalSeconds", clean.slideIntervalSeconds)
            put("updatedAt", clean.updatedAt)
            put("items", arr)
        }.toString()
    }

    companion object {
        const val DEFAULT_INTERVAL_SECONDS = 3
        val ALLOWED_INTERVAL_SECONDS = listOf(2, 3, 4, 5)

        fun clampIntervalSeconds(raw: Int): Int {
            return if (raw in ALLOWED_INTERVAL_SECONDS) raw else DEFAULT_INTERVAL_SECONDS
        }

        fun defaultConfig(): DashboardBannerConfig = DashboardBannerConfig(
            items = listOf(DashboardBannerItem.defaultFaskes()),
            slideIntervalSeconds = DEFAULT_INTERVAL_SECONDS,
            updatedAt = 0L
        )

        fun fromJsonString(raw: String?): DashboardBannerConfig? {
            if (raw.isNullOrBlank()) return null
            return runCatching {
                val root = JSONObject(raw.trim())
                if (!root.has("items")) return@runCatching null
                val arr = root.optJSONArray("items") ?: JSONArray()
                val parsedItems = buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        add(DashboardBannerItem.fromJson(obj, fallbackIndex = i))
                    }
                }
                DashboardBannerConfig(
                    items = parsedItems,
                    slideIntervalSeconds = clampIntervalSeconds(
                        root.optInt("slideIntervalSeconds", DEFAULT_INTERVAL_SECONDS)
                    ),
                    updatedAt = root.optLong("updatedAt", 0L)
                ).normalized()
            }.getOrNull()
        }
    }
}
