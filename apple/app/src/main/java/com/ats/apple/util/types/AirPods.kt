package com.ats.apple.util.types

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ats.apple.R
import com.ats.apple.util.BluetoothConstants
import com.ats.apple.data.Connectable
import com.ats.apple.data.Device
import com.ats.apple.data.DeviceContext
import com.ats.apple.data.DeviceType
import com.google.gson.annotations.SerializedName
import java.util.*

class AirPods(val context:Context,val id: Int) : Device(), Connectable {

    override val imageResource: Int
//        @DrawableRes
        get() = R.drawable.ic_airpods

    override val defaultDeviceNameWithId: String
        get() = context.resources.getString(R.string.device_name_airpods)
            .format(id)

    override val deviceContext: DeviceContext
        get() = AirPods

    override val bluetoothGattCallback: BluetoothGattCallback
        get() = object : BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        when (newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                Log.d("Connected to gatt device!","success")
                                gatt.discoverServices()
                            }
                            BluetoothProfile.STATE_DISCONNECTED -> {
                                broadcastUpdate(BluetoothConstants.ACTION_GATT_DISCONNECTED)
                                Log.d("Disconnected from gatt device!","Success")
                            }
                            else -> {
                                Log.d("Connection state changed to $newState","Success")
                            }
                        }
                    }
                    else -> {
                        Log.e("Failed to connect to bluetooth device! Status: $status","Success")
                        broadcastUpdate(BluetoothConstants.ACTION_EVENT_FAILED)
                    }
                }
            }

            @SuppressLint("MissingPermission")
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val uuids = gatt.services.map { it.uuid.toString() }
                Log.d("Found UUIDS $uuids","Success")
                val service = gatt.services.firstOrNull {
                    it.uuid.toString().lowercase().contains(
                        AIRPODS_SOUND_SERVICE.lowercase()
                    )
                }

                if (service == null) {
                    Log.e("Playing sound service not found!","Failed")
                    disconnect(gatt)
                    broadcastUpdate(BluetoothConstants.ACTION_EVENT_FAILED)
                    return
                }

                val characteristic = service.getCharacteristic(AIRPODS_SOUND_CHARACTERISTIC)
                characteristic.let {
                    gatt.setCharacteristicNotification(it, true)
                    it.value = AIRPODS_START_SOUND_OPCODE
                    gatt.writeCharacteristic(it)
                    Log.d("Playing sound on Find My device with ${it.uuid}","Success")
                    broadcastUpdate(BluetoothConstants.ACTION_EVENT_RUNNING)
                }
            }


            @SuppressLint("MissingPermission")
            fun stopSoundOnAirPods(gatt: BluetoothGatt) {
                val service = gatt.services.firstOrNull {
                    it.uuid.toString().lowercase().contains(
                       AIRPODS_SOUND_SERVICE
                    )
                }

                if (service == null) {
                    Log.d("Sound service not found","Failed")
                    return
                }

                val uuid = AIRPODS_SOUND_CHARACTERISTIC
                val characteristic = service.getCharacteristic(uuid)
                characteristic.let {
                    gatt.setCharacteristicNotification(it, true)
                    it.value = AIRPODS_STOP_SOUND_OPCODE
                    gatt.writeCharacteristic(it)
                    Log.d("Stopping sound on Find My device with ${it.uuid}","Success")

                }
            }

            @SuppressLint("MissingPermission")
            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("Finished writing to characteristic","Success")
                    if (characteristic?.value.contentEquals(AIRPODS_START_SOUND_OPCODE) && gatt != null) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            stopSoundOnAirPods(gatt)
                        }, 5000)
                    }

                    if (characteristic?.value.contentEquals(AIRPODS_STOP_SOUND_OPCODE)) {
                        disconnect(gatt)
                        broadcastUpdate(BluetoothConstants.ACTION_EVENT_COMPLETED)
                    }

                } else {
                    Log.d("Writing to characteristic failed ${characteristic?.uuid}","Success")
                    disconnect(gatt)
                    broadcastUpdate(BluetoothConstants.ACTION_EVENT_FAILED)
                }
                super.onCharacteristicWrite(gatt, characteristic, status)
            }
        }

    companion object : DeviceContext {
        internal const val AIRPODS_SOUND_SERVICE = "fd44"
        internal val AIRPODS_SOUND_CHARACTERISTIC =
            UUID.fromString("4F860003-943B-49EF-BED4-2F730304427A")
        internal val AIRPODS_START_SOUND_OPCODE = byteArrayOf(0x01, 0x00, 0x03)
        internal val AIRPODS_STOP_SOUND_OPCODE = byteArrayOf(0x01, 0x01, 0x03)

        // What does this scan filter do?
        override val bluetoothFilter: ScanFilter
            get() = ScanFilter.Builder()
                .setManufacturerData(
                    0x4C,
                    byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x18).toByte()), // Empty status byte?
                    byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte()) // ff?
                )
                .build()

        override val deviceType: DeviceType
            get() = DeviceType.AIRPODS

        override val defaultDeviceName: String
            get() = "AirPods"

        override val statusByteDeviceType: UInt
            get() = 3u
    }
}