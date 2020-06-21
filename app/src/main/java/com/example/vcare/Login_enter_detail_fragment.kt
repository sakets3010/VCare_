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
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.vcare.databinding.FragmentLoginEnterDetailFragmentBinding
import com.example.vcare.helper.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_login_enter_detail_fragment.*
import java.util.*


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

        binding.register.setOnClickListener {
            if (binding.loginUsernameEdit.text.toString().trim().isEmpty()){
                binding.loginUsernameEdit.error = "username Required"
                binding.loginUsernameEdit.requestFocus()
                return@setOnClickListener
            }
            else if(binding.loginUsernameEdit.text.toString().trim().isNotEmpty()){
                uploadimageToFirebaseStorage()
                Toast.makeText(requireContext(),"Registered successfully",Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(),HomeActivity::class.java)
                startActivity(intent)
            }
        }


        return binding.root
    }
    var selectedPhotoUri:Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPhotoUri)
            binding.circularProfileHolder.setImageBitmap(bitmap)
            binding.selectPhotoButton.alpha=0f
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
    private fun uploadimageToFirebaseStorage(){
        if(selectedPhotoUri==null){
         Toast.makeText(requireContext(),"profile image required",Toast.LENGTH_SHORT).show()
         return}
        val filename = UUID.randomUUID().toString()
        val ref=FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("LoginActivity","successfully uploaded image:${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
            Log.d("LoginActivity","file location:${it}")
                saveUserToFirebaseDatabase(it.toString())
            }
        }

    }
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid,login_username_edit.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("LoginActivity", "Finally we saved the user to Firebase Database")
            }
            .addOnFailureListener {
                Log.d("LoginActivity", "Failed to set value to database: ${it.message}")
            }
    }
}




