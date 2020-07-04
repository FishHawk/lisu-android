package com.fishhawk.driftinglibraryandroid.more

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.base.BaseListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.DownloadTask
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.launch


class DownloadViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BaseListViewModel<DownloadTask>() {
    override suspend fun loadResult(): Result<List<DownloadTask>> {
        return remoteLibraryRepository.getAllDownloadTasks()
    }

    fun startDownloadTask(id: Int) = viewModelScope.launch {
        val result = remoteLibraryRepository.startDownloadTask(id)
        updateItem(id, result)
    }

    fun pauseDownloadTask(id: Int) = viewModelScope.launch {
        val result = remoteLibraryRepository.pauseDownloadTask(id)
        updateItem(id, result)
    }

    fun deleteDownloadTask(id: Int) = viewModelScope.launch {
        val result = remoteLibraryRepository.deleteDownloadTask(id)
        deleteItem(id, result)
    }

    fun startAllDownloadTasks() = viewModelScope.launch {
        val result = remoteLibraryRepository.startAllDownloadTasks()
        updateList(result)
    }

    fun pauseAllDownloadTasks() = viewModelScope.launch {
        val result = remoteLibraryRepository.pauseAllDownloadTasks()
        updateList(result)
    }


    private fun deleteItem(id: Int, result: Result<DownloadTask>) {
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

    private fun updateItem(id: Int, result: Result<DownloadTask>) {
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

    private fun updateList(result: Result<List<DownloadTask>>) {
        when (result) {
            is Result.Success -> _list.value = Result.Success(result.data.toMutableList())
            is Result.Error -> _list.value = result
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DownloadViewModelFactory(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(DownloadViewModel::class.java) ->
                DownloadViewModel(remoteLibraryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
