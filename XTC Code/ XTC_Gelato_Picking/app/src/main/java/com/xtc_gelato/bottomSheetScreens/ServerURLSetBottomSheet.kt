package com.xtc_gelato.bottomSheetScreens

import android.annotation.SuppressLint
import android.app.Dialog
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.xtc_gelato.R
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.constent_classes.AppPreference
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class ServerURLSetBottomSheet : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view: View = LayoutInflater.from(context).inflate(R.layout.layout_server_url_set_bottom_sheet, null)

        val relativeClose = view.findViewById<View>(R.id.relativeClose) as RelativeLayout
        val textInputServerUrl = view.findViewById<View>(R.id.textInputServerUrl) as TextInputEditText
        val btnSubmit = view.findViewById<View>(R.id.btnSubmit) as Button

        if(AppConstants.isNotEmpty(AppConstants.MainURL)){
            val mainURL = AppConstants.MainURL.replace("https://","").dropLast(1)
            textInputServerUrl.setText(mainURL)
        }

        btnSubmit.setOnClickListener {

            val strServerURL = textInputServerUrl.text.toString().trim()
            val strMainServerURL = "https://"+textInputServerUrl.text.toString().trim()+"/"

            if(strServerURL == ""){
                AppConstants.ToastMessage(requireActivity(),"Please enter Server URL.")
            }else if(!Patterns.WEB_URL.matcher(strMainServerURL).matches()){
                AppConstants.ToastMessage(requireActivity(),"Please enter Valid Server URL.")
            }else{
                val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(btnSubmit.windowToken, 0)

                AppConstants.ToastMessage(requireActivity(),"Server URL set successfully")
                AppConstants.MainURL = strMainServerURL
                AppPreference.setStringPreference(requireActivity(), AppPreference.serverURL,strMainServerURL)
                AppConstants.LOGE("server_URL strServerURL => ",strServerURL)
                AppConstants.LOGE("server_URL strMainServerURL => ",strMainServerURL)
                dialog.dismiss()
            }

        }

        relativeClose.setOnClickListener {
            val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(relativeClose.windowToken, 0)
            dialog.dismiss()
        }

        dialog.setContentView(view)

    }

}