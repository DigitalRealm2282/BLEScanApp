package com.ats.airflagger

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat


fun checkAllPerms(activity: Activity?,context: Context?){
    if (ActivityCompat.checkSelfPermission(context!!,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
        || ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS) == PackageManager.PERMISSION_DENIED
        || ActivityCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED
        || ActivityCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_PRIVILEGED) == PackageManager.PERMISSION_DENIED
        || ActivityCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED
    ) {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_PRIVILEGED
                ),
            112
        )

    }

}

fun sdkhigh(context: Context?,activity: Activity?){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        if (ActivityCompat.checkSelfPermission(context!!,Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED
            || ActivityCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED
            || ActivityCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_ADVERTISE),1046)

    }

}
