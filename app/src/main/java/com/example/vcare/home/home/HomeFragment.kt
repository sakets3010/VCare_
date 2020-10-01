package com.example.vcare.home.home


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.vcare.notifications.OreoNotification
import com.example.vcare.R
import com.example.vcare.chatLog.ChatLogActivity
import com.example.vcare.databinding.FragmentHomeBinding
import com.example.vcare.home.newMessage.NewMessageFragment
import com.google.firebase.iid.FirebaseInstanceId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel by viewModels<HomeFragmentViewmodel>()
    private lateinit var binding: FragmentHomeBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val oreoNotification = OreoNotification(requireContext())
        oreoNotification.getManager?.cancelAll()

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home, container, false
        )


        viewModel.listenForNewMessage().observe(viewLifecycleOwner, {
            viewModel.displayUsers(it).observe(viewLifecycleOwner, { chatMessages ->
                binding.homeRecycler.adapter = HomeAdapter(chatMessages) { chatPartner ->
                    val intent = Intent(requireContext(), ChatLogActivity::class.java)
                    intent.putExtra(NewMessageFragment.USER_KEY, chatPartner)
                    startActivity(intent)
                }
            })
        })



        viewModel.updateToken(FirebaseInstanceId.getInstance().token)
        return binding.root
    }

}