package com.example.vcare.home.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vcare.ChatRepository
import com.example.vcare.helper.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragmentViewmodel: ViewModel() {
    private var userDetails:MutableLiveData<User> = MutableLiveData()
    private val repository = ChatRepository()

    fun fetchUserDetails(uid:String):LiveData<User>{
        repository.getUserReference(uid)?.addSnapshotListener { doc, _ ->
            val user = doc?.toObject(User::class.java)
            userDetails.value = user
        }
        return userDetails
    }
    fun updateUserDetails(uid:String,Bio:String){
        repository.getUserReference(uid)?.update(
            mapOf(
                "bio" to Bio
            )
        )
    }

}