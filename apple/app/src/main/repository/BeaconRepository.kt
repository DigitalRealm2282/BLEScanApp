//package com.ats.apple.repository
//
//import androidx.annotation.WorkerThread
//import com.ats.apple.data.model.BaseDevice
//import com.ats.apple.data.daos.BeaconDao
//import com.ats.apple.data.model.Beacon
//import kotlinx.coroutines.flow.Flow
//import java.time.LocalDateTime
//import javax.inject.Inject
//
//class BeaconRepository @Inject constructor(
//    private val beaconDao: BeaconDao
//) {
//    val totalCount: Flow<Int> = beaconDao.getTotalCount()
//
//    val locationCount: Flow<Int> = beaconDao.getTotalLocationCount()
//
//    fun totalBeaconCountChange(since: LocalDateTime): Flow<Int> =
//        beaconDao.getTotalCountChange(since)
//
//    fun totalLocationCountChange(since: LocalDateTime): Flow<Int> =
//        beaconDao.getLatestLocationsCount(since)
//
//    fun getLatestBeacons(since: LocalDateTime): List<Beacon> = beaconDao.getLatestBeacons(since)
//
//    fun latestBeaconsCount(since: LocalDateTime): Flow<Int> = beaconDao.getLatestBeaconCount(since)
//
//    val latestBeaconPerDevice: Flow<List<Beacon>> = beaconDao.getLatestBeaconPerDevice()
//
//    fun getBeaconsSince(since: LocalDateTime): Flow<List<Beacon>> = beaconDao.getBeaconsSince(since)
//
//    fun getDeviceBeaconsCount(deviceAddress: String): Int =
//        beaconDao.getDeviceBeaconsCount(deviceAddress)
//
//    fun getDeviceBeacons(deviceAddress: String): List<Beacon> =
//        beaconDao.getDeviceBeacons(deviceAddress)
//
//    fun getDeviceBeaconsSince(deviceAddress: String, since: LocalDateTime): List<Beacon> =
//        beaconDao.getDeviceBeaconsSince(deviceAddress, since)
//
//    fun getBeaconsForDevices(baseDevices: List<BaseDevice>): List<Beacon> {
//        return baseDevices.map {
//            beaconDao.getDeviceBeacons(it.address)
//        }.flatten()
//    }
//
//    @WorkerThread
//    suspend fun insert(beacon: Beacon): Long = beaconDao.insert(beacon)
//}
