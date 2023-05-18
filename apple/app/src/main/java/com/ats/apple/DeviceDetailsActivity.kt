package com.ats.apple

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ats.apple.data.DeviceManager
import com.ats.apple.data.DeviceType
import com.ats.apple.util.CustomMarker
import com.ats.apple.util.types.AirPods
import com.ats.apple.util.types.AirTag
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DeviceDetailsActivity : AppCompatActivity() {

    private var m_bluetoothAdapter: BluetoothAdapter? = null
//    private val SCAN_PERIOD: Long = 15000

    var entries: MutableList<Entry?>? = ArrayList()

    private var scanning = false
    private val handler = Handler()
    private val scan_list : ArrayList<ScanResult> = ArrayList()
    private var address = ""
    private var name = ""
    private var lineChart:LineChart?=null
    private var mGatt: BluetoothGatt? = null
    private val devfilters: MutableList<ScanFilter> = ArrayList()
    private var rssi = ""
    private var type = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_details)


        val bundle = intent.extras
        address = bundle!!.getString("address")!!
        name = bundle.getString("name")!!
        rssi = bundle.getString("rssi")!!
        val uuids = bundle.getString("uuids")
        val serialNumber = bundle.getString("serialNumber")
        type = bundle.getString("Type")!!

//        Toast.makeText(this@DeviceDetailsActivity,type,Toast.LENGTH_SHORT).show()
        lineChart = findViewById(R.id.chart1)

        val rssi1 = findViewById<TextView>(R.id.rssiTxt)

        rssi1.text = rssi
