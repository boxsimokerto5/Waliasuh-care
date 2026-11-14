package com.example.data.remote.pocketbase

import com.example.data.model.ChildStudent
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
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
    @Json(name = "room_number") val roomNumber: String? = null,
    @Json(name = "jenjang") val jenjang: String? = null
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
            roomNumber = roomNumber ?: "",
            jenjang = jenjang ?: "",
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
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
                roomNumber = model.roomNumber,
                jenjang = model.jenjang
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
    @Json(name = "created_at") val createdAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): WaliAccount {
        return WaliAccount(
            id = PocketBaseIdCodec.resolveLocalId(localId, id),
            name = name,
            username = username,
            password = password,
            dormitory = dormitory?.ifBlank { "Asrama Putra" } ?: "Asrama Putra",
            phone = phone ?: "",
            createdAt = createdAt?.takeIf { it > 0L } ?: System.currentTimeMillis(),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
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
                createdAt = model.createdAt
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

    companion object {
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
