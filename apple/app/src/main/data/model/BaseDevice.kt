package com.ats.apple.data.model

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import com.ats.apple.converter.DateTimeConverter
import com.ats.apple.data.Device
import com.ats.apple.data.DeviceManager
import com.ats.apple.data.DeviceType
import com.ats.apple.util.types.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.experimental.and

@Entity(tableName = "device", indices = [Index(value = ["address"], unique = true)])
@TypeConverters(DateTimeConverter::class)
data class BaseDevice(
    @PrimaryKey(autoGenerate = true) var deviceId: Int,
    @ColumnInfo(name = "uniqueId") val uniqueId: String?,
    @ColumnInfo(name = "address") var address: String,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "ignore") val ignore: Boolean,
    @ColumnInfo(name = "connectable", defaultValue = "0") val connectable: Boolean?,
    @ColumnInfo(name = "payloadData") val payloadData: Byte?,
    @ColumnInfo(name = "firstDiscovery") val firstDiscovery: LocalDateTime,
    @ColumnInfo(name = "lastSeen") var lastSeen: LocalDateTime,
    @ColumnInfo(name = "notificationSent") var notificationSent: Boolean,
    @ColumnInfo(name = "lastNotificationSent") var lastNotificationSent: LocalDateTime?,
    @ColumnInfo(name = "deviceType") val deviceType: DeviceType?
) {
    @Ignore
    constructor(
        address: String,
        ignore: Boolean,
        connectable: Boolean,
        payloadData: Byte?,
        firstDiscovery: LocalDateTime,
        lastSeen: LocalDateTime,
        deviceType: DeviceType
    ) : this(
        0,
        UUID.randomUUID().toString(),
        address,
        null,
        ignore,
        connectable,
        payloadData,
        firstDiscovery,
        lastSeen,
        false,
        null,
        deviceType
    )

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(scanResult: ScanResult) : this(
        0,
        UUID.randomUUID().toString(),
        scanResult.device.address,
        scanResult.scanRecord?.deviceName,
        false,
        scanResult.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                scanResult.isConnectable
            } else {
                null
            }
        },
        scanResult.scanRecord?.getManufacturerSpecificData(76)?.get(2),
        LocalDateTime.now(), LocalDateTime.now(), false, null,
        DeviceManager.getDeviceType(scanResult)
    )

    fun getDeviceNameWithID(): String = name ?: device.defaultDeviceNameWithId

    @SuppressLint("NewApi")
    @Ignore
    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

    @Ignore
    val device: Device = when (deviceType) {
        DeviceType.AIRTAG -> AirTag(deviceId)
        DeviceType.UNKNOWN -> Unknown(deviceId)
        DeviceType.APPLE -> AppleDevice(deviceId)
        DeviceType.AIRPODS -> AirPods(deviceId)
        DeviceType.FIND_MY -> FindMy(deviceId)
        DeviceType.TILE -> Tile(deviceId)
        else -> {
            // For backwards compatibility
            if (payloadData?.and(0x10)?.toInt() != 0 && connectable == true) {
                AirTag(deviceId)
            } else {
                Unknown(deviceId)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedDiscoveryDate(): String = firstDiscovery.format(dateTimeFormatter)

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedLastSeenDate(): String = lastSeen.format(dateTimeFormatter)
}