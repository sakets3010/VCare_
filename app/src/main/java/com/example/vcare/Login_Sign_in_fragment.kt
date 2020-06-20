package com.example.vcare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.vcare.databinding.FragmentLoginSignInFragmentBinding


class Login_Sign_in_fragment : Fragment() {
    private lateinit var binding: FragmentLoginSignInFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_login__sign_in_fragment,container,false)
        return binding.root
    }
}