package com.example.vcare.home.newMessage

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.vcare.helper.ChatRepository
import com.example.vcare.helper.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

class NewMessageViewmodel@ViewModelInject constructor(
    private val repository: ChatRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

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