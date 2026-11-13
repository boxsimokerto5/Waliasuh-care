package com.example.data.remote.supabase

import com.example.data.model.ChildStudent
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolRegistration
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseStudentDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "name") val name: String,
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
            id = id ?: 0L,
            name = name,
            nis = nis ?: "",
            gender = gender ?: "L",
            age = age ?: 0,
            dormitory = dormitory ?: "",
            classGrade = classGrade ?: "",
            allergies = allergies ?: "Tidak ada",
            bloodType = bloodType ?: "-",
            emergencyContact = emergencyContact ?: "Pengasuh Asrama",
            specialNotes = specialNotes ?: "",
            nik = nik ?: "",
            noKk = noKk ?: "",
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
        fun fromDomain(model: ChildStudent): SupabaseStudentDto {
            return SupabaseStudentDto(
                id = if (model.id > 0) model.id else null,
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
                nik = model.nik,
                noKk = model.noKk,
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
data class SupabaseMedicalRecordDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long,
    @Json(name = "student_name") val studentName: String,
    @Json(name = "student_dormitory") val studentDormitory: String? = null,
    @Json(name = "faskes") val faskes: String,
    @Json(name = "complaint") val complaint: String,
    @Json(name = "is_direct_treatment") val isDirectTreatment: Boolean = false,
    @Json(name = "plan_date") val planDate: Long,
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
    @Json(name = "notes") val notes: String? = null
) {
    fun toDomainModel(): MedicalRecord {
        return MedicalRecord(
            id = id ?: 0L,
            studentId = studentId,
            studentName = studentName,
            studentDormitory = studentDormitory ?: "",
            faskes = faskes,
            complaint = complaint,
            isDirectTreatment = isDirectTreatment,
            planDate = planDate,
            departureTime = departureTime ?: "08:30",
            status = status ?: "PLANNED",
            treatmentDate = treatmentDate,
            diagnosis = diagnosis ?: "",
            actionsTaken = actionsTaken ?: "",
            prescription = prescription ?: "",
            needsControl = needsControl,
            controlDate = controlDate,
            isControlCompleted = isControlCompleted,
            escortName = escortName ?: "",
            notes = notes ?: ""
        )
    }

    companion object {
        fun fromDomain(model: MedicalRecord, schoolId: String = "SRT1-KDR"): SupabaseMedicalRecordDto {
            return SupabaseMedicalRecordDto(
                id = if (model.id > 0) model.id else null,
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
                treatmentDate = model.treatmentDate,
                diagnosis = model.diagnosis,
                actionsTaken = model.actionsTaken,
                prescription = model.prescription,
                needsControl = model.needsControl,
                controlDate = model.controlDate,
                isControlCompleted = model.isControlCompleted,
                escortName = model.escortName,
                notes = model.notes
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseHaidRecordDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "student_id") val studentId: Long = 0L,
    @Json(name = "student_name") val studentName: String,
    @Json(name = "student_nik") val studentNik: String? = null,
    @Json(name = "student_nis") val studentNis: String? = null,
    @Json(name = "student_class") val studentClass: String? = null,
    @Json(name = "student_dorm") val studentDorm: String? = null,
    @Json(name = "start_date") val startDate: Long,
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
            id = id ?: 0L,
            studentId = studentId,
            studentName = studentName,
            studentNik = studentNik ?: "",
            studentNis = studentNis ?: "",
            studentClass = studentClass ?: "",
            studentDorm = studentDorm ?: "",
            startDate = startDate,
            endDate = endDate,
            status = status ?: "HAID",
            symptoms = symptoms ?: "Normal",
            notes = notes ?: "",
            waliName = waliName ?: "Wali Asuh",
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId,
            createdAt = createdAt ?: startDate,
            updatedAt = updatedAt ?: startDate
        )
    }

    companion object {
        fun fromDomain(model: HaidRecord): SupabaseHaidRecordDto {
            return SupabaseHaidRecordDto(
                id = if (model.id > 0) model.id else null,
                schoolId = model.schoolId.ifBlank { "SRT1-KDR" },
                studentId = model.studentId,
                studentName = model.studentName,
                studentNik = model.studentNik,
                studentNis = model.studentNis,
                studentClass = model.studentClass,
                studentDorm = model.studentDorm,
                startDate = model.startDate,
                endDate = model.endDate,
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
data class SupabaseWaliAccountDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "dormitory") val dormitory: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "created_at") val createdAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): WaliAccount {
        return WaliAccount(
            id = id ?: 0L,
            name = name,
            username = username,
            password = password,
            dormitory = dormitory ?: "Asrama Putra",
            phone = phone ?: "",
            createdAt = createdAt ?: System.currentTimeMillis(),
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        )
    }

    companion object {
        fun fromDomain(model: WaliAccount): SupabaseWaliAccountDto {
            return SupabaseWaliAccountDto(
                id = if (model.id > 0) model.id else null,
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
data class SupabaseShiftMessageDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "school_id") val schoolId: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "follow_up_date") val followUpDate: Long,
    @Json(name = "shift") val shift: String? = null,
    @Json(name = "target_wali") val targetWali: String? = null,
    @Json(name = "message_content") val messageContent: String,
    @Json(name = "priority") val priority: String? = null,
    @Json(name = "sender_name") val senderName: String,
    @Json(name = "created_at") val createdAt: Long? = null,
    @Json(name = "is_completed") val isCompleted: Boolean = false,
    @Json(name = "completed_by") val completedBy: String? = null,
    @Json(name = "completed_at") val completedAt: Long? = null
) {
    fun toDomainModel(fallbackSchoolId: String = "SRT1-KDR"): ShiftMessage {
        return ShiftMessage(
            id = id ?: 0L,
            title = title,
            followUpDate = followUpDate,
            shift = shift ?: "Semua Shift",
            targetWali = targetWali ?: "Semua Wali Asuh",
            messageContent = messageContent,
            priority = priority ?: "BIASA",
            senderName = senderName,
            createdAt = createdAt ?: System.currentTimeMillis(),
            isCompleted = isCompleted,
            completedBy = completedBy ?: "",
            completedAt = completedAt ?: 0L,
            schoolId = schoolId?.takeIf { it.isNotBlank() } ?: fallbackSchoolId
        )
    }

    companion object {
        fun fromDomain(model: ShiftMessage): SupabaseShiftMessageDto {
            return SupabaseShiftMessageDto(
                id = if (model.id > 0) model.id else null,
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
data class SupabaseSchoolRegistrationDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "school_name") val schoolName: String,
    @Json(name = "address") val address: String,
    @Json(name = "city") val city: String,
    @Json(name = "estimated_wali_count") val estimatedWaliCount: Int = 0,
    @Json(name = "pic_name") val picName: String,
    @Json(name = "pic_email") val picEmail: String? = null,
    @Json(name = "pic_phone") val picPhone: String,
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
            id = id ?: 0L,
            schoolName = schoolName,
            address = address,
            city = city,
            estimatedWaliCount = estimatedWaliCount,
            picName = picName,
            picEmail = picEmail ?: "",
            picPhone = picPhone,
            submissionDate = submissionDate ?: System.currentTimeMillis(),
            status = status ?: "PENDING",
            rejectionReason = rejectionReason ?: "",
            generatedSchoolCode = generatedSchoolCode ?: "",
            generatedAdminUsername = generatedAdminUsername ?: "",
            generatedAdminPassword = generatedAdminPassword ?: "",
            approvedDate = approvedDate ?: 0L
        )
    }

    companion object {
        fun fromDomain(model: SchoolRegistration): SupabaseSchoolRegistrationDto {
            return SupabaseSchoolRegistrationDto(
                id = if (model.id > 0) model.id else null,
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
