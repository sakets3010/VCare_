package com.example.vcare

import com.example.vcare.helper.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

//object FirestoreUtil {
//    private val db: FirebaseFirestore by lazy { Firebase.firestore }
//
//    private val currentUserDocRef: DocumentReference = db.document("users/${FirebaseAuth.getInstance().currentUser?.uid}")
//    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
//        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
//            if (!documentSnapshot.exists()) {
//                val newUser = User(FirebaseAuth.getInstance().currentUser?.uid,.)
//                currentUserDocRef.set(newUser).addOnSuccessListener {
//                    onComplete()
//                }
//            }
//            else
//                onComplete()
//        }
//    }
//}