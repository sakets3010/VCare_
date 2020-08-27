package com.example.vcare.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vcare.helper.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragmentViewmodel: ViewModel() {
    var userDetails:MutableLiveData<User> = MutableLiveData()

    fun fetchUserDetails(uid:String):LiveData<User>{
        Firebase.firestore.collection("Users").document(uid).addSnapshotListener { doc, error ->
            val user = doc?.toObject(User::class.java)
            userDetails.value = user
        }
        return userDetails
    }
    fun updateUserDetails(uid:String,Bio:String){
        Firebase.firestore.collection("Users").document(uid).update(
            mapOf(
                "bio" to Bio
            )
        )
    }

}