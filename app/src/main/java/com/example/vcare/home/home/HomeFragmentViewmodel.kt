package com.example.vcare.home.home

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.vcare.helper.ChatRepository
import com.example.vcare.helper.ChatChannelId
import com.example.vcare.helper.ChatChannelIdWrapper
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.Id
import com.example.vcare.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

class HomeFragmentViewmodel@ViewModelInject constructor(
    private val repository: ChatRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var activeUsers: MutableLiveData<List<ChatMessage>> = MutableLiveData()
    private var users = mutableListOf<ChatMessage>()
    private var conversations = mutableListOf<ChatChannelIdWrapper>()
    private var conversationList: MutableLiveData<List<ChatChannelIdWrapper>> = MutableLiveData()

    fun updateToken(token: String?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = token?.let { Token(it) }
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    fun listenForNewMessage(): LiveData<List<ChatChannelIdWrapper>> {
        Firebase.firestore.collection("ChatChannels").whereArrayContains(
            "between", Id(FirebaseAuth.getInstance().currentUser?.uid)
        )
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (documents != null) {
                    for (document in documents) {
                        conversations.add(
                            ChatChannelIdWrapper(
                                document.id, document.toObject(
                                    ChatChannelId::class.java
                                )
                            )
                        )

                    }
                } else {
                    Log.d("failed", "Current data: null")
                }
                conversationList.value = conversations
            }
        return conversationList
    }

    fun displayUsers(conversations: List<ChatChannelIdWrapper>): LiveData<List<ChatMessage>> {
        conversations.forEach {
            repository.getChatReference()?.document(it.docId)?.collection("Messages")?.orderBy(
                "timestamp",
                com.google.firebase.firestore.Query.Direction.DESCENDING
            )?.limit(1)
                ?.addSnapshotListener { documents, _ ->
                    if (documents != null) {
                        for (doc in documents) {
                            users.add(doc.toObject(ChatMessage::class.java))
                        }
                    }
                    activeUsers.value = users
                }
        }
        return activeUsers
    }


}