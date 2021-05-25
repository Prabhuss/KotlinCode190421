package com.getpy.dikshasshop.ui.cart

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.CartItemsAdapter
import com.getpy.dikshasshop.adapter.CartUnavailableItemsAdapter
import com.getpy.dikshasshop.bottomsheet.InstructionBottomSheetFragment
import com.getpy.dikshasshop.data.db.AppDataBase
import com.getpy.dikshasshop.data.db.entities.ProductsDataModel
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.FragmentCartBinding
import com.getpy.dikshasshop.ui.cart.cartactivities.CheckOutActivity
import com.getpy.dikshasshop.ui.home.InjectionFragment
import com.getpy.dikshasshop.ui.main.MainActivity
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance

class CartFragment : InjectionFragment() {
    private val appDataBase: AppDataBase by instance()
    private val preference:PreferenceProvider by instance()
    var latitude:String?=null
    var longitude:String?=null
    var city:String=""
    var state: String=""
    var country: String=""
    var postalCode: String=""
    var address:String=""
    var knownName: String=""
    var totalPrice:String=""
    var discount:String=""

    private val factory: CartViewModelFactory by instance()
    lateinit var viewmodel: CartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_cart, container, false)
        binding.recyclerview.setHasFixedSize(true)
        viewmodel= activity?.let { ViewModelProviders.of(it,factory).get(CartViewModel::class.java) }!!

        UbboFreshApp.instance?.instructionString = ""
        MainActivity.binding.activityMainAppbarlayout.showView()
        MainActivity.binding.selectStore.hideView()
        MainActivity.binding.activityMainToolbarTitle.setTypeface(UbboFreshApp.instance?.latoregular)
        MainActivity.binding.layout.gravity=Gravity.CENTER
        MainActivity.binding.activityMainToolbarTitle.setText("My Cart")
        binding.procedBut.setOnClickListener(View.OnClickListener {
            try {
                if(binding.totalPrice.text.toString().toFloat() < 1){
                    Toast.makeText(context, "Seems like your cart is empty.", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                if(UbboFreshApp.instance?.carItemsList!!.size>0) {
                    val intent = Intent(activity, CheckOutActivity::class.java)
                    intent.putExtra("totalPayablePrice", binding.totalPrice.text.toString())
                    intent.putExtra("totalMRP", binding.totMrpValue.text.toString())
                    intent.putExtra("totalTax", binding.gstValue.text.toString())
                    intent.putExtra("discount", binding.discountValue.text.toString())
                    intent.putExtra("address",address)
                    startActivity(intent)
                }else
                {
                    activity?.okDialogWithOneAct(Constants.appName,"Your cart is empty.")
                }
            }
            catch (e: Exception){
                Toast.makeText(context, "CE01:Unable to access the cart.", Toast.LENGTH_SHORT).show()
            }
        })
        runnable= Runnable {
            calculatetotal()
        }
        addRunnable = Runnable {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    UbboFreshApp.instance?.carItemsList?.let { appDataBase.CustomerAddressDao().insertProductsData(it) }
                }catch (e:CancellationException)
                {
                    Log.i("scope","job is canceled")
                }
                catch (e:Exception)
                {
                    activity?.okDialogWithOneAct("Error", e.message.toString())
                }
            }
        }
        removeRunnable = Runnable {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    UbboFreshApp.instance?.productsDataModel?.let { appDataBase.CustomerAddressDao().deleteProductsData(it) }
                }catch (e:CancellationException)
                {
                    Log.i("scope","job is canceled")
                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                }
            }
        }

        naviagateRunnable = Runnable {
            if(MainActivity.navcontroller?.currentDestination?.id==R.id.cartFragment) {
                MainActivity.navcontroller?.navigate(R.id.action_cartFragment_to_productsFragment)
            }
        }

        binding.instrucText.setOnClickListener {
            val sheet = InstructionBottomSheetFragment()
            sheet.show(activity?.supportFragmentManager!!, "DemoBottomSheetFragment")
        }
        init()
        binding.progBar.visibility = View.VISIBLE
        cartItems()
        return binding.root
    }

    fun cartItems()
    {
        try {
            if(UbboFreshApp.instance?.carItemsList!=null) {
                if (UbboFreshApp.instance?.carItemsList!!.size > 0) {
                    binding.cartEmptyLayout.visibility = View.GONE
                    binding.carItemsLayout.visibility = View.VISIBLE
                    //Generate Product Id List to request to Server
                    var productIdList :JsonArray? = JsonArray()
                    UbboFreshApp.instance?.carItemsList?.let {
                        for(i in UbboFreshApp.instance?.carItemsList!!){
                            productIdList?.add(i.citrineProdId)
                        }
                    }
                    var json = JsonObject()
                    json.addProperty("phone_number",preference.getStringData(Constants.saveMobileNumkey))
                    json.addProperty("merchant_id",preference.getIntData(Constants.saveMerchantIdKey))
                    json.addProperty("access_key",preference.getStringData(Constants.saveaccesskey))
                    json.add("Product",productIdList)

                    //call api here
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val response = viewmodel.getCartItemFromServer(json )
                            //assign api items to cartItemsList
                            for(i in response.data){
                                for(j in 0 until UbboFreshApp.instance?.carItemsList!!.size)
                                    if(UbboFreshApp.instance?.carItemsList!![j].citrineProdId == i.citrineProdId){
                                        i.itemCount = UbboFreshApp.instance?.carItemsList!![j].itemCount
                                        i.PrimaryPhone = UbboFreshApp.instance?.carItemsList!![j].PrimaryPhone
                                        break
                                    }
                            }
                            UbboFreshApp.instance?.carItemsList = response.data
                            MainActivity.setupBadge()

                        }
                        catch (e: Exception){
                            Toast.makeText(context,"error", Toast.LENGTH_SHORT).show()
                        }
                        //declare two adapter 1 for available 2 for unavailable
                        //assign to adapter 1
                        var availableProductList : MutableList<ProductsDataModel> = ArrayList()
                        var unavailableProductList : MutableList<ProductsDataModel> = ArrayList()
                        for(i in UbboFreshApp.instance?.carItemsList!!){
                            if(i.availability_Status.toLowerCase().equals("yes")){
                                availableProductList.add(i)
                            }
                            else{
                                unavailableProductList.add(i)
                            }
                        }
                       /* for(item in availableProductList){
                            if(!UbboFreshApp.instance?.hashMap?.containsKey(item.citrineProdId)!!){
                                UbboFreshApp.instance?.hashMap?.remove(item.citrineProdId)
                                item?.let { appDataBase.CustomerAddressDao().deleteProductsData(it) }
                            }
                        }*/


                        //assign adapter value respectively
                        adapter = availableProductList?.let {
                            activity?.let { it1 -> CartItemsAdapter(preference,it1.supportFragmentManager,it1, it) }
                        }
                        binding.recyclerview.adapter = adapter
                        //assign adapter2 value respectively
                        adapter2 = unavailableProductList?.let {
                            activity?.let { it1 -> CartUnavailableItemsAdapter(preference,it1.supportFragmentManager,it1, it) }
                        }
                        binding.recyclerviewUnavailable.adapter = adapter2
                        calculatetotal()
                    }
                } else {
                    binding.cartEmptyLayout.visibility = View.VISIBLE
                    binding.carItemsLayout.visibility = View.GONE
                }
            }else
            {
                binding.cartEmptyLayout.visibility = View.VISIBLE
                binding.carItemsLayout.visibility = View.GONE
            }
        }
        catch (e: Exception){
            Toast.makeText(context, "Error while loading the Cart. Please try after sometime", Toast.LENGTH_SHORT).show()
        }
        finally {

            Handler().postDelayed({binding.progBar.visibility = View.GONE},500)

        }
    }
    fun calculatetotal()
    {
        var totalPrice=0.0
        var totalTax=0.0
        var discount=0.0
        for(i in 0 until UbboFreshApp.instance?.carItemsList!!.size)
        {
            val model=UbboFreshApp.instance?.carItemsList?.get(i)
            if (model != null) {
                if(model.availability_Status.toLowerCase().equals("yes")){
                    totalPrice += (model.itemCount.times(model.sellingPrice.toDouble()))
                    var thisProductTax = 0.0
                    if(model.gst != null ){
                        val gstPercentage = model.gst.toDouble()
                        val gstOnOneUnit = model.sellingPrice.toDouble() * gstPercentage / 100
                        thisProductTax = (model.itemCount.times(gstOnOneUnit))
                    }
                    if(model.cess != null){
                        val cessPercentage = model.cess.toDouble()
                        val cessOnOneUnit = model.sellingPrice.toDouble() * cessPercentage / 100
                        thisProductTax += (model.itemCount.times(cessOnOneUnit))
                    }
                    totalTax += thisProductTax
                    if(model.discount!=null) {
                        discount = discount + model.discount.toDouble()
                    }
                }
            }
        }
        val totalCost = totalTax + totalPrice
        val roundedTotalTax = String.format("%.2f", totalTax)
        val roundedTotalPrice = String.format("%.2f", totalPrice)
        val roundedTotalCost= String.format("%.2f", totalCost)
        binding.totMrpValue.setText(roundedTotalPrice)
        binding.gstValue.setText(roundedTotalTax)
        binding.totalPrice.setText(roundedTotalCost)
        binding.discountValue.setText(discount.toString())
    }
    fun init()
    {
        binding.totMrp.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.totMrpValue.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.discount.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.discountValue.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.total.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.totalPrice.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.procedBut.setTypeface(UbboFreshApp.instance?.latoheavy)
    }
    override fun onStart() {
        super.onStart()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        lateinit var binding:FragmentCartBinding
        var runnable:Runnable?=null
        var addRunnable:Runnable?=null
        var removeRunnable:Runnable?=null
        var naviagateRunnable:Runnable?=null
        var adapter:CartItemsAdapter?=null
        var adapter2:CartUnavailableItemsAdapter?=null
    }

}