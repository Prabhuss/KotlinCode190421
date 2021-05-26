package com.getpy.dikshasshop.ui.referandearn

import androidx.lifecycle.ViewModel
import com.getpy.dikshasshop.data.model.GetOrderResponse
import com.getpy.dikshasshop.data.model.GetReferenceResponse
import com.getpy.dikshasshop.data.repositories.UserRepository

class ReferPageViewModel(val repository: UserRepository) : ViewModel()  {

    suspend fun getReferenceData(access_key:String, phone_number: String, merchant_id:Int): GetReferenceResponse =
            repository.getReferenceData(access_key,phone_number,merchant_id)
}