package com.ats.airflagger.util.types

import com.ats.airflagger.data.DeviceType
import java.util.Locale


class Beacon(
    val name: String, val address: String, val rssi: String, val uuids: String,
    val type: DeviceType, val firstDiscovery: String, val lastSeen:String) {
    val serialNumber: String

    init {
        serialNumber = createSerialNumber(address)
    }

    private fun createSerialNumber(macAddress: String): String {
        val macAddressArray = macAddress.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val macAddressAsByte = ByteArray(6)
        val serialNumberAsInt = IntArray(6)
        val createdSerialNumber = StringBuilder()
        for (i in macAddressArray.indices) {
            macAddressAsByte[i] = Integer.decode("0x" + macAddressArray[i]).toByte()
            serialNumberAsInt[i] = macAddressAsByte[i].toInt() and 0xff
            val stringToAppend = String.format(
                Locale.getDefault(), "%03d",
                serialNumberAsInt[i]
            )
            createdSerialNumber.append(stringToAppend)
            if (i < macAddressArray.size - 1) {
                createdSerialNumber.append("-")
            }
        }
        return createdSerialNumber.toString()
    }


}
