package com.ats.apple

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ats.apple.data.DeviceManager
import com.ats.apple.data.DeviceType
import com.ats.apple.util.Util
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private val SCAN_PERIOD: Long = 10000
    private var scanning = false
    private val handler = Handler()
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val scan_list : ArrayList<ScanResult> = ArrayList()
    private val device_list : ArrayList<BluetoothDevice> = ArrayList()

    private val devfilters: MutableList<ScanFilter> = ArrayList()
//    val IphoneMatcher = ScanFilter.Builder().setManufacturerData(
//        0x4C,
//        byteArrayOf((0x12).toByte(), (0x02).toByte(), (0x18).toByte()),
//        byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
//    )
//        .build()
//
//    val AirPodsMatcher = ScanFilter.Builder()
//        .setManufacturerData(
//            0x4C,
//            byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x18).toByte()), // Empty status byte?
//            byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte()) // ff?
//        )
//        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAllPerms(this,this.applicationContext)
        sdkhigh(this.applicationContext,this)
        checkBTPerms()
        scanFilters()
        setupUI()

    }


    @SuppressLint("MissingPermission")
    private fun setupUI(){
        val textBtn = findViewById<MaterialButton>(R.id.mscan)
        val cardAirPods = findViewById<MaterialCardView>(R.id.AirPodsCard)
        val cardIphone = findViewById<MaterialCardView>(R.id.IPhoneCard)
        val cardUnknown = findViewById<MaterialCardView>(R.id.unkCard)
        val cardTiles = findViewById<MaterialCardView>(R.id.TilesCard)
        val cardAirTag = findViewById<MaterialCardView>(R.id.airtagsCard)
        val findMyDev = findViewById<MaterialCardView>(R.id.FMDCard)
        val appleDevCard = findViewById<MaterialCardView>(R.id.AppleDeviceCard)


        textBtn.setOnClickListener {
            if (Util.checkBluetoothPermission(this.applicationContext)) {
//                registerReceiver(mReceiver,IntentFilter())
                scan_list.clear()
                scanLeDevice()
            }else{
                checkAllPerms(this, this.applicationContext)
                sdkhigh(this.applicationContext,this)
                checkBTPerms()
            }
        }

        val intent = Intent(this, ListActivity::class.java)

        cardAirPods.setOnClickListener {
            intent.putExtra("Category","AirPods")
            startActivity(intent)
        }
        cardIphone.setOnClickListener {
            intent.putExtra("Category","IPhone")
            startActivity(intent)
        }
        cardUnknown.setOnClickListener {
            intent.putExtra("Category","UNK")
            startActivity(intent)
        }
        cardTiles.setOnClickListener {
            intent.putExtra("Category","Tile")
            startActivity(intent)
        }
        cardAirTag.setOnClickListener {
            intent.putExtra("Category","AirTag")
            startActivity(intent)
        }
        findMyDev.setOnClickListener {
            intent.putExtra("Category","FMD")
            startActivity(intent)
        }
        appleDevCard.setOnClickListener {
            intent.putExtra("Category","Apple")
            startActivity(intent)
        }


    }

    private fun checkBTPerms(){
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
        // Check to see if the Bluetooth classic feature is available.
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH) }?.also {
            Toast.makeText(this, "bluetooth not supported", Toast.LENGTH_SHORT).show()
            finish()
        }
// Check to see if the BLE feature is available.
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "BlueTooth Low Energy not Supported", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //agree
        }else{
            //deny
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    val lleScanCallback = object: ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("Error", "Scan Failed: $errorCode")
//            val view = findViewById<ConstraintLayout>(R.id.constraintll)
//            view.let {
//                Snackbar.make(
//                    it,
//                    R.string.ble_service_connection_error,
//                    Snackbar.LENGTH_LONG
//                )
//            }
        }


        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val txt = findViewById<TextView>(R.id.text4)

//            if (AirPodsMatcher.matches(result)){
//                Toast.makeText(this@MainActivity,"Done",Toast.LENGTH_SHORT).show()
//            }

