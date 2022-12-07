package com.xtc_gelato.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.preference.PreferenceManager
import com.xtc_gelato.R
import com.xtc_gelato.bottomSheetScreens.ServerURLSetBottomSheet
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.constent_classes.AppPreference
import com.xtc_gelato.server_api_calls.API
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

/*
* This is Login class with forgot password and user can set his server URL to configure app
* */
class LoginActivity : AppCompatActivity() {

    private lateinit var relativePasswordHideShow: RelativeLayout
    private lateinit var imgPasswordIcon: ImageView
    private lateinit var adapter: ArrayAdapter<String?>
    private lateinit var textServerURL: TextView
    private lateinit var textForgotPassword: TextView
    private lateinit var checkboxRememberMe: CheckBox
    private lateinit var btnLogin: AppCompatButton
    private lateinit var textinputPassword: TextInputEditText
    private lateinit var textinputUsernameEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilUsernameEmail: TextInputLayout
    private lateinit var relativeProgressView: RelativeLayout
    private lateinit var prefs: SharedPreferences

    private var isPasswordShow: Boolean = false

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        AppConstants.statusbarColorWhiteChange(this)

        relativeProgressView = findViewById<View>(R.id.relativeProgressView) as RelativeLayout

        checkboxRememberMe = findViewById<View>(R.id.checkboxRememberMe) as CheckBox

        tilUsernameEmail = findViewById<View>(R.id.tilUsernameEmail) as TextInputLayout
        tilPassword = findViewById<View>(R.id.tilPassword) as TextInputLayout

        relativePasswordHideShow = findViewById<View>(R.id.relativePasswordHideShow) as RelativeLayout
        imgPasswordIcon = findViewById<View>(R.id.imgPasswordIcon) as ImageView

        textinputUsernameEmail = findViewById<View>(R.id.textinputUsernameEmail) as TextInputEditText
        textinputPassword = findViewById<View>(R.id.textinputPassword) as TextInputEditText

        textServerURL = findViewById<View>(R.id.textServerURL) as TextView
        textForgotPassword = findViewById<View>(R.id.textForgotPassword) as TextView

        textServerURL.paintFlags = textServerURL.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        textForgotPassword.paintFlags = textForgotPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        relativePasswordHideShow.setOnClickListener {

            if(isPasswordShow){
                isPasswordShow = false
                imgPasswordIcon.background = getDrawable(R.drawable.icon_pass_hide)
                tilPassword.editText!!.transformationMethod = PasswordTransformationMethod.getInstance()
            }else{
                isPasswordShow = true
                imgPasswordIcon.background = getDrawable(R.drawable.icon_pass_show)
                tilPassword.editText!!.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }

        }

        textForgotPassword.setOnClickListener {

            if(AppConstants.isNotEmpty(AppConstants.MainURL)){

                val forgotPasswordURL = "${AppConstants.MainURL}login#forgot"
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("web_url",forgotPasswordURL)
                startActivity(intent)
                overridePendingTransition(R.anim.enter, R.anim.exit)

            }else{
                AppConstants.ToastMessage(this@LoginActivity,"Please Set Server URL.")
            }


        }

        if(AppConstants.MainURL == ""){
            goToServerURLScreen()
        }

        textServerURL.setOnClickListener {
            goToServerURLScreen()
        }

        val isRemember = prefs.getBoolean("is_remember", false)
        val isLogin = prefs.getBoolean("is_login", false)
        val loginUsername = prefs.getString("login_username", "").toString()
        val loginPassword = prefs.getString("login_password", "").toString()

        checkboxRememberMe.isChecked = isRemember

        if(isLogin){
            textinputUsernameEmail.setText(loginUsername)
            textinputPassword.setText(loginPassword)
        }

        AppConstants.errorRemoveOnTextChange(tilUsernameEmail)
        AppConstants.errorRemoveOnTextChange(tilPassword)

        btnLogin = findViewById<View>(R.id.btnLogin) as AppCompatButton

