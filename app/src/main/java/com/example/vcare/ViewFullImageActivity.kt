package com.example.vcare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {
    private var imageFull:ImageView?=null
    private var url:String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)

        url = intent.getStringExtra("url")
        imageFull = findViewById(R.id.image_full)

        Picasso.get().load(url).into(imageFull)
    }
}
