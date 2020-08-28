package com.example.vcare.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.vcare.R
import com.example.vcare.databinding.FragmentCategoryBinding


class CategoryFragment : Fragment() {
    private lateinit var binding : FragmentCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPref = context?.getSharedPreferences("Vcare", Context.MODE_PRIVATE)
        if (sharedPref?.getString("category"," ")!==" ")
        {
            Navigation.findNavController(requireActivity(),
                R.id.login_navhost
            ).navigate(R.id.action_categoryFragment_to_login_enter_detail_fragment)
        }
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_category,container,false)
        val items = arrayOf("Helper", "Seeker")
        val dropDownAdapter = ArrayAdapter(requireContext(),
            R.layout.category_list_item, items)
        binding.dropdownText.setAdapter(dropDownAdapter)

        binding.buttonGetStarted.setOnClickListener {
            val category = binding.dropdownText.text.toString().trim()
            if(category.isEmpty())
            {
                binding.dropdownText.error = "Category Required"
                binding.dropdownText.requestFocus()
                return@setOnClickListener
            }
            else{
                val editor = sharedPref?.edit()
                editor?.putString("category",binding.dropdownText.text.toString())
                editor?.apply()
                val action =
                    CategoryFragmentDirections.actionCategoryFragmentToLoginEnterDetailFragment(
                        category
                    )
                findNavController().navigate(action)
            }
        }
        binding.backButton2.setOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_loginSignInFragment)
        }

        return binding.root
    }

}