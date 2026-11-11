package com.example.data.repository

import com.example.data.dao.ChildStudentDao
import com.example.data.dao.MedicalRecordDao
import com.example.data.dao.WaliAccountDao
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.WaliAccount
import kotlinx.coroutines.flow.Flow

class HealthRepository(
    private val studentDao: ChildStudentDao,
    private val recordDao: MedicalRecordDao,
    private val waliAccountDao: WaliAccountDao
) {
    val allStudents: Flow<List<ChildStudent>> = studentDao.getAllStudents()
    val allRecords: Flow<List<MedicalRecord>> = recordDao.getAllRecords()
    val activePlans: Flow<List<MedicalRecord>> = recordDao.getActivePlans()
    val completedRecords: Flow<List<MedicalRecord>> = recordDao.getCompletedRecords()
    val pendingControlRecords: Flow<List<MedicalRecord>> = recordDao.getPendingControlRecords()
    val allWaliAccounts: Flow<List<WaliAccount>> = waliAccountDao.getAllAccounts()

    suspend fun insertWaliAccount(account: WaliAccount): Long =
        waliAccountDao.insert(account)

    suspend fun updateWaliAccount(account: WaliAccount) =
        waliAccountDao.update(account)

    suspend fun deleteWaliAccount(account: WaliAccount) =
        waliAccountDao.delete(account)

    suspend fun deleteWaliAccountById(id: Long) =
        waliAccountDao.deleteById(id)

    suspend fun getWaliAccountByUsername(username: String): WaliAccount? =
        waliAccountDao.getByUsername(username)

    fun getRecordsForStudent(studentId: Long): Flow<List<MedicalRecord>> =
        recordDao.getRecordsForStudent(studentId)

    suspend fun insertStudent(student: ChildStudent): Long =
        studentDao.insert(student)

    suspend fun updateStudent(student: ChildStudent) =
        studentDao.update(student)

    suspend fun deleteStudent(student: ChildStudent) =
        studentDao.delete(student)

    suspend fun insertRecord(record: MedicalRecord): Long =
        recordDao.insert(record)

    suspend fun updateRecord(record: MedicalRecord) =
        recordDao.update(record)

    suspend fun deleteRecord(record: MedicalRecord) =
        recordDao.delete(record)

    suspend fun updateStatus(id: Long, status: String) =
        recordDao.updateStatus(id, status)

    suspend fun updateControlCompleted(id: Long, isCompleted: Boolean) =
        recordDao.updateControlCompleted(id, isCompleted)

    suspend fun insertAllRecords(records: List<MedicalRecord>) =
        recordDao.insertAll(records)

    suspend fun insertAllStudents(students: List<ChildStudent>) =
        studentDao.insertAll(students)
}
