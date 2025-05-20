package com.example.turismo

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WheatherActivity : AppCompatActivity() {
    private lateinit var temperatureValue: TextView
    private lateinit var weatherDescription: TextView
    private lateinit var locationName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_wheather)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        temperatureValue = findViewById(R.id.temperatureValue)
        weatherDescription = findViewById(R.id.weatherDescription)
        locationName = findViewById(R.id.locationName)

        loadWeather()
    }

    private fun loadWeather() {
        CoroutineScope(Dispatchers.Main).launch {
            val weatherData = fetchWeatherFromApi("Curitiba")
            if (weatherData != null) {
                temperatureValue.text = weatherData.temperature
                weatherDescription.text = weatherData.description
                locationName.text = weatherData.location
            } else {
                temperatureValue.text = "N/A"
                weatherDescription.text = "Ошибка загрузки данных"
                locationName.text = "Curitiba"
            }
        }
    }

    private suspend fun fetchWeatherFromApi(city: String): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://goweather.herokuapp.com/weather/Curitiba")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val stream = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(stream)

                val temperature = json.optString("temperature", "N/A")
                val description = json.optString("description", "N/A")
                val location = city

                return@withContext WeatherData(temperature, description, location)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    data class WeatherData(
        val temperature: String,
        val description: String,
        val location: String
    )
}