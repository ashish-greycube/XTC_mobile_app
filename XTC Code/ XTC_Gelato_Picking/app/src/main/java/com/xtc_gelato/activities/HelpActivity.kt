package com.xtc_gelato.activities

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.xtc_gelato.R
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.server_api_calls.API
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

/*
* This Help class display help section here...
* */

class HelpActivity : AppCompatActivity() {

    private lateinit var relativeProgressView: RelativeLayout
    private lateinit var textDetails: TextView

    private lateinit var textHeaderText: TextView
    private lateinit var relativeLeft: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        AppConstants.statusbarColorWhiteChange(this)

        relativeLeft = findViewById<View>(R.id.relativeLeft) as RelativeLayout
        textHeaderText = findViewById<View>(R.id.textHeaderText) as TextView
        val linearHeaderTitle = findViewById<View>(R.id.linearHeaderTitle) as LinearLayout

        textHeaderText.text = "Help"

        linearHeaderTitle.setOnClickListener {
            relativeLeft.performClick()
        }
        relativeLeft.setOnClickListener {
            onBackPressed()
        }

        relativeProgressView = findViewById<View>(R.id.relativeProgressView) as RelativeLayout
        textDetails = findViewById<View>(R.id.textDetails) as TextView


        if (NetworkConnection.hasConnection(this)) {
            getHelpData()
        }else{
            AppConstants.CheckConnection(this)
        }


    }

    private fun getHelpData() {

        relativeProgressView.visibility = View.VISIBLE

        val URL = "api/method/frappe.client.get_single_value?doctype=XTC Settings&field=mobile_app_help_content"

        AppConstants.LOGE("URL getHelpData :-", URL)

        val call: Call<ResponseBody> = API.getAPI().getHelp(URL,AppConstants.cookieSessionId)

        call.enqueue(object : retrofit2.Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                relativeProgressView.visibility = View.GONE

                if(response.isSuccessful){
                    try {

                        val jsonObject = JSONObject(response.body()!!.string())
                        AppConstants.LOGE("response getHelpData => ",jsonObject.toString())

                        val message = jsonObject.getString("message")

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            textDetails.text = Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT)
                        } else {
                            textDetails.text = Html.fromHtml(message)
                        }

                    }catch (e: Exception) {
                        AppConstants.ToastMessage(this@HelpActivity,"Something went wrong, please try again.")
                        AppConstants.LOGE("getHelpData error message => " , e.toString())
                    }
                }else{
                    val errorBody = response.errorBody()
                    val jObjError = JSONObject(errorBody!!.string())
                    AppConstants.LOGE("getHelpData error message => " , jObjError.toString())

                    if(jObjError.has("message")){
                        val strMessage = jObjError.getString("message")
                        AppConstants.ToastMessage(this@HelpActivity,strMessage)
                    }else{
                        AppConstants.ToastMessage(this@HelpActivity,"Something went wrong, please try again.")
                    }

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                relativeProgressView.visibility = View.GONE
                AppConstants.ToastMessage(this@HelpActivity,"Something went wrong, please try again.")
                AppConstants.LOGE("getHelpData error message => " , t.message.toString())

            }

        })

    }

}