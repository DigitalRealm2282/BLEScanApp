package com.ats.apple

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ats.apple.data.DeviceManager
import com.ats.apple.data.DeviceType
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries


class DeviceDetailsActivity : AppCompatActivity() {

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private val SCAN_PERIOD: Long = 15000
    private var scanning = false
    private val handler = Handler()
    private val scan_list : ArrayList<ScanResult> = ArrayList()
    private var address = ""
    private var name = ""
    private var chart:ValueLineChart?=null
    private var mGatt: BluetoothGatt? = null
    private val devfilters: MutableList<ScanFilter> = ArrayList()
    private var rssi = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_details)


        val bundle = intent.extras
        address = bundle!!.getString("address")!!
        name = bundle.getString("name")!!
        rssi = bundle.getString("rssi")!!
        val uuids = bundle.getString("uuids")
        val serialNumber = bundle.getString("serialNumber")

        chart = findViewById(R.id.chart1)

        val rssi1 = findViewById<TextView>(R.id.rssiTxt)

        rssi1.text = rssi
        chart!!.startAnimation()
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        setupUI(address,name)

        scanLeDevice()
    }

    private fun setupUI(address: String?, name: String?) {
        val Address = findViewById<TextView>(R.id.MacAddressTxt)
        Address.text = address
        val play = findViewById<MaterialButton>(R.id.play)
        play.setOnClickListener {
            scanLeDevice()
        }

        if (name!!.isNotEmpty() or !name.contains("Device")) {
            this.title = name
        }else{
            //Check Dev Type and return name of category
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
                handler.postDelayed({
                    scanning = false
                    m_bluetoothAdapter!!.bluetoothLeScanner.stopScan(lleScanCallback)
                    if (scan_list.isNotEmpty()) {
//                        Toast.makeText(
//                            this@ListActivity,
//                            "Found: " + scan_list[0].device.name + scan_list[0].device.address,
//                            Toast.LENGTH_SHORT
//                        ).show()
                        Toast.makeText(this@DeviceDetailsActivity,"Found: " + scan_list.size.toString(), Toast.LENGTH_SHORT).show()
                        Log.i("ScanListLA","Found: " + scan_list.size.toString())

                    }else{
                        Toast.makeText(this@DeviceDetailsActivity,"Empty", Toast.LENGTH_SHORT).show()

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

                Toast.makeText(this@DeviceDetailsActivity,"Fired", Toast.LENGTH_SHORT).show()

                val res = result
                val btDevice = res!!.device
                val uuidsFromScan = result.scanRecord?.serviceUuids.toString()
                // start scan get results callback & if bundle address = address of one of results get its rssi
                if (btDevice.address == address){
                    Toast.makeText(this@DeviceDetailsActivity,"Device Found", Toast.LENGTH_SHORT).show()
                    checkType(result)

                    val rssi = findViewById<TextView>(R.id.rssiTxt)

                    val series = ValueLineSeries()
                    series.color = -0xa9480f

                    series.addPoint(ValueLinePoint("Jan", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Feb", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Mar", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Apr", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Mai", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Jun", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Jul", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Aug", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Sep", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Oct", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Nov", res.rssi.toFloat()))
                    series.addPoint(ValueLinePoint("Dec", res.rssi.toFloat()))

                    chart?.addSeries(series)
//                    chart.data = res.rssi
                    rssi.text = res.rssi.toString()
                }

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



    @SuppressLint("MissingPermission")
    private fun checkType(result: ScanResult){
//        Toast.makeText(this@ListActivity,"CallBack Fired",Toast.LENGTH_SHORT).show()

        if (!scan_list.contains(result)) {
            scan_list.add(result)

            if (DeviceManager.getDeviceType(result) == DeviceType.AIRPODS && this@DeviceDetailsActivity.title == name) {
//                Toast.makeText(this@ListActivity,"AirPodz",Toast.LENGTH_SHORT).show()
                connectToDevice(result.device)

            } else if (DeviceManager.getDeviceType(result) == DeviceType.IPhone && this@DeviceDetailsActivity.title == name) {
//                Toast.makeText(this@ListActivity,"Iphone",Toast.LENGTH_SHORT).show()

            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.AIRTAG && this.title == name) {
//                Toast.makeText(this@ListActivity,"AirTag",Toast.LENGTH_SHORT).show()
                connectToDevice(result.device)



            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.FIND_MY && this.title == name) {
//                Toast.makeText(this@ListActivity,"Find",Toast.LENGTH_SHORT).show()


            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.TILE && this.title == name) {
//                Toast.makeText(this@ListActivity,"Tile",Toast.LENGTH_SHORT).show()

                connectToDevice(result.device)



            }else if(DeviceManager.getDeviceType(result) == DeviceType.APPLE && this.title == name){
//                Toast.makeText(this@ListActivity,"Apple",Toast.LENGTH_SHORT).show()

            }else if(DeviceManager.getDeviceType(result) == DeviceType.UNKNOWN && this.title == name) {
//                Toast.makeText(this@ListActivity,"Unknown",Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(this@DeviceDetailsActivity,"Unknown Checked Data or not matching ", Toast.LENGTH_SHORT).show()

            }
        }else{
            val ind = scan_list.indexOf(result)
            scan_list[ind]


        }

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


}