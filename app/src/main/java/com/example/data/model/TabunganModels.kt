package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.UserRole
import java.text.NumberFormat
import java.util.Locale

@Entity(tableName = "tabungan_transactions")
data class TabunganTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long = 0L,
    val studentName: String,
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val ownerWaliUsername: String = "",
    val ownerWaliName: String = "Wali Asuh",
    val isPrivate: Boolean = true,
    val transactionType: String = TabunganHelper.TYPE_IN, // IN | OUT
    val amount: Long = 0L,
    val title: String = "",
    val notes: String = "",
    val transactionDate: Long = System.currentTimeMillis(),
    val status: String = TabunganHelper.STATUS_ACTIVE, // ACTIVE | CANCELLED
    val cancelledBy: String = "",
    val cancelledReason: String = "",
    val cancelledAt: Long? = null,
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tabungan_logs")
data class TabunganLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long = 0L,
    val studentId: Long = 0L,
    val studentName: String,
    val studentClass: String = "",
    val ownerWaliUsername: String = "",
    val ownerWaliName: String = "Wali Asuh",
    val isPrivate: Boolean = true,
    val actionType: String = TabunganHelper.LOG_INPUT_IN, // INPUT_IN | INPUT_OUT | CANCEL | DELETE
    val amount: Long = 0L,
    val balanceBefore: Long = 0L,
    val balanceAfter: Long = 0L,
    val summary: String = "",
    val actorName: String = "Wali Asuh",
    val actorUsername: String = "",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis()
)

data class TabunganStudentSummary(
    val totalIn: Long,
    val totalOut: Long,
    val balance: Long,
    val activeCount: Int,
    val cancelledCount: Int,
    val latestTransaction: TabunganTransaction?
)

data class TabunganAggregateTotals(
    val totalBalance: Long,
    val totalIn: Long,
    val totalOut: Long,
    val activeCount: Int,
    val holdersCount: Int
)

object TabunganHelper {
    const val TYPE_IN = "IN"
    const val TYPE_OUT = "OUT"

    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_CANCELLED = "CANCELLED"

    const val LOG_INPUT_IN = "INPUT_IN"
    const val LOG_INPUT_OUT = "INPUT_OUT"
    const val LOG_EDIT = "EDIT"
    const val LOG_CANCEL = "CANCEL"
    const val LOG_DELETE = "DELETE"

    fun formatRupiah(amount: Long): String {
        val absVal = kotlin.math.abs(amount)
        val formatter = NumberFormat.getIntegerInstance(Locale("id", "ID"))
        val formatted = formatter.format(absVal)
        return if (amount < 0L) "-Rp $formatted" else "Rp $formatted"
    }

    fun isIncome(type: String): Boolean = type.trim().equals(TYPE_IN, ignoreCase = true)

    fun isActive(status: String): Boolean = !status.trim().equals(STATUS_CANCELLED, ignoreCase = true)

    fun matchesStudent(tx: TabunganTransaction, student: ChildStudent): Boolean {
        if (student.id > 0L && tx.studentId == student.id) return true
        if (student.nis.isNotBlank() && tx.studentNis.isNotBlank() &&
            tx.studentNis.trim().equals(student.nis.trim(), ignoreCase = true)
        ) {
            return true
        }
        return false
    }

    fun matchesStudentIdOrNis(
        tx: TabunganTransaction,
        studentId: Long,
        studentNis: String = ""
    ): Boolean {
        if (studentId > 0L && tx.studentId == studentId) return true
        val cleanNis = studentNis.trim()
        if (cleanNis.isNotBlank() && tx.studentNis.isNotBlank() &&
            tx.studentNis.trim().equals(cleanNis, ignoreCase = true)
        ) {
            return true
        }
        return false
    }

    fun resolveHolderKey(tx: TabunganTransaction): String {
        val cleanNis = tx.studentNis.trim().uppercase()
        if (cleanNis.isNotBlank()) return "nis:$cleanNis"
        if (tx.studentId > 0L) return "id:${tx.studentId}"
        return "name:${tx.studentName.trim().lowercase()}"
    }

