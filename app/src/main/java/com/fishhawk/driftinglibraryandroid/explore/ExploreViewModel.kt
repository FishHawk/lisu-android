package com.fishhawk.driftinglibraryandroid.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModel() {
    private var address = remoteLibraryRepository.url
    var keyword: String = ""

    private val _mangaList: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val mangaList: LiveData<Result<List<MangaSummary>>> = _mangaList

    fun reload(keyword: String) {
        this.keyword = keyword
        _mangaList.value = Result.Loading
        GlobalScope.launch(Dispatchers.Main) {
            _mangaList.value =
                remoteLibraryRepository.search("漫画人", keyword, 1)
        }
    }

    fun reloadIfNeed(keyword: String) {
        if (address != remoteLibraryRepository.url || mangaList.value !is Result.Success)
            reload(keyword)
        address = remoteLibraryRepository.url
    }
}

@Suppress("UNCHECKED_CAST")
class ExploreViewModelFactory(
    private val remoteLibraryRepository: RemoteLibraryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) = with(modelClass) {
        when {
            isAssignableFrom(ExploreViewModel::class.java) ->
                ExploreViewModel(remoteLibraryRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}