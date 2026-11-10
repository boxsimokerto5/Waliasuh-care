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

object NotificationHelper {

    const val CHANNEL_ID_HEALTH_ALERT = "channel_wali_asuh_health"
    private const val CHANNEL_NAME = "Laporan Kesehatan Anak Asuh"
    private const val CHANNEL_DESC = "Pemberitahuan resmi saat ada inputan anak asuh sakit atau jadwal pengobatan Puskesmas"

    /**
     * Memastikan Notification Channel terdaftar di sistem Android (Android 8.0 Oreo ke atas).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID_HEALTH_ALERT, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
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
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        try {
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

            val roomDisplay = if (roomNumber.isNotBlank()) "Kamar $roomNumber" else "Asrama"
            val classDisplay = if (className.isNotBlank()) "Kelas $className" else "Siswa SRT 1"
            val faskesDisplay = if (faskes.isNotBlank()) faskes else "Puskesmas Rujukan"

            val title = "🏥 Anak Asuh Sakit: $studentName"
            val shortContent = "$classDisplay ($roomDisplay) • Keluhan: $complaint"
            val expandedContent = buildString {
                append("Nama Siswa: ").append(studentName).append("\n")
                append("Jenjang/Kelas: ").append(classDisplay).append(" • ").append(roomDisplay).append("\n")
                append("Keluhan: ").append(complaint).append("\n")
                append("Tujuan Rujukan: ").append(faskesDisplay).append("\n")
                append("Status: Terdaftar di sistem pemantauan Wali Asuh")
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_HEALTH_ALERT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(shortContent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(expandedContent))
                .setColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Gagal memunculkan notifikasi", e)
        }
    }

    /**
     * Notifikasi uji coba (test) agar pengguna dapat langsung memverifikasi sistem notifikasi resmi di perangkatnya.
     */
    fun showTestNotification(context: Context) {
        showSickStudentNotification(
            context = context,
            studentName = "Rama Aditia (Uji Coba)",
            className = "7 SMP",
            roomNumber = "A-02",
            complaint = "Demam & Pusing (Tes Notifikasi Resmi)",
            faskes = "Puskesmas Wates",
            notificationId = 9999
        )
    }
}
