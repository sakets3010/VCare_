package com.example.vcare.chatLog


import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vcare.R
import com.example.vcare.helper.ChatMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat_row_list.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val message: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEW_TYPE_1 = 1
        const val VIEW_TYPE_2 = 2
    }

    private inner class View1ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView =itemView.textMessage
        val messageTimestamp: TextView =itemView.messageTimestamp
        val imageMessage: ImageView =itemView.imageMessage
        val imageCover:ImageView =itemView.imageCover
        fun bind(position: Int) {
            setIsRecyclable(false)
            val item = message[position]
            if(item.url==""){
                textMessage.text = item.text
                messageTimestamp.text = getDateTime(item.timestamp)
            }
            else if(item.url !== ""){
                textMessage.visibility= View.GONE
                messageTimestamp.visibility =View.GONE
                Picasso.get().load(item.url).into(imageMessage)
                imageCover.visibility= View.VISIBLE
                imageMessage.visibility = View.VISIBLE
                imageMessage.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Image","Cancel"
                    )

                    val builder: AlertDialog.Builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle("what now?")
                    builder.setItems(options) { _, which ->
                        if (which==0){
                            val intent = Intent(builder.context, ViewFullImageActivity::class.java)
                            intent.putExtra("url",item.url)
                            builder.context.startActivity(intent)
                        }
                    }
                    builder.show()
                }
            }
        }
    }

    private inner class View2ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView =itemView.textMessage
        val messageTimestamp: TextView =itemView.messageTimestamp
        val imageMessage: ImageView =itemView.imageMessage
        val imageCover:ImageView =itemView.imageCover
        fun bind(position: Int) {
            setIsRecyclable(false)
            val item = message[position]
            if(item.url==""){
                textMessage.text = item.text
                messageTimestamp.text = getDateTime(item.timestamp)
            }
            else if(item.url !== ""){
                textMessage.visibility= View.GONE
                messageTimestamp.visibility =View.GONE
                Picasso.get().load(item.url).into(imageMessage)
                imageCover.visibility= View.VISIBLE
                imageMessage.visibility = View.VISIBLE
                imageMessage.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Image","Cancel"
                    )

                    val builder: AlertDialog.Builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle("what now?")
                    builder.setItems(options) { _, which ->
                        if (which==0){
                            val intent = Intent(builder.context, ViewFullImageActivity::class.java)
                            intent.putExtra("url",item.url)
                            builder.context.startActivity(intent)
                        }
                    }
                    builder.show()
                }
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_1) {
            return View1ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.chat_row_to, parent, false)
            )
        }
        return View2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chat_row_from, parent, false)
        )
    }

    override fun getItemCount(): Int = message.size

    private fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("EEE,hh:mmaa", Locale.getDefault())
            val netDate = Date(s * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (message[position].fromId == Firebase.auth.uid) {
            (holder as View1ViewHolder).bind(position)
        } else {
            (holder as View2ViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(message[position].fromId == Firebase.auth.uid)
        {
            VIEW_TYPE_1
        }
        else{
            VIEW_TYPE_2
        }
    }


}