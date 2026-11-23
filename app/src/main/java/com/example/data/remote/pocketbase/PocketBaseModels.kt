package com.example.data.remote.pocketbase

import com.example.data.model.BibleBookmark
import com.example.data.model.BibleNote
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.DashboardBannerConfig
import com.example.data.model.DashboardBannerItem
import com.example.data.model.DashboardIconKey
import com.example.data.model.DashboardIconLayoutConfig
import com.example.data.model.DeletionLog
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.InfoPentingComment
import com.example.data.model.InfoPentingHelper
import com.example.data.model.InfoPentingPost
import com.example.data.model.InfoPentingReaction
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.SharedJournalHelper
import com.example.data.model.SharedJournalNote
import com.example.data.model.ShiftMessage
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.VisitHelper
import com.example.data.model.VisitConfirmation
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.data.model.WaliAccount
import com.example.data.model.WaliGalleryHelper
import com.example.data.model.WaliGalleryPost
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.model.WaliShiftCode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray

object PocketBaseIdCodec {
    private val validPbIdRegex = Regex("^[a-z0-9]{15}$")

    /**
     * Memetakan Long ID lokal Room menjadi ID PocketBase 15 karakter [a-z0-9] secara deterministik & tanpa tabrakan.
     * Karena Long.MAX_VALUE dalam base-36 panjangnya 13 karakter, awalan 'w' + 14 karakter selalu tepat 15 karakter.
     */
    fun toPbId(localId: Long): String {
        val safeId = if (localId > 0L) localId else stablePositiveHash(localId.toString())
        val base36 = safeId.toString(36).lowercase()
        return if (base36.length <= 14) {
            "w" + base36.padStart(14, '0')
        } else {
            "w" + base36.takeLast(14)
        }
    }

    fun toPbSchoolProfileId(schoolId: String): String {
        val clean = schoolId.trim().uppercase().ifBlank { "SRT1-KDR" }
        return toPbId(stablePositiveHash("school_profile:$clean"))
    }

    fun resolveLocalId(localId: Long?, pbId: String?): Long {
        if (localId != null && localId > 0L) return localId
        if (!pbId.isNullOrBlank()) {
            if (pbId.length == 15 && pbId.startsWith("w")) {
                val parsed = pbId.drop(1).toLongOrNull(36)
                if (parsed != null && parsed > 0L) return parsed
            }
            return stablePositiveHash(pbId)
        }
        return 0L
    }

    fun isValidPbId(id: String?): Boolean {
        return !id.isNullOrBlank() && validPbIdRegex.matches(id)
    }

    fun stablePositiveHash(input: String): Long {
        var hash = -3750763034362895579L // FNV-1a 64-bit offset
        val prime = 1099511628211L
        for (ch in input) {
            hash = hash xor ch.code.toLong()
            hash *= prime
        }
        val positive56 = hash and 0x00FFFFFFFFFFFFFFL
        return if (positive56 > 0L) positive56 else 1L
    }
}

@JsonClass(generateAdapter = true)
data class PbHealthResponse(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class PbListResponse<T>(
    @Json(name = "page") val page: Int = 1,
    @Json(name = "perPage") val perPage: Int = 500,
    @Json(name = "totalItems") val totalItems: Int = 0,
    @Json(name = "totalPages") val totalPages: Int = 1,
    @Json(name = "items") val items: List<T> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PbIdOnlyDto(
    @Json(name = "id") val id: String = ""
)

@JsonClass(generateAdapter = true)
data class PbStudentDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "nis") val nis: String? = null,
    @Json(name = "gender") val gender: String? = null,
    @Json(name = "age") val age: Int? = null,
    @Json(name = "dormitory") val dormitory: String? = null,
    @Json(name = "class_grade") val classGrade: String? = null,
    @Json(name = "allergies") val allergies: String? = null,
    @Json(name = "blood_type") val bloodType: String? = null,
    @Json(name = "emergency_contact") val emergencyContact: String? = null,
    @Json(name = "special_notes") val specialNotes: String? = null,
    @Json(name = "nik") val nik: String? = null,
    @Json(name = "no_kk") val noKk: String? = null,
    @Json(name = "birth_place") val birthPlace: String? = null,
    @Json(name = "birth_date") val birthDate: String? = null,
    @Json(name = "mother_name") val motherName: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "district") val district: String? = null,
    @Json(name = "room_number") val roomNumber: String? = null,
    @Json(name = "jenjang") val jenjang: String? = null,
    @Json(name = "parent_phone") val parentPhone: String? = null,
    @Json(name = "assigned_wali_username") val assignedWaliUsername: String? = null,
    @Json(name = "assigned_wali_name") val assignedWaliName: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "photo_fallback_url") val photoFallbackUrl: String? = null,
    @Json(name = "religion") val religion: String? = null,
    @Json(name = "tandem_walis_json") val tandemWalisJson: String? = null,
    @Json(name = "tandem_requests_json") val tandemRequestsJson: String? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): ChildStudent {
        return ChildStudent(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            name = name,
            nis = nis ?: "",
            gender = gender?.ifBlank { "L" } ?: "L",
            age = age ?: 0,
            dormitory = dormitory ?: "",
            classGrade = classGrade ?: "",
            allergies = allergies?.ifBlank { "Tidak ada" } ?: "Tidak ada",
            bloodType = bloodType?.ifBlank { "-" } ?: "-",
            emergencyContact = emergencyContact?.ifBlank { "Pengasuh Asrama" } ?: "Pengasuh Asrama",
            specialNotes = specialNotes ?: "",
            nik = com.example.util.StudentCsvParser.cleanDigitsOnly(nik),
            noKk = com.example.util.StudentCsvParser.cleanDigitsOnly(noKk),
            birthPlace = birthPlace ?: "",
            birthDate = birthDate ?: "",
            motherName = motherName ?: "",
            address = address ?: "",
            district = district ?: "",
            roomNumber = roomNumber ?: "",
            jenjang = jenjang ?: "",
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            parentPhone = com.example.util.StudentCsvParser.cleanPhoneNumber(parentPhone),
            assignedWaliUsername = assignedWaliUsername ?: "",
            assignedWaliName = assignedWaliName ?: "",
            photoUrl = photoUrl?.trim().orEmpty(),
            photoFallbackUrl = photoFallbackUrl?.trim().orEmpty(),
            religion = com.example.data.model.StudentReligionHelper.normalize(religion),
            tandemWalisJson = tandemWalisJson.orEmpty(),
            tandemRequestsJson = tandemRequestsJson.orEmpty()
        )
    }

    companion object {
        fun fromDomain(model: ChildStudent): PbStudentDto {
            val cleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.nik)
            val cleanNoKk = com.example.util.StudentCsvParser.cleanDigitsOnly(model.noKk)
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "${model.schoolId}:$cleanNik:${model.nis}:${model.name}"
            )
            return PbStudentDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                name = model.name,
                nis = model.nis,
                gender = model.gender,
                age = model.age,
                dormitory = model.dormitory,
                classGrade = model.classGrade,
                allergies = model.allergies,
                bloodType = model.bloodType,
                emergencyContact = model.emergencyContact,
                specialNotes = model.specialNotes,
                nik = cleanNik,
                noKk = cleanNoKk,
                birthPlace = model.birthPlace,
                birthDate = model.birthDate,
                motherName = model.motherName,
                address = model.address,
                district = model.district,
                roomNumber = model.roomNumber,
                jenjang = model.jenjang,
                parentPhone = com.example.util.StudentCsvParser.cleanPhoneNumber(model.parentPhone),
                assignedWaliUsername = model.assignedWaliUsername,
                assignedWaliName = model.assignedWaliName,
                photoUrl = model.photoUrl.trim(),
                photoFallbackUrl = model.photoFallbackUrl.trim(),
                religion = com.example.data.model.StudentReligionHelper.normalize(model.religion),
                tandemWalisJson = model.tandemWalisJson,
                tandemRequestsJson = model.tandemRequestsJson
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbMedicalRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_dormitory") val studentDormitory: String? = null,
    @Json(name = "faskes") val faskes: String = "",
    @Json(name = "complaint") val complaint: String = "",
    @Json(name = "is_direct_treatment") val isDirectTreatment: Boolean = false,
    @Json(name = "plan_date") val planDate: Long = 0L,
    @Json(name = "departure_time") val departureTime: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "treatment_date") val treatmentDate: Long? = null,
    @Json(name = "diagnosis") val diagnosis: String? = null,
    @Json(name = "actions_taken") val actionsTaken: String? = null,
    @Json(name = "prescription") val prescription: String? = null,
    @Json(name = "needs_control") val needsControl: Boolean = false,
    @Json(name = "control_date") val controlDate: Long? = null,
    @Json(name = "is_control_completed") val isControlCompleted: Boolean = false,
    @Json(name = "escort_name") val escortName: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "is_inpatient") val isInpatient: Boolean = false,
    @Json(name = "inpatient_room") val inpatientRoom: String? = null,
    @Json(name = "discharge_date") val dischargeDate: Long? = null,
    @Json(name = "created_by") val createdBy: String? = null,
    @Json(name = "edited_by") val editedBy: String? = null,
    @Json(name = "edited_at") val editedAt: Long? = null,
    @Json(name = "created_at") val createdAt: Long? = null
) {
    fun toDomainModel(): MedicalRecord {
        val resolvedCreated = createdAt ?: if (planDate > 0L) planDate else System.currentTimeMillis()
        return MedicalRecord(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            studentId = studentId,
            studentName = studentName,
            studentDormitory = studentDormitory ?: "",
            faskes = faskes,
            complaint = complaint,
            isDirectTreatment = isDirectTreatment,
            planDate = planDate,
            departureTime = departureTime?.ifBlank { "08:30" } ?: "08:30",
            status = status?.ifBlank { "PLANNED" } ?: "PLANNED",
            treatmentDate = treatmentDate?.takeIf { it > 0L },
            diagnosis = diagnosis ?: "",
            actionsTaken = actionsTaken ?: "",
            prescription = prescription ?: "",
            needsControl = needsControl,
            controlDate = controlDate?.takeIf { it > 0L },
            isControlCompleted = isControlCompleted,
            escortName = escortName ?: "",
            notes = notes ?: "",
            isInpatient = isInpatient,
            inpatientRoom = inpatientRoom ?: "",
            dischargeDate = dischargeDate?.takeIf { it > 0L },
            createdBy = createdBy ?: "",
            editedBy = editedBy ?: "",
            editedAt = editedAt?.takeIf { it > 0L },
            createdAt = resolvedCreated
        )
    }

    companion object {
        fun fromDomain(model: MedicalRecord, schoolId: String = "SRT1-KDR"): PbMedicalRecordDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "$schoolId:${model.studentId}:${model.planDate}:${model.complaint}"
            )
            return PbMedicalRecordDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentDormitory = model.studentDormitory,
                faskes = model.faskes,
                complaint = model.complaint,
                isDirectTreatment = model.isDirectTreatment,
                planDate = model.planDate,
                departureTime = model.departureTime,
                status = model.status,
                treatmentDate = model.treatmentDate ?: 0L,
                diagnosis = model.diagnosis,
                actionsTaken = model.actionsTaken,
                prescription = model.prescription,
                needsControl = model.needsControl,
                controlDate = model.controlDate ?: 0L,
                isControlCompleted = model.isControlCompleted,
                escortName = model.escortName,
                notes = model.notes,
                isInpatient = model.isInpatient,
                inpatientRoom = model.inpatientRoom,
                dischargeDate = model.dischargeDate ?: 0L,
                createdBy = model.createdBy,
                editedBy = model.editedBy,
                editedAt = model.editedAt ?: 0L,
                createdAt = model.createdAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbHaidRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "start_date") val startDate: Long = 0L,
    @Json(name = "end_date") val endDate: Long? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "symptoms") val symptoms: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "wali_name") val waliName: String? = null,
    @Json(name = "last_checked_in_at") val lastCheckedInAt: Long? = null,
    @Json(name = "last_check_in_by") val lastCheckInBy: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): HaidRecord {
        return HaidRecord(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            studentId = studentId,
            studentName = studentName,
            studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(studentNik),
            studentNis = studentNis ?: "",
            studentClass = studentClass ?: "",
            studentDorm = studentDorm ?: "",
            startDate = startDate,
            endDate = endDate?.takeIf { it > 0L },
            status = status?.ifBlank { "HAID" } ?: "HAID",
            symptoms = symptoms?.ifBlank { "Normal" } ?: "Normal",
            notes = notes ?: "",
            waliName = waliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            lastCheckedInAt = lastCheckedInAt?.takeIf { it > 0L },
            lastCheckInBy = lastCheckInBy ?: "",
            createdAt = createdAt?.takeIf { it > 0L } ?: startDate,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: startDate
        )
    }

    companion object {
        fun fromDomain(model: HaidRecord): PbHaidRecordDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "${model.schoolId}:${model.studentId}:${model.startDate}"
            )
            return PbHaidRecordDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik),
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                startDate = model.startDate,
                endDate = model.endDate ?: 0L,
                status = model.status,
                symptoms = model.symptoms,
                notes = model.notes,
                waliName = model.waliName,
                lastCheckedInAt = model.lastCheckedInAt ?: 0L,
                lastCheckInBy = model.lastCheckInBy,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbChristianWorshipRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "collectionId") val collectionId: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "activity_type") val activityType: String? = null,
    @Json(name = "activity_date") val activityDate: Long? = null,
    @Json(name = "date_key") val dateKey: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "wali_username") val waliUsername: String? = null,
    @Json(name = "wali_name") val waliName: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "photo_fallback_url") val photoFallbackUrl: String? = null,
    @Json(name = "pb_record_id") val pbRecordId: String? = null,
    @Json(name = "pb_collection_id") val pbCollectionId: String? = null,
    @Json(name = "pb_filename") val pbFilename: String? = null,
    @Json(name = "photo_file") val photoFile: String? = null,
    @Json(name = "proof_uploaded_at") val proofUploadedAt: Long? = null,
    @Json(name = "proof_expires_at") val proofExpiresAt: Long? = null,
    @Json(name = "proof_status") val proofStatus: String? = null,
    @Json(name = "is_media_purged_from_server") val isMediaPurgedFromServer: Boolean? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(
        fallbackSchoolId: String = "SRT1-KDR",
        pbBaseUrl: String = DashboardBannerItem.DEFAULT_POCKETBASE_BASE_URL
    ): ChristianWorshipRecord {
        val now = System.currentTimeMillis()
        val resolvedSchool = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        val purged = isMediaPurgedFromServer ?: false
        val recId = pbRecordId.orEmpty().trim().ifBlank { id.orEmpty().trim() }
        val colId = pbCollectionId.orEmpty().trim().ifBlank { collectionId.orEmpty().trim() }
            .ifBlank { "christian_worship_records" }
        val file = pbFilename.orEmpty().trim().ifBlank { photoFile.orEmpty().trim() }
        val resolvedPrimary = if (purged) "" else {
            photoUrl?.takeIf { it.isNotBlank() } ?: run {
                if (recId.isNotBlank() && file.isNotBlank()) {
                    DashboardBannerItem.buildR2CdnUrl(colId, recId, file)
                } else ""
            }
        }
        val resolvedFallback = if (purged) "" else {
            photoFallbackUrl?.takeIf { it.isNotBlank() } ?: run {
                if (recId.isNotBlank() && file.isNotBlank()) {
                    DashboardBannerItem.buildPbApiFileUrl(pbBaseUrl, colId, recId, file)
                } else ""
            }
        }
        val actDate = activityDate?.takeIf { it > 0L } ?: now
        val uploaded = proofUploadedAt?.takeIf { it > 0L }
        val expires = proofExpiresAt?.takeIf { it > 0L } ?: uploaded?.let { ChristianWorshipHelper.computeExpiresAt(it) }
        val expiredByTime = expires != null && expires <= now
        val status = when {
            purged || proofStatus == ChristianWorshipRecord.PROOF_STATUS_EXPIRED || expiredByTime ->
                ChristianWorshipRecord.PROOF_STATUS_EXPIRED
            resolvedPrimary.isNotBlank() || resolvedFallback.isNotBlank() || proofStatus == ChristianWorshipRecord.PROOF_STATUS_ACTIVE ->
                ChristianWorshipRecord.PROOF_STATUS_ACTIVE
            else -> ChristianWorshipRecord.PROOF_STATUS_NONE
        }
        val finalPurged = purged || (status == ChristianWorshipRecord.PROOF_STATUS_EXPIRED && resolvedPrimary.isBlank() && resolvedFallback.isBlank())
        return ChristianWorshipRecord(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = resolvedSchool,
            studentId = studentId,
            studentName = studentName,
            studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(studentNik),
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            activityType = ChristianWorshipHelper.normalizeType(activityType),
            activityDate = actDate,
            dateKey = dateKey?.takeIf { it.isNotBlank() } ?: ChristianWorshipHelper.dateKeyFromMillis(actDate),
            title = title.orEmpty().trim(),
            notes = notes.orEmpty().trim(),
            waliUsername = waliUsername.orEmpty().trim(),
            waliName = waliName?.takeIf { it.isNotBlank() } ?: "Wali Asuh",
            photoUrl = if (finalPurged) "" else resolvedPrimary,
            photoFallbackUrl = if (finalPurged) "" else resolvedFallback,
            localPhotoPath = "",
            pbRecordId = recId,
            pbCollectionId = if (recId.isNotBlank()) colId else "",
            pbFilename = if (finalPurged) "" else file,
            proofUploadedAt = uploaded,
            proofExpiresAt = expires,
            proofStatus = status,
            isMediaPurgedFromServer = finalPurged,
            isDeleted = isDeleted ?: false,
            createdAt = createdAt?.takeIf { it > 0L } ?: actDate,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: ChristianWorshipRecord): PbChristianWorshipRecordDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "christian_worship:${model.schoolId}:${model.studentId}:${model.activityType}:${model.createdAt}"
            )
            val pbId = model.pbRecordId.trim().takeIf { it.length == 15 } ?: PocketBaseIdCodec.toPbId(safeId)
            return PbChristianWorshipRecordDto(
                id = pbId,
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik),
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                activityType = ChristianWorshipHelper.normalizeType(model.activityType),
                activityDate = model.activityDate,
                dateKey = model.dateKey.ifBlank { ChristianWorshipHelper.dateKeyFromMillis(model.activityDate) },
                title = model.title,
                notes = model.notes,
                waliUsername = model.waliUsername,
                waliName = model.waliName,
                photoUrl = if (model.isMediaPurgedFromServer) "" else model.photoUrl,
                photoFallbackUrl = if (model.isMediaPurgedFromServer) "" else model.photoFallbackUrl,
                pbRecordId = pbId,
                pbCollectionId = model.pbCollectionId.ifBlank { "christian_worship_records" },
                pbFilename = if (model.isMediaPurgedFromServer) "" else model.pbFilename,
                proofUploadedAt = model.proofUploadedAt,
                proofExpiresAt = model.proofExpiresAt,
                proofStatus = model.proofStatus,
                isMediaPurgedFromServer = model.isMediaPurgedFromServer,
                isDeleted = model.isDeleted,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}


