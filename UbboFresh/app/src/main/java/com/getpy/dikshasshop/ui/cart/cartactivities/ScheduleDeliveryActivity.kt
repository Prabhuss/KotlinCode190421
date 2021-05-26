package com.getpy.dikshasshop.ui.cart.cartactivities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.Constants
import com.getpy.dikshasshop.Utils.toast
import com.getpy.dikshasshop.adapter.ScheduleDateAdapter
import com.getpy.dikshasshop.adapter.ScheduleSlotAdapter
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityScheduleDeliveryBinding
import com.getpy.dikshasshop.listeners.ItemClickListener
import com.getpy.dikshasshop.ui.cart.CartViewModel
import com.getpy.dikshasshop.ui.cart.CartViewModelFactory
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ScheduleDeliveryActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    lateinit var binding: ActivityScheduleDeliveryBinding
    private val factory: CartViewModelFactory by instance()
    lateinit var viewModel: CartViewModel
    var selectedDate : String? = null
    var selectedSlot : String? = null
    private val preference: PreferenceProvider by instance()
    var dateAdapter: ScheduleDateAdapter?=null
    var slotAdapter: ScheduleSlotAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_schedule_delivery)
        viewModel = ViewModelProviders.of(this, factory).get(CartViewModel::class.java)



        //setup Page---------------------------------------------------------------------------
        //getCouponList()
        val date = getCurrentDateTime()
        val dateInString = date.toString("yyyy-MM-dd")
        setPage(dateInString)
        binding.saveSchedule.setOnClickListener {
            if(selectedSlot == null){
                toast("No slot selected")
            }
            else{
                UbboFreshApp.instance?.selectedScheduleDate = selectedDate
                UbboFreshApp.instance?.selectedScheduleSlot = selectedSlot
                finish()
            }
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
    fun setPage(dateInString:String){
        val date = getCurrentDateTime()
        getSlotDetailsAndLoadPage(dateInString)
        getSlotDetails(date)
    }

    fun getSlotDetails(date: Date){
        lifecycleScope.launch {
            try {
                binding.pBar.visibility = View.VISIBLE
                val dateInString = date.toString("yyyy-MM-dd")
                val response = viewModel.getSlotDetails(
                        preference.getIntData(Constants.saveMerchantIdKey),
                        preference.getStringData(Constants.saveaccesskey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        dateInString
                )

                    //Assign value from Api
                    slotAdapter = response.data?.let { it1 ->
                        //test------------------------------------------------
                        if(it1.size>0)
                            binding.slotsErrorMsg.visibility = View.GONE
                        ScheduleSlotAdapter(this@ScheduleDeliveryActivity, it1, object : ItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                val model = it1[position]
                                selectedSlot = model.slotId
                                binding.selectedSlot.text = model.slotTimings.toString()
                                //getSlotDetails(date)
                            }
                        }) }
                    binding.slotList.adapter = slotAdapter
                slotAdapter?.notifyDataSetChanged()
                binding.pBar.visibility = View.GONE
            }
            catch (e:Exception){

            }
        }
    }
    private fun getSlotDetailsAndLoadPage(date: String) {
        lifecycleScope.launch {
            try {
                val response = viewModel.getSlotDetails(
                        preference.getIntData(Constants.saveMerchantIdKey),
                        preference.getStringData(Constants.saveaccesskey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        date
                )
                var dateList: MutableList<Date> = ArrayList()
                for(i in 0 until (response.deliverywindow?.toInt() ?: 1)){
                    val date = getCurrentDateTime()
                    val listDate = addDaysToDate(date,i)
                    val dateInString = listDate.toString("EEE, d MMM yyyy")
                    dateList.add(listDate)
                }
                if (dateList != null) {
                    //Assign value from Api
                    dateAdapter = dateList?.let { it1 ->
                        ScheduleDateAdapter(this@ScheduleDeliveryActivity, it1, object : ItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                val model = it1[position]
                                selectedDate = model.toString("EEE, dd MMM yyyy")
                                selectedSlot = null
                                binding.selectedSlot.text = ""
                                binding.selectedDate.text = selectedDate.toString()
                                //Call check function----
                                getSlotDetails(model)
                            }

                        }) }
                    selectedDate = dateList[0].toString("EEE, dd MMM yyyy")
                    binding.selectedDate.text = selectedDate.toString()
                    binding.dateList.adapter = dateAdapter
                    dateAdapter?.notifyDataSetChanged()
                }
                else{
                    //binding.noCouponText.visibility = View.VISIBLE
                }
                val it = 9
            }
            catch (e: Exception){

            }
        }

    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun addDaysToDate(dt: Date, days: Int) :Date{
        val c = Calendar.getInstance()
        c.time = dt
        c.add(Calendar.DATE, days)
        return  c.time

    }
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }
}