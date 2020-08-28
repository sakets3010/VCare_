package com.example.vcare.helper.groupieAdapters

import android.graphics.Color
import android.view.View
import com.example.vcare.R
import com.example.vcare.helper.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.new_message_list.view.*

class UserItem(val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_message_list
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_list.text = user.username
        viewHolder.itemView.user_bio.text = user.bio
        if(user.category=="Seeker")
        {
            viewHolder.itemView.category_text_new_message.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
            viewHolder.itemView.category_text_new_message.setTextColor(Color.parseColor("#ffff00"))
        }

        viewHolder.itemView.category_text_new_message.text=user.category
        if(user.status==100L){
            viewHolder.itemView.online_status_new_message.visibility = View.VISIBLE
        }
        else if(user.status==102L){
            viewHolder.itemView.online_status_new_message.visibility = View.GONE
        }
        if(user.profileImageUrl.isNotEmpty())
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.profile_list)
    }


}