package com.getpy.dikshasshop.ui.cart.cartactivities


import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.AutoCompleteAdapter
import com.getpy.dikshasshop.data.db.entities.CustomerAddressData
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityAddAddressBinding
import com.getpy.dikshasshop.ui.cart.CartViewModel
import com.getpy.dikshasshop.ui.cart.CartViewModelFactory
import kotlinx.android.synthetic.main.activity_add_address.view.*
import kotlinx.android.synthetic.main.products_items_row.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*


class AddAddressActivity : AppCompatActivity(),KodeinAware,OnMapReadyCallback {
    override val kodein by kodein()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var mapFragment: SupportMapFragment? = null
    private val factory: CartViewModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    lateinit var binding:ActivityAddAddressBinding
    lateinit var viewModel: CartViewModel
    var adapter: AutoCompleteAdapter? = null
    var placesClient: PlacesClient? = null
    var latitudeFetched:String?=null
    var longitudeFetched:String?=null
    var latitudeForAddress:String?=null
    var longitudeForAddress:String?=null
    var city:String=""
    var state: String=""
    var country: String=""
    var postalCode: String=""
    var address:String=""
    var saveAs:String=""
    var permissionFlag : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_add_address)
        viewModel=ViewModelProviders.of(this,factory).get(CartViewModel::class.java)

        AppCenter.start(
            application, "9e64f71e-a876-4d54-a2ce-3c4c1ea86334",
            Analytics::class.java, Crashes::class.java
        )
        binding.applyChanges.setOnClickListener(View.OnClickListener {
            setCustomerAddress()
        })
        binding.icBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        init()
        binding.currentLocationCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                val dialog = displayLoadingBox("Fetching Location...")
                getLocationPermission(dialog)
            }
            else{
                val dialog = displayLoadingBox("Please Wait...")
                dialog.show()
                resetFields()
                Handler().postDelayed({dialog.dismiss()},1000)
            }
        }
