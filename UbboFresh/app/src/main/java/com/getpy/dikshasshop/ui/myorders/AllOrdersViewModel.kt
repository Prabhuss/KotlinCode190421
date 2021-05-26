package com.getpy.dikshasshop.ui.myorders

import androidx.lifecycle.ViewModel
import com.getpy.dikshasshop.data.model.GetOrderResponse
import com.getpy.dikshasshop.data.repositories.UserRepository
class AllOrdersViewModel(val repository: UserRepository) : ViewModel() {
    // TODO: Implement the ViewModel

    suspend fun callAllOrders(access_key:String, phone_number: String, merchant_id:Int,
        start_date:String, page_size:Int, page_number:Int, end_date:String):GetOrderResponse=
        repository.getOrders(access_key,phone_number,merchant_id,start_date,page_size,page_number, end_date)
}