package com.example.vcare.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.vcare.ChatRepository
import com.example.vcare.R
import com.example.vcare.databinding.FragmentLoginEnterDetailFragmentBinding
import com.example.vcare.helper.User
import com.example.vcare.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class LoginUserDetailFragment : Fragment() {
    private lateinit var binding: FragmentLoginEnterDetailFragmentBinding
    private val repository = ChatRepository()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args: LoginUserDetailFragmentArgs by navArgs()
        val sharedPref = context?.getSharedPreferences("Vcare",Context.MODE_PRIVATE)
        if (sharedPref?.getString("username"," ")!==" ")
        {
            val intent = Intent(requireContext(),
                HomeActivity::class.java)
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
            if(selectedPhotoUri==null){
                binding.selectPhotoButton.error =  "Profile photo Required"
                binding.selectPhotoButton.requestFocus()
                Toast.makeText(requireContext(),"Please add a profile picture",Toast.LENGTH_SHORT).show()
            }
            else{
                uploadimageToFirebaseStorage(args.category)
                Toast.makeText(requireContext(),"Registered successfully",Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), HomeActivity::class.java)
                val editor = sharedPref?.edit()
                editor?.putString("username",binding.loginUsernameEdit.toString())
                editor?.apply()
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return binding.root
    }
    private var selectedPhotoUri:Uri?=null
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
        Navigation.findNavController(requireActivity(),
            R.id.login_navhost
        )
            .navigate(R.id.action_LoginUserDetailFragment_to_loginSignInFragment)
        Toast.makeText(requireContext(), "Sign out successful!", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
    }
    private fun uploadimageToFirebaseStorage(category:String){
        val filename = UUID.randomUUID().toString()
        val ref=FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                saveUserToFirebaseDatabase(it.toString(),category)
            }
        }

    }
    private fun saveUserToFirebaseDatabase(profileImageUrl: String,category:String) {
        Log.d("EnterDetailFragment","saving called...")
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val user = User(uid,binding.loginUsernameEdit.text.toString(),profileImageUrl,102L,category,"")
        repository.getUserReference(uid)?.set(user)
            ?.addOnSuccessListener{ Log.d("EnterDetailFragment", "DocumentSnapshot successfully written!") }
    }


}




