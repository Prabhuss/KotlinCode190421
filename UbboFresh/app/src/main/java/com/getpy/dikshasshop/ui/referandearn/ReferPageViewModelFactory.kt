package com.getpy.dikshasshop.ui.referandearn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getpy.dikshasshop.data.repositories.UserRepository

@Suppress("UNCHECKED_CAST")
class ReferPageViewModelFactory (
        private val repository: UserRepository): ViewModelProvider.NewInstanceFactory()
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReferPageViewModel(repository) as T
    }
}