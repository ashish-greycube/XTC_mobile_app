package com.xtc_gelato.localDB

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.models.beanPickedScanDetailsDB
import com.xtc_gelato.models.beanPickupDetailsDB

class DatabaseHandler(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    companion object {

        // database version
        private val DATABASE_VERSION = 3 //07-12-2022

        // database name
        private val DATABASE_NAME = "xtc_gelato_picking.db"

        // table name
        private val TABLE_PICKUP_DETAILS = "tbl_pickup_details"
        private val TABLE_PICKED_DETAILS = "tbl_picked_details"

        private val KEY_ID : String = "id"

        // TABLE_PICKUP_DETAILS table column name
        private val KEY_SO_NO : String = "so_no"
        private val KEY_CLIENT : String = "client"
        private val KEY_PICKER : String = "picker"
        private val KEY_SO_LINE_NO : String = "so_line_no"
        private val KEY_ITEM_CODE : String = "item_code"
        private val KEY_ITEM_NAME : String = "item_name"
        private val KEY_QUANTITY : String = "quantity"
        private val KEY_BATCH_DETAILS_JSON : String = "batch_details_json"

        // TABLE_PICKED_DETAILS table column name
        private val KEY_PICKED_SO_NO : String = "picked_so_no"
        private val KEY_PICKED_CLIENT : String = "picked_client"
        private val KEY_PICKED_PICKER : String = "picked_picker"
        private val KEY_PICKED_SO_LINE_NO : String = "picked_so_line_no"
        private val KEY_PICKED_ITEM_CODE : String = "picked_item_code"
        private val KEY_PICKED_ITEM_NAME : String = "picked_item_name"
        private val KEY_PICKED_QUANTITY : String = "picked_quantity"
        private val KEY_PICKED_BATCH : String = "picked_batch"

    }

    override fun onCreate(db: SQLiteDatabase) {

        val CREATE_TABLE_PICKUP_DETAILS =
            ("CREATE TABLE " + TABLE_PICKUP_DETAILS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_SO_NO + " TEXT,"
                    + KEY_CLIENT + " TEXT,"
                    + KEY_PICKER + " TEXT,"
                    + KEY_SO_LINE_NO + " TEXT,"
                    + KEY_ITEM_CODE + " TEXT,"
                    + KEY_ITEM_NAME + " TEXT,"
                    + KEY_QUANTITY + " TEXT,"
                    + KEY_BATCH_DETAILS_JSON + " TEXT " + ")")


        val CREATE_TABLE_PICKED_DETAILS =
            ("CREATE TABLE " + TABLE_PICKED_DETAILS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_PICKED_SO_NO + " TEXT,"
                    + KEY_PICKED_CLIENT + " TEXT,"
                    + KEY_PICKED_PICKER + " TEXT,"
                    + KEY_PICKED_SO_LINE_NO + " TEXT,"
                    + KEY_PICKED_ITEM_CODE + " TEXT,"
                    + KEY_PICKED_ITEM_NAME + " TEXT,"
                    + KEY_PICKED_QUANTITY + " TEXT,"
                    + KEY_PICKED_BATCH + " TEXT " + ")")

        db.execSQL(CREATE_TABLE_PICKUP_DETAILS)
        db.execSQL(CREATE_TABLE_PICKED_DETAILS)

    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PICKUP_DETAILS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PICKED_DETAILS")
        onCreate(db)
    }



    // Pickup details tables..................

    fun addPickupDetails(strSoNo: String?,strClient: String?,strPicker: String?,strLineNo: String?,strItemCode: String?,strItemName: String?,
                         strQuantity: String?, strBatchDetailsJson: String?) {

        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_SO_NO, strSoNo)
        values.put(KEY_CLIENT, strClient)
        values.put(KEY_PICKER, strPicker)
        values.put(KEY_SO_LINE_NO, strLineNo)
        values.put(KEY_ITEM_CODE, strItemCode)
        values.put(KEY_ITEM_NAME, strItemName)
        values.put(KEY_QUANTITY, strQuantity)
        values.put(KEY_BATCH_DETAILS_JSON, strBatchDetailsJson)
        db.insert(TABLE_PICKUP_DETAILS, null, values)
        db.close()

        AppConstants.LOGE("local_db pickup_details_insert => ","success")

    }

    @SuppressLint("Range")
    fun getPickupDetailsList():List<beanPickupDetailsDB>{
        val arrPickupDetails: ArrayList<beanPickupDetailsDB> = ArrayList<beanPickupDetailsDB>()
        val selectQuery = "SELECT * FROM $TABLE_PICKUP_DETAILS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var strSoNo: String
        var strClient: String
        var strPicker: String
        var strLineNo: String
        var strItemCode: String
        var strItemName: String
        var strQuantity: String
        var strBatchDetailsJson: String

        if (cursor.moveToFirst()) {
            do {
                strSoNo = cursor.getString(cursor.getColumnIndex(KEY_SO_NO))
                strClient = cursor.getString(cursor.getColumnIndex(KEY_CLIENT))
                strPicker = cursor.getString(cursor.getColumnIndex(KEY_PICKER))
                strLineNo = cursor.getString(cursor.getColumnIndex(KEY_SO_LINE_NO))
                strItemCode = cursor.getString(cursor.getColumnIndex(KEY_ITEM_CODE))
                strItemName = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NAME))
                strQuantity = cursor.getString(cursor.getColumnIndex(KEY_QUANTITY))
                strBatchDetailsJson = cursor.getString(cursor.getColumnIndex(KEY_BATCH_DETAILS_JSON))

                val objStore = beanPickupDetailsDB(strSoNo,strClient,strPicker,strLineNo,strItemCode,strItemName,strQuantity,strBatchDetailsJson)

                arrPickupDetails.add(objStore)


            } while (cursor.moveToNext())
        }
        return arrPickupDetails
    }

    @SuppressLint("Range")
    fun getPickupDetailsListSoNoWise(soNo: String):List<beanPickupDetailsDB>{
        val arrPickupDetails: ArrayList<beanPickupDetailsDB> = ArrayList<beanPickupDetailsDB>()
        val selectQuery = "SELECT * FROM $TABLE_PICKUP_DETAILS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var strSoNo: String
        var strClient: String
        var strPicker: String
        var strLineNo: String
        var strItemCode: String
        var strItemName: String
        var strQuantity: String
        var strBatchDetailsJson: String

        if (cursor.moveToFirst()) {
            do {
                strSoNo = cursor.getString(cursor.getColumnIndex(KEY_SO_NO))

                if(soNo.equals(strSoNo,ignoreCase = true)){

                    strClient = cursor.getString(cursor.getColumnIndex(KEY_CLIENT))
                    strPicker = cursor.getString(cursor.getColumnIndex(KEY_PICKER))
                    strLineNo = cursor.getString(cursor.getColumnIndex(KEY_SO_LINE_NO))
                    strItemCode = cursor.getString(cursor.getColumnIndex(KEY_ITEM_CODE))
                    strItemName = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NAME))
                    strQuantity = cursor.getString(cursor.getColumnIndex(KEY_QUANTITY))
                    strBatchDetailsJson = cursor.getString(cursor.getColumnIndex(KEY_BATCH_DETAILS_JSON))

                    val objStore = beanPickupDetailsDB(strSoNo,strClient,strPicker,strLineNo,strItemCode,strItemName,strQuantity,strBatchDetailsJson)
                    arrPickupDetails.add(objStore)
                }



            } while (cursor.moveToNext())
        }
        return arrPickupDetails
    }


    fun isExistsPickupItemCodeDetails(strItemCode: String): Boolean {
        val query = "SELECT * FROM $TABLE_PICKUP_DETAILS WHERE $KEY_ITEM_CODE = ?"
        val whereArgs = arrayOf(strItemCode)
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, whereArgs)
        val count = cursor.count
        cursor.close()
        return count >= 1
    }

    fun deletePickupDetailsByItemCode(strItemCode: String, soNo: String) {
        val db = this.writableDatabase
        db.delete(
            TABLE_PICKUP_DETAILS, "$KEY_ITEM_CODE = ? and $KEY_SO_NO = ?",
            arrayOf(java.lang.String.valueOf(strItemCode),java.lang.String.valueOf(soNo))
        )

        db.close()
    }


    // Picked details tables..................

    fun addScanPickedDetails(strSoNo: String?,strClient: String?,strPicker: String?,strLineNo: String?,strItemCode: String?,strItemName: String?,
                         strQuantity: String?, strBatchNo: String?) {

        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_PICKED_SO_NO, strSoNo)
        values.put(KEY_PICKED_CLIENT, strClient)
        values.put(KEY_PICKED_PICKER, strPicker)
        values.put(KEY_PICKED_SO_LINE_NO, strLineNo)
        values.put(KEY_PICKED_ITEM_CODE, strItemCode)
        values.put(KEY_PICKED_ITEM_NAME, strItemName)
        values.put(KEY_PICKED_QUANTITY, strQuantity)
        values.put(KEY_PICKED_BATCH, strBatchNo)
        db.insert(TABLE_PICKED_DETAILS, null, values)
        db.close()

        AppConstants.LOGE("local_db picked_scan_details_insert => ","success")

    }


    @SuppressLint("Range")
    fun getScanPickedListAll():List<beanPickedScanDetailsDB>{
        val arrScanPickedDetails: ArrayList<beanPickedScanDetailsDB> = ArrayList<beanPickedScanDetailsDB>()
        val selectQuery = "SELECT * FROM $TABLE_PICKED_DETAILS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var strSoNo: String
        var strClient: String
        var strPicker: String
        var strLineNo: String
        var strItemCode: String
        var strItemName: String
        var strQuantity: String
        var strBatchNo: String

        if (cursor.moveToFirst()) {
            do {
                strSoNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_NO))
                strClient = cursor.getString(cursor.getColumnIndex(KEY_PICKED_CLIENT))
                strPicker = cursor.getString(cursor.getColumnIndex(KEY_PICKED_PICKER))
                strLineNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_LINE_NO))
                strItemCode = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_CODE))
                strItemName = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_NAME))
                strQuantity = cursor.getString(cursor.getColumnIndex(KEY_PICKED_QUANTITY))
                strBatchNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_BATCH))

                val objStore = beanPickedScanDetailsDB(strSoNo,strClient,strPicker,strLineNo,strItemCode,strItemName,strQuantity,strBatchNo)

                arrScanPickedDetails.add(objStore)

            } while (cursor.moveToNext())
        }
        return arrScanPickedDetails
    }

    @SuppressLint("Range")
    fun getScanPickedListSoNoWise(soNo: String):List<beanPickedScanDetailsDB>{
        val arrScanPickedDetails: ArrayList<beanPickedScanDetailsDB> = ArrayList<beanPickedScanDetailsDB>()
        val selectQuery = "SELECT * FROM $TABLE_PICKED_DETAILS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var strSoNo: String
        var strClient: String
        var strPicker: String
        var strLineNo: String
        var strItemCode: String
        var strItemName: String
        var strQuantity: String
        var strBatchNo: String

        if (cursor.moveToFirst()) {

            do {
                strSoNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_NO))

                if(soNo.equals(strSoNo,ignoreCase = true)){

                    strClient = cursor.getString(cursor.getColumnIndex(KEY_PICKED_CLIENT))
                    strPicker = cursor.getString(cursor.getColumnIndex(KEY_PICKED_PICKER))
                    strLineNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_LINE_NO))
                    strItemCode = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_CODE))
                    strItemName = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_NAME))
                    strQuantity = cursor.getString(cursor.getColumnIndex(KEY_PICKED_QUANTITY))
                    strBatchNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_BATCH))

                    val objStore = beanPickedScanDetailsDB(strSoNo,strClient,strPicker,strLineNo,strItemCode,strItemName,strQuantity,strBatchNo)
                    arrScanPickedDetails.add(objStore)
                }

            } while (cursor.moveToNext())
        }
        return arrScanPickedDetails
    }

    @SuppressLint("Range")
    fun getScanPickedListItemCodeWise(itemCode: String):List<beanPickedScanDetailsDB>{
        val arrScanPickedDetails: ArrayList<beanPickedScanDetailsDB> = ArrayList<beanPickedScanDetailsDB>()
        val selectQuery = "SELECT * FROM $TABLE_PICKED_DETAILS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var strSoNo: String
        var strClient: String
        var strPicker: String
        var strLineNo: String
        var strItemCode: String
        var strItemName: String
        var strQuantity: String
        var strBatchNo: String

        if (cursor.moveToFirst()) {

            do {
                strSoNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_NO))
                strClient = cursor.getString(cursor.getColumnIndex(KEY_PICKED_CLIENT))
                strPicker = cursor.getString(cursor.getColumnIndex(KEY_PICKED_PICKER))
                strLineNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_LINE_NO))
                strItemCode = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_CODE))
                strItemName = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_NAME))
                strQuantity = cursor.getString(cursor.getColumnIndex(KEY_PICKED_QUANTITY))
                strBatchNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_BATCH))

                if(itemCode.equals(strItemCode,ignoreCase = true)){
                    val objStore = beanPickedScanDetailsDB(strSoNo,strClient,strPicker,strLineNo,strItemCode,strItemName,strQuantity,strBatchNo)
                    arrScanPickedDetails.add(objStore)
                }

            } while (cursor.moveToNext())
        }
        return arrScanPickedDetails
    }

    @SuppressLint("Range")
    fun getScanPickedListLineNoWise(lineNo: String):List<beanPickedScanDetailsDB>{
        val arrScanPickedDetails: ArrayList<beanPickedScanDetailsDB> = ArrayList<beanPickedScanDetailsDB>()
        val selectQuery = "SELECT * FROM $TABLE_PICKED_DETAILS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var strSoNo: String
        var strClient: String
        var strPicker: String
        var strLineNo: String
        var strItemCode: String
        var strItemName: String
        var strQuantity: String
        var strBatchNo: String

        if (cursor.moveToFirst()) {

            do {
                strSoNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_NO))
                strClient = cursor.getString(cursor.getColumnIndex(KEY_PICKED_CLIENT))
                strPicker = cursor.getString(cursor.getColumnIndex(KEY_PICKED_PICKER))
                strLineNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_LINE_NO))
                strItemCode = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_CODE))
                strItemName = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_NAME))
                strQuantity = cursor.getString(cursor.getColumnIndex(KEY_PICKED_QUANTITY))
                strBatchNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_BATCH))

                if(lineNo.equals(strLineNo,ignoreCase = true)){
                    val objStore = beanPickedScanDetailsDB(strSoNo,strClient,strPicker,strLineNo,strItemCode,strItemName,strQuantity,strBatchNo)
                    arrScanPickedDetails.add(objStore)
                }

            } while (cursor.moveToNext())
        }
        return arrScanPickedDetails
    }


    fun updateScanPickedByBatchNo(strQuantity: String?, strBatchNo: String?, soNo: String?): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_PICKED_QUANTITY, strQuantity)
        //val isUpdate = db.update(TABLE_PICKED_DETAILS, values, "$KEY_PICKED_BATCH = ?", arrayOf(strBatchNo))
        val isUpdate = db.update(TABLE_PICKED_DETAILS, values, "$KEY_PICKED_BATCH = ? and $KEY_PICKED_SO_NO = ?", arrayOf(strBatchNo,soNo))

        AppConstants.LOGE("local_db updateScanPickedByBatchNo => ","success")

        return isUpdate
    }

    fun isExistsScanPickedBatchNoDetails(strBatchNo: String,soNo : String): Boolean {

        val query = "SELECT * FROM $TABLE_PICKED_DETAILS WHERE $KEY_PICKED_BATCH = ? and $KEY_PICKED_SO_NO = ?"
        val whereArgs = arrayOf(strBatchNo,soNo)

        val db = this.readableDatabase
        val cursor = db.rawQuery(query, whereArgs)
        val count = cursor.count
        cursor.close()
        return count >= 1
    }


    @SuppressLint("Range")
    fun getSinglePickedRecordByBatchNo(strBatchNo: String,soNo: String): beanPickedScanDetailsDB {

        val query = "SELECT * FROM $TABLE_PICKED_DETAILS WHERE $KEY_PICKED_BATCH = ? and $KEY_PICKED_SO_NO = ?"
        val whereArgs = arrayOf(strBatchNo,soNo)

        val db = this.readableDatabase

        try {
            val cursor = db.rawQuery(query, whereArgs)
            cursor?.moveToFirst()
            if (cursor != null) {

                val strSoNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_NO))
                val strClient = cursor.getString(cursor.getColumnIndex(KEY_PICKED_CLIENT))
                val strPicker = cursor.getString(cursor.getColumnIndex(KEY_PICKED_PICKER))
                val strLineNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_SO_LINE_NO))
                val strItemCode = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_CODE))
                val strItemName = cursor.getString(cursor.getColumnIndex(KEY_PICKED_ITEM_NAME))
                val strQuantity = cursor.getString(cursor.getColumnIndex(KEY_PICKED_QUANTITY))
                val batchNo = cursor.getString(cursor.getColumnIndex(KEY_PICKED_BATCH))

                return beanPickedScanDetailsDB(strSoNo,strClient,strPicker,strLineNo,strItemCode,strItemName,strQuantity,batchNo)

            }else{
                return beanPickedScanDetailsDB("","","","","","","0","")
            }
        } catch (e: Exception) {
            return beanPickedScanDetailsDB("","","","","","","0","")
        }

    }


    fun deleteScanPickedDetailsByBatchNoAndSoNo(strBatchNo: String,soNo: String) {
        val db = this.writableDatabase
        db.delete(TABLE_PICKED_DETAILS, "$KEY_PICKED_BATCH = ? and $KEY_PICKED_SO_NO = ?", arrayOf(java.lang.String.valueOf(strBatchNo),java.lang.String.valueOf(soNo)))
        db.close()
        AppConstants.LOGE("local_db deleteScanPicked SoNo => $soNo ","success")
    }


    fun deleteScanPickedDetailsBySoNo(strSoNo: String) {
        val db = this.writableDatabase
        db.delete(TABLE_PICKED_DETAILS, "$KEY_PICKED_SO_NO = ?", arrayOf(java.lang.String.valueOf(strSoNo)))
        db.close()
        AppConstants.LOGE("local_db deleteScanPicked SoNo => $strSoNo ","success")
    }

    fun deleteScanPickedDetailsByBatchNo(strSoNo: String) {
        val db = this.writableDatabase
        db.delete(TABLE_PICKED_DETAILS, "$KEY_PICKED_BATCH = ?", arrayOf(java.lang.String.valueOf(strSoNo)))
        db.close()
    }


}