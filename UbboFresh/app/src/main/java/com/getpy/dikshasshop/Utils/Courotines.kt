package com.getpy.dikshasshop.Utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Courotines{
    fun main(work:suspend(()->Unit))=
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }
}