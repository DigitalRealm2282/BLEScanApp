package com.ats.airflagger.util.types

import android.bluetooth.le.ScanFilter
import androidx.annotation.DrawableRes
import com.ats.airflagger.FlaggerApplication
import com.ats.airflagger.R
import com.ats.airflagger.data.Device
import com.ats.airflagger.data.DeviceContext
import com.ats.airflagger.data.DeviceType

class Unknown(val id: Int) : Device() {
    override val imageResource: Int
        @DrawableRes
        get() = R.drawable.ic_baseline_device_unknown_24

    override val defaultDeviceNameWithId: String
        get() = FlaggerApplication.getAppContext().resources.getString(R.string.device_name_unknown_device)
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
            get() = "Unknown_Device"

        override val statusByteDeviceType: UInt
            get() = 0u
    }
}