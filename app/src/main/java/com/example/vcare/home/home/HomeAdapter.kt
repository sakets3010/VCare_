package com.example.vcare.home.home

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.vcare.R
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.Status
import com.example.vcare.helper.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.home_list_item.view.*

class HomeAdapter(private val chatMessages: List<ChatMessage>,private val listener: (User?) -> Unit) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_list_item,parent,false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int = chatMessages.size

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        holder.setIsRecyclable(false)
        holder.latestMessage.text=chatMessage.text
        val chatPartnerId:String = if(chatMessage.fromId== FirebaseAuth.getInstance().uid){
            chatMessage.toId
        } else{
            chatMessage.fromId
        }
        
        Firebase.firestore.collection("Users").document(chatPartnerId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("HomeFragment", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val chatPartner = snapshot.toObject(User::class.java)
                holder.userName.text = chatPartner?.username
                holder.homeEntry.setOnClickListener {
                    listener(chatPartner)
                }
                if(chatPartner?.category=="Seeker")
                {
                    holder.category.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
                    holder.category.setTextColor(Color.parseColor("#fdd835"))
                }
                holder.category.text = chatPartner?.category
                if(chatPartner?.status==Status.ONLINE)
                {holder.onlineStatus.visibility = View.VISIBLE}
                else if(chatPartner?.status==Status.OFFLINE)
                {holder.onlineStatus.visibility = View.GONE}
                Picasso.get().load(chatPartner?.profileImageUrl).into(holder.profile)
            } else {
                Log.d("HomeFragment", "Current data: null")
            }
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val userName:TextView=itemView.home_username
        val latestMessage:TextView=itemView.home_latestMessage
        val category:TextView=itemView.category_text
        val profile:ImageView=itemView.home_profile
        val onlineStatus:ImageView=itemView.online_status_home
        val homeEntry:ConstraintLayout = itemView.home_entry
    }

}