package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ChildStudentDao
import com.example.data.dao.MedicalRecordDao
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [ChildStudent::class, MedicalRecord::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childStudentDao(): ChildStudentDao
    abstract fun medicalRecordDao(): MedicalRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "waliasuh_care.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.childStudentDao(), database.medicalRecordDao())
                }
            }
        }

        private suspend fun populateInitialData(
            studentDao: ChildStudentDao,
            recordDao: MedicalRecordDao
        ) {
            val students = listOf(
                ChildStudent(
                    id = 1,
                    name = "Rama Aditia",
                    nis = "SRT1-2024-01",
                    gender = "L",
                    age = 9,
                    dormitory = "Asrama Putra 1 (Wates)",
                    classGrade = "Kelas 3 SD",
                    allergies = "Tidak ada",
                    bloodType = "O",
                    emergencyContact = "0812-3456-7801 (Sari Devi)"
                ),
                ChildStudent(
                    id = 2,
                    name = "Siti Halimah",
                    nis = "SRT1-2023-14",
                    gender = "P",
                    age = 8,
                    dormitory = "Asrama Putri 2 (Wates)",
                    classGrade = "Kelas 2 SD",
                    allergies = "Alergi Dingin",
                    bloodType = "B",
                    emergencyContact = "0813-9876-5432 (Sari Devi)"
                ),
                ChildStudent(
                    id = 3,
                    name = "Anton Wiryo",
                    nis = "SRT1-2022-22",
                    gender = "L",
                    age = 10,
                    dormitory = "Asrama Putra 2 (Plosoklaten)",
                    classGrade = "Kelas 4 SD",
                    allergies = "Tidak ada",
                    bloodType = "A",
                    emergencyContact = "0821-4455-6677 (Sari Devi)"
                ),
                ChildStudent(
                    id = 4,
                    name = "Bella Astari",
                    nis = "SRT1-2023-33",
                    gender = "P",
                    age = 7,
                    dormitory = "Asrama Putri 1 (Plosoklaten)",
                    classGrade = "Kelas 1 SD",
                    allergies = "Alergi Seafood",
                    bloodType = "AB",
                    emergencyContact = "0822-1122-3344 (Sari Devi)"
                ),
                ChildStudent(
                    id = 5,
                    name = "Rian Pratama",
                    nis = "SRT1-2021-05",
                    gender = "L",
                    age = 11,
                    dormitory = "Asrama Behir 10",
                    classGrade = "Kelas 5A",
                    allergies = "Tidak ada",
                    bloodType = "O",
                    emergencyContact = "0812-3456-7801 (Sari Devi)"
                ),
                ChildStudent(
                    id = 6,
                    name = "Susi Astuti",
                    nis = "SRT1-2023-18",
                    gender = "P",
                    age = 9,
                    dormitory = "Asrama Putri Khadijah",
                    classGrade = "Kelas 3B",
                    allergies = "Alergi Amoxicillin",
                    bloodType = "A",
                    emergencyContact = "0813-8888-9999 (Sari Devi)"
                ),
                ChildStudent(
                    id = 7,
                    name = "Agus Wiryo",
                    nis = "SRT1-2022-09",
                    gender = "L",
                    age = 10,
                    dormitory = "Asrama Putra Diponegoro",
                    classGrade = "Kelas 4A",
                    allergies = "Tidak ada",
                    bloodType = "B",
                    emergencyContact = "0812-7777-6666 (Sari Devi)"
                )
            )

            studentDao.insertAll(students)

            val now = Calendar.getInstance()
            val todayMillis = now.timeInMillis

            now.add(Calendar.DAY_OF_YEAR, 3)
            val in3Days = now.timeInMillis

            now.add(Calendar.DAY_OF_YEAR, -5)
            val fiveDaysAgo = now.timeInMillis

            val initialRecords = listOf(
                // 1. Rencana Hari Ini (Sesuai Mockup Gambar 3)
                MedicalRecord(
                    studentId = 1,
                    studentName = "Rama Aditia",
                    studentDormitory = "Asrama Putra 1 (Wates)",
                    faskes = "Puskesmas Wates",
                    complaint = "Demam/Flu tinggi sejak semalam",
                    isDirectTreatment = false,
                    planDate = todayMillis,
                    departureTime = "08:30",
                    status = "PLANNED",
                    escortName = "Sari Devi (Wali Asuh)",
                    notes = "Sudah dikompres air hangat"
                ),
                MedicalRecord(
                    studentId = 2,
                    studentName = "Siti Halimah",
                    studentDormitory = "Asrama Putri 2 (Wates)",
                    faskes = "Puskesmas Wates",
                    complaint = "Sakit Perut melilit dan mual",
                    isDirectTreatment = false,
                    planDate = todayMillis,
                    departureTime = "09:00",
                    status = "DEPARTED", // Dianter
                    escortName = "Sari Devi",
                    notes = "Sedang diantar menggunakan kendaraan asrama"
                ),
                MedicalRecord(
                    studentId = 3,
                    studentName = "Anton Wiryo",
                    studentDormitory = "Asrama Putra 2 (Plosoklaten)",
                    faskes = "Puskesmas Plosoklaten",
                    complaint = "Batuk Pilek berdahak",
                    isDirectTreatment = false,
                    planDate = todayMillis,
                    departureTime = "09:30",
                    status = "PLANNED",
                    escortName = "Sari Devi",
                    notes = "Menunggu giliran berangkat kloter kedua"
                ),
                MedicalRecord(
                    studentId = 4,
                    studentName = "Bella Astari",
                    studentDormitory = "Asrama Putri 1 (Plosoklaten)",
                    faskes = "Puskesmas Plosoklaten",
                    complaint = "Pusing/Mual lemas",
                    isDirectTreatment = false,
                    planDate = todayMillis,
                    departureTime = "10:00",
                    status = "DEPARTED", // Siap Jalan
                    escortName = "Sari Devi",
                    notes = "Siap berangkat ke puskesmas"
                ),

                // 2. Rekam Medis Selesai Pasca Puskesmas (Sesuai Mockup Gambar 2)
                MedicalRecord(
                    studentId = 5,
                    studentName = "Rian Pratama",
                    studentDormitory = "Asrama Behir 10",
                    faskes = "Puskesmas Wates",
                    complaint = "Demam tinggi & lemas",
                    isDirectTreatment = false,
                    planDate = todayMillis,
                    treatmentDate = todayMillis,
                    departureTime = "09:30",
                    status = "COMPLETED",
                    diagnosis = "Observasi Febris hari ke-2",
                    actionsTaken = "Istirahat, Cairan infus, Pemantauan Lab darah",
                    prescription = "Paracetamol 500mg 3x1, Vitamin C 1x1, Banyak minum air putih",
                    needsControl = true,
                    controlDate = in3Days,
                    isControlCompleted = false,
                    escortName = "Sari Devi"
                ),
                MedicalRecord(
                    studentId = 6,
                    studentName = "Susi Astuti",
                    studentDormitory = "Asrama Putri Khadijah",
                    faskes = "Puskesmas Plosoklaten",
                    complaint = "Batuk kering, sesak nafas ringan & demam",
                    isDirectTreatment = true,
                    planDate = todayMillis,
                    treatmentDate = todayMillis,
                    departureTime = "09:30",
                    status = "COMPLETED",
                    diagnosis = "Infeksi Saluran Pernapasan Akut (ISPA)",
                    actionsTaken = "Pemeriksaan dada, Nebulisasi, Pemantauan saturasi O2",
                    prescription = "Ambroxol syrup 3x1 cth, Paracetamol syrup 3x1 cth",
                    needsControl = false,
                    isControlCompleted = true,
                    escortName = "Sari Devi"
                ),
                MedicalRecord(
                    studentId = 7,
                    studentName = "Agus Wiryo",
                    studentDormitory = "Asrama Putra Diponegoro",
                    faskes = "Puskesmas Wates",
                    complaint = "Diare cair 4x dan lemas",
                    isDirectTreatment = true,
                    planDate = todayMillis,
                    treatmentDate = todayMillis,
                    departureTime = "11:00",
                    status = "COMPLETED",
                    diagnosis = "Gastroenteritis Akut (GEA) Dehidrasi Ringan",
                    actionsTaken = "Rehidrasi oral, Pemberian tablet zinc, Edukasi kebersihan makanan",
                    prescription = "Oralit 4 bungkus, Zinc 20mg 1x1 selama 10 hari",
                    needsControl = true,
                    controlDate = in3Days,
                    isControlCompleted = false,
                    escortName = "Sari Devi"
                )
            )

            recordDao.insertAll(initialRecords)
        }
    }
}
