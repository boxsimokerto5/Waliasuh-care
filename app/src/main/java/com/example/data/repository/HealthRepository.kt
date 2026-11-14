package com.example.data.repository

import com.example.data.dao.ChildStudentDao
import com.example.data.dao.MedicalRecordDao
import com.example.data.dao.SchoolRegistrationDao
import com.example.data.dao.WaliAccountDao
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolRegistration
import com.example.data.dao.ShiftMessageDao
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

import com.example.data.dao.FastingRecordDao
import com.example.data.dao.FastingSessionDao
import com.example.data.dao.HaidRecordDao
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord

class HealthRepository(
    private val studentDao: ChildStudentDao,
    private val recordDao: MedicalRecordDao,
    private val waliAccountDao: WaliAccountDao,
    private val registrationDao: SchoolRegistrationDao,
    private val shiftMessageDao: ShiftMessageDao? = null,
    private val haidRecordDao: HaidRecordDao? = null,
    private val fastingSessionDao: FastingSessionDao? = null,
    private val fastingRecordDao: FastingRecordDao? = null
) {
    val allStudents: Flow<List<ChildStudent>> = studentDao.getAllStudents()
    val allRecords: Flow<List<MedicalRecord>> = recordDao.getAllRecords()
    val activePlans: Flow<List<MedicalRecord>> = recordDao.getActivePlans()
    val completedRecords: Flow<List<MedicalRecord>> = recordDao.getCompletedRecords()
    val pendingControlRecords: Flow<List<MedicalRecord>> = recordDao.getPendingControlRecords()
    val allWaliAccounts: Flow<List<WaliAccount>> = waliAccountDao.getAllAccounts()
    val allSchoolRegistrations: Flow<List<SchoolRegistration>> = registrationDao.getAllRegistrations()
    val allShiftMessages: Flow<List<ShiftMessage>> = shiftMessageDao?.getAllMessages() ?: emptyFlow()
    val allHaidRecords: Flow<List<HaidRecord>> = haidRecordDao?.getAllRecords() ?: emptyFlow()
    val activeHaidRecords: Flow<List<HaidRecord>> = haidRecordDao?.getActiveHaidRecords() ?: emptyFlow()
    val suciRecords: Flow<List<HaidRecord>> = haidRecordDao?.getSuciRecords() ?: emptyFlow()
    val allFastingSessions: Flow<List<FastingSession>> = fastingSessionDao?.getAllSessions() ?: emptyFlow()
    val allFastingRecords: Flow<List<FastingRecord>> = fastingRecordDao?.getAllRecords() ?: emptyFlow()

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

    suspend fun getAllWaliAccountsList(): List<WaliAccount> =
        waliAccountDao.getAllAccountsList()

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

    suspend fun deleteAllRecords() =
        recordDao.deleteAll()

    fun getRecordById(id: Long): Flow<MedicalRecord?> =
        recordDao.getRecordById(id)

    suspend fun updateStatus(id: Long, status: String) =
        recordDao.updateStatus(id, status)

    suspend fun updateDepartureWithFaskes(id: Long, status: String, faskes: String, escortName: String) =
        recordDao.updateDepartureWithFaskes(id, status, faskes, escortName)

    suspend fun updateControlCompleted(id: Long, isCompleted: Boolean) =
        recordDao.updateControlCompleted(id, isCompleted)

    suspend fun insertAllRecords(records: List<MedicalRecord>) =
        recordDao.insertAll(records)

    suspend fun insertAllStudents(students: List<ChildStudent>) =
        studentDao.insertAll(students)

    suspend fun deleteAllStudents() =
        studentDao.deleteAll()

    suspend fun deleteStudentsBySchool(schoolId: String) =
        studentDao.deleteBySchoolId(schoolId)

    suspend fun getAllStudentsList(): List<ChildStudent> =
        studentDao.getAllStudentsList()

    suspend fun getStudentsBySchoolList(schoolId: String): List<ChildStudent> =
        studentDao.getStudentsBySchoolList(schoolId)

    suspend fun deleteStudentsByIds(ids: List<Long>) {
        if (ids.isNotEmpty()) studentDao.deleteByIds(ids)
    }

    suspend fun getAllRecordsList(): List<MedicalRecord> =
        recordDao.getAllRecordsList()

    suspend fun getWaliAccountsBySchoolList(schoolId: String): List<WaliAccount> =
        waliAccountDao.getAccountsBySchoolList(schoolId)

    suspend fun deleteAllWaliAccounts() =
        waliAccountDao.deleteAll()

    suspend fun deleteWaliAccountsBySchool(schoolId: String) =
        waliAccountDao.deleteBySchoolId(schoolId)

    suspend fun insertAllWaliAccounts(accounts: List<WaliAccount>) =
        waliAccountDao.insertAll(accounts)

    // --- School Registration Operations ---
    suspend fun insertSchoolRegistration(registration: SchoolRegistration): Long =
        registrationDao.insert(registration)

    suspend fun insertAllSchoolRegistrations(registrations: List<SchoolRegistration>) =
        registrationDao.insertAll(registrations)

    suspend fun getAllSchoolRegistrationsList(): List<SchoolRegistration> =
        registrationDao.getAllRegistrationsList()

    suspend fun updateSchoolRegistration(registration: SchoolRegistration) =
        registrationDao.update(registration)

    suspend fun deleteSchoolRegistration(registration: SchoolRegistration) =
        registrationDao.delete(registration)

    suspend fun deleteSchoolRegistrationById(id: Long) =
        registrationDao.deleteById(id)

    suspend fun deleteSchoolRegistrationsByCode(code: String) {
        if (code.isNotBlank()) registrationDao.deleteBySchoolCode(code)
    }

    suspend fun getSchoolRegistrationById(id: Long): SchoolRegistration? =
        registrationDao.getById(id)

    suspend fun getSchoolByAdminUsername(username: String): SchoolRegistration? =
        registrationDao.getByAdminUsername(username)

    suspend fun getSchoolByCode(code: String): SchoolRegistration? =
        registrationDao.getBySchoolCode(code)

    // --- Shift Message Handover Operations ---
    suspend fun insertShiftMessage(message: ShiftMessage): Long =
        shiftMessageDao?.insert(message) ?: 0L

    suspend fun insertAllShiftMessages(messages: List<ShiftMessage>) {
        shiftMessageDao?.insertAll(messages)
    }

    suspend fun getShiftMessagesBySchoolList(schoolId: String): List<ShiftMessage> =
        shiftMessageDao?.getMessagesBySchoolList(schoolId) ?: emptyList()

    suspend fun getShiftMessageById(id: Long): ShiftMessage? =
        shiftMessageDao?.getById(id)

    suspend fun updateShiftMessage(message: ShiftMessage) {
        shiftMessageDao?.update(message)
    }

    suspend fun deleteShiftMessage(message: ShiftMessage) {
        shiftMessageDao?.delete(message)
    }

    suspend fun updateShiftMessageCompleted(id: Long, isCompleted: Boolean, completedBy: String, completedAt: Long) {
        shiftMessageDao?.updateCompleted(id, isCompleted, completedBy, completedAt)
    }

    suspend fun deleteShiftMessagesBySchool(schoolId: String) {
        shiftMessageDao?.deleteBySchoolId(schoolId)
    }

    // --- Haid Tracking Operations ---
    suspend fun insertHaidRecord(record: HaidRecord): Long =
        haidRecordDao?.insert(record) ?: 0L

    suspend fun insertAllHaidRecords(records: List<HaidRecord>) {
        haidRecordDao?.insertAll(records)
    }

    suspend fun getAllHaidRecordsList(): List<HaidRecord> =
        haidRecordDao?.getAllRecordsList() ?: emptyList()

    suspend fun getHaidRecordsBySchoolList(schoolId: String): List<HaidRecord> =
        haidRecordDao?.getRecordsBySchoolList(schoolId) ?: emptyList()

    suspend fun getHaidRecordById(id: Long): HaidRecord? =
        haidRecordDao?.getById(id)

    suspend fun updateHaidRecord(record: HaidRecord) {
        haidRecordDao?.update(record)
    }

    suspend fun deleteHaidRecord(record: HaidRecord) {
        haidRecordDao?.delete(record)
    }

    suspend fun deleteHaidRecordById(id: Long) {
        haidRecordDao?.deleteById(id)
    }

    suspend fun completeHaidRecord(id: Long, endDate: Long = System.currentTimeMillis(), status: String = "SUCI") {
        haidRecordDao?.completeHaid(id, endDate, status)
    }

    suspend fun getActiveHaidForStudent(studentId: Long): HaidRecord? =
        haidRecordDao?.getActiveRecordForStudent(studentId)

    suspend fun getActiveHaidForNik(nik: String): HaidRecord? =
        haidRecordDao?.getActiveRecordForNik(nik)

    suspend fun deleteHaidRecordsBySchool(schoolId: String) {
        haidRecordDao?.deleteBySchoolId(schoolId)
    }

    // --- Fasting Session & Record Operations ---
    suspend fun insertFastingSession(session: FastingSession): Long =
        fastingSessionDao?.insert(session) ?: 0L

    suspend fun insertAllFastingSessions(sessions: List<FastingSession>) {
        fastingSessionDao?.insertAll(sessions)
    }

    suspend fun getFastingSessionsBySchoolList(schoolId: String): List<FastingSession> =
        fastingSessionDao?.getSessionsBySchoolList(schoolId) ?: emptyList()

    suspend fun updateFastingSession(session: FastingSession) {
        fastingSessionDao?.update(session)
    }

    suspend fun deleteFastingSession(session: FastingSession) {
        fastingRecordDao?.deleteBySessionId(session.id)
        fastingSessionDao?.delete(session)
    }

    suspend fun deleteFastingSessionsBySchool(schoolId: String) {
        fastingSessionDao?.deleteBySchoolId(schoolId)
    }

    suspend fun insertFastingRecord(record: FastingRecord): Long =
        fastingRecordDao?.insert(record) ?: 0L

    suspend fun insertAllFastingRecords(records: List<FastingRecord>) {
        fastingRecordDao?.insertAll(records)
    }

    suspend fun getAllFastingRecordsList(): List<FastingRecord> =
        fastingRecordDao?.getAllRecordsList() ?: emptyList()

    suspend fun getFastingRecordsBySchoolList(schoolId: String): List<FastingRecord> =
        fastingRecordDao?.getRecordsBySchoolList(schoolId) ?: emptyList()

    suspend fun getFastingRecordsBySessionList(sessionId: Long): List<FastingRecord> =
        fastingRecordDao?.getRecordsBySessionList(sessionId) ?: emptyList()

    suspend fun getFastingRecordInSessionForStudent(sessionId: Long, studentId: Long): FastingRecord? =
        fastingRecordDao?.getRecordInSessionForStudent(sessionId, studentId)

    suspend fun updateFastingRecord(record: FastingRecord) {
        fastingRecordDao?.update(record)
    }

    suspend fun deleteFastingRecord(record: FastingRecord) {
        fastingRecordDao?.delete(record)
    }

    suspend fun deleteFastingRecordsBySchool(schoolId: String) {
        fastingRecordDao?.deleteBySchoolId(schoolId)
    }
}
