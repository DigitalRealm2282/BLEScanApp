package com.ats.apple

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
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
    private val SCAN_PERIOD: Long = 60000
    private var scanning = false
    private val handler = Handler()
    private val scan_list : ArrayList<ScanResult> = ArrayList()
    private var address = ""
    private var name = ""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_details)


        val bundle = intent.extras
        address = bundle!!.getString("address")!!
        name = bundle.getString("name")!!
        val rssi = bundle.getString("rssi")
        val uuids = bundle.getString("uuids")
        val serialNumber = bundle.getString("serialNumber")

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        setupUI(address,name)

        scanLeDevice()
        // start scan get results & if bundle address = address of one of results get its rssi
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
//                        Toast.makeText(this@ListActivity,"Found: " + scan_list.size.toString(), Toast.LENGTH_SHORT).show()
                        Log.i("ScanListLA","Found: " + scan_list.size.toString())

                    }
                }, SCAN_PERIOD)
                scanning = true
                val sett = ScanSettings.MATCH_MODE_STICKY
                val settBuilder = ScanSettings.Builder().setScanMode(sett).build()
                m_bluetoothAdapter!!.bluetoothLeScanner.startScan(
                    DeviceManager.scanFilter,
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

                Toast.makeText(this@DeviceDetailsActivity,"Fired", Toast.LENGTH_SHORT).show()


                val res = result
                val btDevice = res.device
                val uuidsFromScan = result.scanRecord?.serviceUuids.toString()
                if (btDevice.address == address){
                    Toast.makeText(this@DeviceDetailsActivity,"Device Found", Toast.LENGTH_SHORT).show()

                    val rssi = findViewById<TextView>(R.id.rssiTxt)
                    val chart = findViewById<ValueLineChart>(R.id.chart1)

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

                    chart.addSeries(series)
                    chart.startAnimation()
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



    @SuppressLint("MissingPermission")
    private fun checkType(result: ScanResult){
//        Toast.makeText(this@ListActivity,"CallBack Fired",Toast.LENGTH_SHORT).show()

        if (!scan_list.contains(result)) {
            scan_list.add(result)

            if (DeviceManager.getDeviceType(result) == DeviceType.AIRPODS && this@DeviceDetailsActivity.title == name) {
//                Toast.makeText(this@ListActivity,"AirPodz",Toast.LENGTH_SHORT).show()

            } else if (DeviceManager.getDeviceType(result) == DeviceType.IPhone && this@DeviceDetailsActivity.title == name) {
//                Toast.makeText(this@ListActivity,"Iphone",Toast.LENGTH_SHORT).show()

            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.AIRTAG && this.title == name) {
//                Toast.makeText(this@ListActivity,"AirTag",Toast.LENGTH_SHORT).show()



            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.FIND_MY && this.title == name) {
//                Toast.makeText(this@ListActivity,"Find",Toast.LENGTH_SHORT).show()


            } else if (DeviceManager.getDeviceType(result!!)== DeviceType.TILE && this.title == name) {
//                Toast.makeText(this@ListActivity,"Tile",Toast.LENGTH_SHORT).show()


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




}