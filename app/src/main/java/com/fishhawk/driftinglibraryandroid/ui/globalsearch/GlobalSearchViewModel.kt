package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.ResultX
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    val keywords = MutableLiveData(savedStateHandle.get<String>("keywords"))

    private val providerList = liveData {
        emit(null)
        emit(remoteLibraryRepository.listProvider())
    }

    val searchGroupList: MediatorLiveData<List<SearchGroup>> = MediatorLiveData()

    init {
        searchGroupList.addSource(keywords) { search() }
        searchGroupList.addSource(providerList) { search() }
    }

    private fun search() =
        viewModelScope.launch {
            val keywords = keywords.value ?: return@launch
            val it = providerList.value?.getOrNull() ?: return@launch

            searchGroupList.value = it.map { info ->
                SearchGroup(info, null)
            }
            searchGroupList.value = it.map { info ->
                SearchGroup(info, remoteLibraryRepository.listManga(info.id, keywords, 1))
            }
        }
}