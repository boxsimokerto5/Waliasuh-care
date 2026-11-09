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
    version = 1,
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
                    "srt1_kediri_health.db"
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
                    name = "Ahmad Fauzi",
                    nis = "SRT1-2023-01",
                    gender = "L",
                    age = 13,
                    dormitory = "Asrama Putra Al-Fatih (Wates)",
                    classGrade = "Kelas 7 SMP",
                    allergies = "Alergi Dingin (Asma kambuhan)",
                    bloodType = "O",
                    emergencyContact = "0812-3456-7801 (Ust. Ilham)",
                    specialNotes = "Perlu perhatian bila cuaca dingin"
                ),
                ChildStudent(
                    name = "Muhammad Rizky",
                    nis = "SRT1-2022-14",
                    gender = "L",
                    age = 11,
                    dormitory = "Asrama Putra Diponegoro (Wates)",
                    classGrade = "Kelas 5 SD",
                    allergies = "Tidak ada",
                    bloodType = "B",
                    emergencyContact = "0813-9876-5432 (Ust. Wahid)",
                    specialNotes = "Aktif di tim sepak bola asrama"
                ),
                ChildStudent(
                    name = "Siti Nurhaliza",
                    nis = "SRT1-2023-28",
                    gender = "P",
                    age = 14,
                    dormitory = "Asrama Putri Khadijah (Plosoklaten)",
                    classGrade = "Kelas 8 SMP",
                    allergies = "Alergi Seafood & Amoxicillin",
                    bloodType = "A",
                    emergencyContact = "0821-4455-6677 (Usth. Maryam)",
                    specialNotes = "Perlu hati-hati pemilihan antibiotik"
                ),
                ChildStudent(
                    name = "Bayu Pratama",
                    nis = "SRT1-2024-05",
                    gender = "L",
                    age = 10,
                    dormitory = "Asrama Putra Al-Fatih (Wates)",
                    classGrade = "Kelas 4 SD",
                    allergies = "Tidak ada",
                    bloodType = "AB",
                    emergencyContact = "0812-3456-7801 (Ust. Ilham)",
                    specialNotes = "Sering telat makan, riwayat maag"
                ),
                ChildStudent(
                    name = "Aisyah Putri",
                    nis = "SRT1-2023-33",
                    gender = "P",
                    age = 12,
                    dormitory = "Asrama Putri Aisyah (Plosoklaten)",
                    classGrade = "Kelas 6 SD",
                    allergies = "Alergi Debu (Bersin/Rhinitis)",
                    bloodType = "O",
                    emergencyContact = "0822-1122-3344 (Usth. Anisa)",
                    specialNotes = "Rajin membersihkan kamar"
                ),
                ChildStudent(
                    name = "Dimas Wahyu Saputra",
                    nis = "SRT1-2021-09",
                    gender = "L",
                    age = 16,
                    dormitory = "Asrama Putra Sudirman (Wates)",
                    classGrade = "Kelas 10 SMK",
                    allergies = "Tidak ada",
                    bloodType = "B",
                    emergencyContact = "0812-3456-7802 (Ust. Syakir)",
                    specialNotes = "Jurusan Teknik Mesin"
                )
            )

            studentDao.insertAll(students)

            val now = Calendar.getInstance()
            val todayMillis = now.timeInMillis

            now.add(Calendar.DAY_OF_YEAR, 3)
            val controlIn3Days = now.timeInMillis

            now.add(Calendar.DAY_OF_YEAR, -5)
            val fiveDaysAgo = now.timeInMillis

            // Initial records demonstrating all flows
            val initialRecords = listOf(
                MedicalRecord(
                    studentId = 1,
                    studentName = "Ahmad Fauzi",
                    studentDormitory = "Asrama Putra Al-Fatih (Wates)",
                    faskes = "Puskesmas Wates",
                    complaint = "Demam panas tinggi sejak semalam, batuk kering dan pusing",
                    isDirectTreatment = false,
                    planDate = todayMillis,
                    status = "PLANNED",
                    escortName = "Ust. Ilham (Pengasuh)",
                    notes = "Sudah diberi paracetamol UKS namun panas naik turun"
                ),
                MedicalRecord(
                    studentId = 2,
                    studentName = "Muhammad Rizky",
                    studentDormitory = "Asrama Putra Diponegoro (Wates)",
                    faskes = "Puskesmas Wates",
                    complaint = "Sakit gigi geraham kanan bawah bengkak dan susah makan",
                    isDirectTreatment = false,
                    planDate = todayMillis,
                    status = "DEPARTED",
                    escortName = "Ust. Wahid",
                    notes = "Diberangkatkan jam 08:30 WIB menggunakan motor operasional asrama"
                ),
                MedicalRecord(
                    studentId = 3,
                    studentName = "Siti Nurhaliza",
                    studentDormitory = "Asrama Putri Khadijah (Plosoklaten)",
                    faskes = "Puskesmas Plosoklaten",
                    complaint = "Luka robek lecet di lutut kiri dan memar setelah terjatuh di tangga",
                    isDirectTreatment = true,
                    planDate = fiveDaysAgo,
                    status = "COMPLETED",
                    treatmentDate = fiveDaysAgo,
                    diagnosis = "Vulnus Laceratum regio genu sinistra + memar superficial",
                    actionsTaken = "Pembersihan luka dengan NaCl & Betadine, penjahitan 2 simpul, perban steril",
                    prescription = "Cefadroxil 500mg 2x1, Asam Mefenamat 500mg 3x1 (sesudah makan), Salep gentamisin",
                    needsControl = true,
                    controlDate = controlIn3Days,
                    isControlCompleted = false,
                    escortName = "Usth. Maryam",
                    notes = "Kontrol ulang untuk angkat jahitan dan cek infeksi luka"
                ),
                MedicalRecord(
                    studentId = 4,
                    studentName = "Bayu Pratama",
                    studentDormitory = "Asrama Putra Al-Fatih (Wates)",
                    faskes = "Puskesmas Wates",
                    complaint = "Nyeri ulu hati mual dan muntah 2 kali",
                    isDirectTreatment = true,
                    planDate = fiveDaysAgo,
                    status = "COMPLETED",
                    treatmentDate = fiveDaysAgo,
                    diagnosis = "Gastritis Akut / Dispepsia",
                    actionsTaken = "Pemeriksaan fisik perut, edukasi pola makan teratur",
                    prescription = "Antasida syrup 3x1 cth (sebelum makan), Omeprazole 20mg 1x1 pagi",
                    needsControl = false,
                    controlDate = null,
                    isControlCompleted = true,
                    escortName = "Ust. Ilham",
                    notes = "Kondisi sudah membaik, dianjurkan tidak makan pedas & asam di asrama"
                )
            )

            recordDao.insertAll(initialRecords)
        }
    }
}
