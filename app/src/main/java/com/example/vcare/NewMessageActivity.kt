package com.example.vcare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.vcare.helper.User
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


        fetchUsers()

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
                recyclerview_newMessage.adapter=adapter
            }
        })
    }


}
class UserItem(val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_message_list
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_list.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.profile_list)
    }


}