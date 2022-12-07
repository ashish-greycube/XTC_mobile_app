package com.xtc_gelato.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtc_gelato.R
import com.xtc_gelato.adapters.ToPickupListAdapter
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.models.beanLiveStock
import com.xtc_gelato.server_api_calls.API
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

/*
* This is ToPickupListActivity class is use for display available Pickup batches listing.
* */
class ToPickupListActivity : AppCompatActivity() {

    private lateinit var imgHeaderIcon: ImageView
    private lateinit var textCategoryName: TextView
    private lateinit var textErrorMessage: TextView
    private lateinit var imgError: ImageView
    private lateinit var toPickupListAdapter: ToPickupListAdapter
    private lateinit var relativeFilter: RelativeLayout

    private lateinit var relativeSearch: RelativeLayout
    private lateinit var imgSearch: ImageView

    private lateinit var edtSearch: EditText

    private lateinit var relativeErrorView: RelativeLayout
    private lateinit var relativeProgressView: RelativeLayout

    private lateinit var recyclerLiveStock: RecyclerView

    private lateinit var arrToPickupListMain: java.util.ArrayList<beanLiveStock>
    private lateinit var arrToPickupListTemp: java.util.ArrayList<beanLiveStock>

    private lateinit var textHeaderText: TextView
    private lateinit var relativeLeft: RelativeLayout
    private lateinit var relativeRight: RelativeLayout

