package com.erns.androidbeacon

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var btnStartTransmitter: Button
    private lateinit var etTemperature: EditText
    private lateinit var etHumidity: EditText

    private var permissionsGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etTemperature = findViewById(R.id.etTemperature)
        etHumidity = findViewById(R.id.etHumidity)
        btnStartTransmitter = findViewById(R.id.btnStartTransmitter)

        btnStartTransmitter.setOnClickListener {
            if (permissionsGranted) {
                val temp = etTemperature.text.toString().toIntOrNull()
                val hum = etHumidity.text.toString().toIntOrNull()

                if (temp != null && hum != null) {
                    val transmitter = Transmitter(applicationContext)
                    transmitter.startAdvertiser(temp, hum)
                } else {
                    Log.d(TAG, "Datos inválidos o vacíos")
                }
            } else {
                Log.d(TAG, "Permisos no concedidos aún.")
            }
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        activityResultLauncher.launch(permissions)
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.all { it.value }
        if (permissionsGranted) {
            Log.d(TAG, "Todos los permisos requeridos fueron concedidos.")
        } else {
            Log.d(TAG, "Faltan permisos necesarios.")
        }
    }
}
