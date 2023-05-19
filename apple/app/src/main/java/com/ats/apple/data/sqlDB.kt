package com.ats.apple.data

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ats.apple.data.model.BaseDevice
import com.ats.apple.util.types.AirPods
import com.ats.apple.util.types.AirTag
import com.ats.apple.util.types.Beacon
import com.ats.apple.util.types.FindMy
import com.ats.apple.util.types.IPhoneDevice
import com.ats.apple.util.types.Tile
import com.ats.apple.util.types.Unknown


class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        //Error Hereeeeeee
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                NAME_COl + " TEXT," +
                sn + " TEXT," +
                rssi + " FLOAT, " +
                firstSeen + " TEXT," +
                lastSeen + " TEXT," +
                uid + " TEXT," +
                DeviceType + " TEXT"+")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    // This method is for adding data in our database
    fun addDevice(name : String, Serial : String, Rssi:Float, FS:String, LS:String,uuid:String,type: DeviceType){

        // below we are creating
        // a content values variable
        val values = ContentValues()

        // we are inserting our values
        // in the form of key-value pair
        values.put(NAME_COl, name)
        values.put(sn, Serial)
        values.put(rssi,Rssi)
        values.put(firstSeen,FS)
        values.put(lastSeen,LS)
        values.put(uid,uuid)
        values.put(DeviceType,type.name)


        // here we are creating a
        // writable variable of
        // our database as we want to
        // insert value in our database
        val db = this.writableDatabase

        // all values are inserted into database
        db.insert(TABLE_NAME, null, values)

        // at last we are
        // closing our database
        db.close()
    }

    // below is the method for updating our courses
    fun updateCourse(
        originalCourseName: String, name : String?, Serial : String? ,Rssi:Float? , FS:String?,LS:String?
    ) {

        // calling a method to get writable database.
        val db = this.writableDatabase
        val values = ContentValues()

        // on below line we are passing all values
        // along with its key and value pair.
        values.put(NAME_COl, name)
        values.put(sn, Serial)
        values.put(rssi,Rssi)
        values.put(firstSeen,FS)
        values.put(lastSeen,LS)

        // on below line we are calling a update method to update our database and passing our values.
        // and we are comparing it with name of our course which is stored in original name variable.
        db.update(TABLE_NAME, values, "name=?", arrayOf(originalCourseName))
        db.close()
    }


    // below method is to get
    // all data from our database
    fun getName(): Cursor? {

        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)

    }

    fun getAllIphoneDetail(): Array<String?>? {
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $DeviceType = ${IPhoneDevice.deviceType.name} "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: Array<String?>? = null
        if (cursor.moveToFirst()) {
            do {

//                cursor.getString(0)
                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }

        return data
    }
    fun getAllAirTagDetail(): Array<String?>? {
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $DeviceType = ${AirTag.deviceType.name} "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: Array<String?>? = null
        if (cursor.moveToFirst()) {
            do {

                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data
    }
    fun getAllAirPodsDetail(): Array<String?>? {
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $DeviceType = ${AirPods.deviceType.name} "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: Array<String?>? = null
        if (cursor.moveToFirst()) {
            do {

                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data
    }

    fun getAllDetail(): Array<String?>? {
        val selectQuery = "SELECT  * FROM $TABLE_NAME"
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: Array<String?>? = null
        if (cursor.moveToFirst()) {
            do {

                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data
    }

    fun getAllUnknownDetails(): Array<String?>? {
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $DeviceType = ${Unknown.deviceType.name} "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: Array<String?>? = null
        if (cursor.moveToFirst()) {
            do {

                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data
    }
    fun getAllAppleDetail(): Cursor? {
        val selectQuery = "SELECT  * FROM $TABLE_NAME "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: Array<String?>? = null
        if (cursor.moveToFirst()) {
            do {

                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
//        cursor.close()
        return  cursor
    }

    fun getAllTilesDetail(): Array<String?>? {
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $DeviceType = ${Tile.deviceType.name} "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: Array<String?>? = null
        if (cursor.moveToFirst()) {
            do {

                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data
    }

    fun getAllFindMyDetail(): Array<String?>? {
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $DeviceType = ${FindMy.deviceType.name} "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: Array<String?>? = null
        if (cursor.moveToFirst()) {
            do {

                cursor.getColumnIndex("DevType")
                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data
    }

    @SuppressLint("Range")
    fun getDev(beacon:Beacon): MutableList<String?>? {
        val device = beacon
        val uuid = device.uuids
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $uid = $uuid "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: MutableList<String?>? = null
        if (cursor.moveToFirst()) {
            do {
//                val type = cursor.getString(cursor.getColumnIndex(devType.name))
                val uuid = cursor.getString(cursor.getColumnIndex(uid))
//                data.add(0,Beacon(type,)))
//                data?.add(0,type)
                data?.add(0,uuid)

                cursor.getColumnIndex("DevType")
                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data
    }


    companion object{
        // here we have defined variables for our database

        // below is variable for database name
        private val DATABASE_NAME = "BaseDevices"

        // below is the variable for database version
        private val DATABASE_VERSION = 5

        // below is the variable for table name
        val TABLE_NAME = "devices_table"

        // below is the variable for id column
        val ID_COL = "id"

        // below is the variable for name column
        val NAME_COl = "name"

        // below is the variable for age column
        val sn = "SerialNumebr"

        val rssi = "rssi"
        val firstSeen = "FirstSeen"
        val lastSeen = "lastSeen"
        val uid = "Uuid"
        val DeviceType = "DevType"


    }
}
