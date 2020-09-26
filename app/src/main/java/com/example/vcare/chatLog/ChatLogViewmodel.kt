package com.example.vcare.chatLog

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.vcare.helper.ChatRepository
import com.example.vcare.R
import com.example.vcare.helper.ChatChannelId
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.Id
import com.example.vcare.helper.User
import com.example.vcare.notifications.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatLogViewmodel @ViewModelInject constructor(
    private val repository: ChatRepository
) : ViewModel() {
    private var status: MutableLiveData<User> = MutableLiveData()
    private var messageList: MutableLiveData<List<ChatMessage>> = MutableLiveData()
    private var messages = mutableListOf<ChatMessage>()
    private var docids = mutableListOf<String>()
    private var docList: MutableLiveData<List<String>> = MutableLiveData()
    private lateinit var id: String
    private lateinit var docId: String
    var notify = false
    private val fromId = Firebase.auth.uid
    val uid = Firebase.auth.uid
    private val firebaseUser = FirebaseAuth.getInstance().currentUser

    fun evaluateStatus(user: User?): LiveData<User> {
        if (user != null) {
            repository.getUserReference(user.uid)?.addSnapshotListener { snap, _ ->
                val details = snap?.toObject(User::class.java)
                status.value = details
            }
        }
        return status
    }

    fun returnUser(user: User?): User? {
        return user
    }

    fun listenForMessages(user: User): LiveData<List<String>> {
        val toId = user.uid
        val betweenList = mutableListOf(Id(fromId), Id(toId))
        val sortedList = betweenList.sortedBy { it.Id }
        repository.getChatReference()?.whereEqualTo("between", sortedList)
            ?.addSnapshotListener { documents, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (documents != null) {
                    for (document in documents) {
                        docId = document.id
                        docids.clear()
                        docids.add(docId)
                    }
                }
                docList.value = docids
            }
        return docList
    }

    var isScrollable: MutableLiveData<Boolean> = MutableLiveData()

    fun listener(docId: String): LiveData<List<ChatMessage>> {
//        val first = repository.getChatReference()?.document(docId)?.collection("Messages")
//            ?.orderBy("timestamp",Query.Direction.ASCENDING)
//            ?.limit(25)
//
//        first?.addSnapshotListener { snap, error ->
//            val lastVisible = snap!!.documents[snap.size() - 1]
//            val next = repository.getChatReference()?.document(docId)?.collection("Messages")
//                ?.orderBy("timestamp",Query.Direction.ASCENDING)
//                ?.startAfter(lastVisible)
//                ?.limit(25)
// }
        repository.getChatReference()?.document(docId)?.collection("Messages")?.orderBy(
            "timestamp",
            Query.Direction.ASCENDING
        )?.addSnapshotListener { snap, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snap != null) {
                messages.clear()
                for (doc in snap.documents) {
                    val chatMessage = doc.toObject(ChatMessage::class.java)
                    if (chatMessage != null) {
                        messages.add(chatMessage)
                        isScrollable.value = true
                    }
                }
            }

            messageList.value = messages
        }
        return messageList


    }

    fun updateTypingStatus(status: Long) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        repository.getUserReference(uid)?.update(
            mapOf(
                "status" to status
            )
        )
    }

    fun performSendMessage(user: User?, text: String, apiService: ApiService?) {
        val toId = user?.uid
        val time = System.currentTimeMillis() / 1000
        val chatMessage = ChatMessage(text, fromId!!, toId!!, time)
        val betweenList = mutableListOf(Id(fromId), Id(toId))
        val sortedList = betweenList.sortedBy { it.Id }

        repository.getChatReference()?.whereEqualTo("between", sortedList)
            ?.get()?.addOnSuccessListener { documents ->
                if (!(documents.isEmpty)) {
                    for (document in documents) {
                        if (document.exists()) {
                            id = document.id
                            addMessage(id, chatMessage)
                            return@addOnSuccessListener
                        }
                    }
                }
                repository.getChatReference()!!.add(ChatChannelId(sortedList))
                    .addOnSuccessListener { doc ->
                        id = doc.id
                        addMessage(id, chatMessage)
                        return@addOnSuccessListener
                    }
            }
        //fcm
        if (firebaseUser != null) {
            repository.getUserReference(uid!!)?.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d("ChatLogActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val userLocal = snapshot.toObject(User::class.java)
                    if (notify) {
                        if (userLocal != null) {
                            sendNotifications(
                                toId,
                                userLocal.username,
                                chatMessage.text,
                                apiService!!
                            )
                            Log.d("notif","sent:${userLocal.username}")
                        }
                    }
                    notify = false

                } else {
                    Log.d("ChatLogActivity", "Current data: null")
                }
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


    private fun sendNotifications(
        toId: String,
        username: String,
        text: String,
        apiService: ApiService
    ) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(toId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                //does nothing
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {

                    val token: Token? = snap.getValue(Token::class.java)
                    val chatMessage =
                        ChatMessage(text, fromId!!, toId!!, System.currentTimeMillis() / 1000)

                    val data = Data(uid!!,username, chatMessage.text, "New Message from $username",toId)
                    val sender = Sender(data, token?.getToken().toString())

                    apiService.sendNotification(sender).enqueue(object : Callback<MyResponse> {

                        override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                            //does nothing
                        }

                        override fun onResponse(
                            call: Call<MyResponse>,
                            response: Response<MyResponse>
                        ) {
                            //does nothing
                            Log.d("notif","sending:${data}")
                        }
                    })
                }
            }
        })
    }

    fun imageMessage(data: Intent, user: User, apiService: ApiService) {
        val fileUri = data.data
        val storageReference = FirebaseStorage.getInstance().reference.child("chat_images")
        val ref = FirebaseDatabase.getInstance().reference
        val messageId = ref.push().key
        val filePath = storageReference.child("$messageId.jpg")

        val uploadTask: StorageTask<*>
        uploadTask = filePath.putFile(fileUri!!)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation filePath.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                val url = downloadUrl.toString()
                val toId = user.uid
                val time = System.currentTimeMillis() / 1000
                val chatMessage =
                    ChatMessage(fromId = fromId!!, toId = toId, timestamp = time, url = url)
                val betweenList = mutableListOf(Id(fromId), Id(toId))
                val sortedList = betweenList.sortedBy { it.Id }
                repository.getChatReference()?.whereEqualTo("between", sortedList)
                    ?.get()?.addOnSuccessListener { documents ->
                        if (documents !== null) {
                            for (document in documents) {
                                repository.getChatReference()!!.document(document.id)
                                    .collection("Messages").add(chatMessage).addOnSuccessListener {
                                        if (firebaseUser != null) {
                                            repository.getUserReference(uid!!)
                                                ?.addSnapshotListener { snapshot, e ->
                                                    if (e != null) {
                                                        Log.w(
                                                            "ChatLogActivity",
                                                            "Listen failed.",
                                                            e
                                                        )
                                                        return@addSnapshotListener
                                                    }
                                                    if (snapshot != null && snapshot.exists()) {
                                                        val userSnap =
                                                            snapshot.toObject(User::class.java)
                                                        if (notify) {
                                                            sendNotifications(
                                                                toId,
                                                                userSnap!!.username,
                                                                "sent you an image",
                                                                apiService
                                                            )
                                                        }
                                                        notify = false
                                                    } else {
                                                        Log.d(
                                                            "ChatLogActivity",
                                                            "Current data: null"
                                                        )
                                                    }
                                                }
                                        }
                                    }
                            }
                        } else {
                            repository.getChatReference()!!.add(ChatChannelId(sortedList))
                                .addOnSuccessListener { documentReference ->
                                    repository.getChatReference()!!.document(documentReference.id)
                                        .collection("Messages").add(chatMessage)
                                        .addOnSuccessListener {
                                            if (firebaseUser != null) {
                                                repository.getUserReference(firebaseUser.uid)
                                                    ?.addSnapshotListener { snapshot, e ->
                                                        if (e != null) {
                                                            Log.w(
                                                                "ChatLogActivity",
                                                                "Listen failed.",
                                                                e
                                                            )
                                                            return@addSnapshotListener
                                                        }
                                                        if (snapshot != null && snapshot.exists()) {
                                                            val userSnap =
                                                                snapshot.toObject(User::class.java)
                                                            if (notify) {
                                                                sendNotifications(
                                                                    toId,
                                                                    userSnap!!.username,
                                                                    "sent you an image",
                                                                    apiService
                                                                )
                                                            }
                                                            notify = false
                                                        } else {
                                                            Log.d(
                                                                "ChatLogActivity",
                                                                "Current data: null"
                                                            )
                                                        }
                                                    }
                                            }
                                        }
                                }
                        }
                    }
            }
        }
    }
}