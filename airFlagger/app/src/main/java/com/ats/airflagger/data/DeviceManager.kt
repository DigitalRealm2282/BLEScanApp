package com.ats.airflagger.data

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.content.IntentFilter
import android.util.Log
import com.ats.airflagger.util.BluetoothConstants
import com.ats.airflagger.util.types.*
import kotlin.experimental.and

object DeviceManager {

    val devices = listOf(AirTag, FindMy, AirPods, AppleDevice, Tile,IPhoneDevice)
    val appleDevices = listOf(AirTag, FindMy, AirPods, AppleDevice,IPhoneDevice)

    fun getDeviceType(scanResult: ScanResult): DeviceType {
//        Timber.d("Checking device type for ${scanResult.device.address}")

        val manufacturerData = scanResult.scanRecord?.getManufacturerSpecificData(0x004c)
        val services = scanResult.scanRecord?.serviceUuids
        if (manufacturerData != null) {
            val statusByte: Byte = manufacturerData[2]
//            Timber.d("Status byte $statusByte, ${statusByte.toString(2)}")
            // Get the correct int from the byte
            val deviceTypeInt = (statusByte.and(0x30).toInt() shr 4)
//            Timber.d("Device type int: $deviceTypeInt")

            var deviceTypeCheck: DeviceType? = null

            for (device in appleDevices) {
                // Implementation of device detection is incorrect.
                if (device.statusByteDeviceType == deviceTypeInt.toUInt()) {
                    Log.d("DevManager", deviceTypeInt.toString())
                    deviceTypeCheck = device.deviceType
                }
            }

            return deviceTypeCheck ?: Unknown.deviceType
        }else if (services != null) {
            //Check if this device is a Tile
            if (services.contains(Tile.offlineFindingServiceUUID)) {
                return Tile.deviceType
            }
        }
        return Unknown.deviceType
    }
    val scanFilter: List<ScanFilter> = devices.map { it.bluetoothFilter }

    val gattIntentFilter: IntentFilter = IntentFilter().apply {
        addAction(BluetoothConstants.ACTION_EVENT_RUNNING)
        addAction(BluetoothConstants.ACTION_GATT_DISCONNECTED)
        addAction(BluetoothConstants.ACTION_EVENT_COMPLETED)
        addAction(BluetoothConstants.ACTION_EVENT_FAILED)
    }
}