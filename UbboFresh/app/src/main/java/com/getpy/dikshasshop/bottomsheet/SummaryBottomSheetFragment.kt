package com.getpy.dikshasshop.bottomsheet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import coil.load
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.BottomSheetPagerAdapter
import com.getpy.dikshasshop.data.model.CustomerInvoiceData
import com.getpy.dikshasshop.data.model.InvocieLineItems
import com.getpy.dikshasshop.data.model.SlidingImageData
import com.getpy.dikshasshop.databinding.SummaryFragmentDemoSheetBinding
import com.getpy.dikshasshop.ui.Products.ProductImageViewerActivity

class SummaryBottomSheetFragment(val isCmgFrmHm:Boolean, val position:Int, val model:InvocieLineItems,
                                 val plist:ArrayList<InvocieLineItems>,val cmodel:CustomerInvoiceData) : SuperBottomSheetFragment() {
    lateinit var binding:SummaryFragmentDemoSheetBinding
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding=DataBindingUtil.inflate(inflater,R.layout.summary_fragment_demo_sheet, container, false)
        val list=ArrayList<SlidingImageData>()
        for(i in 0 until 3)
        {
            if(i==0)
            {
                val data= SlidingImageData()
                if(model.ProductImage!=null)
                {
                    data.OfferImgURL= model.ProductImage

                }else
                {
                    data.OfferImgURL= ""
                }
                list.add(data)
            }else
            {
                val data= SlidingImageData()
                list.add(data)
            }

        }
        binding.viewItemImage.load(list[0].OfferImgURL){
            placeholder(R.drawable.ic_no_image_found)
            error(R.drawable.ic_no_image_found)
        }

        binding.viewItemImage.setOnClickListener {
            try {
                val intent= Intent(context, ProductImageViewerActivity::class.java)
                intent.putExtra("ImgURL", list[0].OfferImgURL)
                startActivity(intent)
            }
            catch(ex: Exception){
                activity?.toast("Error in loading Image")
            }
        }
        binding.productName.text=model.ProductName
        if(model.Discount!="0") {
            binding.strikeLine.showView()
            binding.mrp.showView()
            binding.discount.showView()
            binding.yousave.showView()
            val discount = model.Discount
            binding.discount.text= discount?.toDouble()?.let { formatString(it) } +"%"
            binding.mrp.text = "(" + model.UnitPrice?.let { formatStrWithPrice(it) } + ")"//Constants.priceSymbol+model.mrp
            binding.strikeLine.text = "(" + model.UnitPrice?.let { formatStrWithPrice(it) } + ")"
        }else
        {
            binding.strikeLine.hideView()
            binding.mrp.hideView()
            binding.discount.hideView()
            binding.yousave.hideView()
        }
        binding.price.text= model.UnitPriceAfterDiscount?.let { formatStrWithPrice(it) }
        binding.desc.text=""
        /*binding.packDem.text=""
        binding.brand.text=""
        binding.manfac.text=""
        binding.cntyor.text=""*/


        binding.productName.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.discount.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.price.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.mrp.setTypeface(UbboFreshApp.instance?.latoregular)

        binding.inclueLayout.productName.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.inclueLayout.countText.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.inclueLayout.totPrice.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.inclueLayout.productPrice.setTypeface(UbboFreshApp.instance?.latoregular)

        binding.packDem.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.brand.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.manfac.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.cntyor.setTypeface(UbboFreshApp.instance?.latoregular)

        binding.inclueLayout.productName.text=model.ProductName
        binding.inclueLayout.productPrice.text="Item Price"+ model.UnitPrice?.let { formatStrWithPrice(it) }
        binding.inclueLayout.totPrice.text="Items Cost"+ model.UnitPrice?.let { formatStrWithPrice(it) }

        return binding.root
    }

}