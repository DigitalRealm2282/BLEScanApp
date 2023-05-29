package com.ats.airflagger.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.util.Log
import androidx.room.RoomMasterTable
import com.ats.airflagger.util.types.Beacon


class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                NAME_COl + " TEXT," +
                SERIALKEY + " TEXT," +
                SIGNALKEY + " FLOAT, " +
                FSEENKEY + " TEXT, " +
                LSEENKEY + " TEXT, " +
                UIDKEY + " TEXT, " +
                TYPEKEY + " TEXT "+ ");")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

//    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
//        // this method is to check if table already exists
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
//        onCreate(db)
//    }

    fun CheckIsDataAlreadyInDBorNot(
        TableName: String,
        dbfield: String, fieldValue: String
    ): Boolean {

        val sqldb: SQLiteDatabase = this.readableDatabase
        val Query = "Select * from $TableName where $dbfield = '$fieldValue'"
        val cursor = sqldb.rawQuery(Query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true

    }

//    fun Exists(searchItem: String): Boolean {
//        val columns = arrayOf<String>(SERIALKEY)
//        val selection: String = "$SERIALKEY =?"
//        val selectionArgs = arrayOf(searchItem)
//        val limit = "1"
//        val cursor: Cursor = this.readableDatabase.query(
//            TABLE_NAME,
//            columns,
//            selection,
//            selectionArgs,
//            null,
//            null,
//            null,
//            limit
//        )
//        val exists = cursor.count > 0
//        cursor.close()
//        return exists
//    }

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

//        db.close()
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

    // below is the method for updating our courses
    fun updateLastSeen(
        originalCourseName: String,name: String,LS:String?
    ) {

        // calling a method to get writable database.
        val db = this.writableDatabase
        val values = ContentValues()

        // on below line we are passing all values
        // along with its key and value pair.
//        values.put(NAME_COl, name)
//        values.put(SERIALKEY, Serial)
//        values.put(SIGNALKEY,Rssi)
//        values.put(FSEENKEY,FS)
        values.put(LSEENKEY,LS)

        // on below line we are calling a update method to update our database and passing our values.
        // and we are comparing it with name of our course which is stored in original name variable.
        db.update(TABLE_NAME, values, "name = '$name'", arrayOf(originalCourseName))
        db.close()
    }


    @SuppressLint("Range")
    fun getSpecificType(type: DeviceType): ArrayList<Beacon>? {
        val typeName = type.name
        val sqldb: SQLiteDatabase = this.readableDatabase
        val Query = "Select * from $TABLE_NAME where $TYPEKEY = '$typeName'"
        val cursor = sqldb.rawQuery(Query, null)
        val data: ArrayList<Beacon>? = null
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
                    "IPhone" -> DeviceType.IPhone
                    "UNKNOWN" -> DeviceType.UNKNOWN
                    "AIRPODS" -> DeviceType.AIRPODS
                    "AIRTAG" -> DeviceType.AIRTAG
                    "TILE" -> DeviceType.TILE
                    "APPLE" -> DeviceType.APPLE
                    "FIND_MY" -> DeviceType.FIND_MY
                    else -> DeviceType.UNKNOWN
                }
                Log.d("SQL",name!!+add!!+rssi.toString()+uid!!+dev+fs!!+ls!!)
//                val beacon = Beacon(name!!,add!!,rssi.toString(),uid!!,dev,fs!!,ls!!)
                data?.add(Beacon(name!!,add!!,rssi.toString(),uid!!,dev,fs!!,ls!!))
                // get the data into array, or class variable
            } while (cursor.moveToNext())
//            return data
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
        private const val DATABASE_VERSION = 2

        // below is the variable for table name
        const val TABLE_NAME = "devices_table"

        // below is the variable for id column
        const val ID_COL = "rowid"

        // below is the variable for name column
        const val NAME_COl = "name"

        // below is the variable for device column
        const val SERIALKEY = "SerialNumber"

        const val SIGNALKEY = "rssi"
        const val FSEENKEY = "FirstSeen"
        const val LSEENKEY = "lastSeen"
        const val UIDKEY = "Uuid"
        const val TYPEKEY = "DevType"


    }
}
