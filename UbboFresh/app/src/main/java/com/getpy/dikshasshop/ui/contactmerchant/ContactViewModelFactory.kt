package com.getpy.dikshasshop.ui.contactmerchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getpy.dikshasshop.data.repositories.UserRepository

@Suppress("UNCHECKED_CAST")
class ContactViewModelFactory(
    private val repository: UserRepository):ViewModelProvider.NewInstanceFactory()
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ContactViewModel(repository) as T
    }
}