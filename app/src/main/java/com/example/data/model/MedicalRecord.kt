package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medical_records",
    foreignKeys = [
        ForeignKey(
            entity = ChildStudent::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId")]
)
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val studentDormitory: String,
    val faskes: String, // "Puskesmas Wates", "Puskesmas Plosoklaten", atau lainnya
    val complaint: String, // Keluhan atau gejala awal
    val isDirectTreatment: Boolean = false, // True jika pengobatan langsung tanpa lewat rencana
    val planDate: Long, // Tanggal rencana berobat (timestamp millis)
    val status: String = "PLANNED", // "PLANNED" (Menunggu), "DEPARTED" (Dianter/Siap Jalan), "COMPLETED" (Selesai), "CANCELLED"
    val departureTime: String = "08:30", // Estimasi jam keberangkatan seperti di mockup (08:30, 09:00, dsb)
    val treatmentDate: Long? = null, // Tanggal saat tindakan/pengobatan dilakukan
    val diagnosis: String = "", // Diagnosa dokter
    val actionsTaken: String = "", // Tindakan medis di puskesmas
    val prescription: String = "", // Resep obat dan anjuran
    val needsControl: Boolean = false, // Apakah perlu kontrol ulang?
    val controlDate: Long? = null, // Tanggal kontrol kembali jika perlu
    val isControlCompleted: Boolean = false, // Status jika kunjungan kontrol sudah selesai
    val escortName: String = "", // Nama pendamping/pengasuh yang mengantar
    val notes: String = "", // Catatan tambahan
    val isInpatient: Boolean = false, // True jika anak sedang rawat inap / opname
    val inpatientRoom: String = "", // Nama ruang / kamar rawat inap (opsional)
    val dischargeDate: Long? = null, // Tanggal keluar / pulang dari rawat inap
    val createdAt: Long = System.currentTimeMillis()
)