//        chart!!.startAnimation()
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        setupUI(address,name)

        scanLeDevice()
    }

    private fun setupUI(address: String?, name: String?) {
        val Address = findViewById<TextView>(R.id.MacAddressTxt)
        Address.text = address
        val play = findViewById<MaterialButton>(R.id.play)

        when (type) {
            DeviceType.AIRTAG.name -> {
                play.visibility = View.VISIBLE
            }
            DeviceType.AIRPODS.name -> {
                play.visibility = View.VISIBLE
            }
            else -> {
                play.visibility = View.GONE
            }
        }
        val ll = findViewById<SwipeRefreshLayout>(R.id.refreshCl)
        ll.setOnRefreshListener {
            if (!scanning) {
                scanLeDevice()
                ll.isRefreshing = false

            }else{
                ll.isRefreshing = false
                Toast.makeText(this@DeviceDetailsActivity,"Already Scanning",Toast.LENGTH_SHORT).show()
            }
        }

        if (name!!.isNotEmpty() or !name.contains("Device")) {
            this.title = name
        }else{
            //Check Dev Type and return name of category
        }

    }

    private fun setupRisk(result: ScanResult){
        val rssi = findViewById<TextView>(R.id.rssiTxt)
        val rss = result.rssi

        if (rss >= 80) {
            rssi.setBackgroundResource(R.drawable.rtv_low)
            rssi.background = resources.getDrawable(R.drawable.rtv_low)
        }else if (rss < 50 ) {
            rssi.setBackgroundResource(R.drawable.rtv_high)
            rssi.background = resources.getDrawable(R.drawable.rtv_high)
        }else if (rss >=50) {
            rssi.setBackgroundResource(R.drawable.rounded_text_view)
            rssi.background = resources.getDrawable(R.drawable.rounded_text_view)
        }
    }

    override fun onResume() {
        super.onResume()
        scanLeDevice()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {

        GlobalScope.launch(Dispatchers.IO) {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
//                handler.postDelayed({
//                    scanning = false
//                    m_bluetoothAdapter!!.bluetoothLeScanner.stopScan(lleScanCallback)
//                    if (scan_list.isNotEmpty()) {
////                        Toast.makeText(
////                            this@ListActivity,
////                            "Found: " + scan_list[0].device.name + scan_list[0].device.address,
////                            Toast.LENGTH_SHORT
////                        ).show()
//                        Toast.makeText(this@DeviceDetailsActivity,"Found: " + scan_list.size.toString(), Toast.LENGTH_SHORT).show()
//                        Log.i("ScanListLA","Found: " + scan_list.size.toString())
//
//                    }else{
//                        Toast.makeText(this@DeviceDetailsActivity,"Empty", Toast.LENGTH_SHORT).show()
//
//                    }
//                }, SCAN_PERIOD)
                scanning = true
                val sett = ScanSettings.SCAN_MODE_LOW_LATENCY
                val settBuilder = ScanSettings.Builder().setScanMode(sett).build()
                m_bluetoothAdapter!!.bluetoothLeScanner.startScan(
                    devfilters,
                    settBuilder,
                    lleScanCallback
                )
//                DeviceManager.scanFilter

            } else {
                scanning = false
                m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
//                Toast.makeText(this@MainActivity, "Stopped", Toast.LENGTH_SHORT).show()

            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    val lleScanCallback = object: ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("Error", "Scan Failed: $errorCode")
        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            GlobalScope.launch(Dispatchers.Main) {

                val res = result
                val btDevice = res!!.device
                val uuidsFromScan = result.scanRecord?.serviceUuids.toString()
                // start scan get results callback & if bundle address = address of one of results get its rssi
                if (btDevice.address == address){
//                    checkType(result)
                    chartSetup(res.rssi.toFloat())
                    setupRisk(result)

//                    connectToDevice(result.device,result)
                }

            }

        }

    }

    private fun chartSetup(rss:Float){

        val rssi = findViewById<TextView>(R.id.rssiTxt)

//Part2

//        // now in hours
//        val now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
//        val values = ArrayList<Entry>()
//
//        // count = hours
//        val count = 1000
//        val to = (now + count).toFloat()
//
//        // increment by 1 hour
//        var x = now.toFloat()
//        while (x < to) {
//            val y: Float = (rss.toFloat())
//            entries?.add(Entry(x, y)) // add one entry per hour
//            x++
//        }
        entries?.add(Entry(rss, rss))
//        entries?.add(Entry(rss, x2))
//        entries?.add(Entry(rss, x3))
//        entries?.add(Entry(rss, x4))
//        entries?.add(Entry(rss, x5))
//        entries?.add(Entry(rss, x6))
//        entries?.add(Entry(rss, x7))
//        entries?.add(Entry(rss, x8))
//        entries?.add(Entry(rss, x9))

//Part3
        val vl = LineDataSet(entries, "Signal Strength")

        vl.axisDependency = YAxis.AxisDependency.RIGHT
        vl.color = Color.GREEN
        vl.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        vl.cubicIntensity = 0.2f
//Part4
        vl.setDrawValues(true)
        vl.setDrawFilled(true)
//        vl.enableDashedLine(10f,5f,1f)

//        vl.setDrawHighlightIndicators(true)
//        vl.isVisible = true
        vl.lineWidth = 3f
        vl.fillColor = R.color.green
        vl.fillAlpha = R.color.risk_high

//Part5
        lineChart!!.xAxis.labelRotationAngle = 0f

//Part6

        lineChart!!.data = LineData(vl)
        lineChart!!.notifyDataSetChanged()
        lineChart!!.lineData.notifyDataChanged()
        lineChart!!.invalidate()

//Part7
        lineChart!!.axisRight.isEnabled = false
        lineChart!!.xAxis.axisMaximum = 1+0.1f

//Part8
        lineChart!!.setTouchEnabled(true)
        lineChart!!.setPinchZoom(true)
        lineChart!!.background = getDrawable(R.drawable.gradiant_bg)

//Part9
        lineChart!!.description.text = "Sec"
        lineChart!!.setNoDataText("No Signal Detected")

//Part10
        lineChart?.xAxis?.spaceMin = 3.5f // As per you requiedment
        lineChart?.xAxis?.spaceMax = 0.1f // As per you requiedment
        lineChart!!.animateX(1800, Easing.EaseInBounce)

//        lineChart!!.invalidate()
//        lineChart!!.animateY(1800)
//Part11
        val markerView = CustomMarker(this@DeviceDetailsActivity, R.layout.marker_view)
        lineChart!!.marker = markerView

        if (rss > 80) {
            rssi.setBackgroundResource(0)

            rssi.setBackgroundResource(R.drawable.rtv_low)
            rssi.background = resources.getDrawable(R.drawable.rtv_low)
        }else if (rss < 50 ) {
            rssi.setBackgroundResource(0)

            rssi.setBackgroundResource(R.drawable.rtv_high)
            rssi.background = resources.getDrawable(R.drawable.rtv_high)
        }else if (rss >=50) {
            rssi.setBackgroundResource(0)
            rssi.setBackgroundResource(R.drawable.rounded_text_view)
            rssi.background = resources.getDrawable(R.drawable.rounded_text_view)
        }


        rssi.text = rss.toString()
    }


    private fun scanFilters(FilterCheck: String){
        val APbluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x18).toByte()), // Empty status byte?
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte()) // ff?
            )
            .build()

        val ADbluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x00).toByte()), // Empty status byte?
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte()) // ff?
            )
            .build()

        //AirPods
        val ATbluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
            )
            .build()

        //IPhone
        val PhonebluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x02).toByte(), (0x18).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
            )
            .build()

        val AllFilter : ScanFilter = ScanFilter.Builder().build()

        if (FilterCheck == "AirPods"){
            devfilters.add(0,APbluetoothFilter)

        }
        else if (FilterCheck == "Apple Devices"){
            devfilters.add(0,ADbluetoothFilter)
        }

        else if (FilterCheck == "IPhone"){
            devfilters.add(0,PhonebluetoothFilter)
        }
        else if (FilterCheck == "AirTags"){
            devfilters.add(0,ATbluetoothFilter)

        }else if (FilterCheck == "Find My Devices") {
            devfilters.add(0,AllFilter)
            // Add Find My Devices Filter
        }else{
            devfilters.add(0,AllFilter)

            // Add Unknown Filters
        }


    }



