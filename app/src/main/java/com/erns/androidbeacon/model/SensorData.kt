package com.erns.androidbeacon.model

import java.nio.ByteBuffer
import kotlin.random.Random

data class SensorData(
    val temperature: Float,
    val humidity: Float,
    val timestamp: Long = System.currentTimeMillis()
) {

    /**
     * Convierte los datos del sensor a un array de bytes para transmisión via beacon
     * Formato: [temp_high, temp_low, hum_high, hum_low, timestamp_bytes...]
     */
    fun toByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(12)

        // Convertir temperatura a entero (multiplicar por 100 para mantener 2 decimales)
        val tempInt = (temperature * 100).toInt()
        buffer.putShort(tempInt.toShort())

        // Convertir humedad a entero (multiplicar por 100 para mantener 2 decimales)
        val humInt = (humidity * 100).toInt()
        buffer.putShort(humInt.toShort())

        // Agregar timestamp (últimos 8 bytes)
        buffer.putLong(timestamp)

        return buffer.array()
    }

    companion object {
        /**
         * Crea SensorData desde un array de bytes
         */
        fun fromByteArray(data: ByteArray): SensorData? {
            if (data.size < 12) return null

            val buffer = ByteBuffer.wrap(data)

            val tempInt = buffer.short.toInt()
            val temperature = tempInt / 100.0f

            val humInt = buffer.short.toInt()
            val humidity = humInt / 100.0f

            val timestamp = buffer.long

            return SensorData(temperature, humidity, timestamp)
        }

        /**
         * Genera datos de sensor simulados para pruebas
         */
        fun generateMockData(): SensorData {
            val temperature = Random.nextFloat() * 40 + 10 // Entre 10°C y 50°C
            val humidity = Random.nextFloat() * 80 + 20    // Entre 20% y 100%
            return SensorData(temperature, humidity)
        }
    }
}