@JsonClass(generateAdapter = true)
data class PbWaliAccountDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "username") val username: String = "",
    @Json(name = "password") val password: String = "",
    @Json(name = "dormitory") val dormitory: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "photo_fallback_url") val photoFallbackUrl: String? = null,
    @Json(name = "title_position") val titlePosition: String? = null,
    @Json(name = "employee_code") val employeeCode: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "profile_updated_at") val profileUpdatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): WaliAccount {
        val resolvedRole = role?.trim()?.uppercase()?.ifBlank { WaliAccount.ROLE_WALI }
            ?: WaliAccount.ROLE_WALI
        return WaliAccount(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            name = name,
            username = username,
            password = password,
            dormitory = dormitory?.ifBlank { "Asrama Putra" } ?: "Asrama Putra",
            phone = phone ?: "",
            createdAt = createdAt?.takeIf { it > 0L } ?: System.currentTimeMillis(),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            role = resolvedRole,
            photoUrl = photoUrl?.trim().orEmpty(),
            photoFallbackUrl = photoFallbackUrl?.trim().orEmpty(),
            titlePosition = titlePosition?.trim().orEmpty(),
            employeeCode = employeeCode?.trim().orEmpty(),
            bio = bio?.trim().orEmpty(),
            profileUpdatedAt = profileUpdatedAt ?: 0L
        )
    }

    companion object {
        fun fromDomain(model: WaliAccount): PbWaliAccountDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "${model.schoolId}:${model.username}"
            )
            return PbWaliAccountDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                name = model.name,
                username = model.username,
                password = model.password,
                dormitory = model.dormitory,
                phone = model.phone,
                role = model.role.trim().uppercase().ifBlank { WaliAccount.ROLE_WALI },
                createdAt = model.createdAt,
                photoUrl = model.photoUrl.trim(),
                photoFallbackUrl = model.photoFallbackUrl.trim(),
                titlePosition = model.titlePosition.trim(),
                employeeCode = model.employeeCode.trim(),
                bio = model.bio.trim(),
                profileUpdatedAt = model.profileUpdatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbShiftMessageDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "follow_up_date") val followUpDate: Long = 0L,
    @Json(name = "shift") val shift: String? = null,
    @Json(name = "target_wali") val targetWali: String? = null,
    @Json(name = "message_content") val messageContent: String = "",
    @Json(name = "priority") val priority: String? = null,
    @Json(name = "sender_name") val senderName: String = "",
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "is_completed") val isCompleted: Boolean = false,
    @Json(name = "completed_by") val completedBy: String? = null,
    @Json(name = "completed_at") val completedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): ShiftMessage {
        return ShiftMessage(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            title = title,
            followUpDate = followUpDate,
            shift = shift?.ifBlank { "Semua Shift" } ?: "Semua Shift",
            targetWali = targetWali?.ifBlank { "Semua Wali Asuh" } ?: "Semua Wali Asuh",
            messageContent = messageContent,
            priority = priority?.ifBlank { "BIASA" } ?: "BIASA",
            senderName = senderName,
            createdAt = createdAt?.takeIf { it > 0L } ?: System.currentTimeMillis(),
            isCompleted = isCompleted,
            completedBy = completedBy ?: "",
            completedAt = completedAt ?: 0L,
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        )
    }

    companion object {
        fun fromDomain(model: ShiftMessage): PbShiftMessageDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "${model.schoolId}:${model.createdAt}:${model.title}"
            )
            return PbShiftMessageDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                title = model.title,
                followUpDate = model.followUpDate,
                shift = model.shift,
                targetWali = model.targetWali,
                messageContent = model.messageContent,
                priority = model.priority,
                senderName = model.senderName,
                createdAt = model.createdAt,
                isCompleted = model.isCompleted,
                completedBy = model.completedBy,
                completedAt = model.completedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbSchoolRegistrationDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_name") val schoolName: String = "",
    @Json(name = "address") val address: String = "",
    @Json(name = "city") val city: String = "",
    @Json(name = "estimated_wali_count") val estimatedWaliCount: Int = 0,
    @Json(name = "pic_name") val picName: String = "",
    @Json(name = "pic_email") val picEmail: String? = null,
    @Json(name = "pic_phone") val picPhone: String = "",
    @Json(name = "submission_date") val submissionDate: Long? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "rejection_reason") val rejectionReason: String? = null,
    @Json(name = "generated_school_code") val generatedSchoolCode: String? = null,
    @Json(name = "generated_admin_username") val generatedAdminUsername: String? = null,
    @Json(name = "generated_admin_password") val generatedAdminPassword: String? = null,
    @Json(name = "approved_date") val approvedDate: Long? = null
) {
    fun toDomainModel(): SchoolRegistration {
        return SchoolRegistration(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolName = schoolName,
            address = address,
            city = city,
            estimatedWaliCount = estimatedWaliCount,
            picName = picName,
            picEmail = picEmail ?: "",
            picPhone = picPhone,
            submissionDate = submissionDate?.takeIf { it > 0L } ?: System.currentTimeMillis(),
            status = status?.ifBlank { "PENDING" } ?: "PENDING",
            rejectionReason = rejectionReason ?: "",
            generatedSchoolCode = generatedSchoolCode ?: "",
            generatedAdminUsername = generatedAdminUsername ?: "",
            generatedAdminPassword = generatedAdminPassword ?: "",
            approvedDate = approvedDate ?: 0L
        )
    }

    companion object {
        fun fromDomain(model: SchoolRegistration): PbSchoolRegistrationDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "${model.schoolName}:${model.picPhone}:${model.submissionDate}"
            )
            return PbSchoolRegistrationDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolName = model.schoolName,
                address = model.address,
                city = model.city,
                estimatedWaliCount = model.estimatedWaliCount,
                picName = model.picName,
                picEmail = model.picEmail,
                picPhone = model.picPhone,
                submissionDate = model.submissionDate,
                status = model.status,
                rejectionReason = model.rejectionReason,
                generatedSchoolCode = model.generatedSchoolCode,
                generatedAdminUsername = model.generatedAdminUsername,
                generatedAdminPassword = model.generatedAdminPassword,
                approvedDate = model.approvedDate
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbFastingSessionDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "session_date") val sessionDate: Long = 0L,
    @Json(name = "description") val description: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "created_by") val createdBy: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): FastingSession {
        return FastingSession(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            title = title,
            sessionDate = sessionDate,
            description = description ?: "",
            isActive = isActive,
            createdBy = createdBy?.ifBlank { "Admin Sekolah" } ?: "Admin Sekolah",
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: sessionDate,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: sessionDate
        )
    }

    companion object {
        fun fromDomain(model: FastingSession): PbFastingSessionDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "${model.schoolId}:${model.sessionDate}:${model.title}"
            )
            return PbFastingSessionDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                title = model.title,
                sessionDate = model.sessionDate,
                description = model.description,
                isActive = model.isActive,
                createdBy = model.createdBy,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbFastingRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "session_id") val sessionId: Long = 0L,
    @Json(name = "session_title") val sessionTitle: String? = null,
    @Json(name = "session_date") val sessionDate: Long? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "student_jenjang") val studentJenjang: String? = null,
    @Json(name = "is_sahur_checked") val isSahurChecked: Boolean = false,
    @Json(name = "sahur_checked_at") val sahurCheckedAt: Long? = null,
    @Json(name = "sahur_checked_by") val sahurCheckedBy: String? = null,
    @Json(name = "is_buka_checked") val isBukaChecked: Boolean = false,
    @Json(name = "buka_checked_at") val bukaCheckedAt: Long? = null,
    @Json(name = "buka_checked_by") val bukaCheckedBy: String? = null,
    @Json(name = "wali_name") val waliName: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): FastingRecord {
        return FastingRecord(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            sessionId = sessionId,
            sessionTitle = sessionTitle ?: "",
            sessionDate = sessionDate ?: 0L,
            studentId = studentId,
            studentName = studentName,
            studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(studentNik),
            studentNis = studentNis ?: "",
            studentClass = studentClass ?: "",
            studentDorm = studentDorm ?: "",
            studentJenjang = studentJenjang ?: "",
            isSahurChecked = isSahurChecked,
            sahurCheckedAt = sahurCheckedAt?.takeIf { it > 0L },
            sahurCheckedBy = sahurCheckedBy ?: "",
            isBukaChecked = isBukaChecked,
            bukaCheckedAt = bukaCheckedAt?.takeIf { it > 0L },
            bukaCheckedBy = bukaCheckedBy ?: "",
            waliName = waliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            notes = notes ?: "",
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: System.currentTimeMillis(),
            updatedAt = updatedAt?.takeIf { it > 0L } ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(model: FastingRecord): PbFastingRecordDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "${model.schoolId}:${model.sessionId}:${model.studentId}:${model.studentName}"
            )
            return PbFastingRecordDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                sessionId = model.sessionId,
                sessionTitle = model.sessionTitle,
                sessionDate = model.sessionDate,
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik),
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                studentJenjang = model.studentJenjang,
                isSahurChecked = model.isSahurChecked,
                sahurCheckedAt = model.sahurCheckedAt ?: 0L,
                sahurCheckedBy = model.sahurCheckedBy,
                isBukaChecked = model.isBukaChecked,
                bukaCheckedAt = model.bukaCheckedAt ?: 0L,
                bukaCheckedBy = model.bukaCheckedBy,
                waliName = model.waliName,
                notes = model.notes,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbSchoolProfileDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "school_name") val schoolName: String = "",
    @Json(name = "short_name") val shortName: String? = null,
    @Json(name = "admin_name") val adminName: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "primary_faskes_name") val primaryFaskesName: String? = null,
    @Json(name = "secondary_faskes_name") val secondaryFaskesName: String? = null,
    @Json(name = "additional_faskes_json") val additionalFaskesJson: String? = null,
    @Json(name = "faskes_contacts_json") val faskesContactsJson: String? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): SchoolProfile {
        val cleanName = schoolName.trim().ifBlank { "Sekolah Rakyat SRT 1 Kabupaten Kediri" }
        val additionalList = try {
            if (additionalFaskesJson.isNullOrBlank()) {
                emptyList()
            } else {
                val arr = JSONArray(additionalFaskesJson)
                buildList {
                    for (i in 0 until arr.length()) {
                        val item = arr.optString(i, "").trim()
                        if (item.isNotBlank()) add(item)
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }

        return SchoolProfile(
            schoolName = cleanName,
            shortName = SchoolProfile.sanitizeShortName(cleanName, shortName),
            adminName = adminName?.takeIf { it.isNotBlank() } ?: "Admin Utama",
            address = address ?: "",
            city = city?.takeIf { it.isNotBlank() } ?: "Kabupaten Kediri",
            tenantCode = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            primaryFaskesName = primaryFaskesName?.takeIf { it.isNotBlank() } ?: "UKS Sekolah",
            secondaryFaskesName = secondaryFaskesName ?: "",
            additionalFaskesList = additionalList,
            faskesContacts = SchoolProfile.parseFaskesContacts(faskesContactsJson)
        )
    }

    fun toDashboardLayoutConfig(): DashboardIconLayoutConfig? {
        val parsed = DashboardIconKey.parseJson(additionalFaskesJson) ?: return null
        return DashboardIconLayoutConfig(
            order = parsed,
            updatedAt = updatedAt ?: 0L
        )
    }

    fun toDashboardBannerConfig(): DashboardBannerConfig? {
        val parsed = DashboardBannerConfig.fromJsonString(additionalFaskesJson) ?: return null
        return parsed.copy(updatedAt = maxOf(parsed.updatedAt, updatedAt ?: 0L))
    }

    companion object {
        const val GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID = "__GLOBAL_DASHBOARD_LAYOUT__"
        const val GLOBAL_DASHBOARD_BANNER_SCHOOL_ID = "__GLOBAL_DASHBOARD_BANNER__"

        fun fromDomain(profile: SchoolProfile, updatedAt: Long): PbSchoolProfileDto {
            val cleanCode = profile.tenantCode.trim().ifBlank { "SRT1-KDR" }
            val localHash = PocketBaseIdCodec.stablePositiveHash("school_profile:${cleanCode.uppercase()}")
            val additionalArr = JSONArray().apply {
                profile.additionalFaskesList.map { it.trim() }.filter { it.isNotBlank() }.forEach { put(it) }
            }
            return PbSchoolProfileDto(
                id = PocketBaseIdCodec.toPbSchoolProfileId(cleanCode),
                localId = localHash,
                schoolId = cleanCode,
                schoolName = profile.schoolName,
                shortName = profile.shortName,
                adminName = profile.adminName,
                address = profile.address,
                city = profile.city,
                primaryFaskesName = profile.primaryFaskesName,
                secondaryFaskesName = profile.secondaryFaskesName,
                additionalFaskesJson = additionalArr.toString(),
                faskesContactsJson = SchoolProfile.serializeFaskesContacts(profile.faskesContacts),
                updatedAt = updatedAt
            )
        }

        fun fromDashboardLayout(
            order: List<DashboardIconKey>,
            updatedAt: Long
        ): PbSchoolProfileDto {
            val localHash = PocketBaseIdCodec.stablePositiveHash(
                "school_profile:${GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID}"
            )
            return PbSchoolProfileDto(
                id = PocketBaseIdCodec.toPbSchoolProfileId(GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID),
                localId = localHash,
                schoolId = GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID,
                schoolName = "Global Dashboard Icon Layout",
                shortName = "Global Layout",
                adminName = "anaksuperadmin",
                address = "",
                city = "Global",
                primaryFaskesName = "UKS Sekolah",
                secondaryFaskesName = "",
                additionalFaskesJson = DashboardIconKey.serializeJson(order),
                faskesContactsJson = "[]",
                updatedAt = updatedAt
            )
        }

        fun fromDashboardBannerConfig(
            config: DashboardBannerConfig
        ): PbSchoolProfileDto {
            val clean = config.normalized()
            val localHash = PocketBaseIdCodec.stablePositiveHash(
                "school_profile:${GLOBAL_DASHBOARD_BANNER_SCHOOL_ID}"
            )
            return PbSchoolProfileDto(
                id = PocketBaseIdCodec.toPbSchoolProfileId(GLOBAL_DASHBOARD_BANNER_SCHOOL_ID),
                localId = localHash,
                schoolId = GLOBAL_DASHBOARD_BANNER_SCHOOL_ID,
                schoolName = "Global Dashboard Banner Carousel",
                shortName = "Global Banner",
                adminName = "anaksuperadmin",
                address = "",
                city = "Global",
                primaryFaskesName = "UKS Sekolah",
                secondaryFaskesName = "",
                additionalFaskesJson = clean.toJsonString(),
                faskesContactsJson = "[]",
                updatedAt = clean.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbMengajiRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "student_jenjang") val studentJenjang: String? = null,
    @Json(name = "session_date") val sessionDate: Long? = null,
    @Json(name = "book_type") val bookType: String? = null,
    @Json(name = "jilid_or_juz") val jilidOrJuz: String? = null,
    @Json(name = "page_or_verse") val pageOrVerse: String? = null,
    @Json(name = "start_page") val startPage: Int? = null,
    @Json(name = "end_page") val endPage: Int? = null,
    @Json(name = "fluency_status") val fluencyStatus: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "wali_name") val waliName: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): MengajiRecord {
        val now = System.currentTimeMillis()
        val resolvedSession = sessionDate?.takeIf { it > 0L } ?: now
        return MengajiRecord(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            studentId = studentId,
            studentName = studentName,
            studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(studentNik),
            studentNis = studentNis ?: "",
            studentClass = studentClass ?: "",
            studentDorm = studentDorm ?: "",
            studentJenjang = studentJenjang ?: "",
            sessionDate = resolvedSession,
            bookType = bookType?.ifBlank { "Iqro'" } ?: "Iqro'",
            jilidOrJuz = jilidOrJuz?.ifBlank { "Jilid 1" } ?: "Jilid 1",
            pageOrVerse = pageOrVerse?.ifBlank { "Hal. 1" } ?: "Hal. 1",
            startPage = startPage?.takeIf { it > 0 },
            endPage = endPage?.takeIf { it > 0 },
            fluencyStatus = fluencyStatus?.ifBlank { MengajiHelper.FLUENCY_LANCAR } ?: MengajiHelper.FLUENCY_LANCAR,
            notes = notes ?: "",
            waliName = waliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: resolvedSession,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedSession
        )
    }

    companion object {
        fun fromDomain(model: MengajiRecord): PbMengajiRecordDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "${model.schoolId}:${model.studentId}:${model.sessionDate}:${model.jilidOrJuz}:${model.pageOrVerse}"
            )
            return PbMengajiRecordDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik),
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                studentJenjang = model.studentJenjang,
                sessionDate = model.sessionDate,
                bookType = model.bookType,
                jilidOrJuz = model.jilidOrJuz,
                pageOrVerse = model.pageOrVerse,
                startPage = model.startPage ?: 0,
                endPage = model.endPage ?: 0,
                fluencyStatus = model.fluencyStatus,
                notes = model.notes,
                waliName = model.waliName,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbVisitSessionDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "month_year") val monthYear: String? = null,
    @Json(name = "session_dates_csv") val sessionDatesCsv: String? = null,
    @Json(name = "selected_districts_csv") val selectedDistrictsCsv: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "visit_time_range") val visitTimeRange: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "published_to_wali") val publishedToWali: Boolean? = null,
    @Json(name = "published_to_parent") val publishedToParent: Boolean? = null,
    @Json(name = "wali_launched_at") val waliLaunchedAt: Long? = null,
    @Json(name = "parent_launched_at") val parentLaunchedAt: Long? = null,
    @Json(name = "updated_notice_after_parent_launch") val updatedNoticeAfterParentLaunch: Boolean? = null,
    @Json(name = "created_by") val createdBy: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): VisitSession {
        val now = System.currentTimeMillis()
        val resolvedCreated = createdAt?.takeIf { it > 0L } ?: now
        val resolvedParentLive = publishedToParent ?: true
        val resolvedWaliLive = (publishedToWali ?: true) || resolvedParentLive
        return VisitSession(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            title = title.ifBlank { "Kunjungan Bulanan" },
            monthYear = monthYear?.takeIf { it.isNotBlank() } ?: VisitHelper.buildDefaultMonthYear(resolvedCreated),
            sessionDatesCsv = sessionDatesCsv.orEmpty(),
            selectedDistrictsCsv = selectedDistrictsCsv.orEmpty(),
            description = description.orEmpty(),
            visitTimeRange = VisitHelper.normalizeTimeRange(visitTimeRange),
            isActive = isActive ?: true,
            publishedToWali = resolvedWaliLive,
            publishedToParent = resolvedParentLive,
            waliLaunchedAt = waliLaunchedAt ?: 0L,
            parentLaunchedAt = parentLaunchedAt ?: 0L,
            updatedNoticeAfterParentLaunch = updatedNoticeAfterParentLaunch ?: false,
            createdBy = createdBy?.ifBlank { "Admin Sekolah" } ?: "Admin Sekolah",
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = resolvedCreated,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedCreated
        )
    }

    companion object {
        fun fromDomain(model: VisitSession): PbVisitSessionDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "visit_session:${model.schoolId}:${model.monthYear}:${model.title}:${model.createdAt}"
            )
            return PbVisitSessionDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                title = model.title,
                monthYear = model.monthYear,
                sessionDatesCsv = model.sessionDatesCsv,
                selectedDistrictsCsv = model.selectedDistrictsCsv,
                description = model.description,
                visitTimeRange = model.displayTimeRange(),
                isActive = model.isActive,
                publishedToWali = model.publishedToWali || model.publishedToParent,
                publishedToParent = model.publishedToParent,
                waliLaunchedAt = model.waliLaunchedAt,
                parentLaunchedAt = model.parentLaunchedAt,
                updatedNoticeAfterParentLaunch = model.updatedNoticeAfterParentLaunch,
                createdBy = model.createdBy,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbVisitRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "session_id") val sessionId: Long = 0L,
    @Json(name = "session_title") val sessionTitle: String? = null,
    @Json(name = "visit_date") val visitDate: Long? = null,
    @Json(name = "checked_in_at") val checkedInAt: Long? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "student_district") val studentDistrict: String? = null,
    @Json(name = "visitor_name") val visitorName: String? = null,
    @Json(name = "visitor_relation") val visitorRelation: String? = null,
    @Json(name = "check_in_method") val checkInMethod: String? = null,
    @Json(name = "is_out_of_schedule_district") val isOutOfScheduleDistrict: Boolean? = null,
    @Json(name = "wali_name") val waliName: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): VisitRecord {
        val now = System.currentTimeMillis()
        val resolvedVisitDate = visitDate?.takeIf { it > 0L } ?: now
        val resolvedCheckIn = checkedInAt?.takeIf { it > 0L } ?: resolvedVisitDate
        return VisitRecord(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            sessionId = sessionId,
            sessionTitle = sessionTitle.orEmpty(),
            visitDate = resolvedVisitDate,
            checkedInAt = resolvedCheckIn,
            studentId = studentId,
            studentName = studentName,
            studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(studentNik),
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            studentDistrict = VisitHelper.normalizeDistrict(studentDistrict),
            visitorName = visitorName.orEmpty(),
            visitorRelation = visitorRelation?.ifBlank { "Orang Tua / Wali" } ?: "Orang Tua / Wali",
            checkInMethod = checkInMethod?.ifBlank { VisitRecord.METHOD_BARCODE } ?: VisitRecord.METHOD_BARCODE,
            isOutOfScheduleDistrict = isOutOfScheduleDistrict ?: false,
            waliName = waliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            notes = notes.orEmpty(),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: resolvedCheckIn,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedCheckIn
        )
    }

    companion object {
        fun fromDomain(model: VisitRecord): PbVisitRecordDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "visit_rec:${model.schoolId}:${model.sessionId}:${model.studentId}:${model.visitDate}:${model.studentName}"
            )
            return PbVisitRecordDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                sessionId = model.sessionId,
                sessionTitle = model.sessionTitle,
                visitDate = model.visitDate,
                checkedInAt = model.checkedInAt,
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik),
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                studentDistrict = VisitHelper.normalizeDistrict(model.studentDistrict),
                visitorName = model.visitorName,
                visitorRelation = model.visitorRelation,
                checkInMethod = model.checkInMethod,
                isOutOfScheduleDistrict = model.isOutOfScheduleDistrict,
                waliName = model.waliName,
                notes = model.notes,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbVisitConfirmationDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "session_id") val sessionId: Long = 0L,
    @Json(name = "session_title") val sessionTitle: String? = null,
    @Json(name = "visit_date") val visitDate: Long? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "student_district") val studentDistrict: String? = null,
    @Json(name = "parent_username") val parentUsername: String? = null,
    @Json(name = "visitor_name") val visitorName: String? = null,
    @Json(name = "visitor_relation") val visitorRelation: String? = null,
    @Json(name = "attendance_status") val attendanceStatus: String? = null,
    @Json(name = "estimated_arrival_time") val estimatedArrivalTime: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "barcode_token") val barcodeToken: String? = null,
    @Json(name = "confirmed_at") val confirmedAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): VisitConfirmation {
        val now = System.currentTimeMillis()
        val resolvedConfirmed = confirmedAt?.takeIf { it > 0L } ?: now
        return VisitConfirmation(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            sessionId = sessionId,
            sessionTitle = sessionTitle.orEmpty(),
            visitDate = visitDate ?: 0L,
            studentId = studentId,
            studentName = studentName,
            studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(studentNik),
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            studentDistrict = VisitHelper.normalizeDistrict(studentDistrict),
            parentUsername = parentUsername.orEmpty(),
            visitorName = visitorName.orEmpty(),
            visitorRelation = visitorRelation?.ifBlank { "Orang Tua / Wali" } ?: "Orang Tua / Wali",
            attendanceStatus = attendanceStatus?.ifBlank { VisitConfirmation.STATUS_HADIR } ?: VisitConfirmation.STATUS_HADIR,
            estimatedArrivalTime = estimatedArrivalTime.orEmpty(),
            notes = notes.orEmpty(),
            barcodeToken = barcodeToken?.takeIf { it.isNotBlank() } ?: VisitHelper.buildBarcodeToken(sessionId, studentId),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            confirmedAt = resolvedConfirmed,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedConfirmed
        )
    }

    companion object {
        fun fromDomain(model: VisitConfirmation): PbVisitConfirmationDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "visit_conf:${model.schoolId}:${model.sessionId}:${model.studentId}:${model.studentName}"
            )
            return PbVisitConfirmationDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                sessionId = model.sessionId,
                sessionTitle = model.sessionTitle,
                visitDate = model.visitDate,
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik),
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                studentDistrict = VisitHelper.normalizeDistrict(model.studentDistrict),
                parentUsername = model.parentUsername,
                visitorName = model.visitorName,
                visitorRelation = model.visitorRelation,
                attendanceStatus = model.attendanceStatus,
                estimatedArrivalTime = model.estimatedArrivalTime,
                notes = model.notes,
                barcodeToken = model.barcodeToken,
                confirmedAt = model.confirmedAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbVisitSessionSwapDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "month_year") val monthYear: String? = null,
    @Json(name = "from_session_id") val fromSessionId: Long = 0L,
    @Json(name = "from_session_title") val fromSessionTitle: String? = null,
    @Json(name = "to_session_id") val toSessionId: Long = 0L,
    @Json(name = "to_session_title") val toSessionTitle: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "student_district") val studentDistrict: String? = null,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "had_prior_check_in") val hadPriorCheckIn: Boolean? = null,
    @Json(name = "prior_check_in_at") val priorCheckInAt: Long? = null,
    @Json(name = "swapped_by_username") val swappedByUsername: String? = null,
    @Json(name = "swapped_by_name") val swappedByName: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): VisitSessionSwap {
        val now = System.currentTimeMillis()
        val resolvedCreated = createdAt?.takeIf { it > 0L } ?: now
        return VisitSessionSwap(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            monthYear = monthYear.orEmpty(),
            fromSessionId = fromSessionId,
            fromSessionTitle = fromSessionTitle.orEmpty(),
            toSessionId = toSessionId,
            toSessionTitle = toSessionTitle.orEmpty(),
            studentId = studentId,
            studentName = studentName,
            studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(studentNik),
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            studentDistrict = VisitHelper.normalizeDistrict(studentDistrict),
            reason = reason.orEmpty(),
            hadPriorCheckIn = hadPriorCheckIn ?: false,
            priorCheckInAt = priorCheckInAt ?: 0L,
            swappedByUsername = swappedByUsername.orEmpty(),
            swappedByName = swappedByName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            createdAt = resolvedCreated,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedCreated
        )
    }

    companion object {
        fun fromDomain(model: VisitSessionSwap): PbVisitSessionSwapDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "visit_swap:${model.schoolId}:${model.fromSessionId}:${model.studentId}:${model.studentName}"
            )
            return PbVisitSessionSwapDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                monthYear = model.monthYear,
                fromSessionId = model.fromSessionId,
                fromSessionTitle = model.fromSessionTitle,
                toSessionId = model.toSessionId,
                toSessionTitle = model.toSessionTitle,
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik),
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                studentDistrict = VisitHelper.normalizeDistrict(model.studentDistrict),
                reason = model.reason,
                hadPriorCheckIn = model.hadPriorCheckIn,
                priorCheckInAt = model.priorCheckInAt,
                swappedByUsername = model.swappedByUsername,
                swappedByName = model.swappedByName,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbParentAccessLinkDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "parent_name") val parentName: String? = null,
    @Json(name = "parent_phone") val parentPhone: String? = null,
    @Json(name = "username") val username: String = "",
    @Json(name = "password") val password: String = "",
    @Json(name = "assigned_wali_username") val assignedWaliUsername: String? = null,
    @Json(name = "assigned_wali_name") val assignedWaliName: String? = null,
    @Json(name = "tandem_walis_json") val tandemWalisJson: String? = null,
    @Json(name = "can_view_mengaji") val canViewMengaji: Boolean? = null,
    @Json(name = "can_view_medical") val canViewMedical: Boolean? = null,
    @Json(name = "can_view_fasting") val canViewFasting: Boolean? = null,
    @Json(name = "can_view_haid") val canViewHaid: Boolean? = null,
    @Json(name = "can_view_profile") val canViewProfile: Boolean? = null,
    @Json(name = "can_view_tabungan") val canViewTabungan: Boolean? = null,
    @Json(name = "can_view_sholat") val canViewSholat: Boolean? = null,
    @Json(name = "deletion_requested") val deletionRequested: Boolean? = null,
    @Json(name = "deletion_reason") val deletionReason: String? = null,
    @Json(name = "deletion_requested_at") val deletionRequestedAt: Long? = null,
    @Json(name = "student_photo_url") val studentPhotoUrl: String? = null,
    @Json(name = "student_photo_fallback_url") val studentPhotoFallbackUrl: String? = null,
    @Json(name = "student_religion") val studentReligion: String? = null,
    @Json(name = "student_district") val studentDistrict: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): ParentAccessLink {
        val now = System.currentTimeMillis()
        val resolvedCreated = createdAt?.takeIf { it > 0L } ?: now
        return ParentAccessLink(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            studentId = studentId,
            studentName = studentName,
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            parentName = parentName.orEmpty(),
            parentPhone = com.example.util.StudentCsvParser.cleanPhoneNumber(parentPhone),
            username = username,
            password = password,
            assignedWaliUsername = assignedWaliUsername.orEmpty(),
            assignedWaliName = assignedWaliName.orEmpty(),
            tandemWalisJson = tandemWalisJson.orEmpty(),
            canViewMengaji = canViewMengaji ?: true,
            canViewMedical = canViewMedical ?: true,
            canViewFasting = canViewFasting ?: true,
            canViewHaid = canViewHaid ?: true,
            canViewProfile = canViewProfile ?: true,
            canViewTabungan = canViewTabungan ?: false,
            canViewSholat = canViewSholat ?: true,
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            deletionRequested = deletionRequested ?: false,
            deletionReason = deletionReason.orEmpty(),
            deletionRequestedAt = deletionRequestedAt ?: 0L,
            studentPhotoUrl = studentPhotoUrl?.trim().orEmpty(),
            studentPhotoFallbackUrl = studentPhotoFallbackUrl?.trim().orEmpty(),
            studentReligion = com.example.data.model.StudentReligionHelper.normalize(studentReligion),
            studentDistrict = VisitHelper.normalizeDistrict(studentDistrict),
            createdAt = resolvedCreated,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedCreated
        )
    }

    companion object {
        fun fromDomain(model: ParentAccessLink): PbParentAccessLinkDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "parent_access:${model.schoolId}:${model.studentId}:${model.username}"
            )
            return PbParentAccessLinkDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                parentName = model.parentName,
                parentPhone = com.example.util.StudentCsvParser.cleanPhoneNumber(model.parentPhone),
                username = model.username,
                password = model.password,
                assignedWaliUsername = model.assignedWaliUsername,
                assignedWaliName = model.assignedWaliName,
                tandemWalisJson = model.tandemWalisJson,
                canViewMengaji = model.canViewMengaji,
                canViewMedical = model.canViewMedical,
                canViewFasting = model.canViewFasting,
                canViewHaid = model.canViewHaid,
                canViewProfile = model.canViewProfile,
                canViewTabungan = model.canViewTabungan,
                canViewSholat = model.canViewSholat,
                deletionRequested = model.deletionRequested,
                deletionReason = model.deletionReason,
                deletionRequestedAt = model.deletionRequestedAt,
                studentPhotoUrl = model.studentPhotoUrl.trim(),
                studentPhotoFallbackUrl = model.studentPhotoFallbackUrl.trim(),
                studentReligion = com.example.data.model.StudentReligionHelper.normalize(model.studentReligion),
                studentDistrict = VisitHelper.normalizeDistrict(model.studentDistrict),
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbParentMessageDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "collectionId") val collectionId: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "parent_name") val parentName: String? = null,
    @Json(name = "wali_username") val waliUsername: String? = null,
    @Json(name = "wali_name") val waliName: String? = null,
    @Json(name = "sender_role") val senderRole: String? = null,
    @Json(name = "sender_name") val senderName: String? = null,
    @Json(name = "message_content") val messageContent: String? = null,
    @Json(name = "is_read_by_wali") val isReadByWali: Boolean? = null,
    @Json(name = "is_read_by_parent") val isReadByParent: Boolean? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "photo_fallback_url") val photoFallbackUrl: String? = null,
    @Json(name = "pb_record_id") val pbRecordId: String? = null,
    @Json(name = "pb_collection_id") val pbCollectionId: String? = null,
    @Json(name = "pb_filename") val pbFilename: String? = null,
    @Json(name = "photo_file") val photoFile: String? = null,
    @Json(name = "is_downloaded_by_recipient") val isDownloadedByRecipient: Boolean? = null,
    @Json(name = "is_media_purged_from_server") val isMediaPurgedFromServer: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(
        fallbackSchoolId: String = "SRT1-KDR",
        pbBaseUrl: String = DashboardBannerItem.DEFAULT_POCKETBASE_BASE_URL
    ): ParentMessage {
        val now = System.currentTimeMillis()
        val purged = isMediaPurgedFromServer ?: false
        val recId = pbRecordId.orEmpty().trim().ifBlank { id.orEmpty().trim() }
        val colId = pbCollectionId.orEmpty().trim().ifBlank { collectionId.orEmpty().trim() }.ifBlank { "parent_messages" }
        val file = pbFilename.orEmpty().trim().ifBlank { photoFile.orEmpty().trim() }
        val resolvedPrimary = if (purged) "" else {
            photoUrl?.takeIf { it.isNotBlank() } ?: run {
                if (recId.isNotBlank() && file.isNotBlank()) {
                    DashboardBannerItem.buildR2CdnUrl(colId, recId, file)
                } else ""
            }
        }
        val resolvedFallback = if (purged) "" else {
            photoFallbackUrl?.takeIf { it.isNotBlank() } ?: run {
                if (recId.isNotBlank() && file.isNotBlank()) {
                    DashboardBannerItem.buildPbApiFileUrl(pbBaseUrl, colId, recId, file)
                } else ""
            }
        }
        val created = createdAt?.takeIf { it > 0L } ?: now
        return ParentMessage(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            studentId = studentId,
            studentName = studentName.orEmpty(),
            parentName = parentName.orEmpty(),
            waliUsername = waliUsername.orEmpty(),
            waliName = waliName.orEmpty(),
            senderRole = senderRole?.ifBlank { "WALI" } ?: "WALI",
            senderName = senderName.orEmpty(),
            messageContent = messageContent.orEmpty(),
            isReadByWali = isReadByWali ?: false,
            isReadByParent = isReadByParent ?: false,
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = created,
            photoUrl = resolvedPrimary,
            photoFallbackUrl = resolvedFallback,
            pbRecordId = recId,
            pbCollectionId = if (recId.isNotBlank()) colId else "",
            pbFilename = if (purged) "" else file,
            isDownloadedByRecipient = isDownloadedByRecipient ?: false,
            isMediaPurgedFromServer = purged,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: created
        )
    }

    companion object {
        fun fromDomain(model: ParentMessage): PbParentMessageDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "parent_msg:${model.schoolId}:${model.studentId}:${model.createdAt}:${model.senderRole}"
            )
            val pbId = model.pbRecordId.trim().takeIf { it.length == 15 } ?: PocketBaseIdCodec.toPbId(safeId)
            return PbParentMessageDto(
                id = pbId,
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                parentName = model.parentName,
                waliUsername = model.waliUsername,
                waliName = model.waliName,
                senderRole = model.senderRole,
                senderName = model.senderName,
                messageContent = model.messageContent,
                isReadByWali = model.isReadByWali,
                isReadByParent = model.isReadByParent,
                photoUrl = if (model.isMediaPurgedFromServer) "" else model.photoUrl,
                photoFallbackUrl = if (model.isMediaPurgedFromServer) "" else model.photoFallbackUrl,
                pbRecordId = pbId,
                pbCollectionId = model.pbCollectionId.ifBlank { "parent_messages" },
                pbFilename = if (model.isMediaPurgedFromServer) "" else model.pbFilename,
                isDownloadedByRecipient = model.isDownloadedByRecipient,
                isMediaPurgedFromServer = model.isMediaPurgedFromServer,
                createdAt = model.createdAt,
                updatedAt = if (model.updatedAt > 0L) model.updatedAt else model.createdAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbParentExpectationDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "parent_name") val parentName: String? = null,
    @Json(name = "wali_username") val waliUsername: String? = null,
    @Json(name = "wali_name") val waliName: String? = null,
    @Json(name = "character_focus_csv") val characterFocusCsv: String? = null,
    @Json(name = "character_expectation") val characterExpectation: String = "",
    @Json(name = "advice_focus_notes") val adviceFocusNotes: String? = null,
    @Json(name = "parent_prayer_message") val parentPrayerMessage: String? = null,
    @Json(name = "is_read_by_wali") val isReadByWali: Boolean? = null,
    @Json(name = "wali_follow_up_note") val waliFollowUpNote: String? = null,
    @Json(name = "wali_responded_at") val waliRespondedAt: Long? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): ParentExpectation {
        val now = System.currentTimeMillis()
        val resolvedCreated = createdAt?.takeIf { it > 0L } ?: now
        return ParentExpectation(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            studentId = studentId,
            studentName = studentName.orEmpty(),
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            parentName = parentName.orEmpty(),
            waliUsername = waliUsername.orEmpty(),
            waliName = waliName.orEmpty(),
            characterFocusCsv = characterFocusCsv.orEmpty(),
            characterExpectation = characterExpectation,
            adviceFocusNotes = adviceFocusNotes.orEmpty(),
            parentPrayerMessage = parentPrayerMessage.orEmpty(),
            isReadByWali = isReadByWali ?: false,
            waliFollowUpNote = waliFollowUpNote.orEmpty(),
            waliRespondedAt = waliRespondedAt ?: 0L,
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = resolvedCreated,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedCreated
        )
    }

    companion object {
        fun fromDomain(model: ParentExpectation): PbParentExpectationDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "parent_exp:${model.schoolId}:${model.studentId}:${model.createdAt}"
            )
            return PbParentExpectationDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                parentName = model.parentName,
                waliUsername = model.waliUsername,
                waliName = model.waliName,
                characterFocusCsv = model.characterFocusCsv,
                characterExpectation = model.characterExpectation,
                adviceFocusNotes = model.adviceFocusNotes,
                parentPrayerMessage = model.parentPrayerMessage,
                isReadByWali = model.isReadByWali,
                waliFollowUpNote = model.waliFollowUpNote,
                waliRespondedAt = model.waliRespondedAt,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbDeletionLogDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "category") val category: String = "",
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "record_id") val recordId: Long = 0L,
    @Json(name = "deleted_summary") val deletedSummary: String? = null,
    @Json(name = "deleted_by") val deletedBy: String? = null,
    @Json(name = "deleted_by_username") val deletedByUsername: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): DeletionLog {
        val now = System.currentTimeMillis()
        return DeletionLog(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            category = category.trim().uppercase().ifBlank { DeletionLog.CATEGORY_MENGAJI },
            studentId = studentId,
            studentName = studentName,
            studentClass = studentClass.orEmpty(),
            recordId = recordId,
            deletedSummary = deletedSummary.orEmpty(),
            deletedBy = deletedBy?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            deletedByUsername = deletedByUsername.orEmpty(),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: DeletionLog): PbDeletionLogDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "del_log:${model.schoolId}:${model.category}:${model.recordId}:${model.createdAt}"
            )
            return PbDeletionLogDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                category = model.category,
                studentId = model.studentId,
                studentName = model.studentName,
                studentClass = model.studentClass,
                recordId = model.recordId,
                deletedSummary = model.deletedSummary,
                deletedBy = model.deletedBy,
                deletedByUsername = model.deletedByUsername,
                createdAt = model.createdAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbTabunganTransactionDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "owner_wali_username") val ownerWaliUsername: String? = null,
    @Json(name = "owner_wali_name") val ownerWaliName: String? = null,
    @Json(name = "is_private") val isPrivate: Boolean? = null,
    @Json(name = "transaction_type") val transactionType: String? = null,
    @Json(name = "amount") val amount: Long = 0L,
    @Json(name = "title") val title: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "transaction_date") val transactionDate: Long? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "cancelled_by") val cancelledBy: String? = null,
    @Json(name = "cancelled_reason") val cancelledReason: String? = null,
    @Json(name = "cancelled_at") val cancelledAt: Long? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): TabunganTransaction {
        val now = System.currentTimeMillis()
        val resolvedTxDate = transactionDate?.takeIf { it > 0L } ?: now
        return TabunganTransaction(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            studentId = studentId,
            studentName = studentName,
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            ownerWaliUsername = ownerWaliUsername.orEmpty(),
            ownerWaliName = ownerWaliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            isPrivate = isPrivate ?: true,
            transactionType = transactionType?.trim()?.uppercase()?.ifBlank { TabunganHelper.TYPE_IN } ?: TabunganHelper.TYPE_IN,
            amount = amount.coerceAtLeast(0L),
            title = title.orEmpty(),
            notes = notes.orEmpty(),
            transactionDate = resolvedTxDate,
            status = status?.trim()?.uppercase()?.ifBlank { TabunganHelper.STATUS_ACTIVE } ?: TabunganHelper.STATUS_ACTIVE,
            cancelledBy = cancelledBy.orEmpty(),
            cancelledReason = cancelledReason.orEmpty(),
            cancelledAt = cancelledAt?.takeIf { it > 0L },
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: resolvedTxDate,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedTxDate
        )
    }

    companion object {
        fun fromDomain(model: TabunganTransaction): PbTabunganTransactionDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "tab_tx:${model.schoolId}:${model.ownerWaliUsername}:${model.studentId}:${model.createdAt}"
            )
            return PbTabunganTransactionDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                ownerWaliUsername = model.ownerWaliUsername,
                ownerWaliName = model.ownerWaliName,
                isPrivate = model.isPrivate,
                transactionType = model.transactionType,
                amount = model.amount,
                title = model.title,
                notes = model.notes,
                transactionDate = model.transactionDate,
                status = model.status,
                cancelledBy = model.cancelledBy,
                cancelledReason = model.cancelledReason,
                cancelledAt = model.cancelledAt ?: 0L,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbTabunganLogDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "transaction_id") val transactionId: Long = 0L,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "owner_wali_username") val ownerWaliUsername: String? = null,
    @Json(name = "owner_wali_name") val ownerWaliName: String? = null,
    @Json(name = "is_private") val isPrivate: Boolean? = null,
    @Json(name = "action_type") val actionType: String? = null,
    @Json(name = "amount") val amount: Long = 0L,
    @Json(name = "balance_before") val balanceBefore: Long = 0L,
    @Json(name = "balance_after") val balanceAfter: Long = 0L,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "actor_name") val actorName: String? = null,
    @Json(name = "actor_username") val actorUsername: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): TabunganLog {
        val now = System.currentTimeMillis()
        return TabunganLog(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            transactionId = transactionId,
            studentId = studentId,
            studentName = studentName,
            studentClass = studentClass.orEmpty(),
            ownerWaliUsername = ownerWaliUsername.orEmpty(),
            ownerWaliName = ownerWaliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            isPrivate = isPrivate ?: true,
            actionType = actionType?.trim()?.uppercase()?.ifBlank { TabunganHelper.LOG_INPUT_IN } ?: TabunganHelper.LOG_INPUT_IN,
            amount = amount.coerceAtLeast(0L),
            balanceBefore = balanceBefore.coerceAtLeast(0L),
            balanceAfter = balanceAfter.coerceAtLeast(0L),
            summary = summary.orEmpty(),
            actorName = actorName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            actorUsername = actorUsername.orEmpty(),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: TabunganLog): PbTabunganLogDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "tab_log:${model.schoolId}:${model.ownerWaliUsername}:${model.transactionId}:${model.createdAt}"
            )
            return PbTabunganLogDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                transactionId = model.transactionId,
                studentId = model.studentId,
                studentName = model.studentName,
                studentClass = model.studentClass,
                ownerWaliUsername = model.ownerWaliUsername,
                ownerWaliName = model.ownerWaliName,
                isPrivate = model.isPrivate,
                actionType = model.actionType,
                amount = model.amount,
                balanceBefore = model.balanceBefore,
                balanceAfter = model.balanceAfter,
                summary = model.summary,
                actorName = model.actorName,
                actorUsername = model.actorUsername,
                createdAt = model.createdAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbKonselingRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "owner_wali_username") val ownerWaliUsername: String? = null,
    @Json(name = "owner_wali_name") val ownerWaliName: String? = null,
    @Json(name = "is_confidential") val isConfidential: Boolean? = null,
    @Json(name = "share_with_tandem") val shareWithTandem: Boolean? = null,
    @Json(name = "entry_type") val entryType: String? = null,
    @Json(name = "topic") val topic: String? = null,
    @Json(name = "urgency_level") val urgencyLevel: String? = null,
    @Json(name = "mood_level") val moodLevel: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "intervention") val intervention: String? = null,
    @Json(name = "follow_up_plan") val followUpPlan: String? = null,
    @Json(name = "follow_up_date") val followUpDate: Long? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "session_date") val sessionDate: Long? = null,
    @Json(name = "photo_urls") val photoUrls: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): KonselingRecord {
        val now = System.currentTimeMillis()
        val resolvedSessionDate = sessionDate?.takeIf { it > 0L } ?: now
        return KonselingRecord(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            studentId = studentId,
            studentName = studentName,
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            ownerWaliUsername = ownerWaliUsername.orEmpty(),
            ownerWaliName = ownerWaliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            isConfidential = isConfidential ?: true,
            shareWithTandem = shareWithTandem ?: false,
            entryType = entryType?.trim()?.uppercase()?.ifBlank { KonselingHelper.ENTRY_SESI } ?: KonselingHelper.ENTRY_SESI,
            topic = topic?.trim()?.uppercase()?.ifBlank { KonselingHelper.TOPIC_ADAPTASI } ?: KonselingHelper.TOPIC_ADAPTASI,
            urgencyLevel = urgencyLevel?.trim()?.uppercase()?.ifBlank { KonselingHelper.URGENCY_RINGAN } ?: KonselingHelper.URGENCY_RINGAN,
            moodLevel = moodLevel?.trim()?.uppercase()?.ifBlank { KonselingHelper.MOOD_NETRAL } ?: KonselingHelper.MOOD_NETRAL,
            title = title.orEmpty(),
            summary = summary.orEmpty(),
            intervention = intervention.orEmpty(),
            followUpPlan = followUpPlan.orEmpty(),
            followUpDate = followUpDate?.takeIf { it > 0L },
            status = status?.trim()?.uppercase()?.ifBlank { KonselingHelper.STATUS_DIPANTAU } ?: KonselingHelper.STATUS_DIPANTAU,
            sessionDate = resolvedSessionDate,
            photoPaths = "",
            photoUrls = photoUrls.orEmpty(),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: resolvedSessionDate,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: resolvedSessionDate
        )
    }

    companion object {
        fun fromDomain(model: KonselingRecord): PbKonselingRecordDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "kons_rec:${model.schoolId}:${model.ownerWaliUsername}:${model.studentId}:${model.createdAt}"
            )
            return PbKonselingRecordDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                ownerWaliUsername = model.ownerWaliUsername,
                ownerWaliName = model.ownerWaliName,
                isConfidential = model.isConfidential,
                shareWithTandem = model.shareWithTandem,
                entryType = model.entryType,
                topic = model.topic,
                urgencyLevel = model.urgencyLevel,
                moodLevel = model.moodLevel,
                title = model.title,
                summary = model.summary,
                intervention = model.intervention,
                followUpPlan = model.followUpPlan,
                followUpDate = model.followUpDate ?: 0L,
                status = model.status,
                sessionDate = model.sessionDate,
                photoUrls = model.photoUrls,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbKonselingLogDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "record_id") val recordId: Long = 0L,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "owner_wali_username") val ownerWaliUsername: String? = null,
    @Json(name = "owner_wali_name") val ownerWaliName: String? = null,
    @Json(name = "is_confidential") val isConfidential: Boolean? = null,
    @Json(name = "action_type") val actionType: String? = null,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "actor_name") val actorName: String? = null,
    @Json(name = "actor_username") val actorUsername: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): KonselingLog {
        val now = System.currentTimeMillis()
        return KonselingLog(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            recordId = recordId,
            studentId = studentId,
            studentName = studentName,
            studentClass = studentClass.orEmpty(),
            ownerWaliUsername = ownerWaliUsername.orEmpty(),
            ownerWaliName = ownerWaliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            isConfidential = isConfidential ?: true,
            actionType = actionType?.trim()?.uppercase()?.ifBlank { KonselingHelper.LOG_INPUT_SESI } ?: KonselingHelper.LOG_INPUT_SESI,
            summary = summary.orEmpty(),
            actorName = actorName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh",
            actorUsername = actorUsername.orEmpty(),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: KonselingLog): PbKonselingLogDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "kons_log:${model.schoolId}:${model.ownerWaliUsername}:${model.recordId}:${model.createdAt}"
            )
            return PbKonselingLogDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                recordId = model.recordId,
                studentId = model.studentId,
                studentName = model.studentName,
                studentClass = model.studentClass,
                ownerWaliUsername = model.ownerWaliUsername,
                ownerWaliName = model.ownerWaliName,
                isConfidential = model.isConfidential,
                actionType = model.actionType,
                summary = model.summary,
                actorName = model.actorName,
                actorUsername = model.actorUsername,
                createdAt = model.createdAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbWaliShiftCodeDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "code") val code: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "shift_group") val shiftGroup: String? = null,
    @Json(name = "zone_label") val zoneLabel: String? = null,
    @Json(name = "time_range") val timeRange: String? = null,
    @Json(name = "start_hour") val startHour: Int? = null,
    @Json(name = "end_hour") val endHour: Int? = null,
    @Json(name = "work_hours") val workHours: Int? = null,
    @Json(name = "color_hex") val colorHex: String? = null,
    @Json(name = "sop_notes") val sopNotes: String? = null,
    @Json(name = "sort_order") val sortOrder: Int? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): WaliShiftCode {
        val now = System.currentTimeMillis()
        return WaliShiftCode(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            code = code.trim().uppercase(),
            name = name.trim(),
            shiftGroup = shiftGroup?.trim()?.uppercase()?.ifBlank { WaliScheduleHelper.GROUP_PAGI }
                ?: WaliScheduleHelper.GROUP_PAGI,
            zoneLabel = zoneLabel.orEmpty(),
            timeRange = timeRange.orEmpty(),
            startHour = (startHour ?: 7).coerceIn(0, 23),
            endHour = (endHour ?: 15).coerceIn(0, 23),
            workHours = (workHours ?: 8).coerceAtLeast(0),
            colorHex = colorHex?.takeIf { it.isNotBlank() } ?: "#0284C7",
            sopNotes = sopNotes.orEmpty(),
            sortOrder = sortOrder ?: 0,
            isActive = isActive ?: true,
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: WaliShiftCode): PbWaliShiftCodeDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "shift_code:${model.schoolId}:${model.code.trim().uppercase()}"
            )
            return PbWaliShiftCodeDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                code = model.code.trim().uppercase(),
                name = model.name.trim(),
                shiftGroup = model.shiftGroup.trim().uppercase(),
                zoneLabel = model.zoneLabel,
                timeRange = model.timeRange,
                startHour = model.startHour,
                endHour = model.endHour,
                workHours = model.workHours,
                colorHex = model.colorHex,
                sopNotes = model.sopNotes,
                sortOrder = model.sortOrder,
                isActive = model.isActive,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbWaliScheduleEntryDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "month_key") val monthKey: String = "",
    @Json(name = "wali_account_id") val waliAccountId: Long = 0L,
    @Json(name = "wali_username") val waliUsername: String? = null,
    @Json(name = "wali_name") val waliName: String = "",
    @Json(name = "wali_code") val waliCode: String? = null,
    @Json(name = "sort_order") val sortOrder: Int? = null,
    @Json(name = "day_codes_json") val dayCodesJson: String? = null,
    @Json(name = "day_pins_json") val dayPinsJson: String? = null,
    @Json(name = "day_leave_json") val dayLeaveJson: String? = null,
    @Json(name = "updated_by") val updatedBy: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): WaliScheduleEntry {
        val now = System.currentTimeMillis()
        return WaliScheduleEntry(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            monthKey = monthKey.ifBlank { WaliScheduleHelper.currentMonthKey(now) },
            waliAccountId = waliAccountId,
            waliUsername = waliUsername.orEmpty(),
            waliName = waliName,
            waliCode = waliCode.orEmpty(),
            sortOrder = sortOrder ?: 0,
            dayCodesJson = dayCodesJson?.ifBlank { "{}" } ?: "{}",
            dayPinsJson = dayPinsJson?.ifBlank { "{}" } ?: "{}",
            dayLeaveJson = dayLeaveJson?.ifBlank { "{}" } ?: "{}",
            updatedBy = updatedBy?.ifBlank { "Admin Sekolah" } ?: "Admin Sekolah",
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: WaliScheduleEntry): PbWaliScheduleEntryDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "wali_sched:${model.schoolId}:${model.monthKey}:${model.waliUsername.ifBlank { model.waliName }}"
            )
            return PbWaliScheduleEntryDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                monthKey = model.monthKey,
                waliAccountId = model.waliAccountId,
                waliUsername = model.waliUsername,
                waliName = model.waliName,
                waliCode = model.waliCode,
                sortOrder = model.sortOrder,
                dayCodesJson = model.dayCodesJson,
                dayPinsJson = model.dayPinsJson,
                dayLeaveJson = model.dayLeaveJson,
                updatedBy = model.updatedBy,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbInfoPentingPostDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "fallback_media_url") val fallbackMediaUrl: String? = null,
    @Json(name = "pb_record_id") val pbRecordId: String? = null,
    @Json(name = "pb_collection_id") val pbCollectionId: String? = null,
    @Json(name = "pb_filename") val pbFilename: String? = null,
    @Json(name = "media_name") val mediaName: String? = null,
    @Json(name = "media_size_bytes") val mediaSizeBytes: Long? = null,
    @Json(name = "external_link") val externalLink: String? = null,
    @Json(name = "author_display_name") val authorDisplayName: String? = null,
    @Json(name = "author_school_name") val authorSchoolName: String? = null,
    @Json(name = "is_pinned") val isPinned: Boolean? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(pbBaseUrl: String = DashboardBannerItem.DEFAULT_POCKETBASE_BASE_URL): InfoPentingPost {
        val now = System.currentTimeMillis()
        val recId = pbRecordId.orEmpty().trim()
        val colId = pbCollectionId.orEmpty().trim().ifBlank { "important_info_posts" }
        val file = pbFilename.orEmpty().trim()
        val resolvedPrimary = mediaUrl?.takeIf { it.isNotBlank() } ?: run {
            if (recId.isNotBlank() && file.isNotBlank()) {
                DashboardBannerItem.buildR2CdnUrl(colId, recId, file)
            } else {
                ""
            }
        }
        val resolvedFallback = fallbackMediaUrl?.takeIf { it.isNotBlank() } ?: run {
            if (recId.isNotBlank() && file.isNotBlank()) {
                DashboardBannerItem.buildPbApiFileUrl(pbBaseUrl, colId, recId, file)
            } else {
                ""
            }
        }
        return InfoPentingPost(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: InfoPentingHelper.GLOBAL_SCHOOL_ID,
            title = title.orEmpty().trim(),
            content = content.orEmpty().trim(),
            category = category?.trim()?.uppercase()?.ifBlank { InfoPentingHelper.CATEGORY_PENGUMUMAN }
                ?: InfoPentingHelper.CATEGORY_PENGUMUMAN,
            mediaType = mediaType?.trim()?.uppercase()?.ifBlank { InfoPentingHelper.MEDIA_NONE }
                ?: InfoPentingHelper.MEDIA_NONE,
            mediaUrl = resolvedPrimary,
            fallbackMediaUrl = resolvedFallback,
            pbRecordId = recId,
            pbCollectionId = if (recId.isNotBlank()) colId else "",
            pbFilename = file,
            mediaName = mediaName.orEmpty().trim(),
            mediaSizeBytes = mediaSizeBytes ?: 0L,
            externalLink = externalLink.orEmpty().trim(),
            authorDisplayName = InfoPentingHelper.resolvePublicAuthor(authorDisplayName),
            authorSchoolName = authorSchoolName.orEmpty().trim(),
            isPinned = isPinned ?: false,
            isDeleted = isDeleted ?: false,
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: InfoPentingPost): PbInfoPentingPostDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "info_post:${model.createdAt}:${model.title}"
            )
            return PbInfoPentingPostDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = InfoPentingHelper.GLOBAL_SCHOOL_ID,
                title = model.title,
                content = model.content,
                category = model.category,
                mediaType = model.mediaType,
                mediaUrl = model.mediaUrl,
                fallbackMediaUrl = model.fallbackMediaUrl,
                pbRecordId = model.pbRecordId,
                pbCollectionId = model.pbCollectionId,
                pbFilename = model.pbFilename,
                mediaName = model.mediaName,
                mediaSizeBytes = model.mediaSizeBytes,
                externalLink = model.externalLink,
                authorDisplayName = InfoPentingHelper.PUBLIC_AUTHOR_NAME,
                authorSchoolName = model.authorSchoolName,
                isPinned = model.isPinned,
                isDeleted = model.isDeleted,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbInfoPentingReactionDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "post_id") val postId: Long = 0L,
    @Json(name = "user_key") val userKey: String = "",
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "user_school_id") val userSchoolId: String? = null,
    @Json(name = "reaction_type") val reactionType: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(): InfoPentingReaction {
        val now = System.currentTimeMillis()
        return InfoPentingReaction(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: InfoPentingHelper.GLOBAL_SCHOOL_ID,
            postId = postId,
            userKey = userKey.trim(),
            userName = userName.orEmpty().trim(),
            userSchoolId = userSchoolId.orEmpty().trim(),
            reactionType = reactionType?.trim()?.uppercase()?.ifBlank { InfoPentingHelper.REACTION_NONE }
                ?: InfoPentingHelper.REACTION_NONE,
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: InfoPentingReaction): PbInfoPentingReactionDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "info_react:${model.postId}:${model.userKey}"
            )
            return PbInfoPentingReactionDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = InfoPentingHelper.GLOBAL_SCHOOL_ID,
                postId = model.postId,
                userKey = model.userKey,
                userName = model.userName,
                userSchoolId = model.userSchoolId,
                reactionType = model.reactionType,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbInfoPentingCommentDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "post_id") val postId: Long = 0L,
    @Json(name = "parent_comment_id") val parentCommentId: Long? = null,
    @Json(name = "reply_to_author_name") val replyToAuthorName: String? = null,
    @Json(name = "author_key") val authorKey: String = "",
    @Json(name = "author_name") val authorName: String? = null,
    @Json(name = "author_school_id") val authorSchoolId: String? = null,
    @Json(name = "author_school_name") val authorSchoolName: String? = null,
    @Json(name = "content") val content: String = "",
    @Json(name = "is_deleted") val isDeleted: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(): InfoPentingComment {
        val now = System.currentTimeMillis()
        return InfoPentingComment(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: InfoPentingHelper.GLOBAL_SCHOOL_ID,
            postId = postId,
            parentCommentId = parentCommentId ?: 0L,
            replyToAuthorName = replyToAuthorName.orEmpty().trim(),
            authorKey = authorKey.trim(),
            authorName = InfoPentingHelper.resolveCommentAuthorDisplay(authorName, null),
            authorSchoolId = authorSchoolId.orEmpty().trim(),
            authorSchoolName = authorSchoolName.orEmpty().trim(),
            content = content,
            isDeleted = isDeleted ?: false,
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: InfoPentingComment): PbInfoPentingCommentDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "info_comment:${model.postId}:${model.authorKey}:${model.createdAt}"
            )
            return PbInfoPentingCommentDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = InfoPentingHelper.GLOBAL_SCHOOL_ID,
                postId = model.postId,
                parentCommentId = model.parentCommentId,
                replyToAuthorName = model.replyToAuthorName,
                authorKey = model.authorKey,
                authorName = model.authorName,
                authorSchoolId = model.authorSchoolId,
                authorSchoolName = model.authorSchoolName,
                content = model.content,
                isDeleted = model.isDeleted,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbSholatRecordDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "date_key") val dateKey: String = "",
    @Json(name = "date_millis") val dateMillis: Long? = null,
    @Json(name = "waktu") val waktu: String = SholatWaktu.DZUHUR.code,
    @Json(name = "status") val status: String = SholatStatus.JAMAAH.code,
    @Json(name = "discipline_tag") val disciplineTag: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String = "",
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "student_jenjang") val studentJenjang: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "input_method") val inputMethod: String? = null,
    @Json(name = "recorded_by") val recordedBy: String? = null,
    @Json(name = "recorded_by_username") val recordedByUsername: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): SholatRecord {
        val now = System.currentTimeMillis()
        val resolvedSchool = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        val safeDateKey = dateKey.trim().ifBlank { SholatHelper.todayDateKey(dateMillis ?: now) }
        val resolvedWaktu = SholatWaktu.fromCode(waktu)
        val rawId = PocketBaseIdCodec.resolveLocalId(localId, id)
        val safeId = if (rawId > 0L) rawId else SholatHelper.deterministicId(resolvedSchool, studentId, safeDateKey, resolvedWaktu)
        return SholatRecord(
            id = safeId,
            dateKey = safeDateKey,
            dateMillis = dateMillis?.takeIf { it > 0L } ?: SholatHelper.startOfDayMillis(safeDateKey),
            waktu = resolvedWaktu.code,
            status = SholatStatus.fromCode(status).code,
            disciplineTag = disciplineTag.orEmpty().trim(),
            studentId = studentId,
            studentName = studentName,
            studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(studentNik),
            studentNis = studentNis.orEmpty(),
            studentClass = studentClass.orEmpty(),
            studentDorm = studentDorm.orEmpty(),
            studentJenjang = studentJenjang.orEmpty(),
            notes = notes.orEmpty(),
            inputMethod = inputMethod?.takeIf { it.isNotBlank() } ?: SholatHelper.METHOD_MANUAL,
            recordedBy = recordedBy?.takeIf { it.isNotBlank() } ?: "Wali Asuh",
            recordedByUsername = recordedByUsername.orEmpty(),
            schoolId = resolvedSchool,
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: SholatRecord): PbSholatRecordDto {
            val resolvedSchool = model.schoolId.ifBlank { "SRT1-KDR" }
            val resolvedWaktu = SholatWaktu.fromCode(model.waktu)
            val safeId = if (model.id > 0L) model.id else SholatHelper.deterministicId(
                resolvedSchool,
                model.studentId,
                model.dateKey,
                resolvedWaktu
            )
            return PbSholatRecordDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = resolvedSchool,
                dateKey = model.dateKey,
                dateMillis = model.dateMillis,
                waktu = resolvedWaktu.code,
                status = SholatStatus.fromCode(model.status).code,
                disciplineTag = model.disciplineTag,
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik),
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                studentJenjang = model.studentJenjang,
                notes = model.notes,
                inputMethod = model.inputMethod,
                recordedBy = model.recordedBy,
                recordedByUsername = model.recordedByUsername,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbWaliGalleryPostDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "collectionId") val collectionId: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "wali_username") val waliUsername: String? = null,
    @Json(name = "wali_name") val waliName: String? = null,
    @Json(name = "caption") val caption: String? = null,
    @Json(name = "activity_tag") val activityTag: String? = null,
    @Json(name = "target_scope") val targetScope: String? = null,
    @Json(name = "target_student_ids_csv") val targetStudentIdsCsv: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "photo_fallback_url") val photoFallbackUrl: String? = null,
    @Json(name = "pb_record_id") val pbRecordId: String? = null,
    @Json(name = "pb_collection_id") val pbCollectionId: String? = null,
    @Json(name = "pb_filename") val pbFilename: String? = null,
    @Json(name = "photo_file") val photoFile: String? = null,
    @Json(name = "retention_days") val retentionDays: Int? = null,
    @Json(name = "expires_at") val expiresAt: Long? = null,
    @Json(name = "expected_recipient_count") val expectedRecipientCount: Int? = null,
    @Json(name = "downloaded_by_csv") val downloadedByCsv: String? = null,
    @Json(name = "is_media_purged_from_server") val isMediaPurgedFromServer: Boolean? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(
        fallbackSchoolId: String = "SRT1-KDR",
        pbBaseUrl: String = DashboardBannerItem.DEFAULT_POCKETBASE_BASE_URL
    ): WaliGalleryPost {
        val now = System.currentTimeMillis()
        val resolvedSchool = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        val purged = isMediaPurgedFromServer ?: false
        val recId = pbRecordId.orEmpty().trim().ifBlank { id.orEmpty().trim() }
        val colId = pbCollectionId.orEmpty().trim().ifBlank { collectionId.orEmpty().trim() }.ifBlank { "wali_gallery_posts" }
        val file = pbFilename.orEmpty().trim().ifBlank { photoFile.orEmpty().trim() }
        val resolvedPrimary = if (purged) "" else {
            photoUrl?.takeIf { it.isNotBlank() } ?: run {
                if (recId.isNotBlank() && file.isNotBlank()) {
                    DashboardBannerItem.buildR2CdnUrl(colId, recId, file)
                } else ""
            }
        }
        val resolvedFallback = if (purged) "" else {
            photoFallbackUrl?.takeIf { it.isNotBlank() } ?: run {
                if (recId.isNotBlank() && file.isNotBlank()) {
                    DashboardBannerItem.buildPbApiFileUrl(pbBaseUrl, colId, recId, file)
                } else ""
            }
        }
        val resolvedDays = (retentionDays ?: WaliGalleryHelper.DEFAULT_RETENTION_DAYS).coerceIn(1, 90)
        val created = createdAt?.takeIf { it > 0L } ?: now
        return WaliGalleryPost(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = resolvedSchool,
            waliUsername = waliUsername.orEmpty().trim(),
            waliName = waliName?.takeIf { it.isNotBlank() } ?: "Wali Asuh",
            caption = caption.orEmpty().trim(),
            activityTag = activityTag?.trim()?.uppercase()?.ifBlank { WaliGalleryHelper.TAG_KEGIATAN }
                ?: WaliGalleryHelper.TAG_KEGIATAN,
            targetScope = targetScope?.trim()?.uppercase()?.ifBlank { WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS }
                ?: WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS,
            targetStudentIdsCsv = targetStudentIdsCsv.orEmpty().trim(),
            photoUrl = resolvedPrimary,
            photoFallbackUrl = resolvedFallback,
            pbRecordId = recId,
            pbCollectionId = if (recId.isNotBlank()) colId else "",
            pbFilename = if (purged) "" else file,
            retentionDays = resolvedDays,
            expiresAt = expiresAt?.takeIf { it > 0L } ?: (created + resolvedDays * WaliGalleryHelper.DAY_MILLIS),
            expectedRecipientCount = (expectedRecipientCount ?: 0).coerceAtLeast(0),
            downloadedByCsv = downloadedByCsv.orEmpty().trim(),
            isMediaPurgedFromServer = purged,
            isDeleted = isDeleted ?: false,
            createdAt = created,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: WaliGalleryPost): PbWaliGalleryPostDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "wali_gallery:${model.schoolId}:${model.waliUsername}:${model.createdAt}"
            )
            val pbId = model.pbRecordId.trim().takeIf { it.length == 15 } ?: PocketBaseIdCodec.toPbId(safeId)
            return PbWaliGalleryPostDto(
                id = pbId,
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                waliUsername = model.waliUsername,
                waliName = model.waliName,
                caption = model.caption,
                activityTag = model.activityTag,
                targetScope = model.targetScope,
                targetStudentIdsCsv = model.targetStudentIdsCsv,
                photoUrl = if (model.isMediaPurgedFromServer) "" else model.photoUrl,
                photoFallbackUrl = if (model.isMediaPurgedFromServer) "" else model.photoFallbackUrl,
                pbRecordId = pbId,
                pbCollectionId = model.pbCollectionId.ifBlank { "wali_gallery_posts" },
                pbFilename = if (model.isMediaPurgedFromServer) "" else model.pbFilename,
                retentionDays = model.retentionDays,
                expiresAt = model.expiresAt,
                expectedRecipientCount = model.expectedRecipientCount,
                downloadedByCsv = model.downloadedByCsv,
                isMediaPurgedFromServer = model.isMediaPurgedFromServer,
                isDeleted = model.isDeleted,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbBibleBookmarkDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "owner_wali_username") val ownerWaliUsername: String? = null,
    @Json(name = "owner_wali_name") val ownerWaliName: String? = null,
    @Json(name = "book_id") val bookId: String? = null,
    @Json(name = "book_name_id") val bookNameId: String? = null,
    @Json(name = "book_name_en") val bookNameEn: String? = null,
    @Json(name = "chapter") val chapter: Int? = null,
    @Json(name = "verse") val verse: Int? = null,
    @Json(name = "verse_text_id") val verseTextId: String? = null,
    @Json(name = "verse_text_en") val verseTextEn: String? = null,
    @Json(name = "highlight_color") val highlightColor: String? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): BibleBookmark {
        val now = System.currentTimeMillis()
        val resolvedSchool = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        return BibleBookmark(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = resolvedSchool,
            ownerWaliUsername = ownerWaliUsername.orEmpty().trim(),
            ownerWaliName = ownerWaliName?.takeIf { it.isNotBlank() } ?: "Wali Asuh",
            bookId = bookId?.trim()?.uppercase()?.ifBlank { "JHN" } ?: "JHN",
            bookNameId = bookNameId?.takeIf { it.isNotBlank() } ?: "Yohanes",
            bookNameEn = bookNameEn?.takeIf { it.isNotBlank() } ?: "John",
            chapter = (chapter ?: 1).coerceAtLeast(1),
            verse = (verse ?: 1).coerceAtLeast(1),
            verseTextId = verseTextId.orEmpty(),
            verseTextEn = verseTextEn.orEmpty(),
            highlightColor = highlightColor?.takeIf { it.isNotBlank() } ?: "GOLD",
            isDeleted = isDeleted ?: false,
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: BibleBookmark): PbBibleBookmarkDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "bible_bm:${model.schoolId}:${model.ownerWaliUsername}:${model.bookId}:${model.chapter}:${model.verse}"
            )
            return PbBibleBookmarkDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                ownerWaliUsername = model.ownerWaliUsername,
                ownerWaliName = model.ownerWaliName,
                bookId = model.bookId,
                bookNameId = model.bookNameId,
                bookNameEn = model.bookNameEn,
                chapter = model.chapter,
                verse = model.verse,
                verseTextId = model.verseTextId,
                verseTextEn = model.verseTextEn,
                highlightColor = model.highlightColor,
                isDeleted = model.isDeleted,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbBibleNoteDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "owner_wali_username") val ownerWaliUsername: String? = null,
    @Json(name = "owner_wali_name") val ownerWaliName: String? = null,
    @Json(name = "book_id") val bookId: String? = null,
    @Json(name = "book_name_id") val bookNameId: String? = null,
    @Json(name = "book_name_en") val bookNameEn: String? = null,
    @Json(name = "chapter") val chapter: Int? = null,
    @Json(name = "verse") val verse: Int? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "category_tag") val categoryTag: String? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): BibleNote {
        val now = System.currentTimeMillis()
        val resolvedSchool = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        return BibleNote(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = resolvedSchool,
            ownerWaliUsername = ownerWaliUsername.orEmpty().trim(),
            ownerWaliName = ownerWaliName?.takeIf { it.isNotBlank() } ?: "Wali Asuh",
            bookId = bookId?.trim()?.uppercase()?.ifBlank { "2TI" } ?: "2TI",
            bookNameId = bookNameId?.takeIf { it.isNotBlank() } ?: "2 Timotius",
            bookNameEn = bookNameEn?.takeIf { it.isNotBlank() } ?: "2 Timothy",
            chapter = (chapter ?: 1).coerceAtLeast(1),
            verse = (verse ?: 1).coerceAtLeast(0),
            title = title.orEmpty(),
            content = content.orEmpty(),
            categoryTag = categoryTag?.takeIf { it.isNotBlank() } ?: "RENUNGAN",
            isDeleted = isDeleted ?: false,
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: BibleNote): PbBibleNoteDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "bible_note:${model.schoolId}:${model.ownerWaliUsername}:${model.createdAt}"
            )
            return PbBibleNoteDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                ownerWaliUsername = model.ownerWaliUsername,
                ownerWaliName = model.ownerWaliName,
                bookId = model.bookId,
                bookNameId = model.bookNameId,
                bookNameEn = model.bookNameEn,
                chapter = model.chapter,
                verse = model.verse,
                title = model.title,
                content = model.content,
                categoryTag = model.categoryTag,
                isDeleted = model.isDeleted,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PbSharedJournalNoteDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "local_id") val localId: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "checklist_json") val checklistJson: String? = null,
    @Json(name = "image_urls_json") val imageUrlsJson: String? = null,
    @Json(name = "is_pinned") val isPinned: Boolean? = null,
    @Json(name = "author_username") val authorUsername: String? = null,
    @Json(name = "author_name") val authorName: String? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean? = null,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): SharedJournalNote {
        val now = System.currentTimeMillis()
        val resolvedSchool = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        return SharedJournalNote(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            schoolId = resolvedSchool,
            category = SharedJournalHelper.normalizeCategory(category),
            title = title.orEmpty(),
            content = content.orEmpty(),
            checklistJson = checklistJson.orEmpty(),
            imageUrlsJson = imageUrlsJson.orEmpty(),
            isPinned = isPinned ?: false,
            authorUsername = authorUsername.orEmpty().trim(),
            authorName = authorName?.takeIf { it.isNotBlank() } ?: "Wali Asuh",
            isDeleted = isDeleted ?: false,
            createdAt = createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = updatedAt?.takeIf { it > 0L } ?: now
        )
    }

    companion object {
        fun fromDomain(model: SharedJournalNote): PbSharedJournalNoteDto {
            val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
                "shared_journal:${model.schoolId}:${model.authorUsername}:${model.createdAt}:${model.title.hashCode()}"
            )
            return PbSharedJournalNoteDto(
                id = PocketBaseIdCodec.toPbId(safeId),
                localId = safeId,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                category = SharedJournalHelper.normalizeCategory(model.category),
                title = model.title,
                content = model.content,
                checklistJson = model.checklistJson,
                imageUrlsJson = model.imageUrlsJson,
                isPinned = model.isPinned,
                authorUsername = model.authorUsername,
                authorName = model.authorName,
                isDeleted = model.isDeleted,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }
}



