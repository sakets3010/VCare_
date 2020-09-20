package com.example.vcare.home.newMessage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.vcare.R
import com.example.vcare.chatLog.ChatLogActivity
import kotlinx.android.synthetic.main.fragment_new_message.*

class NewMessageFragment : Fragment() {
    private val viewModel by viewModels<NewMessageViewmodel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.availableUsers.observe(viewLifecycleOwner, Observer { users ->
            recyclerview_newMessage.adapter = NewMessageAdapter(users) { availableUsers ->
                val intent = Intent(requireContext(), ChatLogActivity::class.java)
                intent.putExtra(USER_KEY, availableUsers)
                startActivity(intent)
            }
        })
        return inflater.inflate(R.layout.fragment_new_message, container, false)
    }

    companion object {
        const val USER_KEY = "USER_KEY"
    }
}


