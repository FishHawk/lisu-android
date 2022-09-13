package com.fishhawk.lisu.ui.gallery

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.MangaMetadataDto
import com.fishhawk.lisu.data.network.model.MangaState
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface GalleryEffect : Event {
    data class AddToLibraryFailure(val exception: Throwable) : GalleryEffect
    data class RemoveFromLibraryFailure(val exception: Throwable) : GalleryEffect

    object UpdateMetadataSuccess : GalleryEffect
    data class UpdateMetadataFailure(val exception: Throwable) : GalleryEffect

    object UpdateCoverSuccess : GalleryEffect
    data class UpdateCoverFailure(val exception: Throwable) : GalleryEffect
}

class GalleryViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    readingHistoryRepository: ReadingHistoryRepository,
) : BaseViewModel<GalleryEffect>() {
    val manga = args.getParcelable<MangaDto>("manga")!!

    val providerId = manga.providerId
    val mangaId = manga.id

    val searchBoardId = lisuRepository
        .providers.value?.value?.getOrNull()
        ?.find { it.id == providerId }
        ?.searchBoardId

    private val _detail = lisuRepository.getManga(manga.providerId, manga.id)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val detail = _detail
        .filterNotNull()
        .map { it.value }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val history = readingHistoryRepository.select(manga.id)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _comments = lisuRepository
        .getComment(manga.providerId, manga.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val comments = _comments
        .map { it?.value }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun reloadManga() {
        viewModelScope.launch {
            _detail.value?.reload()
        }
    }

    fun reloadComments() {
        viewModelScope.launch {
            _comments.value?.reload()
        }
    }

    fun requestCommentsNextPage() {
        viewModelScope.launch {
            _comments.value?.requestNextPage()
        }
    }

    fun addToLibrary() {
        if (manga.state != MangaState.Remote) return
        viewModelScope.launch {
            lisuRepository.addMangaToLibrary(
                manga.providerId, manga.id
            ).onFailure {
                sendEvent(GalleryEffect.AddToLibraryFailure(it))
            }
        }
    }

    fun removeFromLibrary() {
        if (manga.state != MangaState.RemoteInLibrary) return
        viewModelScope.launch {
            lisuRepository.removeMangaFromLibrary(
                manga.providerId, manga.id
            ).onFailure {
                sendEvent(GalleryEffect.RemoveFromLibraryFailure(it))
            }
        }
    }

    fun updateCover(cover: ByteArray, coverType: String) {
        viewModelScope.launch {
            lisuRepository.updateMangaCover(
                manga.providerId, manga.id, cover, coverType
            ).onSuccess {
                sendEvent(GalleryEffect.UpdateCoverSuccess)
            }.onFailure {
                sendEvent(GalleryEffect.UpdateCoverFailure(it))
            }
        }
    }

    fun updateMetadata(metadata: MangaMetadataDto) {
        if (manga.state != MangaState.Local) return
        viewModelScope.launch {
            lisuRepository.updateMangaMetadata(
                manga.providerId, manga.id, metadata
            ).onSuccess {
                sendEvent(GalleryEffect.UpdateMetadataSuccess)
            }.onFailure {
                sendEvent(GalleryEffect.UpdateMetadataFailure(it))
            }
        }
    }
}
