package com.xtc_gelato.constent_classes

import android.app.Activity
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object AppPreference {

    // Preference KEY....

    var serverURL = "server_url"
    var fullName = "full_name"
    var userEmail = "user_email"

    fun getStringPreference(activity: Activity, preferenceKEY: String): String {
        return PreferenceManager.getDefaultSharedPreferences(activity).getString(preferenceKEY, "").toString()
    }

    fun setStringPreference(activity: Activity, preferenceKEY: String, preferenceVALUE: String){
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putString(preferenceKEY,preferenceVALUE.toString())
        editor.apply()
    }

}