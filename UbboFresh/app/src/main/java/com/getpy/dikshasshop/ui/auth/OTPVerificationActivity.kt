package com.getpy.dikshasshop.ui.auth

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.MultiStoreAdater
import com.getpy.dikshasshop.brodcastreceiver.SmsBroadcastReceiver
import com.getpy.dikshasshop.data.model.AuthResponse
import com.getpy.dikshasshop.data.model.MultiStoreDataModel
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.OtpVerificationBinding
import com.getpy.dikshasshop.ui.main.MainActivity
import com.getpy.dikshasshop.ui.multistore.MultiStoreActivity
import com.google.android.gms.location.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.regex.Matcher
import java.util.regex.Pattern


class OTPVerificationActivity : AppCompatActivity(), KodeinAware {
    private val REQ_USER_CONSENT = 200
    var latitudeFetched: String? = "0"
    var longitudeFetched: String? = "0"
    var accessKey: String? = null
    var nearestStore : MultiStoreDataModel? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var smsBroadcastReceiver: SmsBroadcastReceiver? = null
    private var binding: OtpVerificationBinding? = null
    private var viewmodel: AuthViewModel? = null
    override val kodein by kodein()
    private val preference: PreferenceProvider by instance()
    private val factory: AuthViewModelFactory by instance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = DataBindingUtil.setContentView(this, R.layout.otp_verification)
        viewmodel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)

        AppCenter.start(
            application, "9e64f71e-a876-4d54-a2ce-3c4c1ea86334",
            Analytics::class.java, Crashes::class.java
        )
        getLocationPermission()
        setUpLocationListener(null)

        initFont()
        startSmsUserConsent()
        binding?.sendOtpBut?.setOnClickListener(View.OnClickListener {
            binding?.sendOtpBut?.setBackgroundResource(R.drawable.rounded_corner_selected)
            binding?.sendOtpBut?.setTextColor(Color.WHITE)
            verifyOtpCall()
        })
        binding?.resendOtp?.setOnClickListener(View.OnClickListener {
            startSmsUserConsent()
            registerBroadcastReceiver()
            generateOtpCall()
        })
    }

    override fun onResume() {
        super.onResume()
        setUpLocationListener(null)
    }
    fun initFont() {
        binding?.loginTxt?.setTypeface(UbboFreshApp.instance?.latobold)
        binding?.otpHasSent?.setTypeface(UbboFreshApp.instance?.latoregular)
        binding?.resendOtp?.setTypeface(UbboFreshApp.instance?.latoblackitalic)
        binding?.mobNoTxt?.setTypeface(UbboFreshApp.instance?.latoregular)
        binding?.mobileNumEdit?.setTypeface(UbboFreshApp.instance?.latoregular)
        binding?.sendOtpBut?.setTypeface(UbboFreshApp.instance?.latoregular)
        binding?.otpHasSent?.setText(viewmodel?.getHtmlText(resources.getString(R.string.otp_verification_sent), "+91"))
        binding?.resendOtp?.setText(viewmodel?.getHtmlText(resources.getString(R.string.resend_otp), "RESEND"))
        binding?.mobNoTxt?.text = preference.getStringData(Constants.saveMobileNumkey)
    }

    fun generateBaseUrl() {
        val mobleNum = preference.getStringData(Constants.saveMobileNumkey)
        binding?.progressBar?.show()
        lifecycleScope.launch {
            try {
                binding?.progressBar?.dismiss()
                val params: MutableMap<String, String> = HashMap()
                params["access_key"] = preference.getStringData(Constants.saveaccesskey)
                params["phone_number"] = preference.getStringData(Constants.saveMobileNumkey)
                params["merchant_id"] = Constants.merchantid.toString()
                val baseres = viewmodel?.generateBaseUrl(params)
                if(baseres?.BaseUrl!=null)
                {

                    UbboFreshApp.instance?.imageLoadUrl=baseres.BaseUrl.replace("http://","https://")
                    UbboFreshApp.instance?.imageLoadUrl.let {
                        preference.saveStringData(Constants.saveBaseUrl,it!!)
                    }
                }else
                {
                    preference.saveStringData(Constants.saveBaseUrl,"")
                    UbboFreshApp.instance?.imageLoadUrl=""
                }

            } catch (e: NoInternetExcetion) {
                networkDialog()
            }catch (e:CancellationException)
            {
                Log.i("scope","job is canceled")
            }
            catch (e: Exception) {
                okDialogWithOneAct("Error", e.message.toString())
            }
        }
    }
    fun generateOtpCall() {
        val mobleNum = preference.getStringData(Constants.saveMobileNumkey)
        binding?.progressBar?.show()
        lifecycleScope.launch {
            try {
                binding?.progressBar?.dismiss()
                val authres = viewmodel?.generateOtp(mobleNum, Constants.merchantid, Constants.hashkey, Constants.regtoken) as AuthResponse
                if(authres.status?.toLowerCase().equals(Constants.status)) {
                    authres.accessKey?.let { preference.saveStringData(Constants.saveaccesskey,it)}
                }else
                {
                    okDialogWithOneAct(Constants.appName,authres.message.toString())
                }
            } catch (e: NoInternetExcetion) {
                networkDialog()
            }catch (e:CancellationException)
            {
                Log.i("scope","job is canceled")
            }
            catch (e: Exception) {
                okDialogWithOneAct("Error", e.message.toString())
            }
        }
        val map=HashMap<String,String>()
        map.put("mobileNum",mobleNum)
        map.put("merchantid", Constants.merchantid.toString())
        Analytics.trackEvent("Resend OTP clicked", map)
    }

    fun verifyOtpCall() {
        val mobleno = preference.getStringData(Constants.saveMobileNumkey)
        val otp = binding?.mobileNumEdit?.text.toString()
        if (TextUtils.isEmpty(binding?.mobileNumEdit?.text.toString())) {
            okDialogWithOneAct(Constants.appName, "Did you receive otp?please try with resend", binding!!.sendOtpBut)
            return
        }
        binding?.progressBar?.show()
        lifecycleScope.launch {
            try {
                val response = viewmodel?.verifyOtp(otp, mobleno, Constants.merchantid)
                generateBaseUrl()
                if (response?.status?.toLowerCase().equals(Constants.status)) {
                    response?.numberofstores?.let { preference.saveIntData(Constants.saveNoOfStores, it) }
                    if (response?.numberofstores?:0 > 1) {
                        getMultiStoreList()
                         Handler().postDelayed( {
                             var isBypassSuccessful = false
                             if(response?.ByPass?.toLowerCase().equals("yes")){
                                 isBypassSuccessful = bypassMultiStorePage()
                             }
                             if(!isBypassSuccessful){
                             preference.saveBoolData(Constants.savelogin, false)
                             preference.saveBoolData(Constants.saveMultistore, true)
                             val intent = Intent(this@OTPVerificationActivity, MultiStoreActivity::class.java)
                                 binding?.progressBar?.dismiss()
                             startActivity(intent)
                         }
                                                }, 2000)

                    } else {
                        callMutistore()
                    }
                } else {
                    okDialogWithOneAct(Constants.appName, response?.message.toString())
                }
            } catch (e: NoInternetExcetion) {
                binding?.progressBar?.dismiss()
                networkDialog()
            }catch (e:CancellationException)
            {
                Log.i("scope","job is canceled")
            }
            catch (e: Exception) {
                binding?.progressBar?.dismiss()
                okDialogWithOneAct("Error", "Invalid OTP. Please try again with correct OTP")
            }
        }
        val map=HashMap<String,String>()
        map.put("mobileNum",mobleno)
        map.put("merchantid", Constants.merchantid.toString())
        Analytics.trackEvent("Verify OTP clicked", map)
    }

    private fun bypassMultiStorePage():Boolean {
        try {
            if(nearestStore != null){
                nearestStore?.mid?.let { preference.saveIntData(Constants.saveMerchantIdKey, it) }
                preference.saveBoolData(Constants.savelogin, true)
                preference.saveBoolData(Constants.saveMultistore, false)
                preference.saveStringData(Constants.saveStorename, nearestStore?.NameofStore.toString())

                val map=HashMap<String,String>()
                map.put("Mobile Number",preference.getStringData(Constants.saveMobileNumkey))
                map.put("Merchant Id", preference.getIntData(Constants.saveMerchantIdKey).toString())
                map.put("Store Name", nearestStore?.NameofStore.toString())
                Analytics.trackEvent("Store Selected", map)

                val intent = Intent(this@OTPVerificationActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                binding?.progressBar?.dismiss()
                startActivity(intent)
                return true
            }
            else
                return false
        }
        catch (e: Exception)
        {
            return false
        }
    }

    fun callMutistore()
    {
        lifecycleScope.launchWhenCreated {
            try {
                val jsonobject= JSONObject()
                jsonobject.put("access_key",preference.getStringData(Constants.saveaccesskey))
                jsonobject.put("phone_number",preference.getStringData(Constants.saveMobileNumkey))
                jsonobject.put("merchant_id", Constants.merchantid)

                Log.i("getMultipleStoreDetails",jsonobject.toString())

                val multires=viewmodel?.getStoreDetailsforMultiStore(
                        preference.getStringData(Constants.saveaccesskey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        Constants.merchantid,
                "0",
                "0")
                if(multires?.status?.toLowerCase().equals(Constants.status)) {
                    val multiStoreDataModel = multires?.data?.get(0)
                    multiStoreDataModel?.mid?.let { preference.saveIntData(Constants.saveMerchantIdKey, it) }
                    preference.saveBoolData(Constants.savelogin, true)
                    preference.saveBoolData(Constants.saveMultistore, false)
                    preference.saveStringData(Constants.saveStorename, multiStoreDataModel?.NameofStore.toString())

                    val map=HashMap<String,String>()
                    map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
                    map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
                    map.put("storename", multiStoreDataModel?.NameofStore.toString())
                    Analytics.trackEvent("Store Selected", map)

                    val intent = Intent(this@OTPVerificationActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }else
                {
                    if(multires?.message?.toLowerCase().equals(Constants.message))
                    {
                        okDialogWithNavigateToLogin(this@OTPVerificationActivity,preference,Constants.appName,multires?.message.toString())
                    }else
                    {
                        okDialogWithOneAct(Constants.appName,multires?.message.toString())
                    }
                }
            }catch (e: NoInternetExcetion)
            {
                networkDialog()
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
    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            @Nullable data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                //Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                message?.let { getOtpFromMessage(it) }
            }
        }
    }

    private fun startSmsUserConsent() {
        val client = SmsRetriever.getClient(this)
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null)
                .addOnSuccessListener {
                    log("start listener")
                }
                .addOnFailureListener {
                    log("stop message")
                }
    }

    private fun getOtpFromMessage(message: String) {
        // This will match any 6 digit number in the message
        val pattern: Pattern = Pattern.compile("(|^)\\d{6}")
        val matcher: Matcher = pattern.matcher(message)
        if (matcher.find()) {
            binding?.mobileNumEdit?.setText(matcher.group(0))
        }
    }

    fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener =
                object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                    override fun onSuccess(intent: Intent?) {
                        startActivityForResult(intent, REQ_USER_CONSENT)
                    }
                    override fun onFailure() {}
                }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }


    private  fun getLocationPermission(dialog: AlertDialog? = null){
        // if location permission granted
        if(PermissionUtils.isAccessFineLocationGranted(this)) {
            getLastKnownLocation()
        }
        // if location permission is not granted
        else{
            //request for permission
            PermissionUtils.requestAccessFineLocationPermission(
                this,
                OTPVerificationActivity.LOCATION_PERMISSION_REQUEST_CODE
            )

        }
    }

    private fun setUpLocationListener(dialog: AlertDialog?= null) {
        dialog?.show()
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        //if location permission is not granted (load Store list and return)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED
        ) {
            //getMultiStoreList()
            return
        }
        //if location permission is granted
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    try {
                        if(locationResult.lastLocation != null){
                            latitudeFetched = locationResult.lastLocation.latitude.toString()
                            longitudeFetched = locationResult.lastLocation.longitude.toString()
                            //getMultiStoreList()
                        }
                        //getMultiStoreList()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            null
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            OTPVerificationActivity.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            //setUpLocationListener(null)
                            getLastKnownLocation()
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                    //setUpLocationListener(null)
                    getMultiStoreList()
                }
            }
        }
    }


    fun getLastKnownLocation() {
        var isGpsOn : Boolean
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getMultiStoreList()
            return
        }
        isGpsOn = when {
            //if GPS is on
            PermissionUtils.isLocationEnabled(this) -> true
            //if GPS is off
            else ->{
                PermissionUtils.showGPSNotEnabledDialog(this)
                false
            }
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    latitudeFetched = location.latitude.toString()
                    longitudeFetched = location.longitude.toString()
                    getMultiStoreList()
                }
                else if(isGpsOn){
                    setUpLocationListener()
                    Handler().postDelayed({getMultiStoreList()},5000)
                }
                else
                    Handler().postDelayed({getMultiStoreList()},10)
            }
    }


    fun getMultiStoreList()
    {
        val jsonObj=JSONObject()
        jsonObj.put("access_key", preference.getStringData(Constants.saveaccesskey))
        jsonObj.put("phone_number", preference.getStringData(Constants.saveMobileNumkey))
        jsonObj.put("merchant_id", Constants.merchantid)

        Log.i("getMultipleStoreDetails", jsonObj.toString())
        lifecycleScope.launchWhenCreated {
            try {
                //sudo geo location to test bangalore distance
                //if(preference.getStringData(Constants.saveMobileNumkey) == "7635850811")
                {
                    longitudeFetched =  "77.31263129866218"
                    latitudeFetched = "29.461320526460273"
                }
                if(latitudeFetched != "0" || longitudeFetched != "0"){
                    val response=viewmodel?.getStoreDetailsforMultiStore(
                        preference.getStringData(Constants.saveaccesskey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        Constants.merchantid,
                        latitudeFetched.toString(),
                        longitudeFetched.toString())
                    //binding.pbar.dismiss()
                    if(response?.status?.toLowerCase().equals(Constants.status))
                    {
                        //Select first Store
                        nearestStore = response?.data?.get(0)
                    }

                }
            }catch (e: NoInternetExcetion)
            {
                //binding.pbar.dismiss()
                networkDialog()
            }catch (e: Exception)
            {
                //binding.pbar.dismiss()
                okDialogWithOneAct("Error", e.message.toString())
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }
}