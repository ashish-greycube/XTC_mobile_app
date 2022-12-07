package com.xtc_gelato.constent_classes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager

class Connectivity {

    /**
     * Get the network info
     * @param context
     * @return
     */
    fun getNetworkInfo(context: Context): NetworkInfo {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo!!
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    fun isConnected(context: Context): Boolean {
        val info: NetworkInfo = getNetworkInfo(context)
        return info != null && info.isConnected
    }

    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     *
     * @return
     */
    fun isConnectedWifi(context: Context): Boolean {
        val info: NetworkInfo = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     *
     * @return
     */
    fun isConnectedMobile(context: Context): Boolean {
        val info: NetworkInfo = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    fun isConnectedFast(context: Context): Boolean {
        val info: NetworkInfo = getNetworkInfo(context)
        return info != null && info.isConnected && isConnectionFast(
            info.type,
            info.subtype
        )
    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    fun isConnectionFast(type: Int, subType: Int): Boolean {
        return if (type == ConnectivityManager.TYPE_WIFI) {
            true
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            when (subType) {
                TelephonyManager.NETWORK_TYPE_1xRTT -> false // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_CDMA -> false // ~ 14-64 kbps
                TelephonyManager.NETWORK_TYPE_EDGE -> false // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> true // ~ 400-1000 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_A -> true // ~ 600-1400 kbps
                TelephonyManager.NETWORK_TYPE_GPRS -> false // ~ 100 kbps
                TelephonyManager.NETWORK_TYPE_HSDPA -> true // ~ 2-14 Mbps
                TelephonyManager.NETWORK_TYPE_HSPA -> true // ~ 700-1700 kbps
                TelephonyManager.NETWORK_TYPE_HSUPA -> true // ~ 1-23 Mbps
                TelephonyManager.NETWORK_TYPE_UMTS -> true // ~ 400-7000 kbps
                TelephonyManager.NETWORK_TYPE_EHRPD -> true // ~ 1-2 Mbps
                TelephonyManager.NETWORK_TYPE_EVDO_B -> true // ~ 5 Mbps
                TelephonyManager.NETWORK_TYPE_HSPAP -> true // ~ 10-20 Mbps
                TelephonyManager.NETWORK_TYPE_IDEN -> false // ~25 kbps
                TelephonyManager.NETWORK_TYPE_LTE -> true // ~ 10+ Mbps
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> false
                else -> false
            }
        } else {
            false
        }
    }


    fun getConnectedSpeed(context: Context): String? {
        val info: NetworkInfo = getNetworkInfo(context)
        var connectionSpeed: String? = "Not deletected"
        if (info != null && info.isConnected) {
            connectionSpeed = getConnectionSpeed(info.type, info.subtype)
        }
        return connectionSpeed
    }

    fun getConnectionSpeed(type: Int, subType: Int): String? {
        return if (type == ConnectivityManager.TYPE_WIFI) {
            "Not deletected"
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            when (subType) {
                TelephonyManager.NETWORK_TYPE_1xRTT -> "50-100 kbps" // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_CDMA -> "14-64 kbps" // ~ 14-64 kbps
                TelephonyManager.NETWORK_TYPE_EDGE -> "50-100 kbps" // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> "400-1000 kbps" // ~ 400-1000 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_A -> "600-1400 kbps" // ~ 600-1400 kbps
                TelephonyManager.NETWORK_TYPE_GPRS -> "100 kbps" // ~ 100 kbps
                TelephonyManager.NETWORK_TYPE_HSDPA -> "2-14 Mbps" // ~ 2-14 Mbps
                TelephonyManager.NETWORK_TYPE_HSPA -> "700-1700 kbps" // ~ 700-1700 kbps
                TelephonyManager.NETWORK_TYPE_HSUPA -> "1-23 Mbps" // ~ 1-23 Mbps
                TelephonyManager.NETWORK_TYPE_UMTS -> "400-7000 kbps" // ~ 400-7000 kbps
                TelephonyManager.NETWORK_TYPE_EHRPD -> "1-2 Mbps" // ~ 1-2 Mbps
                TelephonyManager.NETWORK_TYPE_EVDO_B -> "5 Mbps" // ~ 5 Mbps
                TelephonyManager.NETWORK_TYPE_HSPAP -> "10-20 Mbps" // ~ 10-20 Mbps
                TelephonyManager.NETWORK_TYPE_IDEN -> "25 kbps" // ~25 kbps
                TelephonyManager.NETWORK_TYPE_LTE -> "10+ Mbps" // ~ 10+ Mbps
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Not deletected"
                else -> "Not deletected"
            }
        } else {
            "Not deletected"
        }
    }
}