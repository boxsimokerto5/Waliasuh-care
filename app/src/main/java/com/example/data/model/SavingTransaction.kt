package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

@Entity(tableName = "saving_transactions")
data class SavingTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long = 0,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val transactionType: String = TYPE_DEPOSIT, // "DEPOSIT" | "EXPENSE"
    val amount: Long = 0L,
    val transactionDate: Long = System.currentTimeMillis(),
    val purpose: String = "",
    val notes: String = "",
    val waliName: String = "Wali Asuh",
    val schoolId: String = "SRT1-KDR",
    // Bukti foto pengeluaran & retensi 3 bulan (90 hari)
    val proofPhotoLocalPath: String = "",
    val proofPhotoRemoteFile: String = "",
    val proofUploadedAt: Long? = null,
    val proofExpiresAt: Long? = null,
    val proofStatus: String = PROOF_STATUS_NONE, // "NONE" | "ACTIVE" | "EXPIRED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_DEPOSIT = "DEPOSIT"
        const val TYPE_EXPENSE = "EXPENSE"

        const val PROOF_STATUS_NONE = "NONE"
        const val PROOF_STATUS_ACTIVE = "ACTIVE"
        const val PROOF_STATUS_EXPIRED = "EXPIRED"
    }
}

object SavingRetentionHelper {
    const val RETENTION_DAYS = 90L
    const val DAY_MILLIS = 86_400_000L
    const val RETENTION_MILLIS = RETENTION_DAYS * DAY_MILLIS

    fun computeExpiresAt(uploadedAt: Long): Long = uploadedAt + RETENTION_MILLIS

    fun hasAnyProofReference(tx: SavingTransaction): Boolean {
        return tx.proofPhotoLocalPath.isNotBlank() ||
            tx.proofPhotoRemoteFile.isNotBlank() ||
            tx.proofUploadedAt != null ||
            tx.proofStatus != SavingTransaction.PROOF_STATUS_NONE
    }

    fun isProofExpired(tx: SavingTransaction, now: Long = System.currentTimeMillis()): Boolean {
        if (tx.proofStatus == SavingTransaction.PROOF_STATUS_EXPIRED) return true
        val exp = tx.proofExpiresAt ?: return false
        return exp <= now
    }

    fun isProofActive(tx: SavingTransaction, now: Long = System.currentTimeMillis()): Boolean {
        if (tx.proofStatus == SavingTransaction.PROOF_STATUS_EXPIRED) return false
        val hasFile = tx.proofPhotoLocalPath.isNotBlank() || tx.proofPhotoRemoteFile.isNotBlank()
        if (!hasFile) return false
        val exp = tx.proofExpiresAt ?: return true
        return exp > now
    }

    fun remainingDays(tx: SavingTransaction, now: Long = System.currentTimeMillis()): Long {
        val exp = tx.proofExpiresAt ?: return 0L
        val diff = exp - now
        if (diff <= 0L) return 0L
        return max(1L, (diff + DAY_MILLIS - 1L) / DAY_MILLIS)
    }

    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        return "Rp ${formatter.format(amount)}"
    }

    fun computeStudentBalance(transactions: List<SavingTransaction>, studentId: Long, studentNik: String = "", studentName: String = ""): Long {
        val cleanNik = studentNik.trim()
        val cleanName = studentName.trim()
        return transactions.filter { tx ->
            (studentId > 0L && tx.studentId == studentId) ||
                (cleanNik.isNotBlank() && tx.studentNik == cleanNik) ||
                (cleanName.isNotBlank() && tx.studentName.equals(cleanName, ignoreCase = true))
        }.sumOf { tx ->
            if (tx.transactionType == SavingTransaction.TYPE_EXPENSE) -tx.amount else tx.amount
        }
    }
}
