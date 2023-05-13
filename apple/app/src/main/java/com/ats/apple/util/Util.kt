package com.ats.apple.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.ats.apple.MainActivity

object Util {

    const val MAX_ZOOM_LEVEL = 19.5
    const val ZOOMED_OUT_LEVEL = 15.0

    fun checkAndRequestPermission(permission: String,context: Context,activity: Activity): Boolean {
        val context = context
        when {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                return true
            }
            shouldShowRequestPermissionRationale(activity, permission) -> {
                val bundle = Bundle().apply { putString("permission", permission) }
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtras(bundle)
                }
                context.startActivity(intent)
                return false
            }
            else -> {
                requestPermissions(
                    activity,
                    arrayOf(permission),
                    0
                )
                return true
            }
        }
    }

    fun checkBluetoothPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||  ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

//    fun enableMyLocationOverlay(
//        map:MapView
//    ) {
//        val context = IOSDetectionApplication.getAppContext()
//        val locationOverlay = MyLocationNewOverlay(map)
//        val options = BitmapFactory.Options()
//        val bitmapPerson =
//            BitmapFactory.decodeResource(context.resources, R.drawable.mylocation, options)
//        locationOverlay.setPersonIcon(bitmapPerson)
//        locationOverlay.setPersonHotspot((26.0 * 1.6).toFloat(), (26.0 * 1.6).toFloat())
//        locationOverlay.setDirectionArrow(bitmapPerson, bitmapPerson)
//        locationOverlay.enableMyLocation()
//        locationOverlay.enableFollowLocation()
//        map.overlays.add(locationOverlay)
//        map.controller.setZoom(ZOOMED_OUT_LEVEL)
//    }
//
//    suspend fun setGeoPointsFromList(
//        beaconList: List<Beacon>,
//        map: MapView,
//        connectWithPolyline: Boolean = false,
//        onMarkerWindowClick: ((beacon: Beacon) -> Unit)? = null
//    ): Boolean {
//
//        val context = IOSDetectionApplication.getAppContext()
//        val copyrightOverlay = CopyrightOverlay(context)
//
//        val mapController = map.controller
//        val geoPointList = ArrayList<GeoPoint>()
//        val markerList = ArrayList<Marker>()
//
//
//        map.setTileSource(TileSourceFactory.MAPNIK)
//        map.setUseDataConnection(true)
//        map.setMultiTouchControls(true)
//
//        map.overlays.add(copyrightOverlay)
//
//        // Causes crashes when the view gets destroyed and markers are still added. Will get fixed in the next Version!
//        withContext(Dispatchers.Default) {
//            beaconList
//                .filter { it.latitude != null && it.longitude != null }
//                .map { beacon ->
//                    if (map.isShown == false) {
//                        return@map
//                    }
//                    val marker = Marker(map)
//                    val geoPoint = GeoPoint(beacon.longitude!!, beacon.latitude!!)
//                    marker.infoWindow = DeviceMarkerInfo(
//                        R.layout.include_device_marker_window, map, beacon, onMarkerWindowClick
//                    )
//                    marker.position = geoPoint
//                    marker.icon = ContextCompat.getDrawable(
//                        context,
//                        R.drawable.ic_baseline_location_on_45_black
//                    )
//                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//                    geoPointList.add(geoPoint)
//                    markerList.add(marker)
//
//                    marker.setOnMarkerClickListener { clickedMarker, _ ->
//                        if (clickedMarker.isInfoWindowShown) {
//                            clickedMarker.closeInfoWindow()
//                        } else {
//                            clickedMarker.showInfoWindow()
//                        }
//                        true
//                    }
//                }
//        }
//        map.overlays.addAll(markerList)
//
//        Timber.d("Added ${geoPointList.size} markers to the map!")
//
//        if (connectWithPolyline) {
//            val line = Polyline(map)
//            line.setPoints(geoPointList)
//            line.infoWindow = null
//            map.overlays.add(line)
//        }
//        if (geoPointList.isEmpty()) {
//            mapController.setZoom(MAX_ZOOM_LEVEL)
//            return false
//        }
//        val myLocationOverlay = map.overlays.firstOrNull{ it is MyLocationNewOverlay} as? MyLocationNewOverlay
//        myLocationOverlay?.disableFollowLocation()
//        val boundingBox = BoundingBox.fromGeoPointsSafe(geoPointList)
//
//        map.post {
//            try {
//                Timber.d("Zoom in to bounds -> $boundingBox")
//                map.zoomToBoundingBox(boundingBox, true, 100, MAX_ZOOM_LEVEL, 1)
//
//            } catch (e: IllegalArgumentException) {
//                mapController.setCenter(boundingBox.centerWithDateLine)
//                mapController.setZoom(10.0)
//                Timber.e("Failed to zoom to bounding box! ${e.message}")
//            }
//        }
//
////            map.zoomToBoundingBox(boundingBox, true)
//
//        return true
//    }
//
//    fun setSelectedTheme(sharedPreferences: SharedPreferences) {
//        when (sharedPreferences.getString("app_theme", "system_default")) {
//            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            "system_default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//        }
//    }
}
