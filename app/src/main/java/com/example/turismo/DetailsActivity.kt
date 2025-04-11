package com.example.turismo

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.turismo.database.TourismoDatabase

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val title = intent.getStringExtra("place_name")

        if (title != null && title.isNotEmpty()) {
            val database = TourismoDatabase(this)
            val item = database.getItemByTitle(title)

            if (item != null) {
                Log.d("DetailsActivity", "Title: ${item.title}")
                Log.d("DetailsActivity", "Image URL: ${item.imagePath}")
                Log.d("DetailsActivity", "Description: ${item.description}")

                val titleView: TextView = findViewById(R.id.textView2)
                val imageView: ImageView = findViewById(R.id.image1)
                val descriptionView: EditText = findViewById(R.id.placeDescription)

                titleView.text = item.title

                Glide.with(this)
                    .load(item.imagePath)
                    .placeholder(R.drawable.image1)
                    .into(imageView)

                descriptionView.setText(item.description ?: "Описание недоступно")
            } else {
                Log.d("DetailsActivity", "Item not found")
                val descriptionView: EditText = findViewById(R.id.placeDescription)
                descriptionView.setText("Место не найдено")
            }
        } else {
            Log.d("DetailsActivity", "Invalid title")
            val descriptionView: EditText = findViewById(R.id.placeDescription)
            descriptionView.setText("Ошибка: название не задано")
        }
    }
}