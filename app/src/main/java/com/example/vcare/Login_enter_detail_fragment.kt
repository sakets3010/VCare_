package com.example.vcare

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.vcare.databinding.FragmentLoginEnterDetailFragmentBinding
import com.google.firebase.auth.FirebaseAuth


class Login_enter_detail_fragment : Fragment() {
    private lateinit var binding: FragmentLoginEnterDetailFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login_enter_detail_fragment,
            container,
            false
        )
        setupUI()
        binding.selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


        return binding.root
    }
    var selectedPhotoUri:Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
             selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPhotoUri)
            binding.selectPhotoButton.setBackgroundDrawable(BitmapDrawable(bitmap))
        }
    }

    private fun setupUI() {
        binding.signOut.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        Navigation.findNavController(requireActivity(), R.id.login_navhost)
            .navigate(R.id.action_login_enter_detail_fragment_to_login_Sign_in_fragment)
        Toast.makeText(requireContext(), "Sign out successful!", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
    }



    }




