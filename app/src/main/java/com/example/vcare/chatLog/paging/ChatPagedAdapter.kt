package com.example.vcare.chatLog.paging

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.vcare.R
import com.example.vcare.chatLog.ViewFullImageActivity
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.convertDurationToFormatted
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat_row_list.view.imageCover
import kotlinx.android.synthetic.main.chat_row_list.view.imageMessage
import kotlinx.android.synthetic.main.chat_row_list.view.messageTimestamp
import kotlinx.android.synthetic.main.chat_row_list.view.textMessage
import kotlinx.android.synthetic.main.chat_row_to.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatPagedAdapter : PagingDataAdapter<ChatMessage, RecyclerView.ViewHolder>(Companion) {
    companion object : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
        const val VIEW_TYPE_1 = 1
        const val VIEW_TYPE_2 = 2
    }
    private inner class View1ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView = itemView.textMessage
        val messageTimestamp: TextView = itemView.messageTimestamp
        val imageMessage: ImageView = itemView.imageMessage
        val imageCover: ImageView = itemView.imageCover
        val deliveredReceipt: ImageView = itemView.delivered_image
        fun bind(item:ChatMessage,itemPrev:ChatMessage,position: Int) {
            setIsRecyclable(false)
            if(item.status){
                deliveredReceipt.visibility = View.VISIBLE
            }

            if (item.url == "") {
                textMessage.text = item.text
                Log.d("return","text1:${item.text}")
                if (convertDurationToFormatted(
                        itemPrev.timestamp * 1000,
                        item.timestamp * 1000
                    ) || itemPrev == item
                ) {
                    messageTimestamp.visibility = View.VISIBLE
                    messageTimestamp.text = getDateTime(item.timestamp)
                } else {
                    messageTimestamp.visibility = View.GONE
                }

            } else if (item.url !== "") {
                textMessage.visibility = View.GONE
                messageTimestamp.visibility = View.GONE
                Picasso.get().load(item.url).into(imageMessage)
                imageCover.visibility = View.VISIBLE
                imageMessage.visibility = View.VISIBLE
                imageMessage.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Image", "Cancel"
                    )

                    val builder: AlertDialog.Builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle("what now?")
                    builder.setItems(options) { _, which ->
                        if (which == 0) {
                            val intent = Intent(builder.context, ViewFullImageActivity::class.java)
                            intent.putExtra("url", item.url)
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
        val textMessage: TextView = itemView.textMessage
        val messageTimestamp: TextView = itemView.messageTimestamp
        val imageMessage: ImageView = itemView.imageMessage
        val imageCover: ImageView = itemView.imageCover
        fun bind(item:ChatMessage,itemPrev:ChatMessage,position: Int) {
            setIsRecyclable(false)


            if (item.url == "") {
                textMessage.text = item.text
                Log.d("return","text2:${item.text}")
                if (convertDurationToFormatted(
                        itemPrev.timestamp * 1000,
                        item.timestamp * 1000
                    ) || itemPrev == item
                ) {
                    messageTimestamp.visibility = View.VISIBLE
                    messageTimestamp.text = getDateTime(item.timestamp)
                }
            } else if (item.url !== "") {
                textMessage.visibility = View.GONE
                messageTimestamp.visibility = View.GONE
                Picasso.get().load(item.url).into(imageMessage)
                imageCover.visibility = View.VISIBLE
                imageMessage.visibility = View.VISIBLE
                imageMessage.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Image", "Cancel"
                    )

                    val builder: AlertDialog.Builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle("what now?")
                    builder.setItems(options) { _, which ->
                        if (which == 0) {
                            val intent = Intent(builder.context, ViewFullImageActivity::class.java)
                            intent.putExtra("url", item.url)
                            builder.context.startActivity(intent)
                        }
                    }
                    builder.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_1) {
            Log.d("return","first viewholder called")
            return View1ViewHolder(

                LayoutInflater.from(parent.context).inflate(R.layout.chat_row_to, parent, false)
            )
        }
        Log.d("return","second viewholder called")
        return View2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chat_row_from, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.fromId == Firebase.auth.uid) {
            VIEW_TYPE_1
        } else {
            VIEW_TYPE_2
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val itemPrev = if (position !== 0) {
            getItem(position-1)
        } else getItem(position)
        if (item?.fromId == Firebase.auth.uid) {
            Log.d("return","first viewholder set")
            (holder as ChatPagedAdapter.View1ViewHolder).bind(item!!,itemPrev!!,position)
        } else {
            Log.d("return","second viewholder set")
            (holder as ChatPagedAdapter.View2ViewHolder).bind(item!!,itemPrev!!,position)
        }
    }
    private fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("EEE,hh:mmaa", Locale.getDefault())
            val netDate = Date(s * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }
}

