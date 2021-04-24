package com.getpy.dikshasshop.ui.cart.cartactivities

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.CouponListAdapter
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityCouponPageBinding
import com.getpy.dikshasshop.databinding.OkCustomDialogBinding
import com.getpy.dikshasshop.listeners.ItemClickListener
import com.getpy.dikshasshop.ui.cart.CartViewModel
import com.getpy.dikshasshop.ui.cart.CartViewModelFactory
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class CouponPageActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    lateinit var binding: ActivityCouponPageBinding
    private val factory: CartViewModelFactory by instance()
    var adapter: CouponListAdapter?=null
    lateinit var viewModel: CartViewModel
    private val preference: PreferenceProvider by instance()
    lateinit var totalPayableAmount : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        totalPayableAmount = intent.getStringExtra("TotalAmount").toString()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_coupon_page)
        viewModel = ViewModelProviders.of(this, factory).get(CartViewModel::class.java)
        binding.progressBar.show()
        //setContentView(R.layout.activity_coupon_page)
        binding.checkButton.setOnClickListener {
            if(binding.mannualCouponCode.text.isNullOrEmpty()){
                okDialogWithOneAct("Message","Please enter the coupon code")
                return@setOnClickListener
            }
            else{
                val mannualCouponCode = binding.mannualCouponCode.text.toString()
                checkCoupon(mannualCouponCode)
            }
        }
        //set Coupon List---------------------------------------------------------------------------
        getCouponList()
    }

    fun getCouponList(){
        lifecycleScope.launch {
            //Call Api
            binding.noCouponText.visibility = View.GONE
            val response = viewModel.getCouponCode(
                    preference.getIntData(Constants.saveMerchantIdKey),
                    preference.getStringData(Constants.saveMobileNumkey),
                    preference.getStringData(Constants.saveaccesskey))
            val list = response.data
            if (list != null) {
                    //Assign value from Api
                    adapter = list?.let { it1 ->
                        CouponListAdapter(this@CouponPageActivity, it1,object : ItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                val model= it1[position]
                                //Call check function----
                                checkCoupon(model.CouponCode)
                            }

                        }) }
                    binding.couponListRecyler.adapter = adapter
                    adapter?.notifyDataSetChanged()
            }
            else{
                binding.noCouponText.visibility = View.VISIBLE
            }
            binding.progressBar.dismiss()
        }
    }

    private fun checkCoupon(couponCode: String?) {

        binding.progressBar.show()
        lifecycleScope.launch {
            try {
                val response = viewModel.checkCouponCode(
                        preference.getIntData(Constants.saveMerchantIdKey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        preference.getStringData(Constants.saveaccesskey),
                        couponCode,
                        totalPayableAmount
                )
                if(response.status?.toLowerCase() == "success") {
                    if(response.ValidStatus?.toLowerCase()== "yes"){
                        val couponDiscount = response.CalculatedDiscount?.toDouble()
                        UbboFreshApp.instance?.couponDiscontAmount= couponDiscount
                        UbboFreshApp.instance?.couponApplied= couponCode
                        okDialogBox("Coupon Applied","Yayyy!! You save Rs. ${response.CalculatedDiscount} on this purchase.")
                        //toast(couponDiscount.toString())
                    }
                    else{
                        okDialogWithOneAct("Coupon not applied",response.message.toString())
                        UbboFreshApp.instance?.couponDiscontAmount= null
                        UbboFreshApp.instance?.couponApplied= null
                    }
                }
                else{
                    okDialogWithOneAct("Invalid",response.message.toString())
                    UbboFreshApp.instance?.couponDiscontAmount= null
                    UbboFreshApp.instance?.couponApplied= null
                }
            }
            catch(e : Exception){
                okDialogWithOneAct("Server Error",e.message.toString())
                UbboFreshApp.instance?.couponDiscontAmount= null
                UbboFreshApp.instance?.couponApplied= null
            }
            binding.progressBar.dismiss()
        }
    }

    fun okDialogBox(title:String,message:String)
    {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val binding : OkCustomDialogBinding = DataBindingUtil.inflate(
                LayoutInflater.from(this), R.layout.ok_custom_dialog, null, false);
        dialog.setContentView(binding.root)
        binding.cancelText.hideView()
        binding.header.text = title
        binding.message.text=message
        binding.okText.text="Ok"
        binding.okText.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.header.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.message.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.okText.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        if(this!=null)
            dialog.show()
    }
}