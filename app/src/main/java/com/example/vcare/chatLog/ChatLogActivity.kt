package com.example.vcare.chatLog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.vcare.Notifications.ApiService
import com.example.vcare.Notifications.Client
import com.example.vcare.R
import com.example.vcare.helper.Status
import com.example.vcare.helper.User
import com.example.vcare.home.HomeActivity
import com.example.vcare.home.NewMessageFrag
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat_log.*


class ChatLogActivity : AppCompatActivity() {
    private val viewModel by viewModels<ChatLogViewmodel>()
    private var apiService : ApiService?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        chat_log_recycler.scrollToPosition(viewModel.adapter.itemCount -1)
        val user = intent.getParcelableExtra<User>(NewMessageFrag.USER_KEY)
        val toId = user?.uid
        setUi(user)
        viewModel.evaluateStatus(user).observe(this, {
            if(it.status == Status.ONLINE){
                online_indicator.setImageResource(R.drawable.online_color)
                typing_status_chat_log.text=getString(R.string.online)
            }
            else if (it.status == Status.OFFLINE){
               online_indicator.setImageResource(R.drawable.hollow_circle)
               typing_status_chat_log.text=getString(R.string.away)
            }
            else if (it.status== Status.ONLINE_AND_TYPING && it.uid==toId){
               online_indicator.setImageResource(R.drawable.online_color)
               typing_status_chat_log.text=getString(R.string.typing)
            }
        })

        back_button_chat_log.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        chat_log_profile.setOnClickListener {
            Log.d("scroll","item count:${viewModel.adapter.itemCount}")
            chat_log_recycler.scrollToPosition(viewModel.adapter.itemCount -1)
        }

        viewModel.isScrollable.observe(this, Observer {
            if(it){
                chat_log_recycler.scrollToPosition(viewModel.adapter.itemCount-1)
            }
        })

        setRecycler()

        if (user != null) {
            viewModel.listenForMessages(user)
        }

        apiService = Client.client.getClient("https://fcm.googleapis.com/")!!.create(
            ApiService::class.java)

        send_button.setOnClickListener {
            if(edittext_chat_log.text.toString()==""){
                edittext_chat_log.requestFocus()
                return@setOnClickListener
            }
            else{
                viewModel.notify = true
                Log.d("scroll","present position:${viewModel.adapter.itemCount}")
                chat_log_recycler.scrollToPosition(viewModel.adapter.itemCount -1)
                viewModel.performSendMessage(user,edittext_chat_log.text.toString(),apiService)
                Log.d("scroll","present position:${viewModel.adapter.itemCount}")
                edittext_chat_log.text.clear()
            }
        }
        edittext_chat_log.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(edittext_chat_log.text.toString().trim().isNotEmpty())
                {
                    if (toId != null) {
                        viewModel.updateTypingStatus(Status.ONLINE_AND_TYPING)
                    }
                }
                else
                {
                   viewModel.updateTypingStatus(Status.ONLINE)
                }
            }
        })
        send_image.setOnClickListener {
            viewModel.notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"pick an image"),438)
        }

    }

    private fun setRecycler() {
        chat_log_recycler.setHasFixedSize(true)
        chat_log_recycler.adapter = viewModel.adapter
    }

    private fun setUi(user: User?) {
        if(viewModel.returnUser(user)?.category =="Seeker")
        {
            category_text_chat_log.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
            category_text_chat_log.setTextColor(Color.parseColor("#ffff00"))
        }
        username_chat_log?.text = viewModel.returnUser(user)?.username
        Picasso.get().load(viewModel.returnUser(user)?.profileImageUrl).into(chat_log_profile)
        category_text_chat_log.text= user?.category
    }
    override fun onResume() {
        super.onResume()
        Log.d("resume","resume called")
        chat_log_recycler.scrollToPosition(viewModel.adapter.itemCount -1)
        HomeActivity.Status.updateStatus(FirebaseAuth.getInstance().currentUser?.uid.toString(),
            Status.ONLINE)
    }

    override fun onPause() {
        super.onPause()
        HomeActivity.Status.updateStatus(FirebaseAuth.getInstance().currentUser?.uid.toString(),
            Status.OFFLINE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==438 && resultCode == RESULT_OK && data!!.data!==null && data !==null){
            val user = intent.getParcelableExtra<User>(NewMessageFrag.USER_KEY)
            if (user != null) {
                viewModel.imageMessage(data,user,apiService!!)
            }
        }
    }



}
