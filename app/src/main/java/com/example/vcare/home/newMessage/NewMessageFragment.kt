package com.example.vcare.home.newMessage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vcare.R
import com.example.vcare.chatLog.ChatLogActivity
import com.example.vcare.helper.User
import com.example.vcare.helper.groupieAdapters.UserItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_new_message.*

class NewMessageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fetchUsers()
        return inflater.inflate(R.layout.fragment_new_message, container, false)
    }
    companion object {
        const val USER_KEY = "USER_KEY"
    }
    private fun fetchUsers() {
        val db = Firebase.firestore
        db.collection("Users").addSnapshotListener{snapshot,e ->
            if (e != null) {
                Log.w("NewMessageFragment", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val adapter = GroupAdapter<ViewHolder>()
                 snapshot.documents.forEach {
                     val user = it.toObject(User::class.java)
                     if(user!==null&& user.uid !== FirebaseAuth.getInstance().currentUser?.uid){
                         adapter.add(UserItem(user))
                    }
                 }
                adapter.setOnItemClickListener { item, view ->
                    val userItem: UserItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                }
                recyclerview_newMessage?.adapter=adapter
            } else {
                Log.d("NewMessageFragment", "Current data: null")
            }
        }
    }
}


