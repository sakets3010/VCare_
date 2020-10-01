package com.example.vcare.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import com.example.vcare.R

class OreoNotification(base: Context?) : ContextWrapper(base) {
    private var _notificationManager: NotificationManager? = null


    companion object {
        const val CHANNEL_ID = "com.example.vcare"
        private const val CHANNEL_NAME = "VCare"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

    }


    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        getManager?.createNotificationChannel(channel)
    }


    val getManager: NotificationManager?
        get() {
            if (_notificationManager == null) {
                _notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }

            return _notificationManager

        }

    @TargetApi(Build.VERSION_CODES.O)
    fun getOreoNotification(
        pendingIntent: PendingIntent?,
        replyAction: Notification.Action,
        style: Notification.Style
    )
            : Notification.Builder {

        return Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.v_care))
            .setSmallIcon(R.drawable.cherry378)
            .setAutoCancel(true)
            .addAction(replyAction)
            .setStyle(style)
    }


}