    var isSearch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_pickup_list)

        AppConstants.statusbarColorWhiteChange(this)

        relativeLeft = findViewById<View>(R.id.relativeLeft) as RelativeLayout
        relativeRight = findViewById<View>(R.id.relativeRight) as RelativeLayout
        val linearHeaderTitle = findViewById<View>(R.id.linearHeaderTitle) as LinearLayout
        relativeRight.visibility = View.VISIBLE

        textHeaderText = findViewById<View>(R.id.textHeaderText) as TextView
        imgHeaderIcon = findViewById<View>(R.id.imgHeaderIcon) as ImageView
        imgHeaderIcon.background = getDrawable(R.drawable.icon_menu_active_stock)


        textHeaderText.text = "to pick"

        linearHeaderTitle.setOnClickListener { relativeLeft.performClick() }

        relativeLeft.setOnClickListener {
            onBackPressed()
        }

        relativeRight.setOnClickListener {
            if (NetworkConnection.hasConnection(this)) {
                getLiveStockInfo()
            }else{
                AppConstants.CheckConnection(this)
            }
        }

        val relativeGoToHome = findViewById<View>(R.id.relativeGoToHome) as RelativeLayout

        relativeGoToHome.setOnClickListener {
            AppConstants.goToHomeScreen(this)
        }

        relativeErrorView = findViewById<View>(R.id.relativeErrorView) as RelativeLayout
        imgError = findViewById<View>(R.id.imgError) as ImageView
        textErrorMessage = findViewById<View>(R.id.textErrorMessage) as TextView
        textErrorMessage.text = "Pick up item not found :("
        imgError.background = getDrawable(R.drawable.icon_menu_active_stock)

        relativeProgressView = findViewById<View>(R.id.relativeProgressView) as RelativeLayout
        textCategoryName = findViewById<View>(R.id.textCategoryName) as TextView


        recyclerLiveStock = findViewById<View>(R.id.recyclerLiveStock) as RecyclerView
        recyclerLiveStock.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerLiveStock.setHasFixedSize(true)
        recyclerLiveStock.isNestedScrollingEnabled = false


        edtSearch = findViewById<View>(R.id.edtSearch) as EditText

        relativeFilter = findViewById<View>(R.id.relativeFilter) as RelativeLayout

        relativeSearch = findViewById<View>(R.id.relativeSearch) as RelativeLayout
        imgSearch = findViewById<View>(R.id.imgSearch) as ImageView

        relativeFilter.setOnClickListener {

            AppConstants.ToastMessage(this,"Coming soon")

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(relativeFilter.windowToken, 0)
//
//            val popupMenu: PopupMenu = PopupMenu(this,relativeFilter)
//            popupMenu.menuInflater.inflate(R.menu.filter_poup_menu,popupMenu.menu)
//            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
//
//                when(item.itemId) {
//
//                    R.id.date_range -> filterBy("date_range")
//
//                    R.id.last_week -> filterBy("last_week")
//                    R.id.last_month -> filterBy("last_month")
//                    R.id.last_three_month -> filterBy("last_three_month")
//                    R.id.a_year -> filterBy("a_year")
//
//                    R.id.category_wise -> filterBy("category_wise")
//
//                }
//                true
//            })
//            popupMenu.show()

        }

        relativeSearch.setOnClickListener {
            if(isSearch){
                resetSearch()
            }
        }

        arrToPickupListMain = ArrayList<beanLiveStock>()
        arrToPickupListTemp = ArrayList<beanLiveStock>()


        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val strSearchText = edtSearch.text.trim().toString().lowercase()

                if(arrToPickupListTemp.size > 0){
                    if(strSearchText.trim().isNotEmpty()){

                        AppConstants.LOGE("search_text => ",strSearchText)

                        isSearch = true
                        imgSearch.background = getDrawable(R.drawable.icon_cancel)

                        val arrToPickupListTempSearch = ArrayList<beanLiveStock>()

                        for(item in arrToPickupListTemp){
                            if(item.client.lowercase().contains(strSearchText) ||
                                item.soNo.lowercase().contains(strSearchText)){
                                arrToPickupListTempSearch.add(item)
                            }
                        }
                        toPickupListAdapter.setData(this@ToPickupListActivity,arrToPickupListTempSearch)

                    }else{

                        isSearch = false
                        imgSearch.background = getDrawable(R.drawable.icon_search)
                        toPickupListAdapter.setData(this@ToPickupListActivity,arrToPickupListTemp)

                    }

                }


            }
        })


        if (NetworkConnection.hasConnection(this)) {
            getLiveStockInfo()
        }else{
            AppConstants.CheckConnection(this)
        }

    }

    override fun onResume() {
        super.onResume()
        if(AppConstants.isReloadToPickupList){
            AppConstants.isReloadToPickupList = false
            if (NetworkConnection.hasConnection(this)) {
                getLiveStockInfo()
            }else{
                AppConstants.CheckConnection(this)
            }
        }
    }

    private fun resetSearch(){
        edtSearch.setText("")
        isSearch = false
        imgSearch.background = getDrawable(R.drawable.icon_search)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(relativeSearch.windowToken, 0)
    }


    private fun getLiveStockInfo() {

        relativeProgressView.visibility = View.VISIBLE

        val URL = "api/method/xtc_mobile_api.get_order_list"

        AppConstants.LOGE("URL getLiveStockInfo :-", URL)

        val call: Call<ResponseBody> = API.getAPI().commonGetApiCall(URL)

        call.enqueue(object : retrofit2.Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                relativeProgressView.visibility = View.GONE

                if(response.isSuccessful){
                    try {

                        val jsonObject = JSONObject(response.body()!!.string())
                        AppConstants.LOGE("response getLiveStockInfo => ",jsonObject.toString())

                        val jsonObjectMessage = jsonObject.getJSONObject("message")
                        val resultArray = jsonObjectMessage.getJSONArray("result")

                        AppConstants.LOGE("resultArray => " , resultArray.length().toString())

                        arrToPickupListTemp.clear()
                        arrToPickupListMain.clear()

                        for (i in 0 until resultArray.length()) {
                            val soNo = resultArray.getJSONObject(i).getString("so_no")
                            val client = resultArray.getJSONObject(i).getString("client")
                            val deliveryDate = resultArray.getJSONObject(i).getString("delivery_date")
                            val picker = resultArray.getJSONObject(i).getString("picker")
                            val printLabelUrl = resultArray.getJSONObject(i).getString("print_label_url")
                            val pickerInstruction = resultArray.getJSONObject(i).getString("picker_instruction")

                            arrToPickupListMain.add(beanLiveStock(soNo,client,deliveryDate,picker,printLabelUrl,pickerInstruction))
                            arrToPickupListTemp.add(beanLiveStock(soNo,client,deliveryDate,picker,printLabelUrl,pickerInstruction))
                        }

                        if(arrToPickupListTemp.size > 0){

                            recyclerLiveStock.visibility = View.VISIBLE
                            relativeErrorView.visibility = View.GONE

                            toPickupListAdapter = ToPickupListAdapter()
                            toPickupListAdapter.setData(this@ToPickupListActivity,arrToPickupListTemp)
                            recyclerLiveStock.adapter = toPickupListAdapter
                            //AppConstants.runLayoutAnimation(recyclerLiveStock)

                        }else{
                            recyclerLiveStock.visibility = View.GONE
                            relativeErrorView.visibility = View.VISIBLE
                        }

                    }catch (e: Exception) {
                        relativeErrorView.visibility = View.VISIBLE
                        AppConstants.ToastMessage(this@ToPickupListActivity,"Something went wrong, please try again.")
                        AppConstants.LOGE("getLiveStockInfo error message => " , e.toString())
                    }
                }else{
                    relativeErrorView.visibility = View.VISIBLE
                    val errorBody = response.errorBody()
                    val jObjError = JSONObject(errorBody!!.string())
                    AppConstants.LOGE("getLiveStockInfo error message => " , jObjError.toString())

                    if(jObjError.has("message")){
                        val strMessage = jObjError.getString("message")
                        AppConstants.ToastMessage(this@ToPickupListActivity,strMessage)
                    }else{
                        AppConstants.ToastMessage(this@ToPickupListActivity,"Something went wrong, please try again.")
                    }

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                relativeProgressView.visibility = View.GONE
                relativeErrorView.visibility = View.VISIBLE
                AppConstants.ToastMessage(this@ToPickupListActivity,"Something went wrong, please try again.")
                AppConstants.LOGE("getLiveStockInfo error message => " , t.message.toString())

            }

        })

    }

}