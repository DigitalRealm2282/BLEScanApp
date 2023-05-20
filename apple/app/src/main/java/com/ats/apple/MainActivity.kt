package com.ats.apple

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY
import android.content.*
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
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
import com.ats.apple.data.DBHelper
import com.ats.apple.data.model.BaseDevice
import com.ats.apple.data.DeviceManager
import com.ats.apple.data.DeviceType
import com.ats.apple.util.Util
import com.ats.apple.util.types.Beacon
import com.ats.apple.util.types.Tile
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private val SCAN_PERIOD: Long = 5000
    private var scanning = false
    private val handler = Handler()
    private val REQUEST_ENABLE_BLUETOOTH = 1
//    private val scan_list : ArrayList<ScanResult> = ArrayList()
//    private val device_list : ArrayList<BluetoothDevice> = ArrayList()
//    val activeFilter: MutableMap<String, Filter> = mutableMapOf()
//    val devices = MediatorLiveData<List<BaseDevice>>()
//    var filterSummaryText: MutableLiveData<String> = MutableLiveData("")
//    private lateinit var deviceRepository: DeviceRepository
    var IphoneInt :Int?=null
    var AIRPODSInt:Int?=null
    var AirTagInt:Int?=null
    var FMDInt:Int?=null
    var ADInt:Int?=null
    var unkInt:Int?=null
    var TilesInt:Int?=null
    private var db :DBHelper?=null
    private var Name :String?=null
    private var beacons: ArrayList<Beacon>?= null


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
//            byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()), // Empty status byte?
//            byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte()) // ff?
//        )
//        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAllPerms(this,this.applicationContext)
        sdkhigh(this.applicationContext,this)
        beacons = ArrayList()

        checkBTPerms()
        setupUI()
        db = DBHelper(this, null)

    }

    fun addBeacon(iBeacon: Beacon) {
        if (beacons!!.isNotEmpty()) {
            val it: MutableIterator<Beacon> = beacons!!.iterator() as MutableIterator<Beacon>
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
        beacons!!.add(iBeacon)
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
        }


        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val txt = findViewById<TextView>(R.id.text4)


            GlobalScope.launch(Dispatchers.Main) {
                if (result != null)
                    checkType(result)
            }

        }

    }


    @SuppressLint("MissingPermission", "NewApi")
    private fun checkType(result: ScanResult){
        val baseDev = BaseDevice(result)
//        val Type = baseDev.deviceType
        val Type = DeviceManager.getDeviceType(result)
        AddDevToDatabase(result)

        when (Type) {
                DeviceType.AIRPODS -> {
                    val textAp = findViewById<TextView>(R.id.APTextno)
                    if (AIRPODSInt != null) AIRPODSInt!!.inc() else AIRPODSInt = 1
                    textAp.text = AIRPODSInt.toString()
                }

                DeviceType.IPhone -> {
                    val textAD = findViewById<TextView>(R.id.iphoneTextno)
                    if (IphoneInt != null) IphoneInt!!.inc() else IphoneInt = 1
                    textAD.text = IphoneInt.toString()

                }

                DeviceType.AIRTAG -> {
                    val textAT = findViewById<TextView>(R.id.atags)
                    if (AirTagInt != null) AirTagInt!!.inc() else AirTagInt = 1
                    textAT.text = AirTagInt.toString()

                }

                DeviceType.FIND_MY -> {
                    val textFMD = findViewById<TextView>(R.id.findMyDev)
                    if (FMDInt != null) FMDInt!!.inc() else FMDInt = 1
                    textFMD.text = FMDInt.toString()

                }

                DeviceType.TILE -> {
                    val textTile = findViewById<TextView>(R.id.tileFoundText)
                    if (TilesInt != null) TilesInt!!.inc() else TilesInt = 1
                    textTile.text = TilesInt.toString()

                }

                DeviceType.APPLE -> {
                    val textADT = findViewById<TextView>(R.id.AppleDevText)
                    if (ADInt != null) ADInt!!.inc() else ADInt = 1
                    textADT.text = ADInt.toString()

                }

                DeviceType.UNKNOWN -> {
                    val textund = findViewById<TextView>(R.id.unkDevText)
                    if (unkInt != null) unkInt!!.inc() else unkInt = 1
                    textund.text = unkInt.toString()

                }

                else -> {

                }
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun resultToBeacon(result: ScanResult): Beacon? {
        // Add first Desc Time and Last Seen
        var beacon :Beacon ?=null
        if (!result.device.name.isNullOrEmpty()) {

            val iBeacon = Beacon(
                result.device.name.toString(),
                result.device.address.toString(),
                result.rssi.toString(),
                result.scanRecord?.serviceUuids.toString(),
                BaseDevice(result).deviceType!!,
                BaseDevice(result).firstDiscovery.second.toString(),
                BaseDevice(result).lastSeen.second.toString()
            )
            addBeacon(iBeacon)
            beacon = iBeacon

        }else{
            for (i in 0 until beacons!!.size+1) {
                Name = "Device $i"
            }

            if ( Name != null && Name != "null") {
                val iBeacon = Beacon(
                    Name.toString(),
                    result.device.address.toString(),
                    result.rssi.toString(),
                    result.scanRecord?.serviceUuids.toString(), BaseDevice(result).deviceType!!,
                    BaseDevice(result).firstDiscovery.second.toString(),
                    BaseDevice(result).lastSeen.second.toString()
                )
                addBeacon(iBeacon)
                beacon= iBeacon

            }
        }
        return beacon
    }

    @SuppressLint("NewApi", "MissingPermission")
    private fun AddDevToDatabase(result: ScanResult){
        val ind = beacons!!.indexOf(resultToBeacon(result))
        val mac = beacons!![ind].serialNumber.replace(":","")
        //Edit this to false and add ! this in final version
        if (!db?.CheckIsDataAlreadyInDBorNot("devices_table","SerialNumber",mac)!!) {

            if (!result.device.name.isNullOrEmpty()) {
                val dev = BaseDevice(result)
                db!!.addDevice(
                    result.device?.name.toString(),
                    result.device.address.replace(":",""),
                    result.rssi.toFloat(),
                    dev.firstDiscovery.second.toString(),
                    dev.lastSeen.second.toString(),
                    dev.uniqueId!!,
                    dev.deviceType!!
                )

            } else {
                for (i in 0 until beacons!!.size+1) {
                    Name = "Device $i"

                }
                val dev = BaseDevice(result)
                db!!.addDevice(
                    Name!!,
                    result.device.address.replace(":",""),
                    result.rssi.toFloat(),
                    dev.firstDiscovery.second.toString(),
                    dev.lastSeen.second.toString(),
                    dev.uniqueId!!,
                    dev.deviceType!!
                )

            }
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
//                    if (scan_list.isNotEmpty()) {
                    Log.i("ScanListMA","Stopped")


//                    }
                }, SCAN_PERIOD)
                scanning = true
                val sett = SCAN_MODE_LOW_LATENCY
                val settBuilder = ScanSettings.Builder().setScanMode(sett).build()
                m_bluetoothAdapter!!.bluetoothLeScanner.startScan(
                    scanFilters(),
                    settBuilder,
                    lleScanCallback)
            } else {
                scanning = false
                m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)

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


    private fun scanFilters(): MutableList<ScanFilter> {
        //AirPods
        val APbluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x18).toByte()), // Empty status byte?
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0x18).toByte()) // ff?
            )
            .build()
        //Apple Find Dev
        val ADbluetoothFilter: ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x00).toByte()), // Empty status byte?
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
        val unkFilter :ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte())
            ).build()
        //FMD
        val FMDFilter :ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                byteArrayOf((0x12).toByte(), (0x19).toByte(), (0x10).toByte()),
                byteArrayOf((0xFF).toByte(), (0xFF).toByte(), (0xFF).toByte())).build()

        val TileFilter = ScanFilter.Builder().setServiceUuid(Tile.offlineFindingServiceUUID).build()

        //All Apple
        val AllDevFilter :ScanFilter = ScanFilter.Builder()
            .setManufacturerData(
                0x4C,
                null, null).build()

        devfilters.add(0,ATbluetoothFilter)
        devfilters.add(1,ADbluetoothFilter)
        devfilters.add(2,APbluetoothFilter)
        devfilters.add(3,PhonebluetoothFilter)
        devfilters.add(4,FMDFilter)
        devfilters.add(5,TileFilter)
        devfilters.add(6,unkFilter)
        devfilters.add(7,AllDevFilter)

        return devfilters

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
        IphoneInt =null
        AIRPODSInt =null
        AirTagInt =null
        FMDInt =null
        ADInt =null
        unkInt =null
        TilesInt =null

        devfilters.clear()

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        m_bluetoothAdapter?.bluetoothLeScanner?.startScan(scanFilters(),ScanSettings.Builder().setScanMode(
            SCAN_MODE_LOW_LATENCY).build(),lleScanCallback)

        super.onResume()
    }
    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
//        scan_list.clear()

        IphoneInt =null
        AIRPODSInt =null
        AirTagInt =null
        FMDInt =null
        ADInt =null
        unkInt =null
        TilesInt =null
        m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
        m_bluetoothAdapter = null
        db?.close()

        devfilters.clear()
//        unregisterReceiver(mReceiver)
    }
}