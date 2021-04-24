package com.getpy.dikshasshop.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.toast
import com.getpy.dikshasshop.data.model.CouponDataModel
import com.getpy.dikshasshop.databinding.CouponListRowBinding
import com.getpy.dikshasshop.listeners.ItemClickListener

class CouponListAdapter( val context: Context, var mCategoriesList:MutableList<CouponDataModel>,val itemClickListener: ItemClickListener) : RecyclerView.Adapter<CouponListAdapter.DeveloperViewHolder>() {
    inner class DeveloperViewHolder(var mDeveloperListItemBinding: CouponListRowBinding) :
            RecyclerView.ViewHolder(mDeveloperListItemBinding.root){
        init{
            mDeveloperListItemBinding.couponCode.typeface = UbboFreshApp.instance?.latoregular
            mDeveloperListItemBinding.moreDetails.typeface = UbboFreshApp.instance?.latoregular
            mDeveloperListItemBinding.couponInfo.typeface = UbboFreshApp.instance?.latoregular
            mDeveloperListItemBinding.minValue.typeface = UbboFreshApp.instance?.latoregular
            mDeveloperListItemBinding.moreDetails.setOnClickListener(View.OnClickListener {
                context.toast("No Details Available")
                //toggleDetails(mDeveloperListItemBinding)
            })
        }
    }

    private fun toggleDetails(mDeveloperListItemBinding: CouponListRowBinding) {
        if(mDeveloperListItemBinding.moreDetailsLayout.visibility == View.GONE){
            mDeveloperListItemBinding.moreDetailsLayout.visibility = View.VISIBLE
            mDeveloperListItemBinding.dropdownArrow.setImageResource(R.drawable.ic_arrow_up)
        }
        else{
            mDeveloperListItemBinding.moreDetailsLayout.visibility = View.GONE
            mDeveloperListItemBinding.dropdownArrow.setImageResource(R.drawable.ic_arrow_down)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeveloperViewHolder {
        val mDeveloperListItemBinding = DataBindingUtil.inflate<CouponListRowBinding>(
                LayoutInflater.from(parent.context),
                R.layout.coupon_list_row, parent, false)
        return DeveloperViewHolder(mDeveloperListItemBinding)
    }

    override fun getItemCount(): Int {
        return mCategoriesList?.size
    }

    override fun onBindViewHolder(holder: DeveloperViewHolder, position: Int) {
        val model = mCategoriesList.get(position)
        holder.mDeveloperListItemBinding.couponCode.text = model.CouponCode
        holder.mDeveloperListItemBinding.minValue.text = "Rs. "+model.MinAmount
        holder.mDeveloperListItemBinding.couponInfo.text = model.CouponInfo
        holder.mDeveloperListItemBinding.tncDetails.text = model.TnCDetais
        holder.mDeveloperListItemBinding.startDateValue.text = model.StartsFrom
        holder.mDeveloperListItemBinding.endDateValue.text = model.ExpiresOn
        holder.mDeveloperListItemBinding.maxDiscountValue.text = model.MaxDiscount
        holder.mDeveloperListItemBinding.root.setOnClickListener (View.OnClickListener {
            itemClickListener.onItemClick(holder.mDeveloperListItemBinding.root,position)
        })

    }

}