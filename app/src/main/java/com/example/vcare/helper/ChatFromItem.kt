package com.example.vcare.helper

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.View
import com.example.vcare.R
import com.example.vcare.ViewFullImageActivity
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_from.view.*

class ChatFromItem(val text:String="",val url:String="",val user:User?,time:String?):
    Item<ViewHolder>(){

    val time_:String?

    init{
        this.time_ = time
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_from
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {


        viewHolder.setIsRecyclable(false)
        if(url=="")
        {
            viewHolder.itemView.textView_from_row.text = text
            viewHolder.itemView.timestamp_from.text =time_
            Log.d("time","time:$time_")
        }
        else if(url !== "")
        {
            viewHolder.itemView.textView_from_row.visibility= View.GONE
            viewHolder.itemView.image_from_cover.visibility= View.VISIBLE
            viewHolder.itemView.image_from.visibility = View.VISIBLE
            Picasso.get().load(url).into(viewHolder.itemView.image_from)

            viewHolder.itemView.image_from.setOnClickListener {
                val options = arrayOf<CharSequence>(
                    "View Image","Cancel"
                )

                val builder: AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
                builder.setTitle("what now?")
                builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                    if (which==0){
                        val intent = Intent(builder.context, ViewFullImageActivity::class.java)
                        intent.putExtra("url",url)
                        builder.context.startActivity(intent)
                    }

                })
                builder.show()
            }
        }


        val uri = user?.profileImageUrl
        val targetImage = viewHolder.itemView.from_profile
        Picasso.get().load(uri).into(targetImage)

    }
}
