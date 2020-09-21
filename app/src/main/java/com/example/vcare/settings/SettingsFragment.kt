package com.example.vcare.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.vcare.R
import com.example.vcare.databinding.FragmentSettingsBinding
import com.example.vcare.helper.HelperActivity
import com.example.vcare.home.HomeActivity

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val sharedPref = requireContext().getSharedPreferences("Vcare", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)

        binding.biometricsSwitch.isChecked = sharedPref.getBoolean("ifBiometric", true)

        binding.biometricsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean("ifBiometric", true)
                editor.apply()
            } else if (!isChecked) {
                editor.putBoolean("ifBiometric", false)
                editor.apply()
            }

        }
        binding.darkModeSwitch.isChecked = sharedPref.getBoolean("isDark", false)



        if (binding.darkModeSwitch.isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            binding.radioGroupLight.visibility = View.GONE
            binding.radioGroupDark.visibility = View.VISIBLE
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding.radioGroupLight.visibility = View.VISIBLE
            binding.radioGroupDark.visibility = View.GONE
        }

        binding.darkModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                binding.radioGroupLight.visibility = View.GONE
                binding.radioGroupDark.visibility = View.VISIBLE
                editor.putBoolean("isDark", true)
                editor.apply()
                val intent = Intent(requireContext(), HelperActivity::class.java)
                startActivity(intent)

            } else if(!isChecked) {
                binding.radioGroupLight.visibility = View.VISIBLE
                binding.radioGroupDark.visibility = View.GONE
                editor.putBoolean("isDark", false)
                editor.apply()
                val intent = Intent(requireContext(), HelperActivity::class.java)
                startActivity(intent)
            }
        }
        if (sharedPref.getLong("theme", 1L) == 1L) {
            binding.radioButton1.isChecked = true
            Log.d("theme", "1 called")
        } else if (sharedPref.getLong("theme", 1L) == 2L) {
            binding.radioButton2.isChecked = true
            Log.d("theme", "2 called")
        } else if (sharedPref.getLong("theme", 1L) == 3L) {
            binding.radioButton1Dark.isChecked = true
            Log.d("theme", "3 called")
        } else if (sharedPref.getLong("theme", 1L) == 4L) {
            binding.radioButton2Dark.isChecked = true
            Log.d("theme", "4 called")
        }

        binding.radioButton1.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                editor.putLong("theme", 1L)
                editor.apply()
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)
                Log.d("theme", "1 put")

            }
        }
        binding.radioButton2.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                editor.putLong("theme", 2L)
                editor.apply()
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)
                Log.d("theme", "2 put")

            }
        }
        binding.radioButton1Dark.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                editor.putLong("theme", 3L)
                editor.apply()
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)
                Log.d("theme", "3 put")

            }
        }
        binding.radioButton2Dark.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                editor.putLong("theme", 4L)
                editor.apply()
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)
                Log.d("theme", "4 put")

            }
        }

        return binding.root
    }


}