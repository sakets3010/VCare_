package com.example.vcare.helper

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Status {
    companion object{
        const val ONLINE = 100L
        const val ONLINE_AND_TYPING = 101L
        const val OFFLINE = 102L
    }
}