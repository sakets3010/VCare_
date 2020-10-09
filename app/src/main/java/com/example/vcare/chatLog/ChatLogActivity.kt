package com.example.vcare.chatLog

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import com.example.vcare.settings.SettingsFragment
import com.example.vcare.settings.SettingsFragment.Companion.THEME_1
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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

    private lateinit var binding: ActivityChatLogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val chatAdapter = ChatPagedAdapter()

        setRecycler(chatAdapter)

        binding.suggestionRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)

        val user = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)


        val toId = user?.uid
        setUi(user)

        binding.backButtonChatLog.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.sendButton.setOnClickListener {
            if (binding.edittextChatLog.text.toString() == "") {
                binding.edittextChatLog.requestFocus()
                return@setOnClickListener
            } else {
                viewModel.notify = true
                viewModel.performSendMessage(user, edittext_chat_log.text.toString(), _apiService)
                binding.edittextChatLog.text.clear()
            }
        }

        binding.edittextChatLog.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //does nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //does nothing
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
            _pickImages.launch("image/*")
        }

        binding.chatLogRecycler.adapter?.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.chatLogRecycler.smoothScrollToPosition(0)
                }
            }
        })

        viewModel.messageAdded.observe(this, {
            if (it) {
                chatAdapter.refresh()
            }
        })

        if (user != null) {
            viewModel.documentId.observe(this@ChatLogActivity, { docId ->
                lifecycleScope.launch {
                    viewModel.getFlow(docId).collect {
                        chatAdapter.submitData(it)
                    }
                }

            })
        }

        viewModel.replies.observe(this, {
            binding.suggestionRv.adapter = SuggestionAdapter(it) { reply ->
                binding.edittextChatLog.text.clear()
                binding.edittextChatLog.setText(reply)
            }
        })

        lifecycleScope.launch {
            chatAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.loadMoreProgressBar.isVisible = loadStates.refresh is LoadState.Loading
                binding.loadMoreProgressBar.isVisible = loadStates.append is LoadState.Loading
            }
        }

        viewModel.status.observe(this, {
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

        _apiService = Client.getClient("https://fcm.googleapis.com/")!!.create(
            ApiService::class.java
        )

    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        val sharedPref = this.getSharedPreferences(getString(R.string.v_care), Context.MODE_PRIVATE)

        when (sharedPref.getLong("theme", THEME_1)) {
            THEME_1 -> theme.applyStyle(R.style.AppTheme, true)
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
        if (user?.category == getString(R.string.seeker)) {
            binding.categoryTextChatLog.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
            binding.categoryTextChatLog.setTextColor(Color.parseColor("#fdd835"))
        }
        binding.usernameChatLog.text = user?.username
        Picasso.get().load(user?.profileImageUrl).into(binding.chatLogProfile)
        binding.categoryTextChatLog.text = user?.category
    }

    private val _pickImages =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val user = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)
            if (user != null && uri != null) {
                _apiService?.let { viewModel.imageMessage(uri, user, it) }
            }
        }

    override fun onResume() {
        super.onResume()
        if (binding.chatLogRecycler.adapter !== null)
            binding.chatLogRecycler.smoothScrollToPosition(0)

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


}
