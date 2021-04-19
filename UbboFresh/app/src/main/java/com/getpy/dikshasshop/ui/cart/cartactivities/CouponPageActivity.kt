package com.getpy.dikshasshop.ui.cart.cartactivities

import android.os.Bundle
import android.text.Layout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.databinding.ActivityCouponPageBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein

class CouponPageActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    lateinit var binding: ActivityCouponPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_coupon_page)
        //setContentView(R.layout.activity_coupon_page)
    }
}