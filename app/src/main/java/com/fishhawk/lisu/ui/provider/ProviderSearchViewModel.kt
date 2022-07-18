package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.database.model.SearchHistory
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


sealed interface ProviderSearchEvent : Event {
    data class RemoveFromLibraryFailure(val exception: Throwable) : ProviderSearchEvent
    data class AddToLibraryFailure(val exception: Throwable) : ProviderSearchEvent
    data class RefreshFailure(val exception: Throwable) : ProviderSearchEvent
}

class ProviderSearchViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : BaseViewModel<ProviderSearchEvent>() {

    val providerId = args.getString("providerId")!!

    private val _keywords = MutableStateFlow(args.getString("keywords") ?: "")
    val keywords = _keywords.asStateFlow()

    val suggestions = searchHistoryRepository.listByProvider(providerId)
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _mangas =
        keywords
            .filter { it.isNotBlank() }
            .onEach { searchHistoryRepository.update(SearchHistory(providerId, it)) }
            .flatMapLatest { lisuRepository.search(providerId = providerId, keywords = it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val mangas =
        _mangas
            .filterNotNull()
            .map { it.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun search(keywords: String) {
        _keywords.value = keywords
    }

    fun deleteSuggestion(keywords: String) = viewModelScope.launch {
        searchHistoryRepository.deleteByKeywords(providerId, keywords)
    }

    fun addToLibrary(manga: MangaDto) = viewModelScope.launch {
        lisuRepository.addMangaToLibrary(manga.providerId, manga.id)
            .onFailure { sendEvent(ProviderSearchEvent.AddToLibraryFailure(it)) }
    }

    fun removeFromLibrary(manga: MangaDto) = viewModelScope.launch {
        lisuRepository.removeMangaFromLibrary(manga.providerId, manga.id)
            .onFailure { sendEvent(ProviderSearchEvent.RemoveFromLibraryFailure(it)) }
    }

    fun reload() {
        viewModelScope.launch {
            _mangas.value?.reload()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            _mangas.value?.refresh()
                ?.onFailure { sendEvent(ProviderSearchEvent.RefreshFailure(it)) }
            _isRefreshing.value = false
        }
    }

    fun requestNextPage() {
        viewModelScope.launch {
            _mangas.value?.requestNextPage()
        }
    }
}
