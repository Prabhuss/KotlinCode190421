package com.getpy.dikshasshop.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.Utils.Constants
import com.getpy.dikshasshop.data.db.entities.CustomerAddressData
import com.getpy.dikshasshop.databinding.CustomerAddrRowBinding
import com.getpy.dikshasshop.ui.cart.cartactivities.AddAddressActivity

class CustomerAddressAdapter(var context:Context, var mCategoriesList:List<CustomerAddressData>): RecyclerView.Adapter<CustomerAddressAdapter.DeveloperViewHolder>() {
    var selectedmodel:CustomerAddressData?=null
    var isSelected:Boolean?=false
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DeveloperViewHolder {
        val mDeveloperListItemBinding = DataBindingUtil.inflate<CustomerAddrRowBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.customer_addr_row, viewGroup, false)
        return DeveloperViewHolder(mDeveloperListItemBinding)
    }

    override fun onBindViewHolder(mDeveloperViewHolder: DeveloperViewHolder, i: Int) {
        val model = mCategoriesList.get(i)
        if(model.TagName.equals(Constants.address1))
        {
            mDeveloperViewHolder.mBinding.title.text="Home Address"
        }
        if(model.TagName.equals(Constants.address2))
        {
            mDeveloperViewHolder.mBinding.title.text="Work address"
        }
        if(model.TagName.equals(Constants.address3))
        {
            mDeveloperViewHolder.mBinding.title.text="Other Address"
        }
        if(model.TagName.equals(Constants.pickUp))
        {
            mDeveloperViewHolder.mBinding.title.text="Pick up from store"
            mDeveloperViewHolder.mBinding.change.visibility = View.GONE
            mDeveloperViewHolder.mBinding.landmarkLayout.visibility = View.GONE
            mDeveloperViewHolder.mBinding.address.visibility = View.GONE
        }
        mDeveloperViewHolder.mBinding.radioBut.isChecked = model.ischecked
        mDeveloperViewHolder.mBinding.name.text=model.FirstName
        mDeveloperViewHolder.mBinding.address.text=model.Address1
        mDeveloperViewHolder.mBinding.landmarkText.text=model.Address2
    }

    override fun getItemCount(): Int {
        return if (mCategoriesList != null) {
            mCategoriesList.size
        } else {
            0
        }
    }

    fun getModel():CustomerAddressData?
    {
        return selectedmodel
    }

    inner class DeveloperViewHolder(var mBinding: CustomerAddrRowBinding) :
        RecyclerView.ViewHolder(mBinding.root)
    {
        init {
            mBinding.name.setTypeface(UbboFreshApp.instance?.latoregular)
            mBinding.address.setTypeface(UbboFreshApp.instance?.latoregular)
            mBinding.title.setTypeface(UbboFreshApp.instance?.latoregular)
            mBinding.change.setTypeface(UbboFreshApp.instance?.latoregular)
            mBinding.change.setOnClickListener(View.OnClickListener {
                try {
                    val intent=Intent(context, AddAddressActivity::class.java)
                    intent.putExtra("model",mCategoriesList.get(adapterPosition))
                    intent.putExtra("callType","change")
                    context.startActivity(intent)
                }catch (e:Exception)
                {
                    e.printStackTrace()
                }

            })
            mBinding.radioBut.setOnClickListener(View.OnClickListener {
                val model=mCategoriesList.get(adapterPosition)
                for(i in 0 until mCategoriesList.size)
                {
                    val model1=mCategoriesList.get(i)
                    if(model.TagName.equals(model1.TagName))
                    {
                        model.ischecked=true
                        selectedmodel=model
                        getDeliveryCharges(selectedmodel!!.ID)
                    }else
                    {
                        model1.ischecked=false
                    }
                }
                notifyDataSetChanged()
            })
        }
    }
    private fun getDeliveryCharges(addressId: String?) {

        val builder = AlertDialog.Builder(context)

        val dialogView =  LayoutInflater.from(context).inflate(R.layout.progress_dialog, null, false)
        val message = dialogView.findViewById<TextView>(R.id.message)
        message.text = "Loading..."
        builder.setView(dialogView)
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()

        Handler().postDelayed({dialog.dismiss()},2500)
        val addressId: String? = addressId
        val intent = Intent("custom-message")
        intent.putExtra("addressId", addressId)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}