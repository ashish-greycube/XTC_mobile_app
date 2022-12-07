package com.xtc_gelato.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.xtc_gelato.R
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.localDB.DatabaseHandler
import com.github.barteksc.pdfviewer.PDFView
import com.tanodxyz.gdownload.DownloadInfo
import com.tanodxyz.gdownload.DownloadProgressListener
import com.tanodxyz.gdownload.GDownload
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*
* This pickup class is used to Display PDF of pickup details.
* */

class PickupScreenActivity : AppCompatActivity() {
    private lateinit var textErrorMessage: TextView
    private lateinit var pdfView: PDFView
    private lateinit var imgRight: ImageView
    private var deliveryNote: String? = null
    private var soNo: String? = null
    private var deliveryPdfUrl: String? = null
    private lateinit var relativeProgressView: RelativeLayout
    private lateinit var btnFinishOrder: Button
    private lateinit var imgPDF: ImageView
    private lateinit var textSerialNo: TextView
    private lateinit var textPickupNote: TextView
    private lateinit var textClient: TextView
    private lateinit var imgHeaderIcon: ImageView
    private lateinit var textHeaderText: TextView
    private lateinit var relativeRight: RelativeLayout
    private lateinit var relativeLeft: RelativeLayout

    var PERMISSION_ALL = 1
    var PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_screen)

        AppConstants.statusbarColorWhiteChange(this)

        // Initialization File Downloader
        GDownload.init(lifecycle)

        relativeProgressView = findViewById<View>(R.id.relativeProgressView) as RelativeLayout
        relativeLeft = findViewById<View>(R.id.relativeLeft) as RelativeLayout
        relativeRight = findViewById<View>(R.id.relativeRight) as RelativeLayout
        val linearHeaderTitle = findViewById<View>(R.id.linearHeaderTitle) as LinearLayout
        relativeRight.visibility = View.VISIBLE

        textHeaderText = findViewById<View>(R.id.textHeaderText) as TextView
        imgRight = findViewById<View>(R.id.imgRight) as ImageView
        imgHeaderIcon = findViewById<View>(R.id.imgHeaderIcon) as ImageView
        imgHeaderIcon.background = getDrawable(R.drawable.icon_menu_active_stock)

        imgRight.background = getDrawable(R.drawable.icon_download)

        textHeaderText.text = "to pick"

        val clientName = intent.getStringExtra("client_name")
        soNo = intent.getStringExtra("so_no")
        deliveryNote = intent.getStringExtra("delivery_note")
        deliveryPdfUrl = intent.getStringExtra("delivery_pdf_url")

        linearHeaderTitle.setOnClickListener {
            btnFinishOrder.performClick()
        }
        relativeLeft.setOnClickListener {
            btnFinishOrder.performClick()
        }

        textErrorMessage = findViewById<View>(R.id.textErrorMessage) as TextView
        pdfView = findViewById<View>(R.id.pdfView) as PDFView

        //-----/ uploaded SoNo record delete... /--------------------------------------------
        val localDB = DatabaseHandler(this@PickupScreenActivity)
        val arrPickedListSoNoWise = localDB.getScanPickedListSoNoWise(soNo.toString())
        for(picked in arrPickedListSoNoWise){
            localDB.deleteScanPickedDetailsBySoNo(picked.strSoNo)
        }
        //-----------------------------------------------------------------------------------

        textClient = findViewById<View>(R.id.textClient) as TextView
        textSerialNo = findViewById<View>(R.id.textSerialNo) as TextView
        textPickupNote = findViewById<View>(R.id.textPickupNote) as TextView

        textClient.text = clientName.toString().trim()
        textSerialNo.text = soNo.toString().trim()
        textPickupNote.text = deliveryNote.toString().trim()

        relativeRight.setOnClickListener {
            if(AppConstants.isNotEmpty(deliveryPdfUrl)){
                if (NetworkConnection.hasConnection(this)) {
                    if (!hasPermissions(this)) {
                        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
                    } else {
                        pdfDownloadTask(deliveryPdfUrl!!)
                    }
                }else{
                    AppConstants.CheckConnection(this)
                }
            }else{
                AppConstants.ToastMessage(this@PickupScreenActivity,"PDF Not found.")
            }
        }

        btnFinishOrder = findViewById<View>(R.id.btnFinishOrder) as Button

        btnFinishOrder.setOnClickListener {
            AppConstants.isReloadToPickupList = true
            onBackPressed()
        }

        if (NetworkConnection.hasConnection(this)) {
            loadPDF(deliveryPdfUrl!!)
        }else{
            AppConstants.CheckConnection(this)
        }

    }

    fun loadPDF(downloadUrl: String){

        val downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/'), downloadUrl.length)
        val path = File(cacheDir,downloadFileName.replace("/",""))

        AppConstants.LOGE("path => ",path.absolutePath)

        pdfDownload(downloadUrl,path.toString(),false)

    }

    fun hasPermissions(context: Context?): Boolean {

        if (context != null) {
            for (permission in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == PERMISSION_ALL) {

            val TAG = "permission => "

            // BEGIN_INCLUDE(permission_result)
            // Received permission result for location permission.
            // Check if the only required permission has been granted

            if (hasPermissions(this)) {

                // Storage permission has been granted, preview can be displayed
                AppConstants.LOGE(TAG, "Storage permission has now been granted, for download PDF")

                pdfDownloadTask(deliveryPdfUrl!!)

            } else {

                Toast.makeText(this, "Please grant storage permission for download PDF.", Toast.LENGTH_LONG).show()
                AppConstants.LOGE(TAG, "Storage permission was NOT granted")

            } // END_INCLUDE(permission_result)
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }


    fun pdfDownloadTask(downloadUrl: String) {

        //val downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/'), downloadUrl.length) //Create file name by picking download file name from URL
        //Log.e(TAG, downloadFileName);

        val timeStamp: String = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Date())
        var downloadFileName = ""
        if(AppConstants.isNotEmpty(deliveryNote)){
            downloadFileName = deliveryNote+"_"+timeStamp+".pdf"
        }else{
            downloadFileName = "$timeStamp.pdf"
        }

        AppConstants.LOGE("downloadFileName => ",downloadFileName.toString())

        val fileDirectory = File(
            Environment.getExternalStorageDirectory().toString()
                    + File.separator
                    + Environment.DIRECTORY_DOWNLOADS
                    + File.separator
                    + resources.getString(R.string.app_name)
        )

        //If File is not present create directory
        if (!fileDirectory.exists()) {
            fileDirectory.mkdirs()
            // Log.e(TAG, "Directory Created.");
        }

        val filePath = File(fileDirectory,downloadFileName.replace("/",""))

        pdfDownload(downloadUrl,filePath.toString(),true)

    }

    fun pdfDownload(fileURL: String?, filePath: String?, isPDFDownload: Boolean){

        relativeProgressView.visibility = View.VISIBLE
        AppConstants.LOGE("fileDownload fileURL => " , fileURL.toString())
        AppConstants.LOGE("fileDownload filePath => " , filePath.toString())

        GDownload.singleDownload(this) {

            url = fileURL
            name = filePath //"fileName or FilePath"

            downloadProgressListener = object : DownloadProgressListener {

                override fun onConnectionEstablished(downloadInfo: DownloadInfo?) {}

                override fun onDownloadProgress(downloadInfo: DownloadInfo) {
                    AppConstants.LOGE("fileDownload => " , downloadInfo.progress.toString())
                }

                override fun onDownloadFailed(downloadInfo: DownloadInfo, ex: String) {}

                override fun onDownloadSuccess(downloadInfo: DownloadInfo) {

                    AppConstants.LOGE("fileDownload Success => " , downloadInfo.filePath.toString())

                    //val message = "PDF downloaded at : \n\n"+ downloadInfo.filePath.toString();




                    if(isPDFDownload){
                        val message = "PDF downloaded successfully";
                        AppConstants.ToastMessage(this@PickupScreenActivity,message)
                        relativeProgressView.visibility = View.GONE
                        openPDF(downloadInfo.filePath.toString())
                    }else{

                        pdfView.fromFile(File(filePath.toString()))
//                            .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
                            .enableSwipe(true) // allows to block changing pages using swipe
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .defaultPage(0)
//                            .onDraw(this@InvoiceDetailsActivity) // allows to draw something on a provided canvas, above the current page
//                            .onLoad(this) // called after document is loaded and starts to be rendered
//                            .onPageChange(this)
//                            .onPageScroll(this)
//                            .onError(this)
//                            .onRender(this) // called after document is rendered for the first time
                            .onError {
                                textErrorMessage.visibility = View.VISIBLE
                            }
                            .onLoad {
                                textErrorMessage.visibility = View.GONE
                            }
                            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                            .password(null)
                            .scrollHandle(null)
                            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                            .load()
                        relativeProgressView.visibility = View.GONE
                    }

                }

                override fun onDownloadIsMultiConnection(
                    downloadInfo: DownloadInfo,
                    multiConnection: Boolean
                ) {}

                override fun onPause(downloadInfo: DownloadInfo, paused: Boolean, reason: String) {}

                override fun onRestart(
                    downloadInfo: DownloadInfo,
                    restarted: Boolean,
                    reason: String
                ) {}

                override fun onResume(downloadInfo: DownloadInfo, resume: Boolean, reason: String) {}

                override fun onStop(downloadInfo: DownloadInfo, stopped: Boolean, reason: String) {}

            }
        }

    }


    private fun openPDF(filePath: String) {

        // Get the File location and file name.
        val file = File(filePath)
        AppConstants.LOGE("pdfFIle", "" + file)

        // Get the URI Path of file.
        val uriPdfPath: Uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        AppConstants.LOGE("pdfPath", "" + uriPdfPath)

        // Start Intent to View PDF from the Installed Applications.
        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
        pdfOpenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        pdfOpenIntent.clipData = ClipData.newRawUri("", uriPdfPath)
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf")
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            startActivity(pdfOpenIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(this, "There is no app to load corresponding PDF", Toast.LENGTH_LONG).show()
        }
    }

}