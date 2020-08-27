package com.example.vcare.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.vcare.R
import com.example.vcare.chatLog.ChatLogViewmodel
import com.example.vcare.databinding.FragmentProfileBinding
import com.example.vcare.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {
    private val viewModel by viewModels<ProfileFragmentViewmodel>()
    private lateinit var binding:FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile,container,false)
        binding.btnSignOut.setOnClickListener {
            signOut()
        }
        val bio = binding.editTextBio.text.toString()
        binding.btnSave.setOnClickListener {
            viewModel.updateUserDetails(uid!!,binding.editTextBio.text.toString())
            Snackbar.make(requireView(),"bio updated!",Snackbar.LENGTH_SHORT).show()
        }
        viewModel.fetchUserDetails(uid!!).observe(viewLifecycleOwner, Observer {
            binding.loginUsernameTextLayoutProfile.text = it.username
            binding.editTextBio.setText(it.bio, TextView.BufferType.EDITABLE)
            Picasso.get().load(it.profileImageUrl).into(binding.imageViewProfilePicture)
        })
        return binding.root
    }
    private fun signOut() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        Toast.makeText(requireContext(), "Sign out successful!", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
    }

}