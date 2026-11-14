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
     * Notifikasi uji coba (test) agar pengguna dapat langsung memverifikasi sistem notifikasi resmi di perangkatnya.
     */
    fun showTestNotification(context: Context) {
        val s = AppLanguageManager.getStrings(context).report
        showSickStudentNotification(
            context = context,
            studentName = s.notifTestStudentName,
            className = "7 SMP",
            roomNumber = "A-02",
            complaint = s.notifTestComplaint,
            faskes = "Puskesmas Wates",
            notificationId = 9999
        )
    }
}
