package com.fishhawk.lisu.ui.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    readingHistoryRepository: ReadingHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    private val manga = savedStateHandle.get<MangaDto>("manga")!!
    private val _detail = MutableStateFlow(manga.let {
        MangaDetailDto(
            providerId = it.providerId,
            id = it.id,
            cover = it.cover,
            updateTime = it.updateTime,
            title = it.title,
            authors = it.authors,
            isFinished = it.isFinished,
        )
    })
    val detail = _detail.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val history = readingHistoryRepository.select(manga.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun reloadManga() = viewModelScope.launch {
        _viewState.value = ViewState.Loading
        remoteProviderRepository
            .getManga(manga.providerId, manga.id)
            .onSuccess {
                _viewState.value = ViewState.Loaded
                _detail.value = it
            }.onFailure {
                _viewState.value = ViewState.Failure(it)
            }
    }

    init {
        reloadManga()
    }

    fun addToLibrary() = viewModelScope.launch {
        remoteLibraryRepository.createManga(manga.providerId, manga.id).fold(
            { _detail.value = _detail.value.copy(inLibrary = true) },
            {}
        )
    }

    fun removeFromLibrary() = viewModelScope.launch {
        remoteLibraryRepository.deleteManga(manga.providerId, manga.id).fold(
            { _detail.value = _detail.value.copy(inLibrary = false) },
            {}
        )
    }

//    fun updateCover(requestBody: RequestBody) = viewModelScope.launch {
//        val result = remoteProviderRepository.updateMangaCover(
//            manga.providerId, manga.id, requestBody
//        )
//        result.onSuccess { _detail.value = it }
//        resultWarp(result) {
//            feed(R.string.toast_manga_cover_updated)
//        }
//    }
//
//    fun updateMetadata(metadata: MetadataDetail) = viewModelScope.launch {
//        val result = remoteProviderRepository.updateMangaMetadata(
//            manga.providerId, manga.id, metadata
//        )
//        result.onSuccess {
//            _detail.value = it
//            feed(R.string.toast_manga_metadata_updated)
//        }.onFailure {
//            result.exceptionOrNull()?.let { feed(it) }
//        }
//    }
}
