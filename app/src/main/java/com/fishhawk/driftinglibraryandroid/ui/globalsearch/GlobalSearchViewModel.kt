package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import androidx.lifecycle.*
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchGroup(
    val provider: ProviderInfo,
    var result: Result<List<MangaOutline>>?
)

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val remoteLibraryRepository: RemoteProviderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val keywords = MutableLiveData(savedStateHandle.get<String>("keywords"))

    private val providerList: LiveData<Result<List<ProviderInfo>>?> = liveData {
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
            providerList.value?.onSuccess {
                searchGroupList.value = it.map { info ->
                    SearchGroup(info, null)
                }
                searchGroupList.value = it.map { info ->
                    val result = remoteLibraryRepository.listManga(info.id, keywords, 1)
                    // Hack, see https://youtrack.jetbrains.com/issue/KT-46477#focus=Comments-27-4952485.0-0
                    SearchGroup(info, result.fold({ Result.success(it) }, { Result.failure(it) }))
                }
            }
        }
}