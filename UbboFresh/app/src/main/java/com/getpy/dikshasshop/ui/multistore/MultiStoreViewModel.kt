package com.getpy.dikshasshop.ui.multistore

import androidx.lifecycle.ViewModel
import com.getpy.dikshasshop.data.model.MultiStoreResponse
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.data.repositories.UserRepository
import retrofit2.http.Field

class MultiStoreViewModel( val preference:PreferenceProvider, val repository: UserRepository): ViewModel() {

    suspend fun getStoreDetailsforMultiStore (
            @Field("access_key") access_key:String,
            @Field("phone_number") phoneNumer:String,
            @Field("merchant_id") merchantId:Int,
            @Field("latitude") lati: String?,
            @Field("longitude") longi: String?
    ) : MultiStoreResponse = repository.getStoreDetailsforMultiStore(
            access_key,phoneNumer,merchantId,lati,longi
    )


}