package com.example.vcare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.example.vcare.HomeActivity.Status.Companion.updateStatus
import com.example.vcare.helper.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")  //TODO(7)
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                //does nothing
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)!!

            }
        })
    }
class Status{
    companion object{
        private val firebaseUser = FirebaseAuth.getInstance().currentUser
        fun updateStatus(status:Long){
            val ref = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid) //TODO(8)
            val hashMap = HashMap<String,Any>()
            hashMap["status"] = status
            ref.updateChildren(hashMap)
        }
    }
}
    override fun onResume() {
        super.onResume()
        updateStatus(100L)
    }

    override fun onPause() {
        super.onPause()
        updateStatus(102L)
    }
}