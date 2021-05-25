package com.getpy.dikshasshop.data.model

class SlotDetailsResponse (
    var status: String? = null,
    var deliverywindow: String? = null,
    var data : MutableList<SlotDetailsData>? = null
)

class SlotDetailsData(
        var slotTimings:String? = null,
        var slotId:String? = null,
        var slotStatus : String? = null
)