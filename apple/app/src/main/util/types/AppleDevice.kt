package com.ats.apple.util.types

import android.bluetooth.le.ScanFilter
import android.content.Context
import androidx.annotation.DrawableRes
import com.ats.apple.ATTrackingDetectionApplication
import com.ats.apple.R
import com.ats.apple.data.Device
import com.ats.apple.data.DeviceContext
import com.ats.apple.data.DeviceType

class AppleDevice( val id: Int) : Device() {
    override val imageResource: Int
        @DrawableRes
        get() = R.drawable.ic_baseline_device_unknown_24

    override val defaultDeviceNameWithId: String
        get() = ATTrackingDetectionApplication.getAppContext().resources.getString(R.string.device_name_apple_device)
            .format(id)

    override val deviceContext: DeviceContext
        get() = AppleDevice

    companion object : DeviceContext {
        override val bluetoothFilter: ScanFilter
            get() = ScanFilter.Builder()
                .setManufacturerData(
                    0x4C,
                    byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x00).toByte()),
                    byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
                )
                .build()

        override val deviceType: DeviceType
            get() = DeviceType.APPLE

        override val defaultDeviceName: String
            get() = "Apple_Device"

        override val minTrackingTime: Int
            get() = 150 * 60

        override val statusByteDeviceType: UInt
            get() = 0u
    }
}