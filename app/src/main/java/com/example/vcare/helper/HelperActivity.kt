package com.example.vcare.helper

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.vcare.R
import com.example.vcare.biometrics.BiometricActivity
import com.example.vcare.login.LoginActivity

class HelperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper)
        val sharedPref = this.getSharedPreferences(getString(R.string.v_care), Context.MODE_PRIVATE)

        if (sharedPref.getBoolean("isDark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        if (sharedPref.getBoolean("ifBiometric", true)) {
            val intent = Intent(this, BiometricActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}