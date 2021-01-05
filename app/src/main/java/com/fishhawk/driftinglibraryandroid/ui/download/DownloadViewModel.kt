package com.fishhawk.driftinglibraryandroid.ui.download

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.DownloadDesc
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.remoteList
import com.fishhawk.driftinglibraryandroid.widget.ViewState
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val repository: RemoteDownloadRepository
) : FeedbackViewModel() {

    val downloads = remoteList { repository.getAllDownloadTasks() }

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
            downloads.data.value = downloads.data.value?.toMutableList()?.apply {
                val index = indexOfFirst { it.id == id }
                removeAt(index)
            }
        }
    }

    private fun updateItem(id: String, result: Result<DownloadDesc>) {
        resultWarp(result) { task ->
            downloads.data.value = downloads.data.value?.toMutableList()?.apply {
                val index = indexOfFirst { it.id == id }
                this[index] = task
            }
        }
    }

    private fun updateList(result: Result<List<DownloadDesc>>) {
        if (result is Result.Success) downloads.data.value = result.data
        downloads.state.value = when (result) {
            is Result.Success ->
                if (result.data.isEmpty()) ViewState.Empty
                else ViewState.Content
            is Result.Error -> ViewState.Error(result.exception)
        }
    }
}
