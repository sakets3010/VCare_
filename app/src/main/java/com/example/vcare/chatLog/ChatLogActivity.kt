package com.example.vcare.chatLog

import android.content.Context
import android.content.Intent
import android.content.res.Resources
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
import com.example.vcare.R
import com.example.vcare.chatLog.paging.ChatPagedAdapter
import com.example.vcare.databinding.ActivityChatLogBinding
import com.example.vcare.helper.Status
import com.example.vcare.helper.User
import com.example.vcare.home.HomeActivity
import com.example.vcare.home.newMessage.NewMessageFragment
import com.example.vcare.notifications.ApiService
import com.example.vcare.notifications.Client
import com.example.vcare.settings.SettingsFragment
import com.example.vcare.settings.SettingsFragment.Companion.THEME_1
import com.google.firebase.auth.ktx.auth
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
    private var _apiService: ApiService? = null
    private val _messages = arrayListOf<FirebaseTextMessage>()
    private val _suggestions = arrayListOf<String>()

    private val suggestionAdapter = SuggestionAdapter(_suggestions) {
        binding.edittextChatLog.text.clear()
        binding.edittextChatLog.setText(it)
    }

    private val _smartReply = FirebaseNaturalLanguage.getInstance().smartReply
    private lateinit var binding: ActivityChatLogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val chatAdapter = ChatPagedAdapter()
        setRecycler(chatAdapter)
        suggestion_rv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        suggestion_rv.adapter = suggestionAdapter

        val user = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)

        val toId = user?.uid
        setUi(user)

        viewModel.evaluateStatus(user).observe(this, {
            when (it.status) {
                Status.ONLINE -> {
                    binding.onlineIndicator.setImageResource(R.drawable.online_color)
                    binding.typingStatusChatLog.text = getString(R.string.online)
                }
                Status.OFFLINE -> {
                    binding.onlineIndicator.setImageResource(R.drawable.hollow_circle)
                    binding.typingStatusChatLog.text = getString(R.string.away)
                }
                Status.ONLINE_AND_TYPING -> {
                    binding.onlineIndicator.setImageResource(R.drawable.online_color)
                    binding.typingStatusChatLog.text = getString(R.string.typing)
                }
            }
        })

        binding.backButtonChatLog.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        viewModel.isScrollable.observe(this, {
            if (it) {
                if (binding.chatLogRecycler.adapter !== null)
                    chatAdapter.refresh()
                binding.chatLogRecycler.scrollToPosition(0)
            }
        })



        if (user != null) {
            viewModel.listenForMessages(user).observe(this@ChatLogActivity, { docId ->
                lifecycleScope.launch {
                    viewModel.getFlow(docId).collect {
                        chatAdapter.submitData(it)
                    }
                }

            })
        }

        viewModel.documentId.observe(this, Observer {
            if(it.isNotEmpty() && it!==null){
                viewModel.incomingMessageListener(it).observe(this,{
                    chatAdapter.refresh()
                    binding.chatLogRecycler.scrollToPosition(0)
                })
            }
        })


        lifecycleScope.launch {
            chatAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.loadMoreProgressBar.isVisible = loadStates.refresh is LoadState.Loading
                binding.loadMoreProgressBar.isVisible = loadStates.append is LoadState.Loading
            }
        }
        _apiService = Client.getClient("https://fcm.googleapis.com/")!!.create(
            ApiService::class.java
        )

        binding.sendButton.setOnClickListener {
            if (binding.edittextChatLog.text.toString() == "") {
                binding.edittextChatLog.requestFocus()
                return@setOnClickListener
            } else {
                viewModel.notify = true
                viewModel.performSendMessage(user, edittext_chat_log.text.toString(), _apiService)
                val message = user?.uid?.let { it1 ->
                    FirebaseTextMessage.createForRemoteUser(
                        edittext_chat_log.text.toString(),
                        System.currentTimeMillis(),
                        it1
                    )
                }
                if (message != null) {
                    _messages.add(
                        message
                    )
                }
                binding.edittextChatLog.text.clear()
                binding.chatLogRecycler.scrollToPosition(0)
                _smartReply.suggestReplies(
                    _messages.takeLast(10)
                )
                    .addOnSuccessListener { it ->
                        _suggestions.clear()
                        it.suggestions.forEach {
                            _suggestions.add(it.text)
                        }
                        suggestionAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
            }
        }
        binding.edittextChatLog.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (binding.edittextChatLog.text.toString().trim().isNotEmpty()) {
                    if (toId !== null) {
                        viewModel.updateTypingStatus(Status.ONLINE_AND_TYPING)
                    }
                } else {
                    viewModel.updateTypingStatus(Status.ONLINE)
                }
            }
        })
        binding.sendImage.setOnClickListener {
            viewModel.notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "pick an image"), 438)
        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        val sharedPref = this.getSharedPreferences(getString(R.string.v_care), Context.MODE_PRIVATE)
        Log.d("color", "getting:${sharedPref.getLong("theme", THEME_1)}")
        when (sharedPref.getLong("theme", THEME_1)) {
            SettingsFragment.THEME_1 -> theme.applyStyle(R.style.AppTheme, true)
            SettingsFragment.THEME_2 -> theme.applyStyle(R.style.OverlayThemeBlue, true)
            SettingsFragment.THEME_3 -> theme.applyStyle(R.style.DarkOverlayDefault, true)
            SettingsFragment.THEME_4 -> theme.applyStyle(R.style.DarkOverlayNonDefault, true)
        }
        return theme
    }


    private fun setRecycler(chatAdapter: ChatPagedAdapter) {
        binding.chatLogRecycler.setHasFixedSize(true)
        binding.chatLogRecycler.adapter = chatAdapter
        binding.chatLogRecycler.smoothScrollToPosition(0)
    }

    private fun setUi(user: User?) {
        if (viewModel.returnUser(user)?.category == getString(R.string.seeker)) {
            binding.categoryTextChatLog.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
            binding.categoryTextChatLog.setTextColor(Color.parseColor("#fdd835"))
        }
        binding.usernameChatLog.text = viewModel.returnUser(user)?.username
        Picasso.get().load(viewModel.returnUser(user)?.profileImageUrl).into(binding.chatLogProfile)
        binding.categoryTextChatLog.text = user?.category
    }

    override fun onResume() {
        super.onResume()
        if (binding.chatLogRecycler.adapter !== null)
            binding.chatLogRecycler.scrollToPosition(0)

        Firebase.auth.uid?.let {
            viewModel.updateStatus(
                it,
                Status.ONLINE
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Firebase.auth.uid?.let {
            viewModel.updateStatus(
                it,
                Status.OFFLINE
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == RESULT_OK && data?.data !== null) {
            val user = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)
            if (user != null) {
                _apiService?.let { viewModel.imageMessage(data, user, it) }
            }
        }
    }
}
