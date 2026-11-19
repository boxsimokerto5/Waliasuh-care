package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.UserRole

@Entity(tableName = "konseling_records")
data class KonselingRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long = 0L,
    val studentName: String,
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val ownerWaliUsername: String = "",
    val ownerWaliName: String = "Wali Asuh",
    val isConfidential: Boolean = true,
    val entryType: String = KonselingHelper.ENTRY_SESI, // SESI | PERKEMBANGAN
    val topic: String = KonselingHelper.TOPIC_ADAPTASI,
    val urgencyLevel: String = KonselingHelper.URGENCY_RINGAN, // RINGAN | PANTAU | PRIORITAS
    val moodLevel: String = KonselingHelper.MOOD_NETRAL, // BAIK | NETRAL | SEDIH | TERTEKAN
    val title: String = "",
    val summary: String = "",
    val intervention: String = "",
    val followUpPlan: String = "",
    val followUpDate: Long? = null,
    val status: String = KonselingHelper.STATUS_DIPANTAU, // DIPANTAU | SELESAI
    val sessionDate: Long = System.currentTimeMillis(),
    val photoPaths: String = "",
    val photoUrls: String = "",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "konseling_logs")
data class KonselingLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long = 0L,
    val studentId: Long = 0L,
    val studentName: String,
    val studentClass: String = "",
    val ownerWaliUsername: String = "",
    val ownerWaliName: String = "Wali Asuh",
    val isConfidential: Boolean = true,
    val actionType: String = KonselingHelper.LOG_INPUT_SESI,
    val summary: String = "",
    val actorName: String = "Wali Asuh",
    val actorUsername: String = "",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis()
)

data class KonselingStudentStats(
    val totalEntries: Int,
    val sessionsCount: Int,
    val growthCount: Int,
    val followUpCount: Int,
    val completedCount: Int,
    val highPriorityCount: Int,
    val latestRecord: KonselingRecord?
)

data class KonselingAggregateStats(
    val totalEntries: Int,
    val followUpCount: Int,
    val highPriorityCount: Int,
    val growthCount: Int,
    val counseledStudentsCount: Int
)

object KonselingHelper {
    const val ENTRY_SESI = "SESI"
    const val ENTRY_PERKEMBANGAN = "PERKEMBANGAN"

    const val TOPIC_ADAPTASI = "ADAPTASI"
    const val TOPIC_AKADEMIK = "AKADEMIK"
    const val TOPIC_PERTEMANAN = "PERTEMANAN"
    const val TOPIC_KELUARGA = "KELUARGA"
    const val TOPIC_EMOSI = "EMOSI"
    const val TOPIC_IBADAH = "IBADAH"
    const val TOPIC_APRESIASI = "APRESIASI"

    val ALL_TOPICS = listOf(
        TOPIC_ADAPTASI,
        TOPIC_AKADEMIK,
        TOPIC_PERTEMANAN,
        TOPIC_KELUARGA,
        TOPIC_EMOSI,
        TOPIC_IBADAH,
        TOPIC_APRESIASI
    )

    const val URGENCY_RINGAN = "RINGAN"
    const val URGENCY_PANTAU = "PANTAU"
    const val URGENCY_PRIORITAS = "PRIORITAS"

    const val MOOD_BAIK = "BAIK"
    const val MOOD_NETRAL = "NETRAL"
    const val MOOD_SEDIH = "SEDIH"
    const val MOOD_TERTEKAN = "TERTEKAN"

    const val STATUS_DIPANTAU = "DIPANTAU"
    const val STATUS_SELESAI = "SELESAI"

    const val LOG_INPUT_SESI = "INPUT_SESI"
    const val LOG_INPUT_GROWTH = "INPUT_GROWTH"
    const val LOG_EDIT = "EDIT"
    const val LOG_COMPLETE = "COMPLETE"
    const val LOG_REOPEN = "REOPEN"
    const val LOG_DELETE = "DELETE"

    fun isGrowthEntry(entryType: String): Boolean =
        entryType.trim().equals(ENTRY_PERKEMBANGAN, ignoreCase = true)

    fun isCompleted(status: String): Boolean =
        status.trim().equals(STATUS_SELESAI, ignoreCase = true)

    fun isHighPriority(urgencyLevel: String): Boolean =
        urgencyLevel.trim().equals(URGENCY_PRIORITAS, ignoreCase = true)

    fun matchesStudent(rec: KonselingRecord, student: ChildStudent): Boolean {
        if (student.id > 0L && rec.studentId == student.id) return true
        val cleanNis = student.nis.trim()
        if (cleanNis.isNotBlank() && rec.studentNis.isNotBlank() &&
            rec.studentNis.trim().equals(cleanNis, ignoreCase = true)
        ) {
            return true
        }
        return false
    }

