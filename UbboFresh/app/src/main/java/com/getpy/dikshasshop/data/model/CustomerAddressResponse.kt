package com.getpy.dikshasshop.data.model

import com.getpy.dikshasshop.data.db.entities.CustomerAddressData
import java.util.*

data class CustomerAddressResponse (
        var data: ArrayList<CustomerAddressData>?,
        var status: String?
)