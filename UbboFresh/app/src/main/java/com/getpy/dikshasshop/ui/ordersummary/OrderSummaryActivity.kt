package com.getpy.dikshasshop.ui.ordersummary

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.OrderSummaryAdapter
import com.getpy.dikshasshop.data.db.AppDataBase
import com.getpy.dikshasshop.data.model.CustomerInvoiceData
import com.getpy.dikshasshop.data.model.InvocieLineItems
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityOrderSummaryBinding
import com.getpy.dikshasshop.databinding.OkCustomDialogBinding
import com.getpy.dikshasshop.ui.main.MainActivity
import com.getpy.dikshasshop.ui.orderstatus.OrderStatusActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.HashMap


class OrderSummaryActivity : AppCompatActivity(),KodeinAware{
    override val kodein by kodein()
    private val appDataBase: AppDataBase by instance()
    private val factory: OrderSumModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    lateinit var binding:ActivityOrderSummaryBinding
    lateinit var viewmodel:OrderSummaryViewModel
    var cmodel:CustomerInvoiceData?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_order_summary)
        viewmodel=ViewModelProviders.of(this,factory).get(OrderSummaryViewModel::class.java)
        AppCenter.start(
            application, "9e64f71e-a876-4d54-a2ce-3c4c1ea86334",
            Analytics::class.java, Crashes::class.java
        )

        val ilist=intent.extras?.getParcelableArrayList<InvocieLineItems>("list")
        cmodel=intent.extras?.getParcelable<CustomerInvoiceData>("model")

        binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.setItemAnimator(null)
        val adapter= ilist?.let { cmodel?.let { it1 -> OrderSummaryAdapter(supportFragmentManager,this, it, it1) } }
        binding.recyclerview.adapter=adapter
        binding.totItemsValue.text=ilist?.size.toString()
        binding.totMrpValue.text=cmodel?.TotalInvoiceAmount
        binding.discountValue.text=cmodel?.DiscountAmount
        if(cmodel?.LabourAmount.isNullOrEmpty()){
            binding.deliveryChargesValue.text="Free"
        }
        else{
            binding.deliveryChargesValue.text=cmodel?.LabourAmount
        }
        binding.paymentModValue.text=cmodel?.PaymentMode
        binding.orderSummaryGstValue.text = cmodel?.TaxAmount
        binding.totalValue.text=cmodel?.PayableAmount

        if(cmodel?.TypeOfRoom.toString().toLowerCase() == "yes"){
            binding.cancelOrder.visibility = View.VISIBLE
        }
        else {
            binding.cancelOrder.visibility = View.GONE
            binding.cancelOrder.isEnabled = false
        }
        binding.reorder.setOnClickListener {
            callReorderService()
        }
        binding.cancelOrder.setOnClickListener {
            okDialogWithCancelOrder("Cancel Order","Are you sure you want to cancel the order?")
        }
        getMerchantAppSettingDetails()
        binding.image.setOnClickListener(View.OnClickListener {
            finish()
        })
    }
    fun init()
    {
        binding.ordSummary.setTypeface(UbboFreshApp.instance?.latobold)
        binding.orderDetails.setTypeface(UbboFreshApp.instance?.latobold)
        binding.totItems.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.totItemsValue.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.totMrp.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.totMrpValue.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.discount.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.discountValue.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.deliveryCharges.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.deliveryChargesValue.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.paymentMod.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.paymentModValue.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.toal.setTypeface(UbboFreshApp.instance?.latobold)
        binding.totalValue.setTypeface(UbboFreshApp.instance?.latobold)
    }

    fun callReorderService()
    {
        val jsonobject= JSONObject()
        jsonobject.put("merchant_id", preference.getIntData(Constants.saveMerchantIdKey))
        jsonobject.put("phone_number",preference.getStringData(Constants.saveMobileNumkey))
        jsonobject.put("access_key",preference.getStringData(Constants.saveaccesskey))
        jsonobject.put("invoice_id",cmodel?.InvoiceId)
        lifecycleScope.launch {
            try {
                val response= cmodel?.InvoiceId?.let {

                    //analytics
                    val map1= HashMap<String,String>()
                    map1.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
                    map1.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
                    map1.put("Previous Order Invoice Id",cmodel?.InvoiceId.toString())
                    Analytics.trackEvent("Reorder Clicked", map1)
                    viewmodel.reorderInvoiceItems(
                            preference.getIntData(Constants.saveMerchantIdKey),
                            preference.getStringData(Constants.saveMobileNumkey),
                            preference.getStringData(Constants.saveaccesskey),
                            it)
                }
                //UbboFreshApp.instance?.carItemsList= ArrayList<ProductsDataModel>()
                for(i in 0 until response?.data!!.size)
                {
                    val model=response.data?.get(i)
                    model?.itemCount= 1
                    model?.mobileNumber=preference.getStringData(Constants.saveMobileNumkey)
                    if(!UbboFreshApp.instance?.hashMap!!.containsKey(model?.citrineProdId))
                    {
                        model?.citrineProdId?.let { UbboFreshApp.instance?.hashMap?.put(it,model) }
                        model?.let { UbboFreshApp.instance?.carItemsList?.add(it) }
                    }
                }

                UbboFreshApp.instance?.isComingFromReOrder=true
                val intent = Intent(this@OrderSummaryActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }catch (e: NoInternetExcetion)
            {
                MainActivity.binding.coordinateLayout.snakBar("Please check network")
                //activity?.networkDialog()
            }catch (e:CancellationException)
            {
                Log.i("scope","job is canceled")
            }
            catch (e:Exception)
            {
                okDialogWithOneAct("Error",e.message.toString())
            }
        }
    }

    fun cancelOrder(CurrentOrderInvoiceId : String?,
                    orderStatus: String){
        lifecycleScope.launch(){
            try{
                val pdata =  viewmodel.updateStatusAfterPayment(
                    preference.getIntData(Constants.saveMerchantIdKey),
                    preference.getStringData(Constants.saveMobileNumkey),
                    preference.getStringData(Constants.saveaccesskey),
                    CurrentOrderInvoiceId.toString(),
                    "",
                    orderStatus)
                val map=HashMap<String,String>()
                if(pdata!= null){
                    try {
                        map.put("Cancel Request", "$CurrentOrderInvoiceId , $orderStatus")
                        map.put("Status", pdata.status.toString())
                        map.put("Message", pdata.data?.message.toString())
                        map.put("API Message", pdata.data?.details.toString())
                        Analytics.trackEvent("Cancel Order Request", map)
                        if(pdata.status.equals("Success") || pdata.status.equals("Sucess")){
                            //okDialog("Cancel Order",pdata.data?.message?:"Order successfully cancelled.")
                            okDialog("Cancel Order","Order successfully cancelled.")

                            //popup and reload the page
                        }
                        else  {
                            //error msg and reload the page
                            //okDialogWithOneAct("Cancel Order",pdata.data?.message?:"Unable to cancel the order.")
                            okDialogWithOneAct("Cancel Order","Unable to cancel the order.")
                        }
                    }catch (e: NoInternetExcetion)
                    {
                        //binding.pbar.dismiss()
                        networkDialog()
                    }catch (e:CancellationException)
                    {
                        //binding.pbar.dismiss()
                        Log.i("scope","job is canceled")
                    }
                    catch (e:Exception)
                    {
                        map.put("Status", "Failure due to Exception")
                        map.put("Exception Message", e.message.toString())
                        Analytics.trackEvent("Cancel Order Request", map)
                        //binding.pbar.dismiss()
                        okDialogWithOneAct("Error CO01",e.message.toString())
                    }
                }
                else{
                    map.put("Status", "NULL as response")
                    Analytics.trackEvent("Cancel Order Request", map)
                    //error
                    startActivity(intent)
                }
            }
            catch (e:Exception)
            {
                okDialogWithOneAct("Error CO02",e.message.toString())
            }
        }
    }

    fun getMerchantAppSettingDetails() {

        val jsonobject=JSONObject()
        jsonobject.put("access_key",preference.getStringData(Constants.saveaccesskey))
        jsonobject.put("phone_number",preference.getStringData(Constants.saveMobileNumkey))
        jsonobject.put("merchant_id", preference.getIntData(Constants.saveMerchantIdKey))
        Log.i("getMerAppSetDetails",jsonobject.toString())


        lifecycleScope.launch {
            try {
                val mdata = viewmodel.merchantAppSettingDetails(
                    preference.getIntData(Constants.saveMerchantIdKey),
                    "TypeOfRoom",
                    preference.getStringData(Constants.saveMobileNumkey),
                    preference.getStringData(Constants.saveaccesskey))
                val temp = 0
            } catch (e: NoInternetExcetion) {
                networkDialog()
            }catch (e:CancellationException)
            {
                Log.i("scope","job is cancelled")
            }
            catch (e: Exception) {
                okDialogWithOneAct("Error MI01.1", e.message.toString())
            }
        }
    }
    fun okDialogWithCancelOrder(title:String,message:String)
    {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val binding : OkCustomDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this), R.layout.ok_custom_dialog, null, false);
        dialog.setContentView(binding.root)
        binding.header.text = title
        binding.message.text=message
        binding.okText.text="Yes"
        binding.okText.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.header.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.message.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.okText.setOnClickListener {
            dialog.dismiss()
            cancelOrder(cmodel?.CustomerInvoiceId,"Cancelled")
        }
        binding.cancelText.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        if(this!=null)
            dialog.show()
    }
    fun okDialog(title:String,message:String)
    {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val binding :OkCustomDialogBinding= DataBindingUtil.inflate(
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