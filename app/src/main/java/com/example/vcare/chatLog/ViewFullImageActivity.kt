package com.example.vcare.chatLog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.vcare.R
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {
    private var _imageFull: ImageView? = null
    private var _url: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)
        _url = intent.getStringExtra("url")
        _imageFull = findViewById(R.id.image_full)
        Picasso.get().load(_url).into(_imageFull)
    }
}
