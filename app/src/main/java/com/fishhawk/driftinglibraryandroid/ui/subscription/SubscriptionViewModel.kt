package com.fishhawk.driftinglibraryandroid.ui.subscription

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListViewModel
import kotlinx.coroutines.launch


class SubscriptionViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : RefreshableListViewModel<Subscription>() {
    override suspend fun loadResult() = remoteLibraryRepository.getAllSubscriptions()

    fun enableSubscription(id: Int) = viewModelScope.launch {
        val result = remoteLibraryRepository.enableSubscription(id)
        updateItem(id, result)
    }

    fun disableSubscription(id: Int) = viewModelScope.launch {
        val result = remoteLibraryRepository.disableSubscription(id)
        updateItem(id, result)
    }

    fun deleteSubscription(id: Int) = viewModelScope.launch {
        val result = remoteLibraryRepository.deleteSubscription(id)
        deleteItem(id, result)
    }

    fun enableAllSubscription() = viewModelScope.launch {
        val result = remoteLibraryRepository.enableAllSubscriptions()
        updateList(result)
    }

    fun disableAllSubscription() = viewModelScope.launch {
        val result = remoteLibraryRepository.disableAllSubscriptions()
        updateList(result)
    }


    private fun deleteItem(id: Int, result: Result<Subscription>) {
        resultWarp(result) { _ ->
            (_list.value as? Result.Success)?.data?.let { taskList ->
                val index = taskList.indexOfFirst { it.id == id }
                taskList.removeAt(index)
            }
        }
        _list.value = _list.value
    }

    private fun updateItem(id: Int, result: Result<Subscription>) {
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
