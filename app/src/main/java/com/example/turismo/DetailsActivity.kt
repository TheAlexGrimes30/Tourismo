package com.example.turismo

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val title = intent.getStringExtra("place_name")
        val imageUrl = intent.getStringExtra("image_url")
        val description = intent.getStringExtra("place_description")

        val titleView: TextView = findViewById(R.id.textView2)
        val imageView: ImageView = findViewById(R.id.image1)
        val descriptionView: EditText = findViewById(R.id.placeDescription)

        titleView.text = title

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.image1)
            .into(imageView)
        
        if (description != null) {
            descriptionView.setText(description)
        }
    }
}
