   package com.getpy.dikshasshop.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.databinding.DateListRowBinding
import com.getpy.dikshasshop.listeners.ItemClickListener
import java.text.SimpleDateFormat
import java.util.*

   class ScheduleDateAdapter(val context: Context, var mCategoriesList: MutableList<Date>, val itemClickListener: ItemClickListener) : RecyclerView.Adapter<ScheduleDateAdapter.DeveloperViewHolder>() {

   var lastSelectedPosition : Int = 0
   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleDateAdapter.DeveloperViewHolder {
    val mDeveloperListItemBinding = DataBindingUtil.inflate<DateListRowBinding>(
            LayoutInflater.from(parent.context),
            R.layout.date_list_row, parent, false)
    return DeveloperViewHolder(mDeveloperListItemBinding)
   }

   override fun onBindViewHolder(holder: ScheduleDateAdapter.DeveloperViewHolder, position: Int) {
        val model = mCategoriesList.get(position)
        holder.mDeveloperListItemBinding.date.text = model.toString("EEE, d MMM ")
        val tempDate = getCurrentDateTime()
        val sdf = SimpleDateFormat("yyyyMMdd")
        val tempNextDate = addDaysToDate(tempDate, 1)
        when (sdf.format(model)) {
             sdf.format(tempDate) -> {
                holder.mDeveloperListItemBinding.dateStatus.text = "(today)"
            }
             sdf.format(tempNextDate) -> {
                holder.mDeveloperListItemBinding.dateStatus.text = "(tomorrow)"
            }
            else -> {
                holder.mDeveloperListItemBinding.dateStatus.text = ""
            }
        }

        holder.mDeveloperListItemBinding.dateCard.setOnClickListener  {
            //val lastModel = mCategoriesList.get(position)
            lastSelectedPosition = position
            itemClickListener.onItemClick(holder.mDeveloperListItemBinding.root, position)
            notifyDataSetChanged()
        }
       if(lastSelectedPosition==position){
           holder.mDeveloperListItemBinding.dateCard.setCardBackgroundColor(Color.parseColor("#B02B18"))
           holder.mDeveloperListItemBinding.date.setTextColor(Color.WHITE)
           holder.mDeveloperListItemBinding.dateStatus.setTextColor(Color.WHITE)
       }
       else
       {
           holder.mDeveloperListItemBinding.dateCard.setCardBackgroundColor(Color.WHITE)
           holder.mDeveloperListItemBinding.date.setTextColor(Color.DKGRAY)
           holder.mDeveloperListItemBinding.dateStatus.setTextColor(Color.parseColor("#B02B18"))
       }
   }

   override fun getItemCount(): Int {
       return mCategoriesList?.size
   }

   inner class DeveloperViewHolder(var mDeveloperListItemBinding: DateListRowBinding) :
           RecyclerView.ViewHolder(mDeveloperListItemBinding.root){
               init{
               }
           }

   private fun getCurrentDateTime(): Date {
       return Calendar.getInstance().time
   }

   fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
       val formatter = SimpleDateFormat(format, locale)
       return formatter.format(this)
   }

   private fun addDaysToDate(dt: Date, days: Int) :Date{
       val c = Calendar.getInstance()
       c.time = dt
       c.add(Calendar.DATE, days)
       return  c.time

   }

}