    fun matchesStudentIdOrNis(
        rec: KonselingRecord,
        studentId: Long,
        studentNis: String = ""
    ): Boolean {
        if (studentId > 0L && rec.studentId == studentId) return true
        val cleanNis = studentNis.trim()
        if (cleanNis.isNotBlank() && rec.studentNis.isNotBlank() &&
            rec.studentNis.trim().equals(cleanNis, ignoreCase = true)
        ) {
            return true
        }
        return false
    }

    fun resolveHolderKey(rec: KonselingRecord): String {
        val cleanNis = rec.studentNis.trim().uppercase()
        if (cleanNis.isNotBlank()) return "nis:$cleanNis"
        if (rec.studentId > 0L) return "id:${rec.studentId}"
        return "name:${rec.studentName.trim().lowercase()}"
    }

    /**
     * Privasi Mutlak Konseling:
     * HANYA Wali Asuh pemilik catatan (matching username atau nama Wali) pada sekolah yang sama
     * yang dapat melihat catatan konseling miliknya.
     * Admin Sekolah, Guru Ngaji, Wali lain, Portal Ortu, maupun akun lintas Wali tidak diperbolehkan.
     */
    fun matchesOwnerOnly(
        ownerUsername: String,
        ownerName: String,
        currentUsername: String?,
        currentWaliName: String
    ): Boolean {
        val cleanCurUser = currentUsername?.trim().orEmpty()
        val cleanOwnerUser = ownerUsername.trim()
        if (cleanCurUser.isNotBlank() && cleanOwnerUser.isNotBlank()) {
            return cleanOwnerUser.equals(cleanCurUser, ignoreCase = true)
        }
        val cleanCurName = currentWaliName.trim()
        val cleanOwnerName = ownerName.trim()
        return cleanCurName.isNotBlank() && cleanOwnerName.isNotBlank() &&
            cleanOwnerName.equals(cleanCurName, ignoreCase = true)
    }

    fun filterPrivateRecords(
        allRecords: List<KonselingRecord>,
        schoolId: String,
        role: UserRole,
        currentUsername: String?,
        currentWaliName: String
    ): List<KonselingRecord> {
        if (role != UserRole.WALI) return emptyList()
        val cleanSchool = schoolId.trim()
        return allRecords.filter { rec ->
            rec.schoolId.equals(cleanSchool, ignoreCase = true) &&
                matchesOwnerOnly(
                    ownerUsername = rec.ownerWaliUsername,
                    ownerName = rec.ownerWaliName,
                    currentUsername = currentUsername,
                    currentWaliName = currentWaliName
                )
        }
    }

    fun filterPrivateLogs(
        allLogs: List<KonselingLog>,
        schoolId: String,
        role: UserRole,
        currentUsername: String?,
        currentWaliName: String
    ): List<KonselingLog> {
        if (role != UserRole.WALI) return emptyList()
        val cleanSchool = schoolId.trim()
        return allLogs.filter { log ->
            log.schoolId.equals(cleanSchool, ignoreCase = true) &&
                matchesOwnerOnly(
                    ownerUsername = log.ownerWaliUsername,
                    ownerName = log.ownerWaliName,
                    currentUsername = currentUsername,
                    currentWaliName = currentWaliName
                )
        }
    }

    fun computeStudentStats(
        records: List<KonselingRecord>,
        student: ChildStudent
    ): KonselingStudentStats {
        val matching = records
            .filter { matchesStudent(it, student) }
            .sortedWith(compareByDescending<KonselingRecord> { it.sessionDate }.thenByDescending { it.createdAt })
        val growthCount = matching.count { isGrowthEntry(it.entryType) }
        val sessionsCount = matching.size - growthCount
        val followUpCount = matching.count { !isCompleted(it.status) }
        val completedCount = matching.size - followUpCount
        val highPriorityCount = matching.count { !isCompleted(it.status) && isHighPriority(it.urgencyLevel) }
        return KonselingStudentStats(
            totalEntries = matching.size,
            sessionsCount = sessionsCount,
            growthCount = growthCount,
            followUpCount = followUpCount,
            completedCount = completedCount,
            highPriorityCount = highPriorityCount,
            latestRecord = matching.firstOrNull()
        )
    }

    fun computeAggregateStats(records: List<KonselingRecord>): KonselingAggregateStats {
        val followUp = records.count { !isCompleted(it.status) }
        val highPriority = records.count { !isCompleted(it.status) && isHighPriority(it.urgencyLevel) }
        val growth = records.count { isGrowthEntry(it.entryType) }
        val holders = records.map { resolveHolderKey(it) }.distinct().size
        return KonselingAggregateStats(
            totalEntries = records.size,
            followUpCount = followUp,
            highPriorityCount = highPriority,
            growthCount = growth,
            counseledStudentsCount = holders
        )
    }

