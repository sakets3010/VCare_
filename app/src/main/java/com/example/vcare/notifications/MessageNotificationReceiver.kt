package com.example.vcare.notifications

import android.app.Notification
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.vcare.helper.ChatChannelId
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.ChatRepository
import com.example.vcare.helper.Id
import com.example.vcare.notifications.MyFirebaseMessaging.Companion.KEY_TEXT_REPLY
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.messaging.RemoteMessage


class MessageNotificationReceiver : BroadcastReceiver() {
    val repository = ChatRepository()
    private lateinit var id: String

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("notif","on recieve called")

        val sharedPref = context?.getSharedPreferences("Vcare", Context.MODE_PRIVATE)
        val fromId = sharedPref?.getString(MyFirebaseMessaging.USER_ID,"null")
        val toId = sharedPref?.getString(MyFirebaseMessaging.TO_ID,"null")
        val notifId = sharedPref?.getInt(MyFirebaseMessaging.NOTIFICATION_ID,1)


        Log.d("notif","userId:${fromId},toId:${toId}")

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val time = System.currentTimeMillis() / 1000

        val betweenList = mutableListOf(Id(fromId), Id(toId))
        val sortedList = betweenList.sortedBy { it.Id }

        if (remoteInput != null) {

            val inputString = remoteInput.getCharSequence(
                KEY_TEXT_REPLY
            ).toString()
            if(fromId!==null&&toId!==null) {
                Log.d("notif","main loop called")
                repository.getChatReference()?.whereEqualTo("between", sortedList)
                    ?.get()?.addOnSuccessListener { documents ->
                        if (!(documents.isEmpty)) {
                            for (document in documents) {
                                if (document.exists()) {
                                    id = document.id
                                    addMessage(id, ChatMessage(inputString, toId, fromId, time))
                                    return@addOnSuccessListener
                                }
                            }
                        }
                        repository.getChatReference()!!.add(ChatChannelId(sortedList))
                            .addOnSuccessListener { doc ->
                                id = doc.id
                                addMessage(id, ChatMessage(inputString, fromId, toId, time))
                                return@addOnSuccessListener
                            }
                    }
            }

            val repliedNotification = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Notification.Builder(context, OreoNotification.CHANNEL_ID)
                    .setSmallIcon(
                        android.R.drawable.ic_dialog_info)
                    .setContentText("Reply sent")
                    .build()
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val oreoNotification = OreoNotification(context)
            if (notifId != null) {
                oreoNotification.getManager?.notify(notifId,
                    repliedNotification)
            }

        }

    }

    private fun addMessage(Id: String, chatMessage: ChatMessage) {
        Log.d("notif","2nd loop called")
        repository.getChatReference()?.document(Id)?.collection("Messages")?.add(chatMessage)
            ?.addOnSuccessListener {
                updateMessageStatus(Id, it)
            }
    }

    private fun updateMessageStatus(id: String, it: DocumentReference?) {
        Log.d("notif","3rd loop called")
        if (it != null) {
            repository.getChatReference()?.document(id)?.collection("Messages")?.document(it.id)
                ?.update(
                    mapOf(
                        "status" to true
                    )
                )
        }

    }

}