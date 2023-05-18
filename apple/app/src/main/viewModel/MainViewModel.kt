//package com.ats.apple.viewModel
//
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.asLiveData
//import com.ats.apple.ATTrackingDetectionApplication
//import com.ats.apple.R
//import com.ats.apple.data.DeviceType
//import com.ats.apple.repo.DeviceRepository
//import com.ats.apple.util.risk.RiskLevel
//import com.ats.apple.util.risk.RiskLevelEvaluator
//import javax.inject.Inject
//
//class MainViewModel@Inject constructor(
//    riskLevelEvaluator: RiskLevelEvaluator,
//    deviceRepository: DeviceRepository
//):ViewModel() {
//
//    val countNotTracking = deviceRepository.countNotTracking.asLiveData()
//    val countIgnored = deviceRepository.countIgnored.asLiveData()
//    val countTracking = deviceRepository.trackingDevicesSinceCount(RiskLevelEvaluator.relevantTrackingDate).asLiveData()
//
//    val countAirTags = deviceRepository.countForDeviceType(DeviceType.AIRTAG).asLiveData()
//    val countFindMy = deviceRepository.countForDeviceType(DeviceType.FIND_MY).asLiveData()
//    val countTile = deviceRepository.countForDeviceType(DeviceType.TILE).asLiveData()
//
//    var riskColor: Int
//
//    init {
//        val context = ATTrackingDetectionApplication.getAppContext()
//        riskColor = when (riskLevelEvaluator.evaluateRiskLevel()) {
//            RiskLevel.LOW -> ContextCompat.getColor(context, R.color.risk_low)
//            RiskLevel.MEDIUM -> ContextCompat.getColor(context, R.color.risk_medium)
//            RiskLevel.HIGH -> ContextCompat.getColor(context, R.color.risk_high)
//        }
//    }
//}