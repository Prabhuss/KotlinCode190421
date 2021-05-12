package com.getpy.dikshasshop.ui.referandearn

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.Constants
import com.getpy.dikshasshop.Utils.dismiss
import com.getpy.dikshasshop.Utils.show
import com.getpy.dikshasshop.Utils.toast
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.databinding.ActivityReferPageBinding
import com.microsoft.appcenter.analytics.Analytics
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class ReferPageActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    lateinit var binding: ActivityReferPageBinding
    private val factory: ReferPageViewModelFactory by instance()
    private val preference: PreferenceProvider by instance()
    lateinit var viewModel: ReferPageViewModel
    var appLink = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refer_page)
        viewModel = ViewModelProviders.of(this, factory).get(ReferPageViewModel::class.java)
        binding.progBar.show()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.icWhatsappBtn.setOnClickListener {
            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = "text/plain"
            whatsappIntent.setPackage("com.whatsapp")
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, appLink)
            val map= HashMap<String, String>()
            map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
            map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
            map.put("Refer Type", "Shared over WhatsApp")
            Analytics.trackEvent("Refer Clicked", map)
            try {
                startActivity(whatsappIntent)
            } catch (ex: ActivityNotFoundException) {
                toast("WhatsApp have not been installed.")
            }
        }
        binding.icCopyBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(appLink, appLink)
                clipboard.setPrimaryClip(clip)
                val map= HashMap<String, String>()
                map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
                map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
                map.put("Refer Type", "Refer link copied")
                Analytics.trackEvent("Refer Clicked", map)
                toast("Link Copied")
            } else {
                toast("Permission to copy denied.")
            }
        }
        binding.icMsgShareBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, appLink)
            intent.type = "text/plain"
            val map= HashMap<String, String>()
            map.put("mobileNum",preference.getStringData(Constants.saveMobileNumkey))
            map.put("merchantid", preference.getIntData(Constants.saveMerchantIdKey).toString())
            map.put("Refer Type", "Refer link Shared")
            Analytics.trackEvent("Refer Clicked", map)
            startActivity(Intent.createChooser(intent, "Please select app: "))
        }
        getReferPageContentFromAPI()
    }
    fun getReferPageContentFromAPI(){

        val pageTitle =intent.extras?.getString("PageTitle")
        val couponConfigured =intent.extras?.getString("CouponConfigured")
        val shareLinkTxt =intent.extras?.getString("ShareLinkTxt")
        val referDisplayMsg =intent.extras?.getString("ReferDisplayMsg")
        binding.displayMsg.text = referDisplayMsg
        appLink = shareLinkTxt.toString()
        if(couponConfigured.toString().toLowerCase() == "share"){
            binding.refProcessLayout.visibility = View.GONE
        }
        if(pageTitle!= null){
            binding.refPageTitle.text = pageTitle.toString()
        }
        Handler().postDelayed({binding.progBar.dismiss()},100)

    }

    override fun onBackPressed(){
        super.onBackPressed()
    }
}