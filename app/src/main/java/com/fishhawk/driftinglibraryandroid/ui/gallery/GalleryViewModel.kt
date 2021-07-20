package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataDetail
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.data.remote.ResultX
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

    private val _detail = MutableStateFlow<ResultX<MangaDetail>?>(null)
    val detail = _detail
        .map { it?.getOrNull() }
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val history = readingHistoryRepository
        .select(P.selectedServer.get(), mangaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        refreshManga()
    }

    fun refreshManga() = viewModelScope.launch {
        _isRefreshing.value = true
        _detail.value =
            if (providerId == null) remoteLibraryRepository.getManga(mangaId)
            else remoteProviderRepository.getManga(providerId, mangaId)
        _isRefreshing.value = false
    }

    init {
        refreshManga()
    }

    fun updateCover(requestBody: RequestBody) = viewModelScope.launch {
        val result = remoteLibraryRepository.updateMangaCover(mangaId, requestBody)
        if (result.isSuccess) _detail.value = result
        resultWarp(result) {
            feed(R.string.toast_manga_cover_updated)
        }
    }

    fun updateMetadata(metadata: MetadataDetail) = viewModelScope.launch {
        val result = remoteLibraryRepository.updateMangaMetadata(mangaId, metadata)
        if (result.isSuccess) {
            _detail.value = result
            feed(R.string.toast_manga_metadata_updated)
        } else result.exceptionOrNull()?.let { feed(it) }
    }

    fun syncSource() = viewModelScope.launch {
        if (isFromProvider) return@launch
        val result = remoteLibraryRepository.syncMangaSource(mangaId)
        if (result.isSuccess) {
            feed(R.string.successfully_create_sync_task)
        } else result.exceptionOrNull()?.let { feed(it) }
    }

    fun deleteSource() = viewModelScope.launch {
        if (isFromProvider) return@launch
        val result = remoteLibraryRepository.deleteMangaSource(mangaId)
        resultWarp(result) { }
        if (result.isSuccess) {
            feed(R.string.successfully_delete_source)
        } else result.exceptionOrNull()?.let { feed(it) }
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
