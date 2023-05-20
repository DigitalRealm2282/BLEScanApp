package com.ats.apple.util.types

import com.ats.apple.data.DeviceType
import java.time.LocalDateTime
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

//
//class Beacon {
//    var name: String ? = null
//    var address: String ? = null
//    var rssi: String ? = null
//    var uuids:String ? =null
//    var type:DeviceType ?= null
//    var firstDiscovery:LocalDateTime ?= null
//    var lastSeen:LocalDateTime ?= null
//    var serialNumber:String?=null
//    constructor(
//        name: String, address: String,  rssi: String,  uuids: String,
//        type: DeviceType,  firstDiscovery: LocalDateTime,  lastSeen: LocalDateTime
//    ):this() {
//
//        this.name = name
//        this.address = address
//        this.rssi = rssi
//        this.uuids = uuids
//        this.type = type
//        this.firstDiscovery = firstDiscovery
//        this.lastSeen = lastSeen
//    }
//
//    init {
//        this.serialNumber = createSerialNumber(address!!)
//    }
//    private fun createSerialNumber(macAddress: String): String {
//        val macAddressArray = macAddress.split(":".toRegex()).dropLastWhile { it.isEmpty() }
//            .toTypedArray()
//        val macAddressAsByte = ByteArray(6)
//        val serialNumberAsInt = IntArray(6)
//        val createdSerialNumber = StringBuilder()
//        for (i in macAddressArray.indices) {
//            macAddressAsByte[i] = Integer.decode("0x" + macAddressArray[i]).toByte()
//            serialNumberAsInt[i] = macAddressAsByte[i].toInt() and 0xff
//            val stringToAppend = String.format(
//                Locale.getDefault(), "%03d",
//                serialNumberAsInt[i]
//            )
//            createdSerialNumber.append(stringToAppend)
//            if (i < macAddressArray.size - 1) {
//                createdSerialNumber.append("-")
//            }
//        }
//        return createdSerialNumber.toString()
//    }
//
//    constructor()
//}