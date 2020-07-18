package com.fishhawk.driftinglibraryandroid.ui.download

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.DownloadTask
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableListViewModel
import kotlinx.coroutines.launch


class DownloadViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : RefreshableListViewModel<DownloadTask>() {
    override suspend fun loadResult() = remoteLibraryRepository.getAllDownloadTasks()

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
        resultWarp(result) { _ ->
            (_list.value as? Result.Success)?.data?.let { taskList ->
                val index = taskList.indexOfFirst { it.id == id }
                taskList.removeAt(index)
            }
        }
        _list.value = _list.value
    }

    private fun updateItem(id: Int, result: Result<DownloadTask>) {
        resultWarp(result) { task ->
            (_list.value as? Result.Success)?.data?.let { taskList ->
                val index = taskList.indexOfFirst { it.id == id }
                taskList[index] = task
            }
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
