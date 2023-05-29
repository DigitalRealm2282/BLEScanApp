package com.ats.airflagger

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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.ats.airflagger.data.DBHelper
import com.ats.airflagger.data.DeviceManager
import com.ats.airflagger.data.DeviceType
import com.ats.airflagger.data.model.BaseDevice
import com.ats.airflagger.util.Util
import com.ats.airflagger.util.types.Beacon
import com.ats.airflagger.util.types.Tile
import com.ats.airflagger.viewModel.MainActivityViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import io.cryptolens.methods.Helpers
import io.cryptolens.methods.Key
import io.cryptolens.models.ActivateModel
import io.cryptolens.models.LicenseKey
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(),LifecycleOwner {

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

    val iPhoneList:ArrayList<Beacon> = ArrayList()
    val airPodsList:ArrayList<Beacon> = ArrayList()
    val airTagList:ArrayList<Beacon> = ArrayList()
    val tileList:ArrayList<Beacon> = ArrayList()
    val appleList:ArrayList<Beacon> = ArrayList()
    val fmdList:ArrayList<Beacon> = ArrayList()
    val unkList:ArrayList<Beacon> = ArrayList()




    private var db :DBHelper?=null
    private var Name :String?=null
    private var beacons: ArrayList<Beacon>?= null


    private val devfilters: MutableList<ScanFilter> = ArrayList()
    lateinit var viewModel:MainActivityViewModel

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

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        sdkhigh(this.applicationContext,this)
        checkAllPerms(this,this.applicationContext)
        checkBTPerms()

        beacons = ArrayList()
        setupUI()
        db = DBHelper(this, null)
//        cryptoLensKeys()

    }
    private fun cryptoLensKeys(){
        val RSAPubKey = "Enter the RSA Public key here"
        val auth =
            "Access token with permission to access the activate method"

        val license: LicenseKey = Key.Activate(
            auth, RSAPubKey,
            ActivateModel(
                3349,  // <-- change this to your Product Id
                "ICVLD-VVSZR-ZTICT-YKGXL",  // <--  change this to your license key
                Helpers.GetMachineCode(2)
            )
        )

        if (license == null || !Helpers.IsOnRightMachine(license, 2)) {
            println("The license does not work.")
        } else {
            println("The license is valid!")
            println("It will expire: " + license.Expires)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addBeacon(iBeacon: Beacon) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (beacons!!.isNotEmpty()) {
                    val it: MutableIterator<Beacon> =
                        beacons!!.iterator()
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
            }catch (ex:Exception){Log.e("MA","AddBeacon")}
        }
    }


    private fun addDeviceToList(iBeacon: Beacon, list:ArrayList<Beacon>) {
        if (list.isNotEmpty()) {
            val it: MutableIterator<Beacon> = list.iterator() as MutableIterator<Beacon>
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
        list.add(iBeacon)
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
            if (Util.checkBluetoothPermissionScan(this.applicationContext) && Util.checkBluetoothPermissionConnect(this.applicationContext)) {
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
                if (result != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        resultToBeacon(result)
                    }
                    checkType(result)
                }
            }

        }

    }


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission", "NewApi")
    private fun checkType(result: ScanResult){
        val baseDev = BaseDevice(result)
//        val Type = baseDev.deviceType
        val Type = DeviceManager.getDeviceType(result)
        GlobalScope.launch(Dispatchers.IO) {
            AddDevToDatabase(result)
        }

        when (Type) {
                DeviceType.AIRPODS -> {
                    try {
                        val textAp = findViewById<TextView>(R.id.APTextno)
                        beacons?.forEach {
                            if (it.type == DeviceType.AIRPODS)
                                if (!airPodsList.contains(it)) {
                                    airPodsList.add(it)
                                    addDeviceToList(it, airPodsList)
                                }
                        }

                        textAp.text = airPodsList.size.toString()
                    }catch (ex:ConcurrentModificationException){Log.e("MA","Iterator")}

                }

                DeviceType.IPhone -> {
                    try {
                        beacons?.forEach {
                            if (it.type == DeviceType.IPhone)
                                if (!iPhoneList.contains(it)) {
                                    iPhoneList.add(it)
                                    addDeviceToList(it,iPhoneList)
                                }
                        }

                        val textAD = findViewById<TextView>(R.id.iphoneTextno)
                        textAD.text = iPhoneList.size.toString()
                    }catch  (ex:ConcurrentModificationException){Log.e("MA","Iterator")}

                }

                DeviceType.AIRTAG -> {
                    try {
                        beacons?.forEach {
                            if (it.type == DeviceType.AIRTAG)
                                if (!airTagList.contains(it)) {
                                    airTagList.add(it)
                                    addDeviceToList(it,airTagList)
                                }
                        }
                        val textAT = findViewById<TextView>(R.id.atags)
                        textAT.text = airTagList.size.toString()
                    }catch  (ex:ConcurrentModificationException){Log.e("MA","Iterator")}

                }

                DeviceType.FIND_MY -> {
                    try {
                        beacons?.forEach {
                            if (it.type == DeviceType.FIND_MY)
                                if (!fmdList.contains(it)) {
                                    fmdList.add(it)
                                    addDeviceToList(it,fmdList)

                                }
                        }

                        val textFMD = findViewById<TextView>(R.id.findMyDev)
                        textFMD.text = fmdList.size.toString()
                    }catch  (ex:ConcurrentModificationException){Log.e("MA","Iterator")}

                }

                DeviceType.TILE -> {
                    try {
                        val textTile = findViewById<TextView>(R.id.tileFoundText)
                        beacons?.forEach {
                            if (it.type == DeviceType.TILE)
                                if (!tileList.contains(it)) {
                                    tileList.add(it)
                                    addDeviceToList(it,tileList)

                                }
                        }
                        textTile.text = tileList.size.toString()
                    }catch (ex:ConcurrentModificationException){Log.e("MA","Iterator")}

                }

                DeviceType.APPLE -> {
                    try {
                        val textADT = findViewById<TextView>(R.id.AppleDevText)
                        beacons?.forEach {
                            if (it.type == DeviceType.APPLE)
                                if (!appleList.contains(it)) {
                                    appleList.add(it)
                                    addDeviceToList(it,appleList)
                                }
                        }
                        textADT.text = appleList.size.toString()
                    }catch (ex:ConcurrentModificationException){Log.e("MA","Iterator")}

                }

                DeviceType.UNKNOWN -> {
                    try {
                        val textund = findViewById<TextView>(R.id.unkDevText)
                        beacons?.forEach {
                            if (it.type == DeviceType.UNKNOWN)
                                if (!unkList.contains(it)) {
                                    unkList.add(it)
                                    addDeviceToList(it,unkList)
                                }
                        }
                        textund.text = unkList.size.toString()
                    }catch (ex:ConcurrentModificationException){Log.e("MA","Iterator")}

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
        //Edit this to false and add ! this in final version
        try {
            val ind = beacons!!.indexOf(resultToBeacon(result))
            val mac = beacons!![ind].address.replace(":","")
            if (!db?.CheckIsDataAlreadyInDBorNot("devices_table", "SerialNumber", mac)!!) {

                if (!result.device.name.isNullOrEmpty()) {
                    val dev = BaseDevice(result)
                    db!!.addDevice(
                        result.device?.name.toString(),
                        result.device.address.replace(":", ""),
                        result.rssi.toFloat(),
                        dev.firstDiscovery.second.toString(),
                        dev.lastSeen.second.toString(),
                        dev.uniqueId!!,
                        dev.deviceType!!
                    )

                } else {
                    for (i in 0 until beacons!!.size + 1) {
                        Name = "Device $i"

                    }
                    val dev = BaseDevice(result)
                    db!!.addDevice(
                        Name!!,
                        result.device.address.replace(":", ""),
                        result.rssi.toFloat(),
                        dev.firstDiscovery.second.toString(),
                        dev.lastSeen.second.toString(),
                        dev.uniqueId!!,
                        dev.deviceType!!
                    )

                }
            } else {
//            db?.readableDatabase?.isOpen
                if (result.device.name.isNullOrEmpty())
                    db?.updateLastSeen("devices_table",Name!!, BaseDevice(result).lastSeen.second.toString())
                else
                    db?.updateLastSeen("devices_table", result.device.name, BaseDevice(result).lastSeen.second.toString())

                Log.e("MADataBase", "Exist in DB")
            }
        } catch (ex: Exception){ Log.e("DBEx",ex.message.toString())}

    }


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {

        GlobalScope.launch(Dispatchers.IO) {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
//                    if (scan_list.isNotEmpty()) {
                    Log.i("ScanListMA","Stopped")


//                    }
                }, SCAN_PERIOD)
                scanning = true
                val sett = SCAN_MODE_LOW_LATENCY
                val settBuilder = ScanSettings.Builder().setScanMode(sett).build()
                m_bluetoothAdapter?.bluetoothLeScanner?.startScan(
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

        iPhoneList.clear()
        airPodsList.clear()
        airTagList.clear()
        fmdList.clear()
        appleList.clear()
        unkList.clear()
        tileList.clear()

        m_bluetoothAdapter?.bluetoothLeScanner?.stopScan(lleScanCallback)
        m_bluetoothAdapter = null
        db?.close()

        devfilters.clear()
//        unregisterReceiver(mReceiver)
    }
}