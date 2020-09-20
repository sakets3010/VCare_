package com.example.vcare.home.newMessage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vcare.helper.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NewMessageViewmodel : ViewModel() {

    private val _availableUsers: MutableLiveData<List<User>> = MutableLiveData()
    val availableUsers: LiveData<List<User>>
        get() = _availableUsers

    private fun fetchUsers() {
        val db = Firebase.firestore
        db.collection("Users").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("NewMessageFragment", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = mutableListOf<User>()
                snapshot.documents.forEach {
                    val user = it.toObject(User::class.java)
                    if (user !== null && !(user.uid.equals(Firebase.auth.uid))) {
                        users.add(user)
                    }
                }
                _availableUsers.value = users
            }
        }
    }

    init {
        fetchUsers()
    }
}