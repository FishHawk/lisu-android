package com.fishhawk.lisu.ui.library

import androidx.lifecycle.viewModelScope
import com.fishhawk.lisu.data.database.SearchHistoryRepository
import com.fishhawk.lisu.data.network.LisuRepository
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.MangaKeyDto
import com.fishhawk.lisu.ui.base.BaseViewModel
import com.fishhawk.lisu.ui.base.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface LibraryEvent : Event {
    data class GetRandomSuccess(val manga: MangaDto) : LibraryEvent
    data class GetRandomFailure(val exception: Throwable) : LibraryEvent
    data class DeleteMultipleFailure(val exception: Throwable) : LibraryEvent
}

class LibraryViewModel(
    private val lisuRepository: LisuRepository,
    searchHistoryRepo: SearchHistoryRepository,
) : BaseViewModel<LibraryEvent>() {

    private val _keywords = MutableStateFlow("")
    val keywords = _keywords.asStateFlow()

    val suggestions = searchHistoryRepo.list()
        .map { list -> list.map { it.keywords }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _mangas =
        keywords
            .flatMapLatest { lisuRepository.searchFromLibrary(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val mangas =
        _mangas
            .filterNotNull()
            .map { it.value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun getRandomManga() {
        viewModelScope.launch {
            lisuRepository.getRandomMangaFromLibrary()
                .onSuccess { sendEvent(LibraryEvent.GetRandomSuccess(it)) }
                .onFailure { sendEvent(LibraryEvent.GetRandomFailure(it)) }
        }
    }

    fun deleteMultipleManga(mangas: List<MangaKeyDto>) {
        viewModelScope.launch {
            lisuRepository.removeMultipleMangasFromLibrary(mangas)
                .onSuccess { _mangas.value?.reload() }
                .onFailure { sendEvent(LibraryEvent.DeleteMultipleFailure(it)) }
        }
    }

    fun search(keywords: String) {
        _keywords.value = keywords
    }
}
