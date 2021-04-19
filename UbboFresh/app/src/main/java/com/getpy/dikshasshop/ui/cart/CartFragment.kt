package com.getpy.dikshasshop.ui.cart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.CartItmesAdapter
import com.getpy.dikshasshop.bottomsheet.InstructionBottomSheetFragment
import com.getpy.dikshasshop.data.db.AppDataBase
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.FragmentCartBinding
import com.getpy.dikshasshop.ui.cart.cartactivities.CheckOutActivity
import com.getpy.dikshasshop.ui.home.InjectionFragment
import com.getpy.dikshasshop.ui.main.MainActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance
import java.util.*

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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_cart, container, false)
        binding.recyclerview.setHasFixedSize(true)
        MainActivity.binding.activityMainAppbarlayout.showView()
        MainActivity.binding.selectStore.hideView()
        MainActivity.binding.activityMainToolbarTitle.setTypeface(UbboFreshApp.instance?.latoregular)
        MainActivity.binding.layout.gravity=Gravity.CENTER
        MainActivity.binding.activityMainToolbarTitle.setText("My Cart")
        binding.procedBut.setOnClickListener(View.OnClickListener {
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
                activity?.okDialogWithOneAct(Constants.appName,"Hello sir, your cart is empty")
            }
        })
        runnable= Runnable {
            caleculateTotal()
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
                    e.printStackTrace()
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
        cartItems()
        caleculateTotal()
        return binding.root
    }

    fun cartItems()
    {
        if(UbboFreshApp.instance?.carItemsList!=null) {
            if (UbboFreshApp.instance?.carItemsList!!.size > 0) {
                binding.cartEmptyLayout.visibility = View.GONE
                binding.carItemsLayout.visibility = View.VISIBLE
                adapter = UbboFreshApp.instance?.carItemsList?.let {
                    activity?.let { it1 -> CartItmesAdapter(preference,it1.supportFragmentManager,it1, it) }
                }
                binding.recyclerview.adapter = adapter
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
    fun caleculateTotal()
    {
        var totalPrice=0.0
        var totalTax=0.0
        var discount=0.0
        for(i in 0 until UbboFreshApp.instance?.carItemsList!!.size)
        {
            val model=UbboFreshApp.instance?.carItemsList?.get(i)
            totalPrice=totalPrice+(model?.itemCount!!.times(model.sellingPrice.toDouble()))
            var thisProductTax = 0.0
            if(model.gst != null ){
                var gstPercentage = model.gst.toDouble()
                var gstOnOneUnit = model.sellingPrice.toDouble() * gstPercentage / 100
                thisProductTax = (model?.itemCount!!.times(gstOnOneUnit))
            }
            if(model.cess != null){
                var cessPercentage = model.cess.toDouble()
                var cessOnOneUnit = model.sellingPrice.toDouble() * cessPercentage / 100
                thisProductTax += (model?.itemCount!!.times(cessOnOneUnit))
            }
            totalTax += thisProductTax
            if(model.discount!=null) {
                discount = discount + model.discount.toDouble()
            }
        }
        var totalCost = totalTax + totalPrice
        var roundedTotalTax = String.format("%.2f", totalTax)
        var roundedTotalPrice = String.format("%.2f", totalPrice)
        var roundedTotalCost= String.format("%.2f", totalCost)
        binding.totMrpValue.setText(roundedTotalPrice)
        binding.gstValue.setText(roundedTotalTax)
        binding.totalPrice.setText(roundedTotalCost)
        binding.discountValue.setText(discount.toString())
    }
    fun init()
    {
        binding.totMrp.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.totMrpValue.setTypeface(UbboFreshApp.instance?.latoheavy)
        //binding.gstText.setTypeface(UbboFreshApp.instance?.latoheavy)
        //binding.gstValue.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.discount.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.discountValue.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.couponDiscount.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.couponDiscountValue.setTypeface(UbboFreshApp.instance?.latoheavy)
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
        var adapter:CartItmesAdapter?=null
    }

}