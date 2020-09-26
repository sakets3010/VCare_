package com.example.vcare.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import androidx.media.session.MediaButtonReceiver.handleIntent
import com.example.vcare.R
import kotlinx.android.synthetic.main.chat_row_to.*

class OreoNotification(base:Context?):ContextWrapper(base) {
    private var notificationManager : NotificationManager?=null


    companion object{
        const val CHANNEL_ID = "com.example.vcare"
        private const val CHANNEL_NAME = "VCare"
    }
    init{
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            createChannel()


        }

    }



    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(){
          val channel = NotificationChannel(
              CHANNEL_ID,
              CHANNEL_NAME,
              NotificationManager.IMPORTANCE_DEFAULT
          )
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getManager!!.createNotificationChannel(channel)

    }





    val getManager  :NotificationManager? get() {
        if(notificationManager==null){
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

            return notificationManager

    }
    @TargetApi(Build.VERSION_CODES.O)
    fun getOreoNotification(pendingIntent: PendingIntent?,replyAction:Notification.Action,style: Notification.Style)
            :Notification.Builder{

        return Notification.Builder(applicationContext, OreoNotification.CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle("Vcare")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .addAction(replyAction)
            .setStyle(style)
    }



}