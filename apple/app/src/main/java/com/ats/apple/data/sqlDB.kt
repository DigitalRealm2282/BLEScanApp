package com.ats.apple.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getStringOrNull
import androidx.room.RoomMasterTable
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
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                NAME_COl + " TEXT," +
                SERIALKEY + " TEXT," +
                SIGNALKEY + " FLOAT, " +
                FSEENKEY + " TEXT, " +
                LSEENKEY + " TEXT, " +
                UIDKEY + " TEXT, " +
                TYPEKEY + " TEXT )")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun CheckIsDataAlreadyInDBorNot(
        TableName: String,
        dbfield: String, fieldValue: String
    ): Boolean {
        val sqldb: SQLiteDatabase = this.readableDatabase
        val Query = "Select * from $TableName where $dbfield = $fieldValue"
        val cursor = sqldb.rawQuery(Query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    fun Exists(searchItem: String): Boolean {
        val columns = arrayOf<String>(SERIALKEY)
        val selection: String = "$SERIALKEY =?"
        val selectionArgs = arrayOf(searchItem)
        val limit = "1"
        val cursor: Cursor = this.readableDatabase.query(
            TABLE_NAME,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null,
            limit
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

//    fun checkDuplicate(beacon: Beacon):Boolean {
//        try {
//            val db = this.writableDatabase
//            val address = beacon.address.replace(":","")
//            val c = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $SERIALKEY = $address", null)
//
//            if (c.moveToFirst()) {
////            showMessage("Error", "Record exist");
//                c.close()
//                return false
//            } else {
//                // Inserting record
//                c.close()
//                return true
//            }
//        }catch (ex:SQLiteException){
//            Log.e("SqlLite",ex.message.toString())
//
//        }
//        return false
//
//    }

    // This method is for adding data in our database
    fun addDevice(name : String, Serial : String, Rssi:Float, FS:String, LS:String,uuid:String,type: DeviceType){

        // below we are creating
        // a content values variable
        val values = ContentValues()

        // we are inserting our values
        // in the form of key-value pair
        values.put(NAME_COl, name)
        values.put(SERIALKEY, Serial)
        values.put(SIGNALKEY,Rssi)
        values.put(FSEENKEY,FS)
        values.put(LSEENKEY,LS)
        values.put(UIDKEY,uuid)
        values.put(TYPEKEY,type.name)


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
        values.put(SERIALKEY, Serial)
        values.put(SIGNALKEY,Rssi)
        values.put(FSEENKEY,FS)
        values.put(LSEENKEY,LS)

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


    @SuppressLint("Range")
    fun getSpecificType(type: DeviceType): MutableList<Beacon>? {
        val typeName = type.name
        val sqldb: SQLiteDatabase = this.readableDatabase
        val Query = "Select * from $TABLE_NAME where $TYPEKEY = $typeName"
        val cursor = sqldb.rawQuery(Query, null)
        val data: MutableList<Beacon>? = null

        if (cursor.moveToFirst()) {
            do {

                val name = cursor.getString(cursor.getColumnIndex(NAME_COl))
                val add = cursor.getString(cursor.getColumnIndex(SERIALKEY))
                val rssi = cursor.getString(cursor.getColumnIndex(SIGNALKEY))
                val fs = cursor.getString(cursor.getColumnIndex(FSEENKEY))
                val ls = cursor.getString(cursor.getColumnIndex(LSEENKEY))
                val uid = cursor.getString(cursor.getColumnIndex(UIDKEY))
                val type = cursor.getString(cursor.getColumnIndex(TYPEKEY))
                val dev:DeviceType = when (type) {
                    "IPhone" -> DeviceType.APPLE
                    "UNKNOWN" -> DeviceType.UNKNOWN
                    "AIRPODS" -> DeviceType.AIRPODS
                    "AIRTAG" -> DeviceType.AIRTAG
                    "TILE" -> DeviceType.TILE
                    "APPLE" -> DeviceType.APPLE
                    "FIND_MY" -> DeviceType.FIND_MY
                    else -> DeviceType.UNKNOWN
                }

                data?.add(Beacon(name!!,add!!,rssi.toString(),uid!!,dev,fs!!,ls!!))
//                data.add(Beacon())
                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data

    }

    @SuppressLint("Range")
    fun getAllDetail(): MutableList<Beacon?>? {
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: MutableList<Beacon?>? = null
        if (cursor.moveToFirst()) {
            do {

                val name = cursor.getString(cursor.getColumnIndex(NAME_COl))
                val add = cursor.getString(cursor.getColumnIndex(SERIALKEY))
                val rssi = cursor.getString(cursor.getColumnIndex(SIGNALKEY))
                val fs = cursor.getString(cursor.getColumnIndex(FSEENKEY))
                val ls = cursor.getString(cursor.getColumnIndex(LSEENKEY))
                val uid = cursor.getString(cursor.getColumnIndex(UIDKEY))
                val type = cursor.getString(cursor.getColumnIndex(TYPEKEY))
                val dev:DeviceType = when (type) {
                    "IPhone" -> DeviceType.APPLE
                    "UNKNOWN" -> DeviceType.UNKNOWN
                    "AIRPODS" -> DeviceType.AIRPODS
                    "AIRTAG" -> DeviceType.AIRTAG
                    "TILE" -> DeviceType.TILE
                    "APPLE" -> DeviceType.APPLE
                    "FIND_MY" -> DeviceType.FIND_MY
                    else -> DeviceType.UNKNOWN
                }



                data?.add(Beacon(name!!,add!!,rssi.toString(),uid!!,dev,fs!!,ls!!))
//                data.add(Beacon())
                // get the data into array, or class variable
            } while (cursor.moveToNext())
        }
        cursor.close()
        return data
    }


    @SuppressLint("Range")
    fun getDev(beacon:Beacon): MutableList<String?>? {
        val deviceUID = beacon.uuids
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $UIDKEY = $deviceUID "
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val data: MutableList<String?>? = null
        if (cursor.moveToFirst()) {
            do {

                val name = cursor.getString(cursor.getColumnIndex(NAME_COl))
                val add = cursor.getString(cursor.getColumnIndex(SERIALKEY))
                val rssi = cursor.getString(cursor.getColumnIndex(SIGNALKEY))
                val fs = cursor.getString(cursor.getColumnIndex(FSEENKEY))
                val ls = cursor.getString(cursor.getColumnIndex(LSEENKEY))
                val uid = cursor.getString(cursor.getColumnIndex(UIDKEY))
                val type = cursor.getString(cursor.getColumnIndex(TYPEKEY))
//                val type = cursor.getString(cursor.getColumnIndex(devType.name))
                val uuid = cursor.getString(cursor.getColumnIndex(UIDKEY))
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
        private const val DATABASE_NAME = "BaseDevices"

        // below is the variable for database version
        private const val DATABASE_VERSION = 8

        // below is the variable for table name
        const val TABLE_NAME = "devices_table"

        // below is the variable for id column
        const val ID_COL = "id"

        // below is the variable for name column
        const val NAME_COl = "name"

        // below is the variable for age column
        const val SERIALKEY = "SerialNumber"

        const val SIGNALKEY = "rssi"
        const val FSEENKEY = "FirstSeen"
        const val LSEENKEY = "lastSeen"
        const val UIDKEY = "Uuid"
        const val TYPEKEY = "DevType"


    }
}
