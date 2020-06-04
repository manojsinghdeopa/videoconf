package com.appypie.video.app.ui.room

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.appypie.video.app.R
import com.appypie.video.app.ui.home.HomeActivity

private const val VIDEO_SERVICE_CHANNEL = "VIDEO_SERVICE_CHANNEL"
const val ONGOING_NOTIFICATION_ID = 1

class RoomNotification(private val context: Context) {


    private fun pendingIntent(): PendingIntent? {
        val intent = Intent(context, HomeActivity::class.java)
        intent.putExtra("isFromNotification", true)
        intent.addFlags(/*Intent.FLAG_ACTIVITY_CLEAR_TASK*/Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT/*0*/)
    }


    init {
        createDownloadNotificationChannel(VIDEO_SERVICE_CHANNEL, context.getString(R.string.room_notification_channel_title), context)
    }

    fun buildNotification(roomName: String): Notification =
            NotificationCompat.Builder(context, VIDEO_SERVICE_CHANNEL)
                    .setContentTitle(context.getString(R.string.room_notification_title, roomName))
                   /* .setContentText(context.getString(R.string.room_notification_message))
                    .setContentIntent(pendingIntent())
                    .setTicker(context.getString(R.string.room_notification_message))*/
                    .setUsesChronometer(true)
                    .setSmallIcon(R.drawable.ic_videocam_notification)
                    .build()


    private fun createDownloadNotificationChannel(channelId: String, channelName: String, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}