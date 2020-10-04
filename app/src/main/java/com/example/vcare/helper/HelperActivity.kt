package com.example.vcare.helper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.vcare.R
import com.example.vcare.biometrics.BiometricActivity
import com.example.vcare.login.LoginActivity

class HelperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper)
        val sharedPref = this.getSharedPreferences(getString(R.string.v_care), Context.MODE_PRIVATE)

        if (sharedPref.getBoolean(getString(R.string.isdark), false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        if (sharedPref.getBoolean(getString(R.string.ifbiometric), true)) {
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