package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_students")
data class ChildStudent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nis: String,
    val gender: String, // "L" (Laki-laki) atau "P" (Perempuan)
    val age: Int,
    val dormitory: String, // Asrama Putra / Asrama Putri
    val classGrade: String, // Kelas SD/SMP/SMA
    val allergies: String = "Tidak ada",
    val bloodType: String = "-",
    val emergencyContact: String = "Pengasuh Asrama",
    val specialNotes: String = "",
    val nik: String = "",
    val noKk: String = "",
    val birthPlace: String = "",
    val birthDate: String = "",
    val motherName: String = "",
    val address: String = "",
    val roomNumber: String = "",
    val jenjang: String = "",
    val schoolId: String = "SRT1-KDR"
)
