package com.getpy.dikshasshop.data.model

data class CategoriesExpModel (
    var mMainAndSubCatDataModel:MainAndSubCatDataModel?=null,
    var list:ArrayList<MainAndSubCatDataModel>?=null,
    var Expandable:Boolean=false
)