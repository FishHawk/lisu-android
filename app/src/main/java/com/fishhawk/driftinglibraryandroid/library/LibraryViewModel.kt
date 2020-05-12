package com.fishhawk.driftinglibraryandroid.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import com.fishhawk.driftinglibraryandroid.setting.PreferenceStringLiveData
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LibraryViewModel : ViewModel() {
    var filter: String = ""

    private val _mangaList: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val mangaList: LiveData<Result<List<MangaSummary>>> = _mangaList

    fun reload() {
        _mangaList.value = Result.Loading
        GlobalScope.launch(Dispatchers.Main) {
            _mangaList.value = Repository.getMangaList("", filter)
        }
    }

    // library address
    val libraryAddress: PreferenceStringLiveData = SettingsHelper.libraryAddress

    // library content
    private val _refreshResult: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val refreshResult: LiveData<Result<List<MangaSummary>>> = _refreshResult

    fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            val result = Repository.getMangaList("", filter)
            if (result is Result.Success) _mangaList.value = result
            _refreshResult.value = result
        }
    }

    private val _fetchMoreResult: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val fetchMoreResult: LiveData<Result<List<MangaSummary>>> = _fetchMoreResult

    fun fetchMore() {
        val lastId = (_mangaList.value as Result.Success).data.let {
            if (it.isEmpty()) "" else it.last().id
        }

        GlobalScope.launch(Dispatchers.Main) {
            val result = Repository.getMangaList(lastId, filter)
            if (result is Result.Success)
                _mangaList.value = (_mangaList.value as Result.Success).also { old ->
                    old.data.plus(result.data)
                }
            _fetchMoreResult.value = result
        }
    }
}
