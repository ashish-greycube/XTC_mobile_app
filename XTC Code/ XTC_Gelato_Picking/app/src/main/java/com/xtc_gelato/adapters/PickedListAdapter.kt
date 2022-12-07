package com.xtc_gelato.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xtc_gelato.R
import com.xtc_gelato.constent_classes.AppConstants
import com.xtc_gelato.models.beanPickedScanDetailsDB

class PickedListAdapter : RecyclerView.Adapter<PickedListAdapter.MyViewHolder>() {
    private lateinit var arrPickedList: List<beanPickedScanDetailsDB>
    private lateinit var activity: Activity
    private lateinit var clickListener: (beanPickedScanDetailsDB) -> Unit

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_picked_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textBatchCode.text = arrPickedList[position].strBatchNo
        holder.textBatchQTY.text = arrPickedList[position].strQuantity.toString()

        holder.itemView.setOnClickListener {
            AppConstants.isDeleteBatch = false
            clickListener(arrPickedList[position])
        }

        holder.relativeDeleteBatch.setOnClickListener {
            AppConstants.isDeleteBatch = true
            clickListener(arrPickedList[position])
        }
    }

    override fun getItemCount(): Int {
        return arrPickedList.size
    }

    fun setData(
        mActivity: Activity,
        mArrPickedList: List<beanPickedScanDetailsDB>,
        clickListener: (beanPickedScanDetailsDB) -> Unit
        ) {
        activity = mActivity
        this.arrPickedList = mArrPickedList
        this.clickListener = clickListener
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textBatchCode: TextView = itemView.findViewById<View>(R.id.textBatchCode) as TextView
        var textBatchQTY: TextView = itemView.findViewById<View>(R.id.textBatchQTY) as TextView
        var relativeDeleteBatch: RelativeLayout = itemView.findViewById<View>(R.id.relativeDeleteBatch) as RelativeLayout

    }

}