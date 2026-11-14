package com.example.data.remote.pocketbase

data class PocketBaseCollectionMeta(
    val collectionName: String,
    val description: String,
    val multiTenantField: String
)

object PocketBaseSchemaProvider {
    const val SCHEMA_VERSION = "PocketBase v0.40.6 (9 Koleksi Multi-Tenant + Checklist Online)"

    val COLLECTIONS = listOf(
        PocketBaseCollectionMeta(
            collectionName = "child_students",
            description = "Master data anak asuh (Putra & Putri) per sekolah",
            multiTenantField = "school_id"
        ),
        PocketBaseCollectionMeta(
            collectionName = "medical_records",
            description = "Rencana berobat, rujukan puskesmas, diagnosa, kontrol & rawat inap",
            multiTenantField = "school_id"
        ),
        PocketBaseCollectionMeta(
            collectionName = "haid_records",
            description = "Pendataan siklus haid, mandi suci & pantauan istihadhah",
            multiTenantField = "school_id"
        ),
        PocketBaseCollectionMeta(
            collectionName = "wali_accounts",
            description = "Akun login Wali Asuh & sinkronisasi kata sandi",
            multiTenantField = "school_id"
        ),
        PocketBaseCollectionMeta(
            collectionName = "shift_messages",
            description = "Pesan operan jaga & checklist penyelesaian antar-shift",
            multiTenantField = "school_id"
        ),
        PocketBaseCollectionMeta(
            collectionName = "school_registrations",
            description = "Pendaftaran multi-sekolah, kode tenant & status otorisasi",
            multiTenantField = "generated_school_code"
        ),
        PocketBaseCollectionMeta(
            collectionName = "fasting_sessions",
            description = "Sesi kegiatan puasa siswa (dibuat oleh Admin Sekolah)",
            multiTenantField = "school_id"
        ),
        PocketBaseCollectionMeta(
            collectionName = "fasting_records",
            description = "Pendataan peserta puasa, checklist sahur & berbuka online",
            multiTenantField = "school_id"
        ),
        PocketBaseCollectionMeta(
            collectionName = "school_profiles",
            description = "Profil sekolah, daftar faskes rujukan & kontak faskes",
            multiTenantField = "school_id"
        )
    )

    val POCKETBASE_SCHEMA_SUMMARY: String = """
        Endpoint Server: ${PocketBaseConfig.EMBEDDED_URL}
        Dashboard Admin: ${PocketBaseConfig.EMBEDDED_URL}/_/
        Versi Skema    : $SCHEMA_VERSION

        Daftar 9 Koleksi Aktif:
        1. child_students       (index: school_id)
        2. medical_records      (index: school_id)
        3. haid_records         (index: school_id)
        4. wali_accounts        (index: school_id)
        5. shift_messages       (index: school_id)
        6. school_registrations (index: generated_school_code)
        7. fasting_sessions     (index: school_id)
        8. fasting_records      (index: school_id, session_id)
        9. school_profiles      (index: school_id)

        Catatan Performa:
        • Semua koleksi mendukung paginasi & filter multi-tenant (school_id).
        • Opsional: Aktifkan "Batch API" di Settings -> Application pada dashboard PocketBase agar unggah ratusan data siswa CSV berlangsung dalam < 1 detik.
    """.trimIndent()
}
