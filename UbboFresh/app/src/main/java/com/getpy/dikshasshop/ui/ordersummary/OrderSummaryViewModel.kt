package com.getpy.dikshasshop.ui.ordersummary

import androidx.lifecycle.ViewModel
import com.getpy.dikshasshop.data.model.MerAppSettingsDetailsResponse
import com.getpy.dikshasshop.data.model.OrderResponse
import com.getpy.dikshasshop.data.model.ReOrderInvoiceItems
import com.getpy.dikshasshop.data.repositories.UserRepository

class OrderSummaryViewModel(val repository: UserRepository) : ViewModel()
{
    suspend fun reorderInvoiceItems(
        merchantbranchid:Int,
        phone_number:String,
        access_key:String,
        invoice_id:String): ReOrderInvoiceItems
    {
        return repository.reorderInvoiceItems(merchantbranchid, phone_number, access_key, invoice_id)
    }


    suspend fun updateStatusAfterPayment(mid:Int,pnum:String,ackey:String,orderId: String,onlinePaymentId: String,orderStatus: String): OrderResponse
    {
        return repository.updateStatusAfterPayment(mid,pnum,ackey,orderId,onlinePaymentId,orderStatus)
    }
    suspend fun merchantAppSettingDetails(mid:Int,setting_name:String,pnum:String,ackey:String): MerAppSettingsDetailsResponse
    {
        return repository.merchantAppSettingDetails1(mid,setting_name,pnum,ackey)
    }
}