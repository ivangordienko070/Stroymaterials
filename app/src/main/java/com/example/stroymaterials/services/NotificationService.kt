// services/NotificationService.kt
package com.example.stroymaterials.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.stroymaterials.data.database.entities.MaterialEntity
import com.example.stroymaterials.R

class NotificationService(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "material_tracker_channel"
        const val LOW_STOCK_NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Уведомления о материалах",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о низком запасе материалов"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showLowStockNotification(materials: List<MaterialEntity>) {
        if (materials.isEmpty()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Используйте существующую иконку
            .setContentTitle("Низкий запас материалов")
            .setContentText("${materials.size} материала(ов) требуют пополнения")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(LOW_STOCK_NOTIFICATION_ID, notification)
    }
}