package com.example.vcare

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Status {

    private val db = Firebase.firestore
    companion object{
        const val ONLINE = 100L
        const val ONLINE_AND_TYPING = 101L
        const val OFFLINE = 102L
    }
    fun updateStatus(userId:String,status:Long){
        db.collection("Users").document(userId).update(
            mapOf(
                "status" to status
            )
        )
    }
    fun updateBio(userId:String,Bio:String){
        db.collection("Users").document(userId).update(
            mapOf(
                "Bio" to Bio
            )
        )
    }
}