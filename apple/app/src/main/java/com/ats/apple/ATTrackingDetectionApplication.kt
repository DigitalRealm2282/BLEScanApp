package com.ats.apple

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.material.color.DynamicColors


class ATTrackingDetectionApplication : Application() {
    private val activityLifecycleCallbacks = ATTDLifecycleCallbacks()


    override fun onCreate() {
        instance = this
        super.onCreate()


        if (BuildConfig.DEBUG) {
            // We use this to access our logs from a file for on device debugging
//            File(filesDir.path + "/logs.log").createNewFile()
//            val t: Timber.Tree = FileLoggerTree.Builder()
//                .withSizeLimit(500_000)
//                .withDir(filesDir)
//                .withFileName("logs.log")
//                .withMinPriority(Log.VERBOSE)
//                .appendToFile(true)
//                .build()
//
//            Timber.plant(t)
//            Timber.v("File tree planted")
        }

        DynamicColors.applyToActivitiesIfAvailable(this)

        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
//
//        Util.setSelectedTheme(sharedPreferences)

//        if (showOnboarding() or !hasPermissions()) {
//            startOnboarding()
//        }else {
//            backgroundWorkScheduler.launch()
//        }

//        if (SharedPrefs.shareData) {
//            backgroundWorkScheduler.scheduleShareData()
//        }
//
//        if (SharedPrefs.lastDataDonation == null) {
//            SharedPrefs.lastDataDonation = LocalDateTime.now()
//        }

//        notificationService.setup()
//        notificationService.scheduleSurveyNotification(false)
//        BackgroundWorkScheduler.scheduleAlarmWakeupIfScansFail()
    }

//    private fun showOnboarding(): Boolean = !SharedPrefs.onBoardingCompleted or SharedPrefs.showOnboarding

    private fun hasPermissions(): Boolean {
        val requiredPermissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        for (permission in requiredPermissions) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return false
            }
        }
        return true
    }

    private fun startOnboarding() =
        startActivity(Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })

    companion object {
        private lateinit var instance: ATTrackingDetectionApplication
        fun getAppContext(): Context = instance.applicationContext
        fun getCurrentActivity(): Context? {
            return try {
                instance.activityLifecycleCallbacks.currentActivity
            }catch (e: UninitializedPropertyAccessException) {
//                Timber.e("Failed accessing current activity $e")
                null
            }
        }
        fun getCurrentApp(): ATTrackingDetectionApplication? {
            return instance
        }
        //TODO: Add real survey URL
//        val SURVEY_URL = "https://survey.seemoo.tu-darmstadt.de/index.php/117478?G06Q39=AirGuardAppAndroid&newtest=Y&lang=en"
//        val SURVEY_IS_RUNNING = true
    }
}