//package com.ats.apple.util.risk
//
//import android.annotation.SuppressLint
//import com.ats.apple.data.model.BaseDevice
//import com.ats.apple.repo.DeviceRepository
//import com.ats.apple.repository.BeaconRepository
//import java.time.LocalDateTime
//import java.time.ZoneId
//import java.time.temporal.ChronoUnit
//import java.util.*
//import kotlin.math.abs
//
//@SuppressLint("NewApi")
//class RiskLevelEvaluator(
//    private val deviceRepository: DeviceRepository,
//    private val beaconRepository: BeaconRepository
//) {
//
//    /**
//     * Evaluates the risk that the user is at. For this all notifications sent (equals trackers discovered) for the last `RELEVANT_DAYS` are checked and a risk score is evaluated
//     */
//    fun evaluateRiskLevel(): RiskLevel {
//        val relevantDate = relevantTrackingDate
//        val baseDevices: List<BaseDevice> = deviceRepository.trackingDevicesSince(relevantDate)
//
//        val totalTrackers = baseDevices.count()
//
//        if (totalTrackers == 0) {
//            return RiskLevel.LOW
//        } else {
//            val trackedLocations = baseDevices.map {
//                beaconRepository.getDeviceBeacons(it.address)
//            }.flatten()
//
//            val firstBeacon = trackedLocations.first()
//            val lastBeacon = trackedLocations.last()
//
//            val daysDiff = abs(firstBeacon.receivedAt.until(lastBeacon.receivedAt, ChronoUnit.DAYS))
//            return if (daysDiff >= 1) {
//                //High risk
//                RiskLevel.HIGH
//            } else {
//                RiskLevel.MEDIUM
//            }
//        }
//    }
//
//    fun getLastTrackerDiscoveryDate(): Date {
//        val relevantDate = relevantTrackingDate
//        val baseDevices: List<BaseDevice> = deviceRepository.trackingDevicesSince(relevantDate)
//            .sortedByDescending {  }
//
//        return baseDevices.firstOrNull()
//            ?.let { Date.from(it.lastSeen.atZone(ZoneId.systemDefault()).toInstant()) }
//            ?: Date()
//    }
//
//    fun getNumberRelevantTrackers(): Int {
//        val relevantDate = LocalDateTime.now().minusDays(RELEVANT_DAYS)
//        val baseDevices: List<BaseDevice> = deviceRepository.trackingDevicesSince(relevantDate)
//
//        return baseDevices.count()
//    }
//
//    companion object {
//        const val RELEVANT_DAYS: Long = 14
//        val relevantTrackingDate: LocalDateTime = LocalDateTime.now().minusDays(RELEVANT_DAYS)
//    }
//}