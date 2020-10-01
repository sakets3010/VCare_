package com.example.vcare.home

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.example.vcare.R
import com.example.vcare.helper.ChatRepository
import com.example.vcare.helper.Status
import com.example.vcare.settings.SettingsFragment.Companion.THEME_1
import com.example.vcare.settings.SettingsFragment.Companion.THEME_2
import com.example.vcare.settings.SettingsFragment.Companion.THEME_3
import com.example.vcare.settings.SettingsFragment.Companion.THEME_4
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {


    private val _repository = ChatRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val navController = Navigation.findNavController(this, R.id.home_nav)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        val sharedPref = this.getSharedPreferences(getString(R.string.v_care), Context.MODE_PRIVATE)
        when (sharedPref.getLong("theme", 1L)) {
            THEME_1 -> theme.applyStyle(R.style.AppTheme, true)
            THEME_2 -> theme.applyStyle(R.style.OverlayThemeBlue, true)
            THEME_3 -> theme.applyStyle(R.style.DarkOverlayDefault, true)
            THEME_4 -> theme.applyStyle(R.style.DarkOverlayNonDefault, true)
        }
        return theme
    }

    private fun updateStatus(userId: String, status: Long) {
        _repository.getUserReference(userId)?.update(
            mapOf(
                "status" to status
            )
        )
    }


    override fun onResume() {
        super.onResume()
        Firebase.auth.uid?.let {
            updateStatus(
                it, Status.ONLINE
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Firebase.auth.uid?.let {
            updateStatus(
                it, Status.OFFLINE
            )
        }
    }
}