        btnLogin.setOnClickListener {

            if(AppConstants.isNotEmpty(AppConstants.MainURL)){

                val strUsernameEmail = textinputUsernameEmail.text.toString().trim()
                val strPassword = textinputPassword.text.toString().trim()

                if(strUsernameEmail == ""){
                    tilUsernameEmail.error = resources.getString(R.string.enter_username_email)
                } else if(strPassword == ""){
                    tilPassword.error = resources.getString(R.string.enter_password)
                } else{

                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(btnLogin.windowToken, 0)

                    if (NetworkConnection.hasConnection(this)) {

                        AppConstants.LOGE("login strUsernameEmail => ",strUsernameEmail)
                        AppConstants.LOGE("login strPassword => ",strPassword)

                        userLoginAPICall(strUsernameEmail,strPassword)

                    }else{
                        AppConstants.CheckConnection(this)
                    }

                }
            }else{
                AppConstants.ToastMessage(this@LoginActivity,"Please Set Server URL.")
            }

        }

    }

    private  fun goToServerURLScreen(){
        val serverURLSetBottomSheet = ServerURLSetBottomSheet()
        serverURLSetBottomSheet.isCancelable = false
        serverURLSetBottomSheet.show(supportFragmentManager, "Login")
    }


    private fun userLoginAPICall(strUsernameEmail: String, strPassword: String) {

        relativeProgressView.visibility = View.VISIBLE

        val call: Call<ResponseBody> = API.getAPI().userLogin(strUsernameEmail,strPassword)

        call.enqueue(object : retrofit2.Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if(response.isSuccessful){
                    try {

                        val jsonObject = JSONObject(response.body()!!.string())
                        AppConstants.LOGE("response userLogin => ",jsonObject.toString())

                        val Cookielist = response.headers().values("Set-Cookie")
                        val cookieSessionId = Cookielist[0].split(";").toTypedArray()[0]

                        Log.e("loginRequest => ", " cookieval => ${cookieSessionId.toString()}")
                        AppConstants.cookieSessionId = cookieSessionId;

                        val strMessage = jsonObject.getString("message")
                        val strHomePage = jsonObject.getString("home_page")
                        val strFullName = jsonObject.getString("full_name")

                        AppPreference.setStringPreference(this@LoginActivity,AppPreference.fullName,strFullName)
                        AppPreference.setStringPreference(this@LoginActivity,AppPreference.userEmail,strUsernameEmail)

                        AppConstants.LOGE("userLogin strMessage => " , strMessage.toString())
                        AppConstants.LOGE("userLogin strHomePage => " , strHomePage.toString())
                        AppConstants.LOGE("userLogin strFullName => " , strFullName.toString())

                        val editor: SharedPreferences.Editor = prefs.edit()

                        if(checkboxRememberMe.isChecked){
                            editor.putString("login_username",strUsernameEmail.toString())
                            editor.putString("login_password",strPassword)
                            editor.putBoolean("is_remember",true)
                        }else{
                            editor.putString("login_username","")
                            editor.putString("login_password","")
                            editor.putBoolean("is_remember",false)
                        }
                        editor.putBoolean("is_login",true)
                        editor.apply()

                        goToHomeScreen()

                    }catch (e: Exception) {
                        relativeProgressView.visibility = View.GONE
                        AppConstants.ToastMessage(this@LoginActivity,"Something went wrong, please try again.")
                        AppConstants.LOGE("userLogin error message => " , e.toString())
                    }
                }else{
                    relativeProgressView.visibility = View.GONE

                    try {

                        val errorBody = response.errorBody()
                        val jObjError = JSONObject(errorBody!!.string())
                        AppConstants.LOGE("userLogin error message => " , jObjError.toString())


                        if(jObjError.has("message")){
                            val strMessage = jObjError.getString("message")
                            AppConstants.ToastMessage(this@LoginActivity,strMessage)
                        }else{
                            AppConstants.ToastMessage(this@LoginActivity,"Something went wrong, please try again.")
                        }

                    }catch (e: Exception) {
                        AppConstants.ToastMessage(this@LoginActivity,"Something went wrong, please try again.")
                        AppConstants.LOGE("userLogin error message => " , e.toString())
                    }

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                relativeProgressView.visibility = View.GONE
                AppConstants.ToastMessage(this@LoginActivity,"Something went wrong, please try again.")
                AppConstants.LOGE("userLogin error message => " , t.message.toString())
            }

        })

    }

    fun goToHomeScreen(){
        startActivity(Intent(this, DashboardActivity::class.java))
        overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        finish()
    }

}