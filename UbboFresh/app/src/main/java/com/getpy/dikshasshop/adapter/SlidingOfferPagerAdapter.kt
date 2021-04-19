package com.getpy.dikshasshop.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import coil.load
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.data.model.GetOfferDetailsData
import com.getpy.dikshasshop.databinding.SlidingPagerRowBinding
import com.getpy.dikshasshop.listeners.ItemClickListener

class SlidingOfferPagerAdapter(val context: Context, val images: ArrayList<GetOfferDetailsData>,val itemClickListener:ItemClickListener) : PagerAdapter() {
    override fun getCount(): Int = images.size
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = SlidingPagerRowBinding.inflate(LayoutInflater.from(context), container, false)
        val model=images.get(position)
        val url=model.OfferImgURL?:""
        binding.imageView.load(url) {
            placeholder(R.drawable.ic_no_image_found)
            error(R.drawable.ic_no_image_found)
        }
        binding.imageView.setOnClickListener(View.OnClickListener {
            itemClickListener.onItemClick(binding.imageView,position)
        })
        container.addView(binding.root)
        return binding.root
    }
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}