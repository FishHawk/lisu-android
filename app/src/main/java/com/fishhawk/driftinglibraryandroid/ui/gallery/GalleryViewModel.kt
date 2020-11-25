package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.local.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MetadataDetail
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class GalleryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val mangaId: String,
    private val providerId: String?
) : FeedbackViewModel() {

    private val _detail: MutableLiveData<Result<MangaDetail>?> = MutableLiveData(null)
    val detail: LiveData<Result<MangaDetail>?> = _detail

    val history: LiveData<ReadingHistory> = detail.switchMap {
        if (it is Result.Success) readingHistoryRepository.observeReadingHistory(
            GlobalPreference.selectedServer.get(),
            it.data.id
        )
        else MutableLiveData()
    }

    private fun loadManga() {
        _detail.value = null
        viewModelScope.launch {
            _detail.value = providerId?.let {
                remoteProviderRepository.getManga(it, mangaId)
            } ?: remoteLibraryRepository.getManga(mangaId)
        }
    }

    init {
        loadManga()
    }

    fun updateThumb(requestBody: RequestBody) = viewModelScope.launch {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val result = remoteLibraryRepository.updateMangaThumb(id, requestBody)
            resultWarp(result) {
                _detail.value = result
                feed(R.string.toast_manga_cover_updated)
            }
        }
    }

    fun updateMetadata(metadata: MetadataDetail) = viewModelScope.launch {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val result = remoteLibraryRepository.updateMangaMetadata(id, metadata)
            resultWarp(result) {
                _detail.value = result
                feed(R.string.toast_manga_metadata_updated)
            }
        }
    }

    fun download() {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val title = it.data.title
            val providerId = it.data.providerId ?: return
            viewModelScope.launch {
                val result = remoteDownloadRepository.postDownloadTask(providerId, id, title)
                resultWarp(result) { feed(R.string.download_task_created) }
            }
        }
    }

    fun subscribe() {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val title = it.data.title
            val providerId = it.data.providerId ?: return
            viewModelScope.launch {
                val result = remoteSubscriptionRepository.postSubscription(providerId, id, title)
                resultWarp(result) { feed(R.string.subscription_created) }
            }
        }
    }
}
