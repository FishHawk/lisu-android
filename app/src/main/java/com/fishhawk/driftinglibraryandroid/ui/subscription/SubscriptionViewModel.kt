package com.fishhawk.driftinglibraryandroid.ui.subscription

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.Subscription
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.remoteList
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val repository: RemoteSubscriptionRepository
) : FeedbackViewModel() {

    val subscriptions = remoteList { repository.getAllSubscriptions() }

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
            subscriptions.data.value = subscriptions.data.value?.let { list ->
                val newList = list.toMutableList()
                val index = newList.indexOfFirst { it.id == id }
                newList.removeAt(index)
                newList
            }
        }
    }

    private fun updateItem(id: String, result: Result<Subscription>) {
        resultWarp(result) { subscription ->
            subscriptions.data.value = subscriptions.data.value?.let { list ->
                val newList = list.toMutableList()
                val index = newList.indexOfFirst { it.id == id }
                newList[index] = subscription
                newList
            }
        }
    }

    private fun updateList(result: Result<List<Subscription>>) {
        if (result is Result.Success) subscriptions.data.value = result.data
        subscriptions.state.value = when (result) {
            is Result.Success ->
                if (result.data.isEmpty()) ViewState.Empty
                else ViewState.Content
            is Result.Error -> ViewState.Error(result.exception)
        }
    }
}
