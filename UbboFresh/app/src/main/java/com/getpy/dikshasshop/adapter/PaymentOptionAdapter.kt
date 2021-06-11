package com.getpy.dikshasshop.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getpy.dikshasshop.data.model.CouponDataModel
import com.getpy.dikshasshop.listeners.ItemClickListener

class PaymentOptionAdapter(val context: Context, var mCategoriesList:MutableList<CouponDataModel>, val itemClickListener: ItemClickListener) : RecyclerView.Adapter<CouponListAdapter.DeveloperViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CouponListAdapter.DeveloperViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: CouponListAdapter.DeveloperViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}