//            txt.text = result!!.device.type!!.toUInt().toString()

            GlobalScope.launch(Dispatchers.Main) {
                if (result != null)
                    checkType(result)
            }

        }

    }


    @SuppressLint("MissingPermission", "NewApi")
    private fun checkType(result: ScanResult){
        val tx3 = findViewById<TextView>(R.id.text3)

        if (!scan_list.contains(result)) {
            scan_list.add(result)

            if ( DeviceManager.getDeviceType(result) == DeviceType.AIRPODS) {
                val textAp = findViewById<TextView>(R.id.APTextno)
                var no = 1
                textAp.text = no++.toString()

//                val scanRecord: ScanRecord = result.scanRecord!!
//                val manufacturerData = scanRecord.getManufacturerSpecificData(0x004c)


            } else if (DeviceManager.getDeviceType(result) == DeviceType.IPhone) {

                val textAD = findViewById<TextView>(R.id.iphoneTextno)
                var no = 1
                textAD.text = no++.toString()


            } else if (DeviceManager.getDeviceType(result) == DeviceType.AIRTAG) {
                val textAT = findViewById<TextView>(R.id.atags)
                var no = 1
                textAT.text = no++.toString()


            } else if (DeviceManager.getDeviceType(result) == DeviceType.FIND_MY) {
                val textFMD = findViewById<TextView>(R.id.findMyDev)
                var no = 1
                textFMD.text = no++.toString()


            } else if (DeviceManager.getDeviceType(result) == DeviceType.TILE) {
                val textTile = findViewById<TextView>(R.id.tileFoundText)
                var no = 1
                textTile.text = no++.toString()
            }else if(DeviceManager.getDeviceType(result) == DeviceType.APPLE ){
                val textADT = findViewById<TextView>(R.id.AppleDevText)
                var no = 1
                textADT.text = no++.toString()


            }else if(DeviceManager.getDeviceType(result) == DeviceType.UNKNOWN) {
                val textund = findViewById<TextView>(R.id.unkDevText)
                var no = 1
                textund.text = no++.toString()

            } else {
                tx3.text = "None detected"


            }
        }else{
            val ind = scan_list.indexOf(result)
            scan_list[ind]
            tx3.text = "Already detected"


        }

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
                        Log.i("ScanListMA","Found: " + scan_list.size.toString())


                    }
                }, SCAN_PERIOD)
                scanning = true
                val sett = SCAN_MODE_LOW_LATENCY
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



    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }


    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)



//    private val mReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val action = intent.action
//            if (BluetoothDevice.ACTION_FOUND == action) {
//
//                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//                device_list.add(device!!)
//
//            }
//        }
//    }


    private fun scanFilters(){
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

        val AllbluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(0x4C,

            null, // Empty status byte?
            byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte())
            )
            .build()

//        val All2bluetoothFilter: ScanFilter = ScanFilter.Builder().build()
//
        devfilters.add(0,ATbluetoothFilter)
        devfilters.add(1,ADbluetoothFilter)
        devfilters.add(2,APbluetoothFilter)
        devfilters.add(3,PhonebluetoothFilter)
        devfilters.add(4,AllbluetoothFilter)
//        devfilters.add(5,All2bluetoothFilter)
//        devfilters.add(0,PhonebluetoothFilter)


        // Get All devices then Apply Filters and show data by deviceTypes and Manager

    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.app_name)
                    .setMessage("BLE needs to open the positioning function")
                    .setNegativeButton(
                        R.string.cancel
                    ) { _, _ ->
                        this.finish()
                    }
                    .setPositiveButton(
                        R.string.ok_button
                    ) { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, 0)
                    }
                    .setCancelable(false)
                    .show()
                println("Bluettoth Enabled")
            } else if (resultCode == RESULT_CANCELED) {
                println("Bluetooth Not permitted")
                finish()
            }


        }
    }



    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()
        m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        m_bluetoothAdapter?.bluetoothLeScanner?.startScan(devfilters,ScanSettings.Builder().setScanMode(
            SCAN_MODE_LOW_LATENCY).build(),lleScanCallback)

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