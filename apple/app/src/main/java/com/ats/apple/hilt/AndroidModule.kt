//package com.ats.apple.hilt
//
//import android.content.Context
//import android.content.SharedPreferences
//import android.location.LocationManager
//import android.preference.PreferenceManager
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.content.getSystemService
//import androidx.work.WorkManager
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import com.ats.apple.repo.BeaconRepository
//import com.ats.apple.repository.DeviceRepository
//import com.ats.apple.util.risk.RiskLevelEvaluator
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//class AndroidModule {
//    @Provides
//    @Singleton
//    fun workManager(workManagerProvider: WorkManagerProvider): WorkManager =
//        workManagerProvider.workManager
//
//    @Provides
//    @Singleton
//    fun notificationManagerCompat(@ApplicationContext context: Context): NotificationManagerCompat =
//        NotificationManagerCompat.from(context)
//
//    @Provides
//    @Singleton
//    fun sharedPreferences(@ApplicationContext context: Context): SharedPreferences =
//        PreferenceManager.getDefaultSharedPreferences(context)
//
//    @Provides
//    @Singleton
//    fun locationManager(@ApplicationContext context: Context): LocationManager =
//        context.getSystemService()!!
//
//    @Provides
//    @Singleton
//    fun riskLevelEvaluator(
//        deviceRepository: DeviceRepository,
//        beaconRepository: BeaconRepository
//    ): RiskLevelEvaluator = RiskLevelEvaluator(deviceRepository, beaconRepository)
//}