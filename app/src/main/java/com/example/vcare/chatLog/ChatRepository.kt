package com.example.vcare.chatLog

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatRepository {

    private val uid = Firebase.auth.currentUser?.uid

    fun getUserReference(): DocumentReference? {
        return if(uid != null)
            Firebase.firestore.collection("Users").document(uid)
        else null
    }
    fun getChatReference(id:String): CollectionReference? {
        return if(uid != null)
            Firebase.firestore.collection("ChatChannels").document(id).collection("Messages")
        else null
    }
}