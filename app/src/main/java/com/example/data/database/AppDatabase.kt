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
import org.json.JSONArray

@Database(
    entities = [ChildStudent::class, MedicalRecord::class],
    version = 4,
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
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "waliasuh_care.db"
                )
                    .addCallback(DatabaseCallback(appContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun loadStudentsFromAssets(context: Context): List<ChildStudent> {
            return try {
                val jsonString = context.assets.open("students_data.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val list = ArrayList<ChildStudent>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        ChildStudent(
                            id = obj.optLong("id", (i + 1).toLong()),
                            name = obj.optString("name", ""),
                            nis = obj.optString("nis", ""),
                            gender = obj.optString("gender", "L"),
                            age = obj.optInt("age", 12),
                            dormitory = obj.optString("dormitory", "Asrama"),
                            classGrade = obj.optString("classGrade", "-"),
                            allergies = obj.optString("allergies", "Tidak ada"),
                            bloodType = obj.optString("bloodType", "-"),
                            emergencyContact = obj.optString("emergencyContact", "Pengasuh Asrama"),
                            specialNotes = obj.optString("specialNotes", ""),
                            nik = obj.optString("nik", ""),
                            noKk = obj.optString("noKk", ""),
                            birthPlace = obj.optString("birthPlace", ""),
                            birthDate = obj.optString("birthDate", ""),
                            motherName = obj.optString("motherName", ""),
                            address = obj.optString("address", ""),
                            roomNumber = obj.optString("roomNumber", ""),
                            jenjang = obj.optString("jenjang", "")
                        )
                    )
                }
                list
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val students = loadStudentsFromAssets(context)
                    if (students.isNotEmpty()) {
                        database.childStudentDao().deleteAll()
                        database.medicalRecordDao().deleteAll()
                        database.childStudentDao().insertAll(students)
                    }
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val count = database.childStudentDao().count()
                    // Jika data masih data dummy lama (< 300 siswa), bersihkan dan ganti dengan 342 siswa baru
                    if (count < 300) {
                        database.childStudentDao().deleteAll()
                        database.medicalRecordDao().deleteAll()
                        val students = loadStudentsFromAssets(context)
                        if (students.isNotEmpty()) {
                            database.childStudentDao().insertAll(students)
                        }
                    }
                }
            }
        }
    }
}
