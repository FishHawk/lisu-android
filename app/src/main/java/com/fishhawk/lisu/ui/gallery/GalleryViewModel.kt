package com.fishhawk.lisu.ui.gallery

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaMetadataDto
import com.fishhawk.lisu.data.remote.model.MangaState
import com.fishhawk.lisu.data.remote.model.toDetail
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Effect
import com.fishhawk.lisu.ui.widget.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GalleryEffect : Effect {
    object MetadataUpdateSuccessfully : GalleryEffect
    data class Failure(val exception: Throwable) : GalleryEffect
}

class GalleryViewModel(
    args: Bundle,
    private val remoteLibraryRepository: RemoteLibraryRepository,
    private val remoteProviderRepository: RemoteProviderRepository,
    readingHistoryRepository: ReadingHistoryRepository,
) : BaseViewModel<GalleryEffect>() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    private val manga = args.getParcelable<MangaDto>("manga")!!
    private val _detail = MutableStateFlow(manga.toDetail())
    val id = manga.id
    val detail = _detail.asStateFlow()

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
            { _detail.value = _detail.value.copy(state = MangaState.RemoteInLibrary) },
            { sendEffect(GalleryEffect.Failure(it)) }
        )
    }

    fun removeFromLibrary() = viewModelScope.launch {
        remoteLibraryRepository.deleteManga(manga.providerId, manga.id).fold(
            { _detail.value = _detail.value.copy(state = MangaState.Remote) },
            { sendEffect(GalleryEffect.Failure(it)) }
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

    fun updateMetadata(metadata: MangaMetadataDto) = viewModelScope.launch {
        remoteLibraryRepository.updateMangaMetadata(
            manga.providerId, manga.id, metadata
        ).onSuccess {
            sendEffect(GalleryEffect.MetadataUpdateSuccessfully)
        }.onFailure {
            sendEffect(GalleryEffect.Failure(it))
        }
    }
}
