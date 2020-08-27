package com.example.vcare.home


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.vcare.chatLog.ChatLogActivity
import com.example.vcare.login.LoginActivity
import com.example.vcare.Notifications.OreoNotification
import com.example.vcare.R
import com.example.vcare.databinding.FragmentHomeBinding
import com.example.vcare.helper.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


class HomeFragment : Fragment() {
    private val viewModel by viewModels<HomeFragmentViewmodel>()
    private lateinit var binding : FragmentHomeBinding
    private val adapter = GroupAdapter<ViewHolder>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val oreoNotification = OreoNotification(requireContext())
        oreoNotification.getManager!!.cancelAll()

        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_home,container,false)

        binding.homeRecycler.adapter = viewModel.adapter

        viewModel.adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(requireContext(), ChatLogActivity::class.java)
            val row = item as HomeItem
            intent.putExtra(NewMessageFrag.USER_KEY,row.chatPartner)
            startActivity(intent)
        }

        binding.signOutButton.setOnClickListener {
            val intent = Intent(requireContext(),
                LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(requireContext(), "Sign out successful!", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
        }
        viewModel.listenForNewMessage()
        viewModel.updateToken(FirebaseInstanceId.getInstance().token)
        return binding.root
    }

}