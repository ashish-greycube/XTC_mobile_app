package com.xtc_gelato.activities

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xtc_gelato.R
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.server_api_calls.API
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

/*
* This Dashboard class display Total Clients and Total Pieces , Help and Logout functionality
* */
class DashboardActivity : AppCompatActivity() {

    private lateinit var textClientsPieces: TextView
    private lateinit var relativeProgressView: RelativeLayout
    private lateinit var linearLogout: LinearLayout
    private lateinit var linearHelp: LinearLayout
    private lateinit var linearLiveStock: LinearLayout

    private lateinit var layoutSwipeRefresh: SwipeRefreshLayout

    var isSwipeReload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        AppConstants.statusbarColorChange(this)

        layoutSwipeRefresh = findViewById<View>(R.id.layoutSwipeRefresh) as SwipeRefreshLayout

        layoutSwipeRefresh.setOnRefreshListener {

            if (NetworkConnection.hasConnection(this)) {
                isSwipeReload = true
                Handler(Looper.getMainLooper()).postDelayed({
                    // swipe refresh disable when api response get...
                    getOrderSummary()
                },250)
            }else{
                layoutSwipeRefresh.isRefreshing = false
                AppConstants.ToastMessage(this@DashboardActivity,resources.getString(R.string.Please_Check_Your_Internet_Connection))
            }


        }

        textClientsPieces = findViewById<View>(R.id.textClientsPieces) as TextView
        relativeProgressView = findViewById<View>(R.id.relativeProgressView) as RelativeLayout
        linearLiveStock = findViewById<View>(R.id.linearLiveStock) as LinearLayout
        linearHelp = findViewById<View>(R.id.linearHelp) as LinearLayout
        linearLogout = findViewById<View>(R.id.linearLogout) as LinearLayout

        linearLiveStock.setOnClickListener {
            val intent = Intent(this, ToPickupListActivity::class.java)
            intent.putExtra("category","all")
            startActivity(intent)
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }

        linearHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }

        linearLogout.setOnClickListener {

            val dialog = Dialog(this)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_comman_layout)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setWindowAnimations(R.style.DialogAnimation)
            val params: ViewGroup.LayoutParams = getWindow().getAttributes()
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params as WindowManager.LayoutParams

            val textTitle = dialog.findViewById(R.id.textTitle) as TextView
            val textMessage = dialog.findViewById(R.id.textMessage) as TextView
            val buttonCancel = dialog.findViewById(R.id.buttonCancel) as Button
            val buttonOk = dialog.findViewById(R.id.buttonOk) as Button

            textTitle.text = resources.getText(R.string.app_name)
            textMessage.text = resources.getText(R.string.logout_message)

            buttonOk.text = resources.getText(R.string.logout)

            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            buttonOk.setOnClickListener {
                if (NetworkConnection.hasConnection(this)) {
                    dialog.dismiss()

                    val editor: SharedPreferences.Editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    editor.putString("login_username","")
                    editor.putString("login_password","")
                    editor.putBoolean("is_login",false)

                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(R.anim.enter, R.anim.exit)
                    finish()

                }else{
                    AppConstants.ToastMessage(this@DashboardActivity,resources.getString(R.string.Please_Check_Your_Internet_Connection))
                }
            }

            dialog.show()

        }

        if (NetworkConnection.hasConnection(this)) {
            getOrderSummary()
        }else{
            AppConstants.ToastMessage(this@DashboardActivity,resources.getString(R.string.Please_Check_Your_Internet_Connection))
        }


    }

    // api/method/xtc_mobile_api.get_order_summary


    private fun getOrderSummary() {

        if(!isSwipeReload){
            relativeProgressView.visibility = View.VISIBLE
        }

        val URL = "api/method/xtc_mobile_api.get_order_summary"

        AppConstants.LOGE("URL getOrderSummary :-", URL)

        val call: Call<ResponseBody> = API.getAPI().commonGetApiCall(URL)

        call.enqueue(object : retrofit2.Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if(isSwipeReload){
                    layoutSwipeRefresh.isRefreshing = false
                }else{
                    relativeProgressView.visibility = View.GONE
                }

                if(response.isSuccessful){
                    try {

                        val jsonObject = JSONObject(response.body()!!.string())
                        AppConstants.LOGE("response getOrderSummary => ",jsonObject.toString())

                        val jsonObjectMessage = jsonObject.getJSONObject("message")
                        val resultArray = jsonObjectMessage.getJSONArray("result")

                        if(resultArray.length() > 0){
                            val clients = resultArray.getJSONObject(0).getString("clients")
                            val pieces = resultArray.getJSONObject(0).getString("pieces")

                            textClientsPieces.text = "$clients clients, $pieces pieces"
                        }



                    }catch (e: Exception) {
                        AppConstants.ToastMessage(this@DashboardActivity,"Something went wrong, please try again.")
                        AppConstants.LOGE("getOrderSummary error message => " , e.toString())
                    }
                }else{
                    val errorBody = response.errorBody()
                    val jObjError = JSONObject(errorBody!!.string())
                    AppConstants.LOGE("getOrderSummary error message => " , jObjError.toString())

                    if(jObjError.has("message")){
                        val strMessage = jObjError.getString("message")
                        AppConstants.ToastMessage(this@DashboardActivity,strMessage)
                    }else{
                        AppConstants.ToastMessage(this@DashboardActivity,"Something went wrong, please try again.")
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if(isSwipeReload){
                    layoutSwipeRefresh.isRefreshing = false
                }else{
                    relativeProgressView.visibility = View.GONE
                }
                AppConstants.ToastMessage(this@DashboardActivity,"Something went wrong, please try again.")
                AppConstants.LOGE("getOrderSummary error message => " , t.message.toString())

            }

        })

    }

}