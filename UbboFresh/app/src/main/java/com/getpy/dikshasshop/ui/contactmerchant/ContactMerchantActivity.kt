package com.getpy.dikshasshop.ui.contactmerchant

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.Constants
import com.getpy.dikshasshop.Utils.NoInternetExcetion
import com.getpy.dikshasshop.Utils.networkDialog
import com.getpy.dikshasshop.Utils.okDialogWithOneAct
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityContactMerchantBinding
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class ContactMerchantActivity : AppCompatActivity(),KodeinAware {
    override val kodein by kodein()
    private val factory: ContactViewModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    lateinit var binding:ActivityContactMerchantBinding
    lateinit var viewModel: ContactViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_contact_merchant)
        viewModel=ViewModelProviders.of(this,factory).get(ContactViewModel::class.java)
        init()
        getContactDetails()

        binding.back.setOnClickListener(View.OnClickListener {
            finish()
        })
        binding.callNum.setOnClickListener(View.OnClickListener {
            call(binding.callNum.text.toString())
        })
        binding.sendUsMsgTxt.setOnClickListener(View.OnClickListener {
            openMessage(binding.sendUsMsgTxt.text.toString())
        })
    }

    fun init()
    {
        binding.shopName.setTypeface(UbboFreshApp.instance?.latobold)
        binding.sendUsMsgTxt.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.callNum.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.locAddr.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.timings.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.storeTiming.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.giveUsCall.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.sendUsMesage.setTypeface(UbboFreshApp.instance?.latoregular)

    }
    fun call(mobilenum:String) {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:" + mobilenum)
        startActivity(dialIntent)
    }
    fun openMessage(mail:String)
    {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email")
        i.putExtra(Intent.EXTRA_TEXT, "body of email")
        try {
            startActivity(Intent.createChooser(i, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this@ContactMerchantActivity, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }
    fun getContactDetails()
    {
        lifecycleScope.launch {
            try {
                val response=viewModel.getContactDetails(preference.getIntData(Constants.saveMerchantIdKey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        preference.getStringData(Constants.saveaccesskey))

                binding.timings.text=response.OpenHours
                binding.locAddr.text= response.Location
                binding.callNum.text=response.ContactNumber
                binding.sendUsMsgTxt.text=response.Email
                binding.shopName.text=response.ShopName

            }catch (e: NoInternetExcetion)
            {
                networkDialog()
            }catch (e:Exception)
            {
                okDialogWithOneAct("Error",e.message.toString())
            }

            //val contactAdater=ContactsAdapter(response.)
            //binding.recyclerivew.adapter=contactAdater
        }
    }
}