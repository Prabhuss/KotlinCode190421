package com.getpy.dikshasshop.ui.notifications

import androidx.lifecycle.ViewModel
import com.getpy.dikshasshop.data.model.CampaignCustomerNotificationDetails
import com.getpy.dikshasshop.data.repositories.UserRepository

class NotificationViewModel(val repository: UserRepository) : ViewModel()
{
    suspend fun campaignCustomerNotificationDetails(
        merapchantbranchid:Int,
        phone_number:String,
        access_key:String,
        page_size:Int,
        page_number:Int
    ): CampaignCustomerNotificationDetails
    {
        return repository.campaignCustomerNotificationDetails(
            merapchantbranchid, phone_number, access_key, page_size, page_number)
    }
}