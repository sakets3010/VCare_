package com.example.vcare.home

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.example.vcare.R
import com.example.vcare.home.HomeActivity.Status.Companion.updateStatus
import com.example.vcare.helper.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    companion object{
       lateinit var currentUser: User
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        val navController = Navigation.findNavController(this, R.id.home_nav)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)
        fetchCurrentUser()
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        val sharedPref = this.getSharedPreferences("Vcare", Context.MODE_PRIVATE)
        Log.d("theme","called :${sharedPref.getLong("theme",1L)}")
        when(sharedPref.getLong("theme",1L)){
            1L -> theme.applyStyle(R.style.AppTheme,true)
            2L -> theme.applyStyle(R.style.OverlayThemeBlue,true)
            3L -> theme.applyStyle(R.style.DarkOverlayDefault,true)
            4L -> theme.applyStyle(R.style.DarkOverlayNonDefault,true)
        }
        return theme
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid ?:""
        val db = Firebase.firestore
        db.collection("Users").document(uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("HomeActivity", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                currentUser = snapshot.toObject(User::class.java)!!
            } else {
                Log.d("HomeActivity", "Current data: null")
            }
        }
    }
class Status{
    companion object{
        private val db = Firebase.firestore
        fun updateStatus(userId:String,status:Long){
            db.collection("Users").document(userId).update(
                mapOf(
                    "status" to status
                )
            )
        }
    }
}
    override fun onResume() {
        super.onResume()
        updateStatus(FirebaseAuth.getInstance().currentUser?.uid.toString(),100L)
    }

    override fun onPause() {
        super.onPause()
        updateStatus(FirebaseAuth.getInstance().currentUser?.uid.toString(),102L)
    }
}