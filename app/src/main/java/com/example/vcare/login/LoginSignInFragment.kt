package com.example.vcare.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.vcare.R
import com.example.vcare.databinding.FragmentLoginSignInFragmentBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class LoginSignInFragment : Fragment() {
    private val rcSignIn: Int = 1
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentLoginSignInFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseAuth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_login_sign_in_fragment,container,false)
        configureGoogleSignIn()
        setupUI()

        return binding.root
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(),mGoogleSignInOptions)
    }
    private fun setupUI() {
        binding.googleButton.setOnClickListener {
            signIn()
        }
    }
    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, rcSignIn)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rcSignIn) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {

                    //if(account.email?.toLowerCase()?.endsWith("@hyderabad.bits-pilani.ac.in")!!)
                    firebaseAuthWithGoogle(account)
//                    else{
//                        Toast.makeText(requireContext(),"Use your BITS Email Id to log in",Toast.LENGTH_SHORT).show()
//                        mGoogleSignInClient.signOut()
//                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Navigation.findNavController(requireActivity(),
                    R.id.login_navhost
                ).navigate(R.id.action_login_Sign_in_fragment_to_categoryFragment)
                Toast.makeText(requireContext(),"Sign in successful!",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Navigation.findNavController(requireActivity(),
                R.id.login_navhost
            ).navigate(R.id.action_login_Sign_in_fragment_to_categoryFragment)
        }
    }

}