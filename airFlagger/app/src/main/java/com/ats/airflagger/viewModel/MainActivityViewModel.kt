package com.ats.airflagger.viewModel

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import androidx.lifecycle.ViewModel
import com.ats.airflagger.data.model.BaseDevice
import com.ats.airflagger.util.types.Beacon

class MainActivityViewModel:ViewModel() {

    var Name = ""
    var beacons = ArrayList<Beacon>()

    @SuppressLint("MissingPermission", "NewApi")
    fun resultToBeacon(result: ScanResult): Beacon? {
        // Add first Desc Time and Last Seen
        var beacon : Beacon?=null
        if (!result.device.name.isNullOrEmpty()) {

            val iBeacon = Beacon(
                result.device.name.toString(),
                result.device.address.toString(),
                result.rssi.toString(),
                result.scanRecord?.serviceUuids.toString(),
                BaseDevice(result).deviceType!!,
                BaseDevice(result).firstDiscovery.second.toString(),
                BaseDevice(result).lastSeen.second.toString()
            )
            addBeacon(iBeacon)
            beacon = iBeacon

        }else{
            for (i in 0 until beacons!!.size+1) {
                Name = "Device $i"
            }

            if ( Name != null && Name != "null") {
                val iBeacon = Beacon(
                    Name.toString(),
                    result.device.address.toString(),
                    result.rssi.toString(),
                    result.scanRecord?.serviceUuids.toString(), BaseDevice(result).deviceType!!,
                    BaseDevice(result).firstDiscovery.second.toString(),
                    BaseDevice(result).lastSeen.second.toString()
                )
                addBeacon(iBeacon)
                beacon= iBeacon

            }
        }
        return beacon
    }


    fun addBeacon(iBeacon: Beacon) {
        if (beacons!!.isNotEmpty()) {
            val it: MutableIterator<Beacon> = beacons!!.iterator() as MutableIterator<Beacon>
            while (it.hasNext()) {
                val beacon = it.next()
                val addressBeacon = beacon.address
                val addressIBeacon = iBeacon.address
                val bool = addressBeacon == addressIBeacon
                if (bool) {
                    it.remove()
                }
            }
        }
        beacons!!.add(iBeacon)
    }


}