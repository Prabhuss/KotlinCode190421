package com.getpy.dikshasshop.data.db.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import java.io.Serializable

@Entity
data class CustomerAddressData(
        var ID: String?,
        @SerializedName("PostalCode/ZipCode")
        var PostalCodeZipCode: String?,
        var StoreCustomerId: String?,
        @PrimaryKey
        var TagName: String,
        var PrimaryPhone: String?,
        var FirstName: String?,
        var Address2: String?,
        var Latitude: String?,
        var Address1: String?,
        @SerializedName(value = "FlatNo/DoorNo")
        var FlatNo_DoorNo: String?,
        var City: String?,
        var Longitude: String?,
        var SecondaryPhone: String?,
        var Area: String?,
        var State: String?,
        var Country: String?,
        @SerializedName(value = "Society/BuildingNo")
        var Society_BuildingNo: String?,
        var MerchantBranchId: String?
):Serializable
{
        var ischecked:Boolean=false
}