package com.example.vcare

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vcare.helper.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_new_message.*
import kotlinx.android.synthetic.main.new_message_list.view.*

class NewMessageFrag : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fetchUsers()
        return inflater.inflate(R.layout.fragment_new_message, container, false)
    }
    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {


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
                }

                recyclerview_newMessage.adapter=adapter
            }
        })
    }

    override fun onResume() {
        super.onResume()
        HomeActivity.Status.updateStatus("online")
    }

    override fun onPause(){
        super.onPause()
        HomeActivity.Status.updateStatus("offline")
    }


}
class UserItem(val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_message_list
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_list.text = user.username
        if(user.category=="Seeker")
        {
            viewHolder.itemView.category_text_new_message.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
            viewHolder.itemView.category_text_new_message.setTextColor(Color.parseColor("#ffff00"))
        }
        viewHolder.itemView.category_text_new_message.text=user.category
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

