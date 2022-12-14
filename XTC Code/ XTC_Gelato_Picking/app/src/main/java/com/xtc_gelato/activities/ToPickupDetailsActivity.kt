package com.xtc_gelato.activities

import NetworkConnection
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtc_gelato.R
import com.xtc_gelato.adapters.PickedListAdapter
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.constent_classes.AppPreference
import com.xtc_gelato.localDB.DatabaseHandler
import com.xtc_gelato.models.beanPickedScanDetailsDB
import com.xtc_gelato.models.beanPickupDetailsDB
import com.xtc_gelato.server_api_calls.API
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

/*
* This ToPickupDetails class is used to get pickup details from API and store in local DB and scan batch code functionality and upload all scan data in API.
* */
class ToPickupDetailsActivity : AppCompatActivity() {

    private lateinit var btnConfirmBottom: Button
    private lateinit var edtQtyBottom: EditText
    private lateinit var textSerialNumberBottom: TextView
    private lateinit var relativeCloseBottom: RelativeLayout
    private lateinit var relativeEditView: RelativeLayout

    private lateinit var textErrorText: TextView
    private lateinit var pickupListAdapter: PickedListAdapter
    private lateinit var arrPickupDetails: List<beanPickupDetailsDB>
    private lateinit var localDB: DatabaseHandler
    private lateinit var relativeProgressView: RelativeLayout
    private lateinit var btnPrevious: AppCompatButton
    private lateinit var btnNext: AppCompatButton
    private lateinit var btnCreateDeliveryNote: AppCompatButton

    private lateinit var imgItemStatus: ImageView
    private lateinit var imgClear: ImageView
    private lateinit var edtBatchNumber: EditText
    private lateinit var relativeBatchNo: RelativeLayout

    private lateinit var linearBatchCodeNotFound: LinearLayout
    private lateinit var linearBatchEnterView: LinearLayout

    private lateinit var recyclerPickedItems: RecyclerView
    private lateinit var textProductName: TextView
    private lateinit var textProductQuantity: TextView

    private lateinit var textSerialNo: TextView
    private lateinit var textClient: TextView
    private lateinit var linearChildHeader: LinearLayout
    private lateinit var linearChildSection: LinearLayout

    private lateinit var imgHeaderIcon: ImageView
    private lateinit var textHeaderText: TextView
    private lateinit var relativeRight: RelativeLayout
    private lateinit var relativePrintLabelURL: RelativeLayout
    private lateinit var relativeLeft: RelativeLayout

    private lateinit var strLoginUsername: String
    private lateinit var strLoginUserEmail: String
    private lateinit var printLabelURL: String
    private lateinit var clientName: String
    private lateinit var soNo: String
    private lateinit var pickerInstruction: String

    private val REQUEST_CAMERA_PERMISSION = 201

    var currentPosition = 0

