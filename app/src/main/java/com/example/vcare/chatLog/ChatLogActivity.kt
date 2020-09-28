package com.example.vcare.chatLog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vcare.R
import com.example.vcare.chatLog.paging.ChatPagedAdapter
import com.example.vcare.databinding.ActivityChatLogBinding
import com.example.vcare.helper.Status
import com.example.vcare.helper.User
import com.example.vcare.home.HomeActivity
import com.example.vcare.home.newMessage.NewMessageFragment
import com.example.vcare.notifications.ApiService
import com.example.vcare.notifications.Client
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChatLogActivity : AppCompatActivity() {
    private val viewModel by viewModels<ChatLogViewmodel>()
    private var apiService : ApiService?=null
    private val messages = arrayListOf<FirebaseTextMessage>()
    private val suggestions = arrayListOf<String>()

    private val suggestionAdapter = SuggestionAdapter(suggestions){
        binding.edittextChatLog.text.clear()
        binding.edittextChatLog.setText(it)
    }
    private val smartReply = FirebaseNaturalLanguage.getInstance().smartReply
    private lateinit var binding: ActivityChatLogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val chatAdapter = ChatPagedAdapter()
        binding.chatLogRecycler.adapter = chatAdapter
        suggestion_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        suggestion_rv.adapter = suggestionAdapter

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
                    chatAdapter.refresh()
                    binding.chatLogRecycler.scrollToPosition(0)
            }
        })

        setRecycler()
        viewModel.shouldRefresh.observe(this, Observer{
            if(it){
                chatAdapter.refresh()
                binding.chatLogRecycler.scrollToPosition(0)
            }
        })

        var docId = " "
        Firebase.firestore.collection("ChatChannels").whereEqualTo("between", viewModel.listenForMessages(user!!))
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (documents != null) {
                    for (document in documents) {
                        docId = document.id
                        lifecycleScope.launch {
                            viewModel.getFlow(docId).collect {
                                Log.d("return","it:$it")
                                chatAdapter.submitData(it)
                            }
                        }
                    }
                }
            }

        binding.chatLogProfile.setOnClickListener {
            binding.chatLogRecycler.scrollToPosition(0)
        }
        lifecycleScope.launch {
            chatAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.loadMoreProgressBar.isVisible = loadStates.refresh is LoadState.Loading
                binding.loadMoreProgressBar.isVisible = loadStates.append is LoadState.Loading
            }
        }
        apiService = Client.getClient("https://fcm.googleapis.com/")!!.create(
            ApiService::class.java)

        binding.sendButton.setOnClickListener {
            if(binding.edittextChatLog.text.toString()==""){
               binding.edittextChatLog.requestFocus()
                return@setOnClickListener
            }
            else{
                viewModel.notify = true
                viewModel.performSendMessage(user,edittext_chat_log.text.toString(),apiService)
                val message = FirebaseTextMessage.createForRemoteUser(
                    edittext_chat_log.text.toString(), //Content of the message
                    System.currentTimeMillis(), //Time at which the message was sent
                    user!!.uid //This has to be unique for every other person involved in the chat who is not your user
                )
                messages.add(
                    message
                )
                binding.edittextChatLog.text.clear()
                smartReply.suggestReplies(
                    messages.takeLast(2)
                )
                    .addOnSuccessListener {
                        suggestions.clear()
                        it.suggestions.forEach {
                            Log.d("smart","text:${it.text}")
                            suggestions.add(it.text)
                        }
                        suggestionAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
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
            binding.categoryTextChatLog.setTextColor(Color.parseColor("#fdd835"))
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
