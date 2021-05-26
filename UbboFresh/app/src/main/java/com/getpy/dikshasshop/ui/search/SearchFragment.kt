package com.getpy.dikshasshop.ui.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.microsoft.appcenter.analytics.Analytics
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.*
import com.getpy.dikshasshop.adapter.PostRecyclerAdapter
import com.getpy.dikshasshop.data.db.AppDataBase
import com.getpy.dikshasshop.data.db.entities.ProductsDataModel
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.FragmentSearchBinding
import com.getpy.dikshasshop.listeners.PaginationListener
import com.getpy.dikshasshop.listeners.PaginationListener.PAGE_START
import com.getpy.dikshasshop.ui.home.InjectionFragment
import com.getpy.dikshasshop.ui.main.MainActivity
import com.getpy.fresh.views.Products.ProductsFragment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : InjectionFragment(), OnRefreshListener {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding:FragmentSearchBinding
    var adapter:PostRecyclerAdapter?=null
    lateinit var viewmodel: SearchViewModel
    private val factory: SearchViewModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    private val database: AppDataBase by instance()

    private var currentPage: Int = PAGE_START
    private var isLastPage = false
    private val totalPage = 10
    private var isLoading = false
    var itemCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_search, container, false)
        viewmodel= activity?.let { ViewModelProviders.of(it,factory).get(SearchViewModel::class.java) }!!

        activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        binding.nodata.hideView()


        val gridLayout = GridLayoutManager(activity?.applicationContext, 2)
        binding.recyclerview.setItemAnimator(null);
        binding.recyclerview.setLayoutManager(gridLayout)
        binding.recyclerview.setHasFixedSize(true)

        adapter = activity?.let {
            activity?.supportFragmentManager?.let { it1 ->
                PostRecyclerAdapter(preference,it1, it, ArrayList<ProductsDataModel>())
            }
        }
        binding.recyclerview.setAdapter(adapter)

        //ButterKnife.bind(this);
        binding.swipeRefresh.setOnRefreshListener(this)
        binding.swipeRefresh.isEnabled=false
        /**
         * add scroll listener while user reach in bottom load more will call
         */
        binding.recyclerview.addOnScrollListener(object : PaginationListener(gridLayout) {
            override fun isLastPage(): Boolean {
                return this@SearchFragment.isLastPage
            }

            override  protected fun loadMoreItems() {
                this@SearchFragment.isLoading = true
                currentPage++
                fetchData(binding.serach.text.toString())
            }

            override fun isLoading(): Boolean {
                return this@SearchFragment.isLoading
            }

        })

        binding.serach.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if(checkStringIsEmpty(binding.serach.text.toString()))
                {
                    activity?.toast("search field should not be empty")
                }else
                {
                    hideKeyboard()
                    itemCount = 0
                    currentPage = PAGE_START
                    isLastPage = false
                    adapter?.clear()
                    binding.pbar.show()

                    val map=HashMap<String,String>()
                    map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
                    map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
                    map.put("SearchItem", binding.serach.text.toString())
                    Analytics.trackEvent("Search Clicked", map)

                    fetchData(binding.serach.text.toString())


                }
                return@OnEditorActionListener true
            }
            false
        })
        binding.searchImg.setOnClickListener(View.OnClickListener {
            if(checkStringIsEmpty(binding.serach.text.toString()))
            {
                activity?.toast("search field should not be empty")
            }else
            {
                hideKeyboard()
                itemCount = 0
                currentPage = PAGE_START
                isLastPage = false
                adapter?.clear()
                binding.pbar.show()

                val map=HashMap<String,String>()
                map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
                map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
                map.put("SearchItem", binding.serach.text.toString())
                Analytics.trackEvent("Search Clicked", map)

                fetchData(binding.serach.text.toString())

            }
        })
        binding.back.setOnClickListener(View.OnClickListener {
            hideKeyboard()
            MainActivity.binding.activityMainAppbarlayout.showView()
            UbboFreshApp.instance?.isSearchBoxclicked=false
            activity?.onBackPressed()
        })


        ProductsFragment.runnable = Runnable {
            lifecycleScope.launch {
                try {
                    UbboFreshApp.instance?.carItemsList?.let { database.CustomerAddressDao().insertProductsData(it) }
                }catch (e:CancellationException)
                {
                    Log.i("scope","job is canceled")
                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                }

            }
        }
        ProductsFragment.removerunnable = Runnable {
            lifecycleScope.launch {
                try {
                    UbboFreshApp.instance?.productsDataModel?.let { database.CustomerAddressDao().deleteProductsData(it) }
                }catch (e:CancellationException)
                {
                    Log.i("scope","job is canceled")
                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                }

            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }
    fun fetchData(searchName:String)
    {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val prodResponse = viewmodel.getSearchProducts(
                    preference.getStringData(Constants.saveMobileNumkey),
                    preference.getStringData(Constants.saveaccesskey),
                    preference.getIntData(Constants.saveMerchantIdKey),
                    searchName,
                    20,
                   currentPage)
                for(i in 0 until prodResponse?.data!!.size)
                {
                    prodResponse.data?.get(i)?.mobileNumber=preference.getStringData(Constants.saveMobileNumkey)
                }
                binding.pbar.dismiss()
                val data = prodResponse.data

                // do this all stuff on Success of APIs response
                /**
                 * manage progress view
                 */
                if (currentPage != PAGE_START) adapter!!.removeLoading()
                adapter?.addItems(data)
                binding.swipeRefresh.isRefreshing = false
                if(adapter?.itemCount!! >0)
                {
                    binding.nodata.hideView()
                }else
                {
                    binding.nodata.showView()
                }

                // check weather is last page or not

                // check weather is last page or not
                if (data.size>=9) {
                    adapter?.addLoading()
                } else {
                    adapter?.LoadingItems()
                    isLastPage = true
                }
                isLoading = false

                /*if (data != null) {
                    if (data.size > 0) {
                        binding.nodata.visibility = View.GONE
                        val latestAditionsAdapter = activity?.let {
                            activity?.supportFragmentManager?.let { it1 ->
                                ProductItemsAdapter(it1, it, data)
                            }
                        }
                        binding.recyclerview.setAdapter(latestAditionsAdapter)
                    } else {
                        binding.nodata.visibility = View.VISIBLE
                    }
                }*/
            } catch (e: NoInternetExcetion) {
                activity?.networkDialog()
            } catch (e: CancellationException) {
                Log.i("scope", "job is canceled")
            } catch (e: Exception) {
                activity?.okDialogWithOneAct("Error", e.message.toString())
            }
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters

        var runnable:Runnable?=null
        var removerunnable:Runnable?=null

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onRefresh() {
        /*itemCount = 0
        currentPage = PAGE_START
        isLastPage = false
        adapter?.clear()*/
        binding.swipeRefresh.isRefreshing=false
        //doApiCall()
    }
}