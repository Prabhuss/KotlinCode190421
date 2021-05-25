package com.getpy.dikshasshop.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.data.model.SlotDetailsData
import com.getpy.dikshasshop.databinding.SlotListRowBinding
import com.getpy.dikshasshop.listeners.ItemClickListener
import java.text.SimpleDateFormat
import java.util.*

class ScheduleSlotAdapter(val context: Context, var mCategoriesList: MutableList<SlotDetailsData>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<ScheduleSlotAdapter.DeveloperViewHolder>() {

    var lastSelectedPosition : Int = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleSlotAdapter.DeveloperViewHolder {
        val mDeveloperListItemBinding = DataBindingUtil.inflate<SlotListRowBinding>(
                LayoutInflater.from(parent.context),
                R.layout.slot_list_row, parent, false)
        return DeveloperViewHolder(mDeveloperListItemBinding)
    }

    override fun onBindViewHolder(holder: DeveloperViewHolder, position: Int) {
        val model = mCategoriesList.get(position)
        holder.mDeveloperListItemBinding.slot.text = model.slotTimings
        holder.mDeveloperListItemBinding.slotStatus.text = model.slotStatus
        holder.mDeveloperListItemBinding.slotCard.setOnClickListener(View.OnClickListener {
            if (model.slotStatus?.toLowerCase() == "available") {
                lastSelectedPosition = position
                holder.mDeveloperListItemBinding.backBtn.setImageResource(R.drawable.radio_checked_filled)
                itemClickListener.onItemClick(holder.mDeveloperListItemBinding.root, position)
                notifyDataSetChanged()
            } else {
                Toast.makeText(context, "This slot is not available.Try different slot.", Toast.LENGTH_SHORT).show()
            }
        })
        if(model.slotStatus?.toLowerCase() != "available"){
            holder.mDeveloperListItemBinding.backBtn.setImageResource(R.drawable.radio_cross)
        }
        else if(lastSelectedPosition!=position){
            holder.mDeveloperListItemBinding.backBtn.setImageResource(R.drawable.radio_unchecked)
        }
    }

    override fun getItemCount(): Int {
        return mCategoriesList?.size
    }

    inner class DeveloperViewHolder(var mDeveloperListItemBinding: SlotListRowBinding) :
            RecyclerView.ViewHolder(mDeveloperListItemBinding.root){
        init{

        }
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }
    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun addDaysToDate(dt: Date, days: Int) : Date {
        val c = Calendar.getInstance()
        c.time = dt
        c.add(Calendar.DATE, days)
        return  c.time

    }


}