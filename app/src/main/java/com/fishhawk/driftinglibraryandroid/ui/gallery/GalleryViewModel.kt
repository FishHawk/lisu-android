package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.data.ReadingHistory
import com.fishhawk.driftinglibraryandroid.ui.base.DownloadCreatedNotification
import com.fishhawk.driftinglibraryandroid.ui.base.NotificationViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.SubscriptionCreatedNotification
import kotlinx.coroutines.launch


class GalleryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : NotificationViewModel() {
    private val _detail: MutableLiveData<Result<MangaDetail>> = MutableLiveData(Result.Loading)
    val detail: LiveData<Result<MangaDetail>> = _detail

    val history: LiveData<ReadingHistory> = detail.switchMap {
        if (it is Result.Success) readingHistoryRepository.observeReadingHistory(it.data.id)
        else MutableLiveData()
    }

    fun openMangaFromLibrary(id: String) = viewModelScope.launch {
        _detail.value = remoteLibraryRepository.getMangaFromLibrary(id)
    }

    fun openMangaFromSource(source: String, id: String) = viewModelScope.launch {
        _detail.value = remoteLibraryRepository.getMangaFromSource(source, id)
    }

    fun download() {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val title = it.data.title
            val source = it.data.source ?: return
            viewModelScope.launch {
                val result = remoteLibraryRepository.postDownloadTask(source, id, title)
                resultWarp(result) { notify(DownloadCreatedNotification()) }
            }
        }
    }

    fun subscribe() {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val title = it.data.title
            val source = it.data.source ?: return
            viewModelScope.launch {
                val result = remoteLibraryRepository.postSubscription(source, id, title)
                resultWarp(result) { notify(SubscriptionCreatedNotification()) }
            }
        }
    }
}
