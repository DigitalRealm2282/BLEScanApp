package com.ats.apple.util.types

import android.bluetooth.le.ScanFilter
import android.content.Context
import android.os.ParcelUuid
import androidx.annotation.DrawableRes
import com.ats.apple.R
import com.ats.apple.data.Device
import com.ats.apple.data.DeviceContext
import com.ats.apple.data.DeviceType

class Tile(val context: Context, val id: Int) : Device() {
    override val imageResource: Int
        @DrawableRes
        get() = R.drawable.ic_baseline_device_unknown_24

    override val defaultDeviceNameWithId: String
        get() = context.resources.getString(R.string.device_name_tile)
            .format(id)

    override val deviceContext: DeviceContext
        get() = Tile

    companion object : DeviceContext {
        // TODO: Implement scan filter for tile
        override val bluetoothFilter: ScanFilter
            get() = ScanFilter.Builder().setServiceUuid(offlineFindingServiceUUID).build()

        override val deviceType: DeviceType
            get() = DeviceType.TILE

        override val defaultDeviceName: String
            get() = "Tile"

        override val statusByteDeviceType: UInt
            get() = 0u

        val offlineFindingServiceUUID: ParcelUuid = ParcelUuid.fromString("0000FEED-0000-1000-8000-00805F9B34FB")
    }
}