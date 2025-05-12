package com.example.crazyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Кнопка "Калькулятор"
        findViewById<Button>(R.id.button_calculator).setOnClickListener {
            startActivity(Intent(this, CalculatorActivity::class.java))
        }

        // Кнопка "MP3-плеер"
        findViewById<Button>(R.id.button_mp3_player).setOnClickListener {
            startActivity(Intent(this, Mp3PlayerActivity::class.java))
        }

        // Кнопка "GPS"
        findViewById<Button>(R.id.button_gps).setOnClickListener {
            startActivity(Intent(this, GpsActivity::class.java))
        }
    }
}