package com.xtc_gelato.constent_classes

import android.app.Activity
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.xtc_gelato.R
import com.xtc_gelato.activities.DashboardActivity
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

object AppConstants {

   // var MainURL = "https://xtc.greycube.in/"
    var MainURL = ""


    var cookieSessionId = ""
    var scannedBarCode = ""

    var isDeleteBatch = false

    var isReloadToPickupList = false

    var LogsDisplay = true //if display logs true.

    fun LOGE(tag: String,message: String) {
        if (LogsDisplay) {
            Log.e(tag, "" + message)
        }
    }

    fun ToastMessage(activity: Activity,message: String?){
        if(message != null && message != ""){
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }


    //-------/ Date Formate Review /-------------------------------------------------------------

    fun dateFormatChangesDDMMYYYY(strDate: String?): String {
        var finalDate = ""
        try {
            var spf = SimpleDateFormat("yyyy-MM-dd")
            val newDate = spf.parse(strDate)
            spf = SimpleDateFormat("dd-MM-yyyy")
            finalDate = spf.format(newDate)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            LOGE("error JSON:- ", e.localizedMessage)
        }
        return finalDate
    }

    fun addDaysInCurrentDate(addDays: Int): String{

        var dateInString = ""
        try {
            val calendar = GregorianCalendar()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_MONTH, addDays)
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val resultDate = Date(calendar.getTimeInMillis())
            dateInString = sdf.format(resultDate)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            LOGE("error JSON:- ", e.localizedMessage)
        }
        return dateInString
    }

    fun isDateExpire(date: String): Boolean{
        return SimpleDateFormat("dd/MM/yyyy").parse(date).before(Date())
    }


    /**
    * //-------/ No Internet Connection Error Alert /-----------------------------------------------------------------------
    */
    fun CheckConnection(activity: Activity) {

        val dialog = Dialog(activity)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_comman_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setWindowAnimations(R.style.DialogAnimation)
        val params: ViewGroup.LayoutParams = activity.getWindow().getAttributes()
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        activity.window.attributes = params as WindowManager.LayoutParams

        val textMessage = dialog.findViewById(R.id.textMessage) as TextView
        val buttonCancel = dialog.findViewById(R.id.buttonCancel) as Button
        val buttonOk = dialog.findViewById(R.id.buttonOk) as Button

        textMessage.text = activity.resources.getString(R.string.Please_Check_Your_Internet_Connection)

        buttonCancel.visibility = View.GONE

        buttonOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    /**
     *  String is not null and not blank("")
     */
    fun isNotEmpty(string: String?): Boolean {
        return string != null && string.isNotEmpty() && !string.equals("null", ignoreCase = true)
    }

    /**
     *  Status bar color changes
     */
    fun statusbarColorChange(activity: Activity) {

        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(activity, R.color.colorBlue)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.getWindowInsetsController()!!.setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS)
        }

    }

    fun goToHomeScreen(activity: Activity) {

        val i = Intent(activity, DashboardActivity::class.java)
        // set the new task and clear flags
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(i)

    }

    fun statusbarColorWhiteChange(activity: Activity) {

        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        window.decorView.setSystemUiVisibility(window.decorView.getSystemUiVisibility() or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.getWindowInsetsController()!!.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }

    }

    fun errorRemoveOnTextChange(outlinedTextField: TextInputLayout) {

        outlinedTextField.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    outlinedTextField.isErrorEnabled = false
                }

            }
            override fun afterTextChanged(s: Editable) {}
        })

    }


}