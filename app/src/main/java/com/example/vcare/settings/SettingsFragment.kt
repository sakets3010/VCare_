package com.example.vcare.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.vcare.R
import com.example.vcare.databinding.FragmentSettingsBinding
import com.example.vcare.helper.HelperActivity
import com.example.vcare.home.HomeActivity

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    companion object {
        const val THEME_1 = 1L
        const val THEME_2 = 2L
        const val THEME_3 = 3L
        const val THEME_4 = 4L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val sharedPref =
            requireContext().getSharedPreferences(getString(R.string.v_care), Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)


        //Biometrics
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

        //Dark Mode
        binding.darkModeSwitch.isChecked = sharedPref.getBoolean("isDark", false)

        if (binding.darkModeSwitch.isChecked) {
            binding.radioGroupLight.visibility = View.GONE
            binding.radioGroupDark.visibility = View.VISIBLE
        } else {
            binding.radioGroupLight.visibility = View.VISIBLE
            binding.radioGroupDark.visibility = View.GONE
        }

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean("isDark", true)
                editor.apply()
                binding.radioGroupLight.visibility = View.GONE
                binding.radioGroupDark.visibility = View.VISIBLE
                val intent = Intent(requireContext(), HelperActivity::class.java)
                startActivity(intent)

            } else {
                editor.putBoolean("isDark", false)
                editor.apply()
                binding.radioGroupLight.visibility = View.VISIBLE
                binding.radioGroupDark.visibility = View.GONE
                val intent = Intent(requireContext(), HelperActivity::class.java)
                startActivity(intent)
            }
        }

        when (sharedPref.getLong("theme", 1L)) {
            THEME_1 -> {
                binding.radioButton1.isChecked = true
            }
            THEME_2 -> {
                binding.radioButton2.isChecked = true
            }
            THEME_3 -> {
                binding.radioButton1Dark.isChecked = true
            }
            THEME_4 -> {
                binding.radioButton2Dark.isChecked = true
            }
        }

        binding.radioButton1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyThemes(THEME_1, editor)
            }
        }
        binding.radioButton2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyThemes(THEME_2, editor)
            }
        }
        binding.radioButton1Dark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyThemes(THEME_3, editor)
            }
        }
        binding.radioButton2Dark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyThemes(THEME_4, editor)
            }
        }

        return binding.root
    }

    private fun applyThemes(theme: Long, editor: SharedPreferences.Editor) {
        editor.putLong("theme", theme)
        editor.apply()
        val intent = Intent(requireContext(), HomeActivity::class.java)
        startActivity(intent)
    }


}