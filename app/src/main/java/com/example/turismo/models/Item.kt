package com.example.turismo.models

data class Item(
    val id: Int,
    val title: String,
    val description: String,
    val imagePath: String,
    val latitude: Double,
    val longitude: Double
)
