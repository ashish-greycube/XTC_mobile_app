package com.xtc_gelato.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.xtc_gelato.R
import com.xtc_gelato.activities.ToPickupDetailsActivity
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.constent_classes.AppPreference
import com.xtc_gelato.models.beanLiveStock

class ToPickupListAdapter : RecyclerView.Adapter<ToPickupListAdapter.MyViewHolder>()  {
    private lateinit var arrLiveStock: java.util.ArrayList<beanLiveStock>
    private var activity: Activity? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_to_pickup_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.textSerialNo.text = arrLiveStock[position].soNo
        holder.textClient.text = arrLiveStock[position].client
        if(AppConstants.isNotEmpty(arrLiveStock[position].deliveryDate)){
            holder.textDate.text = AppConstants.dateFormatChangesDDMMYYYY(arrLiveStock[position].deliveryDate)
        }else{
            holder.textDate.text = "-"
        }

        if(arrLiveStock[position].picker == "" || arrLiveStock[position].picker.equals(AppPreference.getStringPreference(activity!!,AppPreference.userEmail),ignoreCase = true)){
            holder.relativeMain.background = activity!!.getDrawable(R.color.white)
            holder.textClient.setTextColor(activity!!.resources.getColor(R.color.black))
            holder.textSerialNo.setTextColor(activity!!.resources.getColor(R.color.black))
            holder.textDate.setTextColor(activity!!.resources.getColor(R.color.black))
            holder.imgEnable.visibility = View.VISIBLE
            holder.imgDisable.visibility = View.GONE
        }else{
            holder.relativeMain.background = activity!!.getDrawable(R.color.colorGreyLite1)
            holder.textClient.setTextColor(activity!!.resources.getColor(R.color.colorGrey))
            holder.textSerialNo.setTextColor(activity!!.resources.getColor(R.color.colorGrey))
            holder.textDate.setTextColor(activity!!.resources.getColor(R.color.colorGrey))
            holder.imgEnable.visibility = View.GONE
            holder.imgDisable.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {

            if(arrLiveStock[position].picker == "" || arrLiveStock[position].picker == AppPreference.getStringPreference(activity!!,AppPreference.userEmail)){

                val imm = activity!!.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(holder.itemView.windowToken, 0)

                val intent = Intent(activity!!, ToPickupDetailsActivity::class.java)
                intent.putExtra("client_name", arrLiveStock[position].client)
                intent.putExtra("so_no", arrLiveStock[position].soNo)
                intent.putExtra("print_label_url", arrLiveStock[position].printLabelUrl)
                intent.putExtra("picker_instruction", arrLiveStock[position].pickerInstruction)
                activity!!.startActivity(intent)
                activity!!.overridePendingTransition(R.anim.enter, R.anim.exit)

            }

        }

    }

    override fun getItemCount(): Int {
        return arrLiveStock.size
    }

    fun setData(
        mActivity: Activity?,
        mArrLiveStock: ArrayList<beanLiveStock>
    ) {
        activity = mActivity
        arrLiveStock = mArrLiveStock
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var relativeMain: RelativeLayout = itemView.findViewById<View>(R.id.relativeMain) as RelativeLayout
        var textClient: TextView = itemView.findViewById<View>(R.id.textClient) as TextView
        var textDate: TextView = itemView.findViewById<View>(R.id.textDate) as TextView
        var textSerialNo: TextView = itemView.findViewById<View>(R.id.textSerialNo) as TextView
        var imgEnable: ImageView = itemView.findViewById<View>(R.id.imgEnable) as ImageView
        var imgDisable: ImageView = itemView.findViewById<View>(R.id.imgDisable) as ImageView

    }

}