package com.example.vcare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val navcontroller= Navigation.findNavController(this,R.id.login_navhost)
        NavigationUI.setupActionBarWithNavController(this,navcontroller)
    }

}