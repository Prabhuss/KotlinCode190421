package com.getpy.dikshasshop.ui.contactmerchant

import androidx.lifecycle.ViewModel
import com.getpy.dikshasshop.data.repositories.UserRepository

class ContactViewModel(val repository: UserRepository) : ViewModel()
{
    suspend fun getContactDetails(merchantbranchid:Int, phone_number:String, access_key:String)=repository.getContactDetails(
            merchantbranchid, phone_number, access_key)
}