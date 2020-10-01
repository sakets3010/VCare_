package com.example.vcare.home.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.vcare.helper.*
import com.example.vcare.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class HomeFragmentViewmodel @ViewModelInject constructor(
    private val _repository: ChatRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var _activeUsers: MutableLiveData<List<ChatMessage>> = MutableLiveData()
    private var _users = mutableListOf<ChatMessage>()
    private val _latestMessageMap = HashMap<String, ChatMessage>()
    private var _conversationList: MutableLiveData<List<ChatChannelIdWrapper>> = MutableLiveData()

    fun updateToken(token: String?) {
        val firebaseUser = Firebase.auth.currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = token?.let { Token(it) }
        if (firebaseUser != null) {
            ref.child(firebaseUser.uid).setValue(token1)
        }
    }

    fun listenForNewMessage(): LiveData<List<ChatChannelIdWrapper>> {
        _repository.getChatReference()?.whereArrayContains(
            "between", Id(Firebase.auth.currentUser?.uid)
        )
            ?.addSnapshotListener { documents, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (documents != null) {
                    val conversations = mutableListOf<ChatChannelIdWrapper>()
                    for (document in documents) {
                        conversations.add(
                            ChatChannelIdWrapper(
                                document.id, document.toObject(
                                    ChatChannelId::class.java
                                )
                            )
                        )

                    }
                    _conversationList.value = conversations

                } else {
                    Log.d("failed", "Current data: null")
                }

            }
        return _conversationList
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun displayUsers(conversations: List<ChatChannelIdWrapper>?): LiveData<List<ChatMessage>> {
        _users.clear()
        conversations?.forEach {
            _repository.getChatReference()?.document(it.docId)?.collection("Messages")
                ?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                ?.limit(1)
                ?.addSnapshotListener { documents, _ ->
                    if (documents != null) {
                        for (doc in documents) {
                            _latestMessageMap[it.docId] = doc.toObject(ChatMessage::class.java)
                        }
                        _activeUsers.value = _latestMessageMap.values.toList()
                    }
                }
        }
        return _activeUsers
    }


}