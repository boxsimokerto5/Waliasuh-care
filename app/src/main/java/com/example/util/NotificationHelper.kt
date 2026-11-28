package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.util.i18n.AppLanguageManager

object NotificationHelper {

    const val CHANNEL_ID_HEALTH_ALERT = "channel_wali_asuh_health"
    const val CHANNEL_ID_PARENT_PORTAL = "channel_parent_portal_updates"
    const val EXTRA_PARENT_TAB = "EXTRA_PARENT_TAB"
    const val EXTRA_OPEN_PARENT_NOTIFS = "EXTRA_OPEN_PARENT_NOTIFS"

    /**
     * Memastikan Notification Channel terdaftar di sistem Android (Android 8.0 Oreo ke atas).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val s = AppLanguageManager.getStrings(context).report
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID_HEALTH_ALERT, s.notifChannelName, importance).apply {
                description = s.notifChannelDesc
                enableLights(true)
                lightColor = Color.parseColor("#0284C7")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
                setShowBadge(true)
            }

            val notificationManager: NotificationManager? =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Memastikan Notification Channel khusus Portal Orang Tua terdaftar dengan prioritas tinggi
     * agar pemberitahuan baru tampil di bilah atas Android (Heads-Up).
     */
    fun createParentNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val isEn = AppLanguageManager.getLanguage(context) == com.example.util.i18n.AppLanguage.ENGLISH
            val name = if (isEn) "Parent Portal Notifications" else "Notifikasi Portal Orang Tua"
            val desc = if (isEn) {
                "Alerts for your child's latest updates, messages, health, savings, and visits."
            } else {
                "Pemberitahuan pembaruan terbaru ananda: pesan wali, kesehatan, mengaji, sholat, tabungan, & kunjungan."
            }
            val channel = NotificationChannel(CHANNEL_ID_PARENT_PORTAL, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = desc
                enableLights(true)
                lightColor = Color.parseColor("#0D9488")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 280, 120, 280)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager? =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Menampilkan notifikasi resmi sistem Android saat ada laporan anak asuh sakit baru.
     */
    fun showSickStudentNotification(
        context: Context,
        studentName: String,
        className: String,
        roomNumber: String,
        complaint: String,
        faskes: String,
        schoolName: String = "",
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        try {
            val s = AppLanguageManager.getStrings(context).report
            // Pastikan channel sudah dibuat
            createNotificationChannel(context)

            // Cek izin notifikasi di Android 13 (Tiramisu)+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted")
                    return
                }
            }

