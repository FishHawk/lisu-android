package com.fishhawk.driftinglibraryandroid.ui.main.download

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadDesc
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListViewModel
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val repository: RemoteDownloadRepository
) : RefreshableListViewModel<DownloadDesc>() {
    override suspend fun loadResult() = repository.getAllDownloadTasks()

    fun startDownloadTask(id: String) = viewModelScope.launch {
        val result = repository.startDownloadTask(id)
        updateItem(id, result)
    }

    fun pauseDownloadTask(id: String) = viewModelScope.launch {
        val result = repository.pauseDownloadTask(id)
        updateItem(id, result)
    }

    fun deleteDownloadTask(id: String) = viewModelScope.launch {
        val result = repository.deleteDownloadTask(id)
        deleteItem(id, result)
    }

    fun startAllDownloadTasks() = viewModelScope.launch {
        val result = repository.startAllDownloadTasks()
        updateList(result)
    }

    fun pauseAllDownloadTasks() = viewModelScope.launch {
        val result = repository.pauseAllDownloadTasks()
        updateList(result)
    }


    private fun deleteItem(id: String, result: Result<DownloadDesc>) {
        resultWarp(result) { _ ->
            (_list.value as? Result.Success)?.data?.let { taskList ->
                val index = taskList.indexOfFirst { it.id == id }
                taskList.removeAt(index)
            }
        }
        _list.value = _list.value
    }

    private fun updateItem(id: String, result: Result<DownloadDesc>) {
        resultWarp(result) { task ->
            (_list.value as? Result.Success)?.data?.let { taskList ->
                val index = taskList.indexOfFirst { it.id == id }
                taskList[index] = task
            }
        }
        _list.value = _list.value
    }

    private fun updateList(result: Result<List<DownloadDesc>>) {
        when (result) {
            is Result.Success -> _list.value = Result.Success(result.data.toMutableList())
            is Result.Error -> _list.value = result
        }
    }
}
