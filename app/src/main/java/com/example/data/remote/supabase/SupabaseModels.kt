package com.example.data.remote.supabase

import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseStudentDto(
    @Json(name = "id") val id: Long? = null,
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
    fun toDomainModel(): ChildStudent {
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
            jenjang = jenjang ?: ""
        )
    }

    companion object {
        fun fromDomain(model: ChildStudent): SupabaseStudentDto {
            return SupabaseStudentDto(
                id = if (model.id > 0) model.id else null,
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
        fun fromDomain(model: MedicalRecord): SupabaseMedicalRecordDto {
            return SupabaseMedicalRecordDto(
                id = if (model.id > 0) model.id else null,
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
