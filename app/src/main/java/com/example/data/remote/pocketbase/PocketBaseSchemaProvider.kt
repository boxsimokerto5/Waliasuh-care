package com.example.data.remote.pocketbase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PocketBaseSchemaSyncResult(
    val success: Boolean,
    val summary: String,
    val logs: List<String>
)

data class PbFieldDef(
    val name: String,
    val type: String, // "text", "number", "bool", "file"
    val fileMaxSize: Long = 10_485_760L,
    val fileMimeTypes: List<String> = listOf("image/jpeg", "image/png", "image/webp")
)

data class PocketBaseCollectionMeta(
    val collectionName: String,
    val description: String,
    val multiTenantField: String,
    val fields: List<PbFieldDef>,
    val indexes: List<String> = emptyList()
)

object PocketBaseSchemaProvider {
    const val SCHEMA_VERSION = "PocketBase v0.50.0 • 28 Koleksi Multi-Tenant + Foto Chat 2 Arah & Galeri R2 (2026.11)"

    val COLLECTIONS = listOf(
        PocketBaseCollectionMeta(
            collectionName = "child_students",
            description = "Master data anak asuh (Putra & Putri) per sekolah",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("name", "text"),
                PbFieldDef("nis", "text"),
                PbFieldDef("gender", "text"),
                PbFieldDef("age", "number"),
                PbFieldDef("dormitory", "text"),
                PbFieldDef("class_grade", "text"),
                PbFieldDef("allergies", "text"),
                PbFieldDef("blood_type", "text"),
                PbFieldDef("emergency_contact", "text"),
                PbFieldDef("special_notes", "text"),
                PbFieldDef("nik", "text"),
                PbFieldDef("no_kk", "text"),
                PbFieldDef("birth_place", "text"),
                PbFieldDef("birth_date", "text"),
                PbFieldDef("mother_name", "text"),
                PbFieldDef("address", "text"),
                PbFieldDef("district", "text"),
                PbFieldDef("room_number", "text"),
                PbFieldDef("jenjang", "text"),
                PbFieldDef("parent_phone", "text"),
                PbFieldDef("assigned_wali_username", "text"),
                PbFieldDef("assigned_wali_name", "text"),
                PbFieldDef("photo_url", "text"),
                PbFieldDef("photo_fallback_url", "text"),
                PbFieldDef(
                    name = "photo_file",
                    type = "file",
                    fileMaxSize = 10_485_760L,
                    fileMimeTypes = listOf("image/jpeg", "image/png", "image/webp")
                )
            ),
            indexes = listOf("CREATE INDEX idx_cs_school ON child_students (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "medical_records",
            description = "Rencana berobat, rujukan puskesmas, diagnosa, kontrol & rawat inap",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_dormitory", "text"),
                PbFieldDef("faskes", "text"),
                PbFieldDef("complaint", "text"),
                PbFieldDef("is_direct_treatment", "bool"),
                PbFieldDef("plan_date", "number"),
                PbFieldDef("departure_time", "text"),
                PbFieldDef("status", "text"),
                PbFieldDef("treatment_date", "number"),
                PbFieldDef("diagnosis", "text"),
                PbFieldDef("actions_taken", "text"),
                PbFieldDef("prescription", "text"),
                PbFieldDef("needs_control", "bool"),
                PbFieldDef("control_date", "number"),
                PbFieldDef("is_control_completed", "bool"),
                PbFieldDef("escort_name", "text"),
                PbFieldDef("notes", "text"),
                PbFieldDef("is_inpatient", "bool"),
                PbFieldDef("inpatient_room", "text"),
                PbFieldDef("discharge_date", "number"),
                PbFieldDef("created_by", "text"),
                PbFieldDef("edited_by", "text"),
                PbFieldDef("edited_at", "number"),
                PbFieldDef("created_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_mr_school ON medical_records (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "haid_records",
            description = "Pendataan siklus haid, mandi suci & pantauan istihadhah",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nik", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("start_date", "number"),
                PbFieldDef("end_date", "number"),
                PbFieldDef("status", "text"),
                PbFieldDef("symptoms", "text"),
                PbFieldDef("notes", "text"),
                PbFieldDef("wali_name", "text"),
                PbFieldDef("last_checked_in_at", "number"),
                PbFieldDef("last_check_in_by", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_hr_school ON haid_records (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "wali_accounts",
            description = "Akun login & profil resmi Wali Asuh / Guru Ngaji (termasuk foto & legalitas akun)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("name", "text"),
                PbFieldDef("username", "text"),
                PbFieldDef("password", "text"),
                PbFieldDef("dormitory", "text"),
                PbFieldDef("phone", "text"),
                PbFieldDef("role", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("photo_url", "text"),
                PbFieldDef("photo_fallback_url", "text"),
                PbFieldDef("title_position", "text"),
                PbFieldDef("employee_code", "text"),
                PbFieldDef("bio", "text"),
                PbFieldDef("profile_updated_at", "number"),
                PbFieldDef(
                    name = "photo_file",
                    type = "file",
                    fileMaxSize = 10_485_760L,
                    fileMimeTypes = listOf("image/jpeg", "image/png", "image/webp")
                )
            ),
            indexes = listOf("CREATE INDEX idx_wa_school ON wali_accounts (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "shift_messages",
            description = "Pesan operan jaga & checklist penyelesaian antar-shift",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("title", "text"),
                PbFieldDef("follow_up_date", "number"),
                PbFieldDef("shift", "text"),
                PbFieldDef("target_wali", "text"),
                PbFieldDef("message_content", "text"),
                PbFieldDef("priority", "text"),
                PbFieldDef("sender_name", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("is_completed", "bool"),
                PbFieldDef("completed_by", "text"),
                PbFieldDef("completed_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_sm_school ON shift_messages (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "school_registrations",
            description = "Pendaftaran multi-sekolah, kode tenant & status otorisasi",
            multiTenantField = "generated_school_code",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_name", "text"),
                PbFieldDef("address", "text"),
                PbFieldDef("city", "text"),
                PbFieldDef("estimated_wali_count", "number"),
                PbFieldDef("pic_name", "text"),
                PbFieldDef("pic_email", "text"),
                PbFieldDef("pic_phone", "text"),
                PbFieldDef("submission_date", "number"),
                PbFieldDef("status", "text"),
                PbFieldDef("rejection_reason", "text"),
                PbFieldDef("generated_school_code", "text"),
                PbFieldDef("generated_admin_username", "text"),
                PbFieldDef("generated_admin_password", "text"),
                PbFieldDef("approved_date", "number")
            ),
            indexes = listOf("CREATE INDEX idx_sr_code ON school_registrations (generated_school_code)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "fasting_sessions",
            description = "Sesi kegiatan puasa siswa (dibuat oleh Admin Sekolah)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("title", "text"),
                PbFieldDef("session_date", "number"),
                PbFieldDef("description", "text"),
                PbFieldDef("is_active", "bool"),
                PbFieldDef("created_by", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_fs_school ON fasting_sessions (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "fasting_records",
            description = "Pendataan peserta puasa, checklist sahur & berbuka online",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("session_id", "number"),
                PbFieldDef("session_title", "text"),
                PbFieldDef("session_date", "number"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nik", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("student_jenjang", "text"),
                PbFieldDef("is_sahur_checked", "bool"),
                PbFieldDef("sahur_checked_at", "number"),
                PbFieldDef("sahur_checked_by", "text"),
                PbFieldDef("is_buka_checked", "bool"),
                PbFieldDef("buka_checked_at", "number"),
                PbFieldDef("buka_checked_by", "text"),
                PbFieldDef("wali_name", "text"),
                PbFieldDef("notes", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_fr_school ON fasting_records (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "mengaji_records",
            description = "Riwayat mengaji berkelanjutan & kartu prestasi mengaji siswa",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nik", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("student_jenjang", "text"),
                PbFieldDef("session_date", "number"),
                PbFieldDef("book_type", "text"),
                PbFieldDef("jilid_or_juz", "text"),
                PbFieldDef("page_or_verse", "text"),
                PbFieldDef("start_page", "number"),
                PbFieldDef("end_page", "number"),
                PbFieldDef("fluency_status", "text"),
                PbFieldDef("notes", "text"),
                PbFieldDef("wali_name", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_mjr_school ON mengaji_records (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "school_profiles",
            description = "Profil sekolah, daftar faskes rujukan & kontak faskes",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("school_name", "text"),
                PbFieldDef("short_name", "text"),
                PbFieldDef("admin_name", "text"),
                PbFieldDef("address", "text"),
                PbFieldDef("city", "text"),
                PbFieldDef("primary_faskes_name", "text"),
                PbFieldDef("secondary_faskes_name", "text"),
                PbFieldDef("additional_faskes_json", "text"),
                PbFieldDef("faskes_contacts_json", "text"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_sp_school ON school_profiles (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "visit_sessions",
            description = "Sesi jadwal kunjungan orang tua per bulan, multi-tanggal & kecamatan (diatur Admin)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("title", "text"),
                PbFieldDef("month_year", "text"),
                PbFieldDef("session_dates_csv", "text"),
                PbFieldDef("selected_districts_csv", "text"),
                PbFieldDef("description", "text"),
                PbFieldDef("is_active", "bool"),
                PbFieldDef("created_by", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_vs_school ON visit_sessions (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "visit_records",
            description = "Daftar hadir & bukti check-in kunjungan orang tua (barcode scan atau manual)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("session_id", "number"),
                PbFieldDef("session_title", "text"),
                PbFieldDef("visit_date", "number"),
                PbFieldDef("checked_in_at", "number"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nik", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("student_district", "text"),
                PbFieldDef("visitor_name", "text"),
                PbFieldDef("visitor_relation", "text"),
                PbFieldDef("check_in_method", "text"),
                PbFieldDef("is_out_of_schedule_district", "bool"),
                PbFieldDef("wali_name", "text"),
                PbFieldDef("notes", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_vr_school ON visit_records (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "parent_access_links",
            description = "Kartu akses login orang tua & pilihan izin modul per siswa",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("parent_name", "text"),
                PbFieldDef("parent_phone", "text"),
                PbFieldDef("username", "text"),
                PbFieldDef("password", "text"),
                PbFieldDef("assigned_wali_username", "text"),
                PbFieldDef("assigned_wali_name", "text"),
                PbFieldDef("can_view_mengaji", "bool"),
                PbFieldDef("can_view_medical", "bool"),
                PbFieldDef("can_view_fasting", "bool"),
                PbFieldDef("can_view_haid", "bool"),
                PbFieldDef("can_view_profile", "bool"),
                PbFieldDef("can_view_tabungan", "bool"),
                PbFieldDef("can_view_sholat", "bool"),
                PbFieldDef("deletion_requested", "bool"),
                PbFieldDef("deletion_reason", "text"),
                PbFieldDef("deletion_requested_at", "number"),
                PbFieldDef("student_photo_url", "text"),
                PbFieldDef("student_photo_fallback_url", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_pal_school ON parent_access_links (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "parent_messages",
            description = "Pesan dua arah antara Wali Asuh pembina dan Orang Tua siswa",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("parent_name", "text"),
                PbFieldDef("wali_username", "text"),
                PbFieldDef("wali_name", "text"),
                PbFieldDef("sender_role", "text"),
                PbFieldDef("sender_name", "text"),
                PbFieldDef("message_content", "text"),
                PbFieldDef("is_read_by_wali", "bool"),
                PbFieldDef("is_read_by_parent", "bool"),
                PbFieldDef("photo_url", "text"),
                PbFieldDef("photo_fallback_url", "text"),
                PbFieldDef("pb_record_id", "text"),
                PbFieldDef("pb_collection_id", "text"),
                PbFieldDef("pb_filename", "text"),
                PbFieldDef(
                    name = "photo_file",
                    type = "file",
                    fileMaxSize = 10_485_760L,
                    fileMimeTypes = listOf("image/jpeg", "image/png", "image/webp")
                ),
                PbFieldDef("is_downloaded_by_recipient", "bool"),
                PbFieldDef("is_media_purged_from_server", "bool"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_pm_school ON parent_messages (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "parent_expectations",
            description = "Harapan karakter orang tua untuk dinasehatkan Wali Asuh kepada anak (ikhtiar bersama)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("parent_name", "text"),
                PbFieldDef("wali_username", "text"),
                PbFieldDef("wali_name", "text"),
                PbFieldDef("character_focus_csv", "text"),
                PbFieldDef("character_expectation", "text"),
                PbFieldDef("advice_focus_notes", "text"),
                PbFieldDef("parent_prayer_message", "text"),
                PbFieldDef("is_read_by_wali", "bool"),
                PbFieldDef("wali_follow_up_note", "text"),
                PbFieldDef("wali_responded_at", "number"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_pe_school ON parent_expectations (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "dashboard_banners",
            description = "Banner slider global dashboard (Cloudflare R2 file & CDN URL + target fitur/eksternal)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("banner_id", "text"),
                PbFieldDef("title", "text"),
                PbFieldDef("subtitle", "text"),
                PbFieldDef("badge_text", "text"),
                PbFieldDef(
                    name = "image",
                    type = "file",
                    fileMaxSize = 20_971_520L,
                    fileMimeTypes = listOf(
                        "image/jpeg",
                        "image/png",
                        "image/webp",
                        "image/gif",
                        "video/mp4",
                        "video/webm"
                    )
                ),
                PbFieldDef("cdn_image_url", "text"),
                PbFieldDef("action_type", "text"),
                PbFieldDef("action_target", "text"),
                PbFieldDef("sort_order", "number"),
                PbFieldDef("is_enabled", "bool"),
                PbFieldDef("is_default_faskes", "bool"),
                PbFieldDef("slide_interval_seconds", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_db_school ON dashboard_banners (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "deletion_logs",
            description = "Log audit & notifikasi penghapusan riwayat mengaji, puasa, dan berobat per sekolah",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("category", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("record_id", "number"),
                PbFieldDef("deleted_summary", "text"),
                PbFieldDef("deleted_by", "text"),
                PbFieldDef("deleted_by_username", "text"),
                PbFieldDef("created_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_dl_school ON deletion_logs (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "tabungan_transactions",
            description = "Transaksi penitipan uang pribadi siswa kepada Wali Asuh (isolasi pribadi per owner_wali_username)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("owner_wali_username", "text"),
                PbFieldDef("owner_wali_name", "text"),
                PbFieldDef("is_private", "bool"),
                PbFieldDef("transaction_type", "text"),
                PbFieldDef("amount", "number"),
                PbFieldDef("title", "text"),
                PbFieldDef("notes", "text"),
                PbFieldDef("transaction_date", "number"),
                PbFieldDef("status", "text"),
                PbFieldDef("cancelled_by", "text"),
                PbFieldDef("cancelled_reason", "text"),
                PbFieldDef("cancelled_at", "number"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_tt_school ON tabungan_transactions (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "tabungan_logs",
            description = "Log audit & riwayat perubahan tabungan pribadi (input, batalkan, hapus) per Wali Asuh",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("transaction_id", "number"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("owner_wali_username", "text"),
                PbFieldDef("owner_wali_name", "text"),
                PbFieldDef("is_private", "bool"),
                PbFieldDef("action_type", "text"),
                PbFieldDef("amount", "number"),
                PbFieldDef("balance_before", "number"),
                PbFieldDef("balance_after", "number"),
                PbFieldDef("summary", "text"),
                PbFieldDef("actor_name", "text"),
                PbFieldDef("actor_username", "text"),
                PbFieldDef("created_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_tl_school ON tabungan_logs (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "konseling_records",
            description = "Catatan konseling & perkembangan anak binaan rahasia (hanya Wali Asuh pemilik)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("owner_wali_username", "text"),
                PbFieldDef("owner_wali_name", "text"),
                PbFieldDef("is_confidential", "bool"),
                PbFieldDef("entry_type", "text"),
                PbFieldDef("topic", "text"),
                PbFieldDef("urgency_level", "text"),
                PbFieldDef("mood_level", "text"),
                PbFieldDef("title", "text"),
                PbFieldDef("summary", "text"),
                PbFieldDef("intervention", "text"),
                PbFieldDef("follow_up_plan", "text"),
                PbFieldDef("follow_up_date", "number"),
                PbFieldDef("status", "text"),
                PbFieldDef("session_date", "number"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_kr_school ON konseling_records (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "konseling_logs",
            description = "Log audit internal catatan konseling rahasia (hanya Wali Asuh bersangkutan)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("record_id", "number"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("owner_wali_username", "text"),
                PbFieldDef("owner_wali_name", "text"),
                PbFieldDef("is_confidential", "bool"),
                PbFieldDef("action_type", "text"),
                PbFieldDef("summary", "text"),
                PbFieldDef("actor_name", "text"),
                PbFieldDef("actor_username", "text"),
                PbFieldDef("created_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_kl_school ON konseling_logs (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "wali_shift_codes",
            description = "Master kode shift jadwal Wali Asuh yang dapat diatur per sekolah",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("code", "text"),
                PbFieldDef("name", "text"),
                PbFieldDef("shift_group", "text"),
                PbFieldDef("zone_label", "text"),
                PbFieldDef("time_range", "text"),
                PbFieldDef("start_hour", "number"),
                PbFieldDef("end_hour", "number"),
                PbFieldDef("work_hours", "number"),
                PbFieldDef("color_hex", "text"),
                PbFieldDef("sop_notes", "text"),
                PbFieldDef("sort_order", "number"),
                PbFieldDef("is_active", "bool"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_wsc_school ON wali_shift_codes (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "wali_schedule_entries",
            description = "Matriks jadwal kerja bulanan, penyematan tugas & izin Wali Asuh per sekolah",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("month_key", "text"),
                PbFieldDef("wali_account_id", "number"),
                PbFieldDef("wali_username", "text"),
                PbFieldDef("wali_name", "text"),
                PbFieldDef("wali_code", "text"),
                PbFieldDef("sort_order", "number"),
                PbFieldDef("day_codes_json", "text"),
                PbFieldDef("day_pins_json", "text"),
                PbFieldDef("day_leave_json", "text"),
                PbFieldDef("updated_by", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_wse_school ON wali_schedule_entries (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "important_info_posts",
            description = "Channel Info-Info Penting Global (teks, foto/GIF, video, dokumen, link) oleh anaksuperadmin tampil sebagai Superadmin",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("title", "text"),
                PbFieldDef("content", "text"),
                PbFieldDef("category", "text"),
                PbFieldDef("media_type", "text"),
                PbFieldDef("media_url", "text"),
                PbFieldDef("fallback_media_url", "text"),
                PbFieldDef("pb_record_id", "text"),
                PbFieldDef("pb_collection_id", "text"),
                PbFieldDef("pb_filename", "text"),
                PbFieldDef("media_name", "text"),
                PbFieldDef("media_size_bytes", "number"),
                PbFieldDef("external_link", "text"),
                PbFieldDef("author_display_name", "text"),
                PbFieldDef("author_school_name", "text"),
                PbFieldDef("is_pinned", "bool"),
                PbFieldDef("is_deleted", "bool"),
                PbFieldDef(
                    name = "media_file",
                    type = "file",
                    fileMaxSize = 52_428_800L,
                    fileMimeTypes = listOf(
                        "image/jpeg",
                        "image/png",
                        "image/webp",
                        "image/gif",
                        "video/mp4",
                        "video/webm",
                        "video/quicktime",
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "text/plain",
                        "text/csv",
                        "application/zip"
                    )
                ),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_iip_school ON important_info_posts (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "important_info_reactions",
            description = "Reaksi Suka / Tidak Suka seluruh Wali Asuh lintas sekolah pada Channel Info-Info Penting",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("post_id", "number"),
                PbFieldDef("user_key", "text"),
                PbFieldDef("user_name", "text"),
                PbFieldDef("user_school_id", "text"),
                PbFieldDef("reaction_type", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_iir_post ON important_info_reactions (post_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "important_info_comments",
            description = "Komentar dan balasan komentar berjenjang seluruh Wali Asuh lintas sekolah pada Channel Info-Info Penting",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("post_id", "number"),
                PbFieldDef("parent_comment_id", "number"),
                PbFieldDef("reply_to_author_name", "text"),
                PbFieldDef("author_key", "text"),
                PbFieldDef("author_name", "text"),
                PbFieldDef("author_school_id", "text"),
                PbFieldDef("author_school_name", "text"),
                PbFieldDef("content", "text"),
                PbFieldDef("is_deleted", "bool"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_iic_post ON important_info_comments (post_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "sholat_records",
            description = "Absensi sholat wajib 5 waktu (Subuh, Dzuhur, Ashar, Maghrib, Isya) per siswa & tanggal",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("date_key", "text"),
                PbFieldDef("date_millis", "number"),
                PbFieldDef("waktu", "text"),
                PbFieldDef("status", "text"),
                PbFieldDef("discipline_tag", "text"),
                PbFieldDef("student_id", "number"),
                PbFieldDef("student_name", "text"),
                PbFieldDef("student_nik", "text"),
                PbFieldDef("student_nis", "text"),
                PbFieldDef("student_class", "text"),
                PbFieldDef("student_dorm", "text"),
                PbFieldDef("student_jenjang", "text"),
                PbFieldDef("notes", "text"),
                PbFieldDef("input_method", "text"),
                PbFieldDef("recorded_by", "text"),
                PbFieldDef("recorded_by_username", "text"),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_sholat_school_date ON sholat_records (school_id, date_key)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "wali_gallery_posts",
            description = "Galeri Wali Asuh untuk Portal Orang Tua (Relay sementara PocketBase + Cloudflare R2 sebelum tersimpan di galeri HP Ortu)",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("wali_username", "text"),
                PbFieldDef("wali_name", "text"),
                PbFieldDef("caption", "text"),
                PbFieldDef("activity_tag", "text"),
                PbFieldDef("target_scope", "text"),
                PbFieldDef("target_student_ids_csv", "text"),
                PbFieldDef("photo_url", "text"),
                PbFieldDef("photo_fallback_url", "text"),
                PbFieldDef("pb_record_id", "text"),
                PbFieldDef("pb_collection_id", "text"),
                PbFieldDef("pb_filename", "text"),
                PbFieldDef("retention_days", "number"),
                PbFieldDef("expires_at", "number"),
                PbFieldDef("expected_recipient_count", "number"),
                PbFieldDef("downloaded_by_csv", "text"),
                PbFieldDef("is_media_purged_from_server", "bool"),
                PbFieldDef("is_deleted", "bool"),
                PbFieldDef(
                    name = "photo_file",
                    type = "file",
                    fileMaxSize = 10_485_760L,
                    fileMimeTypes = listOf("image/jpeg", "image/png", "image/webp")
                ),
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_wgp_school ON wali_gallery_posts (school_id, wali_username)")
        )
    )

    private fun buildIdFieldJson(): JSONObject {
        return JSONObject()
            .put("autogeneratePattern", "[a-z0-9]{15}")
            .put("hidden", false)
            .put("id", "text3208210256")
            .put("max", 15)
            .put("min", 15)
            .put("name", "id")
            .put("pattern", "^[a-z0-9]+$")
            .put("presentable", false)
            .put("primaryKey", true)
            .put("required", true)
            .put("system", true)
            .put("type", "text")
    }

    /**
     * Menghasilkan array JSON skema koleksi PocketBase terbaru secara otomatis dari daftar COLLECTIONS.
     * Siap ditempel langsung ke menu Settings -> Import collections pada dashboard PocketBase.
     */
    val POCKETBASE_COLLECTIONS_JSON: String by lazy {
        val root = JSONArray()
        for (col in COLLECTIONS) {
            val fieldsArr = JSONArray().apply {
                put(buildIdFieldJson())
                for (field in col.fields) {
                    val obj = JSONObject()
                        .put("name", field.name)
                        .put("type", field.type)
                    if (field.type == "file") {
                        obj.put("maxSelect", 1)
                        obj.put("maxSize", field.fileMaxSize)
                        obj.put(
                            "mimeTypes",
                            JSONArray().apply {
                                field.fileMimeTypes.forEach { put(it) }
                            }
                        )
                    }
                    put(obj)
                }
            }
            val indexesArr = JSONArray().apply {
                col.indexes.forEach { put(it) }
            }
            val colObj = JSONObject()
                .put("name", col.collectionName)
                .put("type", "base")
                .put("listRule", "")
                .put("viewRule", "")
                .put("createRule", "")
                .put("updateRule", "")
                .put("deleteRule", "")
                .put("fields", fieldsArr)
                .put("indexes", indexesArr)
            root.put(colObj)
        }
        root.toString(2)
    }

    /**
     * Skrip Python idempoten untuk Termius/SSH VPS agar bisa dijalankan ulang kapan saja saat ada
     * pembaruan aplikasi (otomatis membuat koleksi baru & menambah field/index yang belum ada tanpa menghapus data).
     */
    val POCKETBASE_SSH_SYNC_SCRIPT: String by lazy {
        val compactJson = JSONArray(POCKETBASE_COLLECTIONS_JSON).toString()
        """
cat << 'EOF' > /tmp/sync_pb_waliasuh.py
import getpass, json, re, urllib.request, urllib.error

BASE = "http://127.0.0.1:8092"
SCHEMA = json.loads(r'''$compactJson''')

email = input("Email Superuser PocketBase: ").strip()
pwd = getpass.getpass("Password Superuser PocketBase: ")

def req(path, body=None, token=None, method="GET"):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = token
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(r, timeout=20) as resp:
            return resp.status, json.loads(resp.read().decode() or "{}")
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read().decode() or "{}")

def idx_name(sql):
    m = re.search(r'CREATE\s+(?:UNIQUE\s+)?INDEX\s+(?:IF\s+NOT\s+EXISTS\s+)?[`"\']?([A-Za-z0-9_]+)[`"\']?', sql, re.I)
    return m.group(1).lower() if m else sql.strip().lower()

st, auth = req("/api/collections/_superusers/auth-with-password", {"identity": email, "password": pwd}, method="POST")
if st != 200 or not auth.get("token"):
    raise SystemExit(f"Login gagal ({st}): {auth}")
tok = auth["token"]

st, existing_resp = req("/api/collections?perPage=500", token=tok)
existing_map = {c["name"]: c for c in existing_resp.get("items", [])}

for col in SCHEMA:
    name = col["name"]
    if name not in existing_map:
        c_st, c_res = req("/api/collections", col, token=tok, method="POST")
        print(f"[BARU] {name} -> {'OK' if c_st in (200, 201) else c_res}")
    else:
        cur = existing_map[name]
        cur_fields = list(cur.get("fields", []))
        target_by_name = {f["name"]: f for f in col["fields"]}
        fields_changed = False
        for cf in cur_fields:
            tf = target_by_name.get(cf.get("name"))
            if tf and cf.get("type") == "file" and tf.get("type") == "file":
                if int(cf.get("maxSize") or 0) < int(tf.get("maxSize") or 0):
                    cf["maxSize"] = tf["maxSize"]
                    fields_changed = True
                cur_mimes = list(cf.get("mimeTypes") or [])
                for m in (tf.get("mimeTypes") or []):
                    if m not in cur_mimes:
                        cur_mimes.append(m)
                        fields_changed = True
                cf["mimeTypes"] = cur_mimes
        cur_names = {f["name"] for f in cur_fields}
        added_fields = [f for f in col["fields"] if f["name"] not in cur_names]
        cur_indexes = list(cur.get("indexes", []))
        cur_idx_names = {idx_name(ix) for ix in cur_indexes}
        added_indexes = [ix for ix in col.get("indexes", []) if idx_name(ix) not in cur_idx_names]
        rules_changed = any(cur.get(k) != "" for k in ("listRule", "viewRule", "createRule", "updateRule", "deleteRule"))
        if not added_fields and not fields_changed and not added_indexes and not rules_changed:
            print(f"[UP-TO-DATE] {name}")
            continue
        payload = {
            "fields": cur_fields + added_fields,
            "indexes": cur_indexes + added_indexes,
            "listRule": "",
            "viewRule": "",
            "createRule": "",
            "updateRule": "",
            "deleteRule": "",
        }
        u_st, u_res = req(f"/api/collections/{cur['id']}", payload, token=tok, method="PATCH")
        print(f"[UPDATE +{len(added_fields)} field] {name} -> {'OK' if u_st == 200 else u_res}")
EOF
python3 /tmp/sync_pb_waliasuh.py && rm -f /tmp/sync_pb_waliasuh.py
        """.trimIndent()
    }

    private fun extractIndexName(sql: String): String {
        val regex = Regex("""CREATE\s+(?:UNIQUE\s+)?INDEX\s+(?:IF\s+NOT\s+EXISTS\s+)?[`"']?([A-Za-z0-9_]+)[`"']?""", RegexOption.IGNORE_CASE)
        return regex.find(sql)?.groupValues?.getOrNull(1)?.lowercase() ?: sql.trim().lowercase()
    }

    /**
     * Sinkronisasi skema 10 koleksi langsung ke server PocketBase via HTTPS API menggunakan akun
     * Superuser PocketBase — aman bagi database yang sudah berisi data dan koleksi lama (tanpa
     * kendala merge ID pada menu Import Collections di browser).
     */
    suspend fun syncSchemaWithSuperuser(
        baseUrl: String,
        superuserEmail: String,
        superuserPassword: String
    ): PocketBaseSchemaSyncResult = withContext(Dispatchers.IO) {
        val cleanBase = baseUrl.trim().trimEnd('/')
        if (!cleanBase.startsWith("http")) {
            return@withContext PocketBaseSchemaSyncResult(
                success = false,
                summary = "URL PocketBase tidak valid: $baseUrl",
                logs = emptyList()
            )
        }
        val cleanEmail = superuserEmail.trim()
        if (cleanEmail.isBlank() || superuserPassword.isBlank()) {
            return@withContext PocketBaseSchemaSyncResult(
                success = false,
                summary = "Email dan password Superuser PocketBase wajib diisi.",
                logs = emptyList()
            )
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
        val jsonMedia = "application/json; charset=utf-8".toMediaType()

        fun executeRequest(path: String, method: String, bodyObj: JSONObject? = null, token: String? = null): Pair<Int, JSONObject> {
            val reqBody = bodyObj?.toString()?.toRequestBody(jsonMedia)
            val builder = Request.Builder().url("$cleanBase$path")
            if (token != null) builder.header("Authorization", token)
            when (method) {
                "GET" -> builder.get()
                "POST" -> builder.post(reqBody ?: "{}".toRequestBody(jsonMedia))
                "PATCH" -> builder.patch(reqBody ?: "{}".toRequestBody(jsonMedia))
                else -> builder.method(method, reqBody)
            }
            client.newCall(builder.build()).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                val parsed = runCatching { JSONObject(raw.ifBlank { "{}" }) }.getOrDefault(JSONObject().put("raw", raw))
                return resp.code to parsed
            }
        }

        return@withContext try {
            val authBody = JSONObject()
                .put("identity", cleanEmail)
                .put("password", superuserPassword)
            val (authCode, authResp) = executeRequest(
                path = "/api/collections/_superusers/auth-with-password",
                method = "POST",
                bodyObj = authBody
            )
            val token = authResp.optString("token", "")
            if (authCode !in 200..299 || token.isBlank()) {
                val errMsg = authResp.optString("message").ifBlank { "HTTP $authCode" }
                return@withContext PocketBaseSchemaSyncResult(
                    success = false,
                    summary = "Login Superuser gagal: $errMsg",
                    logs = listOf("[AUTH ERROR] $authResp")
                )
            }

            val (listCode, listResp) = executeRequest(
                path = "/api/collections?perPage=500",
                method = "GET",
                token = token
            )
            if (listCode !in 200..299) {
                return@withContext PocketBaseSchemaSyncResult(
                    success = false,
                    summary = "Gagal membaca daftar koleksi server (HTTP $listCode).",
                    logs = listOf("[LIST ERROR] $listResp")
                )
            }

            val existingByName = mutableMapOf<String, JSONObject>()
            val existingItems = listResp.optJSONArray("items") ?: JSONArray()
            for (i in 0 until existingItems.length()) {
                val item = existingItems.getJSONObject(i)
                existingByName[item.optString("name")] = item
            }

            val schemaArr = JSONArray(POCKETBASE_COLLECTIONS_JSON)
            val logs = mutableListOf<String>()
            var createdCount = 0
            var updatedCount = 0
            var errorCount = 0

            for (i in 0 until schemaArr.length()) {
                val colObj = schemaArr.getJSONObject(i)
                val name = colObj.optString("name")
                val existing = existingByName[name]
                if (existing == null) {
                    val (cCode, cResp) = executeRequest(
                        path = "/api/collections",
                        method = "POST",
                        bodyObj = colObj,
                        token = token
                    )
                    if (cCode in 200..299) {
                        createdCount++
                        logs.add("[BARU] $name -> OK (${colObj.optJSONArray("fields")?.length() ?: 0} field)")
                    } else {
                        errorCount++
                        logs.add("[GAGAL BARU] $name ($cCode): ${cResp.optString("message").ifBlank { cResp.toString() }}")
                    }
                } else {
                    val targetFieldsArr = colObj.optJSONArray("fields") ?: JSONArray()
                    val targetByName = mutableMapOf<String, JSONObject>()
                    for (j in 0 until targetFieldsArr.length()) {
                        val tf = targetFieldsArr.getJSONObject(j)
                        targetByName[tf.optString("name")] = tf
                    }

                    val curFieldsArr = existing.optJSONArray("fields") ?: JSONArray()
                    val existingFieldNames = mutableSetOf<String>()
                    val mergedFields = JSONArray()
                    var fileFieldUpdated = false
                    for (j in 0 until curFieldsArr.length()) {
                        val f = curFieldsArr.getJSONObject(j)
                        val fName = f.optString("name")
                        existingFieldNames.add(fName)
                        val tf = targetByName[fName]
                        if (tf != null && f.optString("type") == "file" && tf.optString("type") == "file") {
                            val targetMax = tf.optLong("maxSize", 0L)
                            if (f.optLong("maxSize", 0L) < targetMax) {
                                f.put("maxSize", targetMax)
                                fileFieldUpdated = true
                            }
                            val curMimes = f.optJSONArray("mimeTypes") ?: JSONArray()
                            val curMimeSet = mutableSetOf<String>()
                            for (mIdx in 0 until curMimes.length()) {
                                curMimeSet.add(curMimes.optString(mIdx))
                            }
                            val targetMimes = tf.optJSONArray("mimeTypes") ?: JSONArray()
                            for (mIdx in 0 until targetMimes.length()) {
                                val tm = targetMimes.optString(mIdx)
                                if (tm.isNotBlank() && tm !in curMimeSet) {
                                    curMimes.put(tm)
                                    curMimeSet.add(tm)
                                    fileFieldUpdated = true
                                }
                            }
                            f.put("mimeTypes", curMimes)
                        }
                        mergedFields.put(f)
                    }

                    val addedFieldNames = mutableListOf<String>()
                    for (j in 0 until targetFieldsArr.length()) {
                        val tf = targetFieldsArr.getJSONObject(j)
                        val fName = tf.optString("name")
                        if (fName !in existingFieldNames) {
                            mergedFields.put(tf)
                            addedFieldNames.add(fName)
                        }
                    }

                    val curIndexesArr = existing.optJSONArray("indexes") ?: JSONArray()
                    val existingIdxNames = mutableSetOf<String>()
                    val mergedIndexes = JSONArray()
                    for (j in 0 until curIndexesArr.length()) {
                        val ix = curIndexesArr.optString(j)
                        if (ix.isNotBlank()) {
                            existingIdxNames.add(extractIndexName(ix))
                            mergedIndexes.put(ix)
                        }
                    }

                    val targetIndexesArr = colObj.optJSONArray("indexes") ?: JSONArray()
                    var addedIdxCount = 0
                    for (j in 0 until targetIndexesArr.length()) {
                        val tix = targetIndexesArr.optString(j)
                        if (tix.isNotBlank() && extractIndexName(tix) !in existingIdxNames) {
                            mergedIndexes.put(tix)
                            addedIdxCount++
                        }
                    }

                    val ruleKeys = listOf("listRule", "viewRule", "createRule", "updateRule", "deleteRule")
                    val rulesNeedUpdate = ruleKeys.any { key ->
                        existing.isNull(key) || existing.optString(key, "null") != ""
                    }

                    if (addedFieldNames.isEmpty() && !fileFieldUpdated && addedIdxCount == 0 && !rulesNeedUpdate) {
                        logs.add("[UP-TO-DATE] $name")
                        continue
                    }

                    val patchPayload = JSONObject()
                        .put("fields", mergedFields)
                        .put("indexes", mergedIndexes)
                        .put("listRule", "")
                        .put("viewRule", "")
                        .put("createRule", "")
                        .put("updateRule", "")
                        .put("deleteRule", "")

                    val colId = existing.optString("id")
                    val (uCode, uResp) = executeRequest(
                        path = "/api/collections/$colId",
                        method = "PATCH",
                        bodyObj = patchPayload,
                        token = token
                    )
                    if (uCode in 200..299) {
                        updatedCount++
                        val addedDesc = when {
                            addedFieldNames.isNotEmpty() -> "+${addedFieldNames.size} field (${addedFieldNames.joinToString(", ")})"
                            fileFieldUpdated -> "mimeTypes GIF/Video +20MB"
                            else -> "index/rules"
                        }
                        logs.add("[UPDATE $addedDesc] $name -> OK")
                    } else {
                        errorCount++
                        logs.add("[GAGAL UPDATE] $name ($uCode): ${uResp.optString("message").ifBlank { uResp.toString() }}")
                    }
                }
            }

            val allOk = errorCount == 0
            val summaryText = if (allOk) {
                "Sinkronisasi selesai: $createdCount tabel baru, $updatedCount tabel diperbarui, ${schemaArr.length() - createdCount - updatedCount} sudah mutakhir."
            } else {
                "Selesai dengan $errorCount kendala ($createdCount baru, $updatedCount diperbarui)."
            }
            PocketBaseSchemaSyncResult(success = allOk, summary = summaryText, logs = logs)
        } catch (e: Exception) {
            PocketBaseSchemaSyncResult(
                success = false,
                summary = "Koneksi gagal: ${e.message ?: e.javaClass.simpleName}",
                logs = listOf("[EXCEPTION] ${e.message}")
            )
        }
    }

    val POCKETBASE_SCHEMA_SUMMARY: String by lazy {
        buildString {
            appendLine("Endpoint Server: ${PocketBaseConfig.EMBEDDED_URL}")
            appendLine("Dashboard Admin: ${PocketBaseConfig.EMBEDDED_URL}/_/")
            appendLine("Versi Skema    : $SCHEMA_VERSION")
            appendLine()
            appendLine("Daftar ${COLLECTIONS.size} Koleksi Aktif:")
            COLLECTIONS.forEachIndexed { idx, col ->
                appendLine("${idx + 1}. ${col.collectionName} (${col.fields.size} field • tenant: ${col.multiTenantField})")
            }
            appendLine()
            appendLine("Catatan Sinkronisasi Skema:")
            appendLine("• Gunakan 'Sync Skema ke Server Sekarang' (Superuser) atau 'Skrip SSH VPS' untuk update aman tanpa kendala ID.")
            appendLine("• Atau gunakan 'JSON Import' melalui Dashboard PocketBase -> Settings -> Import collections (Merge ON).")
        }.trimEnd()
    }
}
