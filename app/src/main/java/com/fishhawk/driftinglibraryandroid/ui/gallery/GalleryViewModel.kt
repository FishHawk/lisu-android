package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataDetail
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    savedStateHandle: SavedStateHandle
) : FeedbackViewModel() {
    val outline: MangaOutline = savedStateHandle.get("outline")!!
    val provider: ProviderInfo? = savedStateHandle.get("provider")

    val mangaId = outline.id
    val providerId = provider?.id
    val isFromProvider = provider != null

    private val _detail: MutableLiveData<MangaDetail> = MutableLiveData()
    val detail: LiveData<MangaDetail> = _detail

    private val _isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    val history = readingHistoryRepository
        .select(P.selectedServer.get(), mangaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun refreshManga() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val detail =
                if (providerId == null) remoteLibraryRepository.getManga(mangaId)
                else remoteProviderRepository.getManga(providerId, mangaId)
            _isRefreshing.value = false
            detail.onSuccess {
                _detail.value = it
            }.onFailure {
                feed(it)
            }
        }
    }

    init {
        refreshManga()
    }

    fun updateCover(requestBody: RequestBody) = viewModelScope.launch {
        val result = remoteLibraryRepository.updateMangaCover(mangaId, requestBody)
        resultWarp(result) {
            _detail.value = it
            feed(R.string.toast_manga_cover_updated)
        }
    }

    fun updateMetadata(metadata: MetadataDetail) = viewModelScope.launch {
        val result = remoteLibraryRepository.updateMangaMetadata(mangaId, metadata)
        resultWarp(result) {
            _detail.value = it
            feed(R.string.toast_manga_metadata_updated)
        }
    }

    fun syncSource() {
        if (isFromProvider) return
        viewModelScope.launch {
            val result = remoteLibraryRepository.syncMangaSource(mangaId)
            resultWarp(result) { feed(R.string.successfully_create_sync_task) }
        }
    }

    fun deleteSource() {
        if (isFromProvider) return
        viewModelScope.launch {
            val result = remoteLibraryRepository.deleteMangaSource(mangaId)
            resultWarp(result) { feed(R.string.successfully_delete_source) }
        }
    }

    fun addMangaToLibrary(keepAfterCompleted: Boolean) {
        val providerId = providerId ?: return
        val sourceMangaId = mangaId
        val targetMangaId = detail.value?.title ?: return

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
