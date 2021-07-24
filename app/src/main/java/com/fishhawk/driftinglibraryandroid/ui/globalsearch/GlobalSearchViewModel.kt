package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.ResultX
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchGroup(
    val provider: ProviderInfo,
    var result: ResultX<List<MangaOutline>>?
)

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteProviderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val keywords = MutableStateFlow(savedStateHandle.get<String>("keywords") ?: "")

    private val providerList =
        flow { emit(remoteLibraryRepository.listProvider().getOrNull()) }.filterNotNull()

    val searchGroupList = combine(keywords, providerList) { keywords, providerList ->
        providerList.map { provider ->
            flow {
                val result = remoteLibraryRepository.listManga(provider.id, keywords, 1)
                emit(SearchGroup(provider, result))
            }.stateIn(viewModelScope, SharingStarted.Lazily, SearchGroup(provider, null))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())
}