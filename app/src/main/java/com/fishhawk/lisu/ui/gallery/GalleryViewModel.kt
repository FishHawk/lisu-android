package com.fishhawk.lisu.ui.gallery

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.fishhawk.lisu.data.database.ReadingHistoryRepository
import com.fishhawk.lisu.data.remote.RemoteLibraryRepository
import com.fishhawk.lisu.data.remote.RemoteProviderRepository
import com.fishhawk.lisu.data.remote.model.*
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import com.fishhawk.lisu.ui.widget.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.RequestBody

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

    private var source: CommentSource? = null

    val commentList = Pager(PagingConfig(pageSize = 20)) {
        CommentSource().also { source = it }
    }.flow.cachedIn(viewModelScope)

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
        remoteLibraryRepository.createManga(
            manga.providerId, manga.id
        ).onSuccess {
            _detail.value = _detail.value.copy(state = MangaState.RemoteInLibrary)
        }.onFailure {
            sendEvent(GalleryEffect.AddToLibraryFailure(it))
        }
    }

    fun removeFromLibrary() = viewModelScope.launch {
        remoteLibraryRepository.deleteManga(
            manga.providerId, manga.id
        ).onSuccess {
            _detail.value = _detail.value.copy(state = MangaState.Remote)
        }.onFailure {
            sendEvent(GalleryEffect.RemoveFromLibraryFailure(it))
        }
    }

    fun updateCover(requestBody: RequestBody) = viewModelScope.launch {
        remoteLibraryRepository.updateMangaCover(
            manga.providerId, manga.id, requestBody
        ).onSuccess {
            sendEvent(GalleryEffect.UpdateCoverSuccess)
        }.onFailure {
            sendEvent(GalleryEffect.UpdateCoverFailure(it))
        }
    }

    fun updateMetadata(metadata: MangaMetadataDto) = viewModelScope.launch {
        remoteLibraryRepository.updateMangaMetadata(
            manga.providerId, manga.id, metadata
        ).onSuccess {
            sendEvent(GalleryEffect.UpdateMetadataSuccess)
        }.onFailure {
            sendEvent(GalleryEffect.UpdateMetadataFailure(it))
        }
    }

    inner class CommentSource : PagingSource<Int, CommentDto>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CommentDto> {
            val page = params.key ?: 0
            return remoteProviderRepository.getComment(manga.providerId, manga.id, page).fold(
                { LoadResult.Page(it, null, if (it.isEmpty()) null else page + 1) },
                { LoadResult.Error(it) }
            )
        }

        override fun getRefreshKey(state: PagingState<Int, CommentDto>): Int = 0
    }
}
