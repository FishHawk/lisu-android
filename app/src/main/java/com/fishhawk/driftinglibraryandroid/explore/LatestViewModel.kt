package com.fishhawk.driftinglibraryandroid.explore

import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.base.BasePartListViewModel
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaOutline
import com.fishhawk.driftinglibraryandroid.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LatestViewModel(
    private val source: String,
    private val remoteLibraryRepository: RemoteLibraryRepository
) : BasePartListViewModel<MangaOutline>() {
    private var page = 1

    override suspend fun loadResult() = remoteLibraryRepository.getLatestMangaList(source, 1)
    override suspend fun fetchMoreResult() =
        remoteLibraryRepository.getLatestMangaList(source, page + 1)

    override fun onRefreshSuccess() {
        page = 1
    }

    override fun onFetchMoreSuccess(size: Int) {
        if (size > 0) page += 1
    }

    fun download(id: String, title: String) {
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = remoteLibraryRepository.postDownloadTask(source!!, id, title)) {
//                    is Result.Success -> _operationError.value = Event(null)
//                    is Result.Error -> _operationError.value = Event(result.exception)
            }
        }
    }

    fun subscribe(id: String, title: String) {
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = remoteLibraryRepository.postSubscription(source, id, title)) {
//                is Result.Success -> _operationError.value = Event(null)
//                is Result.Error -> _operationError.value = Event(result.exception)
            }
        }
    }
}