    fun computeSummaryForStudent(
        transactions: List<TabunganTransaction>,
        student: ChildStudent
    ): TabunganStudentSummary {
        val matching = transactions
            .filter { matchesStudent(it, student) }
            .sortedWith(compareByDescending<TabunganTransaction> { it.transactionDate }.thenByDescending { it.createdAt })
        val active = matching.filter { isActive(it.status) }
        val totalIn = active.filter { isIncome(it.transactionType) }.sumOf { it.amount }
        val totalOut = active.filter { !isIncome(it.transactionType) }.sumOf { it.amount }
        return TabunganStudentSummary(
            totalIn = totalIn,
            totalOut = totalOut,
            balance = (totalIn - totalOut).coerceAtLeast(0L),
            activeCount = active.size,
            cancelledCount = matching.size - active.size,
            latestTransaction = matching.firstOrNull()
        )
    }

    fun computeSummaryByStudentId(
        transactions: List<TabunganTransaction>,
        studentId: Long,
        studentNis: String = ""
    ): TabunganStudentSummary {
        val matching = transactions
            .filter { matchesStudentIdOrNis(it, studentId, studentNis) }
            .sortedWith(compareByDescending<TabunganTransaction> { it.transactionDate }.thenByDescending { it.createdAt })
        val active = matching.filter { isActive(it.status) }
        val totalIn = active.filter { isIncome(it.transactionType) }.sumOf { it.amount }
        val totalOut = active.filter { !isIncome(it.transactionType) }.sumOf { it.amount }
        return TabunganStudentSummary(
            totalIn = totalIn,
            totalOut = totalOut,
            balance = (totalIn - totalOut).coerceAtLeast(0L),
            activeCount = active.size,
            cancelledCount = matching.size - active.size,
            latestTransaction = matching.firstOrNull()
        )
    }

    fun computeRawNetBalance(transactions: List<TabunganTransaction>): Long {
        val active = transactions.filter { isActive(it.status) }
        val totalIn = active.filter { isIncome(it.transactionType) }.sumOf { it.amount }
        val totalOut = active.filter { !isIncome(it.transactionType) }.sumOf { it.amount }
        return totalIn - totalOut
    }

    fun computeAggregateTotals(transactions: List<TabunganTransaction>): TabunganAggregateTotals {
        val active = transactions.filter { isActive(it.status) }
        val totalIn = active.filter { isIncome(it.transactionType) }.sumOf { it.amount }
        val totalOut = active.filter { !isIncome(it.transactionType) }.sumOf { it.amount }
        val holders = active
            .groupBy { resolveHolderKey(it) }
            .count { (_, list) ->
                val inSum = list.filter { isIncome(it.transactionType) }.sumOf { it.amount }
                val outSum = list.filter { !isIncome(it.transactionType) }.sumOf { it.amount }
                inSum - outSum > 0L
            }
        return TabunganAggregateTotals(
            totalBalance = (totalIn - totalOut).coerceAtLeast(0L),
            totalIn = totalIn,
            totalOut = totalOut,
            activeCount = active.size,
            holdersCount = holders
        )
    }

