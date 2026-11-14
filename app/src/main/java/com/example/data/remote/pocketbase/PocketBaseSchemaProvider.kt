package com.example.data.remote.pocketbase

import org.json.JSONArray
import org.json.JSONObject

data class PbFieldDef(
    val name: String,
    val type: String // "text", "number", "bool"
)

data class PocketBaseCollectionMeta(
    val collectionName: String,
    val description: String,
    val multiTenantField: String,
    val fields: List<PbFieldDef>,
    val indexes: List<String> = emptyList()
)

object PocketBaseSchemaProvider {
    const val SCHEMA_VERSION = "PocketBase v0.40.6 • 9 Koleksi Multi-Tenant + Checklist Online (2026.11)"

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
                PbFieldDef("room_number", "text"),
                PbFieldDef("jenjang", "text")
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
                PbFieldDef("created_at", "number"),
                PbFieldDef("updated_at", "number")
            ),
            indexes = listOf("CREATE INDEX idx_hr_school ON haid_records (school_id)")
        ),
        PocketBaseCollectionMeta(
            collectionName = "wali_accounts",
            description = "Akun login Wali Asuh & sinkronisasi kata sandi",
            multiTenantField = "school_id",
            fields = listOf(
                PbFieldDef("local_id", "number"),
                PbFieldDef("school_id", "text"),
                PbFieldDef("name", "text"),
                PbFieldDef("username", "text"),
                PbFieldDef("password", "text"),
                PbFieldDef("dormitory", "text"),
                PbFieldDef("phone", "text"),
                PbFieldDef("created_at", "number")
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
            indexes = listOf("CREATE INDEX idx_mr_school ON mengaji_records (school_id)")
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
        )
    )

    private fun buildIdFieldJson(): JSONObject {
        return JSONObject()
            .put("name", "id")
            .put("type", "text")
            .put("primaryKey", true)
            .put("required", true)
            .put("system", true)
            .put("min", 15)
            .put("max", 15)
            .put("pattern", "^[a-z0-9]+$")
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
                    put(
                        JSONObject()
                            .put("name", field.name)
                            .put("type", field.type)
                    )
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
import getpass, json, urllib.request, urllib.error

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

st, auth = req("/api/collections/_superusers/auth-with-password", {"identity": email, "password": pwd}, method="POST")
if st != 200 or not auth.get("token"):
    raise SystemExit(f"Login gagal ({st}): {auth}")
tok = auth["token"]

st, existing_resp = req("/api/collections?perPage=200", token=tok)
existing_map = {c["name"]: c for c in existing_resp.get("items", [])}

for col in SCHEMA:
    name = col["name"]
    if name not in existing_map:
        c_st, c_res = req("/api/collections", col, token=tok, method="POST")
        print(f"[BARU] {name} -> {'OK' if c_st in (200, 201) else c_res}")
    else:
        cur = existing_map[name]
        cur_fields = list(cur.get("fields", []))
        cur_names = {f["name"] for f in cur_fields}
        added_fields = [f for f in col["fields"] if f["name"] not in cur_names]
        cur_indexes = list(cur.get("indexes", []))
        added_indexes = [ix for ix in col.get("indexes", []) if ix not in cur_indexes]
        rules_changed = any(cur.get(k) != "" for k in ("listRule", "viewRule", "createRule", "updateRule", "deleteRule"))
        if not added_fields and not added_indexes and not rules_changed:
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
            appendLine("• Gunakan tab 'Skrip SSH VPS' untuk buat/tambah kolom baru tanpa menghapus data lama.")
            appendLine("• Atau gunakan 'JSON Import' melalui Dashboard PocketBase -> Settings -> Import collections.")
        }.trimEnd()
    }
}
