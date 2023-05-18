//package com.ats.apple.data
//
//import com.ats.apple.data.model.BaseDevice
//import java.time.LocalDateTime
//
//sealed interface DeviceEvent{
//    object SaveDevice : DeviceEvent
//    data class setName(val name : String): DeviceEvent
//    data class setfs(val firstSeen : LocalDateTime): DeviceEvent
//    data class setls(val lastSeen : LocalDateTime): DeviceEvent
//    data class deleteDevice(val baseDevice: BaseDevice):DeviceEvent
//
//}