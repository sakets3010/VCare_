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
import androidx.annotation.RequiresApi
import com.example.vcare.R
import com.example.vcare.helper.ChatRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessaging() : FirebaseMessagingService() {
    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val USER_ID = "userId"
        const val TO_ID = "toId"
        const val NOTIFICATION_ID = "notificationId"
    }


    val repository = ChatRepository()

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val toId = remoteMessage.data["toId"]
        val user = remoteMessage.data["user"]

        val sharedPref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentOnlineUser = sharedPref.getString("currentUser", "none")
        val firebaseUser = Firebase.auth.currentUser

        if (firebaseUser !== null) {
            if (currentOnlineUser !== user && toId == Firebase.auth.uid) {
                sendOreoNotification(remoteMessage)
            }
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val body = remoteMessage.data["body"]
        val name = remoteMessage.data["username"].toString()
        val toId = remoteMessage.data["toId"]


        val replyLabel = getString(R.string.reply_here)
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(replyLabel)
            .build()


        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        var i = 0
        if (j > 0) {
            i = j
        }
        val intent = Intent(this, MessageNotificationReceiver::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(USER_ID, user)
        intent.putExtra(TO_ID, toId)
        intent.putExtra(NOTIFICATION_ID, i)

        val pendingIntent =
            PendingIntent.getBroadcast(this, j, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val oreoNotification = OreoNotification(this)
        if (!appInForeground(applicationContext)) {
            val icon = Icon.createWithResource(
                applicationContext,
                android.R.drawable.ic_dialog_info
            )
            val replyAction = Notification.Action.Builder(icon, getString(R.string.reply), pendingIntent)
                .addRemoteInput(remoteInput)
                .build()

            val person = android.app.Person.Builder().setName(name).setKey(user).build()


            val message = body?.let { Notification.MessagingStyle.Message(it, Date().time, person)}

            val style = Notification.MessagingStyle(person).addMessage(message)
                .setConversationTitle(getString(R.string.v_care_messaging))


            val builder: Notification.Builder = oreoNotification.getOreoNotification(pendingIntent, replyAction, style)


            oreoNotification.getManager?.notify(i, builder.build())


        } else {
            oreoNotification.getManager?.cancel(i)
        }

    }


    private fun appInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        return runningAppProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

}
