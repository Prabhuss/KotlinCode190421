package com.getpy.dikshasshop.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class CouponDataModel (

    val Id: String?,

    var MerchantBranchId: String? = null,

    val CouponCode: String?,

    val StartsFrom: String?,

    val ExpiresOn: String?,

    val CouponInfo: String?,

    val TnCDetais: String?,

    val DiscountType: String?,

    val DiscountValue: String?,

    val MaxDiscount: String?,

    val MinAmount: String?,

    val IsDelete: String?,

    val IsActive: String?,

    val CreatedDate: String?,

    val CreatedBy: String?,

    val ModifiedDate: String?
    )

