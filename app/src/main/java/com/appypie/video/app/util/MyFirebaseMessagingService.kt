package com.appypie.video.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.appypie.video.app.R
import com.appypie.video.app.ui.splash.SplashActivity
import com.appypie.video.app.util.Constants.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import java.util.*


val TAG = MyFirebaseMessagingService::class.simpleName

class MyFirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Timber.e(TAG, "From: ${remoteMessage.from}")
/*

        sendNotification(remoteMessage.from)

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Timber.e(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body)
        }

*/
        if (APP_TYPE == GUEST) {
            val i = Intent(END_MEETING_BROADCAST)
            i.putExtra("data", remoteMessage.from)
            sendBroadcast(i)
        }
    }

    override fun onNewToken(token: String) {
        DEVICE_TOKEN = token
    }


    private fun sendNotification(titleText: String?) {

        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        // startActivity(intent)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = getString(R.string.app_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(notificationIcon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(titleText)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT)
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel)
        }

        Objects.requireNonNull(notificationManager).notify(0, notificationBuilder.build())

    }


    private val notificationIcon: Int
        get() {
            val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            return if (useWhiteIcon) R.mipmap.ic_launcher else R.mipmap.ic_launcher
        }

}
