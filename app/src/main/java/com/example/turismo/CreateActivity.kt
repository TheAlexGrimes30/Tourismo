package com.example.turismo

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.turismo.database.TourismoDatabase
import java.io.File
import java.io.FileOutputStream

class CreateActivity : ComponentActivity() {

    private lateinit var addFileIcon: ImageView
    private lateinit var placeName: EditText
    private lateinit var placeDescription: EditText
    private lateinit var addPlaceButton: Button

    private var imageUri: Uri? = null
    private var savedImagePath: String? = null

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            imageUri = data?.data
            imageUri?.let {
                addFileIcon.setImageURI(it)
                savedImagePath = saveImageToInternalStorage(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        addFileIcon = findViewById(R.id.addFileIcon)
        placeName = findViewById(R.id.placeName)
        placeDescription = findViewById(R.id.placeDescription)
        addPlaceButton = findViewById(R.id.addPlaceButton)

        latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        Log.d("CreateActivity", "Latitude: $latitude, Longitude: $longitude")

        addFileIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        addPlaceButton.setOnClickListener {
            val title = placeName.text.toString().trim()
            val description = placeDescription.text.toString().trim()

            if (title.isEmpty() || savedImagePath == null) {
                Toast.makeText(this, "Пожалуйста, заполните название и добавьте изображение", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = TourismoDatabase(this)
            val rowId = db.insertItem(
                title, description, savedImagePath!!, latitude, longitude
            )

            if (rowId != -1L) {
                Toast.makeText(this, "Место добавлено", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        val resolver: ContentResolver = contentResolver

        val imageDir = File(filesDir, "images")
        if (!imageDir.exists()) {
            imageDir.mkdir()
        }

        val fileName = getFileName(uri) ?: "image_${System.currentTimeMillis()}.jpg"
        val imageFile = File(imageDir, fileName)

        return try {
            val inputStream = resolver.openInputStream(uri)
            val outputStream = FileOutputStream(imageFile)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return name
    }
}