package com.example.vcare.home.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.vcare.ChatRepository
import com.example.vcare.Notifications.Token
import com.example.vcare.helper.*
import com.example.vcare.helper.groupieAdapters.HomeItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class HomeFragmentViewmodel:ViewModel() {
    val adapter = GroupAdapter<ViewHolder>()
    private val repository = ChatRepository()
    fun updateToken(token: String?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = token?.let { Token(it) }
        ref.child(firebaseUser!!.uid).setValue(token1)
    }
    fun listenForNewMessage(){
        val conversations = mutableListOf<ChatChannelIdWrapper>()

        Firebase.firestore.collection("ChatChannels").whereArrayContains("between",
            Id(FirebaseAuth.getInstance().currentUser?.uid)
        )
            .addSnapshotListener{ documents, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if ( documents != null) {
                    for (document in documents) {
                        Log.d("listen","loop1 : ${document.id}")
                        conversations.add(
                            ChatChannelIdWrapper(document.id,document.toObject(
                                ChatChannelId::class.java))
                        )
                        adapter.clear()
                        displayUsers(conversations)
                    }
                } else {
                    Log.d("failed", "Current data: null")
                }
            }
        }
    private val latestMessagesMap = HashMap<String, ChatMessage>()
    private fun displayUsers(conversations:MutableList<ChatChannelIdWrapper>) {
        adapter.clear()
        conversations.forEach {
            Log.d("listen","loop2 : $it")
            adapter.clear()
            latestMessagesMap.values.clear()
            repository.getChatReference()?.document(it.docId)?.collection("Messages")?.orderBy("timestamp",
                com.google.firebase.firestore.Query.Direction.DESCENDING)?.limit(1)
                ?.addSnapshotListener { documents, _ ->
                    if (documents != null) {
                        for(doc in documents){
                            latestMessagesMap[it.docId] = doc.toObject(ChatMessage::class.java)
                            adapter.clear()
                            refreshMessages()
                        }
                    }
                }
        }
    }
    private fun refreshMessages() {
        Log.d("display","displaying item")
        latestMessagesMap.values.forEach {
            adapter.add(HomeItem(it))
        }
    }

}