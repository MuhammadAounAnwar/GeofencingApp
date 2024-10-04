package com.ono.geofencingapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence

object NotificationHandler {

    fun sendNotification(context: Context?, geofenceTransition: Int, message: String) {
        context?.let {
            val notificationManager = NotificationManagerCompat.from(it)

            val channelId = "geofence_notification_channel"
            val channelName = "Geofence Notification Channel"

            // Create notification channel if Android O or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for geofence notifications"
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Set the notification message based on the transition type
            val notificationMessage = when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "You have entered the geofence"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "You have exited the geofence"
                else -> "Geofence transition"
            }

            // Create an Intent to launch the app when notification is clicked
            val notificationIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build the notification
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Geofence Transition")
                .setContentText(notificationMessage + message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1, notification)
        }
    }
}