//        val apiKey = getString(R.string.google_maps_key)
//        if (!Places.isInitialized()) {
//            Places.initialize(getApplicationContext(), apiKey);
//        }

        //placesClient = Places.createClient(this);
        //initAutoCompleteTextView();

        try{
            if(intent.getStringExtra("callType").equals("add"))
                binding.currentLocationCheckbox.isChecked = true
        }
        catch (e : java.lang.Exception){
            binding.currentLocationCheckbox.isChecked = false
        }

        if(intent.extras?.getSerializable("model")!=null) {
            val model = intent.extras?.getSerializable("model") as CustomerAddressData
            binding.currentLocationCheckbox.isChecked = false
            model.let { updateData(it) }
        }
    }
    fun init()
    {
        binding.editAddress.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.addrDetails.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.name.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.nameLayout.markRequiredInRed()
        binding.alternateNumber.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.landmark.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.landmarkLayout.markRequiredInRed()
        binding.flatno.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.address.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.addressLayout.markRequiredInRed()
        binding.city.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.cityLayout.markRequiredInRed()
        binding.state.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.stateLayout.markRequiredInRed()
        binding.postZipcode.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.postZipcodeLayout.markRequiredInRed()
        binding.useCurrLocation.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.locText.setTypeface(UbboFreshApp.instance?.latoheavy)
        binding.applyChanges.setTypeface(UbboFreshApp.instance?.latoheavy)

        val intent=intent

        latitudeForAddress=intent.getStringExtra("lat")?:"0.0"
        longitudeForAddress=intent.getStringExtra("lng")?:"0.0"
        city=intent.getStringExtra("city")?:""
        state=intent.getStringExtra("state")?:""
        country=intent.getStringExtra("country")?:""
        postalCode=intent.getStringExtra("postalCode")?:""
        address=intent.getStringExtra("address")?:""
        binding.address.setText(address)
        if(checkStringIsEmpty(city))
        {
            binding.city.setHint("")
        }else
        {
            binding.city.setText(city)
        }
        if(checkStringIsEmpty(state))
        {
            binding.state.setHint("")
        }else
        {
            binding.state.setText(state)
        }
        if(checkStringIsEmpty(postalCode))
        {
            binding.postZipcode.setHint("")
        }else
        {
            binding.postZipcode.setText(postalCode)
        }
    }
    fun updateData(model:CustomerAddressData)
    {
        binding.name.setText(model.FirstName)
        binding.alternateNumber.setText(model.SecondaryPhone)
        binding.address.setText(model.Address1)
        binding.landmark.setText(model.Address2)
        binding.flatno.setText(model.FlatNo_DoorNo)
        binding.city.setText(model.City)
        binding.state.setText(model.State)
        if(model.PostalCodeZipCode!=null) {
            if (checkStringIsEmpty(model.PostalCodeZipCode!!)) {
                binding.postZipcode.setHint(postalCode)
            } else {
                binding.postZipcode.setText(model.PostalCodeZipCode)
            }
        }else
        {
            if(checkStringIsEmpty(postalCode))
            {
                binding.postZipcode.setHint("")
            }else
            {
                binding.postZipcode.setText(postalCode)

            }
        }

        if(model.TagName.equals(Constants.address1))
        {
            binding.otherButton.isEnabled=false
            binding.workButton.isEnabled=false
            binding.workButton.isClickable=false
            binding.otherButton.isClickable=false
            binding.homeButton.isChecked=true
        }
        if(model.TagName.equals(Constants.address2))
        {
            binding.otherButton.isEnabled=false
            binding.homeButton.isEnabled=false
            binding.homeButton.isClickable=false
            binding.otherButton.isClickable=false
            binding.workButton.isChecked=true
        }
        if(model.TagName.equals(Constants.address3))
        {
            binding.homeButton.isEnabled=false
            binding.workButton.isEnabled=false
            binding.homeButton.isClickable=false
            binding.workButton.isClickable=false
            binding.otherButton.isChecked=true
        }
    }
    fun setCustomerAddress()
    {
        if(checkStringIsEmpty(binding.name.text.toString()))
        {
            binding.nameLayout.error="Name field should not be empty"
            return
        }else
        {
            binding.nameLayout.error=null
        }
        if(checkStringIsEmpty(binding.address.text.toString()))
        {
            binding.addressLayout.error="Address field should not be empty"
            return
        }else
        {
            binding.addressLayout.error=null
        }
        if(checkStringIsEmpty(binding.city.text.toString()))
        {
            binding.cityLayout.error="City field should not be empty"
            return
        }else
        {
            binding.cityLayout.error=null
        }
        if(checkStringIsEmpty(binding.state.text.toString()))
        {
            binding.stateLayout.error="State field should not be empty"
            return
        }else
        {
            binding.stateLayout.error=null
        }
        if(checkStringIsEmpty(binding.postZipcode.text.toString()))
        {
            binding.postZipcodeLayout.error="Postcode field should not be empty"
            return
        }else
        {
            binding.postZipcodeLayout.error=null
        }
        if(checkStringIsEmpty(binding.landmark.text.toString()))
        {
            binding.landmarkLayout.error="Landmark field should not be empty"
            return
        }else
        {
            binding.landmarkLayout.error=null
        }
        var button:RadioButton?=null
        if (binding.radioGroup.getCheckedRadioButtonId() == -1)
        {
            okDialogWithOneAct(Constants.appName,"Please choose any address type")
            return
        }
        val selectedId = binding.radioGroup.checkedRadioButtonId
        button = findViewById(selectedId) as RadioButton
        if(button?.text?.equals("Home")!!)
        {
            saveAs=Constants.address1
        }
        if(button?.text!!.equals("Work"))
        {
            saveAs=Constants.address2
        }
        if(button?.text!!.equals("Other"))
        {
            saveAs=Constants.address3
        }
        if(checkStringIsEmpty(button.text.toString()))
        {
            okDialogWithOneAct(Constants.appName,"Please choose any address type")
            return
        }
        val temp = latitudeForAddress
        val temp2 = longitudeForAddress
        lifecycleScope.launch {
            try {
                val response=viewModel.setCustomerAddress(
                        preference.getStringData(Constants.saveaccesskey),
                        preference.getIntData(Constants.saveMerchantIdKey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        binding.alternateNumber.text.toString(),
                        binding.address.text.toString(),
                        binding.landmark.text.toString(),
                        longitudeForAddress,
                        latitudeForAddress,
                        saveAs,
                        binding.name.text.toString(),
                        "",
                        binding.flatno.text.toString(),
                        binding.city.text.toString(),
                        binding.state.text.toString(),
                        "",
                        "",
                        binding.postZipcode.text.toString()
                )
                val data=response.string()
                val obj=JSONObject(data)
                finish()
            }catch (e: NoInternetExcetion) {
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
    private fun initAutoCompleteTextView() {
        binding.auto.setThreshold(1)
        binding.auto.setOnItemClickListener(autocompleteClickListener)
        adapter = placesClient?.let { AutoCompleteAdapter(this, it) }
        binding.auto.setAdapter(adapter)
    }
    fun createMap()
    {
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if(latitudeFetched!=null && longitudeFetched!=null) {
            googleMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(latitudeFetched!!.toDouble(), longitudeFetched!!.toDouble()))
                    .title(city)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitudeFetched!!.toDouble(),longitudeFetched!!.toDouble()),
                    10f
                )
            )
        }

       /* googleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(37.4629101, -122.2449094))
                .title("Facebook")
                .snippet("Facebook HQ: Menlo Park")
        )

        googleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(37.3092293, -122.1136845))
                .title("Apple")
        )*/


    }
    override fun onStart() {
        super.onStart()
    }

    private  fun getLocationPermission(dialog: AlertDialog){

            if(PermissionUtils.isAccessFineLocationGranted(this)) {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpLocationListener(dialog)
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                        binding.currentLocationCheckbox.isChecked = false

                    }
                }
            }
            else{
                PermissionUtils.requestAccessFineLocationPermission(
                        this,
                        LOCATION_PERMISSION_REQUEST_CODE
                )

            }
        }
    private  fun displayLoadingBox(msg : String):AlertDialog{
        val builder = AlertDialog.Builder(this@AddAddressActivity)
        val dialogView =  LayoutInflater.from(this@AddAddressActivity).inflate(R.layout.progress_dialog, null, false)
        val message = dialogView.findViewById<TextView>(R.id.message)
        message.text = msg
        builder.setView(dialogView)
        builder.setCancelable(false)
        return builder.create()

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
                            val dialog = displayLoadingBox("Fetching Location...")
                            setUpLocationListener(dialog)
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                            binding.currentLocationCheckbox.isChecked = false
                        }
                    }
                } else {
                    Toast.makeText(
                            this,
                            getString(R.string.location_permission_not_granted),
                            Toast.LENGTH_SHORT
                    ).show()
                    binding.currentLocationCheckbox.isChecked = false
                }
            }
        }
    }

    private fun setUpLocationListener(dialog: AlertDialog) {
        dialog.show()
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
                    for (location in locationResult.locations) {
                        try {
                            latitudeFetched = location.latitude.toString()
                            longitudeFetched = location.longitude.toString()
                            val geocoder: Geocoder
                            val addresses: List<Address>
                            geocoder = Geocoder(this@AddAddressActivity, Locale.getDefault())
                            addresses = geocoder.getFromLocation(
                                latitudeFetched!!.toDouble(),
                                longitudeFetched!!.toDouble(),
                                1
                            )
                            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            if (addresses != null) {
                                address = addresses[0].getAddressLine(0)
                                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                city = addresses[0].getLocality()
                                state = addresses[0].getAdminArea()
                                country = addresses[0].getCountryName()
                                postalCode = addresses[0].getPostalCode()
                                val knownName = addresses[0].getFeatureName()
                            }
                        }catch (e:Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                    // Few more things we can do here:
                    // For example: Update the location of user on server
                }
            },
            Looper.myLooper()
        )
        resetFields()
        Handler().postDelayed({
            setFields(address,city,state,country,postalCode,latitudeFetched,longitudeFetched)
            if(binding.address.text.isNullOrEmpty()){
                Handler().postDelayed({
                    setFields(address,city,state,country,postalCode,latitudeFetched,longitudeFetched)
                    if(binding.address.text.isNullOrEmpty()){
                        Handler().postDelayed({
                            setFields(address,city,state,country,postalCode,latitudeFetched,longitudeFetched)
                            if(binding.address.text.isNullOrEmpty()){
                                binding.currentLocationCheckbox.isChecked= false
                                toast("Unable to fetch location. Please try again")
                            }
                            dialog.dismiss()
                        },1500)
                    }
                    else
                    {
                        createMap()
                        dialog.dismiss()
                    }
                },1500)
            }
            else
            {
                createMap()
                dialog.dismiss()
            }
        },1500)
    }

    private fun setFields(address: String?,city : String?, state: String?, country: String?, postalCode: String?,lati: String?, longi: String?) {
        binding.address.setText(address)
        binding.city.setText(city)
        binding.state.setText(state)
        binding.postZipcode.setText(postalCode)
        latitudeForAddress = lati
        longitudeForAddress = longi

    }
    private  fun resetFields()
    {
        binding.address.setText("")
        binding.city.setText("")
        binding.state.setText("")
        binding.postZipcode.setText("")
        latitudeForAddress = "0.00"
        longitudeForAddress = "0.00"
    }


    private val autocompleteClickListener: AdapterView.OnItemClickListener = object :
        AdapterView.OnItemClickListener {
            override fun onItemClick(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                try {
                    val item: AutocompletePrediction = adapter!!.getItem(i)
                    var placeID: String? = null
                    if (item != null) {
                        placeID = item.getPlaceId()
                    }

//                To specify which data types to return, pass an array of Place.Fields in your FetchPlaceRequest
//                Use only those fields which are required.
                    val placeFields: kotlin.collections.List<Place.Field> = Arrays.asList(
                        Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS
                        , Place.Field.LAT_LNG
                    )
                    var request: FetchPlaceRequest? = null
                    if (placeID != null) {
                        request = FetchPlaceRequest.builder(placeID, placeFields)
                            .build()
                    }
                    if (request != null) {
                        placesClient!!.fetchPlace(request).addOnSuccessListener(object :
                            OnSuccessListener<FetchPlaceResponse?> {


                            override fun onSuccess(task: FetchPlaceResponse?) {
                               toast(task?.getPlace()?.getName().toString() + "\n" + task?.getPlace()
                                   ?.getAddress());
                                /*responseView?.setText(
                                    task.getPlace().getName().toString() + "\n" + task.getPlace()
                                        .getAddress()
                                )*/
                            }
                        }).addOnFailureListener(object : OnFailureListener {
                            override fun onFailure(@NonNull e: Exception) {
                                e.printStackTrace()
                                toast(e.message.toString());
                                //responseView!!.text = e.message
                            }
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }


}