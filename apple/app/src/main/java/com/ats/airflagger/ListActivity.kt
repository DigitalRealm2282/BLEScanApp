package com.ats.airflagger

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Intent
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
import com.ats.airflagger.adapter.BeaconAdapter
import com.ats.airflagger.data.DBHelper
import com.ats.airflagger.data.DeviceManager
import com.ats.airflagger.data.DeviceType
import com.ats.airflagger.data.model.BaseDevice
import com.ats.airflagger.util.types.AirPods
import com.ats.airflagger.util.types.AirTag
import com.ats.airflagger.util.types.AppleDevice
import com.ats.airflagger.util.types.Beacon
import com.ats.airflagger.util.types.FindMy
import com.ats.airflagger.util.types.IPhoneDevice
import com.ats.airflagger.util.types.Tile
import com.ats.airflagger.util.types.Unknown
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*


class ListActivity : AppCompatActivity() {
//    private val leDeviceListAdapter = LeDeviceListAdapter()

//    private val device_list : ArrayList<BluetoothDevice> = ArrayList()

    private val devfilters: MutableList<ScanFilter> = ArrayList()

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private val SCAN_PERIOD: Long = 15000
    private var scanning = false
    private val handler = Handler()

    //    private var mGatt: BluetoothGatt? = null
    private var tx: TextView? = null
    private var Name: String? = null

    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    private lateinit var beacons: ArrayList<Beacon>
    private var db: DBHelper? = null
    var devicesDB: MutableList<Beacon>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        setupUI()
        beacons = ArrayList<Beacon>()
        tx = findViewById<TextView>(R.id.textND)
        //AirTag
        val ATbluetoothFilter: ScanFilter =
            ScanFilter.Builder().setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
            ).build()
        displayBeaconsList()
        db = DBHelper(this, null)
        //Two ways to list devices
        // 1st onscan result result to beacon and on scan filters add intent.getstring("category") // tested and get airTags only
        // 2nd search with all devices filters then check type of each device and add them when they match // tested and get rest of devices
        // 3rd way (Experimental) merge between both of them so we can get all devices
