package com.example.vcare.notifications

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.vcare.helper.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessaging() : FirebaseMessagingService() {
    companion object {
        val KEY_TEXT_REPLY = "key_text_reply"
        val USER_ID = "userId"
        val TO_ID = "toId"
        val NOTIFICATION_ID = "notf-id"
    }


    val repository = ChatRepository()

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun onMessageReceived(mRemoteMessage: RemoteMessage) {
        Log.d("notif", "called 1")
        super.onMessageReceived(mRemoteMessage)
        val toId = mRemoteMessage.data["toId"]
        val user = mRemoteMessage.data["user"]


        Log.d("notif", "${toId},${user}")
        val sharedPref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentOnlineUser = sharedPref.getString("currentUser", "none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser !== null) {
            if (currentOnlineUser !== user && toId == Firebase.auth.uid) {
                Log.d("notif", "called func")
                sendOreoNotification(mRemoteMessage)
            }
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    private fun sendOreoNotification(mRemoteMessage: RemoteMessage) {
        val sharedPref = this.getSharedPreferences("Vcare", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        Log.d("notif", "call received")
        val user = mRemoteMessage.data["user"]
        val body = mRemoteMessage.data["body"]
        val name = mRemoteMessage.data["username"].toString()
        val toId = mRemoteMessage.data["toId"]

        Log.d("notif", "called")
        val replyLabel = "Enter your reply here"
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(replyLabel)
            .build()
        Log.d("notif", "${body},${name},${user}")

        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageNotificationReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, j, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        editor.putString(USER_ID, user)
        editor.apply()
        editor.putString(TO_ID, toId)
        editor.apply()
//        intent.putExtra(USER_ID, user)
//        intent.putExtra(TO_ID, toId)


        var i = 0
        if (j > 0) {
            i = j
        }
        val oreoNotification = OreoNotification(this)
        if (!appInForeground(applicationContext)) {
            Log.d("notif", "final call recieved")
            val icon_ = Icon.createWithResource(
                applicationContext,
                android.R.drawable.ic_dialog_info
            )


            val replyAction = Notification.Action.Builder(
                icon_,
                "Reply", pendingIntent
            )
                .addRemoteInput(remoteInput)
                .build()

            val userP = android.app.Person.Builder().setName(name).setKey(user).build()


            val message = body?.let {
                Notification.MessagingStyle.Message(
                    it,
                    Date().time, userP
                )
            }!!
            Log.d("notif", "recieved :${message.text}")

            val style = Notification.MessagingStyle(userP).addMessage(message)
                .setConversationTitle("VCare Messaging")


            val builder: Notification.Builder =
                oreoNotification.getOreoNotification(pendingIntent, replyAction, style)

            editor.putInt(NOTIFICATION_ID,i)
            editor.apply()
            oreoNotification.getManager!!.notify(i, builder.build())


        } else {
            oreoNotification.getManager!!.cancel(i)
        }


    }


    private fun appInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        return runningAppProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

}
