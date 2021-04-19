package com.getpy.dikshasshop.ui.multistore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getpy.dikshasshop.data.preferences.PreferenceProvider
import com.getpy.dikshasshop.data.repositories.UserRepository

@Suppress("UNCHECKED_CAST")
class MultiStoreViewModelFactory(
    private val reference:PreferenceProvider,private val repository: UserRepository):ViewModelProvider.NewInstanceFactory()
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MultiStoreViewModel(reference,repository) as T
    }
}