package com.getpy.dikshasshop.ui.cart.cartactivities

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface.BOLD_ITALIC
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cashfree.pg.CFPaymentService
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.CustomerAddressAdapter
import com.getpy.dikshasshop.data.db.AppDataBase
import com.getpy.dikshasshop.data.db.entities.CustomerAddressData
import com.getpy.dikshasshop.data.model.GetDistanceData
import com.getpy.dikshasshop.data.model.Merchantdata
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityCheckOutBinding
import com.getpy.dikshasshop.databinding.OkCustomDialogBinding
import com.getpy.dikshasshop.ui.cart.CartViewModel
import com.getpy.dikshasshop.ui.cart.CartViewModelFactory
import com.getpy.dikshasshop.ui.orderstatus.OrderStatusActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.android.synthetic.main.activity_check_out.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

class CheckOutActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val database: AppDataBase by instance()
    private val factory: CartViewModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    lateinit var binding: ActivityCheckOutBinding
    lateinit var viewModel: CartViewModel
    var adapter:CustomerAddressAdapter?=null
    var customerAddressData:CustomerAddressData?=null
    var deliveryCharges : String? = "0.00"
    var couponDiscountAmount : Double = 0.0
    var currentAddressId : String? = ""
    var cashfreeOrderId : String? = null
    var CurrentOrderInvoiceId : String = ""
    var merchantAppSetData:Merchantdata?=null
    private var getDistanceData:GetDistanceData?=null
    var latitude: String? = null
    var longitude: String? = null
    var city: String = ""
    var state: String = ""
    var country: String = ""
    var postalCode: String = ""
    var address: String = ""
    var knownName: String = ""
    var totalMRP: String = ""
    var totalPayablePrice: String = ""
    var totalTax: String = ""
    var discount: String = ""
    var appId: String = ""
    var SecretKey: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_check_out)
        viewModel = ViewModelProviders.of(this, factory).get(CartViewModel::class.java)
        binding.recyclerview.setHasFixedSize(true)

        LocalBroadcastManager.getInstance(this@CheckOutActivity).registerReceiver(mMessageReceiver, IntentFilter("custom-message"));
        AppCenter.start(
            application, "9e64f71e-a876-4d54-a2ce-3c4c1ea86334",
            Analytics::class.java, Crashes::class.java
        )

        binding.addNewAddr.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AddAddressActivity::class.java)
            intent.putExtra("discount",discount)
            intent.putExtra("callType","add")
            startActivity(intent)
        })
        //coupon code button
        binding.couponButton.setOnClickListener {
            //Redirect
            val intent = Intent(this, CouponPageActivity::class.java)
            intent.putExtra("TotalAmount", totalMRP)
            startActivity(intent)
        }
        //remove Coupon Function
        binding.couponRemoveButton.setOnClickListener{
            UbboFreshApp.instance?.couponDiscontAmount= null
            UbboFreshApp.instance?.couponApplied= null
            onResume()
        }

        binding.icBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
        binding.placeOrder.setOnClickListener(View.OnClickListener {
            binding.placeOrder.isEnabled=false
            Handler().postDelayed({ binding.placeOrder.setEnabled(true) }, 5000)
            if(adapter?.getModel()!=null) {
                customerAddressData=adapter?.getModel()
                if (!adapter?.getModel()!!.ischecked) {
                    okDialogWithOneAct(Constants.AlertBoxHeader, "No delivery address selected")
                    if(!binding.placeOrder.isEnabled)
                        binding.placeOrder.setEnabled(true)
                    return@OnClickListener
                }
            }else
            {
                okDialogWithOneAct(Constants.AlertBoxHeader, "No delivery address selected")
                if(!binding.placeOrder.isEnabled)
                    binding.placeOrder.setEnabled(true)
                return@OnClickListener
            }
            if(binding.paymentRadio.getCheckedRadioButtonId() == -1)
            {
                okDialogWithOneAct(Constants.AlertBoxHeader,"Please select payment option")
                if(!binding.placeOrder.isEnabled)
                    binding.placeOrder.setEnabled(true)
                return@OnClickListener
            }

            if(merchantAppSetData?.SettingValue?.toDouble()!! >totalPayablePrice.toDouble())
            {
                okDialogWithOneAct(Constants.AlertBoxHeader,merchantAppSetData?.SettingMessage.toString())
                if(!binding.placeOrder.isEnabled)
                    binding.placeOrder.setEnabled(true)
                return@OnClickListener
            }
            getDistance(customerAddressData?.Latitude, customerAddressData?.Longitude);
        })

        init()
    }

    //Function to get Delivery Charges
    private fun getDeliveryCharges(selectedAddressId: String?) {
        lifecycleScope.launch {
            try {
                checkCouponStatus()
                currentAddressId = selectedAddressId
                binding.totalPayablePrice.setText(totalPayablePrice)
                binding.delChargesValue.text = "0.00"
                deliveryCharges="0.00"
                val response = viewModel.getDeliveryCharges(
                    preference.getIntData(Constants.saveMerchantIdKey),
                    preference.getStringData(Constants.saveaccesskey),
                    preference.getStringData(Constants.saveMobileNumkey),
                        selectedAddressId.toString()
                )
                val thrashHold = response.data?.DeliveryThresholdAmount
                val deliveryChargesFromAPI = response.data?.DeliveryCharge

                if (thrashHold != null) {
                    try {
                        if(totalPayablePrice.toFloat()< thrashHold.toFloat()){
                            val TotalPayableAmount: Float
                            if (deliveryChargesFromAPI != null) {
                                deliveryCharges = deliveryChargesFromAPI.toString()
                                TotalPayableAmount = totalPayablePrice.toFloat()+deliveryChargesFromAPI.toFloat()
                                totalPayablePrice = TotalPayableAmount.toString()
                                binding.totalPayablePrice.setText(totalPayablePrice)
                                binding.delChargesValue.setText(deliveryCharges)
                            }
                        }
                    }
                    catch (ex: Exception)
                    {
                        Toast.makeText(
                            this@CheckOutActivity,
                            "Server Error DC01: Unable to calculate delivery charges",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            } catch (E: Exception) {
                E.printStackTrace()
            }
        }

    }

    fun init() {
        binding.checkout.setTypeface(UbboFreshApp.instance?.latobold)
        binding.deliverto.setTypeface(UbboFreshApp.instance?.latobold)
        binding.paymentOption.setTypeface(UbboFreshApp.instance?.latobold)
        binding.payondeltext.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.priceDetails.setTypeface(UbboFreshApp.instance?.latobold)
        binding.priceDetailsValue.setTypeface(UbboFreshApp.instance?.latobold)
        binding.total.setTypeface(UbboFreshApp.instance?.latobold)
        binding.totalPayablePrice.setTypeface(UbboFreshApp.instance?.latobold)
        binding.placeOrder.setTypeface(UbboFreshApp.instance?.latoregular)

        binding.totMrp.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.totMrpValue.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.discount.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.discountValue.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.couponDiscount.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.couponDiscountValue.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.delCharges.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.delChargesValue.setTypeface(UbboFreshApp.instance?.latoregular)


        totalPayablePrice = intent.getStringExtra("totalPayablePrice") ?: "0.0"
        totalMRP = intent.getStringExtra("totalMRP") ?: "0.0"
        totalTax = intent.getStringExtra("totalTax") ?: "0.0"
        discount = intent.getStringExtra("discount") ?: "0.0"
        latitude = intent.getStringExtra("lat") ?: null
        longitude = intent.getStringExtra("lng") ?: null
        city = intent.getStringExtra("city") ?: ""
        state = intent.getStringExtra("state") ?: ""
        country = intent.getStringExtra("country") ?: ""
        postalCode = intent.getStringExtra("postalCode") ?: ""
        address = intent.getStringExtra("address") ?: ""

        //reference text style
        val spannable = SpannableString("Were you referred? Please click on 'Apply Coupon' to enter your reference code.")
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#FFB02B18")),
            0, 18,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            StyleSpan(BOLD_ITALIC),
            0, 18,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#FF5E5E5E")),
            19, spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


        binding.refText.text = spannable

        binding.totMrpValue.setText(totalMRP)
        binding.gstValue.setText(totalTax)
        binding.delChargesValue.text = "0.00"
        binding.totalPayablePrice.setText(totalPayablePrice)
        binding.discountValue.setText(discount)
        // getDistance()
        //getMerchantAppSettingDetails()

        getMerchantAppSettingDetails()
        getPaymentModeDetails()

    }


    override fun onResume() {
        super.onResume()
        binding.pbar.show()
        getCustomerAddress()
        binding.delChargesValue.text = "0.00"
        //checkCouponStatus called
        checkCouponStatus()
    }
    override fun onBackPressed() {
        UbboFreshApp.instance?.couponDiscontAmount = null
        UbboFreshApp.instance?.couponApplied = ""
        super.onBackPressed()
    }
    private fun checkCouponStatus() {
        if( UbboFreshApp.instance?.couponDiscontAmount != null && UbboFreshApp.instance?.couponDiscontAmount != 0.0){
            couponDiscountAmount = UbboFreshApp.instance?.couponDiscontAmount!!
            binding.couponButton.text = "Change Coupon"
            binding.selectedCouponLayout.visibility = View.VISIBLE
            val selectedCoupon ="Coupon Applied: " + UbboFreshApp.instance?.couponApplied
            binding.selectedCouponText.text = selectedCoupon
            totalPayablePrice = intent.getStringExtra("totalPayablePrice") ?: "0.0"
            val calculatedTotalPayablePrice = (totalPayablePrice.toDouble() - couponDiscountAmount)
            binding.couponDiscountValue.text = couponDiscountAmount.toString()
            totalPayablePrice = String.format("%.2f",calculatedTotalPayablePrice)
            binding.totalPayablePrice.text = String.format("%.2f",calculatedTotalPayablePrice)
        }
        else{
            totalPayablePrice = intent.getStringExtra("totalPayablePrice") ?: "0.0"
            binding.totalPayablePrice.text = totalPayablePrice
            binding.couponDiscountValue.text = "Not Applied"
            binding.couponButton.text = "Apply Coupon"
            binding.selectedCouponLayout.visibility = View.GONE
            val selectedCoupon ="Coupon Applied: "
        }
    }

    var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val selectedAddressId = intent.getStringExtra("addressId")
            getDeliveryCharges(selectedAddressId)
        }
    }


    private fun getCfInputParameter(thisOrderAmount: String): Map<String, String>? {

        //for orderId
        val current = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val formattedDate =formatter.format(current)
        /*
         * appId will be available to you at CashFree Dashboard. This is a unique
         * identifier for your app. Please replace this appId with your appId.
         * Also, as explained below you will need to change your appId to prod
         * credentials before publishing your app.
         */
        val appId = this.appId
        val orderId = "Order_" + preference.getIntData(Constants.saveMerchantIdKey).toString() + "_" + formattedDate
        val orderNote = "Test Order"
        val customerName = "Customer"
        val customerPhone = preference.getStringData(Constants.saveMobileNumkey)
        val customerEmail = "customer@test.com"
        val params: MutableMap<String, String> = HashMap()
        params[CFPaymentService.PARAM_APP_ID] = appId
        params[CFPaymentService.PARAM_ORDER_ID] = orderId
        params[CFPaymentService.PARAM_ORDER_AMOUNT] = thisOrderAmount
        params[CFPaymentService.PARAM_ORDER_NOTE] = orderNote
        params[CFPaymentService.PARAM_CUSTOMER_NAME] = customerName
        params[CFPaymentService.PARAM_CUSTOMER_PHONE] = customerPhone
        params[CFPaymentService.PARAM_CUSTOMER_EMAIL] = customerEmail
        params[CFPaymentService.PARAM_ORDER_CURRENCY] = "INR"
        return params
    }


    private fun getPaymentModeDetails() {
        lifecycleScope.launch {
            try {
                val response = viewModel.getPaymentModeDetails(
                    preference.getIntData(Constants.saveMerchantIdKey),
                    preference.getStringData(Constants.saveMobileNumkey),
                    preference.getStringData(Constants.saveaccesskey)
                )
                if(!response.apiId.toString().isNullOrEmpty()){
                    appId = response.apiId.toString()
                    SecretKey = response.SecretKey.toString()
                    pay_online_checkbox.visibility= View.VISIBLE
                }

            } catch (E: Exception) {
                E.printStackTrace()
            }
        }

    }

    //function to get customer address
    fun getCustomerAddress() {

        lifecycleScope.launch {

            try {
                //Fetch Address List from API
                val response = viewModel.getCustomerAddress(
                    preference.getIntData(Constants.saveMerchantIdKey),
                    preference.getStringData(Constants.saveMobileNumkey),
                    preference.getStringData(Constants.saveaccesskey))
                launch {
                    try {
                        //Add Address to local DB
                        response.data?.let { database.CustomerAddressDao().insertcustomerAddrData(it) }

                        //load Adapter from local DB address
                        loadAddressesFromLocalDB()
                    } catch (E: Exception) {
                        E.printStackTrace()
                        //Load Adapter from API data
                        loadAddressFromResponse(response.data)
                    }
                }
            }catch (e: NoInternetExcetion) {
                networkDialog()
            }catch (e:CancellationException)
            {
                Log.i("scope","job is canceled")
                //Crash Analytics
                val map1=HashMap<String,String>()
                map1.put("Error Message",e.message.toString())
                map1.put("Stack Trace",e.printStackTrace().toString().take(150))
                Analytics.trackEvent("Address Loading Error",map1)
            }
            catch (ex: Exception) {
                //Crash Analytics
                val map1=HashMap<String,String>()
                map1.put("Error From","While calling getCustomerAddress API")
                map1.put("Error Message",ex.message.toString())
                map1.put("Stack Trace",ex.printStackTrace().toString().take(150))
                Analytics.trackEvent("Address Loading Error",map1)
                okDialogWithOneAct("Error CA01", ex.message.toString())
            }
            binding.pbar.dismiss()
        }
    }

    suspend fun loadAddressesFromLocalDB(){
        val list = database.CustomerAddressDao().getCustAddrData(
            preference.getStringData(Constants.saveMobileNumkey),
            preference.getIntData(Constants.saveMerchantIdKey))
        //Adjust Interface as per Address Data
        if (list.size == 0) {
            binding.line.visibility = View.GONE
        } else {
            binding.line.visibility = View.VISIBLE
        }
        if (list.size == 3) {
            binding.addNewAddr.visibility = View.GONE
        }
        adapter = list.let { CustomerAddressAdapter(this@CheckOutActivity, it) }
        binding.recyclerview.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    fun loadAddressFromResponse(data :ArrayList<CustomerAddressData>?){
        try {
            val list = data
            list?.let {
                //Adjust Interface as per Address Data
                if (it.size == 0) {
                    binding.line.visibility = View.GONE
                } else {
                    binding.line.visibility = View.VISIBLE
                }
                if (it.size == 3) {
                    binding.addNewAddr.visibility = View.GONE
                }
            }
            adapter = list?.toList()?.let {
                CustomerAddressAdapter(this@CheckOutActivity,
                    it
                )
            }
            binding.recyclerview.adapter = adapter
            adapter?.notifyDataSetChanged()
        }
        catch (ex : java.lang.Exception){
            ex.printStackTrace()
            //Crash Analytics
            val map1=HashMap<String,String>()
            map1.put("Error Message",ex.message.toString())
            map1.put("Stack Trace",ex.printStackTrace().toString().take(150))
            Analytics.trackEvent("Address Loading Error",map1)
            Toast.makeText(
                this@CheckOutActivity,
                "Error while loading address",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getDistance(lat: String?, long: String?) {
        lifecycleScope.launch {
            try {
                val getDistanceResponse = viewModel.getDeliveryCharges(
                    preference.getIntData(Constants.saveMerchantIdKey),
                    preference.getStringData(Constants.saveaccesskey),
                    preference.getStringData(Constants.saveMobileNumkey),
                        currentAddressId.toString())
                getDistanceData=getDistanceResponse.data
                //Code for "Distance check" pop up
                if(getDistanceData?.active?.toLowerCase().equals("yes"))
                {
                    if(getDistanceData?.deliverable?.toLowerCase().equals("yes"))
                    {
                        getDistanceData?.message?.let { it1 -> okDialog(Constants.AlertBoxHeader, it1)}
                        return@launch
                    }
                }
                if(getDistanceData?.active?.toLowerCase().equals("yes"))
                {
                    if(getDistanceData?.deliverable?.toLowerCase().equals("no"))
                    {
                        getDistanceData?.message?.let { it1 -> okDialogWithOneAct(Constants.AlertBoxHeader, it1)}
                        if(!binding.placeOrder.isEnabled)
                            binding.placeOrder.setEnabled(true)
                        return@launch
                    }
                }
                processPaymentMode()
            } catch (e: NoInternetExcetion) {
                networkDialog()
                if(!binding.placeOrder.isEnabled)
                    binding.placeOrder.setEnabled(true)
            }catch (e:CancellationException)
            {
                Log.i("scope","job is canceled")
                if(!binding.placeOrder.isEnabled)
                    binding.placeOrder.setEnabled(true)
            }
            catch (e: Exception) {
                if(!binding.placeOrder.isEnabled)
                    binding.placeOrder.setEnabled(true)
                okDialogWithOneAct("Error DC01", e.message.toString())
            }
        }
    }

    private fun processPaymentMode() {
        val createOrderLoadingBox = displayLoadingBox("Please wait...")
        createOrderLoadingBox.show()
        if(binding.payOnDeliveryCheckbox.isChecked){
            createOrderWithPOD(createOrderLoadingBox)
        }
        else if(binding.payOnlineCheckbox.isChecked){
            createOrderWithOnlinePayment(createOrderLoadingBox)
        }
    }

    private fun createOrderWithPOD(createOrderLoadingBox: AlertDialog) {
        placeOrder(paymentMode = "COD", createOrderLoadingBox = createOrderLoadingBox)
    }

    private fun createOrderWithOnlinePayment(createOrderLoadingBox: AlertDialog) {
        //Update Order in Database before processing payment
        placeOrder("Online","Payment Pending",createOrderLoadingBox)
        //if true call redirectToCashfreeGateway
    }


    //function to process placeOrder API
    fun placeOrder(paymentMode: String, orderStatus: String = "New", createOrderLoadingBox: AlertDialog) {

        //Create json object to pass as payload for createOrder APi
        val jsonObject=JsonObject()
        val order_details=JsonObject()
        val Invoice=JsonObject()
        val InvoiceItem=JsonArray()
        jsonObject.addProperty("access_key",preference.getStringData(Constants.saveaccesskey))
        jsonObject.addProperty("phone_number",preference.getStringData(Constants.saveMobileNumkey))
        jsonObject.addProperty("merchant_id",preference.getIntData(Constants.saveMerchantIdKey))
        Invoice.addProperty("DiscountAmount",couponDiscountAmount)
        Invoice.addProperty("TaxAmount",totalTax)
        Invoice.addProperty("TotalInvoiceAmount",totalMRP)
        Invoice.addProperty("DeliveryCharge", deliveryCharges)
        Invoice.addProperty("CouponCode",UbboFreshApp.instance?.couponApplied)
        Invoice.addProperty("PayableAmount",totalPayablePrice)
        Invoice.addProperty("InvoiceType","GetPYApp")
        Invoice.addProperty("OrderStatus",orderStatus)
        Invoice.addProperty("PaymentMode",paymentMode)
        Invoice.addProperty("DeliverAddressId",customerAddressData?.ID)
        Invoice.addProperty("PaymentOrderId","")
        Invoice.addProperty("DeliveryInstruction",UbboFreshApp.instance?.instructionString)
        order_details.add("Invoice",Invoice)
        var productNameListForAnalytics = "("
        for(i in 0 until UbboFreshApp.instance?.carItemsList!!.size)
        {
            val InvoiceItemObj=JsonObject()
            val model=UbboFreshApp.instance?.carItemsList?.get(i)
            InvoiceItemObj.addProperty("ProductId",model?.citrineProdId)
            InvoiceItemObj.addProperty("quantity",model?.itemCount)
            InvoiceItemObj.addProperty("ProductName",model?.productName)
            productNameListForAnalytics +=  model?.productName.toString() + ","
            if(model?.mrp!=null && model.sellingPrice!=null)
            {
                InvoiceItemObj.addProperty("UnitPrice",model.mrp)
                InvoiceItemObj.addProperty("Discount",(model.mrp?.toDouble()?.minus(model.sellingPrice.toDouble()))?.div(model.mrp?.toDouble()!!))
                InvoiceItemObj.addProperty("UnitPriceAfterDiscount",model.sellingPrice)
                InvoiceItemObj.addProperty("TotalPrice",(model.itemCount.times(model.sellingPrice.toDouble())))
            }
            var url=""
            if(model?.imageLinkFlag!=null) {
                url = if (model.imageLinkFlag.equals("R")) {
                    UbboFreshApp.instance?.imageLoadUrl+model.productPicUrl
                } else {
                    model.productPicUrl
                }
            }else
            {
                if(model?.productPicUrl!=null)
                {
                    url=UbboFreshApp.instance?.imageLoadUrl+model.productPicUrl
                }else
                {
                    url= UbboFreshApp.instance?.imageLoadUrl.toString()
                }

            }
            InvoiceItemObj.addProperty("ProductImage",url)
            InvoiceItemObj.addProperty("Category",model?.category)
            InvoiceItem.add(InvoiceItemObj)
        }
        productNameListForAnalytics += ")"
        order_details.add("InvoiceItem",InvoiceItem)
        jsonObject.add("order_details",order_details)

        //call analytics for order placement attempt

        val map1=HashMap<String,String>()
        map1.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
        map1.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
        map1.put("PayableAmount", totalPayablePrice)
        map1.put("DiscountAmount", discount)
        map1.put("TaxAmount", totalTax)
        map1.put("TotalInvoiceAmount", totalMRP)
        map1.put("Payment Mode", paymentMode)
        map1.put("TaxAmount", totalTax)
        map1.put("DeliveryCharge", deliveryCharges.toString())
        map1.put("Products", productNameListForAnalytics)
        map1.put("TaxAmount", totalTax)
        Analytics.trackEvent("New Order Request", map1)
        val tempp = jsonObject.toString()

        //binding.pbar.show()

        //Send analytics
           val map=HashMap<String,String>()
        map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
        map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
        map.put("PaymentMode", paymentMode)
        map.put("TotalAmount", totalPayablePrice)
        //payment mode dependent Code
        var continueToPaymentPage = false
            lifecycleScope.launch {
                try {
                    val response=viewModel.createOrder(jsonObject)
                    //binding.pbar.dismiss()
                    UbboFreshApp.instance?.instructionString=""
                    UbboFreshApp.instance?.couponDiscontAmount = null
                    if(response.status.equals("Success")) {
                        map.put("Status", response.status.toString())
                        map.put("Order Message", response.data?.message.toString())
                        map.put("API Message", response.data?.details.toString())
                        Analytics.trackEvent("New Order Processed", map)
                        if(paymentMode == "COD"){
                            val intent = Intent(this@CheckOutActivity, OrderStatusActivity::class.java)
                            intent.putExtra("message", response.data?.message)
                            intent.putExtra("caption", response.data?.caption)
                            intent.putExtra("status", "success")
                            if(!binding.placeOrder.isEnabled)
                                binding.placeOrder.setEnabled(true)
                            if(createOrderLoadingBox.isShowing)
                                createOrderLoadingBox.dismiss()
                            startActivity(intent)
                        }
                        if(paymentMode == "Online"){
                            CurrentOrderInvoiceId= response.data?.orderInvoiceId.toString()
                            redirectToCashfreeGateway(createOrderLoadingBox)
                        }
                    }
                    else  {
                        map.put("Status", response.status.toString())
                        map.put("Order Message", response.data?.message.toString())
                        map.put("API Message", response.data?.details.toString())
                        Analytics.trackEvent("New Order Processed", map)
                        val intent=Intent(this@CheckOutActivity, OrderStatusActivity::class.java)
                        intent.putExtra("message",response.data?.message)
                        intent.putExtra("caption",response.data?.caption)
                        intent.putExtra("status",response.status)
                        if(!binding.placeOrder.isEnabled)
                            binding.placeOrder.setEnabled(true)
                        if(createOrderLoadingBox.isShowing)
                            createOrderLoadingBox.dismiss()
                        startActivity(intent)
                    }
                }catch (e: NoInternetExcetion)
                {
                    //binding.pbar.dismiss()
                    if(!binding.placeOrder.isEnabled)
                        binding.placeOrder.setEnabled(true)
                    networkDialog()
                }catch (e:CancellationException)
                {
                    //binding.pbar.dismiss()
                    if(!binding.placeOrder.isEnabled)
                        binding.placeOrder.setEnabled(true)
                    Log.i("scope","job is canceled")
                }
                catch (e:Exception)
                {
                    map.put("Status", "Failure due to Exception")
                    map.put("Exception Message", e.message.toString())
                    Analytics.trackEvent("New Order Processed", map)
                    if(!binding.placeOrder.isEnabled)
                        binding.placeOrder.setEnabled(true)
                    //binding.pbar.dismiss()
                    okDialogWithOneAct("Error OP01",e.message.toString())
                }
                finally {
                    if(createOrderLoadingBox.isShowing)
                        createOrderLoadingBox.dismiss()
                }
        }
    }


    private fun redirectToCashfreeGateway(createOrderLoadingBox: AlertDialog){//get cashfree payload values
        val inputParma = getCfInputParameter(totalPayablePrice)
        cashfreeOrderId = inputParma?.get(CFPaymentService.PARAM_ORDER_ID).toString()
        val orderId = inputParma?.get(CFPaymentService.PARAM_ORDER_ID).toString()
        //call CfTokenGeneration Code
        lifecycleScope.launch {
            try {
                val str : String? = null
                //val len = str!!.length
                val response = viewModel.cfTokenGenerator(
                        preference.getIntData(Constants.saveMerchantIdKey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        preference.getStringData(Constants.saveaccesskey),
                        totalPayablePrice,
                        orderId,
                        "INR"
                )
                val cfToken = response.cftoken
                val stage = response.stage
                if(createOrderLoadingBox.isShowing)
                    createOrderLoadingBox.dismiss()
                val cfPaymentService = CFPaymentService.getCFPaymentServiceInstance()
                cfPaymentService.setOrientation(0)
                cfPaymentService.doPayment(
                        this@CheckOutActivity,
                        inputParma,
                        cfToken,
                        stage,
                        "#784BD2",
                        "#FFFFFF",
                        false
                )
            } catch (E: Exception) {
                okDialogWithOneAct(Constants.AlertBoxHeader, "Something went wrong while opening payment gateway.Please try later.")
                Log.i("scope","job is canceled")
            }
            finally {
                if(createOrderLoadingBox.isShowing)
                    createOrderLoadingBox.dismiss()
            }
        }
    }


    //payment gateway Response
    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        //Same request code for all payment APIs.
        Log.d("Tag", "ReqCode : " + CFPaymentService.REQ_CODE)
        Log.d("TAG", "API Response : ")
        //Prints all extras. Replace with app logic.
        if (data != null) {
            val bundle = data.extras
            val map=HashMap<String,String>()
            map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
            map.put("merchantId", preference.getIntData(Constants.saveMerchantIdKey).toString())
            map.put("Order Invoice ID", CurrentOrderInvoiceId )
            if (bundle != null)
                for (key in bundle.keySet()) {
                    if (bundle.getString(key) != null) {
                        //Analytics for payment Response
                        map.put(key, bundle.getString(key).toString())
                        //Print Payment Response in Console
                        Log.d("TAGG", key + " : " + bundle.getString(key))
                    }
                }
            Analytics.trackEvent("Payment Response", map)
            if (bundle != null) {
                if(bundle.getString("txStatus") =="SUCCESS"){
                    updatePaymentToDatabase(bundle.getString("orderId").toString(), orderStatus="New")
                }
                else{
                    okDialogWithOneAct(Constants.AlertBoxHeader, "Transaction Failed.Unable to process Order.Please try again.")
                    if(!binding.placeOrder.isEnabled)
                        binding.placeOrder.setEnabled(true)
                }
            }
        }
    }


    private fun updatePaymentToDatabase( orderPaymentId: String, orderStatus : String){
        lifecycleScope.launch(){
            try{
                val pdata =  viewModel.updateStatusAfterPayment(
                            preference.getIntData(Constants.saveMerchantIdKey),
                            preference.getStringData(Constants.saveMobileNumkey),
                            preference.getStringData(Constants.saveaccesskey),
                            CurrentOrderInvoiceId,
                            orderPaymentId,
                            orderStatus)
                if(!binding.placeOrder.isEnabled)
                    binding.placeOrder.setEnabled(true)
                val map=HashMap<String,String>()
                if(pdata!= null){
                    try {
                        map.put("Request Params", "$CurrentOrderInvoiceId , $orderPaymentId , $orderStatus")
                        map.put("Status", pdata.status.toString())
                        map.put("Message", pdata.data?.message.toString())
                        map.put("API Message", pdata.data?.details.toString())
                        Analytics.trackEvent("Update Order Status", map)
                        if(pdata.status.equals("Success") || pdata.status.equals("Sucess")){
                            val intent=Intent(this@CheckOutActivity,OrderStatusActivity::class.java)
                            intent.putExtra("message",pdata.data?.message)
                            intent.putExtra("caption",pdata.data?.caption)
                            intent.putExtra("status","success")
                            startActivity(intent)
                        }
                        else  {
                            val intent=Intent(this@CheckOutActivity, OrderStatusActivity::class.java)
                            intent.putExtra("message",pdata.data?.message)
                            intent.putExtra("caption",pdata.data?.caption)
                            intent.putExtra("status",pdata.status)
                            startActivity(intent)
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
                        Analytics.trackEvent("Update Order Status", map)
                        //binding.pbar.dismiss()
                        okDialogWithOneAct("Error UP01",e.message.toString())
                    }
                }
                else{
                    map.put("Status", "NULL as response")
                    Analytics.trackEvent("Update Order Status", map)
                    val intent=Intent(this@CheckOutActivity, OrderStatusActivity::class.java)
                    intent.putExtra("status","Failure")
                    intent.putExtra("caption","Server Error")
                    intent.putExtra("message","Unable to reach server while Processing the Payment.")
                    startActivity(intent)
                }
            }
            catch (e:Exception)
            {
                binding.pbar.dismiss()
                okDialogWithOneAct("Error UP02",e.message.toString())
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
                val mdata = viewModel.merchantAppSettingDetails(
                    preference.getIntData(Constants.saveMerchantIdKey),
                    "Amount",
                    preference.getStringData(Constants.saveMobileNumkey),
                    preference.getStringData(Constants.saveaccesskey))
                merchantAppSetData=mdata.data?.merchantdata
                binding.priceDetailsValue.setText(merchantAppSetData?.SettingMessage)
            } catch (e: NoInternetExcetion) {
                networkDialog()
            }catch (e:CancellationException)
            {
                Log.i("scope","job is canceled")
            }
            catch (e: Exception) {
                okDialogWithOneAct("Error MI01", e.message.toString())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpLocationListener()
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    try {
                        for (location in locationResult.locations) {
                            latitude = location.latitude.toString()
                            longitude = location.longitude.toString()
                            val geocoder: Geocoder
                            val addresses: List<Address>
                            geocoder = Geocoder(this@CheckOutActivity, Locale.getDefault())
                            addresses = geocoder.getFromLocation(latitude!!.toDouble(), longitude!!.toDouble(), 1)
                            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            if(addresses!=null) {
                                address = addresses[0].getAddressLine(0)
                                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                if(addresses[0].getLocality()!=null)
                                {
                                    city =addresses[0].getLocality()
                                }else
                                {
                                    city =""
                                }
                                if(addresses[0].getAdminArea()!=null)
                                {
                                    state =addresses[0].getAdminArea()
                                }else
                                {
                                    state =""
                                }
                                if(addresses[0].getCountryName()!=null)
                                {
                                    country =addresses[0].getCountryName()
                                }else
                                {
                                    country =""
                                }
                                if(addresses[0].getPostalCode()!=null)
                                {
                                    postalCode = addresses[0].getPostalCode()
                                }else
                                {
                                    postalCode = ""
                                }

                                if(addresses[0].getFeatureName()!=null)
                                {
                                    knownName = addresses[0].getFeatureName()
                                }else
                                {
                                    knownName =""
                                }
                            }
                        }
                    }catch (e:Exception)
                    {
                        e.printStackTrace()
                    }

                    // Few more things we can do here:
                    // For example: Update the location of user on server
                }
            },
            Looper.myLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                        else -> {
                            //PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun okDialog(title:String, message:String)
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
            processPaymentMode()
            dialog.dismiss()
        }
        dialog.show()
    }

        private  fun displayLoadingBox(msg : String): AlertDialog {
        val builder = AlertDialog.Builder(this@CheckOutActivity)
        val dialogView =  LayoutInflater.from(this@CheckOutActivity).inflate(R.layout.progress_dialog, null, false)
        val message = dialogView.findViewById<TextView>(R.id.message)
        message.text = msg
        builder.setView(dialogView)
        builder.setCancelable(false)
        return builder.create()

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }
}

