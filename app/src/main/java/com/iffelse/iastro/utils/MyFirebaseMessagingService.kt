package com.iffelse.iastro.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iffelse.iastro.HomeActivity
import com.iffelse.iastro.R
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("TAG", "onNewToken: $token" )
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.i("TAG", "onMessageReceived: $remoteMessage")

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            val title = it.title ?: "iastro"
            val body = it.body ?: "You have a new message."
            val imageUrl =
                remoteMessage.data["image"]  // Get the image URL from the data payload if sent

            // Show notification
            sendNotification(title, body, imageUrl)
        }
    }

    private fun sendNotification(title: String, message: String, imageUrl: String?) {
        val channelId = "default_channel"
        val notificationId = 1

        // Create an intent to open the app when the notification is clicked
        val intent = Intent(this, HomeActivity::class.java)  // Change this to your main activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Use FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        // Create a notification builder
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)  // Your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // If there is an image URL, fetch the image and set it in the notification
        imageUrl?.let { url ->
            val bitmap = getBitmapFromURL(url) // Fetch the bitmap
            bitmap?.let {
                val bigPictureStyle = NotificationCompat.BigPictureStyle()
                    .bigPicture(it)
                    .setBigContentTitle(title)
                    .setSummaryText(message)

                notificationBuilder.setStyle(bigPictureStyle)
            }
        }

        // Show the notification
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MyFirebaseMessagingService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, notificationBuilder.build())
        }

        // For Android O and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getBitmapFromURL(strURL: String): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
