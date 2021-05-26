package com.getpy.dikshasshop.ui.myorders.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.microsoft.appcenter.analytics.Analytics
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.Utils.Constants
import com.getpy.dikshasshop.adapter.MyOrdersAdapter
import com.getpy.dikshasshop.data.model.CustomerInvoiceData
import com.getpy.dikshasshop.data.model.InvocieLineItems
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.AllOrdersFragmentBinding
import com.getpy.dikshasshop.listeners.ItemClickListener
import com.getpy.dikshasshop.ui.home.InjectionFragment
import com.getpy.dikshasshop.ui.myorders.AllOrdersViewModel
import com.getpy.dikshasshop.ui.myorders.MyOrdersModelFactory
import com.getpy.dikshasshop.ui.ordersummary.OrderSummaryActivity
import org.kodein.di.generic.instance
import java.util.HashMap


class AllOrdersFragment(val allOrdersList:ArrayList<CustomerInvoiceData>,val invoieList: ArrayList<InvocieLineItems>) : InjectionFragment(),SwipeRefreshLayout.OnRefreshListener {
    private val factory: MyOrdersModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    private lateinit var viewModel: AllOrdersViewModel
    private lateinit var binding:AllOrdersFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding=DataBindingUtil.inflate(LayoutInflater.from(activity),R.layout.all_orders_fragment, container, false)
        viewModel = ViewModelProvider(this,factory).get(AllOrdersViewModel::class.java)
        // binding.swipeRefresh.setOnRefreshListener(this)
        // binding.swipeRefresh.isEnabled=false
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.layoutManager=layoutManager
        val adapater= allOrdersList.let { activity?.let { it1 ->
            MyOrdersAdapter(it1,it,object :ItemClickListener{
                override fun onItemClick(view: View?, position: Int) {
                    val model=allOrdersList.get(position)

                    val map= HashMap<String,String>()
                    map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
                    map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
                    map.put("PaymentMode", model.PaymentMode.toString())
                    map.put("InvoiceId", model.InvoiceId.toString())
                    map.put("TotalAmount", model.TotalInvoiceAmount.toString())
                    Analytics.trackEvent("Order Summary clicked", map)

                    allOrdersData(model)
                }

            }) } }
        binding.recyclerview.setAdapter(adapater)
        /*binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.setItemAnimator(null)
        val mananger=activity?.supportFragmentManager

        if (MyOrdersActivity.currentPage == PaginationListener.PAGE_START) {
            adapater = ArrayList<CustomerInvoiceData>().let { activity?.let { it1 -> mananger?.let { it2 -> MyOrdersRecyclerAdapter(it2, it1, it) } } }
            binding.recyclerview.adapter = adapater
        }
        if (MyOrdersActivity.currentPage != PaginationListener.PAGE_START) adapater?.removeLoading()
        for(i in 0 until allOrdersList!!.size)
        {
            val model= allOrdersList?.get(i)
            model?.let { adapater?.mPostItems?.add(it) }
        }
        binding.recyclerview.setAdapter(adapater)
        adapater?.notifyDataSetChanged()
        binding.swipeRefresh.isRefreshing = false

        if (allOrdersList.size>0) {
            adapater!!.addLoading()
        } else {
            MyOrdersActivity.isLastPage = true
        }
        MyOrdersActivity.isLoading = false*/



        /*binding.recyclerview.addOnScrollListener(object : PaginationListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return ProductsFragment.isLastPage
            }

            override  protected fun loadMoreItems() {
                MyOrdersActivity.isLoading = true
                MyOrdersActivity.currentPage++
                Handler().postDelayed(MyOrdersActivity.loadRunnable!!,10)

            }

            override fun isLoading(): Boolean {
                return ProductsFragment.isLoading
            }

        })*/

        return binding.root
    }

    fun allOrdersData(model:CustomerInvoiceData)
    {
        val ilist=ArrayList<InvocieLineItems>()
        for(i in 0 until invoieList.size)
        {
            val imodel=invoieList.get(i)
            if(model.CustomerInvoiceId==imodel.CustomerInvoiceId)
            {
                ilist.add(imodel)
            }

        }
        for(i in 0 until invoieList.size)
        {
            val imodel=invoieList.get(i)
            if(model.CustomerInvoiceId==imodel.CustomerInvoiceId)
            {
                val intent= Intent(activity,OrderSummaryActivity::class.java)
                intent.putExtra("list",ilist)
                intent.putExtra("model",model)
                startActivity(intent)
                break
            }

        }
    }
    companion
    object {
        var runnable:Runnable?=null
        fun newInstance(allOrderList: ArrayList<CustomerInvoiceData>,invoieList: ArrayList<InvocieLineItems>) =
                AllOrdersFragment(allOrderList,invoieList)
    }


    override fun onRefresh() {
        /*itemCount = 0
        currentPage = PAGE_START
        isLastPage = false
        adapter?.clear()*/
        //binding.swipeRefresh.isRefreshing=false
        //doApiCall()
    }
}