//    @SuppressLint("MissingPermission")
//    private fun checkType(result: ScanResult){
////        Toast.makeText(this@ListActivity,"CallBack Fired",Toast.LENGTH_SHORT).show()
//
//        if (!scan_list.contains(result)) {
//            scan_list.add(result)
//
//            if (DeviceManager.getDeviceType(result) == DeviceType.AIRPODS && this@DeviceDetailsActivity.title == name) {
////                Toast.makeText(this@ListActivity,"AirPodz",Toast.LENGTH_SHORT).show()
//                connectToDevice(result.device,result)
//
//            } else if (DeviceManager.getDeviceType(result) == DeviceType.IPhone && this@DeviceDetailsActivity.title == name) {
////                Toast.makeText(this@ListActivity,"Iphone",Toast.LENGTH_SHORT).show()
//
//            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.AIRTAG && this.title == name) {
////                Toast.makeText(this@ListActivity,"AirTag",Toast.LENGTH_SHORT).show()
//                AirPods.AIRPODS_START_SOUND_OPCODE
//
//                connectToDevice(result.device,result)
//
//
//
//            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.FIND_MY && this.title == name) {
////                Toast.makeText(this@ListActivity,"Find",Toast.LENGTH_SHORT).show()
//
//
//            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.TILE && this.title == name) {
////                Toast.makeText(this@ListActivity,"Tile",Toast.LENGTH_SHORT).show()
//
//                connectToDevice(result.device,result)
//
//
//
//            }else if(DeviceManager.getDeviceType(result) == DeviceType.APPLE && this.title == name){
////                Toast.makeText(this@ListActivity,"Apple",Toast.LENGTH_SHORT).show()
//
//            }else if(DeviceManager.getDeviceType(result) == DeviceType.UNKNOWN && this.title == name) {
////                Toast.makeText(this@ListActivity,"Unknown",Toast.LENGTH_SHORT).show()
//            } else {
////                Toast.makeText(this@DeviceDetailsActivity,"Unknown Checked Data or not matching ", Toast.LENGTH_SHORT).show()
//
//            }
//        }else{
//            val ind = scan_list.indexOf(result)
//            scan_list[ind]
//
//
//        }
//
//    }


    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice,scanResult: ScanResult) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback)
//            scanLeDevice(false) // will stop after first device detection
            if (DeviceManager.getDeviceType(scanResult) == DeviceType.AIRPODS) {
                Toast.makeText(this@DeviceDetailsActivity,"AirPods Detected",Toast.LENGTH_SHORT).show()
                AirPods.Companion.AIRPODS_SOUND_SERVICE
                AirPods.AIRPODS_SOUND_CHARACTERISTIC
                AirPods.AIRPODS_START_SOUND_OPCODE
            }
            if (DeviceManager.getDeviceType(scanResult) == DeviceType.AIRTAG){
                Toast.makeText(this@DeviceDetailsActivity,"AirTags Detected",Toast.LENGTH_SHORT).show()
                AirTag.Companion.AIR_TAG_SOUND_SERVICE
                AirTag.AIR_TAG_SOUND_CHARACTERISTIC
                AirTag.AIR_TAG_EVENT_CALLBACK

            }
        }
    }
    @SuppressLint("MissingPermission")
    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.i("onConnectionStateChange", "Status: $status")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("gattCallback", "STATE_CONNECTED")
                    gatt.discoverServices()
                    gatt.services
                    m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
                    scanning = false


                }
                BluetoothProfile.STATE_DISCONNECTED -> Log.e("gattCallback", "STATE_DISCONNECTED")
                else -> Log.e("gattCallback", "STATE_OTHER")

            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val services = gatt.services
            Log.i("onServicesDiscovered", services.toString())
            gatt.readCharacteristic(services[0].characteristics[0])
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            Log.i("onCharacteristicRead", characteristic.toString())
            gatt.disconnect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish()
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}