            // Intent membuka aplikasi langsung ke menu Tindakan / Rekam Medis
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_NAV_SCREEN", "TINDAKAN")
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                pendingIntentFlags
            )

            val roomDisplay = if (roomNumber.isNotBlank()) s.notifRoomFormat(roomNumber) else s.notifRoomDefault
            val classDisplay = if (className.isNotBlank()) s.notifClassFormat(className) else s.notifClassDefault
            val faskesDisplay = if (faskes.isNotBlank()) faskes else s.notifFaskesDefault
            val schoolBadge = if (schoolName.isNotBlank()) "[$schoolName] " else ""

            val title = s.notifTitleFormat(schoolBadge, studentName)
            val shortContent = s.notifShortFormat(classDisplay, roomDisplay, complaint)
            val expandedContent = buildString {
                if (schoolName.isNotBlank()) {
                    append(s.notifSchoolLine(schoolName)).append("\n")
                }
                append(s.notifStudentLine(studentName)).append("\n")
                append(s.notifClassRoomLine(classDisplay, roomDisplay)).append("\n")
                append(s.notifComplaintLine(complaint)).append("\n")
                append(s.notifFaskesLine(faskesDisplay)).append("\n")
                append(s.notifStatusLine)
            }

            val largeIconBitmap = try {
                android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.logo_sr_care)
            } catch (_: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_HEALTH_ALERT)
                .setSmallIcon(R.drawable.ic_notification)
                .apply {
                    if (largeIconBitmap != null) {
                        setLargeIcon(largeIconBitmap)
                    }
                }
                .setContentTitle(title)
                .setContentText(shortContent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(expandedContent))
                .setColor(0xFF0A5081.toInt())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to show notification", e)
        }
    }

    /**
     * Membersihkan seluruh notifikasi aktif di panel perangkat (misal saat pengguna logout / berpindah akun sekolah).
     */
    fun cancelAllNotifications(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancelAll()
        } catch (e: Exception) {
            Log.w("NotificationHelper", "Failed to cancel notifications", e)
        }
    }

    /**
     * Menampilkan notifikasi sistem Android saat rekan Wali Asuh menghapus riwayat Mengaji, Puasa, atau Berobat.
     */
    fun showDeletionAlertNotification(
        context: Context,
        logs: List<com.example.data.model.DeletionLog>,
        schoolName: String = "",
        notificationId: Int = 8801
    ) {
        if (logs.isEmpty()) return
        try {
            val isEn = AppLanguageManager.getLanguage(context) == com.example.util.i18n.AppLanguage.ENGLISH
            createNotificationChannel(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (permission != PackageManager.PERMISSION_GRANTED) return
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                pendingIntentFlags
            )

            val schoolBadge = if (schoolName.isNotBlank()) "[$schoolName] " else ""
            val latest = logs.first()
            fun catLabel(cat: String): String = when (cat.uppercase()) {
                com.example.data.model.DeletionLog.CATEGORY_MENGAJI -> "Mengaji"
                com.example.data.model.DeletionLog.CATEGORY_PUASA -> if (isEn) "Fasting" else "Puasa"
                com.example.data.model.DeletionLog.CATEGORY_BEROBAT -> if (isEn) "Medical" else "Berobat"
                com.example.data.model.DeletionLog.CATEGORY_SHOLAT -> if (isEn) "Prayer Attendance" else "Absen Sholat"
                else -> cat
            }

            val title = if (logs.size == 1) {
                if (isEn) "${schoolBadge}History Deleted: ${catLabel(latest.category)}"
                else "${schoolBadge}Riwayat ${catLabel(latest.category)} Dihapus"
            } else {
                if (isEn) "${schoolBadge}${logs.size} Student History Entries Deleted"
                else "${schoolBadge}${logs.size} Riwayat Siswa Dihapus"
            }

            val shortText = if (isEn) {
                "${latest.deletedBy} deleted ${catLabel(latest.category)} for ${latest.studentName}"
            } else {
                "${latest.deletedBy} menghapus riwayat ${catLabel(latest.category)} ${latest.studentName}"
            }

            val expanded = buildString {
                logs.take(5).forEach { item ->
                    val cls = if (item.studentClass.isNotBlank()) " (${item.studentClass})" else ""
                    val sum = if (item.deletedSummary.isNotBlank()) ": ${item.deletedSummary}" else ""
                    append("• [${catLabel(item.category)}] ${item.studentName}$cls$sum — ${item.deletedBy}\n")
                }
                if (logs.size > 5) {
                    append(if (isEn) "...and ${logs.size - 5} more." else "...dan ${logs.size - 5} lainnya.")
                }
            }.trim()

            val largeIconBitmap = try {
                android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.logo_sr_care)
            } catch (_: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_HEALTH_ALERT)
                .setSmallIcon(R.drawable.ic_notification)
                .apply {
                    if (largeIconBitmap != null) setLargeIcon(largeIconBitmap)
                }
                .setContentTitle(title)
                .setContentText(shortText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(expanded))
                .setColor(0xFFD97706.toInt())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to show deletion alert notification", e)
        }
    }

    /**
     * Menampilkan notifikasi resmi di bilah atas Android (Heads-Up) saat ada pembaruan baru di Portal Orang Tua.
     * Jika lebih dari 1 item masuk sekaligus, otomatis diringkas menjadi 1 notifikasi InboxStyle agar tidak bising.
     */
    fun showParentPortalUpdateNotification(
        context: Context,
        studentName: String,
        schoolName: String,
        items: List<ParentNotificationItem>,
        notificationId: Int = 7701
    ) {
        if (items.isEmpty()) return
        try {
            val isEn = AppLanguageManager.getLanguage(context) == com.example.util.i18n.AppLanguage.ENGLISH
            createParentNotificationChannel(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Log.w("NotificationHelper", "POST_NOTIFICATIONS not granted for parent update")
                    return
                }
            }

            val topItem = items.first()
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_PARENT_TAB, topItem.category.tabKey)
                if (items.size > 1) {
                    putExtra(EXTRA_OPEN_PARENT_NOTIFS, true)
                }
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                pendingIntentFlags
            )

            val schoolBadge = if (schoolName.isNotBlank()) "[$schoolName] " else ""
            val largeIconBitmap = try {
                android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.logo_sr_care)
            } catch (_: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_PARENT_PORTAL)
                .setSmallIcon(R.drawable.ic_notification)
                .apply {
                    if (largeIconBitmap != null) setLargeIcon(largeIconBitmap)
                }
                .setColor(0xFF0D9488.toInt())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            if (items.size == 1) {
                val title = schoolBadge + if (isEn) topItem.titleEn else topItem.titleId
                val body = if (isEn) topItem.bodyEn else topItem.bodyId
                builder
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            } else {
                val summaryTitle = if (isEn) {
                    "$schoolBadge${items.size} New Updates for $studentName"
                } else {
                    "$schoolBadge${items.size} Pembaruan Baru Ananda ($studentName)"
                }
                val summaryLine = items.take(3).joinToString(" • ") { item ->
                    if (isEn) item.category.labelEn else item.category.labelId
                }
                val inboxStyle = NotificationCompat.InboxStyle()
                    .setBigContentTitle(summaryTitle)
                    .setSummaryText(if (isEn) "Tap to open Parent Portal" else "Ketuk untuk membuka Portal Orang Tua")
                items.take(5).forEach { item ->
                    val cat = if (isEn) item.category.labelEn else item.category.labelId
                    val body = if (isEn) item.bodyEn else item.bodyId
                    inboxStyle.addLine("$cat: $body")
                }
                if (items.size > 5) {
                    inboxStyle.addLine(if (isEn) "+${items.size - 5} more updates" else "+${items.size - 5} pembaruan lainnya")
                }
                builder
                    .setContentTitle(summaryTitle)
                    .setContentText(summaryLine)
                    .setStyle(inboxStyle)
                    .setNumber(items.size)
            }

            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to show parent portal notification", e)
        }
    }

    /**
     * Notifikasi uji coba (test) agar pengguna dapat langsung memverifikasi fitur di perangkatnya.
     */
    fun showTestNotification(context: Context, schoolName: String = "") {
        val s = AppLanguageManager.getStrings(context).report
        showSickStudentNotification(
            context = context,
            studentName = s.notifTestStudentName,
            className = "7 SMP",
            roomNumber = "A-02",
            complaint = s.notifTestComplaint,
            faskes = "Puskesmas Wates",
            schoolName = schoolName,
            notificationId = 9999
        )
    }
}
