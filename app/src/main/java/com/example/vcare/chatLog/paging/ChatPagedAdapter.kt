package com.example.vcare.chatLog.paging

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.vcare.R
import com.example.vcare.chatLog.ViewFullImageActivity
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.convertDurationToFormatted
import com.example.vcare.settings.SettingsFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat_row_from.view.*
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
        const val URL = "url"
    }

    private inner class View1ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView = itemView.textMessage
        val messageTimestamp: TextView = itemView.messageTimestamp
        val imageMessage: ImageView = itemView.imageMessage
        val imageCover: ImageView = itemView.imageCover
        val deliveredReceipt: ImageView = itemView.delivered_image
        fun bind(item: ChatMessage, itemPrev: ChatMessage) {
            setIsRecyclable(false)
            if (item.status) {
                deliveredReceipt.visibility = View.VISIBLE
            }

            if (item.url == "") {
                textMessage.text = item.text
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
                            intent.putExtra(URL, item.url)
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
        val textMessage: TextView = itemView.textMessageFrom
        val messageTimestamp: TextView = itemView.messageTimestampFrom
        val imageMessage: ImageView = itemView.imageMessageFrom
        val imageCover: ImageView = itemView.imageCoverFrom

        @SuppressLint("ResourceAsColor")
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bind(item: ChatMessage, itemPrev: ChatMessage) {
            val sharedPref = itemView.context.getSharedPreferences("Vcare", Context.MODE_PRIVATE)
            Log.d("color", "value:${sharedPref.getLong("theme", 2L)}")
            when (sharedPref.getLong("theme", 1L)) {
                SettingsFragment.THEME_1 -> {
                    textMessage.setBackgroundResource(R.drawable.theme_1_tv)
                    imageCover.setBackgroundResource(R.drawable.theme_1_tv)
                }
                SettingsFragment.THEME_2 -> {
                    textMessage.setBackgroundResource(R.drawable.theme_2_tv)
                    imageCover.setBackgroundResource(R.drawable.theme_2_tv)
                }
                SettingsFragment.THEME_3 -> {
                    textMessage.setBackgroundResource(R.drawable.theme_3_tv)
                    imageCover.setBackgroundResource(R.drawable.theme_3_tv)
                }
                SettingsFragment.THEME_4 -> {
                    textMessage.setBackgroundResource(R.drawable.theme_4_tv)
                    imageCover.setBackgroundResource(R.drawable.theme_4_tv)
                }
            }
            setIsRecyclable(false)
            if (item.url == "") {
                textMessage.text = item.text
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
                            intent.putExtra(URL, item.url)
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

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.fromId == Firebase.auth.uid) {
            VIEW_TYPE_1
        } else {
            VIEW_TYPE_2
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val itemPrev = if (!position.equals(itemCount - 1)) {
            getItem(position + 1)
        } else getItem(position)
        if (item?.fromId == Firebase.auth.uid) {
            (holder as ChatPagedAdapter.View1ViewHolder).bind(item!!, itemPrev!!)
        } else {
            (holder as ChatPagedAdapter.View2ViewHolder).bind(item!!, itemPrev!!)
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

