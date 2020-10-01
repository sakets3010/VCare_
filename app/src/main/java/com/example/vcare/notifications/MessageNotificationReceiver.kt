package com.example.vcare.notifications

import android.app.Notification
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.vcare.R
import com.example.vcare.helper.ChatChannelId
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.ChatRepository
import com.example.vcare.helper.Id
import com.example.vcare.notifications.MyFirebaseMessaging.Companion.KEY_TEXT_REPLY
import com.example.vcare.notifications.MyFirebaseMessaging.Companion.NOTIFICATION_ID
import com.example.vcare.notifications.MyFirebaseMessaging.Companion.TO_ID
import com.example.vcare.notifications.MyFirebaseMessaging.Companion.USER_ID
import com.google.firebase.firestore.DocumentReference


class MessageNotificationReceiver : BroadcastReceiver() {


    val repository = ChatRepository()
    lateinit var id: String

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun onReceive(context: Context?, intent: Intent?) {
        val fromId = intent?.getStringExtra(USER_ID)
        val toId = intent?.getStringExtra(TO_ID)
        val notificationId = intent?.getIntExtra(NOTIFICATION_ID, -1)
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val time = System.currentTimeMillis() / 1000
        val betweenList = mutableListOf(Id(fromId), Id(toId))
        val sortedList = betweenList.sortedBy { it.Id }

        if (remoteInput != null) {
            val inputString = remoteInput.getCharSequence( KEY_TEXT_REPLY ).toString()
            if (fromId !== null && toId !== null) {
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
                                addMessage(id, ChatMessage(inputString, toId, fromId, time))
                                return@addOnSuccessListener
                            }
                    }
            }

            val repliedNotification =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder(context, OreoNotification.CHANNEL_ID)
                        .setSmallIcon( android.R.drawable.ic_dialog_info )
                        .setContentText(context?.getString(R.string.replied))
                        .build()
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
            val oreoNotification = OreoNotification(context)
            if (notificationId != null) {
                oreoNotification.getManager?.notify(
                    notificationId,
                    repliedNotification
                )
            }

        }

    }

    private fun addMessage(Id: String, chatMessage: ChatMessage) {
        repository.getChatReference()?.document(Id)?.collection("Messages")?.add(chatMessage)
            ?.addOnSuccessListener {
                updateMessageStatus(Id, it)
            }
    }

    private fun updateMessageStatus(id: String, it: DocumentReference?) {
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