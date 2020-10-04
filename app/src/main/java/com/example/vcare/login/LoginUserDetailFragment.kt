package com.example.vcare.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.vcare.R
import com.example.vcare.databinding.FragmentLoginEnterDetailFragmentBinding
import com.example.vcare.helper.ChatRepository
import com.example.vcare.helper.Status
import com.example.vcare.helper.User
import com.example.vcare.home.HomeActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*


class LoginUserDetailFragment : Fragment() {
    private lateinit var binding: FragmentLoginEnterDetailFragmentBinding
    private val _repository = ChatRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args: LoginUserDetailFragmentArgs by navArgs()
        val sharedPref =
            requireContext().getSharedPreferences(getString(R.string.v_care), Context.MODE_PRIVATE)
        if (sharedPref?.getString(getString(R.string.username_), " ") !== " ") {
            val intent = Intent(
                requireContext(),
                HomeActivity::class.java
            )
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
            _pickImages.launch("image/*")
        }
        binding.register.setOnClickListener {
            if (binding.loginUsernameEdit.text.toString().trim().isEmpty()) {
                binding.loginUsernameEdit.error = getString(R.string.username_required)
                binding.loginUsernameEdit.requestFocus()
                return@setOnClickListener
            }
            if (_selectedPhotoUri == null) {
                binding.selectPhotoButton.error = getString(R.string.profile_photo_required)
                binding.selectPhotoButton.requestFocus()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_add_profile),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                uploadimageToFirebaseStorage(args.category)
                Toast.makeText(requireContext(), getString(R.string.success), Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(requireContext(), HomeActivity::class.java)
                val editor = sharedPref?.edit()
                editor?.putString(
                    getString(R.string.username_),
                    binding.loginUsernameEdit.toString()
                )
                editor?.apply()
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return binding.root
    }

    private var _selectedPhotoUri: Uri? = null

    private val _pickImages =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                _selectedPhotoUri = uri
                Picasso.get().load(it).into(binding.circularProfileHolder)
                binding.selectPhotoButton.alpha = 0f
            }
        }


    private fun setupUI() {
        binding.signOut.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        Navigation.findNavController(
            requireActivity(),
            R.id.login_navhost
        )
            .navigate(R.id.action_LoginUserDetailFragment_to_loginSignInFragment)
        Toast.makeText(
            requireContext(),
            getString(R.string.sign_out_successful),
            Toast.LENGTH_SHORT
        ).show()
        Firebase.auth.signOut()
    }

    private fun uploadimageToFirebaseStorage(category: String) {
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")
        _selectedPhotoUri?.let { it ->
            ref.putFile(it).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString(), category)
                }
            }
        }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String, category: String) {

        val uid = Firebase.auth.uid ?: ""
        val user = User(
            uid,
            binding.loginUsernameEdit.text.toString(),
            profileImageUrl,
            Status.OFFLINE,
            category
        )
        _repository.getUserReference(uid)?.set(user)

    }


}




