package com.getpy.dikshasshop.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.getpy.dikshasshop.listeners.ItemClickListener
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.data.model.GetOfferDetailsData
import com.getpy.dikshasshop.databinding.VerticalSlidingPagerRowBinding

class VerticalScrollImagesAdapter(val context: Context, var mCategoriesList:ArrayList<GetOfferDetailsData>, val listener: ItemClickListener) : RecyclerView.Adapter<VerticalScrollImagesAdapter.DeveloperViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DeveloperViewHolder {
        val mDeveloperListItemBinding = DataBindingUtil.inflate<VerticalSlidingPagerRowBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.vertical_sliding_pager_row, viewGroup, false)
        return DeveloperViewHolder(mDeveloperListItemBinding)
    }

    override fun onBindViewHolder(holder: DeveloperViewHolder, i: Int) {
        val model = mCategoriesList.get(i)
        /*val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_no_image_found)
            .error(R.drawable.ic_no_image_found)
        Glide.with(context).load(model.OfferImgURL).apply(options).into(holder.binding.imageView)*/
        holder.binding.imageView.load(model.OfferImgURL) {
            placeholder(R.drawable.ic_no_image_found)
            error(R.drawable.ic_no_image_found)
        }

    }

    override fun getItemCount(): Int {
        return if (mCategoriesList != null) {
            mCategoriesList!!.size
        } else {
            0
        }
    }
    inner class DeveloperViewHolder(var binding: VerticalSlidingPagerRowBinding) :
        RecyclerView.ViewHolder(binding.root)
    {
        init {
           binding.root.setOnClickListener(View.OnClickListener {
               listener.onItemClick(binding.root,adapterPosition)
           })
        }
    }
}