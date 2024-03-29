package com.ats.airflagger.util.types

import android.bluetooth.le.ScanFilter
import androidx.annotation.DrawableRes
import com.ats.airflagger.FlaggerApplication
import com.ats.airflagger.R
import com.ats.airflagger.data.Device
import com.ats.airflagger.data.DeviceContext
import com.ats.airflagger.data.DeviceType

class IPhoneDevice(val id: Int) : Device() {

    override val imageResource: Int
        @DrawableRes
        get() = R.drawable.ic_baseline_device_unknown_24

    override val defaultDeviceNameWithId: String
        get() = FlaggerApplication.getAppContext().resources.getString(R.string.device_name_apple_device)
            .format(id)

    override val deviceContext: DeviceContext
        get() = IPhoneDevice

    companion object : DeviceContext {
        override val bluetoothFilter: ScanFilter
            get() = ScanFilter.Builder()
                .setManufacturerData(
                    0x4C,
                    byteArrayOf((0x12).toByte(), (0x02).toByte(), (0x18).toByte()),
                    byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
                )
                .build()

        override val deviceType: DeviceType
            get() = DeviceType.IPhone

        override val defaultDeviceName: String
            get() = "IPhone_Device"

        override val minTrackingTime: Int
            get() = 150 * 60

        override val statusByteDeviceType: UInt
            get() = 0u
    }
}