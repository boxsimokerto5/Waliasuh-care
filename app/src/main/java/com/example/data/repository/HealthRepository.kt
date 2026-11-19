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

import com.example.data.dao.DeletionLogDao
import com.example.data.dao.FastingRecordDao
import com.example.data.dao.FastingSessionDao
import com.example.data.dao.HaidRecordDao
import com.example.data.dao.MengajiRecordDao
import com.example.data.dao.VisitRecordDao
import com.example.data.dao.VisitSessionDao
import com.example.data.dao.ParentAccessLinkDao
import com.example.data.dao.ParentExpectationDao
import com.example.data.dao.ParentMessageDao
import com.example.data.dao.KonselingLogDao
import com.example.data.dao.KonselingRecordDao
import com.example.data.dao.TabunganLogDao
import com.example.data.dao.TabunganTransactionDao
import com.example.data.dao.InfoPentingCommentDao
import com.example.data.dao.InfoPentingPostDao
import com.example.data.dao.InfoPentingReactionDao
import com.example.data.dao.SholatRecordDao
import com.example.data.dao.WaliGalleryLocalSaveDao
import com.example.data.dao.WaliGalleryPostDao
import com.example.data.dao.WaliScheduleEntryDao
import com.example.data.dao.WaliShiftCodeDao
import com.example.data.model.DeletionLog
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.InfoPentingComment
import com.example.data.model.InfoPentingPost
import com.example.data.model.InfoPentingReaction
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import com.example.data.model.SholatRecord
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.WaliGalleryLocalSave
import com.example.data.model.WaliGalleryPost
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliShiftCode

