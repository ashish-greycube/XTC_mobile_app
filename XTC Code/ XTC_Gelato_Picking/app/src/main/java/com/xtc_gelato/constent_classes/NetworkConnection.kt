
import android.content.Context
import android.net.ConnectivityManager

object NetworkConnection {

    fun hasConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return if (activeNetwork != null && activeNetwork.isConnected) {
            true
        } else false
    }
}