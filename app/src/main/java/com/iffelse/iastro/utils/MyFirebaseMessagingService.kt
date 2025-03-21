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
import com.iffelse.iastro.R
import com.iffelse.iastro.view.ui.ChatActivity
import com.iffelse.iastro.view.ui.HomeActivity
import com.sceyt.chatuikit.push.FirebaseMessagingDelegate
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessagingServ"
        private const val TITLE = "title"
        private const val EMPTY = ""
        private const val MESSAGE = "message"
        private const val IMAGE = "image"
        private const val ACTION = "action"
        private const val DATA = "data"
        private const val ACTION_DESTINATION = "action_destination"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirebaseMessagingDelegate.registerFirebaseToken(token)
        Log.i("TAG", "onNewToken: $token")
        if (KeyStorePref.getBoolean(AppConstants.KEY_STORE_IS_FCM_TOKEN_SENT)) {
            if (!KeyStorePref.getString(AppConstants.KEY_STORE_FCM_TOKEN).equals(token)) {
                KeyStorePref.putBoolean(AppConstants.KEY_STORE_IS_FCM_TOKEN_SENT, false)
            }
        } else {
            KeyStorePref.putBoolean(AppConstants.KEY_STORE_IS_FCM_TOKEN_SENT, false)
        }
        KeyStorePref.putString(AppConstants.KEY_STORE_FCM_TOKEN, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            runBlocking {
                if (!FirebaseMessagingDelegate.handleRemoteMessage(remoteMessage)) {
                    Log.i("TAG", "onMessageReceived: ${remoteMessage.notification?.title}")

                    if (remoteMessage.data.isNotEmpty()) {
                        Log.d(TAG, "Message data payload: " + remoteMessage.data)
                        val data = remoteMessage.data
                        handleData(data, null)
                    } else if (remoteMessage.notification != null) {
                        // Check if message contains a notification payload
                        remoteMessage.notification?.let {
                            val title = it.title ?: "iastro"
                            val body = it.body ?: "You have a new message."
                            val imageUrl =
                                remoteMessage.data["image"]  // Get the image URL from the data payload if sent

                            // Show notification
                            sendNotification(title, body, imageUrl, null)
                        }
                    }
                } else {
                    FirebaseMessagingDelegate.handleRemoteMessageGetData(remoteMessage)
                        ?.let { data ->
                            if (data.channel != null) {
                                val channel = data.channel ?: return@runBlocking
                                val user = data.user
                                val message = data.message


                                val intent = Intent(application, ChatActivity::class.java).apply {
                                    putExtra("CHANNEL", channel)
                                    putExtra("astrologer_phone", user?.id)
                                }

                                val pendingIntent =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        PendingIntent.getActivity(
                                            application, Random.nextInt(), intent,
                                            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                                        )
                                    } else {
                                        PendingIntent.getActivity(
                                            application, Random.nextInt(), intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                    }

                                val remoteMsg = mutableMapOf<String, String>()
                                user?.firstName?.let { remoteMsg.put(TITLE, it) }
                                user?.avatarURL?.let { remoteMsg.put(IMAGE, it) }
                                message?.body?.let { remoteMsg.put(MESSAGE, it) }
                                handleData(remoteMsg, pendingIntent)
                            }
                        }
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "handleRemoteMessage error: " + exception.message.toString())
        }
    }

    private fun handleData(data: Map<String, String>, pendingIntent: PendingIntent?) {
        Log.i(TAG, "handleData: Inside this")
        val title = data[TITLE]
        val message = data[MESSAGE]
        val iconUrl = data[IMAGE]
        val action = data[ACTION]
        val actionDestination = data[ACTION_DESTINATION]

        title?.let {
            sendNotification(title, message!!, iconUrl, pendingIntent)
        }
    }

    private fun sendNotification(
        title: String,
        message: String,
        imageUrl: String?,
        pendingIntent: PendingIntent?
    ) {
        val finalPendingIntent: PendingIntent
        val channelId = "default_channel"
        val notificationId = 1

        // For Android O and above, create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Default Channel Notifications"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        if (pendingIntent == null) {
            // Intent to open the app when notification is clicked
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Use FLAG_IMMUTABLE
            finalPendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            finalPendingIntent = pendingIntent
        }


        // Create a notification builder
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)  // Your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(finalPendingIntent)
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

        // Check permissions if on Android 13+ (TIRAMISU) and notify
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, notificationBuilder.build())
            }
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
