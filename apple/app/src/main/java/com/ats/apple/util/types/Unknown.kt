package com.ats.apple.util.types

import android.bluetooth.le.ScanFilter
import android.content.Context
import androidx.annotation.DrawableRes
import com.ats.apple.R
import com.ats.apple.data.Device
import com.ats.apple.data.DeviceContext
import com.ats.apple.data.DeviceType

class Unknown(val context: Context, val id: Int) : Device() {
    override val imageResource: Int
        @DrawableRes
        get() = R.drawable.ic_baseline_device_unknown_24

    override val defaultDeviceNameWithId: String
        get() = context.resources.getString(R.string.device_name_unknown_device)
            .format(id)

    override val deviceContext: DeviceContext
        get() = Unknown

    companion object : DeviceContext {
        override val bluetoothFilter: ScanFilter
            get() = ScanFilter.Builder()
                .setManufacturerData(
                    0x4C,
                    byteArrayOf((0x12).toByte(), (0x19).toByte()),
                    byteArrayOf((0xFF).toByte(), (0xFF).toByte())
                ).build()

        override val deviceType: DeviceType
            get() = DeviceType.UNKNOWN

        override val defaultDeviceName: String
            get() = "Unknown Device"

        override val statusByteDeviceType: UInt
            get() = 0u
    }
}