package com.example.vcare.helper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.example.vcare.R
import com.example.vcare.ViewFullImageActivity
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_to.view.*

class ChatToItem(val text:String="",val urlimg:String="", val user: User?,time:String?,from:String,to:String):
    Item<ViewHolder>(){

    var fromId:String
    var toId:String
    var time:String?

    init {
        this.fromId = from
        this.toId = to
        this.time = time


    }
    override fun getLayout(): Int {
        return R.layout.chat_row_to
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.setIsRecyclable(false)
        if(urlimg=="")
        {
            viewHolder.itemView.textView_to_row.text =text
            viewHolder.itemView.timestamp_to.text =time
            viewHolder.itemView.textView_to_row.setOnClickListener {
                val options = arrayOf<CharSequence>(
                    "Delete Message","Cancel"
                )

                val builder: AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
                builder.setTitle("what now?")
                builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                    if(which == 0){
                        deleteMessage(position,viewHolder)

                    }
                })
                builder.show()
            }}
        else if(urlimg !== "")
        {
            viewHolder.itemView.textView_to_row.visibility= View.GONE
            viewHolder.itemView.image_to_cover.visibility= View.VISIBLE
            viewHolder.itemView.image_to.visibility = View.VISIBLE
            Picasso.get().load(urlimg).into(viewHolder.itemView.image_to)
            viewHolder.itemView.image_to.setOnClickListener {
                val options = arrayOf<CharSequence>(
                    "View Image","Delete Message","Cancel"
                )

                val builder: AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
                builder.setTitle("what now?")
                builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                    if (which==0){
                        val intent = Intent(builder.context, ViewFullImageActivity::class.java)
                        intent.putExtra("url",urlimg)
                        builder.context.startActivity(intent)
                    }
                    else if(which == 1){
                        deleteMessage(position,viewHolder)
                    }
                })
                builder.show()
            }
        }

        val uri = user?.profileImageUrl
        val targetImage = viewHolder.itemView.to_profile
        Picasso.get().load(uri).into(targetImage)
    }

    @SuppressLint("SetTextI18n")
    private fun deleteMessage(position: Int, viewHolder: ViewHolder) {
        val hashMap = HashMap<String,Any>()
        val progressBar = ProgressDialog(viewHolder.itemView.context)
        progressBar.setMessage("deleting message..")
        progressBar.show()
        val deletionrefupdate =
            time?.let {
                FirebaseDatabase.getInstance().reference.child("user-messages").child(fromId).child(toId).child(
                    it
                )
            }
        val to_deletionrefupdate =
            time?.let {
                FirebaseDatabase.getInstance().reference.child("user-messages").child(toId).child(fromId).child(
                    it
                )
            }
        if(urlimg=="")
        {
            hashMap["text"] = "This message was deleted"

        }
        else{
            hashMap["url"] = ""
            hashMap["text"] = "This message was deleted"
        }
        deletionrefupdate?.updateChildren(hashMap)
        to_deletionrefupdate?.updateChildren(hashMap)
        notifyChanged()
        Toast.makeText(viewHolder.itemView.context,"deleted message", Toast.LENGTH_SHORT).show()

    }
}