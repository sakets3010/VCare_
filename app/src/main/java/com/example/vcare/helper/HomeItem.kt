package com.example.vcare.helper

import android.graphics.Color
import android.util.Log
import android.view.View
import com.example.vcare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.home_list.view.*

class HomeItem(private val chatMessage:ChatMessage): Item<ViewHolder>() {

    var chatPartner:User?=null
    override fun getLayout(): Int {
        return R.layout.home_list
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.setIsRecyclable(false)
        viewHolder.itemView.home_latestMessage.text=chatMessage.text

        val chatPartnerId:String = if(chatMessage.fromId== FirebaseAuth.getInstance().uid){
            chatMessage.toId
        } else{
            chatMessage.fromId
        }
        val db = Firebase.firestore
        db.collection("Users").document(chatPartnerId).addSnapshotListener {snapshot, e ->
            if (e != null) {
                Log.w("HomeFragment", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                chatPartner = snapshot.toObject(User::class.java)
                viewHolder.itemView.home_username.text = chatPartner?.username
                if(chatPartner?.category=="Seeker")
                {
                    viewHolder.itemView.category_text.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
                    viewHolder.itemView.category_text.setTextColor(Color.parseColor("#ffff00"))
                }
                viewHolder.itemView.category_text.text = chatPartner?.category
                if(chatPartner?.status==100L)
                {viewHolder.itemView.online_status_home.visibility = View.VISIBLE}
                else if(chatPartner?.status==102L){
                    viewHolder.itemView.online_status_home.visibility = View.GONE
                }
                val targetImage =  viewHolder.itemView.home_profile
                Picasso.get().load(chatPartner?.profileImageUrl).into(targetImage)
            } else {
                Log.d("HomeFragment", "Current data: null")
            }
        }
    }
}