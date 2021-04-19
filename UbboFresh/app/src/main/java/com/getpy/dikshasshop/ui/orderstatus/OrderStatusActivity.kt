package com.getpy.dikshasshop.ui.orderstatus

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.data.db.AppDataBase
import com.getpy.dikshasshop.databinding.ActivityOrderStatusBinding
import com.getpy.dikshasshop.ui.main.MainActivity
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class OrderStatusActivity : AppCompatActivity(),KodeinAware {
    var status:String=""
    override val kodein by kodein()
    private val appDataBase: AppDataBase by instance()
    lateinit var binding:ActivityOrderStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_order_status)
        status=intent.getStringExtra("status")?:"Failure"
        if(status.equals("success"))
        {
            lifecycleScope.launch {
                UbboFreshApp.instance?.carItemsList?.let { appDataBase.CustomerAddressDao().deleteListOfProductsData(it) }
                UbboFreshApp.instance?.carItemsList?.clear()
                UbboFreshApp.instance?.hashMap?.clear()
            }
            binding.checkImage.setImageResource(R.drawable.ic_check_selected)
            var orderStatusSubMessage = intent.getStringExtra("message")?:"Order Placed Successfully"
            var orderStatusMessage = intent.getStringExtra("caption")?:resources.getString(R.string.suc_str)
            binding.thankyou.setText(orderStatusMessage)
            binding.suc.setText(orderStatusSubMessage)
        }else
        {
            binding.checkImage.setImageResource(R.drawable.ic_error)
            var orderStatusSubMessage = intent.getStringExtra("message")?:"Order Failed"
            var orderStatusMessage = intent.getStringExtra("caption")?:resources.getString(R.string.fail_str)
            binding.thankyou.setText(orderStatusMessage)
            binding.suc.setText(orderStatusSubMessage)
            binding.suc.setTextColor(resources.getColor(R.color.family_red))
        }

        binding.continueShopping.setOnClickListener(View.OnClickListener {
            navigateToMain(status)
        })

        binding.back.setOnClickListener(View.OnClickListener {
            navigateToMain(status)
        })

        init()
    }
    fun init()
    {
        binding.thankyou.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.suc.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.continueShopping.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.ordersStatus.setTypeface(UbboFreshApp.instance?.latoregular)
    }
    fun navigateToMain(status:String)
    {
        if(status.equals("Failure"))
        {
            finish()
        }else
        {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMain(status)
    }
}