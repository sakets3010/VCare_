package com.example.vcare.chatLog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.vcare.Notifications.ApiService
import com.example.vcare.Notifications.Client
import com.example.vcare.R
import com.example.vcare.databinding.ActivityChatLogBinding
import com.example.vcare.helper.Status
import com.example.vcare.helper.User
import com.example.vcare.home.HomeActivity
import com.example.vcare.home.newMessage.NewMessageFragment
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat_log.*


class ChatLogActivity : AppCompatActivity() {
    private val viewModel by viewModels<ChatLogViewmodel>()
    private var apiService : ApiService?=null
    private lateinit var binding: ActivityChatLogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val user = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)
        val toId = user?.uid

        setUi(user)

        viewModel.evaluateStatus(user).observe(this, {
            if(it.status == Status.ONLINE){
                binding.onlineIndicator.setImageResource(R.drawable.online_color)
                binding.typingStatusChatLog.text=getString(R.string.online)
            }
            else if (it.status == Status.OFFLINE){
                binding.onlineIndicator.setImageResource(R.drawable.hollow_circle)
                binding.typingStatusChatLog.text=getString(R.string.away)
            }
            else if (it.status== Status.ONLINE_AND_TYPING && it.uid==toId){
                binding.onlineIndicator.setImageResource(R.drawable.online_color)
                binding.typingStatusChatLog.text=getString(R.string.typing)
            }
        })

        binding.backButtonChatLog.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.isScrollable.observe(this, {
            if(it){
                if (binding.chatLogRecycler.adapter!==null)
                    binding.chatLogRecycler.scrollToPosition(((binding.chatLogRecycler.adapter)?.itemCount!!) -1)
            }
        })

        setRecycler()

        if (user != null) {
            viewModel.listenForMessages(user).observe(this, {
                it.forEach {doc->
                    viewModel.listener(doc).observe(this, { chatMessages->
                        binding.chatLogRecycler.adapter = ChatAdapter(chatMessages)
                        if (binding.chatLogRecycler.adapter!==null)
                            binding.chatLogRecycler.scrollToPosition(((binding.chatLogRecycler.adapter)?.itemCount!!) -1)
                    })
                }
            })
        }

        apiService = Client.client.getClient("https://fcm.googleapis.com/")!!.create(
            ApiService::class.java)

        binding.sendButton.setOnClickListener {
            if(binding.edittextChatLog.text.toString()==""){
               binding.edittextChatLog.requestFocus()
                return@setOnClickListener
            }
            else{
                viewModel.notify = true

                viewModel.performSendMessage(user,edittext_chat_log.text.toString(),apiService)

                binding.edittextChatLog.text.clear()
            }
        }
        binding.edittextChatLog.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(binding.edittextChatLog.text.toString().trim().isNotEmpty())
                {
                    if (toId != null)
                    {
                        viewModel.updateTypingStatus(Status.ONLINE_AND_TYPING)
                    }
                }
                else
                {
                   viewModel.updateTypingStatus(Status.ONLINE)
                }
            }
        })
        binding.sendImage.setOnClickListener {
            viewModel.notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"pick an image"),438)
        }
    }

    private fun setRecycler() {
        binding.chatLogRecycler.setHasFixedSize(true)
    }

    private fun setUi(user: User?) {
        if(viewModel.returnUser(user)?.category =="Seeker")
        {
            binding.categoryTextChatLog.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
            binding.categoryTextChatLog.setTextColor(Color.parseColor("#ffff00"))
        }
        binding.usernameChatLog.text = viewModel.returnUser(user)?.username
        Picasso.get().load(viewModel.returnUser(user)?.profileImageUrl).into(binding.chatLogProfile)
        binding.categoryTextChatLog.text= user?.category
    }

    override fun onResume() {
        super.onResume()
        Log.d("resume","resume called")
        if (binding.chatLogRecycler.adapter!==null)
            binding.chatLogRecycler.scrollToPosition(((binding.chatLogRecycler.adapter)?.itemCount!!) -1)

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
            val user = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)
            if (user != null) {
                viewModel.imageMessage(data,user,apiService!!)
            }
        }
    }
}
