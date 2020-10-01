package com.example.vcare.home.profile

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.vcare.helper.ChatRepository
import com.example.vcare.helper.User

class ProfileFragmentViewmodel@ViewModelInject constructor(
    private val repository: ChatRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private var _userDetails:MutableLiveData<User> = MutableLiveData()

    fun fetchUserDetails(uid:String):LiveData<User>{
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