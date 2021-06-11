package com.getpy.dikshasshop.data.model

class PaymentModeResponse (
    var apiId:String?=null,
    var SecretKey:String?=null,
    var OnlinePaymentFlag: String? = null,
    var PaymentMode: String? = null,
    var PaymentOptions: ArrayList<String>? = null
)