    fun resolveStudentByIdOrFallback(
        studentId: Long,
        myFosterStudents: List<ChildStudent>,
        records: List<KonselingRecord>,
        logs: List<KonselingLog>
    ): ChildStudent? {
        myFosterStudents.firstOrNull { it.id == studentId }?.let { return it }
        val sampleRec = records.firstOrNull { it.studentId == studentId }
        if (sampleRec != null) {
            myFosterStudents.firstOrNull { matchesStudent(sampleRec, it) }?.let { return it }
            return ChildStudent(
                id = sampleRec.studentId,
                name = sampleRec.studentName,
                nis = sampleRec.studentNis,
                gender = "L",
                age = 12,
                dormitory = sampleRec.studentDorm,
                classGrade = sampleRec.studentClass,
                assignedWaliUsername = sampleRec.ownerWaliUsername,
                assignedWaliName = sampleRec.ownerWaliName,
                schoolId = sampleRec.schoolId
            )
        }
        val sampleLog = logs.firstOrNull { it.studentId == studentId } ?: return null
        return ChildStudent(
            id = sampleLog.studentId,
            name = sampleLog.studentName,
            nis = "",
            gender = "L",
            age = 12,
            dormitory = "",
            classGrade = sampleLog.studentClass,
            assignedWaliUsername = sampleLog.ownerWaliUsername,
            assignedWaliName = sampleLog.ownerWaliName,
            schoolId = sampleLog.schoolId
        )
    }

    fun formatTopicLabel(topic: String, isEn: Boolean): String {
        return when (topic.trim().uppercase()) {
            TOPIC_ADAPTASI -> if (isEn) "Dorm Adaptation" else "Adaptasi Asrama"
            TOPIC_AKADEMIK -> if (isEn) "Academic & Study" else "Akademik & Belajar"
            TOPIC_PERTEMANAN -> if (isEn) "Peers & Social" else "Pertemanan & Sosial"
            TOPIC_KELUARGA -> if (isEn) "Family & Homesick" else "Keluarga & Rindu Rumah"
            TOPIC_EMOSI -> if (isEn) "Emotion & Behavior" else "Emosi & Perilaku"
            TOPIC_IBADAH -> if (isEn) "Worship & Discipline" else "Ibadah & Disiplin"
            TOPIC_APRESIASI -> if (isEn) "Growth & Praise" else "Perkembangan & Apresiasi"
            else -> topic.ifBlank { if (isEn) "General" else "Umum" }
        }
    }

    fun formatUrgencyLabel(urgency: String, isEn: Boolean): String {
        return when (urgency.trim().uppercase()) {
            URGENCY_PRIORITAS -> if (isEn) "High Priority" else "Prioritas Tinggi"
            URGENCY_PANTAU -> if (isEn) "Monitor Closely" else "Perlu Pantauan"
            else -> if (isEn) "Light / Normal" else "Ringan / Biasa"
        }
    }

    fun formatMoodLabel(mood: String, isEn: Boolean): String {
        return when (mood.trim().uppercase()) {
            MOOD_BAIK -> if (isEn) "Cheerful / Calm" else "Ceria / Tenang"
            MOOD_SEDIH -> if (isEn) "Sad / Quiet" else "Murung / Sedih"
            MOOD_TERTEKAN -> if (isEn) "Stressed / Anxious" else "Tertekan / Cemas"
            else -> if (isEn) "Neutral" else "Wajar / Stabil"
        }
    }

    fun formatStatusLabel(status: String, isEn: Boolean): String {
        return if (isCompleted(status)) {
            if (isEn) "Resolved / Improved" else "Selesai / Membaik"
        } else {
            if (isEn) "In Follow-Up" else "Perlu Tindak Lanjut"
        }
    }

    fun actionLogLabel(actionType: String, isEn: Boolean): String {
        return when (actionType.trim().uppercase()) {
            LOG_INPUT_SESI -> if (isEn) "Session Added" else "Sesi Baru"
            LOG_INPUT_GROWTH -> if (isEn) "Growth Note" else "Catatan Perkembangan"
            LOG_EDIT -> if (isEn) "Edited" else "Diubah"
            LOG_COMPLETE -> if (isEn) "Resolved" else "Dituntaskan"
            LOG_REOPEN -> if (isEn) "Reopened" else "Dipantau Lagi"
            LOG_DELETE -> if (isEn) "Deleted" else "Dihapus"
            else -> actionType
        }
    }
}
