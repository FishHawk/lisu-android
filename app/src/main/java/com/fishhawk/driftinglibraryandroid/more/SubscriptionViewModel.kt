package com.fishhawk.driftinglibraryandroid.more

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.base.BaseListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.DownloadTask
import com.fishhawk.driftinglibraryandroid.repository.data.Subscription
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BaseListViewModel<Subscription>() {
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
        when (result) {
            is Result.Success -> {
                (_list.value as? Result.Success)?.data?.let { taskList ->
                    val index = taskList.indexOfFirst { it.id == id }
                    taskList.removeAt(index)
                }
            }
            is Result.Error -> _operationError.value = Event(result.exception)
        }
        _list.value = _list.value
    }

    private fun updateItem(id: Int, result: Result<Subscription>) {
        when (result) {
            is Result.Success -> {
                (_list.value as? Result.Success)?.data?.let { taskList ->
                    val index = taskList.indexOfFirst { it.id == id }
                    taskList[index] = result.data
                }
            }
            is Result.Error -> _operationError.value = Event(result.exception)
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

@Suppress("UNCHECKED_CAST")
class SubscriptionViewModelFactory(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(SubscriptionViewModel::class.java) ->
                SubscriptionViewModel(remoteLibraryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
