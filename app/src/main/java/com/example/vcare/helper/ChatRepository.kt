package com.example.vcare.helper

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatRepository {

    fun getUserReference(uid:String): DocumentReference? {
        return Firebase.firestore.collection("Users").document(uid)
    }
    fun getChatReference(): CollectionReference? {
        return Firebase.firestore.collection("ChatChannels")
    }
}