    var isAllQtyScanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_pickup_details)

        AppConstants.statusbarColorWhiteChange(this)

        localDB = DatabaseHandler(this@ToPickupDetailsActivity)

        strLoginUsername = AppPreference.getStringPreference(this@ToPickupDetailsActivity, AppPreference.fullName)
        strLoginUserEmail = AppPreference.getStringPreference(this@ToPickupDetailsActivity, AppPreference.userEmail)
        printLabelURL = intent.getStringExtra("print_label_url")!!
        clientName = intent.getStringExtra("client_name")!!
        soNo = intent.getStringExtra("so_no")!!
        pickerInstruction = intent.getStringExtra("picker_instruction")!!

        relativeLeft = findViewById<View>(R.id.relativeLeft) as RelativeLayout
        relativeRight = findViewById<View>(R.id.relativeRight) as RelativeLayout
        val linearHeaderTitle = findViewById<View>(R.id.linearHeaderTitle) as LinearLayout
        relativePrintLabelURL = findViewById<View>(R.id.relativePrintLabelURL) as RelativeLayout
        relativeRight.visibility = View.VISIBLE

        textHeaderText = findViewById<View>(R.id.textHeaderText) as TextView
        imgHeaderIcon = findViewById<View>(R.id.imgHeaderIcon) as ImageView
        imgHeaderIcon.background = getDrawable(R.drawable.icon_menu_active_stock)

        textHeaderText.text = "to pick"

        val linearPickerInstruction = findViewById<View>(R.id.linearPickerInstruction) as LinearLayout
        val textPickerInstruction = findViewById<View>(R.id.textPickerInstruction) as TextView
        if(AppConstants.isNotEmpty(pickerInstruction)){
            linearPickerInstruction.visibility = View.VISIBLE
            textPickerInstruction.text = pickerInstruction.toString().trim()
        }else{
            linearPickerInstruction.visibility = View.GONE
        }

        relativePrintLabelURL.setOnClickListener {

            if(AppConstants.isNotEmpty(printLabelURL)){

                if (NetworkConnection.hasConnection(this)) {

                    // External browser open....
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(printLabelURL))
                    startActivity(browserIntent)

                }else{
                    AppConstants.ToastMessage(this@ToPickupDetailsActivity,resources.getString(R.string.Please_Check_Your_Internet_Connection))
                }

            }else{
                AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Print label url not available.")
            }

        }

        linearHeaderTitle.setOnClickListener { relativeLeft.performClick() }

        relativeLeft.setOnClickListener {
            onBackPressed()
        }

        relativeRight.setOnClickListener {
            if (NetworkConnection.hasConnection(this)) {
                getOrderDetails()
            }else{
                AppConstants.CheckConnection(this)
            }

        }

        relativeProgressView = findViewById<View>(R.id.relativeProgressView) as RelativeLayout

        linearChildHeader = findViewById<View>(R.id.linearChildHeader) as LinearLayout
        textClient = findViewById<View>(R.id.textClient) as TextView
        textSerialNo = findViewById<View>(R.id.textSerialNo) as TextView

        textClient.text = clientName.toString().trim()
        textSerialNo.text = soNo.toString().trim()

        textProductName = findViewById<View>(R.id.textProductName) as TextView
        textProductQuantity = findViewById<View>(R.id.textProductQuantity) as TextView

        recyclerPickedItems = findViewById<View>(R.id.recyclerPickedItems) as RecyclerView
        recyclerPickedItems.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerPickedItems.setHasFixedSize(true)
        recyclerPickedItems.isNestedScrollingEnabled = false

        linearBatchCodeNotFound = findViewById<View>(R.id.linearBatchCodeNotFound) as LinearLayout
        linearBatchEnterView = findViewById<View>(R.id.linearBatchEnterView) as LinearLayout
        imgClear = findViewById<View>(R.id.imgClear) as ImageView
        imgClear.visibility = View.GONE
        imgItemStatus = findViewById<View>(R.id.imgItemStatus) as ImageView
        imgItemStatus.background = getDrawable(R.drawable.icon_status_grey)

        relativeBatchNo = findViewById<View>(R.id.relativeBatchNo) as RelativeLayout
        textErrorText = findViewById<View>(R.id.textErrorText) as TextView
        edtBatchNumber = findViewById<View>(R.id.edtBatchNumber) as EditText

        edtBatchNumber.isFocusableInTouchMode = true
        edtBatchNumber.requestFocus()
        Handler(Looper.getMainLooper()).postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(edtBatchNumber.windowToken, 0)
        },100)

        btnPrevious = findViewById<View>(R.id.btnPrevious) as AppCompatButton
        btnNext = findViewById<View>(R.id.btnNext) as AppCompatButton
        btnCreateDeliveryNote = findViewById<View>(R.id.btnCreateDeliveryNote) as AppCompatButton

        relativeEditView = findViewById<View>(R.id.relativeEditView) as RelativeLayout
        relativeCloseBottom = findViewById<View>(R.id.relativeClose) as RelativeLayout
        textSerialNumberBottom = findViewById<View>(R.id.textSerialNumber) as TextView
        edtQtyBottom = findViewById<View>(R.id.edtQty) as EditText
        btnConfirmBottom = findViewById<View>(R.id.btnConfirm) as Button

        linearChildSection = findViewById<View>(R.id.linearChildSection) as LinearLayout

        relativeEditView.setOnClickListener {  }

        edtBatchNumber.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                val strLineNo = arrPickupDetails[currentPosition].strLineNo
                val arrPickedList = localDB.getScanPickedListLineNoWise(strLineNo)

                val totalQty = arrPickupDetails[currentPosition].strQuantity
                var totalPickupQty = 0.0
                var collectQty = 0.0

                if(AppConstants.isNotEmpty(totalQty)){
                    totalPickupQty += totalQty.toDouble()
                }

                for (picked in arrPickedList){
                    val qty = picked.strQuantity
                    if(AppConstants.isNotEmpty(qty)){
                        collectQty += qty.toDouble()
                    }
                }

                AppConstants.LOGE("123123 totalPickupQty => ",totalPickupQty.toString())
                AppConstants.LOGE("123123 collectQty => ",collectQty.toString())

                if(collectQty >= totalPickupQty){
                    AppConstants.ToastMessage(this,"All quantities of this item have been added.")
                }else{
                    errorClear()
                    val strBatchNumber = edtBatchNumber.text.toString().trim()
                    val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(edtBatchNumber.windowToken, 0)
                    scanCodeAction(strBatchNumber,false,"","")
                }

                true
            } else false
        })


        imgClear.setOnClickListener {
            edtBatchNumber.setText("")
            errorClear()
        }

        btnPrevious.setOnClickListener {
            edtBatchNumber.setText("")
            errorClear()
            if(currentPosition > 0){
                currentPosition -= 1
                reloadChildData()
            }else{
                onBackPressed()
            }
        }

        btnNext.setOnClickListener {
            edtBatchNumber.setText("")
            errorClear()
            if(arrPickupDetails.size-1 == currentPosition){

                if (NetworkConnection.hasConnection(this)) {
                    prepareUploadJson()
                }else{
                    AppConstants.ToastMessage(this@ToPickupDetailsActivity,resources.getString(R.string.Please_Check_Your_Internet_Connection))
                }

            }else{
                currentPosition += 1
                reloadChildData()
            }
        }

        btnCreateDeliveryNote.setOnClickListener {
            if(isAllQtyScanned){
                if (NetworkConnection.hasConnection(this)) {
                    prepareUploadJson()
                }else{
                    AppConstants.ToastMessage(this@ToPickupDetailsActivity,resources.getString(R.string.Please_Check_Your_Internet_Connection))
                }
            }/*else{
                AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Please pick up all item batches.")
            }*/
        }


        arrPickupDetails = localDB.getPickupDetailsListSoNoWise(soNo)
        AppConstants.LOGE("arrPickupDetails12345 => ",arrPickupDetails.size.toString())
        if(arrPickupDetails.isEmpty()){
            if (NetworkConnection.hasConnection(this)) {
                getOrderDetails()
            }else{
                AppConstants.ToastMessage(this@ToPickupDetailsActivity,resources.getString(R.string.Please_Check_Your_Internet_Connection))
            }
        }else{
            // display from local...
            reloadChildData()
        }


        // scanner key enter detect related code....
        edtBatchNumber.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_TAB) {

                val text = edtBatchNumber.text.toString().trim()
                AppConstants.LOGE("text123 => ",text)

                val strLineNo = arrPickupDetails[currentPosition].strLineNo
                val arrPickedList = localDB.getScanPickedListLineNoWise(strLineNo)

                val totalQty = arrPickupDetails[currentPosition].strQuantity
                var totalPickupQty = 0.0
                var collectQty = 0.0

                if(AppConstants.isNotEmpty(totalQty)){
                    totalPickupQty += totalQty.toDouble()
                }

                for (picked in arrPickedList){
                    val qty = picked.strQuantity
                    if(AppConstants.isNotEmpty(qty)){
                        collectQty += qty.toDouble()
                    }
                }

                AppConstants.LOGE("123123 totalPickupQty => ",totalPickupQty.toString())
                AppConstants.LOGE("123123 collectQty => ",collectQty.toString())

                if(collectQty >= totalPickupQty){
                    AppConstants.ToastMessage(this,"All quantities of this item have been added.")
                }else{
                    errorClear()
                    val strBatchNumber = edtBatchNumber.text.toString().trim()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(edtBatchNumber.windowToken, 0)
                    scanCodeAction(strBatchNumber,false,"","")
                }

                return@OnKeyListener true
            }
            false
        })

    }

    private fun prepareUploadJson() {

        val arrPickedListSoNoWise = localDB.getScanPickedListSoNoWise(soNo)
        AppConstants.LOGE("arrPickedListSoNoWise => ",arrPickedListSoNoWise.size.toString())

        val jsonObject = JSONObject()
        val jsonArrayResult = JSONArray()
        for(scannedPicked in arrPickedListSoNoWise){
            val obj = JSONObject()
            obj.put("so_no", scannedPicked.strSoNo)
                .put("client", scannedPicked.strClient)
                .put("picker", scannedPicked.strPicker)
                .put("so_line_no", scannedPicked.strLineNo)
                .put("item_code", scannedPicked.strItemCode)
                .put("item_name", scannedPicked.strItemName)
                .put("picked_qty", scannedPicked.strQuantity)
                .put("picked_batch", scannedPicked.strBatchNo)
            jsonArrayResult.put(obj)
        }

        val obj2 = JSONObject()
        obj2.put("result", jsonArrayResult)
        jsonObject.put("message", obj2)

        val uploadJson = jsonObject.toString().replace("\\", "")

        AppConstants.LOGE("submit jsonObject => ",uploadJson)

        uploadPickedUpDetails(uploadJson)

    }

    private fun errorSet(steError: String){
        textErrorText.text = steError + " "
        textErrorText.visibility = View.VISIBLE
        imgClear.visibility = View.VISIBLE
        relativeBatchNo.background = getDrawable(R.drawable.bg_border_blur_5_list_error)
        imgItemStatus.background = getDrawable(R.drawable.icon_status_red)
    }

    private fun errorClear(){
        textErrorText.visibility = View.GONE
        imgClear.visibility = View.GONE
        relativeBatchNo.background = getDrawable(R.drawable.bg_border_blur_5_list_normal)
        imgItemStatus.background = getDrawable(R.drawable.icon_status_grey)
    }


    private fun checkAllProductScanned(){

        var totalQty = 0.0
        var scannedQty = 0.0

        for (pickup in arrPickupDetails){

            // get Total Quantity
            if(AppConstants.isNotEmpty(pickup.strQuantity)){
                totalQty += pickup.strQuantity.toDouble()
            }

            // get Scanned Quantity
            val strLineNo = pickup.strLineNo
            val arrPickedList = localDB.getScanPickedListLineNoWise(strLineNo)
            for (picked in arrPickedList){
                val qty = picked.strQuantity
                if(AppConstants.isNotEmpty(qty)){
                    scannedQty += qty.toDouble()
                }
            }

        }

        if(scannedQty >= totalQty){
            isAllQtyScanned = true
            linearChildHeader.setBackgroundColor(ContextCompat.getColor(this@ToPickupDetailsActivity, R.color.colorGreenHighlighter))
        }else{
            isAllQtyScanned = false
            linearChildHeader.setBackgroundColor(ContextCompat.getColor(this@ToPickupDetailsActivity, R.color.white))
        }

        if(isAllQtyScanned){
            btnCreateDeliveryNote.background = getDrawable(R.drawable.button_sky_grey_10)
            btnCreateDeliveryNote.setTextColor(resources.getColor(R.color.black))
        }else{
            btnCreateDeliveryNote.background = getDrawable(R.drawable.button_sky_grey_disable_10)
            btnCreateDeliveryNote.setTextColor(resources.getColor(R.color.colorGrey))
        }

    }

    private fun reloadChildData(){
        edtBatchNumber.setText("")
        textProductName.text = arrPickupDetails[currentPosition].strItemName
        textProductQuantity.text = arrPickupDetails[currentPosition].strQuantity
        val strBatchDetailsJson = arrPickupDetails[currentPosition].strBatchDetailsJson

        if(AppConstants.isNotEmpty(strBatchDetailsJson) && strBatchDetailsJson.toString() != "[]"){
            linearBatchCodeNotFound.visibility = View.GONE
            linearBatchEnterView.visibility = View.VISIBLE
        }else{
            linearBatchCodeNotFound.visibility = View.VISIBLE
            linearBatchEnterView.visibility = View.GONE
        }

//        val strItemCode = arrPickupDetails[currentPosition].strItemCode
//        val arrPickedList = localDB.getScanPickedListItemCodeWise(strItemCode)

        val strLineNo = arrPickupDetails[currentPosition].strLineNo
        val arrPickedList = localDB.getScanPickedListLineNoWise(strLineNo)

        if(arrPickedList.isNotEmpty()){
            pickupListAdapter = PickedListAdapter()
            pickupListAdapter.setData(this@ToPickupDetailsActivity,arrPickedList){ pickedData: beanPickedScanDetailsDB -> itemClicked(pickedData) }
            recyclerPickedItems.adapter = pickupListAdapter
            recyclerPickedItems.visibility = View.VISIBLE
        }else{
            recyclerPickedItems.visibility = View.GONE
        }

        val totalQty = arrPickupDetails[currentPosition].strQuantity
        var totalPickupQty = 0.0
        var collectQty = 0.0

        if(AppConstants.isNotEmpty(totalQty)){
            totalPickupQty += totalQty.toDouble()
        }

        for (picked in arrPickedList){
            val qty = picked.strQuantity
            if(AppConstants.isNotEmpty(qty)){
                collectQty += qty.toDouble()
            }
        }

        AppConstants.LOGE("123123 totalPickupQty => ",totalPickupQty.toString())
        AppConstants.LOGE("123123 collectQty => ",collectQty.toString())

        if(collectQty >= totalPickupQty){
            linearChildSection.setBackgroundColor(ContextCompat.getColor(this@ToPickupDetailsActivity, R.color.colorGreenHighlighter))
        }else{
            linearChildSection.setBackgroundColor(ContextCompat.getColor(this@ToPickupDetailsActivity, R.color.white))
        }

        if(currentPosition == arrPickupDetails.size-1){
            //btnNext.text = "Finish"
            btnNext.visibility = View.GONE
            btnCreateDeliveryNote.visibility = View.VISIBLE
        }else{
            //btnNext.text = "Next"
            btnNext.visibility = View.VISIBLE
            btnCreateDeliveryNote.visibility = View.GONE
        }

        if(currentPosition == 0){
            //btnPrevious.text = "Back"
            btnPrevious.visibility = View.GONE
        }else{
            //btnPrevious.text = "Previous"
            btnPrevious.visibility = View.VISIBLE
        }

        checkAllProductScanned()

    }

    private fun itemClicked(pickedData: beanPickedScanDetailsDB) {

        if(AppConstants.isDeleteBatch){

            val dialog = Dialog(this)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_comman_layout)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setWindowAnimations(R.style.DialogAnimation)
            val params: ViewGroup.LayoutParams = window.attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params as WindowManager.LayoutParams

            val textMessage = dialog.findViewById(R.id.textMessage) as TextView
            val buttonCancel = dialog.findViewById(R.id.buttonCancel) as Button
            val buttonOk = dialog.findViewById(R.id.buttonOk) as Button

            buttonOk.text = resources.getString(R.string.delete)
            textMessage.text = "Are you sure you want to delete this batch?"

            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            buttonOk.setOnClickListener {
                localDB.deleteScanPickedDetailsByBatchNoAndSoNo(pickedData.strBatchNo,pickedData.strSoNo)
                reloadChildData()
                dialog.dismiss()
            }

            dialog.show()

        }else{

            relativeEditView.visibility = View.VISIBLE

            textSerialNumberBottom.text = pickedData.strBatchNo
            edtQtyBottom.setText(pickedData.strQuantity)


            btnConfirmBottom.setOnClickListener {

                val strQty = edtQtyBottom.text.toString().trim()

                if(strQty == ""){
                    AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Please enter quantity.")
                }else if(strQty.toInt() < 1){
                    AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Please enter valid quantity.")
                }else{
                    val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(btnConfirmBottom.windowToken, 0)
                    scanCodeAction(pickedData.strBatchNo,true,strQty,pickedData.strQuantity)
                }

            }

            relativeCloseBottom.setOnClickListener {
                relativeEditView.visibility = View.GONE
            }


        }

    }


    private fun scanCodeAction(scannedCode:String, isEdit: Boolean, editQty: String,currentQty:String){

        val currentItem = arrPickupDetails[currentPosition]
        val batchesJson = currentItem.strBatchDetailsJson

        if(isEdit){

            val strLineNo = arrPickupDetails[currentPosition].strLineNo
            val arrPickedList = localDB.getScanPickedListLineNoWise(strLineNo)

            val totalQty = arrPickupDetails[currentPosition].strQuantity
            var totalPickupQty = 0.0
            var collectQty = 0.0

            if(AppConstants.isNotEmpty(totalQty)){
                totalPickupQty += totalQty.toDouble()
            }

            for (picked in arrPickedList){
                val qty = picked.strQuantity
                if(AppConstants.isNotEmpty(qty)){
                    collectQty += qty.toDouble()
                }
            }

            var newQty = (collectQty - currentQty.toDouble()) + editQty.toDouble()

            AppConstants.LOGE("123123 totalPickupQty => ",totalPickupQty.toString())
            AppConstants.LOGE("123123 newQty => ",newQty.toString())
            //AppConstants.LOGE("123123 collectQty => ",collectQty.toString())

            if(totalPickupQty < newQty){
                AppConstants.ToastMessage(this,"All quantities of this item have been added!")
            }else{
                val arrBatchJson = JSONArray(batchesJson.toString())

                var isDateExpire = false
                var expireDate = ""
                var isDateAlert = false
                var batchMaxQuantity = 0.0

                for (i in 0 until arrBatchJson.length()) {
                    val batchId = arrBatchJson.getJSONObject(i).getString("batch_id")
                    val batchQty = arrBatchJson.getJSONObject(i).getString("batch_qty")
                    val daysToExpire = arrBatchJson.getJSONObject(i).getString("days_to_expire") // for testing "30/11/2022"
                    val alertBeforeDays = arrBatchJson.getJSONObject(i).getString("alert_before_days") // for testing "27/11/2022"

                    if(batchId.equals(scannedCode,ignoreCase = true)){

                        expireDate = daysToExpire
                        if(AppConstants.isNotEmpty(daysToExpire)){
                            isDateExpire = AppConstants.isDateExpire(daysToExpire)
                        }
                        if(AppConstants.isNotEmpty(alertBeforeDays)){
                            isDateAlert = AppConstants.isDateExpire(alertBeforeDays)
                        }

                        if(AppConstants.isNotEmpty(batchQty)){
                            batchMaxQuantity = batchQty.toDouble()
                        }

                    }

                }

                if(!isDateExpire){

                    if(batchMaxQuantity >= editQty.toDouble()){

                        if(isDateAlert){
                            displayAlertDate("This batch will expire on $expireDate")
                        }

                        var qty = 1
                        if(AppConstants.isNotEmpty(editQty)){
                            qty = editQty.toInt()
                        }
                        localDB.updateScanPickedByBatchNo(qty.toString(),scannedCode,soNo)
                        AppConstants.ToastMessage(this@ToPickupDetailsActivity, "Batch updated successfully!")
                        relativeCloseBottom.performClick() // for close bottom view
                        reloadChildData()
                    }else{
                        AppConstants.ToastMessage(this@ToPickupDetailsActivity, "You have added the maximum batch!")
                    }

                }else{
                    AppConstants.ToastMessage(this@ToPickupDetailsActivity, "Batch Expired!")
                }

            }

        }else{

            if(AppConstants.isNotEmpty(batchesJson)){

                val arrBatchJson = JSONArray(batchesJson.toString())

                if(arrBatchJson.length() > 0) {

                    var isDateExpire = false
                    var expireDate = ""
                    var isDateAlert = false
                    var isBatchCodeAvailable = false
                    var batchMaxQuantity = 0.0

                    for (i in 0 until arrBatchJson.length()) {
                        val batchId = arrBatchJson.getJSONObject(i).getString("batch_id")
                        val batchQty = arrBatchJson.getJSONObject(i).getString("batch_qty")
                        val daysToExpire = arrBatchJson.getJSONObject(i).getString("days_to_expire") // for testing "30/11/2022"
                        val alertBeforeDays = arrBatchJson.getJSONObject(i).getString("alert_before_days") // for testing "27/11/2022"

                        if(batchId.equals(scannedCode,ignoreCase = true)){
                            expireDate = daysToExpire
                            if(AppConstants.isNotEmpty(daysToExpire)){
                                isDateExpire = AppConstants.isDateExpire(daysToExpire)
                            }
                            if(AppConstants.isNotEmpty(alertBeforeDays)){
                                isDateAlert = AppConstants.isDateExpire(alertBeforeDays)
                            }

                            if(batchId.equals(scannedCode,ignoreCase = true)){
                                isBatchCodeAvailable = true
                                batchMaxQuantity = batchQty.toDouble()
                            }
                        }

                    }

                    if(isBatchCodeAvailable && batchMaxQuantity > 0 && !isDateExpire){

                        if(isDateAlert){
                            displayAlertDate("This batch code expire at $expireDate")
                        }

                        if(localDB.isExistsScanPickedBatchNoDetails(scannedCode,soNo)){

                            val pickedRecord = localDB.getSinglePickedRecordByBatchNo(scannedCode,soNo)

                            val strQuantity = pickedRecord.strQuantity

                            if(batchMaxQuantity > strQuantity.toDouble()){

                                var qty = 1
                                if(AppConstants.isNotEmpty(strQuantity)){
                                    qty = strQuantity.toInt() + 1
                                }
                                localDB.updateScanPickedByBatchNo(qty.toString(),scannedCode,soNo)
                                errorClear()
                                AppConstants.ToastMessage(this@ToPickupDetailsActivity, "Batch updated successfully!")

                            }else{
                                errorSet("You have added the maximum batch!")
                            }

                        }else{

                            val strSoNo = currentItem.strSoNo
                            val strClient = currentItem.strClient
                            val strPicker = currentItem.strPicker
                            val strLineNo = currentItem.strLineNo
                            val strItemCode = currentItem.strItemCode
                            val strItemName = currentItem.strItemName

                            val strQuantity = "1"
                            val strBatchNo = scannedCode

                            localDB.addScanPickedDetails(strSoNo,strClient,strPicker,strLineNo,strItemCode,strItemName,strQuantity,strBatchNo)
                            errorClear()
                            AppConstants.ToastMessage(this@ToPickupDetailsActivity, "Batch added successfully!")

                        }
                        reloadChildData()

                    }else{
                        if(isDateExpire){
                            errorSet("Batch Expired!")
                        }else if(isBatchCodeAvailable){
                            errorSet("Batch not available!")
                        }else{
                            errorSet("Invalid Batch!")
                        }

                    }

                }else{
                    errorSet("Batch not available!")
                }


            }else{
                errorSet("Batch not available!")
            }

        }

    }

    private fun displayAlertDate(message: String) {

        val dialog = Dialog(this@ToPickupDetailsActivity)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_comman_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setWindowAnimations(R.style.DialogAnimation)
        val params: ViewGroup.LayoutParams = window.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        window.attributes = params as WindowManager.LayoutParams

        val textMessage = dialog.findViewById(R.id.textMessage) as TextView
        val buttonCancel = dialog.findViewById(R.id.buttonCancel) as Button
        val buttonOk = dialog.findViewById(R.id.buttonOk) as Button
        buttonCancel.visibility = View.GONE

        buttonOk.text = resources.getString(R.string.ok)
        textMessage.text = message

        buttonOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getOrderDetails() {

        relativeProgressView.visibility = View.VISIBLE

        val url = "api/method/xtc_mobile_api.get_order_details?so_no=${soNo}&picker=${strLoginUserEmail}"

        AppConstants.LOGE("URL getOrderDetails :-", url)

        val call: Call<ResponseBody> = API.getAPI().commonGetApiCall(url)

        call.enqueue(object : retrofit2.Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                relativeProgressView.visibility = View.GONE

                if(response.isSuccessful){
                    try {

                        val jsonObject = JSONObject(response.body()!!.string())
                        AppConstants.LOGE("response getOrderDetails => ",jsonObject.toString())

                        val jsonObjectMessage = jsonObject.getJSONObject("message")
                        val resultArray = jsonObjectMessage.getJSONArray("result")

                        AppConstants.LOGE("resultArray123 => " , resultArray.length().toString())

                        if(resultArray.length() > 0){

                            for (i in 0 until resultArray.length()) {
                                val soNo = resultArray.getJSONObject(i).getString("so_no")
                                val client = resultArray.getJSONObject(i).getString("client")
                                val picker = resultArray.getJSONObject(i).getString("picker")
                                val lineNo = resultArray.getJSONObject(i).getString("so_line_no")
                                val itemCode = resultArray.getJSONObject(i).getString("item_code")
                                val itemName = resultArray.getJSONObject(i).getString("item_name")
                                val qty = resultArray.getJSONObject(i).getString("qty")
                                val batchDetailsJSON = resultArray.getJSONObject(i).getString("batch_details")

                                val jsonArrayBatchDetails = JSONArray()
                                if(AppConstants.isNotEmpty(batchDetailsJSON) && !batchDetailsJSON.equals("[]")){
                                    val jsonArrBatchDetails = JSONArray(batchDetailsJSON)

                                    for (j in 0 until jsonArrBatchDetails.length()) {

                                        val itemCode1 = jsonArrBatchDetails.getJSONObject(j).getString("item_code")
                                        val batchId1 = jsonArrBatchDetails.getJSONObject(j).getString("batch_id")
                                        val batchQty1 = jsonArrBatchDetails.getJSONObject(j).getString("batch_qty")
                                        val daysToExpire1 = jsonArrBatchDetails.getJSONObject(j).getString("days_to_expire")
                                        val alertBeforeDays1 = jsonArrBatchDetails.getJSONObject(j).getString("alert_before_days")

                                        var dayToExpire = ""
                                        var alertBeforeDays = ""

                                        if(AppConstants.isNotEmpty(daysToExpire1)){
                                            dayToExpire = AppConstants.addDaysInCurrentDate(daysToExpire1.toInt())
                                            if(AppConstants.isNotEmpty(alertBeforeDays1)){
                                                val afterBeforeDay = daysToExpire1.toInt() - alertBeforeDays1.toInt()
                                                alertBeforeDays = AppConstants.addDaysInCurrentDate(afterBeforeDay)
                                            }
                                        }

                                        val obj = JSONObject()
                                        obj.put("item_code", itemCode1)
                                            .put("batch_id", batchId1)
                                            .put("batch_qty", batchQty1)
                                            .put("days_to_expire", dayToExpire) //02/12/2022
                                            .put("alert_before_days", alertBeforeDays) // 27/11/2022
                                        jsonArrayBatchDetails.put(obj)

                                    }
                                }

                                val batchDetailsJSONNew = jsonArrayBatchDetails.toString()

                                AppConstants.LOGE("batchDetailsJSONNew => ",batchDetailsJSONNew)

                                if(localDB.isExistsPickupItemCodeDetails(itemCode)){
                                    localDB.deletePickupDetailsByItemCode(itemCode.toString(),soNo)
                                }
                                localDB.addPickupDetails(soNo,client,picker,lineNo,itemCode,itemName,qty,batchDetailsJSONNew.toString())

                            }

                            arrPickupDetails = localDB.getPickupDetailsListSoNoWise(soNo)

                            if(arrPickupDetails.isNotEmpty()){
                                if(resultArray.length() == arrPickupDetails.size){
                                    AppConstants.LOGE("arrPickupDetails123 => ",arrPickupDetails.size.toString())
                                    reloadChildData()
                                }else{
                                    getOrderDetails()
                                }
                            }

                        }

                    }catch (e: Exception) {
                        AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Something went wrong, please try again.")
                        AppConstants.LOGE("getOrderDetails error message => " , e.toString())
                    }
                }else{

                    val errorBody = response.errorBody()
                    val jObjError = JSONObject(errorBody!!.string())
                    AppConstants.LOGE("getOrderDetails error message => " , jObjError.toString())

                    if(jObjError.has("message")){
                        val strMessage = jObjError.getString("message")
                        AppConstants.ToastMessage(this@ToPickupDetailsActivity,strMessage)
                    }else{
                        AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Something went wrong, please try again.")
                    }

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                relativeProgressView.visibility = View.GONE
                AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Something went wrong, please try again.")
                AppConstants.LOGE("getOrderDetails error message => " , t.message.toString())
            }

        })

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            val TAG = "permission => "
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for location permission.
            // Check if the only required permission has been granted

            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // CAMERA permission has been granted, preview can be displayed
                AppConstants.LOGE(TAG, "CAMERA permission has now been granted,For Scan Barcode")

                startActivity(Intent(this, BarcodeScannerActivity::class.java))
                overridePendingTransition(R.anim.enter, R.anim.exit)

            } else {

                Toast.makeText(this, "Please Grant CAMERA Permission For Scan Barcode.", Toast.LENGTH_LONG).show()
                AppConstants.LOGE(TAG, "CAMERA permission was NOT granted")

            } // END_INCLUDE(permission_result)
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }


    fun uploadPickedUpDetails(uploadJson: String) {

        relativeProgressView.visibility = View.VISIBLE

        val jsonToMap = jsonToMap(JSONObject(uploadJson.toString()))

        val call: Call<ResponseBody> = API.getAPI().uploadPickupDetails(AppConstants.cookieSessionId,jsonToMap)

        call.enqueue(object : retrofit2.Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if(response.isSuccessful){
                    try {

                        relativeProgressView.visibility = View.GONE

                        val jsonObject = JSONObject(response.body()!!.string())

                        AppConstants.LOGE("response uploadPickedUpDetails => ",jsonObject.toString())

                        val jsonOBJMessage = jsonObject.getJSONObject("message")
                        val jsonOBJResult = jsonOBJMessage.getJSONObject("result")
                        val deliveryNote = jsonOBJResult.getString("delivery_note")
                        val delivery_PDF_URL = jsonOBJResult.getString("delivery_pdf_url")

                        AppConstants.LOGE("response uploadPickedUpDetails deliveryNote => ",deliveryNote.toString())
                        AppConstants.LOGE("response uploadPickedUpDetails delivery_PDF_URL => ",delivery_PDF_URL.toString())

                        val intent = Intent(this@ToPickupDetailsActivity, PickupScreenActivity::class.java)
                        intent.putExtra("delivery_note",deliveryNote)
                        intent.putExtra("delivery_pdf_url",delivery_PDF_URL)
                        intent.putExtra("client_name", clientName)
                        intent.putExtra("so_no", soNo)
                        startActivity(intent)
                        overridePendingTransition(R.anim.enter, R.anim.exit)
                        finish()

                    }catch (e: Exception) {
                        relativeProgressView.visibility = View.GONE
                        AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Something went wrong, please try again.")
                        AppConstants.LOGE("uploadPickedUpDetails error message => " , e.toString())
                    }

                }else{

                    relativeProgressView.visibility = View.GONE
                    val errorBody = response.errorBody()
                    val jObjError = JSONObject(errorBody!!.string())
                    AppConstants.LOGE("uploadPickedUpDetails error message => " , jObjError.toString())

                    if(jObjError.has("message")){
                        val strMessage = jObjError.getString("message")
                        AppConstants.ToastMessage(this@ToPickupDetailsActivity,strMessage)
                    }else{
                        AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Something went wrong, please try again.")
                    }

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                relativeProgressView.visibility = View.GONE
                AppConstants.ToastMessage(this@ToPickupDetailsActivity,"Something went wrong, please try again.")
                AppConstants.LOGE("uploadPickedUpDetails error message => " , t.message.toString())

            }

        })

    }


    @Throws(JSONException::class)
    fun jsonToMap(json: JSONObject): Map<String, Any> {
        var retMap: Map<String, Any> = HashMap()
        if (json !== JSONObject.NULL) {
            retMap = toMap(json)
        }
        return retMap
    }

    @Throws(JSONException::class)
    fun toMap(`object`: JSONObject): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        val keysItr = `object`.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            var value = `object`[key]
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            map[key] = value
        }
        return map
    }

    @Throws(JSONException::class)
    fun toList(array: JSONArray): List<Any> {
        val list: MutableList<Any> = ArrayList()
        for (i in 0 until array.length()) {
            var value = array[i]
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            list.add(value)
        }
        return list
    }


}