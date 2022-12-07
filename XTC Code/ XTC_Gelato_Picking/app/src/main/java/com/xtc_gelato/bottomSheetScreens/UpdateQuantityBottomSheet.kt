package com.xtc_gelato.bottomSheetScreens

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.xtc_gelato.R
import com.xtc_gelato.constent_classes.AppConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UpdateQuantityBottomSheet : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view: View = LayoutInflater.from(context).inflate(R.layout.layout_update_quantity_bottom_sheet, null)

        val batchNo =  arguments?.getString("batch_no")
        val batchQty =  arguments?.getString("batch_qty")

        val relativeClose = view.findViewById<View>(R.id.relativeClose) as RelativeLayout
        val textSerialNumber = view.findViewById<View>(R.id.textSerialNumber) as TextView
        val edtQty = view.findViewById<View>(R.id.edtQty) as EditText

        textSerialNumber.text = batchNo
        edtQty.setText(batchQty)

        val btnConfirm = view.findViewById<View>(R.id.btnConfirm) as Button

        btnConfirm.setOnClickListener {

            val strQty = edtQty.text.toString().trim()

            if(strQty == ""){
                AppConstants.ToastMessage(requireActivity(),"Please enter quantity.")
            }else{
                val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(btnConfirm.windowToken, 0)

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