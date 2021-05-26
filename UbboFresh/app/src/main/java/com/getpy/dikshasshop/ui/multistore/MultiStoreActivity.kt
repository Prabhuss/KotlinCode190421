package com.getpy.dikshasshop.ui.multistore

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.MultiStoreAdater
import com.getpy.dikshasshop.data.db.AppDataBase
import com.getpy.dikshasshop.data.model.MultiStoreDataModel
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityStoreBinding
import com.getpy.dikshasshop.ui.main.MainActivity
import com.google.android.gms.location.*
import com.microsoft.appcenter.analytics.Analytics
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.HashMap

class MultiStoreActivity : AppCompatActivity(),KodeinAware  {
    var latitudeFetched: String? = "0"
    var longitudeFetched: String? = "0"
    override val kodein by kodein()
    private val databse: AppDataBase by instance()
    private val factory: MultiStoreViewModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    lateinit var binding: ActivityStoreBinding
    lateinit var viewModel: MultiStoreViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var adapter:MultiStoreAdater?=null
    var shouldExecuteOnResume = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_store)
        viewModel= ViewModelProviders.of(this, factory).get(MultiStoreViewModel::class.java)
        init()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        binding.pbar.show()
        //callMutistore()
        shouldExecuteOnResume = false
        getLocationPermission()
        setUpLocationListener(null)
    }
    fun init()
    {
        binding.selectStore.setTypeface(UbboFreshApp.instance?.latobold)
        binding.welcome.setTypeface(UbboFreshApp.instance?.latobold)
        binding.welcome1.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.pullDownTxt.setTypeface(UbboFreshApp.instance?.latoregular)

        //learnit- how to add refresh feature to your page
        binding.swipeRefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                setUpLocationListener()
                getLocationPermission()
            }
        })
    }

    fun loadHomePage(model: MultiStoreDataModel)
    {
        UbboFreshApp.instance?.instructionString=""
        preference.saveBoolData(Constants.saveMultistore, false)
        preference.saveBoolData(Constants.savelogin, true)
        preference.saveStringData(Constants.saveStorename, model.NameofStore.toString())
        model.mid?.let { preference.saveIntData(Constants.saveMerchantIdKey, it) }
        val map=HashMap<String, String>()
        map["mobileNum"] = preference.getStringData(Constants.saveMobileNumkey)
        map["merchantid"] = preference.getIntData(Constants.saveMerchantIdKey).toString()
        map["storeName"] = model.NameofStore.toString()
        Analytics.trackEvent("Store Selected", map)
        lifecycleScope.launch {
            UbboFreshApp.instance?.carItemsList=databse.CustomerAddressDao().getCartData(preference.getIntData(Constants.saveMerchantIdKey).toString(), preference.getStringData(Constants.saveMobileNumkey))
            if(UbboFreshApp.instance?.carItemsList!!.size>0)
            {
                UbboFreshApp.instance?.hashMap?.clear()
                for(i in 0 until UbboFreshApp.instance?.carItemsList!!.size) {
                    val m = UbboFreshApp.instance?.carItemsList?.get(i)
                    m?.citrineProdId?.let { UbboFreshApp.instance?.hashMap?.put(it, m) }
                }
            }else
            {
                UbboFreshApp.instance?.hashMap?.clear()
            }
            val intent=Intent(this@MultiStoreActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        if(shouldExecuteOnResume){
            // Your onResume Code Here
            //setUpLocationListener(null)
            getLastKnownLocation()
        } else{
            shouldExecuteOnResume = true;
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
                 val response=viewModel.getStoreDetailsforMultiStore(
                         preference.getStringData(Constants.saveaccesskey),
                         preference.getStringData(Constants.saveMobileNumkey),
                         Constants.merchantid,
                         latitudeFetched,
                         longitudeFetched)
                 binding.pbar.dismiss()
                 if(binding.swipeRefresh!=null)
                    binding.swipeRefresh.isRefreshing = false
                 if(response.status?.toLowerCase().equals(Constants.status))
                 {
                     adapter= response.data?.let {
                         MultiStoreAdater(this@MultiStoreActivity, it, object : MultiStoreAdater.OnItemClickListener {
                             override fun onItemClick(item: MultiStoreDataModel?) {
                                 item?.let { it1 -> loadHomePage(it1) }
                             }
                        })
                     }
                     binding.recyclerView.adapter=adapter
                 }else
                 {
                     when {
                         response.message?.toLowerCase().equals(Constants.message) -> {
                             okDialogWithNavigateToLogin(this@MultiStoreActivity, preference, Constants.appName, response.message.toString())
                         }
                         response.message?.toLowerCase().equals(Constants.verification_peniding) -> {
                             okDialogWithNavigateToLogin(this@MultiStoreActivity, preference, Constants.appName, response.message.toString())
                         }
                         else -> {
                             okDialogWithOneAct(Constants.appName, response.message.toString())
                         }
                     }
                 }
             }catch (e: NoInternetExcetion)
             {
                 binding.pbar.dismiss()
                 networkDialog()
             }catch (e: Exception)
             {
                 binding.pbar.dismiss()
                 okDialogWithOneAct("Error", e.message.toString())
             }
         }
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
                    LOCATION_PERMISSION_REQUEST_CODE
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
            LOCATION_PERMISSION_REQUEST_CODE -> {
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
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }
}