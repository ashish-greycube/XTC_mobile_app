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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.preference.PreferenceManager
import com.xtc_gelato.R
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.constent_classes.AppPreference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

/*
* This is Splash activity class.
* */
class SplashActivity : AppCompatActivity() {

    private var prefs: SharedPreferences? = null
    private var linearTop: LinearLayout? = null
    private var bottomAnim: Animation? = null
    private var topAnim: Animation? = null
    private var imgBottom: AppCompatImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        linearTop = findViewById<View>(R.id.linearTop) as LinearLayout
        imgBottom = findViewById<View>(R.id.imgBottom) as AppCompatImageView

        linearTop!!.visibility = View.GONE
        imgBottom!!.visibility = View.GONE

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)


        Picasso.get()
            .load(R.drawable.splash_bg_bottom)
            //.into(imgBottom)
            .into(imgBottom, object : Callback {
                override fun onSuccess() {

                    linearTop!!.visibility = View.VISIBLE
                    imgBottom!!.visibility = View.VISIBLE

                    // Start Animation
                    linearTop?.animation = topAnim
                    imgBottom?.animation = bottomAnim

                }
                override fun onError(ex: Exception) {

                }
            })

        //Animations




        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        AppConstants.MainURL = AppPreference.getStringPreference(this, AppPreference.serverURL)

        goToNextScreen()

    }

    private fun goToNextScreen(){

        Handler(Looper.getMainLooper()).postDelayed({

            if (NetworkConnection.hasConnection(this@SplashActivity)) {

                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                finish()

            } else{

                val dialog = Dialog(this@SplashActivity)
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

                buttonOk.text = resources.getString(R.string.retry)
                textMessage.text = resources.getString(R.string.Please_Check_Your_Internet_Connection)

                buttonCancel.setOnClickListener {
                    dialog.dismiss()
                    finish()
                }

                buttonOk.setOnClickListener {
                    dialog.dismiss()
                    goToNextScreen()
                }

                dialog.show()

            }

        },2500)

    }

}