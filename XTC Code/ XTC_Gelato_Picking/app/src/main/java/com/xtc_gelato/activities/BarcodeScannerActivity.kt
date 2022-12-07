package com.xtc_gelato.activities

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xtc_gelato.R
import com.xtc_gelato.constent_classes.AppConstants
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback

/*
* This barcode scanner class is use for barcode scanner functionality
* */

class BarcodeScannerActivity : AppCompatActivity() {

    private lateinit var relativeBottomView: RelativeLayout
    private lateinit var linearAdd: LinearLayout
    private lateinit var linearExit: LinearLayout
    private lateinit var textHeaderText: TextView
    private lateinit var relativeLeft: RelativeLayout
    private lateinit var mCodeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        AppConstants.statusbarColorChange(this)

        AppConstants.scannedBarCode = ""

        relativeLeft = findViewById<View>(R.id.relativeLeft) as RelativeLayout
        textHeaderText = findViewById<View>(R.id.textHeaderText) as TextView

        relativeBottomView = findViewById<View>(R.id.relativeBottomView) as RelativeLayout
        linearAdd = findViewById<View>(R.id.linearAdd) as LinearLayout
        linearExit = findViewById<View>(R.id.linearExit) as LinearLayout

        relativeBottomView.visibility = View.GONE

        textHeaderText.text = "Barcode scanner"

        relativeLeft.setOnClickListener {
            onBackPressed()
        }

        linearAdd.setOnClickListener {
            AppConstants.ToastMessage(this,"Coming soon!")
        }

        linearExit.setOnClickListener {
            AppConstants.ToastMessage(this,"Coming soon!")
        }

        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        mCodeScanner = CodeScanner(this, scannerView)
        mCodeScanner.decodeCallback = DecodeCallback { result ->
            runOnUiThread {
                val strBarCODE = result.text
                val strBarcodeForma = result.barcodeFormat.name
                if (!strBarcodeForma.equals("QR_CODE", ignoreCase = true)) {

                    // Beep Sound after barcode scan...
                    val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)

                    AppConstants.scannedBarCode = strBarCODE.toString()

                    onBackPressed()

                    //AppConstants.ToastMessage(this,strBarCODE.toString())
                    //relativeBottomView.visibility = View.VISIBLE

                } else {
                    Toast.makeText(this@BarcodeScannerActivity, "Invalid Barcode.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        scannerView.setOnClickListener {
            relativeBottomView.visibility = View.GONE
            mCodeScanner.startPreview()
        }

    }

    override fun onResume() {
        mCodeScanner.startPreview()
        super.onResume()
    }

    override fun onPause() {
        mCodeScanner.releaseResources()
        super.onPause()
    }
}