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
        checkBTPerms()
        setupUI()
        db = DBHelper(this, null)

////        db.addName()
//        db.writableDatabase
//
////        db.updateCourse()
//        val cursor = db.getName()
//        cursor!!.moveToFirst()
////        Name.append(cursor.getString(cursor.getColumnIndex(DBHelper.NAME_COl)) + "\n")
//
//        // moving the cursor to first position and
//        // appending value in the text view
//        cursor!!.count
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

//    fun addOrRemoveFilter(filter: Filter, remove: Boolean = false) {
//        val filterName = filter::class.toString()
//        if (remove) {
//            activeFilter.remove(filterName)
//        } else {
//            activeFilter[filterName] = filter
//        }
//
//        updateDeviceList()
////        Timber.d("Active Filter: $activeFilter")
//        updateFilterSummaryText()
//    }

//    private fun updateDeviceList() {
//        deviceRepository = DeviceRepository(DeviceDao)
//        devices.addSource(deviceRepository.devices.asLiveData()) {
//            var filteredDevices = it
//            activeFilter.forEach { (_, filter) ->
//                filteredDevices = filter.apply(filteredDevices)
//            }
//            devices.value = filteredDevices
//        }
//    }

//    private fun updateFilterSummaryText() {
//        val filterStringBuilder = StringBuilder()
//        val context = this.applicationContext
//        // No filters option
//        if (activeFilter.containsKey(IgnoredFilter::class.toString())) {
//            filterStringBuilder.append(context.getString(R.string.ignored_devices))
//            filterStringBuilder.append(", ")
//        }
//
//        if (activeFilter.containsKey(NotifiedFilter::class.toString())) {
//            filterStringBuilder.append(context.getString(R.string.tracker_detected))
//            filterStringBuilder.append(", ")
//        }
//
//        if (activeFilter.containsKey(DeviceTypeFilter::class.toString())) {
//            val deviceTypeFilter = activeFilter[DeviceTypeFilter::class.toString()] as DeviceTypeFilter
//            for (device in deviceTypeFilter.deviceTypes) {
//                filterStringBuilder.append(DeviceType.userReadableName(device))
//                filterStringBuilder.append(", ")
//            }
//            if (deviceTypeFilter.deviceTypes.count() > 0) {
//                filterStringBuilder.delete(filterStringBuilder.length-2, filterStringBuilder.length-1)
//            }
//        }else {
//            // All devices
//            filterStringBuilder.append(context.getString(R.string.title_device_map))
//        }
//
//        filterSummaryText.postValue(filterStringBuilder.toString())
//    }


    @SuppressLint("MissingPermission", "NewApi")
    private fun checkType(result: ScanResult){

        val baseDev = BaseDevice(result)

//        val Type = baseDev.deviceType
        val Type = DeviceManager.getDeviceType(result)

//        if (connectableDev!!){
//            if (Type == DeviceType.AIRTAG){
//                val textAT = findViewById<TextView>(R.id.atags)
//                if (AirTagInt != null) AirTagInt!!.inc() else AirTagInt = 1
//                textAT.text = AirTagInt.toString()
//                Toast.makeText(this@MainActivity,"AirTag Detected",Toast.LENGTH_SHORT).show()
//            }
//            if (Type == DeviceType.AIRPODS){
//                val textAp = findViewById<TextView>(R.id.APTextno)
//                if (AIRPODSInt != null) AIRPODSInt!!.inc() else AIRPODSInt = 1
//                textAp.text = AIRPODSInt.toString()
//                Toast.makeText(this@MainActivity,"Airpods Detected",Toast.LENGTH_SHORT).show()
//
//            }
//        }else{
        when (Type) {
                DeviceType.AIRPODS -> {
                    val textAp = findViewById<TextView>(R.id.APTextno)
                    if (AIRPODSInt != null) AIRPODSInt!!.inc() else AIRPODSInt = 1
                    textAp.text = AIRPODSInt.toString()
                    AddDevToDatabase(result)
                }

                DeviceType.IPhone -> {
                    val textAD = findViewById<TextView>(R.id.iphoneTextno)
                    if (IphoneInt != null) IphoneInt!!.inc() else IphoneInt = 1
                    textAD.text = IphoneInt.toString()
                    AddDevToDatabase(result)

                }

                DeviceType.AIRTAG -> {
                    val textAT = findViewById<TextView>(R.id.atags)
                    if (AirTagInt != null) AirTagInt!!.inc() else AirTagInt = 1
                    textAT.text = AirTagInt.toString()
                    AddDevToDatabase(result)

//                    Toast.makeText(this@MainActivity, "AirTag Detected", Toast.LENGTH_SHORT).show()

                }

                DeviceType.FIND_MY -> {
                    val textFMD = findViewById<TextView>(R.id.findMyDev)
                    if (FMDInt != null) FMDInt!!.inc() else FMDInt = 1
                    textFMD.text = FMDInt.toString()
                    AddDevToDatabase(result)

                }

                DeviceType.TILE -> {
                    val textTile = findViewById<TextView>(R.id.tileFoundText)
                    if (TilesInt != null) TilesInt!!.inc() else TilesInt = 1
                    textTile.text = TilesInt.toString()
                    AddDevToDatabase(result)

                }

                DeviceType.APPLE -> {
                    val textADT = findViewById<TextView>(R.id.AppleDevText)
                    if (ADInt != null) ADInt!!.inc() else ADInt = 1
                    textADT.text = ADInt.toString()
                    AddDevToDatabase(result)

                }

                DeviceType.UNKNOWN -> {
                    val textund = findViewById<TextView>(R.id.unkDevText)
                    if (unkInt != null) unkInt!!.inc() else unkInt = 1
                    textund.text = unkInt.toString()
                    AddDevToDatabase(result)

                }

                else -> {


                }

            }

//        }


    }

    @SuppressLint("NewApi")
    private fun AddDevToDatabase(result: ScanResult){
        val dev = BaseDevice(result)
        db!!.addDevice(result.scanRecord?.deviceName.toString()
            ,result.device.address
            ,result.rssi.toFloat()
            ,dev.firstDiscovery.second.toString()
            ,dev.lastSeen.second.toString()
            ,dev.uniqueId!!
            ,dev.deviceType!!)

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
        //Apple Dev
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

        devfilters.add(0,ATbluetoothFilter)
        devfilters.add(1,ADbluetoothFilter)
        devfilters.add(2,APbluetoothFilter)
        devfilters.add(3,PhonebluetoothFilter)
        devfilters.add(4,FMDFilter)
        devfilters.add(5,TileFilter)
        devfilters.add(6,unkFilter)
//        devfilters.add(7,AllbluetoothFilter)

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

        devfilters.clear()
//        unregisterReceiver(mReceiver)
    }
}