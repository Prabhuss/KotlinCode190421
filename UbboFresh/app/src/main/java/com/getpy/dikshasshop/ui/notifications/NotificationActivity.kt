package com.getpy.dikshasshop.ui.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.Utils.Constants
import com.getpy.dikshasshop.Utils.NoInternetExcetion
import com.getpy.dikshasshop.Utils.okDialogWithOneAct
import com.getpy.dikshasshop.Utils.snakBar
import com.getpy.dikshasshop.adapter.NoticationsAdater
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityNotificationBinding
import com.getpy.dikshasshop.ui.main.MainActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class NotificationActivity : AppCompatActivity() , KodeinAware
{
    override val kodein by kodein()
    private val factory: NotificationViewModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    lateinit var binding:ActivityNotificationBinding
    lateinit var viewModel:NotificationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_notification)
        viewModel=ViewModelProviders.of(this,factory).get(NotificationViewModel::class.java)
        init()
        callNotifications()
        binding.icback.setOnClickListener{
            finish()
        }
    }
    fun init()
    {
        binding.recyclervew.setHasFixedSize(true)
    }
    fun callNotifications()
    {
        lifecycleScope.launch {
            try {
                val response=viewModel.campaignCustomerNotificationDetails(
                        preference.getIntData(Constants.saveMerchantIdKey),
                        preference.getStringData(Constants.saveMobileNumkey),
                        preference.getStringData(Constants.saveaccesskey),
                        100,1)
                if(response.status?.toLowerCase().equals(Constants.status))
                {
                    val adapater =
                            response.data?.let {
                                NoticationsAdater(this@NotificationActivity, it)}
                    binding.recyclervew.adapter = adapater
                }else {
                    okDialogWithOneAct(Constants.appName,response.message.toString())
                }
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
}