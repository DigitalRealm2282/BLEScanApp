package com.ats.apple.data.filter.models

import androidx.collection.ArraySet
import com.ats.apple.data.model.BaseDevice
import com.ats.apple.data.DeviceType

class DeviceTypeFilter(deviceTypes: Set<DeviceType>) : Filter() {
    override fun apply(baseDevices: List<BaseDevice>): List<BaseDevice> {
        return baseDevices.filter {
            deviceTypes.contains(it.deviceType)
        }
    }

    fun contains(deviceType: DeviceType): Boolean = deviceTypes.contains(deviceType)

    fun add(deviceType: DeviceType) = deviceTypes.add(deviceType)

    fun remove(deviceType: DeviceType) = deviceTypes.remove(deviceType)

    var deviceTypes: ArraySet<DeviceType>

    init {
        this.deviceTypes = ArraySet()
        this.deviceTypes.addAll(deviceTypes)
    }

    companion object {
        fun build(deviceTypes: Set<DeviceType>): Filter {
            return DeviceTypeFilter(deviceTypes)
        }
    }
}