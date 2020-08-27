package com.example.vcare.helper

import android.app.AlertDialog
import android.content.Intent
import android.view.View
import com.example.vcare.R
import com.example.vcare.ViewFullImageActivity
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_to.view.*

class ChatToItem(val text: String = "",
    private val urlimg: String = "",
    val user: User?,
    private val time: String? = ""):Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_row_to
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.setIsRecyclable(false)
        if(urlimg=="")
        {
            viewHolder.itemView.textView_to_row.text =text
            viewHolder.itemView.timestamp_to.text =this.time
        }
        else if(urlimg !== "")
        {
            viewHolder.itemView.textView_to_row.visibility= View.GONE
            viewHolder.itemView.image_to_cover.visibility= View.VISIBLE
            viewHolder.itemView.image_to.visibility = View.VISIBLE
            Picasso.get().load(urlimg).into(viewHolder.itemView.image_to)
            viewHolder.itemView.image_to.setOnClickListener {
                val options = arrayOf<CharSequence>(
                    "View Image","Cancel"
                )

                val builder: AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
                builder.setTitle("what now?")
                builder.setItems(options) { _, which ->
                    if (which==0){
                        val intent = Intent(builder.context, ViewFullImageActivity::class.java)
                        intent.putExtra("url",urlimg)
                        builder.context.startActivity(intent)
                    }
                }
                builder.show()
            }
        }
    }
}