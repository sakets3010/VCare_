package com.example.vcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.vcare.HomeActivity.Status.Companion.updateStatus
import com.example.vcare.helper.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.new_message_list.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Connect with Users"

        fetchUsers()


    }
    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object:ValueEventListener {


            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if(user!==null){
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem:UserItem = item as UserItem
                    val intent = Intent(view.context,ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                    finish()

                }

                recyclerview_newMessage.adapter=adapter
            }
        })
    }

    override fun onResume() {
        super.onResume()
         updateStatus("online")
    }

    override fun onPause(){
        super.onPause()
        updateStatus("offline")
    }


}
class UserItem(val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_message_list
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_list.text = user.username
        if(user.status=="online"){
            viewHolder.itemView.online_status_new_message.visibility = View.VISIBLE
        }
        else if(user.status=="offline"){
            viewHolder.itemView.online_status_new_message.visibility = View.GONE
        }
        if(user.profileImageUrl.isNotEmpty())
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.profile_list)
    }


}