package com.example.crazyapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GpsActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var gpsTextView: TextView
    private val gpsFile by lazy {
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "gps_data.json") //указываем путь в загрузки телефона
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)

        gpsTextView = findViewById(R.id.gps_text_view)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            startGps()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun startGps() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                10f,
                locationListener
            )

            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastLocation?.let {
                updateLocation(it)
            }
        } catch (e: SecurityException) {
            gpsTextView.text = "Ошибка доступа к GPS"
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateLocation(location)
        }
    }

    private fun updateLocation(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        gpsTextView.text = """
            Широта: $lat
            Долгота: $lng
            Время: $time
        """.trimIndent()

        saveLocation(lat, lng, time)
    }

    private fun saveLocation(lat: Double, lng: Double, time: String) {
        try {
            val jsonObject = JSONObject().apply {
                put("latitude", lat)
                put("longitude", lng)
                put("time", time)
            }

            gpsFile.writeText(jsonObject.toString())
            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGps()
        } else {
            gpsTextView.text = "Нужно разрешение для работы GPS"
        }
    }
}