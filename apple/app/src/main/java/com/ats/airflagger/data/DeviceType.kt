package com.ats.airflagger.data

import com.ats.airflagger.util.types.*

enum class DeviceType {
    UNKNOWN,
    AIRTAG,
    APPLE,
    AIRPODS,
    TILE,
    FIND_MY,
    IPhone,
    CHIPOLO_ONE,
    CHIPOLO_ONE_SPOT,
    CHIPOLO_CARD,
    CHIPOLO_CARD_SPOT,
    GALAXY_SMART_TAG;

    companion object  {
        fun userReadableName(deviceType: DeviceType): String {
            return when (deviceType) {
                IPhone -> IPhoneDevice.defaultDeviceName
                UNKNOWN -> Unknown.defaultDeviceName
                AIRPODS -> AirPods.defaultDeviceName
                AIRTAG -> AirTag.defaultDeviceName
                APPLE -> AppleDevice.defaultDeviceName
                FIND_MY -> FindMy.defaultDeviceName
                TILE -> Tile.defaultDeviceName
                else -> Unknown.defaultDeviceName
            }
        }
    }

    fun canBeIgnored(): Boolean {
        return when (this) {
            TILE -> true
            APPLE -> true
            else -> false
        }
    }
}