package com.example.turismo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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

        val titleView: TextView = findViewById(R.id.textView2)
        val imageView: ImageView = findViewById(R.id.image1)
        val descriptionView: EditText = findViewById(R.id.placeDescription)
        val updateButton: Button = findViewById(R.id.updateButton)

        if (title != null && title.isNotEmpty()) {
            val database = TourismoDatabase(this)
            val item = database.getItemByTitle(title)

            if (item != null) {
                titleView.text = item.title
                Glide.with(this).load(item.imagePath).into(imageView)
                descriptionView.setText(item.description ?: "Описание недоступно")

                updateButton.setOnClickListener {
                    val newTitle = titleView.text.toString()
                    val newDescription = descriptionView.text.toString()

                    val rowsUpdated = database.updateItem(
                        item.id,
                        newTitle,
                        newDescription,
                        item.imagePath,
                        item.latitude,
                        item.longitude
                    )

                    if (rowsUpdated > 0) {
                        Log.d("DetailsActivity", "Item updated successfully")

                        val intent = Intent(this@DetailsActivity, ListActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("DetailsActivity", "Failed to update item")
                    }

                }
            } else {
                Log.d("DetailsActivity", "Item not found")
                descriptionView.setText("Место не найдено")
            }
        } else {
            Log.d("DetailsActivity", "Invalid title")
            descriptionView.setText("Ошибка: название не задано")
        }
    }
}