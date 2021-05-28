package com.getpy.dikshasshop.bottomsheet

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import coil.load
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.BottomSheetPagerAdapter
import com.getpy.dikshasshop.data.db.entities.ProductsDataModel
import com.getpy.dikshasshop.data.model.SlidingImageData
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.FragmentDemoSheetBinding
import com.getpy.dikshasshop.ui.Products.ProductImageViewerActivity
import com.getpy.dikshasshop.ui.auth.LoginActivity
import com.getpy.dikshasshop.ui.auth.OTPVerificationActivity
import com.getpy.dikshasshop.ui.main.MainActivity
import com.getpy.fresh.views.Products.ProductsFragment
import com.getpy.fresh.views.home.HomeFragment
import com.microsoft.appcenter.analytics.Analytics
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DemoBottomSheetFragment(val preference: PreferenceProvider,val isCmgFrmHm:Boolean,val position:Int,val model:ProductsDataModel,val plist:ArrayList<ProductsDataModel>) : SuperBottomSheetFragment() {
    lateinit var binding:FragmentDemoSheetBinding
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_demo_sheet, container, false)
        val list=ArrayList<SlidingImageData>()
        val data= SlidingImageData()

        //---------------------------------------------------------------
        val imageList = ArrayList<SlideModel>() // Create image list

        // imageList.add(SlideModel("String Url" or R.drawable, "title") You can add title


        //---------------------------------------------------------------
        if(model.imageList != null){
            for(i in model.imageList){
                try {

                    if (i.ImageLinkFlag.equals("R")) {
                        data.OfferImgURL= UbboFreshApp.instance?.imageLoadUrl+i.productPicUrl
                        imageList.add(SlideModel(data.OfferImgURL," "))
                    }
                    else {
                        data.OfferImgURL= i.productPicUrl
                        imageList.add(SlideModel(data.OfferImgURL," "))
                    }
                }
                catch (e: Exception){

                    print(e.message)
                }
            }
            list.add(data)
            binding.imageSlider.setImageList(imageList)
        }
        if(imageList.count()<1){
            if(model.productPicUrl!=null) {
                if (model.imageLinkFlag.equals("R")) {
                    data.OfferImgURL= UbboFreshApp.instance?.imageLoadUrl+model.productPicUrl
                    imageList.add(SlideModel(data.OfferImgURL," "))
                }
                else {
                    data.OfferImgURL= model.productPicUrl
                    imageList.add(SlideModel(data.OfferImgURL," "))
                }
            }
            else{
                data.OfferImgURL = ""
                imageList.add(SlideModel(data.OfferImgURL," "))
            }
            list.add(data)
            binding.imageSlider.setImageList(imageList)
        }
        binding.imageSlider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                try {
                    val intent= Intent(context, ProductImageViewerActivity::class.java)
                    intent.putExtra("ImgURL", imageList[position].imageUrl)
                    startActivity(intent)
                }
                catch(ex: Exception){
                    activity?.toast("Error in loading Image")
                }
                // You can listen here
            }
        })

        val viewPagerAdapter = context?.let { BottomSheetPagerAdapter(it,list) }
        //binding.viewItemImage.adapter = viewPagerAdapter
        //binding.viewItemImage.load(list[0].OfferImgURL){
            //placeholder(R.drawable.ic_no_image_found)
           // error(R.drawable.ic_no_image_found)
        //}

        // Color of Category type indicator
        when (model.productType) {
            "veg" -> {
                binding.productdetailTypeColor.setImageResource(R.drawable.ic_color_green)
                binding.productdetailTypeLabel.text = "veg"
            }
            "nonveg" -> {
                binding.productdetailTypeColor.setImageResource(R.drawable.ic_color_red)
                binding.productdetailTypeLabel.text = "non veg"
            }
            "cold" -> {
                binding.productdetailTypeColor.setImageResource(R.drawable.ic_color_blue)
                binding.productdetailTypeLabel.text = "cold"
            }
            "spicy" -> {
                binding.productdetailTypeColor.setImageResource(R.drawable.ic_color_yellow)
                binding.productdetailTypeLabel.text = "hot/spicy"
            }
        }

        binding.productName.text=model.productName
        if(model.sellingPrice.toDouble()<model.mrp.toDouble()) {
            binding.strikeLine.showView()
            binding.mrp.showView()
            binding.discount.showView()
            binding.yousave.showView()
            val discount = ((model.mrp.toDouble().minus(model.sellingPrice.toDouble()))*100).div(model.mrp.toDouble())
            binding.discount.text= formatString(discount) +"%"
            binding.mrp.text = "(" + formatStrWithPrice(model.mrp) + ")"//Constants.priceSymbol+model.mrp
            binding.strikeLine.text = "(" +formatStrWithPrice(model.mrp) + ")"
        }else
        {
            binding.strikeLine.hideView()
            binding.mrp.hideView()
            binding.discount.hideView()
            binding.yousave.hideView()
        }
        binding.price.text= formatStrWithPrice(model.sellingPrice)
        binding.desc.text=model.productDesc
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




        binding.inclueLayout.productName.text=model.productName
        binding.inclueLayout.productPrice.text="Item Price"+formatStrWithPrice(model.sellingPrice)
        binding.inclueLayout.totPrice.text="Items Cost"+formatStrWithPrice(model.sellingPrice)

        if(UbboFreshApp.instance?.hashMap!!.containsKey(model.citrineProdId))
        {
            val count=UbboFreshApp.instance?.hashMap!!.get(model.citrineProdId)
            binding.inclueLayout.countText.setText(count?.itemCount.toString())
            binding.inclueLayout.remove.showView()
            binding.inclueLayout.add.showView()
        }else
        {
            binding.inclueLayout.remove.hideView()
            binding.inclueLayout.add.hideView()
            binding.inclueLayout.countText.setText("Add")
        }

        if(model.availability_Status.toLowerCase() == "no"){
            binding.inclueLayout.layout.background = ResourcesCompat.getDrawable(resources, R.drawable.rectangle_corner_disabled, null)!!
            binding.inclueLayout.layout.setOnClickListener(View.OnClickListener {
                activity?.toast("Currently out of stock")
            })
        }
        else{
            binding.inclueLayout.layout.setOnClickListener(View.OnClickListener {

                binding.inclueLayout.remove.showView()
                binding.inclueLayout.add.showView()
                addItems(model)
            })
            binding.inclueLayout.add.setOnClickListener(View.OnClickListener {
                addItems(model)
            })
            binding.inclueLayout.remove.setOnClickListener(View.OnClickListener {
                removeItems(position)
            })

        }
        return binding.root
    }

    fun addItems(model:ProductsDataModel)
    {
        binding.inclueLayout.remove.visibility=View.VISIBLE
        var addText=binding.inclueLayout.countText.text.toString()
        var count:Int
        if(addText.equals("Add"))
        {
            count=0
        }else
        {
            count=addText.toInt()
        }
        count=(count+1)
        binding.inclueLayout.countText.setText(count.toString())
        if(UbboFreshApp.instance?.hashMap!!.containsKey(model.citrineProdId))
        {
            model.itemCount=count
            UbboFreshApp.instance?.hashMap?.put(model.citrineProdId,model)
            for(i in 0 until UbboFreshApp.instance?.carItemsList?.size!!)
            {
                val mm=UbboFreshApp.instance?.carItemsList?.get(i)
                if(mm?.citrineProdId.equals(model.citrineProdId))
                {
                    UbboFreshApp.instance?.carItemsList?.removeAt(i)
                    UbboFreshApp.instance?.carItemsList?.add(i,model)
                }
            }
        }else
        {
            model.itemCount=count
            UbboFreshApp.instance?.carItemsList?.add(model)
            UbboFreshApp.instance?.hashMap?.put(model.citrineProdId,model)
        }

        val map= HashMap<String, String>()
        map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
        map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
        map.put("productid", model.citrineProdId)
        map.put("productname",model.productName)
        map.put("itemcount", model.itemCount.toString())
        Analytics.trackEvent("Add Product clicked", map)

        MainActivity.setupBadge()
        if(isCmgFrmHm)
        {
            HomeFragment.tophadapter?.notifyDataSetChanged()
            HomeFragment.dealshadapter?.notifyDataSetChanged()
            HomeFragment.addRunnable?.let { Handler().postDelayed(it,10) }
        }else
        {
            ProductsFragment.binding.viewPager.adapter?.notifyDataSetChanged()
            ProductsFragment.runnable?.let { Handler().postDelayed(it,10) }
        }
    }

    fun removeItems(position: Int)
    {
        var count=binding.inclueLayout.countText.text.toString().toInt()
        var pos:Int=0
        if(count!=0) {
            val model=plist.get(position)
            for(i in 0 until UbboFreshApp.instance?.carItemsList!!.size)
            {
                val mm=UbboFreshApp.instance?.carItemsList?.get(i)
                if(mm?.citrineProdId.equals(model.citrineProdId))
                {
                    pos=i
                    UbboFreshApp.instance?.productsDataModel= UbboFreshApp.instance?.carItemsList?.get(i)
                }
            }
            if (count == 1) {
                count = 0
                UbboFreshApp.instance?.productsDataModel?.itemCount = count
                UbboFreshApp.instance?.carItemsList?.removeAt(pos)
                UbboFreshApp.instance?.hashMap?.remove(UbboFreshApp.instance?.productsDataModel?.citrineProdId)
                binding.inclueLayout.countText.text = "Add"
                binding.inclueLayout.remove.hideView()
                binding.inclueLayout.add.hideView()
                //removing data from db
                if(isCmgFrmHm)
                {
                    HomeFragment.tophadapter?.notifyDataSetChanged()
                    HomeFragment.dealshadapter?.notifyDataSetChanged()
                    HomeFragment.removeRunnable?.let { Handler().postDelayed(it,10) }
                }else {
                    ProductsFragment.binding.viewPager.adapter?.notifyDataSetChanged()
                    ProductsFragment.removerunnable?.let { Handler().postDelayed(it, 10) }
                }
            } else {
                count = --count
                UbboFreshApp.instance?.productsDataModel?.itemCount = count
                UbboFreshApp.instance?.hashMap?.put(UbboFreshApp.instance?.productsDataModel?.citrineProdId!!, UbboFreshApp.instance?.productsDataModel!!)
                binding.inclueLayout.countText.text = count.toString()
                //inserting data into db
                if(isCmgFrmHm) {
                    HomeFragment.tophadapter?.notifyDataSetChanged()
                    HomeFragment.dealshadapter?.notifyDataSetChanged()
                    ProductsFragment.runnable?.let { Handler().postDelayed(it, 10) }
                }else
                {
                    ProductsFragment.binding.viewPager.adapter?.notifyDataSetChanged()
                    HomeFragment.addRunnable?.let { Handler().postDelayed(it,10) }
                }
            }

            val map= HashMap<String, String>()
            map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
            map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
            map.put("productid", UbboFreshApp.instance?.productsDataModel?.citrineProdId.toString())
            map.put("productname", UbboFreshApp.instance?.productsDataModel?.productName.toString())
            map.put("itemcount", UbboFreshApp.instance?.productsDataModel?.itemCount.toString())
            Analytics.trackEvent("Add Product clicked", map)

            MainActivity.setupBadge()
        }
    }
    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    } // Author: silentnuke


}