//        if (!startUp().contains(ATbluetoothFilter)){
//            scanLeDevice()
//        }else{
//            scanLeDeviceAll()
//        }

        scanLeDevice()

    }

    private fun startUp(): MutableList<ScanFilter> {
        //AirTag
        val ATbluetoothFilter: ScanFilter =
            ScanFilter.Builder().setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
            ).build()
        if (intent.getStringExtra("Category") == "AirTag")
            devfilters.add(ATbluetoothFilter)
        else
            devfilters



        return devfilters
    }


    @SuppressLint("MissingPermission")
    private fun setupUI() {

        if (intent.getStringExtra("Category") == "AirPods") {
            this.title = "AirPods"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category") == "IPhone") {
            this.title = "IPhone"

//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category") == "UNK") {
            this.title = "Unknown Devices"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category") == "Tile") {
            this.title = "Tiles"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category") == "AirTag") {
            this.title = "AirTags"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category") == "FMD") {
            this.title = "Find My Devices"
//            scanFilters(intent.getStringExtra("Category")!!)

        }
        if (intent.getStringExtra("Category") == "Apple") {
            this.title = "Apple Devices"
//            scanFilters(intent.getStringExtra("Category")!!)

        }


        val rl = findViewById<SwipeRefreshLayout>(R.id.refreshlayout)

        rl.setOnRefreshListener {
            if (!scanning) {

                displayBeaconsList()
                rl.isRefreshing = false
                scanLeDevice()
            } else {

                displayBeaconsList()
                Toast.makeText(this@ListActivity, "Already Scanning", Toast.LENGTH_SHORT).show()
                rl.isRefreshing = false

                scanLeDevice()
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.scanFAB)
        fab.setOnClickListener {
            if (!scanning) {
                scanLeDevice()
                Toast.makeText(this@ListActivity, "Scanning", Toast.LENGTH_SHORT).show()
            } else {
                m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
                Toast.makeText(this@ListActivity, "Stopped", Toast.LENGTH_SHORT).show()

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


    //three ways to list devices
    // 1st onscan result result to beacon and on scan filters add intent.getstring("category") // tested and get airTags only
    // 2nd search with all devices filters then check type of each device and add them when they match // tested and get rest of devices
    // 3rd way (Experimental) merge between both of them so we can get all devices
    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {

//        scanFilters(intent.getStringExtra("Category")!!)
//        scanFilters()
        GlobalScope.launch(Dispatchers.IO) {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
                }, SCAN_PERIOD)

                if (intent.getStringExtra("Category") == "AirTag") {
                    scanning = true
                    val sett = ScanSettings.SCAN_MODE_LOW_LATENCY
                    val settBuilder = ScanSettings.Builder().setScanMode(sett).build()
                    m_bluetoothAdapter?.bluetoothLeScanner?.startScan(
                        scanFilters(intent.getStringExtra("Category")!!),
                        settBuilder,
                        lleScanCallback
                    )
                }else{
                    scanning = true
                    val sett = ScanSettings.SCAN_MODE_LOW_LATENCY
                    val settBuilder = ScanSettings.Builder().setScanMode(sett).build()
                    m_bluetoothAdapter?.bluetoothLeScanner?.startScan(
                        scanFilters(),
                        settBuilder,
                        lleScanCallback
                    )
                }



            } else {
                scanning = false
                m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)

            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    val lleScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("Error", "Scan Failed: $errorCode")

        }


        //Two ways to list devices
        // 1st onscan result result to beacon and on scan filters with FilterCheck add intent.getstring("category") // tested and get airTags only
        // 2nd search with all devices filters then check type of each device and add them when they match // tested and get rest of devices
        // 3rd way (Experimental) merge between both of them so we can get all devices
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            GlobalScope.launch(Dispatchers.Main) {
                with(Dispatchers.Main) {
                    if (result != null) {
                        if (intent.getStringExtra("Category") == "AirTag") {
//                        val Dev = BaseDevice(result)
//                        checkType(result)
                            resToBeacon(result, DeviceManager.getDeviceType(result))
                        }else{
                            checkType(result)

                        }

                    }
                }

                val btDevice = result!!.device
                val uuidsFromScan = result.scanRecord?.serviceUuids.toString()

            }

        }

    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun resToBeacon(result: ScanResult, type: DeviceType) {
        devicesDB = beacons
        when (type.name) {
            IPhoneDevice.deviceType.name -> {
                try {
                    devicesDB = db?.getSpecificType(DeviceType.IPhone)
                } catch (ex: SQLiteException) {
                    Log.e("DataBaseLA", ex.message.toString())
                }
            }

            AirPods.deviceType.name -> {
                try {
                    devicesDB = db?.getSpecificType(DeviceType.AIRPODS)

                } catch (ex: SQLiteException) {
                    Log.e("DataBaseLA", ex.message.toString())
                }
            }

            AirTag.deviceType.name -> {
                try {
                    devicesDB = db?.getSpecificType(DeviceType.AIRTAG)

                } catch (ex: SQLiteException) {
                    Log.e("DataBaseLA", ex.message.toString())
                }
            }

            AppleDevice.deviceType.name -> {
                try {
                    devicesDB = db?.getSpecificType(DeviceType.APPLE)

                } catch (ex: SQLiteException) {
                    Log.e("DataBaseLA", ex.message.toString())
                }
            }

            FindMy.deviceType.name -> {
                try {
                    devicesDB = db?.getSpecificType(DeviceType.FIND_MY)

                } catch (ex: SQLiteException) {
                    Log.e("DataBaseLA", ex.message.toString())
                }
            }

            Tile.deviceType.name -> {
                try {
                    devicesDB = db?.getSpecificType(DeviceType.TILE)

                } catch (ex: SQLiteException) {
                    Log.e("DataBaseLA", ex.message.toString())
                }
            }

            Unknown.deviceType.name -> {
                try {
                    devicesDB = db?.getSpecificType(DeviceType.UNKNOWN)

                } catch (ex: SQLiteException) {
                    Log.e("DataBaseLA", ex.message.toString())
                }
            }

            else -> {
                Log.e("LA", "No Devices in Db")
            }
        }

        // Add first Discovery Time and Last Seen
        if (!result.device.name.isNullOrEmpty()) {

            if (checkMacAddress(result.device.address.toString())) {
                val iBeacon = Beacon(
                    result.device.name.toString(),
                    result.device.address.toString(),
                    result.rssi.toString(),
                    result.scanRecord?.serviceUuids.toString(),
                    type,
                    BaseDevice(result).firstDiscovery.second.toString(),
                    BaseDevice(result).lastSeen.second.toString()
                )
//                beacons.add(iBeacon)
                checkLastSeen(iBeacon)

                addBeacon(iBeacon)

            }
        } else {
            for (i in 0 until beacons.size + 1) {
                Name = "Device $i"
            }

            if (checkMacAddress(result.device.address.toString()) && Name != null && Name != "null") {

                val iBeacon = Beacon(
                    Name.toString(),
                    result.device.address.toString(),
                    result.rssi.toString(),
                    result.scanRecord?.serviceUuids.toString(), type,
                    BaseDevice(result).firstDiscovery.second.toString(),
                    BaseDevice(result).lastSeen.second.toString()
                )
//                beacons.add(iBeacon)
                //check if it needs to change catch exception like adding beacon
                checkLastSeen(iBeacon)
                addBeacon(iBeacon)

            }
        }

        try {

            val myCollection = devicesDB
            val iterator = myCollection?.iterator()
            while (iterator?.hasNext() == true) {
                val item = iterator?.next()

                addBeacon(item!!)

            }


        } catch (ex: ConcurrentModificationException) {
            Log.e("DataBaseLA", ex.message.toString())
        }

        adapter!!.notifyDataSetChanged()
    }


    @SuppressLint("NewApi")
    private fun checkLastSeen(beacon: Beacon) {
        try {

            try {


                val myCollection = devicesDB
                val iterator = myCollection?.iterator()
                while (iterator?.hasNext() == true) {
                    val item = iterator?.next()

//                if(item == 3){
//                    iterator.remove()
//                }

                    if (beacon.address.replace(
                            ":",
                            ""
                        ) == item?.serialNumber || beacon.address.replace(":", "") == item?.address
                    ) {
                        db?.updateCourse(
                            "devices_table",
                            beacon.name,
                            beacon.serialNumber,
                            beacon.rssi.toFloat(),
                            beacon.firstDiscovery.toString(),
                            beacon.lastSeen.toString()
                        )
                    }


                }
            } catch (ex: ConcurrentModificationException) {
                Log.e("LA", ex.message.toString())
            }

        } catch (ex: Exception) {
            Log.e("LA", ex.message.toString())
        }
    }

    //
    private fun scanFilters(FilterCheck: String): MutableList<ScanFilter> {
        //AirPods
        val APbluetoothFilter: ScanFilter = ScanFilter.Builder().setManufacturerData(
            0x4C,
            byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x18).toByte()),
            byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
        ).build()
        //Apple Dev
        val ADbluetoothFilter: ScanFilter = ScanFilter.Builder().setManufacturerData(
            0x4C,
            byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x00).toByte()),
            byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
        ).build()
        //AirTag
        val ATbluetoothFilter: ScanFilter =
            ScanFilter.Builder().setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
            ).build()
        //IPhone
        val PhonebluetoothFilter: ScanFilter = ScanFilter.Builder().setManufacturerData(
            0x4C,
            byteArrayOf((0x12).toByte(), (0x02).toByte(), (0x18).toByte()),
            byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
        ).build()
        //unk
        val unkFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte())
            ).build()

        val FMDFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0xFF).toByte())
            ).build()

        val TileFilter = ScanFilter.Builder().setServiceUuid(Tile.offlineFindingServiceUUID).build()


        val AllDevFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                null,
                null
            ).build()


        when (FilterCheck) {
            "AirPods" -> {
                devfilters.add(0, APbluetoothFilter)
            }

            "Apple" -> {
                devfilters.add(0, ADbluetoothFilter)
//                devfilters.add(1,AllDevFilter)
                //remove when finish
            }

            "IPhone" -> {
                devfilters.add(0, PhonebluetoothFilter)
            }

            "AirTag" -> {
                devfilters.add(0, ATbluetoothFilter)
            }

            "FMD" -> {
                devfilters.add(0, FMDFilter)
            }

            "UNK" -> {
                devfilters.add(0, unkFilter)
            }

            else -> {
                devfilters.add(0, TileFilter)
            }
        }
        return devfilters
    }


    fun displayBeaconsList() {
//        beacons.sortBy { beacon -> beacon.rssi }
        recyclerView = findViewById<View>(R.id.recycler) as RecyclerView
        layoutManager = LinearLayoutManager(this)
        adapter = BeaconAdapter(this, beacons)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = adapter
        tx?.visibility = View.GONE
        adapter!!.notifyDataSetChanged()
    }

    //check that this is closebeacons device by UUIDS &
    // device with same address is not contains in list.
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


    @SuppressLint("MissingPermission", "NewApi")
    private fun checkType(result: ScanResult) {
        val baseDev = BaseDevice(result)
//        val Type = baseDev.deviceType
        val Type = DeviceManager.getDeviceType(result)

        if (Type == DeviceType.AIRPODS && intent.getStringExtra("Category") == "AirPods") {

            resToBeacon(result,Type)
        } else if (Type == DeviceType.IPhone && intent.getStringExtra("Category") == "IPhone") {
            resToBeacon(result,Type)


        } else if (Type == DeviceType.AIRTAG && intent.getStringExtra("Category") == "AirTag") {
            resToBeacon(result,Type)


        } else if (Type == DeviceType.FIND_MY && intent.getStringExtra("Category") == "FMD") {
            resToBeacon(result,Type)


        } else if (Type == DeviceType.TILE && intent.getStringExtra("Category") == "Tile") {

            resToBeacon(result,Type)

        } else if (Type == DeviceType.APPLE && intent.getStringExtra("Category") == "Apple") {

            resToBeacon(result,Type)

        } else if (Type == DeviceType.UNKNOWN && intent.getStringExtra("Category") == "UNK") {
            resToBeacon(result,Type)


        } else {
            Log.e("LA","No Match")
        }
    }


    private fun scanFilters(): MutableList<ScanFilter> {
        //AirPods
        val APbluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf(
                    (0x12).toByte(),
                    (0x19).toByte(),
                    (0x18).toByte()
                ), // Empty status byte?
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte()) // ff?
            )
            .build()
        //Apple Find Dev
        val ADbluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf(
                    (0x12).toByte(),
                    (0x19).toByte(),
                    (0x00).toByte()
                ), // Empty status byte?
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte()) // ff?
            )
            .build()
        //AirTag
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
                byteArrayOf((0x12).toByte(), (0x02).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
            )
            .build()
        //unk
        val unkFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte())
            ).build()
        //FMD
        val FMDFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0xFF).toByte())
            ).build()

        val TileFilter = ScanFilter.Builder().setServiceUuid(Tile.offlineFindingServiceUUID).build()

        //All Apple
        val AllDevFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                null, null
            ).build()

        devfilters.add(0, ATbluetoothFilter)
        devfilters.add(1, ADbluetoothFilter)
        devfilters.add(2, APbluetoothFilter)
        devfilters.add(3, PhonebluetoothFilter)
        devfilters.add(4, FMDFilter)
        devfilters.add(5, TileFilter)
        devfilters.add(6, unkFilter)
        devfilters.add(7, AllDevFilter)

        return devfilters
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

        m_bluetoothAdapter?.bluetoothLeScanner?.startScan(
            scanFilters(intent.getStringExtra("Category")!!), ScanSettings.Builder().setScanMode(
                ScanSettings.SCAN_MODE_LOW_LATENCY
            ).build(), lleScanCallback
        )

        super.onResume()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
        m_bluetoothAdapter = null
        beacons.clear()
        devfilters.clear()
        db?.close()
//        unregisterReceiver(mReceiver)
    }

}


        // Get All devices then Apply Filters and show data by deviceTypes and Manager


