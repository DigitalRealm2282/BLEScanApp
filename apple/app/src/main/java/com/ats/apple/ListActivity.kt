package com.ats.apple

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ats.apple.adapter.BeaconAdapter
import com.ats.apple.data.DBHelper
import com.ats.apple.data.DeviceManager
import com.ats.apple.data.DeviceType
import com.ats.apple.data.model.BaseDevice
import com.ats.apple.util.types.AirPods
import com.ats.apple.util.types.AirTag
import com.ats.apple.util.types.AppleDevice
import com.ats.apple.util.types.Beacon
import com.ats.apple.util.types.FindMy
import com.ats.apple.util.types.IPhoneDevice
import com.ats.apple.util.types.Tile
import com.ats.apple.util.types.Unknown
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.lang.Exception


class ListActivity : AppCompatActivity() {
//    private val leDeviceListAdapter = LeDeviceListAdapter()

//    private val device_list : ArrayList<BluetoothDevice> = ArrayList()

    private val devfilters: MutableList<ScanFilter> = ArrayList()

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private val SCAN_PERIOD: Long = 10000
    private var scanning = false
    private val handler = Handler()

//    private var mGatt: BluetoothGatt? = null
    private var tx:TextView?=null
    private var Name:String?=null

    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    private lateinit var beacons: ArrayList<Beacon>
    private var db :DBHelper?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        setupUI()
        beacons = ArrayList<Beacon>()
        tx = findViewById<TextView>(R.id.textND)
        displayBeaconsList()

        db = DBHelper(this, null)

//
//        try {
//            //Add 7 function to 7 devices categories and add on each on click then add them to list
//            val iphone = db!!.getAllIphoneDetail()
//            Toast.makeText(this@ListActivity,iphone?.get(1).toString(),Toast.LENGTH_SHORT).show()
//
//        }catch (ex:SQLiteException){
//            Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
//        }

        scanLeDevice()

    }


    @SuppressLint("MissingPermission")
    private fun setupUI(){

        if (intent.getStringExtra("Category")== "AirPods"){
            this.title = "AirPods"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category")== "IPhone"){
            this.title = "IPhone"

//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category")== "UNK"){
            this.title = "Unknown Devices"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category")== "Tile"){
            this.title = "Tiles"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category")== "AirTag"){
            this.title = "AirTags"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category")== "FMD"){
            this.title = "Find My Devices"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category")== "Apple"){
            this.title = "Apple Devices"
//            scanFilters(intent.getStringExtra("Category")!!)

        }


        val rl = findViewById<SwipeRefreshLayout>(R.id.refreshlayout)
//        rl.setOnRefreshListener {
//            if (beacons.isNotEmpty()){
////                Toast.makeText(this@ListActivity,"Data Detected",Toast.LENGTH_SHORT).show()
////                recyclerView = findViewById<View>(R.id.recycler) as RecyclerView
////                recyclerView!!.visibility = View.VISIBLE
////                tx?.visibility = View.GONE
//                displayBeaconsList()
//                rl.isRefreshing = false
//                scanLeDevice()
//            }else{
////                recyclerView = findViewById<View>(R.id.recycler) as RecyclerView
////                recyclerView!!.visibility = View.GONE
////                tx?.visibility = View.VISIBLE
//                Toast.makeText(this@ListActivity,"No Data Found",Toast.LENGTH_SHORT).show()
//                rl.isRefreshing = false
//                recreate()
//                scanLeDevice()
//            }
//        }

        rl.setOnRefreshListener {
            if (!scanning){
//                Toast.makeText(this@ListActivity,"Data Detected",Toast.LENGTH_SHORT).show()
//                recyclerView = findViewById<View>(R.id.recycler) as RecyclerView
//                recyclerView!!.visibility = View.VISIBLE
//                tx?.visibility = View.GONE
                displayBeaconsList()
                rl.isRefreshing = false
                scanLeDevice()
            }else{
//                recyclerView = findViewById<View>(R.id.recycler) as RecyclerView
//                recyclerView!!.visibility = View.GONE
//                tx?.visibility = View.VISIBLE
                displayBeaconsList()
                Toast.makeText(this@ListActivity,"Already Scanning",Toast.LENGTH_SHORT).show()
                rl.isRefreshing = false

                scanLeDevice()
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.scanFAB)
        fab.setOnClickListener {
            if (!scanning){
                scanLeDevice()
                Toast.makeText(this@ListActivity,"Scanning",Toast.LENGTH_SHORT).show()
            }else{
                m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
                Toast.makeText(this@ListActivity,"Stopped",Toast.LENGTH_SHORT).show()

            }
        }
    }

    // Device scan callback.
//    private val leScanCallback: ScanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//            leDeviceListAdapter.addDevice(result.device)
//            leDeviceListAdapter.notifyDataSetChanged()
//        }
//    }



    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {

        GlobalScope.launch(Dispatchers.IO) {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback) }, SCAN_PERIOD)

                scanning = true
                val sett = ScanSettings.SCAN_MODE_LOW_LATENCY
                val settBuilder = ScanSettings.Builder().setScanMode(sett).build()
                m_bluetoothAdapter!!.bluetoothLeScanner.startScan(
                    scanFilters(intent.getStringExtra("Category")!!),
                    settBuilder,
                    lleScanCallback
                )

            } else {
                scanning = false
                m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)

            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    val lleScanCallback = object: ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("Error", "Scan Failed: $errorCode")
