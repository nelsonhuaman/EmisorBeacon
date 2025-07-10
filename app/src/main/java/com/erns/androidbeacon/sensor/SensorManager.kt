package com.erns.androidbeacon.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.erns.androidbeacon.model.SensorData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorManager(private val context: Context) : SensorEventListener {
    private val TAG = "SensorManager"

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    private val humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)

    private var currentTemperature: Float = 25.0f // Valor por defecto
    private var currentHumidity: Float = 50.0f    // Valor por defecto

    private val _sensorData = MutableStateFlow(SensorData.generateMockData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var mockDataJob: Job? = null

    init {
        checkSensorAvailability()
    }

    private fun checkSensorAvailability() {
        Log.d(TAG, "Checking sensor availability...")

        if (temperatureSensor == null) {
            Log.w(TAG, "Temperature sensor not available - using mock data")
        } else {
            Log.d(TAG, "Temperature sensor available: ${temperatureSensor.name}")
        }

        if (humiditySensor == null) {
            Log.w(TAG, "Humidity sensor not available - using mock data")
        } else {
            Log.d(TAG, "Humidity sensor available: ${humiditySensor.name}")
        }
    }

    fun startSensorReading() {
        Log.d(TAG, "Starting sensor reading...")

        var sensorsRegistered = false

        // Intentar registrar sensores reales
        temperatureSensor?.let { sensor ->
            val success = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            if (success) {
                sensorsRegistered = true
                Log.d(TAG, "Temperature sensor registered successfully")
            }
        }

        humiditySensor?.let { sensor ->
            val success = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            if (success) {
                sensorsRegistered = true
                Log.d(TAG, "Humidity sensor registered successfully")
            }
        }

        // Si no hay sensores disponibles, usar datos simulados
        if (!sensorsRegistered) {
            Log.d(TAG, "No real sensors available, starting mock data generation")
            startMockDataGeneration()
        }
    }

    fun stopSensorReading() {
        Log.d(TAG, "Stopping sensor reading...")
        sensorManager.unregisterListener(this)
        mockDataJob?.cancel()
    }

    private fun startMockDataGeneration() {
        mockDataJob = scope.launch {
            while (isActive) {
                delay(5000) // Actualizar cada 5 segundos

                // Simular variaciones graduales
                currentTemperature += (kotlin.random.Random.nextFloat() - 0.5f) * 2
                currentHumidity += (kotlin.random.Random.nextFloat() - 0.5f) * 5

                // Mantener valores en rangos realistas
                currentTemperature = currentTemperature.coerceIn(15.0f, 45.0f)
                currentHumidity = currentHumidity.coerceIn(20.0f, 90.0f)

                val mockData = SensorData(currentTemperature, currentHumidity)
                _sensorData.value = mockData

                Log.d(TAG, "Mock data generated: ${mockData.temperature}°C, ${mockData.humidity}%")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    currentTemperature = sensorEvent.values[0]
                    Log.d(TAG, "Temperature sensor reading: ${currentTemperature}°C")
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    currentHumidity = sensorEvent.values[0]
                    Log.d(TAG, "Humidity sensor reading: ${currentHumidity}%")
                }
            }

            // Actualizar datos combinados
            _sensorData.value = SensorData(currentTemperature, currentHumidity)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: ${sensor?.name} - accuracy: $accuracy")
    }

    fun getCurrentData(): SensorData {
        return _sensorData.value
    }

    fun cleanup() {
        stopSensorReading()
        scope.cancel()
    }
}