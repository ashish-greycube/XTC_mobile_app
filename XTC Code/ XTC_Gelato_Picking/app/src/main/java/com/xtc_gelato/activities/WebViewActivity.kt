package com.xtc_gelato.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.xtc_gelato.R
import im.delight.android.webview.AdvancedWebView


/*
* This is WebViewActivity class display web content from URL in webview.
* */

class WebViewActivity : AppCompatActivity() , AdvancedWebView.Listener {
    private lateinit var mWebView: AdvancedWebView
    private lateinit var progressBar: ProgressBar

    var strWebLink = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        strWebLink = intent.getStringExtra("web_url")!!

        progressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        progressBar.visibility = View.VISIBLE

        mWebView = findViewById<View>(R.id.webview) as AdvancedWebView

        loadWebViewURL()

    }

    private fun loadWebViewURL() {
        mWebView.setListener(this@WebViewActivity, this@WebViewActivity)
        mWebView.setMixedContentAllowed(false)
        mWebView.loadUrl(strWebLink)
        progressBar.progress = 10

        progressBar.progress = 10

        setWebClient()

        mWebView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.e("onPageStarted called", "URL===> $url")
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Log.i("onReceivedError", "=$description")
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }

    }

    private fun setWebClient() {
        mWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(
                view: WebView,
                newProgress: Int
            ) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress < 100 && progressBar.visibility == ProgressBar.GONE) {
                    progressBar.visibility = ProgressBar.VISIBLE
                }
                if (newProgress == 100) {
                    progressBar.visibility = ProgressBar.GONE
                }
            }
        }
    }


    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        mWebView.onResume()
        // ...
    }

    @SuppressLint("NewApi")
    override fun onPause() {
        mWebView.onPause()
        // ...
        super.onPause()
    }

    override fun onDestroy() {
        mWebView.onDestroy()
        // ...
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        mWebView.onActivityResult(requestCode, resultCode, intent)
    }


    override fun onPageStarted(url: String?, favicon: Bitmap?) {}

    override fun onPageFinished(url: String?) {
        progressBar.visibility = View.GONE
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {}

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {}
}