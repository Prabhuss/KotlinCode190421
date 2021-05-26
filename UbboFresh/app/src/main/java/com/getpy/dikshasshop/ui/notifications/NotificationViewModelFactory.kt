package com.getpy.dikshasshop.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getpy.dikshasshop.data.repositories.UserRepository

@Suppress("UNCHECKED_CAST")
class NotificationViewModelFactory(
    private val repository: UserRepository):ViewModelProvider.NewInstanceFactory()
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NotificationViewModel(repository) as T
    }
}