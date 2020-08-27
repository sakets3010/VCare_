package com.example.vcare.home
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.vcare.R
//import com.example.vcare.helper.ChatMessage
//import kotlinx.android.synthetic.main.home_list.view.*
//
//class UsersAdapter(private val chatMessage:MutableList<ChatMessage>) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.home_list,parent,false)
//            return ViewHolder(view)
//        }
//        override fun getItemCount(): Int = chatMessage.size
//
//        override fun onBindViewHolder(holder:ViewHolder, position: Int) {
//            val chat = chatMessage[position]
//            holder.userName.text=chat.
//            holder.lastmessage.text=user.firstName
//            holder.category.text=user.firstName
//            holder.lastName.text=user.lastName
//        }
//
//        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
//        {
//            val userName: TextView =itemView.home_username
//            val lastmessage: TextView =itemView.home_latestMessage
//            val category: TextView = itemView.category_text
//            val image :ImageView = itemView.home_profile
//        }
//
//    }
