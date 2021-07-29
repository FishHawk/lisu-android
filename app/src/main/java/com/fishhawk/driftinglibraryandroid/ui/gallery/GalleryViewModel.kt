package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.ResultX
import com.fishhawk.driftinglibraryandroid.data.remote.model.*
import com.fishhawk.driftinglibraryandroid.ui.base.FeedbackViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    readingHistoryRepository: ReadingHistoryRepository,
    savedStateHandle: SavedStateHandle
) : FeedbackViewModel() {

    private val _detail = MutableStateFlow<ResultX<MangaDetail>?>(null)
    val detail = _detail
        .map { it?.getOrNull() }
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            MangaDetail(savedStateHandle.get("outline")!!)
                .copy(provider = savedStateHandle.get("provider"))
        )

    private val mangaId
        get() = detail.value.id

    private val providerId
        get() = detail.value.provider?.id

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val history = PR.selectedServer.flow.flatMapLatest {
        readingHistoryRepository.select(it, mangaId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun refreshManga() = viewModelScope.launch {
        _isRefreshing.value = true
        _detail.value = providerId?.let {
            remoteProviderRepository.getManga(it, mangaId)
        } ?: remoteLibraryRepository.getManga(mangaId)
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
        if (detail.value.provider != null) return@launch
        val result = remoteLibraryRepository.syncMangaSource(mangaId)
        if (result.isSuccess) {
            feed(R.string.successfully_create_sync_task)
        } else result.exceptionOrNull()?.let { feed(it) }
    }

    fun deleteSource() = viewModelScope.launch {
        if (detail.value.provider != null) return@launch
        val result = remoteLibraryRepository.deleteMangaSource(mangaId)
        resultWarp(result) { }
        if (result.isSuccess) {
            feed(R.string.successfully_delete_source)
        } else result.exceptionOrNull()?.let { feed(it) }
    }

    fun addMangaToLibrary(keepAfterCompleted: Boolean) {
        val providerId = providerId ?: return
        val sourceMangaId = mangaId
        val targetMangaId = detail.value.title

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
