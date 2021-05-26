package com.getpy.dikshasshop.ui.Products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.data.repositories.UserRepository

@Suppress("UNCHECKED_CAST")
class ProductsViewModelFactory(
    private val preference:PreferenceProvider,private val repository: UserRepository):ViewModelProvider.NewInstanceFactory()
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProductsViewModel(preference,repository) as T
    }
}