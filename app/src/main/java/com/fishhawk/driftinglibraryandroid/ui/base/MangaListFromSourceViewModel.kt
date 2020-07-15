package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class MangaListFromSourceViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) :
    RefreshableListViewModelWithFetchMore<MangaOutline>() {
    protected var page = 1

    override fun onRefreshSuccess(data: List<MangaOutline>) {
        page = 1
    }

    override fun onFetchMoreSuccess(data: List<MangaOutline>) {
        if (data.isNotEmpty()) page += 1
    }


    fun download(id: String, title: String) =
        viewModelScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.postDownloadTask(source, id, title)
            networkOperationWarp(result) { notify(DownloadCreatedNotification()) }
        }

    fun subscribe(id: String, title: String) =
        viewModelScope.launch(Dispatchers.Main) {
            val result = remoteLibraryRepository.postSubscription(source, id, title)
            networkOperationWarp(result) { notify(SubscriptionCreatedNotification()) }
        }
}