class HealthRepository(
    private val studentDao: ChildStudentDao,
    private val recordDao: MedicalRecordDao,
    private val waliAccountDao: WaliAccountDao,
    private val registrationDao: SchoolRegistrationDao,
    private val shiftMessageDao: ShiftMessageDao? = null,
    private val haidRecordDao: HaidRecordDao? = null,
    private val fastingSessionDao: FastingSessionDao? = null,
    private val fastingRecordDao: FastingRecordDao? = null,
    private val mengajiRecordDao: MengajiRecordDao? = null,
    private val visitSessionDao: VisitSessionDao? = null,
    private val visitRecordDao: VisitRecordDao? = null,
    private val parentAccessLinkDao: ParentAccessLinkDao? = null,
    private val parentMessageDao: ParentMessageDao? = null,
    private val deletionLogDao: DeletionLogDao? = null,
    private val tabunganTransactionDao: TabunganTransactionDao? = null,
    private val tabunganLogDao: TabunganLogDao? = null,
    private val konselingRecordDao: KonselingRecordDao? = null,
    private val konselingLogDao: KonselingLogDao? = null,
    private val waliShiftCodeDao: WaliShiftCodeDao? = null,
    private val waliScheduleEntryDao: WaliScheduleEntryDao? = null,
    private val infoPentingPostDao: InfoPentingPostDao? = null,
    private val infoPentingReactionDao: InfoPentingReactionDao? = null,
    private val infoPentingCommentDao: InfoPentingCommentDao? = null,
    private val parentExpectationDao: ParentExpectationDao? = null,
    private val sholatRecordDao: SholatRecordDao? = null,
    private val waliGalleryPostDao: WaliGalleryPostDao? = null,
    private val waliGalleryLocalSaveDao: WaliGalleryLocalSaveDao? = null
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
    val allInfoPentingPosts: Flow<List<InfoPentingPost>> = infoPentingPostDao?.getAllActivePosts() ?: emptyFlow()
    val allInfoPentingReactions: Flow<List<InfoPentingReaction>> = infoPentingReactionDao?.getAllReactions() ?: emptyFlow()
    val allInfoPentingComments: Flow<List<InfoPentingComment>> = infoPentingCommentDao?.getAllActiveComments() ?: emptyFlow()
    val allFastingRecords: Flow<List<FastingRecord>> = fastingRecordDao?.getAllRecords() ?: emptyFlow()
    val allMengajiRecords: Flow<List<MengajiRecord>> = mengajiRecordDao?.getAllRecords() ?: emptyFlow()
    val allVisitSessions: Flow<List<VisitSession>> = visitSessionDao?.getAllSessions() ?: emptyFlow()
    val allVisitRecords: Flow<List<VisitRecord>> = visitRecordDao?.getAllRecords() ?: emptyFlow()
    val allParentAccessLinks: Flow<List<ParentAccessLink>> = parentAccessLinkDao?.getAllLinks() ?: emptyFlow()
    val allParentMessages: Flow<List<ParentMessage>> = parentMessageDao?.getAllMessages() ?: emptyFlow()
    val allParentExpectations: Flow<List<ParentExpectation>> = parentExpectationDao?.getAllExpectations() ?: emptyFlow()
    val allDeletionLogs: Flow<List<DeletionLog>> = deletionLogDao?.getAllLogs() ?: emptyFlow()
    val allTabunganTransactions: Flow<List<TabunganTransaction>> =
        tabunganTransactionDao?.getAllTransactions() ?: emptyFlow()
    val allTabunganLogs: Flow<List<TabunganLog>> =
        tabunganLogDao?.getAllLogs() ?: emptyFlow()
    val allKonselingRecords: Flow<List<KonselingRecord>> =
        konselingRecordDao?.getAllRecords() ?: emptyFlow()
    val allKonselingLogs: Flow<List<KonselingLog>> =
        konselingLogDao?.getAllLogs() ?: emptyFlow()
    val allWaliShiftCodes: Flow<List<WaliShiftCode>> =
        waliShiftCodeDao?.getAllShiftCodes() ?: emptyFlow()
    val allWaliScheduleEntries: Flow<List<WaliScheduleEntry>> =
        waliScheduleEntryDao?.getAllScheduleEntries() ?: emptyFlow()
    val allSholatRecords: Flow<List<SholatRecord>> =
        sholatRecordDao?.getAllRecords() ?: emptyFlow()
    val allWaliGalleryPosts: Flow<List<WaliGalleryPost>> =
        waliGalleryPostDao?.getAllActivePosts() ?: emptyFlow()
    val allWaliGalleryLocalSaves: Flow<List<WaliGalleryLocalSave>> =
        waliGalleryLocalSaveDao?.getAllLocalSaves() ?: emptyFlow()

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

    suspend fun getStudentById(id: Long): ChildStudent? =
        studentDao.getById(id)

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

    suspend fun checkInHaidRecord(
        id: Long,
        checkedInAt: Long = System.currentTimeMillis(),
        checkedInBy: String = "Wali Asuh"
    ) {
        haidRecordDao?.checkInHaid(id, checkedInAt, checkedInBy)
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

    // --- Mengaji Berkelanjutan Operations ---
    suspend fun insertMengajiRecord(record: MengajiRecord): Long =
        mengajiRecordDao?.insert(record) ?: 0L

    suspend fun insertAllMengajiRecords(records: List<MengajiRecord>) {
        mengajiRecordDao?.insertAll(records)
    }

    suspend fun getAllMengajiRecordsList(): List<MengajiRecord> =
        mengajiRecordDao?.getAllRecordsList() ?: emptyList()

    suspend fun getMengajiRecordsBySchoolList(schoolId: String): List<MengajiRecord> =
        mengajiRecordDao?.getRecordsBySchoolList(schoolId) ?: emptyList()

    suspend fun getMengajiRecordsForStudentList(studentId: Long): List<MengajiRecord> =
        mengajiRecordDao?.getRecordsForStudentList(studentId) ?: emptyList()

    suspend fun updateMengajiRecord(record: MengajiRecord) {
        mengajiRecordDao?.update(record)
    }

    suspend fun deleteMengajiRecord(record: MengajiRecord) {
        mengajiRecordDao?.delete(record)
    }

    suspend fun deleteMengajiRecordById(id: Long) {
        mengajiRecordDao?.deleteById(id)
    }

    suspend fun deleteMengajiRecordsBySchool(schoolId: String) {
        mengajiRecordDao?.deleteBySchoolId(schoolId)
    }

    // --- Parent Visit (Kunjungan) Session & Attendance Record Operations ---
    suspend fun insertVisitSession(session: VisitSession): Long =
        visitSessionDao?.insert(session) ?: 0L

    suspend fun insertAllVisitSessions(sessions: List<VisitSession>) {
        visitSessionDao?.insertAll(sessions)
    }

    suspend fun getVisitSessionsBySchoolList(schoolId: String): List<VisitSession> =
        visitSessionDao?.getSessionsBySchoolList(schoolId) ?: emptyList()

    suspend fun updateVisitSession(session: VisitSession) {
        visitSessionDao?.update(session)
    }

    suspend fun deleteVisitSession(session: VisitSession) {
        visitRecordDao?.deleteBySessionId(session.id)
        visitSessionDao?.delete(session)
    }

    suspend fun deleteVisitSessionsBySchool(schoolId: String) {
        visitSessionDao?.deleteBySchoolId(schoolId)
    }

    suspend fun insertVisitRecord(record: VisitRecord): Long =
        visitRecordDao?.insert(record) ?: 0L

    suspend fun insertAllVisitRecords(records: List<VisitRecord>) {
        visitRecordDao?.insertAll(records)
    }

    suspend fun getAllVisitRecordsList(): List<VisitRecord> =
        visitRecordDao?.getAllRecordsList() ?: emptyList()

    suspend fun getVisitRecordsBySchoolList(schoolId: String): List<VisitRecord> =
        visitRecordDao?.getRecordsBySchoolList(schoolId) ?: emptyList()

    suspend fun getVisitRecordsBySessionList(sessionId: Long): List<VisitRecord> =
        visitRecordDao?.getRecordsBySessionList(sessionId) ?: emptyList()

    suspend fun updateVisitRecord(record: VisitRecord) {
        visitRecordDao?.update(record)
    }

    suspend fun deleteVisitRecord(record: VisitRecord) {
        visitRecordDao?.delete(record)
    }

    suspend fun deleteVisitRecordsBySchool(schoolId: String) {
        visitRecordDao?.deleteBySchoolId(schoolId)
    }

    // --- Parent Access Link & Two-Way Messages (Hubungan Orang Tua) ---
    suspend fun insertParentAccessLink(link: ParentAccessLink): Long =
        parentAccessLinkDao?.insert(link) ?: 0L

    suspend fun insertAllParentAccessLinks(links: List<ParentAccessLink>) {
        parentAccessLinkDao?.insertAll(links)
    }

    suspend fun updateParentAccessLink(link: ParentAccessLink) {
        parentAccessLinkDao?.update(link)
    }

    suspend fun deleteParentAccessLink(link: ParentAccessLink) {
        parentAccessLinkDao?.delete(link)
    }

    suspend fun getAllParentAccessLinksList(): List<ParentAccessLink> =
        parentAccessLinkDao?.getAllLinksList() ?: emptyList()

    suspend fun getParentAccessLinksBySchoolList(schoolId: String): List<ParentAccessLink> =
        parentAccessLinkDao?.getLinksBySchoolList(schoolId) ?: emptyList()

    suspend fun getParentAccessLinkByStudentId(studentId: Long, schoolId: String): ParentAccessLink? =
        parentAccessLinkDao?.getByStudentId(studentId, schoolId)

    suspend fun getParentAccessLinkByUsername(username: String): ParentAccessLink? =
        parentAccessLinkDao?.getByUsername(username)

    suspend fun deleteParentAccessLinksBySchool(schoolId: String) {
        parentAccessLinkDao?.deleteBySchoolId(schoolId)
    }

    suspend fun insertParentMessage(message: ParentMessage): Long =
        parentMessageDao?.insert(message) ?: 0L

    suspend fun insertAllParentMessages(messages: List<ParentMessage>) {
        parentMessageDao?.insertAll(messages)
    }

    suspend fun getParentMessagesBySchoolList(schoolId: String): List<ParentMessage> =
        parentMessageDao?.getMessagesBySchoolList(schoolId) ?: emptyList()

    suspend fun getParentMessagesForStudentList(studentId: Long, schoolId: String): List<ParentMessage> =
        parentMessageDao?.getMessagesForStudentList(studentId, schoolId) ?: emptyList()

    suspend fun markParentMessagesReadByWali(studentId: Long, schoolId: String) {
        parentMessageDao?.markReadByWali(studentId, schoolId)
    }

    suspend fun markParentMessagesReadByParent(studentId: Long, schoolId: String) {
        parentMessageDao?.markReadByParent(studentId, schoolId)
    }

    suspend fun deleteParentMessagesBySchool(schoolId: String) {
        parentMessageDao?.deleteBySchoolId(schoolId)
    }

    suspend fun deleteParentMessage(message: ParentMessage) {
        parentMessageDao?.delete(message)
    }

    suspend fun deleteParentMessageById(id: Long) {
        parentMessageDao?.deleteById(id)
    }

    // --- Parent Character Expectations (Harapan Karakter Orang Tua) ---
    suspend fun insertParentExpectation(expectation: ParentExpectation): Long =
        parentExpectationDao?.insert(expectation) ?: 0L

    suspend fun insertAllParentExpectations(expectations: List<ParentExpectation>) {
        parentExpectationDao?.insertAll(expectations)
    }

    suspend fun updateParentExpectation(expectation: ParentExpectation) {
        parentExpectationDao?.update(expectation)
    }

    suspend fun deleteParentExpectation(expectation: ParentExpectation) {
        parentExpectationDao?.delete(expectation)
    }

    suspend fun getParentExpectationsBySchoolList(schoolId: String): List<ParentExpectation> =
        parentExpectationDao?.getExpectationsBySchoolList(schoolId) ?: emptyList()

    suspend fun getParentExpectationsForStudentList(studentId: Long, schoolId: String): List<ParentExpectation> =
        parentExpectationDao?.getExpectationsForStudentList(studentId, schoolId) ?: emptyList()

    suspend fun markParentExpectationsReadByWali(studentId: Long, schoolId: String, updatedAt: Long) {
        parentExpectationDao?.markReadByWali(studentId, schoolId, updatedAt)
    }

    suspend fun deleteParentExpectationsBySchool(schoolId: String) {
        parentExpectationDao?.deleteBySchoolId(schoolId)
    }

    // --- Deletion Logs (Audit & Notifikasi Hapus Riwayat Mengaji/Puasa/Berobat) ---
    suspend fun insertDeletionLog(log: DeletionLog): Long =
        deletionLogDao?.insert(log) ?: 0L

    suspend fun insertAllDeletionLogs(logs: List<DeletionLog>) {
        deletionLogDao?.insertAll(logs)
    }

    suspend fun getDeletionLogsBySchoolList(schoolId: String): List<DeletionLog> =
        deletionLogDao?.getLogsBySchoolList(schoolId) ?: emptyList()

    suspend fun deleteDeletionLogsBySchool(schoolId: String) {
        deletionLogDao?.deleteBySchoolId(schoolId)
    }

    suspend fun deleteOldDeletionLogs(cutoffTimestamp: Long) {
        deletionLogDao?.deleteOlderThan(cutoffTimestamp)
    }

    // --- Tabungan (Penitipan Uang Pribadi Wali Asuh) ---
    suspend fun insertTabunganTransaction(transaction: TabunganTransaction): Long =
        tabunganTransactionDao?.insert(transaction) ?: 0L

    suspend fun insertAllTabunganTransactions(transactions: List<TabunganTransaction>) {
        tabunganTransactionDao?.insertAll(transactions)
    }

    suspend fun updateTabunganTransaction(transaction: TabunganTransaction) {
        tabunganTransactionDao?.update(transaction)
    }

    suspend fun deleteTabunganTransaction(transaction: TabunganTransaction) {
        tabunganTransactionDao?.delete(transaction)
    }

    suspend fun deleteTabunganTransactionById(id: Long) {
        tabunganTransactionDao?.deleteById(id)
    }

    suspend fun getTabunganTransactionsBySchoolList(schoolId: String): List<TabunganTransaction> =
        tabunganTransactionDao?.getTransactionsBySchoolList(schoolId) ?: emptyList()

    suspend fun getTabunganTransactionsForStudentList(studentId: Long, schoolId: String): List<TabunganTransaction> =
        tabunganTransactionDao?.getTransactionsForStudentList(studentId, schoolId) ?: emptyList()

    suspend fun deleteTabunganTransactionsBySchool(schoolId: String) {
        tabunganTransactionDao?.deleteBySchoolId(schoolId)
    }

    suspend fun insertTabunganLog(log: TabunganLog): Long =
        tabunganLogDao?.insert(log) ?: 0L

    suspend fun insertAllTabunganLogs(logs: List<TabunganLog>) {
        tabunganLogDao?.insertAll(logs)
    }

    suspend fun getTabunganLogsBySchoolList(schoolId: String): List<TabunganLog> =
        tabunganLogDao?.getLogsBySchoolList(schoolId) ?: emptyList()

    suspend fun deleteTabunganLogsBySchool(schoolId: String) {
        tabunganLogDao?.deleteBySchoolId(schoolId)
    }

    // --- Konseling Rahasia (Hanya Wali Asuh Bersangkutan) ---
    suspend fun insertKonselingRecord(record: KonselingRecord): Long =
        konselingRecordDao?.insert(record) ?: 0L

    suspend fun insertAllKonselingRecords(records: List<KonselingRecord>) {
        konselingRecordDao?.insertAll(records)
    }

    suspend fun updateKonselingRecord(record: KonselingRecord) {
        konselingRecordDao?.update(record)
    }

    suspend fun deleteKonselingRecord(record: KonselingRecord) {
        konselingRecordDao?.delete(record)
    }

    suspend fun deleteKonselingRecordById(id: Long) {
        konselingRecordDao?.deleteById(id)
    }

    suspend fun getKonselingRecordsBySchoolList(schoolId: String): List<KonselingRecord> =
        konselingRecordDao?.getRecordsBySchoolList(schoolId) ?: emptyList()

    suspend fun getKonselingRecordsForStudentList(studentId: Long, schoolId: String): List<KonselingRecord> =
        konselingRecordDao?.getRecordsForStudentList(studentId, schoolId) ?: emptyList()

    suspend fun deleteKonselingRecordsBySchool(schoolId: String) {
        konselingRecordDao?.deleteBySchoolId(schoolId)
    }

    suspend fun insertKonselingLog(log: KonselingLog): Long =
        konselingLogDao?.insert(log) ?: 0L

    suspend fun insertAllKonselingLogs(logs: List<KonselingLog>) {
        konselingLogDao?.insertAll(logs)
    }

    suspend fun getKonselingLogsBySchoolList(schoolId: String): List<KonselingLog> =
        konselingLogDao?.getLogsBySchoolList(schoolId) ?: emptyList()

    suspend fun deleteKonselingLogsBySchool(schoolId: String) {
        konselingLogDao?.deleteBySchoolId(schoolId)
    }

    // --- Jadwal Kerja Wali Asuh (Shift Codes & Schedule Entries) ---
    suspend fun insertWaliShiftCode(shiftCode: WaliShiftCode): Long =
        waliShiftCodeDao?.insert(shiftCode) ?: 0L

    suspend fun insertAllWaliShiftCodes(shiftCodes: List<WaliShiftCode>) {
        waliShiftCodeDao?.insertAll(shiftCodes)
    }

    suspend fun updateWaliShiftCode(shiftCode: WaliShiftCode) {
        waliShiftCodeDao?.update(shiftCode)
    }

    suspend fun deleteWaliShiftCode(shiftCode: WaliShiftCode) {
        waliShiftCodeDao?.delete(shiftCode)
    }

    suspend fun deleteWaliShiftCodeById(id: Long) {
        waliShiftCodeDao?.deleteById(id)
    }

    suspend fun getWaliShiftCodesBySchoolList(schoolId: String): List<WaliShiftCode> =
        waliShiftCodeDao?.getShiftCodesBySchoolList(schoolId) ?: emptyList()

    suspend fun deleteWaliShiftCodesBySchool(schoolId: String) {
        waliShiftCodeDao?.deleteBySchoolId(schoolId)
    }

    suspend fun insertWaliScheduleEntry(entry: WaliScheduleEntry): Long =
        waliScheduleEntryDao?.insert(entry) ?: 0L

    suspend fun insertAllWaliScheduleEntries(entries: List<WaliScheduleEntry>) {
        waliScheduleEntryDao?.insertAll(entries)
    }

    suspend fun updateWaliScheduleEntry(entry: WaliScheduleEntry) {
        waliScheduleEntryDao?.update(entry)
    }

    suspend fun deleteWaliScheduleEntry(entry: WaliScheduleEntry) {
        waliScheduleEntryDao?.delete(entry)
    }

    suspend fun deleteWaliScheduleEntryById(id: Long) {
        waliScheduleEntryDao?.deleteById(id)
    }

    suspend fun getWaliScheduleEntriesBySchoolList(schoolId: String): List<WaliScheduleEntry> =
        waliScheduleEntryDao?.getEntriesBySchoolList(schoolId) ?: emptyList()

    suspend fun getWaliScheduleEntriesBySchoolAndMonthList(
        schoolId: String,
        monthKey: String
    ): List<WaliScheduleEntry> =
        waliScheduleEntryDao?.getEntriesBySchoolAndMonthList(schoolId, monthKey) ?: emptyList()

    suspend fun deleteWaliScheduleEntriesBySchool(schoolId: String) {
        waliScheduleEntryDao?.deleteBySchoolId(schoolId)
    }

    suspend fun deleteWaliScheduleEntriesBySchoolAndMonth(schoolId: String, monthKey: String) {
        waliScheduleEntryDao?.deleteBySchoolAndMonth(schoolId, monthKey)
    }

    // --- Channel Info-Info Penting (Global Seluruh Sekolah) ---
    suspend fun insertInfoPentingPost(post: InfoPentingPost): Long =
        infoPentingPostDao?.insert(post) ?: 0L

    suspend fun insertAllInfoPentingPosts(posts: List<InfoPentingPost>) {
        infoPentingPostDao?.insertAll(posts)
    }

    suspend fun updateInfoPentingPost(post: InfoPentingPost) {
        infoPentingPostDao?.update(post)
    }

    suspend fun getInfoPentingPostById(id: Long): InfoPentingPost? =
        infoPentingPostDao?.getPostById(id)

    suspend fun getAllInfoPentingPostsIncludingDeletedList(): List<InfoPentingPost> =
        infoPentingPostDao?.getAllPostsIncludingDeletedList() ?: emptyList()

    suspend fun insertInfoPentingReaction(reaction: InfoPentingReaction): Long =
        infoPentingReactionDao?.insert(reaction) ?: 0L

    suspend fun insertAllInfoPentingReactions(reactions: List<InfoPentingReaction>) {
        infoPentingReactionDao?.insertAll(reactions)
    }

    suspend fun getAllInfoPentingReactionsList(): List<InfoPentingReaction> =
        infoPentingReactionDao?.getAllReactionsList() ?: emptyList()

    suspend fun getInfoPentingReactionByPostAndUser(postId: Long, userKey: String): InfoPentingReaction? =
        infoPentingReactionDao?.getByPostAndUser(postId, userKey)

    suspend fun insertInfoPentingComment(comment: InfoPentingComment): Long =
        infoPentingCommentDao?.insert(comment) ?: 0L

    suspend fun insertAllInfoPentingComments(comments: List<InfoPentingComment>) {
        infoPentingCommentDao?.insertAll(comments)
    }

    suspend fun updateInfoPentingComment(comment: InfoPentingComment) {
        infoPentingCommentDao?.update(comment)
    }

    suspend fun getInfoPentingCommentById(id: Long): InfoPentingComment? =
        infoPentingCommentDao?.getCommentById(id)

    suspend fun getAllInfoPentingCommentsIncludingDeletedList(): List<InfoPentingComment> =
        infoPentingCommentDao?.getAllCommentsIncludingDeletedList() ?: emptyList()

    // --- Absen Sholat 5 Waktu ---
    suspend fun insertSholatRecord(record: SholatRecord): Long =
        sholatRecordDao?.insert(record) ?: 0L

    suspend fun insertAllSholatRecords(records: List<SholatRecord>) {
        sholatRecordDao?.insertAll(records)
    }

    suspend fun getAllSholatRecordsList(): List<SholatRecord> =
        sholatRecordDao?.getAllRecordsList() ?: emptyList()

    suspend fun getSholatRecordsBySchoolList(schoolId: String): List<SholatRecord> =
        sholatRecordDao?.getRecordsBySchoolList(schoolId) ?: emptyList()

    suspend fun getSholatRecordById(id: Long): SholatRecord? =
        sholatRecordDao?.getById(id)

    suspend fun getSholatRecordByStudentDateWaktu(
        studentId: Long,
        dateKey: String,
        waktu: String
    ): SholatRecord? = sholatRecordDao?.getByStudentDateWaktu(studentId, dateKey, waktu)

    suspend fun updateSholatRecord(record: SholatRecord) {
        sholatRecordDao?.update(record)
    }

    suspend fun deleteSholatRecord(record: SholatRecord) {
        sholatRecordDao?.delete(record)
    }

    suspend fun deleteSholatRecordById(id: Long) {
        sholatRecordDao?.deleteById(id)
    }

    suspend fun deleteSholatRecordsBySchool(schoolId: String) {
        sholatRecordDao?.deleteBySchoolId(schoolId)
    }

    // --- Galeri Wali Asuh (Relay Sementara + Simpan ke Galeri HP Orang Tua) ---
    suspend fun insertWaliGalleryPost(post: WaliGalleryPost): Long =
        waliGalleryPostDao?.insert(post) ?: 0L

    suspend fun insertAllWaliGalleryPosts(posts: List<WaliGalleryPost>) {
        waliGalleryPostDao?.insertAll(posts)
    }

    suspend fun updateWaliGalleryPost(post: WaliGalleryPost) {
        waliGalleryPostDao?.update(post)
    }

    suspend fun getWaliGalleryPostById(id: Long): WaliGalleryPost? =
        waliGalleryPostDao?.getById(id)

    suspend fun getAllWaliGalleryPostsIncludingDeletedList(): List<WaliGalleryPost> =
        waliGalleryPostDao?.getAllPostsIncludingDeletedList() ?: emptyList()

    suspend fun getActiveWaliGalleryPostsBySchoolList(schoolId: String): List<WaliGalleryPost> =
        waliGalleryPostDao?.getActivePostsBySchoolList(schoolId) ?: emptyList()

    suspend fun deleteWaliGalleryPostById(id: Long) {
        waliGalleryPostDao?.deleteById(id)
    }

    suspend fun deleteWaliGalleryPostsBySchool(schoolId: String) {
        waliGalleryPostDao?.deleteBySchoolId(schoolId)
    }

    suspend fun upsertWaliGalleryLocalSave(save: WaliGalleryLocalSave) {
        waliGalleryLocalSaveDao?.upsert(save)
    }

    suspend fun getWaliGalleryLocalSave(postId: Long): WaliGalleryLocalSave? =
        waliGalleryLocalSaveDao?.getByPostId(postId)

    suspend fun deleteWaliGalleryLocalSave(postId: Long) {
        waliGalleryLocalSaveDao?.deleteByPostId(postId)
    }
}
