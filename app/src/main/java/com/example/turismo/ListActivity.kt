package com.example.turismo

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turismo.database.TourismoDatabase

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = TourismoDatabase(this)
        val itemList = db.getAllItems()

        for (item in itemList) {
            Log.d("DB_LOG", "Item: $item")
        }

        val adapter = ItemAdapter(itemList)
        recyclerView.adapter = adapter
    }
}
