package com.fishhawk.driftinglibraryandroid.library

import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fishhawk.driftinglibraryandroid.repository.Repository
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.data.MangaSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LibraryViewModel : ViewModel() {
    fun setLibraryAddress(inputAddress: String) {
        var newAddress = inputAddress
        newAddress = if (URLUtil.isNetworkUrl(newAddress)) newAddress else "http://${inputAddress}"
        newAddress = if (newAddress.last() == '/') newAddress else "$newAddress/"
        Repository.setUrl(newAddress)
        reload()
    }

    private val _mangaList: MutableLiveData<Result<List<MangaSummary>>> = MutableLiveData()
    val mangaList: LiveData<Result<List<MangaSummary>>> = _mangaList

    fun reload(filter: String = "") {
        _mangaList.value = Result.Loading
        GlobalScope.launch(Dispatchers.Main) {
            when (val result = Repository.getMangaList("", filter)) {
                is Result.Success -> _mangaList.value = result
                is Result.Error -> println(result.exception.message)
            }
        }
    }

    suspend fun refresh(filter: String = ""): String? {
        return Repository.getMangaList("", filter).let {
            when (it) {
                is Result.Success -> {
                    _mangaList.value = it
                    null
                }
                is Result.Error -> it.exception.message
                else -> null
            }
        }
    }

    suspend fun fetchMore(filter: String = ""): String? {
        val lastId =
            (_mangaList.value as Result.Success).data.let { if (it.isEmpty()) "" else it.last().id }

        return Repository.getMangaList(lastId, filter).let {
            when (it) {
                is Result.Success -> {
                    if (it.data.isNotEmpty()) {
                        _mangaList.value = (_mangaList.value as Result.Success).also { old ->
                            old.data.plus(it.data)
                        }
                        null
                    } else {
                        "没有更多了"
                    }
                }
                is Result.Error -> it.exception.message
                else -> null
            }
        }
    }
}