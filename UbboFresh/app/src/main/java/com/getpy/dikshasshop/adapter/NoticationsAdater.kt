package com.getpy.dikshasshop.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.data.model.CampaignCustomerNotificationData
import com.getpy.dikshasshop.databinding.NotificationRowBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NoticationsAdater(val context: Context, var list:ArrayList<CampaignCustomerNotificationData>): RecyclerView.Adapter<NoticationsAdater.DeveloperViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(item: CampaignCustomerNotificationData?)
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DeveloperViewHolder {
        val notificationRowBinding = DataBindingUtil.inflate<NotificationRowBinding>(
                LayoutInflater.from(viewGroup.context),
                R.layout.notification_row, viewGroup, false
        )
        return DeveloperViewHolder(notificationRowBinding)
    }

    override fun onBindViewHolder(holder: DeveloperViewHolder, i: Int) {
        var model = list?.get(i)
        holder.mBinding.notificationDate.text= model.SentDate?.let { createdDate(it) }
        holder.mBinding.notificationText.text=model.Title
    }
    fun createdDate(d:String):String
    {
        val form = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        var date: Date? = null
        try {
            date = form.parse(d.toString())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val postFormater = SimpleDateFormat("MMM dd, yyyy hh:mm")
        val newDateStr: String = postFormater.format(date)
        return newDateStr
    }

    override fun getItemCount(): Int {
        return if (list != null) {
            list.size
        } else {
            0
        }
    }

    fun setDeveloperList(mDeveloperModel: ArrayList<CampaignCustomerNotificationData>) {
        this.list = mDeveloperModel
        notifyDataSetChanged()
    }

    inner class DeveloperViewHolder(var mBinding: NotificationRowBinding) :
            RecyclerView.ViewHolder(mBinding.root)
    {
          init {
              mBinding.notificationText.setTypeface(UbboFreshApp.instance?.latobold)
              mBinding.notificationDate.setTypeface(UbboFreshApp.instance?.latoblack)
          }
    }
}