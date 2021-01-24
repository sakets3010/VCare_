package com.example.vcare.home.newMessage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vcare.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_new_message.*

@AndroidEntryPoint
class NewMessageFragment : Fragment() {
    companion object {
        const val USER_KEY = "USER_KEY"
    }

    private val viewModel by viewModels<NewMessageViewmodel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.availableUsers.observe(viewLifecycleOwner, { users ->
            recyclerview_newMessage.adapter = NewMessageAdapter(users) { availableUsers ->
                val action = NewMessageFragmentDirections.actionNewMessageFragToChatLogFragment(
                    availableUsers ?: throw IllegalArgumentException("null encountered")
                )
                findNavController().navigate(action)
            }
        })
        return inflater.inflate(R.layout.fragment_new_message, container, false)
    }


}


