package com.getpy.dikshasshop.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.microsoft.appcenter.analytics.Analytics
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.Constants
import com.getpy.dikshasshop.Utils.formatString
import com.getpy.dikshasshop.Utils.multiPlyVaue
import com.getpy.dikshasshop.bottomsheet.CartBottomSheetFragment
import com.getpy.dikshasshop.data.db.entities.ProductsDataModel
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.CartItemRowBinding
import com.getpy.dikshasshop.ui.cart.CartFragment
import com.getpy.dikshasshop.ui.main.MainActivity

class CartItemsAdapter(val preference: PreferenceProvider, val fm:FragmentManager, val context: Context, var mCategoriesList:MutableList<ProductsDataModel>) : RecyclerView.Adapter<CartItemsAdapter.DeveloperViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DeveloperViewHolder {
        val mDeveloperListItemBinding = DataBindingUtil.inflate<CartItemRowBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.cart_item_row, viewGroup, false)
        return DeveloperViewHolder(mDeveloperListItemBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DeveloperViewHolder, i: Int) {
        val model = mCategoriesList.get(i)
        var url:String
        if(model.imageLinkFlag!=null) {
            if (model.imageLinkFlag.equals("R")) {
                url = UbboFreshApp.instance?.imageLoadUrl + model.productPicUrl
            } else {
                url = model.productPicUrl
            }
        }else
        {
            if(model.productPicUrl!=null)
            {
                url = UbboFreshApp.instance?.imageLoadUrl + model.productPicUrl
            }else
            {
                url=UbboFreshApp.instance?.imageLoadUrl.toString()
            }
        }
        /*val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_no_image_found)
                .error(R.drawable.ic_no_image_found)
        Glide.with(context).load(url).apply(options).into(holder.mItemCategoryRowBinding.image)*/
        holder.mItemCategoryRowBinding.image.load(url) {
            placeholder(R.drawable.ic_no_image_found)
            error(R.drawable.ic_no_image_found)
        }
        holder.mItemCategoryRowBinding.productName.text=model.productName
        holder.mItemCategoryRowBinding.productPrice.text="Item Price: "+ Constants.priceSymbol+formatString(model.sellingPrice.toDouble())
        holder.mItemCategoryRowBinding.totPrice.text="Items cost: "+multiPlyVaue(model.itemCount,model.sellingPrice)
        holder.mItemCategoryRowBinding.count.text= model.itemCount.toString()

        holder.mItemCategoryRowBinding.icAdd.setOnClickListener(View.OnClickListener {
            addItems(holder,i)
        })
        holder.mItemCategoryRowBinding.icRemove.setOnClickListener(View.OnClickListener {
            removeItems(holder,i)
        })
        holder.mItemCategoryRowBinding.removetext.setOnClickListener(View.OnClickListener {
            removeTextItems(holder,i)
        })

    }

    fun addItems(mholder: DeveloperViewHolder, position:Int)
    {
        val model=mCategoriesList.get(position)
        val addText=mholder.mItemCategoryRowBinding.count.text.toString()
        var count:Int
        if(addText.equals("add"))
        {
            count=0
        }else
        {
            count=addText.toInt()
        }
        count=(count+1)
        mholder.mItemCategoryRowBinding.count.setText(count.toString())
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
        notifyDataSetChanged()

        val map= HashMap<String, String>()
        map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
        map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
        map.put("productid", model.citrineProdId.toString())
        map.put("productname",model.productName.toString())
        map.put("itemcount", model.itemCount.toString())
        Analytics.trackEvent("Add Product Clicked", map)

        MainActivity.setupBadge()
        CartFragment.runnable?.let { Handler().postDelayed(it,10) }
        CartFragment.addRunnable?.let { Handler().postDelayed(it,10) }
    }
    fun removeItems(mholder: DeveloperViewHolder, position: Int)
    {
        var count=mholder.mItemCategoryRowBinding.count.text.toString().toInt()
        if(count!=1) {
            UbboFreshApp.instance?.productsDataModel= UbboFreshApp.instance?.carItemsList?.get(position)
            count = --count
            UbboFreshApp.instance?.productsDataModel?.itemCount = count
            UbboFreshApp.instance?.hashMap?.put(UbboFreshApp.instance?.productsDataModel?.citrineProdId!!, UbboFreshApp.instance?.productsDataModel!!)
            mholder.mItemCategoryRowBinding.count.text = count.toString()
            CartFragment.addRunnable?.let { Handler().postDelayed(it,10) }
            notifyDataSetChanged()
            MainActivity.setupBadge()


            val map= HashMap<String, String>()
            map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
            map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
            map.put("productid", UbboFreshApp.instance?.productsDataModel?.citrineProdId.toString())
            map.put("productname",UbboFreshApp.instance?.productsDataModel?.productName.toString())
            map.put("itemcount", UbboFreshApp.instance?.productsDataModel?.itemCount.toString())
            Analytics.trackEvent("Add Product Clicked", map)

            CartFragment.runnable?.let { Handler().postDelayed(it,10) }
        }
    }

    fun removeTextItems(mholder: DeveloperViewHolder, position: Int)
    {
        var count=mholder.mItemCategoryRowBinding.count.text.toString().toInt()
        UbboFreshApp.instance?.productsDataModel= UbboFreshApp.instance?.carItemsList?.get(position)
        count = 0
        UbboFreshApp.instance?.productsDataModel?.itemCount = count
        UbboFreshApp.instance?.carItemsList?.removeAt(position)
        mCategoriesList.removeAt(position)
        var map = HashMap<String,String>()
        UbboFreshApp.instance?.productsDataModel?.citrineProdId?.let { map.put("Product ID", it) }
        UbboFreshApp.instance?.productsDataModel?.productName?.let { map.put("Product Name", it) }
        UbboFreshApp.instance?.productsDataModel?.mrp?.let { map.put("MRP", it) }
        Analytics.trackEvent("Product Removed",map)
        UbboFreshApp.instance?.hashMap?.remove(UbboFreshApp.instance?.productsDataModel?.citrineProdId)
        if(UbboFreshApp.instance?.carItemsList!!.size>0)
        {
            CartFragment.binding.carItemsLayout.visibility=View.VISIBLE
            CartFragment.binding.cartEmptyLayout.visibility=View.GONE
        }else
        {
            CartFragment.binding.carItemsLayout.visibility=View.GONE
            CartFragment.binding.cartEmptyLayout.visibility=View.VISIBLE
        }
        CartFragment.removeRunnable?.let { Handler().postDelayed(it,10) }
        notifyDataSetChanged()
        MainActivity.setupBadge()
        CartFragment.runnable?.let { Handler().postDelayed(it,10) }
    }

    override fun getItemCount(): Int {
        return if (mCategoriesList != null) {
            mCategoriesList!!.size
        } else {
            0
        }
    }

    fun setDeveloperList(mCategoriesList: ArrayList<ProductsDataModel>) {
        this.mCategoriesList = mCategoriesList
        notifyDataSetChanged()
    }

    inner class DeveloperViewHolder(var mItemCategoryRowBinding: CartItemRowBinding) :
        RecyclerView.ViewHolder(mItemCategoryRowBinding.root)
    {

        init {
            mItemCategoryRowBinding.productPrice.setTypeface(UbboFreshApp.instance?.latoregular)
            mItemCategoryRowBinding.productName.setTypeface(UbboFreshApp.instance?.latoregular)
            mItemCategoryRowBinding.count.setTypeface(UbboFreshApp.instance?.latoregular)
            mItemCategoryRowBinding.removetext.setTypeface(UbboFreshApp.instance?.latoregular)
            mItemCategoryRowBinding.image.setOnClickListener(View.OnClickListener {
                val model=mCategoriesList.get(adapterPosition)
                val sheet = CartBottomSheetFragment(preference,false,adapterPosition,model,mCategoriesList)
                sheet.show(fm, "DemoBottomSheetFragment")
            })

        }
    }
}