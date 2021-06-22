package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataDetail
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class GalleryViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
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

    fun syncSource() {
        (detail.value as? Result.Success)?.let {
            if (it.data.provider != null) return
            viewModelScope.launch {
                val result = remoteLibraryRepository.syncMangaSource(it.data.id)
                resultWarp(result) { feed(R.string.successfully_create_sync_task) }
            }
        }
    }

    fun deleteSource() {
        (detail.value as? Result.Success)?.let {
            if (it.data.provider != null) return
            viewModelScope.launch {
                val result = remoteLibraryRepository.deleteMangaSource(it.data.id)
                resultWarp(result) { feed(R.string.successfully_delete_source) }
            }
        }
    }

    fun addMangaToLibrary(keepAfterCompleted: Boolean) {
        (detail.value as? Result.Success)?.let {
            val sourceMangaId = it.data.id
            val targetMangaId = it.data.title
            val providerId = it.data.provider?.id ?: return

            viewModelScope.launch {
                val result = remoteLibraryRepository.createManga(
                    targetMangaId,
                    providerId,
                    sourceMangaId,
                    keepAfterCompleted
                )
                resultWarp(result) { feed(R.string.successfully_add_to_library) }
            }
        }
    }
}
