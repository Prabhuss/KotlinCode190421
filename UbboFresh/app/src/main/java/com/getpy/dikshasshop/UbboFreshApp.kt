package com.getpy.dikshasshop

import android.app.Application
import android.graphics.Typeface
import com.getpy.dikshasshop.data.db.AppDataBase
import com.getpy.dikshasshop.data.db.entities.ProductsDataModel
import com.getpy.dikshasshop.data.model.CategoriesExpModel
import com.getpy.dikshasshop.data.model.MainAndSubCatDataModel
import com.getpy.dikshasshop.data.network.MyApi
import com.getpy.dikshasshop.data.network.NetworkConnectionInterceptor
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.data.repositories.UserRepository
import com.getpy.dikshasshop.ui.Products.ProductsViewModelFactory
import com.getpy.dikshasshop.ui.account.AccountViewModelFactory
import com.getpy.dikshasshop.ui.auth.AuthViewModelFactory
import com.getpy.dikshasshop.ui.cart.CartViewModelFactory
import com.getpy.dikshasshop.ui.contactmerchant.ContactViewModelFactory
import com.getpy.dikshasshop.ui.home.HomeViewModelFactory
import com.getpy.dikshasshop.ui.main.MainViewModelFactory
import com.getpy.dikshasshop.ui.multistore.MultiStoreViewModelFactory
import com.getpy.dikshasshop.ui.myorders.MyOrdersModelFactory
import com.getpy.dikshasshop.ui.notifications.NotificationViewModelFactory
import com.getpy.dikshasshop.ui.ordersummary.OrderSumModelFactory
import com.getpy.dikshasshop.ui.search.SearchViewModelFactory
import com.getpy.dikshasshop.ui.shopping.OffersViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class UbboFreshApp : Application(),KodeinAware{
    companion object{
        var instance:UbboFreshApp?=null

    }
    override fun onCreate() {
        super.onCreate()
        instance=this
    }

    var instructionString:String?=null
    var couponDiscontAmount:Double?=null
    var couponApplied:String?=null

    var isComingFromReOrder:Boolean=false
    var isSearchBoxclicked:Boolean=false
    var isCmgfromHomeItemClick:Boolean=false
    var isComingfromCatOrMen:Boolean=false
    var isSubCatPos:Int=0

    var mainAndSubCatDataModel:MainAndSubCatDataModel?=null
    var productsDataModel:ProductsDataModel?=null
    var pordlist:ArrayList<ProductsDataModel>?=null
    var carItemsList:MutableList<ProductsDataModel>?=null
    var hashMap=HashMap<String, ProductsDataModel>()
    var totCatList:ArrayList<CategoriesExpModel>?=null

    var imageLoadUrl:String?=null

    var latoblack: Typeface? = null
    var latoblackitalic: Typeface? = null
    var latobold: Typeface? = null
    var latobolditalic: Typeface? = null
    var latoheavy: Typeface? = null
    var latoheavyitalic: Typeface? = null
    var latoitalic: Typeface? = null
    var latolight: Typeface? = null
    var latolightitalic: Typeface? = null
    var latomedium: Typeface? = null
    var latoregular: Typeface? = null
    var latomediumitalic: Typeface? = null
    var latosemibold: Typeface? = null
    var latosemibolditalic: Typeface? = null
    var latothin: Typeface? = null
    var latothinitalic: Typeface? = null
    override val kodein: Kodein= Kodein.lazy {
        import(androidXModule(this@UbboFreshApp))

        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { PreferenceProvider(instance()) }
        bind() from singleton { MyApi(instance()) }
        bind() from singleton { AppDataBase(instance()) }
        bind() from singleton { UserRepository(instance(),instance()) }
        bind() from singleton { AuthViewModelFactory(instance(),instance()) }
        bind() from singleton { MultiStoreViewModelFactory(instance(),instance()) }
        bind() from singleton { MainViewModelFactory(instance()) }
        bind() from singleton { ProductsViewModelFactory(instance(), instance()) }
        bind() from singleton { AccountViewModelFactory(instance()) }
        bind() from singleton { MyOrdersModelFactory(instance()) }
        bind() from singleton { ContactViewModelFactory(instance()) }
        bind() from singleton { HomeViewModelFactory(instance()) }
        bind() from singleton { CartViewModelFactory(instance()) }
        bind() from singleton { SearchViewModelFactory(instance(),instance()) }
        bind() from singleton { OffersViewModelFactory(instance()) }
        bind() from singleton { OrderSumModelFactory(instance()) }
        bind() from singleton { NotificationViewModelFactory(instance()) }


    }


}