package com.fishhawk.driftinglibraryandroid.ui.subscription

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.Subscription
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListViewModel
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val repository: RemoteSubscriptionRepository
) : RefreshableListViewModel<Subscription>() {
    override suspend fun loadResult() = repository.getAllSubscriptions()

    fun enableSubscription(id: String) = viewModelScope.launch {
        val result = repository.enableSubscription(id)
        updateItem(id, result)
    }

    fun disableSubscription(id: String) = viewModelScope.launch {
        val result = repository.disableSubscription(id)
        updateItem(id, result)
    }

    fun deleteSubscription(id: String) = viewModelScope.launch {
        val result = repository.deleteSubscription(id)
        deleteItem(id, result)
    }

    fun enableAllSubscription() = viewModelScope.launch {
        val result = repository.enableAllSubscriptions()
        updateList(result)
    }

    fun disableAllSubscription() = viewModelScope.launch {
        val result = repository.disableAllSubscriptions()
        updateList(result)
    }


    private fun deleteItem(id: String, result: Result<Subscription>) {
        resultWarp(result) { _ ->
            (_list.value as? Result.Success)?.data?.let { taskList ->
                val index = taskList.indexOfFirst { it.id == id }
                taskList.removeAt(index)
            }
        }
        _list.value = _list.value
    }

    private fun updateItem(id: String, result: Result<Subscription>) {
        resultWarp(result) { subscription ->
            (_list.value as? Result.Success)?.data?.let { taskList ->
                val index = taskList.indexOfFirst { it.id == id }
                taskList[index] = subscription
            }
        }
        _list.value = _list.value
    }

    private fun updateList(result: Result<List<Subscription>>) {
        when (result) {
            is Result.Success -> _list.value = Result.Success(result.data.toMutableList())
            is Result.Error -> _list.value = result
        }
    }
}
