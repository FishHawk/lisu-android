package com.fishhawk.driftinglibraryandroid.ui.gallery.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.repository.local.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.local.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MetadataDetail
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import com.fishhawk.driftinglibraryandroid.ui.base.DownloadCreatedNotification
import com.fishhawk.driftinglibraryandroid.ui.base.NotificationViewModel
import com.fishhawk.driftinglibraryandroid.ui.base.SubscriptionCreatedNotification
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class GalleryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val remoteDownloadRepository: RemoteDownloadRepository,
    private val remoteSubscriptionRepository: RemoteSubscriptionRepository,
    private val readingHistoryRepository: ReadingHistoryRepository
) : NotificationViewModel() {
    private val _detail: MutableLiveData<Result<MangaDetail>> = MutableLiveData(Result.Loading)
    val detail: LiveData<Result<MangaDetail>> = _detail

    val history: LiveData<ReadingHistory> = detail.switchMap {
        if (it is Result.Success) readingHistoryRepository.observeReadingHistory(
            SettingsHelper.selectedServer.getValueDirectly(),
            it.data.id
        )
        else MutableLiveData()
    }

    fun openMangaFromLibrary(id: String) = viewModelScope.launch {
        _detail.value = remoteLibraryRepository.getManga(id)
    }

    fun openMangaFromProvider(providerId: String, id: String) = viewModelScope.launch {
        _detail.value = remoteProviderRepository.getManga(providerId, id)
    }

    fun updateThumb(requestBody: RequestBody) = viewModelScope.launch {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val result = remoteLibraryRepository.updateMangaThumb(id, requestBody)
            println(result)
            resultWarp(result) { _detail.value = result }
        }
    }

    fun updateMetadata(metadata: MetadataDetail) = viewModelScope.launch {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val result = remoteLibraryRepository.updateMangaMetadata(id, metadata)
            resultWarp(result) { _detail.value = result }
        }
    }

    fun download() {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val title = it.data.metadata.title ?: id
            val providerId = it.data.providerId ?: return
            viewModelScope.launch {
                val result = remoteDownloadRepository.postDownloadTask(providerId, id, title)
                resultWarp(result) { notify(DownloadCreatedNotification()) }
            }
        }
    }

    fun subscribe() {
        (detail.value as? Result.Success)?.let {
            val id = it.data.id
            val title = it.data.metadata.title ?: id
            val providerId = it.data.providerId ?: return
            viewModelScope.launch {
                val result = remoteSubscriptionRepository.postSubscription(providerId, id, title)
                resultWarp(result) { notify(SubscriptionCreatedNotification()) }
            }
        }
    }
}
