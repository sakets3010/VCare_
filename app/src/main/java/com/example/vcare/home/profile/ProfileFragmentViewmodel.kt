package com.example.vcare.home.profile

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.vcare.helper.ChatRepository
import com.example.vcare.helper.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragmentViewmodel@ViewModelInject constructor(
    private val repository: ChatRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private var _userDetails:MutableLiveData<User> = MutableLiveData()
    val userDetails:LiveData<User>
    get() = _userDetails

    init {
        Firebase.auth.uid?.let { fetchUserDetails(it) }
    }

    private fun fetchUserDetails(uid:String):LiveData<User>{
        repository.getUserReference(uid)?.addSnapshotListener { doc, _ ->
            val user = doc?.toObject(User::class.java)
            _userDetails.value = user
        }
        return _userDetails
    }
    fun updateUserDetails(uid:String, bio:String){
        repository.getUserReference(uid)?.update(
            mapOf(
                "bio" to bio
            )
        )
    }

}
