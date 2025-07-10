package com.erns.androidbeacon

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import com.erns.androidbeacon.tools.BleTools
import java.nio.ByteBuffer

class Transmitter(private val context: Context) {
    private val TAG = "Transmitter"

    fun startAdvertiser(temperature: Int, humidity: Int) {
        val Service_UUID = ParcelUuid.fromString("6ef0e30d-7308-4458-b62e-f706c692ca77")

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_ADVERTISE no concedido")
            return
        }

        Log.e(TAG, "Max advertising data length: ${adapter.leMaximumAdvertisingDataLength}")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            adapter.name = "NA"
        }

        val advertiser = adapter.bluetoothLeAdvertiser

        if (!adapter.isLe2MPhySupported) {
            Log.e(TAG, "2M PHY no soportado")
            return
        }

        if (!adapter.isLeExtendedAdvertisingSupported) {
            Log.e(TAG, "LE Extended Advertising no soportado")
            return
        }

        val dataBuilder = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)

        val manufacturerData = ByteBuffer.allocate(23)
        val uuid = BleTools.getIdAsByte("6ef0e30d73084458b62ef706c692ca77")

        manufacturerData.put(0, 0x02.toByte()) // Beacon Identifier
        manufacturerData.put(1, 0x15.toByte())
        for (i in 2..17) {
            manufacturerData.put(i, uuid[i - 2])
        }

        // Insertar temperatura y humedad como enteros (16 bits cada uno)
        manufacturerData.put(18, (temperature shr 8).toByte()) // temp high byte
        manufacturerData.put(19, (temperature and 0xFF).toByte()) // temp low byte
        manufacturerData.put(20, (humidity shr 8).toByte()) // hum high byte
        manufacturerData.put(21, (humidity and 0xFF).toByte()) // hum low byte
        manufacturerData.put(22, 0x76.toByte()) // txPower

        dataBuilder.addManufacturerData(76, manufacturerData.array())

        val settingsBuilder = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)

        advertiser?.let {
            it.stopAdvertising(callbackClose)
            it.startAdvertising(settingsBuilder.build(), dataBuilder.build(), callback)
        } ?: run {
            Log.d(TAG, "advertiser es null")
        }
    }

    private val callback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "Publicidad iniciada exitosamente")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.d(TAG, "Error al iniciar publicidad: $errorCode")
        }
    }

    private val callbackClose = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "Publicidad detenida exitosamente")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.d(TAG, "Error al detener publicidad: $errorCode")
        }
    }
}
