package com.example.vcare

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

        val sharedPref = context?.getSharedPreferences("Vcare",Context.MODE_PRIVATE)
        if (sharedPref?.getString("username"," ")!==" ")
        {
            val intent = Intent(requireContext(),HomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

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
            else if(selectedPhotoUri==null){
                binding.selectPhotoButton.error = "profile photo required"
                binding.selectPhotoButton.requestFocus()
                return@setOnClickListener
            }

            else{
                Log.d("Login-Activity","upload called in onclick listener")
                uploadimageToFirebaseStorage()
                Toast.makeText(requireContext(),"Registered successfully",Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(),HomeActivity::class.java)
                val editor = sharedPref?.edit()
                editor?.putString("username",binding.loginUsernameEdit.toString())
                editor?.apply()
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
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
        Log.d("Login-Activity","uploadimagetoFirebaseStorage called")
            val filename = UUID.randomUUID().toString()
            val ref=FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
                Log.d("Login-Activity","successfully uploaded image:${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Login-Activity","file location:${it}")
                    saveUserToFirebaseDatabase(it.toString())
                }
            }.addOnFailureListener {
                Log.d("Login-Activity","Unable to function properly")
            }

    }



    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid,binding.loginUsernameEdit.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Login-Activity", "Finally we saved the user to Firebase Database")
            }
            .addOnFailureListener {
                Log.d("Login-Activity", "Failed to set value to database: ${it.message}")
            }
    }
}




