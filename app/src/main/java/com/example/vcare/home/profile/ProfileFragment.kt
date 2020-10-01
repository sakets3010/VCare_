package com.example.vcare.home.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vcare.R
import com.example.vcare.databinding.FragmentProfileBinding
import com.example.vcare.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val viewModel by viewModels<ProfileFragmentViewmodel>()
    private lateinit var binding:FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val uid = Firebase.auth.currentUser?.uid
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile,container,false)
        binding.btnSignOut.setOnClickListener {
            signOut()
        }
        binding.buttonSetting.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
        binding.btnSave.setOnClickListener {

            if (uid != null) {
                viewModel.updateUserDetails(uid,binding.editTextBio.text.toString())
            }

            Snackbar.make(requireView(),getString(R.string.updated_bio),Snackbar.LENGTH_SHORT).show()
        }
        if (uid != null) {
            viewModel.fetchUserDetails(uid).observe(viewLifecycleOwner, {
                binding.loginUsernameTextLayoutProfile.text = it.username
                binding.editTextBio.setText(it.bio, TextView.BufferType.EDITABLE)
                Picasso.get().load(it.profileImageUrl).into(binding.imageViewProfilePicture)
            })
        }
        return binding.root
    }
    private fun signOut() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        Toast.makeText(requireContext(), getString(R.string.sign_out_successful), Toast.LENGTH_SHORT).show()
        Firebase.auth.signOut()
    }

}