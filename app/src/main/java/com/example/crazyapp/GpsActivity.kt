package com.example.crazyapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import org.zeromq.ZMQ
import android.widget.Button
import android.content.Context
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager

class GpsActivity : AppCompatActivity() {

    private lateinit var VivodGPS: TextView
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val ZMQ_SERVER_ADDRESS = "tcp://192.168.88.159:5555"
    private var zmqContext: ZMQ.Context? = null
    private var zmqSocket: ZMQ.Socket? = null
    private lateinit var telephonyManager: TelephonyManager
    private var rsrpValue: Double? = null
    private var rsrqValue: Double? = null

    private fun startZmqClient() {
        Thread {
            try {
                zmqContext = ZMQ.context(1)
                zmqSocket = zmqContext?.socket(ZMQ.PUSH)
                zmqSocket?.connect(ZMQ_SERVER_ADDRESS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun sendLocationZmq(location: Location) {
        val json = JSONObject()
        json.put("latitude", location.latitude)
        json.put("longitude", location.longitude)
        json.put("timestamp", System.currentTimeMillis())
        json.put("device_id", "android-client-1")
        json.put("rsrp", rsrpValue)
        json.put("rsrq", rsrqValue)
        val message = json.toString()

        Thread {
            try {
                zmqSocket?.let {
                    val success = it.send(message.toByteArray(ZMQ.CHARSET), 0)
                    if (success) println("ZMQ: Сообщение отправлено")
                    else println("ZMQ: Ошибка отправки сообщения (send вернул false).")
                } ?: println("ZMQ сокет не инициализирован (null).")
            } catch (e: Exception) {
                println("ZMQ: Критическая ошибка при отправке")
            }
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gps)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        VivodGPS = findViewById(R.id.gps)
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        startZmqClient()

        findViewById<Button>(R.id.button_back).setOnClickListener {
            finish()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val timeString = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(it.time))
                    VivodGPS.text = "Широта: ${it.latitude}, Долгота: ${it.longitude}, Время: $timeString"
                } ?: run {
                    VivodGPS.text = "Доступ к координатам закрыт"
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val timeString = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(location.time))
                    VivodGPS.text = "Широта: ${location.latitude}, Долгота: ${location.longitude}, Время: $timeString"
                    updateSignalStrength()
                    saveLocationToJson(location)
                    sendLocationZmq(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    private fun updateSignalStrength() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 2)
            return
        }

        val cellInfoList = telephonyManager.allCellInfo
        for (cellInfo in cellInfoList) {
            if (cellInfo is CellInfoLte) {
                val signal = cellInfo.cellSignalStrength
                rsrpValue = signal.rsrp.toDouble()
                rsrqValue = signal.rsrq.toDouble()
                break
            }
        }
    }

    private fun saveLocationToJson(location: Location) {
        val json = JSONObject()
        json.put("latitude", location.latitude)
        json.put("longitude", location.longitude)
        json.put("timestamp", System.currentTimeMillis())
        json.put("rsrp", rsrpValue)
        json.put("rsrq", rsrqValue)

        val file = File(filesDir, "locations.json")
        file.appendText(json.toString() + "\n")
    }

    override fun onPause() {
        super.onPause()
        locationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        zmqSocket?.close()
        zmqContext?.term()
    }

}
