package com.ats.apple

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ats.apple.adapter.BeaconAdapter
import com.ats.apple.data.DeviceManager
import com.ats.apple.data.DeviceType
import com.ats.apple.util.types.Beacon
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ListActivity : AppCompatActivity() {
//    private val leDeviceListAdapter = LeDeviceListAdapter()

    private val scan_list : ArrayList<ScanResult> = ArrayList()
    private val device_list : ArrayList<BluetoothDevice> = ArrayList()

    private val devfilters: MutableList<ScanFilter> = ArrayList()

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private val SCAN_PERIOD: Long = 15000
    private var scanning = false
    private val handler = Handler()

    private val mLEScanner: BluetoothLeScanner? = null
    private val settings: ScanSettings? = null
    private var mGatt: BluetoothGatt? = null
    private var tx:TextView?=null
    private var Name:String?=null


    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    private lateinit var beacons: ArrayList<Beacon>
    private val UUIDS = "[19721006-2004-2007-2014-acc0cbeac000]" //closebeacons UUIDS


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        setupUI()
        beacons = ArrayList<Beacon>()
        tx = findViewById<TextView>(R.id.textND)

        displayBeaconsList()
        scanLeDevice()

    }

    private fun setupUI(){

        if (intent.getStringExtra("Category")== "AirPods"){
            this.title = "AirPods"
        }
        if (intent.getStringExtra("Category")== "IPhone"){
            this.title = "IPhone"

        }
        if (intent.getStringExtra("Category")== "UNK"){
            this.title = "Unknown Devices"

        }
        if (intent.getStringExtra("Category")== "Tile"){
            this.title = "Tiles"

        }
        if (intent.getStringExtra("Category")== "AirTag"){
            this.title = "AirTags"

        }
        if (intent.getStringExtra("Category")== "FMD"){
            this.title = "Find My Devices"

        }
        if (intent.getStringExtra("Category")== "Apple"){
            this.title = "Apple Devices"

        }

        val rl = findViewById<SwipeRefreshLayout>(R.id.refreshlayout)
        rl.setOnRefreshListener {
            if (beacons.isNotEmpty()){
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
                Toast.makeText(this@ListActivity,"No Data Found",Toast.LENGTH_SHORT).show()
                rl.isRefreshing = false
                recreate()
                scanLeDevice()
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
                    m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
                    if (scan_list.isNotEmpty()) {
//                        Toast.makeText(
//                            this@ListActivity,
//                            "Found: " + scan_list[0].device.name + scan_list[0].device.address,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        Toast.makeText(this@ListActivity,"Found: " + scan_list.size.toString(), Toast.LENGTH_SHORT).show()
                        Log.i("ScanListLA","Found: " + scan_list.size.toString())

                    }
                }, SCAN_PERIOD)
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
                checkType(result!!)

                val btDevice = result.device
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

    @SuppressLint("MissingPermission")
    private fun resToBeacon(result: ScanResult){
        if (!result.device.name.isNullOrEmpty()) {
            if (checkMacAddress(result.device.address.toString())) {
                val iBeacon = Beacon(
                    result.device.name.toString(),
                    result.device.address.toString(),
                    result.rssi.toString(),
                    result.scanRecord?.serviceUuids.toString()
                )
                beacons.add(iBeacon)
                addBeacon(iBeacon)

            }
        }else{
            for (i in 0 until beacons.size) {
                Name = "Device $i"
            }

            if (checkMacAddress(result.device.address.toString())&& Name != null && Name != "null") {
                val iBeacon = Beacon(
                    Name.toString(),
                    result.device.address.toString(),
                    result.rssi.toString(),
                    result.scanRecord?.serviceUuids.toString()
                )
                beacons.add(iBeacon)
                addBeacon(iBeacon)

            }
        }

        adapter!!.notifyDataSetChanged()
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

        val AllFilter :ScanFilter = ScanFilter.Builder().build()

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


    @SuppressLint("MissingPermission")
    private fun checkType(result: ScanResult){
//        Toast.makeText(this@ListActivity,"CallBack Fired",Toast.LENGTH_SHORT).show()

        if (!scan_list.contains(result)) {
            scan_list.add(result)

            if (DeviceManager.getDeviceType(result) == DeviceType.AIRPODS && this@ListActivity.title == "AirPods") {
//                Toast.makeText(this@ListActivity,"AirPodz",Toast.LENGTH_SHORT).show()
                resToBeacon(result)

            } else if (DeviceManager.getDeviceType(result) == DeviceType.IPhone && this@ListActivity.title == "IPhone") {
//                Toast.makeText(this@ListActivity,"Iphone",Toast.LENGTH_SHORT).show()
                resToBeacon(result)

            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.AIRTAG && this.title == "AirTags") {
//                Toast.makeText(this@ListActivity,"AirTag",Toast.LENGTH_SHORT).show()
                resToBeacon(result)


            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.FIND_MY && this.title == "Find My Devices") {
                resToBeacon(result)
//                Toast.makeText(this@ListActivity,"Find",Toast.LENGTH_SHORT).show()


            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.TILE && this.title == "Tiles") {
//                Toast.makeText(this@ListActivity,"Tile",Toast.LENGTH_SHORT).show()
                resToBeacon(result)


            }else if(DeviceManager.getDeviceType(result) == DeviceType.APPLE && this.title == "Apple Devices"){
//                Toast.makeText(this@ListActivity,"Apple",Toast.LENGTH_SHORT).show()
                resToBeacon(result)

            }else if(DeviceManager.getDeviceType(result) == DeviceType.UNKNOWN && this.title == "Unknown Devices") {
//                Toast.makeText(this@ListActivity,"Unknown",Toast.LENGTH_SHORT).show()
                resToBeacon(result)
            } else {
                Toast.makeText(this@ListActivity,"Unknown Checked Data or not matching ",Toast.LENGTH_SHORT).show()

            }
        }else{
            val ind = scan_list.indexOf(result)
            scan_list[ind]


        }

    }


    fun displayBeaconsList() {
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


    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback)
//            scanLeDevice(false) // will stop after first device detection
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
                }
                BluetoothProfile.STATE_DISCONNECTED -> Log.e("gattCallback", "STATE_DISCONNECTED")
                else -> Log.e("gattCallback", "STATE_OTHER")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val services = gatt.services
            Log.i("onServicesDiscovered", services.toString())
            gatt.readCharacteristic(services[1].characteristics[0])
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


    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()
        m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        m_bluetoothAdapter?.bluetoothLeScanner?.startScan(devfilters,ScanSettings.Builder().setScanMode(
            ScanSettings.SCAN_MODE_LOW_LATENCY
        ).build(),lleScanCallback)

        super.onResume()
    }
    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        scan_list.clear()
        m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
        m_bluetoothAdapter = null

//        unregisterReceiver(mReceiver)
    }
}