package com.fishhawk.lisu.ui.provider

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.database.model.SearchHistory
import com.fishhawk.lisu.data.remote.LisuRepository
import com.fishhawk.lisu.data.remote.model.MangaDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProviderSearchViewModel(
    args: Bundle,
    private val lisuRepository: LisuRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {

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

    fun search(keywords: String) {
        _keywords.value = keywords
    }

    fun deleteSuggestion(keywords: String) = viewModelScope.launch {
        searchHistoryRepository.deleteByKeywords(providerId, keywords)
    }

    fun addToLibrary(manga: MangaDto) = viewModelScope.launch {
        lisuRepository.addMangaToLibrary(manga.providerId, manga.id).fold({}, {})
    }

    fun removeFromLibrary(manga: MangaDto) = viewModelScope.launch {
        lisuRepository.removeMangaFromLibrary(manga.providerId, manga.id).fold({}, {})
    }

    fun reload() {
        viewModelScope.launch {
            _mangas.value?.reload()
        }
    }

    fun requestNextPage() {
        viewModelScope.launch {
            _mangas.value?.requestNextPage()
        }
    }
}
