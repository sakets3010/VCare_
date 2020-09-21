package com.example.vcare.home.newMessage

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.vcare.R
import com.example.vcare.helper.Status
import com.example.vcare.helper.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.new_message_list.view.*

class NewMessageAdapter(private val users: List<User>,private val listener: (User?) -> Unit) : RecyclerView.Adapter<NewMessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.new_message_list,parent,false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        val user = users[position]
        holder.username.text = user.username
        holder.userBio.text = user.bio
        if(user.category=="Seeker")
        {
            holder.category.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
            holder.category.setTextColor(Color.parseColor("#fdd835"))
        }

        holder.category.text=user.category
        if(user.status== Status.ONLINE){
            holder.onlineStatus.visibility = View.VISIBLE
        }
        else if(user.status==Status.OFFLINE){
            holder.onlineStatus.visibility = View.GONE
        }
        if(user.profileImageUrl.isNotEmpty())
            Picasso.get().load(user.profileImageUrl).into(holder.profile)
        holder.userEntry.setOnClickListener {
            listener(user)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val username: TextView =itemView.username_list
        val userBio: TextView =itemView.user_bio
        val category: TextView =itemView.category_text_new_message
        val profile:ImageView = itemView.profile_list
        val onlineStatus: ImageView =itemView.online_status_new_message
        val userEntry: ConstraintLayout =itemView.user_entry
    }

}