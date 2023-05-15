package com.ats.apple.data

import android.annotation.SuppressLint
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.content.IntentFilter
import android.util.Log
import com.ats.apple.util.BluetoothConstants
import com.ats.apple.util.types.*
import kotlin.experimental.and

object DeviceManager {

    val devices = listOf(AirTag, FindMy, AirPods, AppleDevice, Tile)
    val appleDevices = listOf(AirTag, FindMy, AirPods, AppleDevice,IPhoneDevice)

    @SuppressLint("MissingPermission", "NewApi")
    fun getDeviceType(scanResult: ScanResult): DeviceType {
        Log.d("Checking device type for ${scanResult.device.address}","Success")

//        var deviceType = DeviceType.UNKNOWN
        val manufacturerData = scanResult.scanRecord?.getManufacturerSpecificData(0x004c)
        val services = scanResult.scanRecord?.serviceUuids
        if (manufacturerData != null) {
            val statusByte: Byte = manufacturerData[2]
//            Timber.d("Status byte $statusByte, ${statusByte.toString(2)}")
            // Get the correct int from the byte
            val deviceTypeInt = (statusByte.and(0x30).toInt() shr 4)

            val manufacturerDataDev8:Byte = manufacturerData[2]

            val manufacturerDataDev9:Byte = manufacturerData[2]
            val deviceTypeInt8 = (manufacturerDataDev8.and(0x30).toInt()  shr 2)
            val deviceTypeInt9 = (manufacturerDataDev9.and(0x30).toInt()  shr 4)
//            Timber.d("Device type int: $deviceTypeInt")

            var deviceTypeCheck: DeviceType? = null

            for (device in appleDevices) {
                // Implementation of device detection is incorrect.
                if (
//                    device.bluetoothFilter.manufacturerData!!.contentEquals(
//                       manufacturerData
//                    )
//                    || device.statusByteDeviceType == deviceTypeInt.toUInt()
//                    || device.bluetoothFilter.matches(scanResult)
//                    || device.statusByteDeviceType == deviceTypeInt8.toUInt()
//                    || device.statusByteDeviceType == deviceTypeInt9.toUInt()
                     scanResult!!.device.type!!.toUInt() == device.statusByteDeviceType

//                    || device.deviceType.ordinal == scanResult.device.type
//                    || device.statusByteDeviceType == deviceTypeInt.toUInt()
//                    || device.bluetoothFilter.advertisingData.contentEquals(manufacturerData)
                ) {
                    Log.i("DevDataApp",device.bluetoothFilter.manufacturerData.contentToString())
                    Log.i("ScannedData",manufacturerData.contentToString())
//                    Log.i("SpecData", scanResult.scanRecord!!.manufacturerSpecificData[2][2].toString(2))



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