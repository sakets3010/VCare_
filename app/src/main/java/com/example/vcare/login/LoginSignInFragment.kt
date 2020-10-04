package com.example.vcare.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.vcare.R
import com.example.vcare.databinding.FragmentLoginSignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginSignInFragment : Fragment() {
    private lateinit var _googleSignInClient: GoogleSignInClient
    private lateinit var _googleSignInOptions: GoogleSignInOptions
    private val _firebaseAuth = Firebase.auth
    private lateinit var binding: FragmentLoginSignInBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login_sign_in,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureGoogleSignIn()
        setupUI()
    }

    private fun configureGoogleSignIn() {
        _googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        _googleSignInClient = GoogleSignIn.getClient(requireContext(), _googleSignInOptions)
    }

    private fun setupUI() {
        binding.googleButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = _googleSignInClient.signInIntent
        _signInLauncher.launch(signInIntent)
    }


//To restrict the application to use by bits-mail only

//if(account.email?.toLowerCase()?.endsWith("@hyderabad.bits-pilani.ac.in")!!){}
//else{
//Toast.makeText(requireContext(),"Use your BITS Email Id to log in",Toast.LENGTH_SHORT).show()
//_googleSignInClient.signOut()
// }


    private fun getAccount(data: Intent) {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_LONG).show()
        }
    }

    private val _signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.let { it1 -> getAccount(it1) }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        _firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Navigation.findNavController(
                    requireActivity(),
                    R.id.login_navhost
                ).navigate(R.id.action_loginSignInFragment_to_categoryFragment)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.sign_in_successful),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.g_sign_in_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = _firebaseAuth.currentUser
        if (user != null) {
            Navigation.findNavController(
                requireActivity(),
                R.id.login_navhost
            ).navigate(R.id.action_loginSignInFragment_to_categoryFragment)
        }
    }

}