    fun buildSummaryStudents(
        myFosterStudents: List<ChildStudent>,
        allStudents: List<ChildStudent>,
        transactions: List<TabunganTransaction>
    ): List<ChildStudent> {
        val result = linkedMapOf<String, ChildStudent>()
        fun studentKey(st: ChildStudent): String {
            val cleanNis = st.nis.trim().uppercase()
            return if (cleanNis.isNotBlank()) "nis:$cleanNis" else "id:${st.id}"
        }
        myFosterStudents.forEach { st -> result[studentKey(st)] = st }
        transactions.forEach { tx ->
            val key = resolveHolderKey(tx)
            if (!result.containsKey(key) && result.values.none { matchesStudent(tx, it) }) {
                val matched = allStudents.firstOrNull { matchesStudent(tx, it) }
                    ?: ChildStudent(
                        id = tx.studentId,
                        name = tx.studentName,
                        nis = tx.studentNis,
                        gender = "L",
                        age = 12,
                        dormitory = tx.studentDorm,
                        classGrade = tx.studentClass,
                        assignedWaliUsername = tx.ownerWaliUsername,
                        assignedWaliName = tx.ownerWaliName,
                        schoolId = tx.schoolId
                    )
                result[studentKey(matched)] = matched
            }
        }
        return result.values.sortedBy { it.name.lowercase() }
    }

    fun resolveStudentByIdOrFallback(
        studentId: Long,
        myFosterStudents: List<ChildStudent>,
        allStudents: List<ChildStudent>,
        transactions: List<TabunganTransaction>,
        logs: List<TabunganLog>
    ): ChildStudent? {
        myFosterStudents.firstOrNull { it.id == studentId }?.let { return it }
        allStudents.firstOrNull { it.id == studentId }?.let { return it }

        val sampleTx = transactions.firstOrNull { it.studentId == studentId }
        if (sampleTx != null) {
            myFosterStudents.firstOrNull { matchesStudent(sampleTx, it) }?.let { return it }
            allStudents.firstOrNull { matchesStudent(sampleTx, it) }?.let { return it }
            return ChildStudent(
                id = sampleTx.studentId,
                name = sampleTx.studentName,
                nis = sampleTx.studentNis,
                gender = "L",
                age = 12,
                dormitory = sampleTx.studentDorm,
                classGrade = sampleTx.studentClass,
                assignedWaliUsername = sampleTx.ownerWaliUsername,
                assignedWaliName = sampleTx.ownerWaliName,
                schoolId = sampleTx.schoolId
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

    fun matchesPrivateOwner(
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

    fun filterPrivateTransactions(
        allTransactions: List<TabunganTransaction>,
        schoolId: String,
        role: UserRole,
        isAnakSuperAdmin: Boolean,
        currentUsername: String?,
        currentWaliName: String
    ): List<TabunganTransaction> {
        val cleanSchool = schoolId.trim()
        val inSchool = allTransactions.filter { it.schoolId.equals(cleanSchool, ignoreCase = true) }
        if (isAnakSuperAdmin) return inSchool
        if (role != UserRole.WALI) return emptyList()
        return inSchool.filter {
            matchesPrivateOwner(it.ownerWaliUsername, it.ownerWaliName, currentUsername, currentWaliName)
        }
    }

    fun filterPrivateLogs(
        allLogs: List<TabunganLog>,
        schoolId: String,
        role: UserRole,
        isAnakSuperAdmin: Boolean,
        currentUsername: String?,
        currentWaliName: String
    ): List<TabunganLog> {
        val cleanSchool = schoolId.trim()
        val inSchool = allLogs.filter { it.schoolId.equals(cleanSchool, ignoreCase = true) }
        if (isAnakSuperAdmin) return inSchool
        if (role != UserRole.WALI) return emptyList()
        return inSchool.filter {
            matchesPrivateOwner(it.ownerWaliUsername, it.ownerWaliName, currentUsername, currentWaliName)
        }
    }

    fun logActionLabel(actionType: String, isEn: Boolean): String {
        return when (actionType.trim().uppercase()) {
            LOG_INPUT_IN -> if (isEn) "Deposit (+)" else "Titip Masuk (+)"
            LOG_INPUT_OUT -> if (isEn) "Expense (-)" else "Pengeluaran (-)"
            LOG_EDIT -> if (isEn) "Edited" else "Diubah"
            LOG_CANCEL -> if (isEn) "Cancelled" else "Dibatalkan"
            LOG_DELETE -> if (isEn) "Deleted" else "Dihapus"
            else -> actionType
        }
    }
}