//            val view = findViewById<SwipeRefreshLayout>(R.id.refreshlayout)
//            view.let {
//                Snackbar.make(
//                    it,
//                    R.string.ble_service_connection_error,
//                    Snackbar.LENGTH_LONG
//                )
//            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            GlobalScope.launch(Dispatchers.Main) {
                with(Dispatchers.Main) {
                    if (result!=null) {
                        val Dev = BaseDevice(result)
//                        checkType(result)
                        resToBeacon(result, DeviceManager.getDeviceType(result))

                    }
                }

                val btDevice = result!!.device
                val uuidsFromScan = result.scanRecord?.serviceUuids.toString()

                //check that this is closebeacons device by UUIDS &
                // device with same address is not contains in list.

                //if((uuidsFromScan != null) && (UUIDS.equals(uuidsFromScan)) &&
//                if (!result.device.name.isNullOrEmpty()) {
//                    if (checkMacAddress(result.device.address.toString())) {
//                        val iBeacon = Beacon(
//                            result.device.name.toString(),
//                            result.device.address.toString(),
//                            result.rssi.toString(),
//                            result.scanRecord?.serviceUuids.toString()
//                        )
//                        beacons?.add(iBeacon)
//                    }
//                }else{
//                    if (checkMacAddress(result.device.address.toString())) {
//                        val iBeacon = Beacon(
//                            "device 1",
//                            result.device.address.toString(),
//                            result.rssi.toString(),
//                            result.scanRecord?.serviceUuids.toString()
//                        )
//                        beacons?.add(iBeacon)
//                    }
//                }
//                displayBeaconsList()
//                connectToDevice(btDevice)
            }

        }

    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun resToBeacon(result: ScanResult,type: DeviceType){
        when(type.name){
            IPhoneDevice.deviceType.name->{
                try {
                    //Add 7 function to 7 devices categories and add on each on click then add them to list
                    val iphone = db!!.getAllIphoneDetail()
                    Toast.makeText(this@ListActivity,iphone?.get(0).toString(),Toast.LENGTH_SHORT).show()

                }catch (ex:SQLiteException){
                    Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
                }
            }
            AirPods.deviceType.name->{
                try {
                    //Add 7 function to 7 devices categories and add on each on click then add them to list
                    val iphone = db!!.getAllAirTagDetail()
                    Toast.makeText(this@ListActivity,iphone?.get(0).toString(),Toast.LENGTH_SHORT).show()

                }catch (ex:SQLiteException){
                    Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
                }
            }
            AirTag.deviceType.name->{
                try {
                    //Add 7 function to 7 devices categories and add on each on click then add them to list
                    val iphone = db!!.getAllAirTagDetail()
                    Toast.makeText(this@ListActivity,iphone?.get(0).toString(),Toast.LENGTH_SHORT).show()

                }catch (ex:SQLiteException){
                    Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
                }
            }
            AppleDevice.deviceType.name->{
                try {
                    //Add 7 function to 7 devices categories and add on each on click then add them to list
                    val iphone = db!!.getAllAppleDetail()
                    Toast.makeText(this@ListActivity,iphone?.getColumnName(0).toString(),Toast.LENGTH_SHORT).show()

                }catch (ex:SQLiteException){
                    Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
                }
            }
            FindMy.deviceType.name->{
                try {
                    //Add 7 function to 7 devices categories and add on each on click then add them to list
                    val iphone = db!!.getAllFindMyDetail()
                    Toast.makeText(this@ListActivity,iphone?.get(0).toString(),Toast.LENGTH_SHORT).show()

                }catch (ex:SQLiteException){
                    Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
                }
            }
            Tile.deviceType.name->{
                try {
                    //Add 7 function to 7 devices categories and add on each on click then add them to list
                    val iphone = db!!.getAllTilesDetail()
                    Toast.makeText(this@ListActivity,iphone?.get(0).toString(),Toast.LENGTH_SHORT).show()

                }catch (ex:SQLiteException){
                    Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
                }
            }
            Unknown.deviceType.name->{
                try {
                    //Add 7 function to 7 devices categories and add on each on click then add them to list
                    val iphone = db!!.getAllUnknownDetails()
                    Toast.makeText(this@ListActivity,iphone?.get(0).toString(),Toast.LENGTH_SHORT).show()

                }catch (ex:SQLiteException){
                    Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                try {
                    //Add 7 function to 7 devices categories and add on each on click then add them to list
                    Log.e("LA","No Devices in Db")
                    val iphone = db!!.getAllAppleDetail()!!
                    Toast.makeText(this@ListActivity,iphone.getColumnName(0).toString(),Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@ListActivity,iphone.getColumnName(1).toString(),Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@ListActivity,iphone.getColumnName(2).toString(),Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@ListActivity,iphone.getColumnName(3).toString(),Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@ListActivity,iphone.getColumnName(4).toString(),Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@ListActivity,iphone.getColumnName(5).toString(),Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@ListActivity,iphone.getColumnName(6).toString(),Toast.LENGTH_SHORT).show()

//                    Toast.makeText(this@ListActivity,iphone.getColumnName(7).toString(),Toast.LENGTH_SHORT).show()

//                    Toast.makeText(this@ListActivity,iphone.getColumnName(1).toString(),Toast.LENGTH_SHORT).show()
//                    Toast.makeText(this@ListActivity,iphone.getColumnName(2).toString(),Toast.LENGTH_SHORT).show()
//                    Toast.makeText(this@ListActivity,iphone.getColumnName(3).toString(),Toast.LENGTH_SHORT).show()

                }catch (ex:SQLiteException){
                    Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
                }
            }

        }

        // Add first Desc Time and Last Seen
        if (!result.device.name.isNullOrEmpty()) {
            if (checkMacAddress(result.device.address.toString())) {
                val iBeacon = Beacon(
                    result.device.name.toString(),
                    result.device.address.toString(),
                    result.rssi.toString(),
                    result.scanRecord?.serviceUuids.toString(),
                    type,
                    BaseDevice(result).firstDiscovery,
                    BaseDevice(result).lastSeen
                )
//                beacons.add(iBeacon)
                checkLastSeen(iBeacon)

                addBeacon(iBeacon)

            }
        }else{
            for (i in 0 until beacons.size+1) {
                Name = "Device $i"
            }

            if (checkMacAddress(result.device.address.toString())&& Name != null && Name != "null") {
                val iBeacon = Beacon(
                    Name.toString(),
                    result.device.address.toString(),
                    result.rssi.toString(),
                    result.scanRecord?.serviceUuids.toString(), type,
                    BaseDevice(result).firstDiscovery,
                    BaseDevice(result).lastSeen
                )
//                beacons.add(iBeacon)
                checkLastSeen(iBeacon)
                addBeacon(iBeacon)

            }
        }

        if (beacons.isNotEmpty()){
            val ls = db?.getDev(beacons[0])?.get(0)?.toString()
            Toast.makeText(this@ListActivity, ls.toString(),Toast.LENGTH_SHORT).show()
        }
        adapter!!.notifyDataSetChanged()
    }

    @SuppressLint("NewApi")
    private fun checkLastSeen(beacon: Beacon){
        try {
            if (beacon.uuids == db?.getDev(beacon)?.get(1)) {
                db?.updateCourse(
                    "devices_table",
                    beacon.name,
                    beacon.serialNumber,
                    beacon.rssi.toFloat(),
                    beacon.firstDiscovery.second.toString(),
                    beacon.lastSeen.second.toString()
                )
            }
        }catch (ex:Exception){Log.e("LA",ex.message.toString())}
    }

//
    private fun scanFilters(FilterCheck: String): MutableList<ScanFilter> {
        //AirPods
        val APbluetoothFilter: ScanFilter = ScanFilter.Builder().setManufacturerData(0x4C, byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x18).toByte()), byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())).build()
        //Apple Dev
        val ADbluetoothFilter: ScanFilter = ScanFilter.Builder().setManufacturerData(0x4C, byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x00).toByte()), byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())).build()
        //AirTag
        val ATbluetoothFilter: ScanFilter = ScanFilter.Builder().setManufacturerData(0x4C, byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()), byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())).build()
        //IPhone
        val PhonebluetoothFilter: ScanFilter = ScanFilter.Builder().setManufacturerData(0x4C, byteArrayOf((0x12).toByte(), (0x02).toByte(), (0x18).toByte()), byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())).build()
        //unk
        val unkFilter :ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte())
            ).build()

        val FMDFilter :ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0xFF).toByte())).build()

        val TileFilter = ScanFilter.Builder().setServiceUuid(Tile.offlineFindingServiceUUID).build()


        val AllDevFilter :ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
               null,
                null).build()


        when (FilterCheck) {
            "AirPods" -> {
                devfilters.add(0,APbluetoothFilter)
            }
            "Apple" -> {
                devfilters.add(0,ADbluetoothFilter)
                devfilters.add(1,AllDevFilter)

            }
            "IPhone" -> {
                devfilters.add(0,PhonebluetoothFilter)
            }
            "AirTag" -> {
                devfilters.add(0,ATbluetoothFilter)
            }
            "FMD" -> {
                devfilters.add(0,FMDFilter)
            }
            "UNK" -> {
                devfilters.add(0,unkFilter)
            }
            else -> {
                devfilters.add(0,TileFilter)
            }
        }
        return devfilters
    }


    @SuppressLint("MissingPermission", "NewApi")
    private fun checkType(result: ScanResult) {
        val baseDev = BaseDevice(result)

        val type = baseDev.deviceType
//        val type = DeviceManager.getDeviceType(result)

        if (type == DeviceType.AIRPODS && intent.getStringExtra("Category") == "AirPods") {
            resToBeacon(result, type)

            try {
                //Add 7 function to 7 devices categories and add on each on click then add them to list
                db!!.getAllAirPodsDetail()
            }catch (ex:SQLiteException){
                Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
            }

        } else if (DeviceType.IPhone == type && intent.getStringExtra("Category") == "IPhone") {
            resToBeacon(result, type)

            try {
                val iphone = db!!.getAllIphoneDetail()
                Toast.makeText(this@ListActivity,iphone?.get(0).toString(),Toast.LENGTH_SHORT).show()

            }catch (ex:SQLiteException){
                Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
            }

        } else if (type == DeviceType.AIRTAG && intent.getStringExtra("Category") == "AirTag") {
            Toast.makeText(this@ListActivity, "AirTag Detected", Toast.LENGTH_SHORT).show()
            resToBeacon(result, type)
            try {
                //Add 7 function to 7 devices categories and add on each on click then add them to list
                db!!.getAllAirTagDetail()
            }catch (ex:SQLiteException){
                Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
            }

        } else if (type == DeviceType.FIND_MY && intent.getStringExtra("Category") == "FMD") {
            resToBeacon(result, type)

            try {
                //Add 7 function to 7 devices categories and add on each on click then add them to list
                db!!.getAllFindMyDetail()
            }catch (ex:SQLiteException){
                Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
            }

        } else if (type == DeviceType.TILE && intent.getStringExtra("Category") == "Tile") {
            resToBeacon(result, type)
            try {
                //Add 7 function to 7 devices categories and add on each on click then add them to list
                db!!.getAllTilesDetail()
            }catch (ex:SQLiteException){
                Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
            }

        } else if (type == DeviceType.APPLE && intent.getStringExtra("Category") == "Apple") {
            resToBeacon(result, type)
            try {
                //Add 7 function to 7 devices categories and add on each on click then add them to list
                db!!.getAllAppleDetail()
            }catch (ex:SQLiteException){
                Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
            }

        } else if (type == DeviceType.UNKNOWN && intent.getStringExtra("Category") == "UNK") {
            resToBeacon(result, type)
            try {
                //Add 7 function to 7 devices categories and add on each on click then add them to list
                db!!.getAllUnknownDetails()
            }catch (ex:SQLiteException){
                Toast.makeText(this@ListActivity,"Failed to load Database",Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("LA", "not matching")
        }

    }


    fun displayBeaconsList() {
        beacons.sortBy { beacon -> beacon.rssi }
        recyclerView = findViewById<View>(R.id.recycler) as RecyclerView
        layoutManager = LinearLayoutManager(this)
        adapter = BeaconAdapter(this, beacons)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = adapter
        tx?.visibility = View.GONE
        adapter!!.notifyDataSetChanged()
    }

    fun checkMacAddress(address: String): Boolean {
        if (beacons.isNotEmpty()) {
            val it: Iterator<Beacon> = beacons.iterator()
            while (it.hasNext()) {
                val beacon = it.next()
                val addressBeacon = beacon.address
                val bool = addressBeacon == address
                if (bool) {
                    return false
                }
            }
        }
        return true
    }

    fun addBeacon(iBeacon: Beacon) {
        if (beacons.isNotEmpty()) {
            val it: MutableIterator<Beacon> = beacons.iterator() as MutableIterator<Beacon>
            while (it.hasNext()) {
                val beacon = it.next()
                val addressBeacon = beacon.address
                val addressIBeacon = iBeacon.address
                val bool = addressBeacon == addressIBeacon
                if (bool) {
                    it.remove()
                }
            }
        }
        beacons.add(iBeacon)
    }


//    @SuppressLint("MissingPermission")
//    fun connectToDevice(device: BluetoothDevice) {
//        if (mGatt == null) {
//            mGatt = device.connectGatt(this, false, gattCallback)
////            scanLeDevice(false) // will stop after first device detection
//        }
//    }
//    @SuppressLint("MissingPermission")
//    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
//        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//            Log.i("onConnectionStateChange", "Status: $status")
//            when (newState) {
//                BluetoothProfile.STATE_CONNECTED -> {
//                    Log.i("gattCallback", "STATE_CONNECTED")
//                    gatt.discoverServices()
//                }
//                BluetoothProfile.STATE_DISCONNECTED -> Log.e("gattCallback", "STATE_DISCONNECTED")
//                else -> Log.e("gattCallback", "STATE_OTHER")
//            }
//        }
//
//        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//            val services = gatt.services
//            Log.i("onServicesDiscovered", services.toString())
//            gatt.readCharacteristic(services[1].characteristics[0])
//        }
//
//        override fun onCharacteristicRead(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic, status: Int
//        ) {
//            Log.i("onCharacteristicRead", characteristic.toString())
//            gatt.disconnect()
//        }
//    }

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


    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()
        m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
//        beacons.clear()
        devfilters.clear()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
//        beacons.clear()

        m_bluetoothAdapter?.bluetoothLeScanner?.startScan(scanFilters(intent.getStringExtra("Category")!!),ScanSettings.Builder().setScanMode(
            ScanSettings.SCAN_MODE_LOW_LATENCY
        ).build(),lleScanCallback)

        super.onResume()
    }
    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
        m_bluetoothAdapter = null
        beacons.clear()
        devfilters.clear()
//        unregisterReceiver(mReceiver)
    }
}