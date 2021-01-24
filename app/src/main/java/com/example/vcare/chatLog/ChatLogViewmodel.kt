package com.example.vcare.chatLog

import android.net.Uri
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.vcare.chatLog.paging.FirestorePagingSource
import com.example.vcare.chatLog.paging.FirestorePagingSource.Companion.PAGING_LIMIT
import com.example.vcare.helper.*
import com.example.vcare.home.newMessage.NewMessageFragment
import com.example.vcare.notifications.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.TextMessage
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatLogViewmodel @ViewModelInject constructor(
    private val repository: ChatRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel(), LifecycleObserver {
    private var _status: MutableLiveData<User> = MutableLiveData()
    val status: LiveData<User>
        get() = _status

    private var _documentId: MutableLiveData<String> = MutableLiveData()
    val documentId: LiveData<String>
        get() = _documentId

    private val _messages = arrayListOf<TextMessage>()
    private val _smartReply = SmartReply.getClient()
    private val _suggestions = arrayListOf<String>()

    private var _replies: MutableLiveData<List<String>> = MutableLiveData()
    val replies: LiveData<List<String>>
        get() = _replies

    private var _messageAdded: MutableLiveData<Boolean> = MutableLiveData()
    val messageAdded: LiveData<Boolean>
        get() = _messageAdded


    private lateinit var _id: String
    var notify = false
    private val _fromId = Firebase.auth.uid
    val uid = Firebase.auth.uid
    private val _firebaseUser = Firebase.auth.currentUser

    private val _userIntent = savedStateHandle.get<User>(NewMessageFragment.USER_KEY)

    init {
        evaluateStatus(_userIntent)
        listenForMessages(_userIntent)
    }

    private fun evaluateStatus(user: User?): LiveData<User> {
        if (user != null) {
            repository.getUserReference(user.uid)?.addSnapshotListener { snap, _ ->
                val details = snap?.toObject(User::class.java)
                _status.value = details
            }
        }
        return _status
    }

    private var _docId: String = ""
    private fun listenForMessages(user: User?) {
        val toId = user?.uid
        val betweenList = mutableListOf(Id(_fromId), Id(toId))
        val sortedList = betweenList.sortedBy { it.Id }
        repository.getChatReference()?.whereEqualTo("between", sortedList)
            ?.addSnapshotListener { documents, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (documents != null) {
                    for (document in documents) {
                        _docId = document.id
                    }
                    _documentId.value = _docId
                    Log.d("observe","value:${_documentId.value}")
                    if(_documentId.value.toString().isNotEmpty()){
                        updateReplies(_documentId.value.toString())
                        checkForIncomingMessages(_documentId.value.toString())
                    }
                }
            }

    }


    private fun updateReplies(docId: String): LiveData<List<String>> {
        repository.getChatReference()?.document(docId)?.collection("Messages")?.orderBy(
            "timestamp",
            Query.Direction.ASCENDING
        )?.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                for (snap in snapshot) {
                    val message = snap.toObject(ChatMessage::class.java)
                    if (message.fromId.equals(Firebase.auth.uid)) {
                        _messages.add(
                            TextMessage.createForLocalUser(
                                message.text,
                                System.currentTimeMillis()
                            )
                        )

                    } else {

                        _messages.add(
                            TextMessage.createForRemoteUser(
                                message.text,
                                System.currentTimeMillis(),
                                message.toId
                            )
                        )
                    }
                    _smartReply.suggestReplies(
                        _messages.takeLast(3)
                    )
                        .addOnSuccessListener { it ->
                            _suggestions.clear()
                            it.suggestions.forEach {
                                _suggestions.add(it.text)
                            }
                            _replies.value = _suggestions

                        }
                }
            }
        }
        return _replies
    }

    private fun checkForIncomingMessages(docId: String){
        repository.getChatReference()?.document(docId)?.collection("Messages")?.orderBy(
            "timestamp",
            Query.Direction.ASCENDING
        )?.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                _messageAdded.postValue(true)
            }
        }

    }

    fun getFlow(docId: String): Flow<PagingData<ChatMessage>> {
        return Pager(PagingConfig(PAGING_LIMIT.toInt())) {
            FirestorePagingSource(FirebaseFirestore.getInstance(), docId)
        }.flow.cachedIn(viewModelScope)
    }


    fun updateTypingStatus(status: Long) {
        val uid = Firebase.auth.uid ?: ""
        repository.getUserReference(uid)?.update(
            mapOf(
                "status" to status
            )
        )
    }

    fun performSendMessage(user: User?, text: String, apiService: ApiService?) {
        val toId = user?.uid
        val time = System.currentTimeMillis() / 1000
        val chatMessage = toId?.let { ChatMessage(text, _fromId!!, it, time) }
        val betweenList = mutableListOf(Id(_fromId), Id(toId))
        val sortedList = betweenList.sortedBy { it.Id }

        repository.getChatReference()?.whereEqualTo("between", sortedList)
            ?.get()?.addOnSuccessListener { documents ->
                if (!(documents.isEmpty)) {
                    for (document in documents) {
                        if (document.exists()) {
                            _id = document.id
                            if (chatMessage != null) {
                                addMessage(_id, chatMessage)
                            }
                            return@addOnSuccessListener
                        }
                    }
                }
                repository.getChatReference()!!.add(ChatChannelId(sortedList))
                    .addOnSuccessListener { doc ->
                        _id = doc.id
                        if (chatMessage != null) {
                            addMessage(_id, chatMessage)
                        }
                        return@addOnSuccessListener
                    }
            }
        //fcm
        if (_firebaseUser != null) {
            if (uid != null) {
                repository.getUserReference(uid)?.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.d("ChatLogActivity", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val userLocal = snapshot.toObject(User::class.java)
                        if (notify) {
                            if (userLocal != null) {
                                if (toId != null) {
                                    if (chatMessage != null) {
                                        sendNotifications(
                                            toId,
                                            userLocal.username,
                                            chatMessage.text,
                                            apiService!!
                                        )
                                    }
                                }

                            }
                        }
                        notify = false

                    }else {
                        Log.d("ChatLogActivity", "Current data: null")
                    }
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

    fun updateStatus(userId: String, status: Long) {
        repository.getUserReference(userId)?.update(
            mapOf(
                "status" to status
            )
        )
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
                        _fromId?.let {
                            ChatMessage(
                                text,
                                it, toId, System.currentTimeMillis() / 1000
                            )
                        }

                    val data =
                        chatMessage?.text?.let {
                            Data(
                                uid!!, username,
                                it, "New Message from $username", toId
                            )
                        }
                    val sender = data?.let { Sender(it, token?.getToken().toString()) }

                    if (sender != null) {
                        apiService.sendNotification(sender).enqueue(object : Callback<MyResponse> {

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                //does nothing
                            }

                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                //does nothing
                            }
                        })
                    }
                }
            }
        })
    }

    fun imageMessage(uri: Uri, user: User, apiService: ApiService) {
        val storageReference = FirebaseStorage.getInstance().reference.child("chat_images")
        val ref = FirebaseDatabase.getInstance().reference
        val messageId = ref.push().key
        val filePath = storageReference.child("$messageId.jpg")

        val uploadTask: StorageTask<*>
        uploadTask = filePath.putFile(uri)
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
                    _fromId?.let {
                        ChatMessage(
                            fromId = it,
                            toId = toId,
                            timestamp = time,
                            url = url
                        )
                    }
                val betweenList = mutableListOf(Id(_fromId), Id(toId))
                val sortedList = betweenList.sortedBy { it.Id }
                repository.getChatReference()?.whereEqualTo("between", sortedList)
                    ?.get()?.addOnSuccessListener { documents ->
                        if (documents !== null) {
                            for (document in documents) {
                                if (chatMessage != null) {
                                    repository.getChatReference()?.document(document.id)
                                        ?.collection("Messages")?.add(chatMessage)
                                        ?.addOnSuccessListener {
                                            updateMessageStatus(document.id, it)
                                            if (_firebaseUser != null) {
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
                                                                if (userSnap != null) {
                                                                    sendNotifications(
                                                                        toId,
                                                                        userSnap.username,
                                                                        "sent you an image",
                                                                        apiService
                                                                    )
                                                                }
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
                        } else {
                            repository.getChatReference()?.add(ChatChannelId(sortedList))
                                ?.addOnSuccessListener { documentReference ->
                                    if (chatMessage != null) {
                                        repository.getChatReference()
                                            ?.document(documentReference.id)
                                            ?.collection("Messages")?.add(chatMessage)
                                            ?.addOnSuccessListener {
                                                updateMessageStatus(documentReference.id, it)
                                                if (_firebaseUser != null) {
                                                    repository.getUserReference(_firebaseUser.uid)
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
}